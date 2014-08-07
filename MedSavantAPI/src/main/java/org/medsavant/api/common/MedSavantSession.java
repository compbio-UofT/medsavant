/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medsavant.api.common;

/**
 * An interface for sessions.
 *
 * @author jim
 */
public interface MedSavantSession {

    public String getSessionId();

    public MedSavantUser getUser();

    public MedSavantProject getProject();
    
    public String getDatabaseName();

}
