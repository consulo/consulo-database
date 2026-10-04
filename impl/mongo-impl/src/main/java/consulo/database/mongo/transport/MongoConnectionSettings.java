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


package consulo.database.mongo.transport;

import consulo.database.datasource.configurable.GenericPropertyKeys;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.configurable.SecureString;
import consulo.database.datasource.model.DataSource;
import consulo.database.datasource.provider.DataSourceConfigurationException;
import consulo.database.mongo.MongoPropertyKeys;
import consulo.database.mongo.localize.MongoLocalize;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.localize.LocalizeValue;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * The connection settings of a MongoDB data source, read from its properties. The password is held only here, in memory, and in
 * the settings {@link #toRuntimeSettings sent} to the runtime process over its loopback connection: it is never put into a
 * connection string, a command line, a log or the {@link #getFingerprint() fingerprint}.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoConnectionSettings {
    private static final String SCHEME = "mongodb://";
    private static final String SRV_SCHEME = "mongodb+srv://";

    /**
     * Test Connection waits in a modal progress - the driver default of 30 seconds would block it far too long.
     */
    private static final int PROBE_SERVER_SELECTION_TIMEOUT_MS = 5_000;
    private static final int SERVER_SELECTION_TIMEOUT_MS = 15_000;
    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final @Nullable String myConnectionString;
    private final String myHost;
    private final int myPort;
    private final @Nullable String myLogin;
    private final String myPassword;
    private final String myAuthSource;
    private final @Nullable String myDriver;
    private final @Nullable String myDriverVersion;

    private MongoConnectionSettings(@Nullable String connectionString,
                                    String host,
                                    int port,
                                    @Nullable String login,
                                    String password,
                                    String authSource,
                                    @Nullable String driver,
                                    @Nullable String driverVersion) {
        myConnectionString = connectionString;
        myHost = host;
        myPort = port;
        myLogin = login;
        myPassword = password;
        myAuthSource = authSource;
        myDriver = driver;
        myDriverVersion = driverVersion;
    }

    public static MongoConnectionSettings of(DataSource dataSource) {
        PropertiesHolder properties = dataSource.getProperties();

        String host = trimToNull(properties.get(GenericPropertyKeys.HOST));
        Integer port = properties.get(GenericPropertyKeys.PORT);
        String authSource = trimToNull(properties.get(MongoPropertyKeys.AUTH_SOURCE));

        return new MongoConnectionSettings(trimToNull(properties.get(MongoPropertyKeys.CONNECTION_STRING)),
            host == null ? "localhost" : host,
            port == null || port <= 0 ? MongoPropertyKeys.DEFAULT_PORT : port,
            trimToNull(properties.get(GenericPropertyKeys.LOGIN)),
            getPassword(dataSource, properties),
            authSource == null ? MongoPropertyKeys.DEFAULT_AUTH_SOURCE : authSource,
            trimToNull(properties.get(GenericPropertyKeys.DRIVER)),
            trimToNull(properties.get(GenericPropertyKeys.DRIVER_VERSION)));
    }

    /**
     * Checks the properties without connecting anywhere.
     */
    public static void validate(PropertiesHolder properties) throws DataSourceConfigurationException {
        String connectionString = trimToNull(properties.get(MongoPropertyKeys.CONNECTION_STRING));
        if (connectionString != null) {
            LocalizeValue error = checkConnectionString(connectionString);
            if (error != null) {
                throw new DataSourceConfigurationException(error);
            }
            return;
        }

        Integer port = properties.get(GenericPropertyKeys.PORT);
        // 0 is the unset value of the key, it means the default port
        if (port != null && (port < 0 || port > 65535)) {
            throw new DataSourceConfigurationException(MongoLocalize.errorPortInvalid());
        }
    }

    /**
     * Checks a connection string without connecting anywhere, DNS included. This is a syntactic check of the parts which matter
     * here - the scheme, the hosts and above all the user information: the property is stored as plain text, so a password in it is
     * an error. The driver checks everything else (options, SRV rules) in the runtime process, reported on connecting.
     * <p>
     * A user information containing an unescaped {@code /} or {@code ?} cannot be told apart from a path or options followed by an
     * {@code @} - and may hide a password - so an {@code @} after the first {@code /} or {@code ?} is refused, including one in a
     * database name or an option value, which must be URL-encoded then.
     *
     * @return the error, or {@code null} when the text is empty or a connection string without a password
     */
    public static @Nullable LocalizeValue checkConnectionString(@Nullable String text) {
        String connectionString = trimToNull(text);
        if (connectionString == null) {
            return null;
        }

        String rest;
        if (connectionString.startsWith(SCHEME)) {
            rest = connectionString.substring(SCHEME.length());
        }
        else if (connectionString.startsWith(SRV_SCHEME)) {
            rest = connectionString.substring(SRV_SCHEME.length());
        }
        else {
            return MongoLocalize.errorConnectionStringScheme();
        }

        int slash = rest.indexOf('/');
        int question = rest.indexOf('?');
        // the end of the user information and the hosts: the first '/' or '?'
        int authorityEnd = slash < 0 ? question : question < 0 ? slash : Math.min(slash, question);
        int at = rest.lastIndexOf('@');

        String hosts;
        if (at >= 0) {
            if (authorityEnd >= 0 && at > authorityEnd) {
                return MongoLocalize.errorConnectionStringUserinfo();
            }

            // as the driver does: any ':' in the user information starts a password, an empty one too, while an encoded %3A does not
            if (rest.substring(0, at).indexOf(':') >= 0) {
                return MongoLocalize.errorConnectionStringPassword();
            }
            if (at == 0) {
                return MongoLocalize.errorConnectionStringHost();
            }
            hosts = authorityEnd < 0 ? rest.substring(at + 1) : rest.substring(at + 1, authorityEnd);
        }
        else {
            hosts = authorityEnd < 0 ? rest : rest.substring(0, authorityEnd);
        }

        if (hosts.isEmpty()) {
            return MongoLocalize.errorConnectionStringHost();
        }
        if (authorityEnd >= 0 && authorityEnd == question) {
            return MongoLocalize.errorConnectionStringOptions();
        }
        return null;
    }

    /**
     * @return the database to authenticate against, which is also where Test Connection sends its ping
     */
    public String getAuthSource() {
        return myAuthSource;
    }

    /**
     * @return the connection string, {@code null} when host and port are used
     */
    public @Nullable String getConnectionString() {
        return myConnectionString;
    }

    /**
     * Builds the settings sent to the runtime process. The result holds the password and its generated {@code toString()} prints
     * it: it must never be logged, printed or put into a message.
     *
     * @param probe {@code true} for the one-shot runtime of Test Connection, which gives up quickly when no server answers
     */
    public MongoConnectSettings toRuntimeSettings(boolean probe) {
        MongoConnectSettings settings = new MongoConnectSettings();
        if (myConnectionString != null) {
            settings.setConnectionString(myConnectionString);
        }
        settings.setHost(myHost);
        settings.setPort(myPort);

        // a login in the fields wins over a user of the connection string, which can only be one without password (x.509 and alike)
        String login = myLogin;
        if (login != null) {
            settings.setLogin(login);
            settings.setPassword(myPassword);
        }
        settings.setAuthSource(myAuthSource);
        settings.setServerSelectionTimeoutMs(probe ? PROBE_SERVER_SELECTION_TIMEOUT_MS : SERVER_SELECTION_TIMEOUT_MS);
        settings.setConnectTimeoutMs(CONNECT_TIMEOUT_MS);
        return settings;
    }

    /**
     * A digest of every setting the runtime connects with, the password and the selected driver and version included. A cached runtime
     * whose fingerprint differs was connected with other settings and must not be reused.
     */
    public String getFingerprint() {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        update(digest, myConnectionString);
        update(digest, myHost);
        update(digest, String.valueOf(myPort));
        update(digest, myLogin);
        update(digest, myPassword);
        update(digest, myAuthSource);
        update(digest, myDriver);
        update(digest, myDriverVersion);
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void update(MessageDigest digest, @Nullable String value) {
        if (value == null) {
            digest.update((byte) 0);
            return;
        }

        digest.update((byte) 1);
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update(intToBytes(bytes.length));
        digest.update(bytes);
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    private static String getPassword(DataSource dataSource, PropertiesHolder properties) {
        SecureString password = properties.get(GenericPropertyKeys.PASSWORD);
        if (password == null) {
            return "";
        }

        String value = password.getValue(dataSource);
        return value == null ? "" : value;
    }

    private static @Nullable String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public String toString() {
        // never the password, and not the connection string either - it is user text which could hold one
        return "MongoConnectionSettings{" + (myConnectionString != null ? "connection string" : myHost + ":" + myPort) + "}";
    }
}
