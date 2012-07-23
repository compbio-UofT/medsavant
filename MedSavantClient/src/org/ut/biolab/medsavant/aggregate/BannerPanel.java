/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.aggregate;

import java.awt.Color;
import javax.swing.*;

import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 * Banner for aggregates which displays a combo-box allowing items to be chosen, along with
 * a progress-bar.
 * @author tarkvara
 */
class BannerPanel extends JPanel {
    BannerPanel(String title, JComponent chooser, JProgressBar progress) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        setBackground(new Color(245,245,245));
        setBorder(BorderFactory.createTitledBorder(title));

        progress.setStringPainted(true);

        add(chooser);
        add(ViewUtil.getMediumSeparator());
        add(Box.createHorizontalGlue());
        add(progress);
    }    
}
