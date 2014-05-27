/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package medsavant.uhn.cancer;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ToggleableIcon extends JButton {

    private boolean currentState;
    private ImageIcon[] icons;

    public ToggleableIcon(String active_location, String inactive_location, int iconWidth, int iconHeight, boolean toggleable, boolean currentState) {
        this.icons = new ImageIcon[]{
            getImageIcon(inactive_location, iconWidth, iconHeight),
            getImageIcon(active_location, iconWidth, iconHeight)
        };
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleState();
            }
        });
        this.currentState = currentState;

        if (!toggleable) {
            this.setEnabled(false);

        }
        this.setIcon(icons[currentState ? 1 : 0]);
        this.setDisabledIcon(icons[currentState ? 1 : 0]);
    }

    private void toggleState() {
        currentState = !currentState;
        this.setIcon(icons[currentState ? 1 : 0]);
        this.setDisabledIcon(icons[currentState ? 1 : 0]);
        this.revalidate();
        this.repaint();
    }

    private ImageIcon getImageIcon(String location, int iconWidth, int iconHeight) {
        URL resource = UserCommentApp.class.getResource(location);
        if (resource == null) {
            System.err.println("Couldn't load resource given by " + location);
        }
        ImageIcon ii = new ImageIcon(resource);
        Image im = ii.getImage().getScaledInstance(iconWidth, iconHeight, java.awt.Image.SCALE_SMOOTH);
        return new ImageIcon(im);
    }

    public boolean getState() {
        return currentState;
    }

}
