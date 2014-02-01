/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.dashboard;

import com.explodingpixels.macwidgets.MacWidgetFactory;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class TopMenu extends JPanel {

    private final MigLayout layout;
    private final JPanel leftComponent;
    private final JPanel centerComponent;
    private final JPanel rightComponent;

    public TopMenu() {

        this.setBackground(ViewUtil.getPrimaryMenuColor());
        this.setBorder(ViewUtil.getBottomLineBorder());

        leftComponent = ViewUtil.getClearPanel();
        leftComponent.setLayout(new MigLayout("insets 0, nogrid"));

        centerComponent = ViewUtil.getClearPanel();

        rightComponent = ViewUtil.getClearPanel();
        rightComponent.setLayout(new MigLayout("insets 0, nogrid, alignx trailing"));

        layout = new MigLayout("gapx 0, gapy 0, insets 5 15 5 15, fillx, filly, height 40");
        this.setLayout(layout);

        this.add(leftComponent, "width 20%");
        this.add(centerComponent, "width 60%, center");
        this.add(rightComponent, "width 20%");

    }

    void addLeftComponent(JComponent c) {
        leftComponent.add(c, "left");
    }

    void addRightComponent(JComponent c) {
        rightComponent.add(c, "right");
    }

    void setCenterComponent(JComponent c) {
        centerComponent.removeAll();
        centerComponent.add(c, "center");
    }

    void setTitle(String title) {
        centerComponent.removeAll();

        JLabel titleLabel = new JLabel("");
        titleLabel.setText(title);
        titleLabel.setForeground(new Color(64, 64, 64));
        titleLabel.setFont(ViewUtil.getBigTitleFont());
        setCenterComponent(titleLabel);
    }

}
