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

import org.jspecify.annotations.Nullable;

/**
 * Cell value markers of MongoDB results.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public final class MongoValues {
    /**
     * The field is not present in the document - unlike a field holding {@code null}.
     * <p>
     * A stand-in for {@code consulo.ui.grid.ReservedCellValue.UNSET} until the data grid is published; it is replaced by it then.
     */
    public static final Object MISSING = Missing.INSTANCE;

    private enum Missing {
        INSTANCE;

        @Override
        public String toString() {
            return "<missing>";
        }
    }

    private MongoValues() {
    }

    public static boolean isMissing(@Nullable Object value) {
        return value == MISSING;
    }
}
