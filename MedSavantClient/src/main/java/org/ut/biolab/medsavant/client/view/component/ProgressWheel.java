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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ProgressWheel extends JPanel {

    public ProgressWheel() {
        
        this.setLayout(new MigLayout("insets 3"));
        //Dimension d = new Dimension(24,23);
        //this.setPreferredSize(d);
        //this.setMaximumSize(d);
        //this.setMinimumSize(d);
        this.setBorder(null);
        this.setOpaque(false);
        ImageIcon waitGif = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.WAIT);
        ImagePanel p = new ImagePanel(waitGif.getImage(),24,8);
        p.setOpaque(false);
        this.add(p,"height 23, center");
    }

    public void setComplete() {
        this.removeAll();
    }

    public void setIndeterminate(boolean b) {

    }

    public void setValue(int d) {

    }

    public int getMaximum() {
        return 100;
    }

    public boolean isIndeterminate() {
        return true;
    }
}
