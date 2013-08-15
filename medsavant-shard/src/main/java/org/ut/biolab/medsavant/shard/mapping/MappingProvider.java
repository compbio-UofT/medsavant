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
package org.ut.biolab.medsavant.shard.mapping;

import java.util.List;

/**
 * Provider of dynamic Hibernate mappings.
 * 
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public interface MappingProvider {

    /**
     * Retrieves the mapping in place.
     * 
     * @return mapping in XML
     */
    String getMapping();

    /**
     * Returns the name of the entity the mapping is for.
     * 
     * @return entity name
     */
    String getClassName();

    /**
     * Change mapping to the given class.
     * 
     * @param clazz
     *            class name
     */
    void setClassName(String pack, String clazz);

    /**
     * Retrieves the name of table the entity is mapped to.
     * 
     * @return table name
     */
    String getTable();

    /**
     * Changes the table the entity is mapped to.
     * 
     * @param table
     *            table name
     */
    void setTable(String table);

    /**
     * Retrieves the list of active properties in the mapping.
     * 
     * @return properties
     */
    List<MappingProperty> getProperties();

    /**
     * Changes the properties in the mapping.
     * 
     * @param properties
     *            new properties
     */
    void setProperties(List<MappingProperty> properties);

    /**
     * Adds a property to the mapping.
     * 
     * @param propertyName
     *            name of the property
     * @param columnName
     *            name of the column to map to
     * @param propertyType
     *            type of the property
     */
    void addProperty(String propertyName, String columnName, String propertyType);
}
