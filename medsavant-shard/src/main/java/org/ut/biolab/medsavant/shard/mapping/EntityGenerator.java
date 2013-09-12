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
 * @author <a href="mailto:mirocupak@gmail.com">Miroslav Cupak</a>
 * 
 */
public interface EntityGenerator {

    /**
     * Generates content for a .java file for the given entity.
     * 
     * @return java source
     */
    String getSource();

    /**
     * Compiles and loads a class.
     * 
     * @return class representing the entity
     */
    Class<?> getCompiled();

    /**
     * Compiles a class.
     */
    void compile();

    /**
     * Generates the name of the class to use.
     * 
     * @return class name
     */
    String getClassName();

    /**
     * Generates the package for the class.
     * 
     * @return package name
     */
    String getPackage();

    /**
     * Retrieves fields in the generated class.
     * 
     * @return list of fields
     */
    List<ClassField> getFields();

    /**
     * Changes the fields being generated for the class including get/set
     * methods.
     * 
     * @param fields
     *            fields
     */
    void setFields(List<ClassField> fields);

    /**
     * Generates a new field including get/set methods.
     * 
     * @param field
     *            field to generate
     */
    void addField(ClassField field);
}
