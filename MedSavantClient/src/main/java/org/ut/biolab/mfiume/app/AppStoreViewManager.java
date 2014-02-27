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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *
 * @author mfiume
 */
public class AppStoreViewManager {
    private final JPanel content;
    private AppStorePage previousPage;
    private AppStoreMenu menu;

    public AppStoreViewManager() {
        this.content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBackground(Color.white);

        int padding = 25;
        content.setBorder(BorderFactory.createEmptyBorder(padding, SIDEPADDING, padding, SIDEPADDING));
    }

    public static final int SIDEPADDING = 50;

    public void switchToPage(AppStorePage page) {
        switchToPage(page,true);
    }

    public void switchToPage(AppStorePage page, boolean programmatically) {

        content.removeAll();

        content.add(page.getView(),BorderLayout.CENTER);
        content.updateUI();

        previousPage = page;

        if (menu != null && programmatically) {
            menu.pageChangedTo(page);
        }
    }

    protected Component getView() {
        return content;
    }

    void setMenu(AppStoreMenu menu) {
        this.menu = menu;
    }

}
