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

import consulo.database.mongo.rt.shared.MongoExecutor;
import org.apache.thrift.TException;
import org.jspecify.annotations.Nullable;

/**
 * A call to the runtime process, run by {@link MongoSession#execute} on a connection which passed the handshake.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@FunctionalInterface
public interface MongoCall<T extends @Nullable Object> {
    T run(MongoExecutor.Client client) throws TException;
}
