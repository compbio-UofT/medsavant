/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.thread;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
class ThreadProgressIndicator extends JPanel {
    private final Thread thread;
    private final String threadname;

    public ThreadProgressIndicator(String threadname, Thread t) {
        this.threadname = threadname;
        this.thread = t;
        initGUI();
    }

    private void initGUI() {
        //this.setPreferredSize(new Dimension(40,40));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setBorder(ViewUtil.getSmallBorder());
        this.setPreferredSize(new Dimension(0,0));
        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        this.add(new JLabel(threadname));
        this.add(Box.createHorizontalGlue());
        this.add(ViewUtil.getMediumSeparator());
        this.add(progress);
        this.add(ViewUtil.getMediumSeparator());
        this.add(new JButton("Cancel"));
    }
    
}
