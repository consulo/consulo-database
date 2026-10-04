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

import com.mongodb.MongoConfigurationException;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoOperationTimeoutException;
import com.mongodb.MongoSecurityException;
import com.mongodb.MongoServerException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.MongoTimeoutException;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoFailError;
import org.jspecify.annotations.Nullable;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Turns exceptions into the {@link MongoFailError} the IDE receives. Messages are driver and server messages, which never contain
 * a password: the driver prints a credential as {@code password=<hidden>}, and a connection string with a password never
 * reaches the driver (see {@link MongoClientSettingsFactory}).
 *
 * @author VISTALL
 * @since 2026-10-04
 */
final class MongoErrors {
    // server error codes
    private static final int BAD_VALUE = 2;
    private static final int FAILED_TO_PARSE = 9;
    private static final int UNAUTHORIZED = 13;
    private static final int MAX_TIME_MS_EXPIRED = 50;

    private MongoErrors() {
    }

    static MongoFailError fail(MongoErrorKind kind, String message) {
        return new MongoFailError(message, "", kind);
    }

    static MongoFailError fail(MongoErrorKind kind, String message, Throwable cause) {
        MongoFailError error = new MongoFailError(message, stackTrace(cause), kind);
        @Nullable Integer serverCode = getServerCode(cause);
        if (serverCode != null) {
            error.setServerCode(serverCode);
        }
        return error;
    }

    static MongoFailError toFailError(Throwable e) {
        if (e instanceof MongoFailError failError) {
            return failError;
        }
        return fail(getKind(e), getMessage(e), e);
    }

    static boolean isUnauthorized(MongoException e) {
        return e.getCode() == UNAUTHORIZED;
    }

    static String getMessage(Throwable e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getName() : message;
    }

    private static MongoErrorKind getKind(Throwable e) {
        if (findCause(e, MongoSecurityException.class) != null) {
            return MongoErrorKind.AUTHENTICATION;
        }

        @Nullable Integer serverCode = getServerCode(e);
        if (e instanceof MongoExecutionTimeoutException
            || e instanceof MongoOperationTimeoutException
            || e instanceof MongoSocketReadTimeoutException
            || isCode(serverCode, MAX_TIME_MS_EXPIRED)) {
            return MongoErrorKind.TIMEOUT;
        }

        if (isCode(serverCode, UNAUTHORIZED)) {
            return MongoErrorKind.UNAUTHORIZED;
        }

        if (isCode(serverCode, BAD_VALUE) || isCode(serverCode, FAILED_TO_PARSE)) {
            return MongoErrorKind.INVALID_QUERY;
        }

        // server selection gave up, or the socket failed
        if (e instanceof MongoTimeoutException || e instanceof MongoSocketException) {
            return MongoErrorKind.CONNECTION;
        }

        // for example the DNS lookup of a mongodb+srv string
        if (e instanceof MongoConfigurationException) {
            return MongoErrorKind.INVALID_SETTINGS;
        }

        if (e instanceof MongoServerException) {
            return MongoErrorKind.SERVER;
        }
        return MongoErrorKind.INTERNAL;
    }

    private static @Nullable Integer getServerCode(Throwable e) {
        if (e instanceof MongoServerException serverException) {
            return serverException.getCode();
        }
        if (e instanceof MongoExecutionTimeoutException timeoutException) {
            return timeoutException.getCode();
        }
        return null;
    }

    private static boolean isCode(@Nullable Integer serverCode, int code) {
        return serverCode != null && serverCode == code;
    }

    private static <T extends Throwable> @Nullable T findCause(Throwable e, Class<T> type) {
        @Nullable Throwable current = e;
        // bounded: a cause chain may loop
        for (int i = 0; current != null && i < 32; i++) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    static String stackTrace(Throwable e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(writer)) {
            e.printStackTrace(printWriter);
        }
        return writer.toString();
    }
}
