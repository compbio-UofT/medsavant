/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mfiume
 */
class DefaultColorScheme implements ColorScheme {

    private static ColorScheme instance;
    private final ArrayList<Color> colors;

    private DefaultColorScheme() {
        colors = new ArrayList<Color>();

        colors.add(new Color(87,61,218));
        colors.add(new Color(61,126,218));
        colors.add(new Color(61,179,218));
        colors.add(new Color(61,218,61));
        colors.add(new Color(194,237,66));
        colors.add(new Color(255,255,71));
        colors.add(new Color(255,218,71));
        colors.add(new Color(255,181,71));
        colors.add(new Color(255,145,71));
        colors.add(new Color(255,71,71));
        colors.add(new Color(227,63,159));
        colors.add(new Color(166,61,218));

        /*
        colors.add(new Color(115, 7, 7));
        colors.add(new Color(217, 59, 24));
        colors.add(new Color(242, 125, 22));
        colors.add(new Color(242, 163, 15));

        colors.add(new Color(124, 241, 222));
        colors.add(new Color(55, 192, 159));
        colors.add(new Color(66, 137, 122));
        colors.add(new Color(32, 81, 100));
        colors.add(new Color(0, 30, 71));
         *
         */

    }

    static ColorScheme getInstance() {
        if (instance == null) {
            instance = new DefaultColorScheme();
        }
        return instance;
    }

    public Color getColor(int index) {
        return colors.get(index % colors.size());
    }

    public Color getColor(int index, int total) {
        return getColor((int) Math.floor(((double) index / total)*colors.size()));
    }
}
