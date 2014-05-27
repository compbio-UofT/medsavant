/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.client.view.component;

import com.explodingpixels.macwidgets.MacPainterFactory;
import com.explodingpixels.macwidgets.MacWidgetFactory;
import com.explodingpixels.macwidgets.UnifiedToolBar;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class NiceMenu extends JPanel {

    private final MigLayout layout;
    private final JPanel leftComponent;
    private final JPanel centerComponent;
    private final JPanel rightComponent;

    public enum MenuLocation {
        TOP,
        BOTTOM
    }

    public NiceMenu() {
        this(MenuLocation.TOP);
        this.setBackground(ViewUtil.getPrimaryMenuColor());
    }

    public NiceMenu(MenuLocation location) {

       

        if (location == MenuLocation.TOP) {
            this.setBorder(ViewUtil.getBottomLineBorder());
        } else {
            this.setBorder(ViewUtil.getTopLineBorder());
        }

        leftComponent = ViewUtil.getClearPanel();
        leftComponent.setLayout(new MigLayout("insets 0, nogrid, gapx 10, hidemode 3, filly"));

        centerComponent = ViewUtil.getClearPanel();

        rightComponent = ViewUtil.getClearPanel();
        rightComponent.setLayout(new MigLayout("insets 0, nogrid, alignx trailing, gapx 10, hidemode 3, filly"));

        layout = new MigLayout("gapx 0, gapy 0, fillx, filly " + ((location == MenuLocation.TOP) ? ", height 44, insets 5 15 5 15" : ", insets 5"));
        
        this.setLayout(layout);

        this.add(leftComponent, "width 20%");
        this.add(centerComponent, "width 60%, center");
        this.add(rightComponent, "width 20%");

    }

    public void addLeftComponent(JComponent c) {
        leftComponent.add(c, "left");
    }

    public void addRightComponent(JComponent c) {
        rightComponent.add(c, "right");
    }

    public  void setCenterComponent(JComponent c) {
        centerComponent.removeAll();
        centerComponent.add(c, "center");
    }

    public void setTitle(String title) {
        centerComponent.removeAll();

        JLabel titleLabel = new JLabel("");
        titleLabel.setText(title);
        titleLabel.setForeground(new Color(64, 64, 64));
        //titleLabel.setForeground(Color.white);
        titleLabel.setFont(FontFactory.getMenuTitleFont());
        setCenterComponent(titleLabel);

        centerComponent.invalidate();
        centerComponent.updateUI();
    }

}
