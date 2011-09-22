/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.border.MatteBorder;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.images.IconFactory.StandardIcon;
import org.ut.biolab.medsavant.view.menu.Menu;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
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
    private PersistencePanel sectionPanel;
    //private JToggleButton buttonSectionPanelController;
    private SectionView currentSection;
    private PeekingPanel peekRight;
    private SubSectionView currentSubsection;

    public void changeSubSectionTo(SubSectionView view) {

        //if (currentSubsection != null) { currentSubsection.viewDidUnload(); }

        currentSubsection = view;

        currentSubsection.viewLoading();

        this.sectionHeader.setSubSection(view);

        SectionView parent = view.getParent();

        if (parent != currentSection) {
            JPanel[] persistentPanels = parent.getPersistentPanels();
            if (persistentPanels != null) {
                peekRight.setVisible(true);
                sectionPanel.setSectionPersistencePanels(persistentPanels);
            } else {
                peekRight.setVisible(false);
            }
        }
        currentSection = parent;
    }

    void setProject(String projectname) {
        System.out.println("Setting project to : " + projectname);
    }

    void clearMenu() {
        menu.removeAll();
    }

    private static class SidePanel extends JPanel {

        public SidePanel() {
            this.setBackground(ViewUtil.getMenuColor());
            this.setBorder(ViewUtil.getSideLineBorder());

            //this.setPreferredSize(new Dimension(200,200));
            this.setLayout(new BorderLayout());
        }

        public void setContent(JPanel p) {
            this.add(p, BorderLayout.CENTER);
        }
    }

    private static class PersistencePanel extends SidePanel {

        private final JTabbedPane panes;

        public PersistencePanel() {
            panes = new JTabbedPane();
            this.setLayout(new BorderLayout());
            this.add(panes, BorderLayout.CENTER);
        }

        private void setSectionPersistencePanels(JPanel[] persistentPanels) {
            panes.removeAll();
            for (JPanel p : persistentPanels) {
                panes.addTab(p.getName(), p);
            }
        }
    }

    private static class Banner extends JPanel implements ProjectListener {
        private final JComboBox projectDropDown;

        public Banner() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getEndzoneLineBorder(), ViewUtil.getMediumBorder()));
            projectDropDown = new JComboBox();
            
            projectDropDown.setMinimumSize(new Dimension(210,23));
                projectDropDown.setPreferredSize(new Dimension(210,23));
                projectDropDown.setMaximumSize(new Dimension(210,23));

            refreshProjectDropDown();
            this.add(projectDropDown);
            this.add(Box.createHorizontalGlue());
            
            ProjectController.getInstance().addProjectListener(this);
        }

        public void paintComponent(Graphics g) {
            PaintUtil.paintDarkMenu(g, this);
        }

        private void refreshProjectDropDown() {
            try {
                projectDropDown.removeAllItems();
                
                List<String> projects = ProjectController.getInstance().getProjectNames();

                for (String s : projects) {
                    projectDropDown.addItem(s);
                }
                projectDropDown.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        ProjectController.getInstance().setProject((String) projectDropDown.getSelectedItem());
                    }
                });
                
                
            } catch (SQLException ex) {
            }

        }

        public void projectAdded(String projectName) {
            refreshProjectDropDown();
        }

        public void projectRemoved(String projectName) {
            refreshProjectDropDown();
        }

        public void projectChanged(String projectName) {
        }

        public void projectTableRemoved(int projid, int refid) {
            refreshProjectDropDown();
        }
    }

    private static class SectionHeader extends JPanel {

        private final JLabel title;
        private final JPanel sectionMenuPanel;
        private final JPanel subSectionMenuPanel;
        private final ImagePanel leftTab;
        private final ImagePanel rightTab;

        public SectionHeader() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(null);

            this.setBorder(ViewUtil.getMediumSideBorder());
            title = ViewUtil.getHeaderLabel(" ");
            this.add(title);
            sectionMenuPanel = new JPanel();//ViewUtil.getClearPanel();
            subSectionMenuPanel = new JPanel();//ViewUtil.getClearPanel();

            sectionMenuPanel.setBackground(new Color(232, 232, 232));
            sectionMenuPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));

            subSectionMenuPanel.setBackground(new Color(232, 232, 232));
            subSectionMenuPanel.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));

            sectionMenuPanel.setLayout(new BoxLayout(sectionMenuPanel, BoxLayout.X_AXIS));
            subSectionMenuPanel.setLayout(new BoxLayout(subSectionMenuPanel, BoxLayout.X_AXIS));

            this.add(Box.createHorizontalGlue());

            leftTab = new ImagePanel(IconFactory.StandardIcon.TAB_LEFT);
            rightTab = new ImagePanel(IconFactory.StandardIcon.TAB_RIGHT);

            this.add(leftTab);
            this.add(sectionMenuPanel);
            this.add(subSectionMenuPanel);
            this.add(rightTab);

        }

        public void paintComponent(Graphics g) {
            PaintUtil.paintLightMenu(g, this);
        }

        private void setTitle(String sectionName, String subsectionName) {
            title.setText(sectionName.toUpperCase() + " › " + subsectionName);
        }

        private void setSubSection(SubSectionView view) {
            setTitle(view.getParent().getName(), view.getName());

            subSectionMenuPanel.removeAll();
            sectionMenuPanel.removeAll();

            Component[] subsectionBanner = view.getBanner();
            Component[] sectionBanner = view.getParent().getBanner();


            if (subsectionBanner == null && sectionBanner == null) {
                leftTab.setVisible(false);
                rightTab.setVisible(false);
            } else {
                leftTab.setVisible(true);
                rightTab.setVisible(true);
            }

            if (subsectionBanner != null) {
                for (Component c : subsectionBanner) {
                    subSectionMenuPanel.add(c);
                }
            }

            if (sectionBanner != null) {

                for (Component c : sectionBanner) {
                    sectionMenuPanel.add(c);
                }

            }
        }
    }

    private static class ImagePanel extends JPanel {

        private final Image img;
        private final Dimension d;

        public ImagePanel(StandardIcon si) {
            img = IconFactory.getInstance().getIcon(si).getImage();
            d = new Dimension(img.getWidth(null), 30);//img.getHeight(null));
            this.setMaximumSize(d);
            this.setPreferredSize(d);
        }

        public void paintComponent(Graphics g) {
            //g.setColor(Color.red);
            //g.fillRect(0, 0, this.getWidth(), this.getHeight());
            g.drawImage(img, 0, 0, null);
        }
    }
    private static ViewController instance;

    public static ViewController getInstance() {
        if (instance == null) {
            instance = new ViewController();
        }
        return instance;
    }

    private ViewController() {
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
        h1.add(sectionHeader, BorderLayout.NORTH);

        // create the content container
        contentContainer = new JPanel();
        contentContainer.setBackground(Color.white);
        contentContainer.setLayout(new BorderLayout());
        h1.add(contentContainer, BorderLayout.CENTER);

        // create the left menu
        leftPanel = new SidePanel();
        menu = new Menu(contentContainer);
        leftPanel.setContent(menu);

        // create the right panel
        sectionPanel = new PersistencePanel();
        sectionPanel.setPreferredSize(new Dimension(350, 999));
        peekRight = new PeekingPanel("", BorderLayout.WEST, sectionPanel, true);
        h1.add(peekRight, BorderLayout.EAST);

        // add it all to the view
        this.add(banner, BorderLayout.NORTH);
        this.add(h1, BorderLayout.CENTER);

        PeekingPanel peekLeft = new PeekingPanel("Menu", BorderLayout.EAST, leftPanel, true, 210);
        this.add(peekLeft, BorderLayout.WEST);

        peekRight.setVisible(false);
        //buttonSectionPanelController.setVisible(false);
    }

    public void addSection(SectionView section) {
        menu.addSection(section);
    }

    public void refreshView() {
        menu.refreshSelection();
    }

    public void addComponent(Component c) {
        menu.addComponent(c);
    }
}
