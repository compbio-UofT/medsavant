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
package org.ut.biolab.medsavant.shard.common;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Generic provider of classes' meta information.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public class MetaEntity<T> {

    private T entity;

    public MetaEntity(Class<T> clazz) {
        // follows the singleton pattern
        entity = getInstanceOfT(clazz);
    }

    private T getInstanceOfT(Class<T> clazz) {
        T instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            // should never happen
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // should never happen
            e.printStackTrace();
        }

        return instance;
    }

    /**
     * Retrieve attributes of the class T.
     */
    public String[] getAttributeNames() {
        // use reflection to get the list
        String atts = ReflectionToStringBuilder.toString(entity, EntityStyle.getInstance());

        // parse attributes, expects null values for entity fields
        atts = atts.substring(1, atts.length() - 1) + ",";
        return atts.split("=<null>,");
    }

    /**
     * Retrieves number of attributes on an object.
     * 
     * @return number of attributes
     */
    public int getAttributesCount() {
        return getAttributeNames().length;
    }
}
