/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.toronto.cs.medsavant.medsavant.app.api.appcomm;

import javax.swing.ImageIcon;

/**
 * Handler for communication events
 * @author mfiume
 * @param <AppCommEvent> App communication event that the handler handles
 */
public interface AppCommHandler<AppCommEvent> {
    
    public String getHandlerName();
    public ImageIcon getHandlerIcon();
    
    public void handleCommEvent(AppCommEvent value);
    
}
