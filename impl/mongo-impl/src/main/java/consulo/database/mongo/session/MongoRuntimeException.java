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


package consulo.database.mongo.session;

import consulo.database.mongo.localize.MongoLocalize;
import consulo.database.mongo.rt.shared.MongoErrorKind;
import consulo.database.mongo.rt.shared.MongoFailError;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import org.apache.thrift.TException;
import org.jspecify.annotations.Nullable;

/**
 * A failure of the MongoDB runtime process or of a call to it. The message is shown as is in notifications and editors: it never
 * holds credentials - the runtime keeps them out of its error messages, and the IDE never puts settings into one.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoRuntimeException extends RuntimeException {
    private static final Logger LOG = Logger.getInstance(MongoRuntimeException.class);

    private final @Nullable MongoErrorKind myKind;
    private final @Nullable Integer myServerCode;

    public MongoRuntimeException(LocalizeValue message) {
        this(message.get(), null, null, null);
    }

    public MongoRuntimeException(LocalizeValue message, @Nullable Throwable cause) {
        this(message.get(), cause, null, null);
    }

    private MongoRuntimeException(String message,
                                  @Nullable Throwable cause,
                                  @Nullable MongoErrorKind kind,
                                  @Nullable Integer serverCode) {
        super(message, cause);
        myKind = kind;
        myServerCode = serverCode;
    }

    /**
     * @param error an error the runtime declared - the connection it came on is still fine
     */
    public static MongoRuntimeException of(MongoFailError error) {
        MongoErrorKind kind = error.getKind();
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            message = kind == null ? MongoErrorKind.INTERNAL.name() : kind.name();
        }

        String trace = error.getTrace();
        if (trace != null && LOG.isDebugEnabled()) {
            LOG.debug("MongoDB runtime error " + kind + ": " + trace);
        }

        Integer serverCode = error.isSetServerCode() ? error.getServerCode() : null;
        return new MongoRuntimeException(message, null, kind, serverCode);
    }

    /**
     * @param error a transport failure: the socket broke, timed out or the process ended
     */
    public static MongoRuntimeException lost(TException error) {
        String reason = error.getMessage();
        if (reason == null || reason.isBlank()) {
            reason = error.getClass().getSimpleName();
        }
        return new MongoRuntimeException(MongoLocalize.errorRuntimeLost(reason).get(), error, null, null);
    }

    /**
     * @return the kind the runtime reported, {@code null} for a failure of the process or the connection itself
     */
    public @Nullable MongoErrorKind getKind() {
        return myKind;
    }

    /**
     * @return the server error code, for example 13 (Unauthorized), when the server reported one
     */
    public @Nullable Integer getServerCode() {
        return myServerCode;
    }
}
