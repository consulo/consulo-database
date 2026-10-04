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

import com.dslplatform.json.CompiledJson;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * The {@code info.json} of an install: one version of a driver - its id, the provider which installed it, its name, version, start and
 * files. {@code source} is {@code predefined} for a driver from a bundled {@link DriverJson description}.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
@CompiledJson
public class DriverInfoJson {
    public @Nullable String id;

    public @Nullable String provider;

    public @Nullable String name;

    public @Nullable String version;

    public @Nullable String source;

    public @Nullable DriverStartJson start;

    public @Nullable List<ArtifactJson> artifacts;
}
