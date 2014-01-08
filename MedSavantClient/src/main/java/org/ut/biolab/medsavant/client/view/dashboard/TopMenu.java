/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.client.view.dashboard;

import java.awt.Color;
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
        
        this.setBackground(new Color(41,41,41));
        
        leftComponent = ViewUtil.getClearPanel();
        
        centerComponent = ViewUtil.getClearPanel();
        
        titleLabel = new JLabel("");
        titleLabel.setForeground(new Color(214,214,214));
        titleLabel.setFont(ViewUtil.getBigTitleFont());
        centerComponent.add(titleLabel);
        
        rightComponent = ViewUtil.getClearPanel();
        
        layout = new MigLayout("gapx 0, gapy 0, insets 0 0 0 0, fillx");
        this.setLayout(layout);
        
        this.add(leftComponent,"width 20%, left");
        this.add(centerComponent, "width 60%, center");
        this.add(rightComponent, "width 20%, right");
    }

    void addLeftComponent(JComponent c) {
        leftComponent.add(c);
    }
    
    void addRightComponent(JComponent c) {
        rightComponent.add(c);
    }

    void setTitle(String title) {
        titleLabel.setText(title);
    }
    
}
