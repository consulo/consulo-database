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

package consulo.database.datasource.driver;

import jakarta.annotation.Nonnull;

/**
 * How a driver is started: {@code kind} names the mechanism, {@code agent} names which agent of that kind runs the driver. Both are
 * kept as the driver description writes them - a kind or agent this plugin does not know still parses, and the session which cannot
 * start it reports it.
 *
 * @param kind  the start mechanism, for example {@link #JAVA_RT}
 * @param agent the agent of that kind; for {@link #JAVA_RT} the runtime module of the plugin: {@code jdbc} or {@code mongo}
 * @author VISTALL
 * @since 2026-10-04
 */
public record DataSourceDriverStart(@Nonnull String kind, @Nonnull String agent) {
    /**
     * A JVM on the Java runtime of the IDE runs the runtime module of the plugin which {@code agent} names, with the driver files on
     * its class path.
     */
    public static final String JAVA_RT = "java-rt";
}
