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

package consulo.database.mongo.rt;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import consulo.database.mongo.rt.shared.MongoConnectSettings;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoFailError;
import org.bson.UuidRepresentation;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Builds the driver settings from the {@link MongoConnectSettings} the IDE sends. The password arrives only in that struct, over
 * the loopback connection: it is put into a {@link MongoCredential} and nowhere else.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class MongoClientSettingsFactory {
    private static final String APPLICATION_NAME = "Consulo";

    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 27017;
    static final String DEFAULT_AUTH_SOURCE = "admin";

    private static final String MONGODB_PREFIX = "mongodb://";
    private static final String MONGODB_SRV_PREFIX = "mongodb+srv://";

    /**
     * Applied when the connection string sets no socketTimeoutMS. Operations are bounded by maxTimeMS on the server, this bounds a
     * read from a server which stopped answering - otherwise a runtime thread would wait on it forever.
     */
    private static final long READ_TIMEOUT_MINUTES = 5;

    private MongoClientSettingsFactory() {
    }

    /**
     * Creates a client. It does not wait for a server: background threads of the driver connect.
     *
     * @param forceServerSelectionTimeout see {@link #create}
     */
    static MongoClient createClient(MongoConnectSettings settings, boolean forceServerSelectionTimeout) throws MongoFailError {
        MongoClientSettings clientSettings = create(settings, forceServerSelectionTimeout);
        try {
            return MongoClients.create(clientSettings);
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            // settings which contradict each other
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, MongoErrors.getMessage(e), e);
        }
    }

    /**
     * @param forceServerSelectionTimeout {@code true} for Test Connection: the timeout of the settings applies even when the
     *                                    connection string sets one, so the test gives up quickly
     */
    static MongoClientSettings create(MongoConnectSettings settings, boolean forceServerSelectionTimeout) throws MongoFailError {
        MongoClientSettings.Builder builder = MongoClientSettings.builder();

        @Nullable ConnectionString connectionString = null;
        @Nullable String connectionText = trimToNull(settings.getConnectionString());
        if (connectionText != null) {
            connectionString = parseConnectionString(connectionText);
            builder.applyConnectionString(connectionString);
        }
        else {
            ServerAddress address = createAddress(settings);
            builder.applyToClusterSettings(it -> it.hosts(List.of(address)));
        }

        // a login in the fields wins over a user of the connection string, which can only be one without password (x.509 and alike)
        @Nullable String login = trimToNull(settings.getLogin());
        if (login != null) {
            @Nullable String password = settings.getPassword();
            char[] passwordChars = password == null ? new char[0] : password.toCharArray();
            builder.credential(MongoCredential.createCredential(login, getAuthSource(settings), passwordChars));
        }

        int serverSelectionTimeout = settings.getServerSelectionTimeoutMs();
        boolean stringSetsServerSelectionTimeout = connectionString != null && connectionString.getServerSelectionTimeout() != null;
        if (serverSelectionTimeout > 0 && (forceServerSelectionTimeout || !stringSetsServerSelectionTimeout)) {
            builder.applyToClusterSettings(it -> it.serverSelectionTimeout(serverSelectionTimeout, TimeUnit.MILLISECONDS));
        }

        int connectTimeout = settings.getConnectTimeoutMs();
        if (connectTimeout > 0 && (connectionString == null || connectionString.getConnectTimeout() == null)) {
            builder.applyToSocketSettings(it -> it.connectTimeout(connectTimeout, TimeUnit.MILLISECONDS));
        }

        if (connectionString == null || connectionString.getSocketTimeout() == null) {
            builder.applyToSocketSettings(it -> it.readTimeout(READ_TIMEOUT_MINUTES, TimeUnit.MINUTES));
        }

        if (connectionString == null || connectionString.getApplicationName() == null) {
            builder.applicationName(APPLICATION_NAME);
        }

        builder.uuidRepresentation(UuidRepresentation.STANDARD);
        try {
            return builder.build();
        }
        catch (IllegalArgumentException | IllegalStateException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, MongoErrors.getMessage(e), e);
        }
    }

    static String getAuthSource(MongoConnectSettings settings) {
        @Nullable String authSource = trimToNull(settings.getAuthSource());
        return authSource == null ? DEFAULT_AUTH_SOURCE : authSource;
    }

    /**
     * Refuses a connection string with a password before the driver parses it: a driver message about an invalid string may
     * quote a part of it, and the password must not travel back to the IDE in a message. The rules are a little stricter than
     * the driver, which ends the hosts at the first {@code /}: here the user info is what precedes the last {@code @}, which must
     * come before the first {@code /} and {@code ?}, and a {@code :} in it means a password - even an empty one.
     *
     * @return the error, {@code null} when the string may be given to the driver
     */
    static @Nullable String checkConnectionString(String text) {
        String rest;
        if (text.startsWith(MONGODB_PREFIX)) {
            rest = text.substring(MONGODB_PREFIX.length());
        }
        else if (text.startsWith(MONGODB_SRV_PREFIX)) {
            rest = text.substring(MONGODB_SRV_PREFIX.length());
        }
        else {
            return "The connection string must start with " + MONGODB_PREFIX + " or " + MONGODB_SRV_PREFIX;
        }

        int at = rest.lastIndexOf('@');
        if (at < 0) {
            return null;
        }

        int delimiter = indexOfDelimiter(rest);
        if (delimiter >= 0 && at > delimiter) {
            // an @ after the hosts: a user info holding / or ? which the driver would cut, and quote a part of as a host. An @ in
            // the database name or an option value is refused too - it can be written as %40
            return "Characters such as @, / and ? in the user name, password, database or options of the connection string must be "
                + "URL-encoded (RFC 3986)";
        }

        if (rest.substring(0, at).indexOf(':') >= 0) {
            return "The connection string must not contain a password";
        }
        return null;
    }

    private static int indexOfDelimiter(String rest) {
        int slash = rest.indexOf('/');
        int question = rest.indexOf('?');
        if (slash < 0) {
            return question;
        }
        return question < 0 ? slash : Math.min(slash, question);
    }

    private static ConnectionString parseConnectionString(String text) throws MongoFailError {
        @Nullable String error = checkConnectionString(text);
        if (error != null) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, error);
        }

        ConnectionString connectionString;
        try {
            connectionString = new ConnectionString(text);
        }
        catch (IllegalArgumentException | MongoException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, "Invalid connection string: " + MongoErrors.getMessage(e), e);
        }

        // the check above follows the driver, this is the driver itself
        if (connectionString.getPassword() != null) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, "The connection string must not contain a password");
        }
        return connectionString;
    }

    private static ServerAddress createAddress(MongoConnectSettings settings) throws MongoFailError {
        @Nullable String host = trimToNull(settings.getHost());
        int port = settings.getPort() <= 0 ? DEFAULT_PORT : settings.getPort();
        try {
            return new ServerAddress(host == null ? DEFAULT_HOST : host, port);
        }
        catch (IllegalArgumentException | MongoException e) {
            throw MongoErrors.fail(MongoErrorKind.INVALID_SETTINGS, "Invalid host or port: " + MongoErrors.getMessage(e), e);
        }
    }

    private static @Nullable String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
