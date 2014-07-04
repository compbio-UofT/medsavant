/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.medsavant.api.common;

import java.util.Dictionary;

/**
 *
 * @author jim
 */
public interface MedSavantServerComponent {
    /**
     * 
     * @return A globally unique identifier identifying the component.
     */
    public String getComponentID();
    
    
    /**
     * 
     * @return The "user friendly" name of this component.
     */
    public String getComponentName();
    
    public void configure(Dictionary dict) throws InvalidConfigurationException;
    
    public void configure(String key, Object val) throws InvalidConfigurationException;
    
    public void setServerContext(MedSavantServerContext context) throws InvalidConfigurationException;
           
}
