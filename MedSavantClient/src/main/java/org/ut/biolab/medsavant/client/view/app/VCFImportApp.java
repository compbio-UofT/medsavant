/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.component.RoundedPanel;
import org.ut.biolab.medsavant.client.view.dashboard.DashboardApp;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class VCFImportApp implements DashboardApp {

    List<File> filesToImport;
    private JPanel fileListView;

    public VCFImportApp() {
        filesToImport = new ArrayList<File>();
    }

    private JPanel view;

    @Override
    public JPanel getView() {
        return view;
    }

    private void initView() {
        if (view == null) {
            view = new JPanel();

            view.setLayout(new BorderLayout());

            JPanel container = ViewUtil.getClearPanel();
            MigLayout layout = new MigLayout("insets 30 200 30 200, fillx");
            container.setLayout(layout);

            //DropPanel dp = new DropPanel();
            RoundedPanel dp = new RoundedPanel(30);
            int topBorder = 50;
            int sideBorder = 150;
            dp.setBorder(BorderFactory.createEmptyBorder(topBorder, sideBorder, topBorder, sideBorder));
            dp.add(new JLabel("Drag and drop VCF files here"));
            container.add(dp, "center, wrap");

            new FileDrop(dp, new FileDrop.Listener() {
                public void filesDropped(java.io.File[] files) {
                    for (File f : files) {
                        System.out.println("File dropped: " + f.getAbsolutePath());
                        addFileToImport(f);
                    }
                }   // end filesDropped
            }); // end FileDrop.Listener

            fileListView = ViewUtil.getClearPanel();
            container.add(fileListView, "wrap");

            JButton advancedOptionsButton = ViewUtil.getIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CONFIGURE));
            advancedOptionsButton.setToolTipText("Advanced Options");
            advancedOptionsButton.setFocusable(false);
            container.add(advancedOptionsButton, "wrap, right, growx 1.0");

            JButton importButton = new JButton("Import");
            JPanel bContainer = ViewUtil.getClearPanel();
            bContainer.add(importButton);
            container.add(bContainer, "wrap, right, growx 1.0");

            JScrollPane p = ViewUtil.getClearBorderlessScrollPane(container);
            p.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            view.add(p, BorderLayout.CENTER);
        }
    }

    private void addFileToImport(File f) {
        filesToImport.add(f);
        refreshFileList();
    }

    @Override
    public void viewWillUnload() {
    }

    @Override
    public void viewWillLoad() {
        initView();
    }

    @Override
    public void viewDidUnload() {
    }

    @Override
    public void viewDidLoad() {
    }

    @Override
    public ImageIcon getIcon() {
        return IconFactory.getInstance().getIcon(IconFactory.StandardIcon.APP_IMPORTVCF);
    }

    @Override
    public String getName() {
        return "VCF Import";
    }

    public static void main(String[] argv) {
        JFrame f = new JFrame();
        VCFImportApp app = new VCFImportApp();
        f.setPreferredSize(new Dimension(400, 400));
        app.viewWillLoad();
        f.add(app.getView());
        f.show();
        app.viewDidLoad();
    }

    private void refreshFileList() {
        fileListView.removeAll();
        MigLayout ml = new MigLayout("wrap 2");
        fileListView.setLayout(ml);

        for (final File f : this.filesToImport) {
            fileListView.add(new JLabel(f.getAbsolutePath()), "growx 1.0");

            JButton b = new JButton("x");
            fileListView.add(b, "width 100");

            b.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    filesToImport.remove(f);
                    refreshFileList();
                }

            });
        }

        fileListView.updateUI();
    }

    private static class DropPanel extends JPanel {

        public DropPanel() {
            //this.setPreferredSize(new Dimension(400,200));
            //this.setBackground(Color.yellow);
            int topBorder = 50;
            int sideBorder = 150;
            this.setBorder(BorderFactory.createEmptyBorder(topBorder, sideBorder, topBorder, sideBorder));
            this.add(new JLabel("Drag and drop VCF files here"));
        }

        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(41, 41, 41));

            int w = this.getWidth();
            int h = this.getHeight();
            int cornerRadius = 30;

            //Rectangle2D r = new Rectangle2D.Double(0,0,w,h,cornerRadius,cornerRadius);
        }
    }
}
