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
package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;

/**
 * Adapter class for mapping Solr documents to OntologyTerm objects.
 */
public class SearcheableOntologyTerm {

    private OntologyType ontology;
    private String id;
    private String name;
    private String def;
    private String[] altIDs;
    private String[] parentIDs;

    private String[] genes;

    private OntologyTerm term;

    public SearcheableOntologyTerm() {   }

    public SearcheableOntologyTerm(OntologyTerm term) {
        this.term = term;
    }

    @Field("genes")
    public void setGenes(String[] genes) {
        this.genes = genes;
    }

    @Field("parent_ids")
    public void setParentIDs(String[] parentIDs) {
        this.parentIDs = parentIDs;
    }

    @Field("alt_ids")
    public void setAltIDs(String[] altIDs) {
        this.altIDs = altIDs;
    }

    @Field("def")
    public void setDef(String def) {
        this.def = def;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("id")
    public void setId(String id) {
        this.id = id;
    }

    @Field("type")
    public void setOntology(OntologyType ontology) {
        this.ontology = ontology;
    }

    public OntologyTerm getTerm() {
        this.term = new OntologyTerm(ontology,id,name,def,altIDs,parentIDs, genes);
        return term;
    }

    public OntologyType getOntology() {
        return term.getOntology();
    }

    public String getId() {
        return term.getID();
    }

    public String getName() {
        return term.getName();
    }

    public String getDef() {
        return term.getDef();
    }

    public String[] getAltIDs() {
        return term.getAltIDs();
    }

    public String[] getParentIDs() {
        return term.getParentIDs();
    }

    public String[] getGenes() {
        return genes;
    }
}
