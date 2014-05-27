/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package org.ut.biolab.medsavant.client.view.util.list;

import java.awt.Color;

/**
 *
 * @author mfiume
 */
public class DarkNiceListColorScheme implements NiceListColorScheme {

    private final Color backgroundColor = new Color(56, 56, 56);
    private final Color selectedColor = new Color(45,45,45);
    private final Color unselectedColor = backgroundColor;//new Color(23,23,23);
    private final Color selectedFontColor = new Color(255, 255, 255);
    private final Color unselectedFontColor = new Color(230, 230, 230);
    private final Color borderColor = new Color(42, 42, 42);
    
    public DarkNiceListColorScheme() {
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getSelectedColor() {
        return selectedColor;
    }

    public Color getUnselectedColor() {
        return unselectedColor;
    }

    public Color getSelectedFontColor() {
        return selectedFontColor;
    }

    public Color getUnselectedFontColor() {
        return unselectedFontColor;
    }

    public Color getBorderColor() {
        return borderColor;
    }
    
    
}
