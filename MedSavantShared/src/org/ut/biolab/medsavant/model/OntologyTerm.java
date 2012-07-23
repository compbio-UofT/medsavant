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

package org.ut.biolab.medsavant.model;

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
public class OntologyTerm implements Serializable {
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
                return new URL("http://http://omim.org/entry/" + id.substring(4));
            default:
                return null;
        }
    }
}
