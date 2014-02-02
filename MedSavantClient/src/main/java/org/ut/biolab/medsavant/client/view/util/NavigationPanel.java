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
package org.ut.biolab.medsavant.client.view.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;

/**
 *
 * @author mfiume
 */
public class NavigationPanel extends JPanel {

    private final JLabel titleLabel;
    //private final ImagePanel downCaret;

    public NavigationPanel() {
        this.setOpaque(false);
        this.setLayout(new MigLayout("hidemode 3, insets 0"));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("Helvetica Neue", Font.PLAIN, 18));
        titleLabel.setVisible(false);
        //titleLabel.setForeground(new Color(96,110,130));//new Color(67,138,218));
        titleLabel.setForeground(new Color(64, 64, 64));

        this.add(titleLabel);
        //this.add(downCaret = new ImagePanel(IconFactory.getInstance().getIcon(IconFactory.ICON_ROOT + "upside-down-caret.png").getImage(), 9, 22), "gapx 0");

        //downCaret.setVisible(false);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
        titleLabel.setVisible(true);
    }

    public static void main(String[] argv) {

        JFrame f = new JFrame();
        NavigationPanel bc;
        f.add(bc = new NavigationPanel());
        bc.setTitle("MedSavant");

        f.pack();
        f.show();
    }

    public void setTitleClickAction(MouseListener mouseListener) {

        //downCaret.setVisible(true);

        for (MouseListener l : titleLabel.getMouseListeners()) {
            titleLabel.removeMouseListener(l);
            //downCaret.removeMouseListener(l);
            this.removeMouseListener(l);
        }
        titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        //downCaret.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        ViewUtil.adjustForegroundColorOnMouseover(titleLabel,-25);

        titleLabel.addMouseListener(mouseListener);
        this.addMouseListener(mouseListener);
    }
}
