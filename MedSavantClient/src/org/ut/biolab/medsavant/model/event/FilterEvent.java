/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.model.event;

import org.ut.biolab.medsavant.model.Filter;


/**
 * Little event which is sent around when our filter list has changed.
 *
 * @author tarkvara
 */
public final class FilterEvent {
    public enum Type { ADDED, REMOVED, MODIFIED };

    private final Type type;
    private final Filter filter;

    public FilterEvent(Type t, Filter f) {
        type = t;
        filter = f;
    }
    
    public Type getType() {
        return type;
    }
    
    public Filter getFilter() {
        return filter;
    }
}
