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
import org.ut.biolab.medsavant.shared.model.Ontology;
import org.ut.biolab.medsavant.shared.model.OntologyType;

import java.net.URL;

/**
 * Adapter class for mapping Solr documents to Ontology objects.
 */
public class SearcheableOntology {

    private OntologyType type;
    private String name;
    private URL oboURL;
    private URL mappingURL;

    private Ontology ontology;

    public SearcheableOntology() { }

    public SearcheableOntology(Ontology ontology) {
        this.ontology = ontology;
    }

    @Field("type")
    public void setType(OntologyType type) {
        this.type = type;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("obo_url")
    public void setOboURL(URL oboURL) {
        this.oboURL = oboURL;
    }

    @Field("mapping_url")
    public void setMappingURL(URL mappingURL) {
        this.mappingURL = mappingURL;
    }

    public OntologyType getType() {
        return ontology.getType();
    }

    public String getName() {
        return ontology.getName();
    }

    public URL getOboURL() {
        return ontology.getOBOURL();
    }

    public URL getMappingURL() {
        return ontology.getMappingURL();
    }

    public Ontology getOntology() {
        this.ontology = new Ontology(type, name,oboURL,mappingURL);
        return this.ontology;
    }
}
