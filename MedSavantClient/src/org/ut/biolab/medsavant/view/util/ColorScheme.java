/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.util;

import java.awt.Color;

/**
 *
 * @author mfiume
 */
interface ColorScheme {
    
    public Color getColor(int index);
    
    public Color getColor(int index, int of);
}
