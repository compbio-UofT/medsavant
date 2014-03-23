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

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author mfiume
 */
public class StandardFixedWidthAppPanel extends JPanel {
    
    private final JPanel content;
    private final JLabel titleLabel;
    private final boolean initialized;
    
    public StandardFixedWidthAppPanel() {
        this(true);
    }
    
    public StandardFixedWidthAppPanel(String title) {
        this(title,true);
    }
    
    public StandardFixedWidthAppPanel(boolean doesScroll) {
        this(null,doesScroll);
    }
    
    public StandardFixedWidthAppPanel(String title,boolean doesScroll) {
        
        content = ViewUtil.getClearPanel();
        content.setLayout(new MigLayout("insets 0, fillx, hidemode 3"));
        
        JPanel fixedWidth = ViewUtil.getDefaultFixedWidthPanel(content);
        
        StandardAppContainer sac = new StandardAppContainer(fixedWidth,doesScroll);
        sac.setBackground(ViewUtil.getLightGrayBackgroundColor());
        
        titleLabel = ViewUtil.getLargeGrayLabel("");
        titleLabel.setVisible(false);
        if (title != null) {
            setTitle(title);
        }
        
        content.add(titleLabel,"wrap");
        
        this.setLayout(new BorderLayout());
        this.add(sac,BorderLayout.CENTER);
        
        initialized = true;
    }
    
    @Override
    public void setLayout(LayoutManager mgr) {
        if (initialized) {
            throw new UnsupportedOperationException("Not allowed to change layout for this component");
        } else {
            super.setLayout(mgr);
        }
    }
    
    public JPanel addBlock() {
        return addBlock(null);
    }

    public JPanel addBlock(String blockTitle) {
        JPanel p = ViewUtil.getWhiteLineBorderedPanel();
        JPanel canvas = ViewUtil.getClearPanel();
        canvas.setLayout(new MigLayout("insets 0"));
        p.setLayout(new MigLayout("fillx, wrap"));
        
        if (blockTitle != null) {
            JLabel l = new JLabel(blockTitle);
            l.setText(blockTitle);
            l.setFont(ViewUtil.getMediumTitleFont());
            p.add(l);
        }
        
        p.add(canvas,"width 100%");
        content.add(p,"width 100%, wrap");
        return canvas;
    }

    public void setTitle(String string) {
        titleLabel.setText(string);
        ViewUtil.ellipsizeLabel(titleLabel, 800);
        titleLabel.setVisible(true);
    }
    
}
