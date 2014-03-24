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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 * A component that has a dark "hero unit" on top, which displays title
 * information, and a "content unit", which can display information pertaining
 * to the title.
 *
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
        this.add(heroUnit, BorderLayout.NORTH);
        this.add(contentUnit, BorderLayout.CENTER);
    }

    public void setContent(JPanel p) {
        contentUnit.removeAll();
        contentUnit.add(p, BorderLayout.CENTER);
    }

    public HeroUnit getHeroUnit() {
        return heroUnit;
    }

    public static class HeroUnit extends JPanel {

        private JLabel heroLabel;
        private Image backgroundImage;

        public HeroUnit() {
            initView();
            //backgroundImage = IconFactory.getInstance().getIcon("/img/bg/bg-fade.png").getImage();
        }

        private void initView() {
            this.setLayout(new MigLayout("hmin 100, filly, insets 30 50 30 50"));
            //this.setBackground(new Color(61,61,61));

            heroLabel = ViewUtil.getLargeGrayLabel("");

            this.add(heroLabel);

        }

        public void setHeroTitle(String title) {
            this.heroLabel.setText(title);
        }

        public void paintComponent(Graphics g) {

            //System.out.println("Drawing image for dash background");
            int width = this.getWidth();
            int height = this.getHeight();

            g.setColor(ViewUtil.getLightGrayBackgroundColor());
            g.fillRect(0, 0, width, height);
            
            if (backgroundImage != null) {
                BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = resizedImage.createGraphics();

                g.drawImage(backgroundImage, 0, 0, null);
            }
            //g.drawImage(resizedImage, 0, 0, null);
            return;
        }
    }
}
