/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.filter.pathways;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author AndrewBrook
 */
public class Gene {

    public static enum geneType{ ENTREZ, ENSEMBL };

    private String chromosome;
    private int start = -1;
    private int end = -1;
    private String name;
    private String description;
    private String id;
    private geneType type;

    public Gene(geneType type, String id){
        this.type = type;
        this.id = id;
    }

    public Gene(String chrom, String start, String end){
        this.chromosome = chrom;
        this.start = Integer.parseInt(start);
        this.end = Integer.parseInt(end);
    }

    public void setChromosome(String chrom){
        this.chromosome = chrom;
    }

    public void setStart(String start){
        this.start = Integer.parseInt(start);
    }

    public void setEnd(String end){
        this.end = Integer.parseInt(end);
    }

    public void setName(String name){
        this.name = name;
    }

    public void setDescription(String desc){
        this.description = desc;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setGeneType(geneType type){
        this.type = type;
    }

    public String getChromosome(){
        return this.chromosome;
    }

    public int getStart(){
        return this.start;
    }

    public int getEnd(){
        return this.end;
    }

    public String getName(){
        return this.name;
    }

    public String getDescription(){
        return this.description;
    }

    public String getId(){
        return this.id;
    }

    public geneType getGeneType(){
        return this.type;
    }

}
