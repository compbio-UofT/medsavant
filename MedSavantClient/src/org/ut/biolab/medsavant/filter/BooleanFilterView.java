/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.filter;

/**
 * Boolean filter as special case of string list with two items.
 *
 * @author tarkvara
 */
public class BooleanFilterView extends StringListFilterView {

    public BooleanFilterView(FilterState state, int queryID) throws Exception {
        super(state, queryID);
    }

    public BooleanFilterView(WhichTable t, String colName, int queryID, String alias) throws Exception {
        super(t, colName, queryID, alias, true);
    }
}
