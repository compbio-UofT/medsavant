/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.dashboard;

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
    private final JLabel titleLabel;

    public TopMenu() {
        
        this.setBackground(new Color(221,221,221));
        this.setBorder(BorderFactory.createEmptyBorder());
        
        leftComponent = ViewUtil.getClearPanel();
        leftComponent.setLayout(new MigLayout("insets 0, nogrid"));
        
        centerComponent = ViewUtil.getClearPanel();
        
        titleLabel = new JLabel("");
        titleLabel.setForeground(new Color(64,64,64));
        titleLabel.setFont(ViewUtil.getBigTitleFont());
        centerComponent.add(titleLabel);
        
        rightComponent = ViewUtil.getClearPanel();
        rightComponent.setLayout(new MigLayout("insets 0, nogrid, alignx trailing"));
        
        layout = new MigLayout("gapx 0, gapy 0, insets 0, fillx");
        this.setLayout(layout);
        
        this.add(leftComponent,"width 20%");
        this.add(centerComponent, "width 60%, center");
        this.add(rightComponent, "width 20%");
    }

    void addLeftComponent(JComponent c) {
        leftComponent.add(c,"left");
    }
    
    void addRightComponent(JComponent c) {
        rightComponent.add(c,"right");
    }

    void setTitle(String title) {
        titleLabel.setText(title);
    }
    
}
