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
import consulo.project.Project;
import consulo.util.concurrent.AsyncResult;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
@ExtensionImpl(id = "fake", order = "last")
public class FakeDataSourceTransport implements DataSourceTransport<FakeResult>
{
	@Override
	public boolean accept(@Nonnull DataSource dataSource)
	{
		return true;
	}

	@Override
	public void testConnection(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<Void> result)
	{
		result.setDone();
	}

	@Override
	public void loadInitialData(@Nonnull ProgressIndicator indicator, @Nonnull Project project, @Nonnull DataSource dataSource, @Nonnull AsyncResult<FakeResult> result)
	{
		result.rejectWithThrowable(new UnsupportedOperationException());
	}

	@Nonnull
	@Override
	public Class<FakeResult> getStateClass()
	{
		return FakeResult.class;
	}

	@Override
	public int getStateVersion()
	{
		return 1;
	}
}
