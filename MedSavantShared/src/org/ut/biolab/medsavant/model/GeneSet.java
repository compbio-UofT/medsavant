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


/**
 * Class which identifies a particular set of genes (e.g. hg18/RefSeq).
 *
 * @author tarkvara
 */
public class GeneSet implements Serializable {
    /** The associated genome (e.g. hg18) */
    private final String genome;

    /** The type of gene set (e.g. RefSeq) */
    private final String type;
    
    /** The number of genes in this set. */
    private final int size;

    public GeneSet(String genome, String type, int size) {
        this.genome = genome;
        this.type = type;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("%s – %s Genes", genome, type);
    }
    
    public String getReference() {
        return genome;
    }
    
    public String getType() {
        return type;
    }
    
    public int getSize() {
        return size;
    }
}
