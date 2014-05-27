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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Encapsulates all information about a single ontology term.
 *
 * @author tarkvara
 */
public class OntologyTerm implements Serializable, Comparable<OntologyTerm> {
    private final OntologyType ontology;
    private final String id;
    private final String name;
    private final String def;
    private final String[] altIDs;
    private final String[] parentIDs;

    public OntologyTerm(OntologyType ontology, String id, String name, String def, String[] altIDs, String[] parentIDs) {
        this.ontology = ontology;
        this.id = id;
        this.name = name;
        this.def = def;
        this.altIDs = altIDs;
        this.parentIDs = parentIDs;
    }

    @Override
    public int compareTo(OntologyTerm t) {
        int c = ontology.name().compareTo(t.ontology.name());        
        if(c == 0){ //same ontology type, sort by id.
            return id.compareTo(t.id);
        }else{
            return c;
        }        
    }
    

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (ontology != null ? ontology.hashCode() : 0);
        hash = 41 * hash + (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof OntologyTerm) {
            OntologyTerm t = (OntologyTerm)o;
            if (id == null || t.id == null || ontology == null || t.ontology == null) {
                System.out.println("How did I get null?");
            }
            return id.equals(t.id) && ontology.equals(t.ontology);
        }
        return false;
    }

    @Override
    public String toString() {
        return id + " " + name;
    }

    public OntologyType getOntology() {
        return ontology;
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDef() {
        return def;
    }

    public String[] getAltIDs() {
        return altIDs;
    }

    public String[] getParentIDs() {
        return parentIDs;
    }

    public OntologyTerm[] getChildren(OntologyTerm[] allTerms) {
        List<OntologyTerm> result = new ArrayList<OntologyTerm>();
        for (OntologyTerm t: allTerms) {
            if (t.parentIDs != null) {
                for (String parent: t.parentIDs) {
                    if (parent.equals(id)) {
                        result.add(t);
                        break;
                    }
                }
            }
        }
        return result.toArray(new OntologyTerm[0]);
    }

    /**
     * All terms are assumed to have a web-page somewhere.
     */
    public URL getInfoURL() throws MalformedURLException {
        switch (ontology) {
            case GO:
                return new URL("http://amigo.geneontology.org/cgi-bin/amigo/term_details?term=" + id);
            case HPO:
                return new URL("http://www.human-phenotype-ontology.org/hpoweb/showterm?id=" + id);
            case OMIM:
                return new URL("http://omim.org/entry/" + id.substring(4));
            default:
                return null;
        }
    }
}
