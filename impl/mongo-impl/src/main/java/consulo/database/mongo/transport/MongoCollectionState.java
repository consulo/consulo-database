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

import consulo.util.xml.serializer.annotation.Transient;

/**
 * A collection, view or time series collection of a MongoDB database.
 *
 * @author VISTALL
 * @since 2026-10-04
 */
public class MongoCollectionState {
    public static final String TYPE_COLLECTION = "collection";
    public static final String TYPE_VIEW = "view";
    public static final String TYPE_TIMESERIES = "timeseries";

    private String myName = "";

    private String myType = TYPE_COLLECTION;

    public MongoCollectionState() {
    }

    public MongoCollectionState(String name, String type) {
        myName = name;
        myType = type;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    /**
     * @return {@link #TYPE_COLLECTION}, {@link #TYPE_VIEW}, {@link #TYPE_TIMESERIES} - or another type a newer server reports
     */
    public String getType() {
        return myType;
    }

    public void setType(String type) {
        myType = type;
    }

    @Transient
    public boolean isView() {
        return TYPE_VIEW.equals(myType);
    }

    @Transient
    public boolean isSystem() {
        return myName.startsWith("system.");
    }
}
