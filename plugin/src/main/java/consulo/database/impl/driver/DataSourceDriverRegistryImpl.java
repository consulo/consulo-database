/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.database.impl.driver;

import com.dslplatform.json.DslJson;
import consulo.annotation.component.ServiceImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.component.ProcessCanceledException;
import consulo.container.boot.ContainerPathManager;
import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.driver.DataSourceDriver;
import consulo.database.datasource.driver.DataSourceDriverRegistry;
import consulo.database.datasource.driver.DataSourceDriverStart;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceProvider;
import consulo.database.impl.localize.DatabaseLocalize;
import consulo.ide.util.DownloadUtil;
import consulo.logging.Logger;
import consulo.util.io.DigestUtil;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Keeps the predefined drivers in {@code <system>/datasource-drivers/<uuid>/}, one directory per installed version of a driver id. The
 * {@code info.json} of an install is written last, so its presence means the install is complete; the directory name carries no
 * meaning, the driver id and version are read from {@code info.json}. Directories whose name is not a UUID are ignored.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@Singleton
@ServiceImpl
public class DataSourceDriverRegistryImpl implements DataSourceDriverRegistry {
    private static final Logger LOG = Logger.getInstance(DataSourceDriverRegistryImpl.class);

    private static final String DRIVERS_DIRECTORY = "datasource-drivers";
    private static final String BUNDLED_DIRECTORY = "/META-INF/datasource-drivers/";
    private static final String INFO_FILE = "info.json";
    private static final String TEMP_SUFFIX = ".tmp";
    private static final String DOWNLOAD_SUFFIX = ".download";
    private static final String SOURCE_PREDEFINED = "predefined";
    private static final int BUFFER_SIZE = 64 * 1024;

    private final DslJson<Object> myJson =
        new DslJson<>(new DslJson.Settings<>().includeServiceLoader(DataSourceDriverRegistryImpl.class.getClassLoader()));

    private final Path myRoot;

    /**
     * Complete installs by driver id and version, from the scan and from installs of this run.
     */
    private final Map<DriverKey, InstalledDriver> myInstalled = new ConcurrentHashMap<>();

    /**
     * Whether the installs were scanned - on the first request which needs them.
     */
    private volatile boolean myScanned;

    private final Object myScanLock = new Object();

    /**
     * Bundled descriptions by driver id.
     */
    private final Map<String, DriverJson> myBundled = new ConcurrentHashMap<>();

    /**
     * Drivers installed or verified in this run - the fast path of resolve.
     */
    private final Map<DriverKey, DataSourceDriver> myVerified = new ConcurrentHashMap<>();

    /**
     * Selections already warned about falling back to the preferred driver or to the latest version, once per data source and
     * selection.
     */
    private final Set<String> myFallbackWarnings = ConcurrentHashMap.newKeySet();

    /**
     * One install or repair at a time in the IDE; application-wide, so it also covers a data source open in two projects.
     */
    private final Lock myDownloadLock = new ReentrantLock();

    @Inject
    public DataSourceDriverRegistryImpl() {
        myRoot = Path.of(ContainerPathManager.get().getSystemPath(), DRIVERS_DIRECTORY);
    }

    /**
     * @return the bundled description of a driver id the provider lists; cached, so the settings dialog reads it without IO
     * @throws IllegalStateException when the description is missing or invalid - a packaging error
     */
    @Nonnull
    public DriverJson getDriver(@Nonnull DataSourceProvider provider, @Nonnull String driverId) {
        return myBundled.computeIfAbsent(driverId, id -> readBundled(provider, id));
    }

    /**
     * @return the installed versions of a driver id, newest first - a version the description no longer lists is still reused when a
     * data source selects it
     */
    @Nonnull
    public List<String> getInstalledVersions(@Nonnull String driverId) {
        ensureScanned();

        List<String> versions = new ArrayList<>();
        for (DriverKey key : myInstalled.keySet()) {
            if (key.id().equals(driverId)) {
                versions.add(key.version());
            }
        }
        versions.sort((v1, v2) -> StringUtil.compareVersionNumbers(v2, v1));
        return versions;
    }

    @Nonnull
    @Override
    public DataSourceDriver resolve(@Nonnull ProgressIndicator indicator, @Nonnull DataSource dataSource) throws IOException {
        DataSourceProvider provider = dataSource.getProvider();
        List<String> driverIds = provider.getDriverIds();
        if (driverIds.isEmpty()) {
            throw new IllegalStateException("The data source provider " + provider.getId() + " lists no driver");
        }

        ensureScanned();

        PropertiesHolder properties = dataSource.getProperties();
        String selectedId = properties.get(GenericPropertyKeys.DRIVER);
        String driverId = chooseDriverId(dataSource, selectedId, driverIds);
        DriverJson driver = getDriver(provider, driverId);

        // a version belongs to the driver it was selected with
        boolean ownVersion = selectedId == null || selectedId.equals(driverId);
        String selectedVersion = ownVersion ? properties.get(GenericPropertyKeys.DRIVER_VERSION) : null;
        String version = chooseVersion(dataSource, driverId, driver, selectedVersion);

        DriverKey key = new DriverKey(driverId, version);
        DataSourceDriver verified = myVerified.get(key);
        if (verified != null) {
            return verified;
        }

        lock(indicator);
        try {
            verified = myVerified.get(key);
            if (verified != null) {
                return verified;
            }

            InstalledDriver installed = myInstalled.get(key);
            DataSourceDriver result =
                installed != null ? verify(indicator, installed) : install(indicator, provider, driverId, driver, version);
            myVerified.put(key, result);
            return result;
        }
        finally {
            myDownloadLock.unlock();
        }
    }

    /**
     * @return the selected driver id when the provider lists it, otherwise the preferred one; an id the provider does not list - a
     * retired one for example - is logged once per data source and id
     */
    @Nonnull
    private String chooseDriverId(@Nonnull DataSource dataSource, @Nullable String selected, @Nonnull List<String> driverIds) {
        if (selected == null) {
            return driverIds.get(0);
        }

        if (driverIds.contains(selected)) {
            return selected;
        }

        if (myFallbackWarnings.add(dataSource.getId() + "/" + selected)) {
            LOG.warn("Data source '" + dataSource.getName() + "' selects the driver '" + selected +
                "' which its provider does not list, '" + driverIds.get(0) + "' is used");
        }
        return driverIds.get(0);
    }

    /**
     * @return the selected version when the description lists it or it is installed - an old one is reused - otherwise the latest
     * version; a version which is neither is logged once per data source and version
     */
    @Nonnull
    private String chooseVersion(@Nonnull DataSource dataSource,
                                 @Nonnull String driverId,
                                 @Nonnull DriverJson driver,
                                 @Nullable String selected) {
        String latest = driver.drivers.get(0).version;
        if (selected == null) {
            return latest;
        }

        if (findVersion(driver, selected) != null || myInstalled.containsKey(new DriverKey(driverId, selected))) {
            return selected;
        }

        if (myFallbackWarnings.add(dataSource.getId() + "/" + driverId + "/" + selected)) {
            LOG.warn("Data source '" + dataSource.getName() + "' selects the version " + selected + " of the driver '" + driverId +
                "' which is neither listed nor installed, the latest version " + latest + " is used");
        }
        return latest;
    }

    @Nullable
    private static DriverVersionJson findVersion(@Nonnull DriverJson driver, @Nonnull String version) {
        for (DriverVersionJson candidate : driver.drivers) {
            if (version.equals(candidate.version)) {
                return candidate;
            }
        }
        return null;
    }

    private void lock(@Nonnull ProgressIndicator indicator) {
        try {
            while (!myDownloadLock.tryLock(100, TimeUnit.MILLISECONDS)) {
                indicator.checkCanceled();
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProcessCanceledException();
        }
    }

    /**
     * Checks every file of an install against its SHA-256 and downloads a missing or damaged one again. The info.json is not
     * rewritten: the content behind a driver id never changes.
     */
    @Nonnull
    private DataSourceDriver verify(@Nonnull ProgressIndicator indicator, @Nonnull InstalledDriver installed) throws IOException {
        for (ArtifactJson artifact : installed.info().artifacts) {
            indicator.checkCanceled();

            Path file = installed.directory().resolve(artifact.fileName);
            if (!Files.isRegularFile(file) || !artifact.sha256.equals(sha256(file))) {
                download(indicator, installed.directory(), artifact);
            }
        }
        return toDriver(installed.directory(), installed.info());
    }

    /**
     * Installs a version of a driver into a new directory. The info.json is written last; when anything fails or is cancelled, the
     * whole directory is deleted.
     */
    @Nonnull
    private DataSourceDriver install(@Nonnull ProgressIndicator indicator,
                                     @Nonnull DataSourceProvider provider,
                                     @Nonnull String driverId,
                                     @Nonnull DriverJson driver,
                                     @Nonnull String version) throws IOException {
        // chooseVersion returns a listed or an installed version, and an installed one is verified instead
        DriverVersionJson bundled = findVersion(driver, version);
        if (bundled == null) {
            throw new IllegalStateException("The driver " + driverId + " lists no version " + version);
        }

        Path directory = myRoot.resolve(UUID.randomUUID().toString());
        Files.createDirectories(directory);
        boolean installed = false;
        try {
            for (ArtifactJson artifact : bundled.artifacts) {
                indicator.checkCanceled();
                download(indicator, directory, artifact);
            }

            DriverInfoJson info = toInstalledInfo(driverId, provider.getId(), driver, bundled);
            writeInfo(directory, info);
            myInstalled.put(new DriverKey(driverId, version), new InstalledDriver(directory, info));
            installed = true;
            return toDriver(directory, info);
        }
        finally {
            if (!installed) {
                FileUtil.delete(directory);
            }
        }
    }

    @Nonnull
    private static DriverInfoJson toInstalledInfo(@Nonnull String driverId,
                                                  @Nonnull String providerId,
                                                  @Nonnull DriverJson driver,
                                                  @Nonnull DriverVersionJson version) {
        DriverInfoJson info = new DriverInfoJson();
        info.id = driverId;
        info.provider = providerId;
        info.name = driver.name;
        info.version = version.version;
        info.source = SOURCE_PREDEFINED;
        info.start = driver.start;
        info.artifacts = version.artifacts;
        return info;
    }

    private void writeInfo(@Nonnull Path directory, @Nonnull DriverInfoJson info) throws IOException {
        Path temp = directory.resolve(INFO_FILE + TEMP_SUFFIX);
        try (OutputStream stream = Files.newOutputStream(temp)) {
            myJson.serialize(info, stream);
        }
        move(temp, directory.resolve(INFO_FILE));
    }

    /**
     * Downloads a file next to its target and moves it into place only once it is complete and matches its SHA-256.
     */
    private static void download(@Nonnull ProgressIndicator indicator, @Nonnull Path directory, @Nonnull ArtifactJson artifact)
        throws IOException {
        indicator.setText(DatabaseLocalize.progressDownloadingDriver(artifact.fileName));

        Path temp = directory.resolve(artifact.fileName + DOWNLOAD_SUFFIX);
        try {
            DownloadUtil.downloadContentToFile(indicator, artifact.url, temp.toFile());

            if (!artifact.sha256.equals(sha256(temp))) {
                throw new IOException(DatabaseLocalize.errorDriverChecksum(artifact.fileName).get());
            }

            move(temp, directory.resolve(artifact.fileName));
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    private static void move(@Nonnull Path source, @Nonnull Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Nonnull
    private static String sha256(@Nonnull Path file) throws IOException {
        MessageDigest digest = DigestUtil.sha256();

        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream stream = Files.newInputStream(file)) {
            int read;
            while ((read = stream.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    @Nonnull
    private static DataSourceDriver toDriver(@Nonnull Path directory, @Nonnull DriverInfoJson info) {
        List<Path> files = new ArrayList<>(info.artifacts.size());
        for (ArtifactJson artifact : info.artifacts) {
            files.add(directory.resolve(artifact.fileName));
        }

        DataSourceDriverStart start = new DataSourceDriverStart(info.start.kind, info.start.agent);
        return new DataSourceDriver(info.id, info.name, info.version, start, files);
    }

    /**
     * Reads the description of a driver id from the provider module. {@code META-INF} is not a package, so module encapsulation does not
     * hide it; the lookup searches the whole class path of the plugin, so a driver id must be unique within the plugin, and copies of one
     * id listed by two providers must be identical.
     */
    @Nonnull
    private DriverJson readBundled(@Nonnull DataSourceProvider provider, @Nonnull String driverId) {
        String path = BUNDLED_DIRECTORY + driverId + ".json";
        try (InputStream stream = provider.getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("No driver description " + path + " for " + provider.getClass().getName());
            }

            DriverJson driver = myJson.deserialize(DriverJson.class, stream);
            if (!isValid(driver)) {
                throw new IllegalStateException("Invalid driver description " + path);
            }
            return driver;
        }
        catch (IOException e) {
            throw new IllegalStateException("Cannot read driver description " + path, e);
        }
    }

    @Nullable
    private DriverInfoJson readInstalled(@Nonnull Path infoFile) {
        try (InputStream stream = Files.newInputStream(infoFile)) {
            DriverInfoJson info = myJson.deserialize(DriverInfoJson.class, stream);
            if (!isValid(info)) {
                LOG.warn("Skipping the driver install " + infoFile.getParent() + ": its " + INFO_FILE + " is not valid");
                return null;
            }
            return info;
        }
        catch (IOException e) {
            LOG.warn("Skipping the driver install " + infoFile.getParent() + ": its " + INFO_FILE + " cannot be read", e);
            return null;
        }
    }

    private static boolean isValid(@Nullable DriverJson driver) {
        if (driver == null || StringUtil.isEmpty(driver.name) || !isValid(driver.start)) {
            return false;
        }

        List<DriverVersionJson> versions = driver.drivers;
        if (versions == null || versions.isEmpty()) {
            return false;
        }

        for (DriverVersionJson version : versions) {
            if (version == null || StringUtil.isEmpty(version.version) || !isValid(version.artifacts)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValid(@Nullable DriverInfoJson info) {
        if (info == null || StringUtil.isEmpty(info.id) || StringUtil.isEmpty(info.name) || StringUtil.isEmpty(info.version)) {
            return false;
        }
        return isValid(info.start) && isValid(info.artifacts);
    }

    private static boolean isValid(@Nullable DriverStartJson start) {
        return start != null && !StringUtil.isEmpty(start.kind) && !StringUtil.isEmpty(start.agent);
    }

    private static boolean isValid(@Nullable List<ArtifactJson> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) {
            return false;
        }

        for (ArtifactJson artifact : artifacts) {
            if (artifact == null ||
                StringUtil.isEmpty(artifact.fileName) ||
                StringUtil.isEmpty(artifact.url) ||
                StringUtil.isEmpty(artifact.sha256)) {
                return false;
            }
        }
        return true;
    }

    private void ensureScanned() {
        if (myScanned) {
            return;
        }

        synchronized (myScanLock) {
            if (!myScanned) {
                scan();
                myScanned = true;
            }
        }
    }

    /**
     * Indexes the complete installs. Cleanup is only needed after the IDE was killed during an install: a UUID directory without
     * info.json is deleted, and stray downloads of an interrupted repair are deleted. An info.json which cannot be read is logged, and
     * its directory is skipped and kept.
     */
    private void scan() {
        try {
            Files.createDirectories(myRoot);

            try (DirectoryStream<Path> children = Files.newDirectoryStream(myRoot)) {
                for (Path child : children) {
                    if (!Files.isDirectory(child) || !isUuid(child.getFileName().toString())) {
                        continue;
                    }

                    try {
                        scanInstall(child);
                    }
                    catch (IOException e) {
                        LOG.warn("Cannot scan the driver install " + child, e);
                    }
                }
            }
        }
        catch (IOException e) {
            LOG.error("Cannot scan the driver directory " + myRoot, e);
        }
    }

    private void scanInstall(@Nonnull Path directory) throws IOException {
        Path infoFile = directory.resolve(INFO_FILE);
        if (!Files.isRegularFile(infoFile)) {
            FileUtil.delete(directory);
            return;
        }

        DriverInfoJson info = readInstalled(infoFile);
        if (info == null) {
            return;
        }

        try (DirectoryStream<Path> downloads = Files.newDirectoryStream(directory, "*" + DOWNLOAD_SUFFIX)) {
            for (Path download : downloads) {
                Files.deleteIfExists(download);
            }
        }

        myInstalled.putIfAbsent(new DriverKey(info.id, info.version), new InstalledDriver(directory, info));
    }

    /**
     * An install is one version of a driver id.
     */
    private record DriverKey(@Nonnull String id, @Nonnull String version) {
    }

    private static boolean isUuid(@Nonnull String name) {
        try {
            return UUID.fromString(name).toString().equals(name);
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }
}
