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

import java.util.Map;


/**
 *
 * @author Andrew
 */
public class FilterState {
    
    private final String name;
    private final String filterID;
    private final Map<String, String> values;
    private final Filter.Type type;
    
    public FilterState(Filter.Type type, String name, String id, Map<String, String> values) {
        this.name = name;
        this.filterID = id;
        this.values = values;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFilterID() {
        return filterID;
    }
    
    public Map<String, String> getValues() {
        return values;
    }
    
    public Filter.Type getType() {
        return type;
    }
    
    public String generateXML() {
        String s = "\t\t<filter name=\"" + name + "\" id=\"" + filterID + "\" type=\"" + type + "\" >\n";
        for(String key : values.keySet()) {
            s += "\t\t\t<param key=\"" + key + "\" value=\"" + values.get(key) + "\" />\n";
        }
        s += "\t\t</filter>";
        return s;
    }
    
}
