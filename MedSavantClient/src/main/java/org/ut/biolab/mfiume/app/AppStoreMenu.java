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
package org.ut.biolab.mfiume.app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GrayFilter;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class AppStoreMenu extends JPanel {

    private final AppStorePage[] pages;
    private final AppStoreViewManager viewManager;
    private HashMap<AppStorePage, AbstractButton> pageToButtonMap;

    AppStoreMenu(AppStorePage[] pages, AppStoreViewManager viewManager) {
        this.pages = pages;
        this.viewManager = viewManager;
        this.setBackground(new Color(222, 222, 222));
        this.setBorder(BorderFactory.createEmptyBorder(10, AppStoreViewManager.SIDEPADDING, 10, AppStoreViewManager.SIDEPADDING));
        initView();
    }

    public static JToggleButton getTogglableIconButton(ImageIcon icon) {

        final ImageIcon selectedIcon = icon;
        //final ImageIcon unselectedIcon = new AlphaImageIcon(icon, 0.3F);
        final ImageIcon unselectedIcon = new ImageIcon(GrayFilter.createDisabledImage(icon.getImage()));

        final JToggleButton button = new JToggleButton(icon);
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorder(null);
        ViewUtil.makeSmall(button);

        final Runnable setSelected = new Runnable() {
            @Override
            public void run() {
                button.setIcon(selectedIcon);
                button.setFocusable(false);
                button.setContentAreaFilled(false);
                button.setBorder(null);
            }
        };

        final Runnable setUnselected = new Runnable() {
            @Override
            public void run() {
                button.setIcon(unselectedIcon);
                button.setFocusable(false);
                button.setContentAreaFilled(false);
                button.setBorder(null);
            }
        };

        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                if (button.getModel().isSelected()) {
                    setSelected.run();
                } else {
                    setUnselected.run();
                }
            }
        });

        setUnselected.run();

        return button;
    }

    public static JComponent subTextComponent(JComponent c, String subtext) {
        int width = c.getPreferredSize().width;
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);
        p.add(ViewUtil.centerHorizontally(c));
        JLabel s = new JLabel(subtext);
        s.setForeground(Color.darkGray);
        ViewUtil.makeSmall(s);
        p.add(ViewUtil.centerHorizontally(ViewUtil.clear(s)));

        FontMetrics fm = s.getFontMetrics(s.getFont());
        p.setMaximumSize(new Dimension(Math.max(width, fm.stringWidth(subtext)), 23 + c.getPreferredSize().height));
        return p;
    }

    private void initView() {

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        ButtonGroup g = new ButtonGroup();

        pageToButtonMap = new HashMap<AppStorePage,AbstractButton>();

        //this.add(Box.createHorizontalGlue());
        int counter = 0;
        for (final AppStorePage p : pages) {

            counter++;

            JPanel buttonContainer = new JPanel();
            buttonContainer.setOpaque(false);
            JToggleButton autoGrayScaleButton = getTogglableIconButton(p.getIcon());
            buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.X_AXIS));
            buttonContainer.add(subTextComponent(autoGrayScaleButton, p.getName()));

            autoGrayScaleButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    viewManager.switchToPage(p,false);
                }
            });
            g.add(autoGrayScaleButton);
            this.add(buttonContainer);

            pageToButtonMap.put(p, autoGrayScaleButton);

            /*
             JToggleButton button = new JToggleButton(p.getName());

             g.add(button);

             button.setIcon(p.getIcon());
             button.setSelectedIcon(p.getPressedIcon());


             button.addActionListener(new ActionListener() {

             @Override
             public void actionPerformed(ActionEvent ae) {
             viewManager.switchToPage(p);
             }

             });


             frgrt
             button.putClientProperty("JButton.buttonType", "textured");
             AbstractButton macButton = MacButtonFactory.makeUnifiedToolBarButton(button);
             button.setPressedIcon(p.getPressedIcon());
             this.add(button);
             */


            if (counter < pages.length) {
                this.add(Box.createHorizontalStrut(10));
            }
        }

        this.add(Box.createHorizontalGlue());
    }

    void pageChangedTo(AppStorePage page) {
        pageToButtonMap.get(page).setSelected(true);
    }
}
