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

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * A component that has a dark "hero unit" on top, which displays
 * title information, and a "content unit", which can display information
 * pertaining to the title.
 * @author mfiume
 */
public class HeroPanel extends JPanel {
    
    private HeroUnit heroUnit;
    private JPanel contentUnit;
    
    public HeroPanel() {
        initView();
    }

    private void initView() {
        this.setBackground(ViewUtil.getDefaultBackgroundColor());
        
        heroUnit = new HeroUnit();
        
        contentUnit = ViewUtil.getClearPanel();
        contentUnit.setLayout(new BorderLayout());
        
        this.setLayout(new BorderLayout());
        this.add(heroUnit,BorderLayout.NORTH);
        this.add(contentUnit,BorderLayout.CENTER);
    }
    
    public void setContent(JPanel p) {
        contentUnit.removeAll();
        contentUnit.add(p,BorderLayout.CENTER);
    }

    public HeroUnit getHeroUnit() {
        return heroUnit;
    }

    public static class HeroUnit extends JPanel {
        
        private JLabel heroLabel;

        public HeroUnit() {
            initView();
            
        }
        
        private void initView() {
           this.setLayout(new MigLayout("hmin 150, filly"));
           this.setBackground(new Color(61,61,61));
           
           heroLabel = new JLabel();
           heroLabel.setFont(FontFactory.getGeneralFont().deriveFont(30f));
           heroLabel.setForeground(Color.white);
           
           this.add(heroLabel);
           
        }
        
        public void setHeroTitle(String title) {
            this.heroLabel.setText(title);
        }
    }
    
}
