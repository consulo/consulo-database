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

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link SimpleLogger} instances.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class SimpleLoggerFactory implements ILoggerFactory
{

	ConcurrentMap<String, Logger> loggerMap;

	public SimpleLoggerFactory()
	{
		loggerMap = new ConcurrentHashMap<String, Logger>();
		SimpleLogger.lazyInit();
	}

	/**
	 * Return an appropriate {@link SimpleLogger} instance by name.
	 */
	public Logger getLogger(String name)
	{
		Logger simpleLogger = loggerMap.get(name);
		if(simpleLogger != null)
		{
			return simpleLogger;
		}
		else
		{
			Logger newInstance = new SimpleLogger(name);
			Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
			return oldInstance == null ? newInstance : oldInstance;
		}
	}

	/**
	 * Clear the internal logger cache.
	 *
	 * This method is intended to be called by classes (in the same package) for
	 * testing purposes. This method is internal. It can be modified, renamed or
	 * removed at any time without notice.
	 *
	 * You are strongly discouraged from calling this method in production code.
	 */
	void reset()
	{
		loggerMap.clear();
	}
}