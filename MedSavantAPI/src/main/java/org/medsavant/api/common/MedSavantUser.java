/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.util.List;

/**
 *
 * @author jim
 */
public interface MedSavantUser {
    public String getUsername();
    public String getEmail();    
    public List<MedSavantUserRole> getRoles();
}
