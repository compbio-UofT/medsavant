/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.menu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.util.PaintUtil;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ViewController extends JPanel {

    private Banner banner;
    private SectionHeader sectionHeader;
    private SidePanel leftPanel;
    private Menu menu;
    private JPanel contentContainer;

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getMenuColor());
            this.setBorder(ViewUtil.getRightLineBorder());
            //this.setPreferredSize(new Dimension(200,200));
            this.setLayout(new BorderLayout());
        }
        
        public void setContent(JPanel p) {
            this.add(p,BorderLayout.CENTER);
        }
    }

    private static class Banner extends JPanel {

        public Banner() {
            this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            this.setBorder(ViewUtil.getMediumBorder());
            this.add(ViewUtil.getTitleLabel("MedSavant"));
        }
        
        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }
    }

    private static class SectionHeader extends JPanel {

        public SectionHeader() {
            this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
            this.add(ViewUtil.getTitleLabel("Section Header"));
        }
        
        public void paintComponent(Graphics g) {
            PaintUtil.paintLightMenu(g, this);
        }
    }

    public ViewController() {
        initUI();
    }

    private void initUI() {
        this.setLayout(new BorderLayout());
        
        // create the banner
        banner = new Banner();
        JPanel h1 = new JPanel();
        h1.setLayout(new BorderLayout());
        
        // create the section header
        sectionHeader = new SectionHeader();
        h1.add(sectionHeader, BorderLayout.CENTER);
        
        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(Color.white);
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer,BorderLayout.CENTER);
        
        // create the left menu
        leftPanel = new SidePanel();
        menu = new Menu(contentContainer);
        leftPanel.setContent(menu);
        
        // add it all to the view
        this.add(banner,BorderLayout.NORTH);
        this.add(h1,BorderLayout.CENTER);
        this.add(leftPanel,BorderLayout.WEST);
        
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }
    
}
