/*
 * Copyright 2013-2020 consulo.io
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

package consulo.database.datasource.transport;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.progress.ProgressIndicator;
import consulo.database.datasource.model.DataSource;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.concurrent.AsyncResult;
import org.jspecify.annotations.NullMarked;

/**
 * Fallback transport for data sources whose provider has no transport. Every operation is rejected.
 *
 * @author VISTALL
 * @since 2020-08-16
 */
@NullMarked
@ExtensionImpl(id = "fake", order = "last")
public class FakeDataSourceTransport implements DataSourceTransport<FakeResult> {
    @Override
    public boolean accept(DataSource dataSource) {
        return true;
    }

    @Override
    public void testConnection(ProgressIndicator indicator, Project project, DataSource dataSource, AsyncResult<Void> result) {
        // rejectWithThrowable, not reject(String): only a throwable reaches doWhenRejectedWithThrowable listeners
        result.rejectWithThrowable(noTransportError(dataSource));
    }

    @Override
    public void loadInitialData(ProgressIndicator indicator, Project project, DataSource dataSource, AsyncResult<FakeResult> result) {
        result.rejectWithThrowable(noTransportError(dataSource));
    }

    @Override
    public Class<FakeResult> getStateClass() {
        return FakeResult.class;
    }

    @Override
    public int getStateVersion() {
        return 1;
    }

    private static UnsupportedOperationException noTransportError(DataSource dataSource) {
        LocalizeValue message = LocalizeValue.localizeTODO("No transport for " + dataSource.getProvider().getName().get());
        return new UnsupportedOperationException(message.get());
    }
}
