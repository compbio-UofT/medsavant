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

package org.ut.biolab.medsavant.client.view.component;

import java.awt.*;
import javax.swing.*;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FontsMain {
    public static void main(String[] a) {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

        for (Font i : e.getAllFonts()) {
            String name = i.getFontName();

            //if (name.startsWith("HelveticaNeue")) {
                JLabel label = new JLabel(name);

                label.setFont(i.deriveFont(18f));

                contentPane.add(label);
            //}
        }

        JFrame frame = new JFrame("Fonts");
        frame.setContentPane(ViewUtil.getClearBorderlessScrollPane(contentPane));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}