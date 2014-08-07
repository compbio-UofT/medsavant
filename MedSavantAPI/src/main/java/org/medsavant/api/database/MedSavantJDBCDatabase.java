/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.database;

/**
 *
 * @author jim
 */
public interface MedSavantJDBCDatabase {
    public String getConnectionString(String dbName);
}
