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

package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;
import java.net.URL;


/**
 * Provides information about an ontology and where it gets its data from.
 *
 * @author tarkvara
 */
public class Ontology implements Serializable {
    private final OntologyType type;
    private final String name;
    private final URL oboURL;
    private final URL mappingURL;

    public Ontology(OntologyType t, String n, URL obo, URL mapping) {
        type = t;
        name = n;
        oboURL = obo;
        mappingURL = mapping;
    }

    @Override
    public String toString() {
        return name;
    }

    public OntologyType getType() {
        return type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the oboURL
     */
    public URL getOBOURL() {
        return oboURL;
    }

    /**
     * @return the mappingURL
     */
    public URL getMappingURL() {
        return mappingURL;
    }
}
