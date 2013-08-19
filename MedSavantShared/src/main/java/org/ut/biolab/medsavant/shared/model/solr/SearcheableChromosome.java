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
import org.ut.biolab.medsavant.shared.model.Chromosome;

/**
 * Adapter class for mapping Solr documents to Chromosome objects.
 */
public class SearcheableChromosome {

    private Chromosome chromosome;

    private int centromerePos;
    private String name;
    private int length;

    public SearcheableChromosome() { }

    public SearcheableChromosome(Chromosome chromosome) {
        this.chromosome = chromosome;
    }

    @Field("centromere_pos")
    public void setCentromerePos(int centromerePos) {
        this.centromerePos = centromerePos;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("length")
    public void setLength(int length) {
        this.length = length;
    }

    public Chromosome getChromosome() {
        this.chromosome = new Chromosome(name,centromerePos,length);
        return chromosome;
    }

    public int getCentromerePos() {
        return chromosome.getCentromerePos();
    }

    public String getName() {
        return chromosome.getName();
    }

    public int getLength() {
        return chromosome.getLength();
    }


}
