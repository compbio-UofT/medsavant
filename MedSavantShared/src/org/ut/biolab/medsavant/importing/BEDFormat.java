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
package org.ut.biolab.medsavant.importing;

import java.io.Serializable;
import java.util.TreeMap;

/**
 *
 * @author mfiume
 */
public class BEDFormat extends FileFormat implements Serializable {

    @Override
    public String getName() {
        return "BED";
    }

    @Override
    public TreeMap<Integer, String> getFieldNumberToFieldNameMap() {
        TreeMap<Integer,String> map = new TreeMap<Integer,String>();
        map.put(0, "Chromosome");
        map.put(1, "Start");
        map.put(2, "End");
        map.put(3, "Name");
        return map;
    }

    @Override
    public TreeMap<Integer, Class> getFieldNumberToClassMap() {
        TreeMap<Integer,Class> map = new TreeMap<Integer,Class>();
        map.put(0, String.class);
        map.put(1, String.class); // todo: 1 and 2 should be ints!!
        map.put(2, String.class);
        map.put(3, String.class);
        return map;
    }
    
    
    
}
