/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.annotation;

/**
 *
 * @author jim
 */
public interface TabixAnnotationField{
    //<field name="info" type="VARCHAR(1023)" filterable="true" alias="clinvar_20140211, info" description="clinvar_20140211, info" />

    public String getFieldName();  //must match that given in tabix file.
    public String getFieldTypeAsString();
    public Class getFieldTypeAsClass();
    public boolean isFilterable();
    public String getAlias();
    public String getDescription();             
        
}

