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

import consulo.component.persist.PersistentStateComponent;
import consulo.util.xml.serializer.XmlSerializerUtil;

import java.util.Map;
import java.util.TreeMap;

/**
 * The cached structure of a MongoDB data source: databases and their collections. Field sampling is not stored here - it is done
 * when a collection is opened.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public class MongoState implements PersistentStateComponent<MongoState> {
    private Map<String, MongoDatabaseState> myDatabases = new TreeMap<>();

    public Map<String, MongoDatabaseState> getDatabases() {
        return myDatabases;
    }

    public void setDatabases(Map<String, MongoDatabaseState> databases) {
        myDatabases = databases;
    }

    public void addDatabase(MongoDatabaseState databaseState) {
        myDatabases.put(databaseState.getName(), databaseState);
    }

    @Override
    public MongoState getState() {
        return this;
    }

    @Override
    public void loadState(MongoState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
