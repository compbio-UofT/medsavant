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

package org.ut.biolab.medsavant.api;

import java.util.List;


/**
 * Gives MedSavant filter plugins access to the information stored within a <c>FilterState</c> object.
 *
 * @author tarkvara
 */
public interface FilterStateAdapter {

    /**
     * Retrieve the value of a unique element.
     */
    public String getOneValue(String key);

    /**
     * Store an element which will be unique.  For instance, any filter will have a single &lt;table&gt; element.
     */
    public void putOneValue(String key, Object val);

    /**
     * Retrieve a list of non-unique elements.
     */
    public List<String> getValues(String key);

    /**
     * Store a list of elements which are not assumed to be unique.
     */
    public void putValues(String key, List<String> vals);
    
    /**
     * Generate an XML representation of the filter's state.
     */
    public String generateXML();
}
