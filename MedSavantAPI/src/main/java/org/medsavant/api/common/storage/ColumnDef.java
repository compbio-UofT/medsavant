/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common.storage;

import java.io.Serializable;

/**
 *
 * @author jim
 */
public interface ColumnDef extends Serializable {

    int getColumnLength();

    String getColumnName();

    int getColumnScale();

    ColumnType getColumnType();
    
}
