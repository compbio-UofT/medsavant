/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.variantstorage;

import org.medsavant.api.common.storage.ColumnDef;

/**
 *
 * @author jim
 */
public interface VariantField {

    public Class getColumnClass();

    public ColumnDef getColumnDef();

    public String getAlias();

    public boolean isUnique();

}
