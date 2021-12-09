/*
 * Copyright 2013-2021 consulo.io
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

package consulo.database.datasource.jdbc.transport;

import consulo.database.datasource.transport.DataSourceTransportResult;
import consulo.database.jdbc.rt.shared.JdbcQueryResult;

/**
 * @author VISTALL
 * @since 09/12/2021
 */
public class JdbcQueryResultWrapper implements DataSourceTransportResult
{
	private final JdbcQueryResult myJdbcQueryResult;
	private final long myAllRowsCount;

	public JdbcQueryResultWrapper(JdbcQueryResult jdbcQueryResult, long allRowsCount)
	{
		myJdbcQueryResult = jdbcQueryResult;
		myAllRowsCount = allRowsCount;
	}

	public JdbcQueryResult getJdbcQueryResult()
	{
		return myJdbcQueryResult;
	}

	@Override
	public long getAllRowsCount()
	{
		return myAllRowsCount;
	}

	@Override
	public long getRowsCount()
	{
		return myJdbcQueryResult.getRowsSize();
	}
}
