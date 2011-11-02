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
package org.ut.biolab.medsavant.db.format;

/**
 *
 * @author Andrew
 */
public class AnnotationField extends CustomField {
    
    public static enum Category {PATIENT, GENOTYPE, PHENOTYPE, GENOME_COORDS, PLUGIN}
    
    public static String categoryToString(Category cat){
        switch(cat){
            case PATIENT:
                return "Patient & Phenotype";
            case GENOTYPE:
                return "Genotype";
            case PHENOTYPE:
                return "Variant Annotation";
            case GENOME_COORDS:
                return "Genomic Coordinates";
            case PLUGIN:
                return "Plugins";
            default:
                return "undefined";
        }
    }
    
    private Category category;
    
    public AnnotationField(String name, String type, boolean filterable, String alias, String description){
        this(name, type, filterable, alias, description, Category.PHENOTYPE);
    }
    
    public AnnotationField(String name, String type, boolean filterable, String alias, String description, Category category){
        super(name, type, filterable, alias, description);
        this.category = category;
    }
  
    public Category getCategory() {
        return category;
    }
    
}
