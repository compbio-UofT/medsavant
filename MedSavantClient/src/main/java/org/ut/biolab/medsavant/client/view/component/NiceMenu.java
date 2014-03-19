/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.component;

import com.explodingpixels.macwidgets.MacWidgetFactory;
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
    }

    public NiceMenu(MenuLocation location) {

        this.setBackground(ViewUtil.getPrimaryMenuColor());

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
        titleLabel.setFont(FontFactory.getMenuTitleFont());
        setCenterComponent(titleLabel);

        centerComponent.invalidate();
        centerComponent.updateUI();
    }

}
