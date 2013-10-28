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
package org.ut.biolab.mfiume.app.component;

import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class TitleBar extends JPanel {

    public TitleBar(String title) {

        this.setOpaque(false);
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JLabel titleLabel = new JLabel(title);

        Font font = titleLabel.getFont();
        Font boldFont = new Font(font.getFontName(), Font.BOLD, font.getSize()+5);
        titleLabel.setFont(boldFont);

        //this.add(Box.createHorizontalStrut(5));
        this.add(titleLabel);
        this.add(Box.createHorizontalGlue());
    }
}
