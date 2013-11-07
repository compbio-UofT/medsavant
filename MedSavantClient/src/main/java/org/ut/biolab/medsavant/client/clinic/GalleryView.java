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
package org.ut.biolab.medsavant.client.clinic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.component.WrapLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GalleryView extends JPanel {

    private JPanel carousel;
    private int itemSize = 128;
    private final String title;
    private final JPanel menuCard;
    private final JPanel content;
    private final JLabel titleLabel;
    private final JButton menuButton;
    private final JPanel footer;

    public GalleryView(String title,String backButtonText,String bottomText) {
        this.setLayout(new BorderLayout());
        this.setBackground(Color.white);
        this.title = title;

        JPanel banner = ViewUtil.getTertiaryBannerPanel();

        titleLabel = ViewUtil.getTitleLabel(title);

        menuButton = new JButton(backButtonText);
        menuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                goToMenu();
            }
        });
        banner.add(titleLabel);

        JPanel left = ViewUtil.getClearPanel();
        fixSizeTo(left, 100, 25);
        ViewUtil.applyHorizontalBoxLayout(left);
        left.add(menuButton);

        JPanel right = ViewUtil.getClearPanel();
        fixSizeTo(right, 100, 25);

        banner.add(left);
        banner.add(Box.createHorizontalGlue());
        banner.add(titleLabel);
        banner.add(Box.createHorizontalGlue());
        banner.add(right);

        content = ViewUtil.getClearPanel();
        content.setLayout(new BorderLayout());

        this.add(banner, BorderLayout.NORTH);
        this.add(content, BorderLayout.CENTER);

        footer = ViewUtil.centerHorizontally(new JLabel(bottomText));
        footer.setBorder(ViewUtil.getMediumBorder());

        this.add(footer,BorderLayout.SOUTH);

        menuCard = getMenuCard();

        goToMenu();

    }

    public void setMenuHeroPanel(JPanel heroPanel) {
        menuCard.add(ViewUtil.centerHorizontally(heroPanel),BorderLayout.NORTH);
    }

    private JPanel getMenuCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.white);
        p.setLayout(new BorderLayout());
        //ViewUtil.applyVerticalBoxLayout(p);

        carousel = ViewUtil.getClearPanel();

        //p.add(Box.createVerticalGlue());
        p.add(ViewUtil.getClearBorderlessScrollPane(carousel),BorderLayout.CENTER);
        //p.add(Box.createVerticalGlue());

        return p;
    }

    public void setGalleryItems(List<GalleryItem> galleryItems) {

        carousel.removeAll();
        carousel.setVisible(false);

        carousel.setLayout(new WrapLayout(WrapLayout.CENTER,10,5));
        for (final GalleryItem g : galleryItems) {

            ImageIcon icon = g.getIcon();
            icon = resizeIconTo(icon, itemSize);
            JButton b = ViewUtil.getIconButton(icon);
            carousel.add(ViewUtil.subTextComponent(b, g.getName(), 16));
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    setContentTo(g.getPanel(), g.getName());
                }
            });
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        carousel.setVisible(true);
        carousel.getParent().invalidate();
        carousel.invalidate();
        carousel.updateUI();
    }

    private ImageIcon resizeIconTo(ImageIcon icon, int itemSize) {
        Image img = icon.getImage();
        Image newimg = img.getScaledInstance(itemSize, itemSize, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(newimg);
    }

    private void setContentTo(JPanel p, String title) {

        menuButton.setVisible(p != menuCard);
        footer.setVisible(p == menuCard);

        this.content.removeAll();
        this.content.add(p, BorderLayout.CENTER);
        titleLabel.setText(title);
        content.updateUI();
    }

    private void goToMenu() {
        setContentTo(menuCard, title);
    }

    private void fixSizeTo(JPanel p, int w, int h) {
        Dimension d = new Dimension(w, h);
        p.setMinimumSize(d);
        p.setPreferredSize(d);
        p.setMaximumSize(d);
    }
}
