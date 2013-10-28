/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
