/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.TextAttribute;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.font.FontFactory;
import org.ut.biolab.medsavant.client.view.images.ImagePanel;
import org.ut.biolab.medsavant.client.view.util.StandardAppContainer;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class PatientView1 extends JPanel {

    private JPanel content;
    private JPanel header;
    private JLabel labelPatientID;

    public PatientView1() {
        initView();
    }

    public static void main(String[] arg) {
        JFrame f = new JFrame();
        PatientView1 v = new PatientView1();
        f.add(v);
        f.pack();
        f.setVisible(true);
    }

    private void initView() {

        this.setBackground(ViewUtil.getDefaultBackgroundColor());
        this.setLayout(new BorderLayout());

        header = initHeader();
        content = initContent();
        JPanel tmp = new StandardAppContainer(content);

        this.add(header, BorderLayout.NORTH);
        this.add(tmp, BorderLayout.CENTER);
    }

    private JPanel initHeader() {
        JPanel header = ViewUtil.getClearPanel();

        //header.setBorder(ViewUtil.getBottomLineBorder());
        header.setLayout(new MigLayout("wrap"));

        JPanel titleContainer = ViewUtil.getClearPanel();
        titleContainer.setLayout(new MigLayout("insets 0, aligny top"));

        header.add(titleContainer, "width 400, growx 0.0");

        labelPatientID = new JLabel("PATIENT ID");
        labelPatientID.setFont(FontFactory.getSectionHeaderFont());

        titleContainer.add(labelPatientID, "wrap");

        return header;
    }

    private JPanel initContent() {
        JPanel content = ViewUtil.getClearPanel();

        content.setLayout(new MigLayout("aligny top, fillx, insets 0, gapx 20, gapy 30, wrap 3"));

        addSection(getProfileSection(), content);
        addSection(getCohortSection(), content);
        addSection(getNotesSection(), content, 1, 4);
        addSection(getPedigreeSection(), content, 2);
        addSection(getGeneticsSection(), content, 2);
        addSection(getPhenotypesSection(), content, 2);

        return content;
    }

    private void refresh() {
        refreshHeader();
        refreshContent();
    }

    private void refreshHeader() {

    }

    private void refreshContent() {

    }

    private void addSectionHeader(String string, JPanel p, Color c) {
        JLabel l = new JLabel(string);
        l.setFont(ViewUtil.getBigTitleFont().deriveFont(Font.PLAIN));

        p.add(l, "wrap, gapy 0");

        JPanel band = new JPanel();
        band.setPreferredSize(new Dimension(999, 1));
        band.setBackground(new Color(61, 61, 61));
        band.setBackground(c);

        p.add(band, "growx 1.0, wrap, height 1, hmax 1");
    }

    private void styleButton(JButton button) {
        button.setFocusable(false);
        ViewUtil.makeSmall(button);
    }

    private KeyValuePairPanel getKVP() {
        KeyValuePairPanel kvp = new KeyValuePairPanel(0,true);
        kvp.setXPadding(10);
        kvp.setYPadding(10);
        return kvp;
    }

    int currentSection = 0;
    int numSections = 5;

    private JPanel getProfileSection() {

        JPanel section = createSectionTemplate("Profile", "icon/patient/info-25.png");

        KeyValuePairPanel kvp = getKVP();
        kvp.addKeyWithValue("Individual ID", "value");
        kvp.addKeyWithValue("Sex", "value");
        kvp.addKeyWithValue("Affected", "value");
        kvp.addKeyWithValue("Family ID", "value");
        kvp.addKeyWithValue("Mother ID", "value");
        kvp.addKeyWithValue("Father ID", "value");

        section.add(kvp, "wrap");

        return section;
    }

    private JPanel createSectionTemplate(String name, String iconPath) {
        
        Color c = ViewUtil.getColor(currentSection++, numSections);
        JPanel template = ViewUtil.getClearPanel();
        template.setLayout(new MigLayout("insets 0"));
        ImagePanel p = new ImagePanel(iconPath);
        //p.setDoColorOverlay(true);
        //p.setColorOverlay(c);
        template.add(p, "split");
        addSectionHeader(name, template, c);

        return template;
    }

    private JPanel getGeneticsSection() {

        JPanel section = createSectionTemplate("Genetics", "icon/patient/biotech-25.png");

        JButton uploadVCFButton = new JButton("Upload VCF...");
        JButton uploadBAMButton = new JButton("Upload BAM...");
        styleButton(uploadVCFButton);
        styleButton(uploadBAMButton);

        KeyValuePairPanel kvp = getKVP();
        kvp.addKeyWithValue("Genetic Mutations", uploadVCFButton);
        kvp.addKeyWithValue("Read alignments", uploadBAMButton);

        section.add(kvp, "wrap");

        return section;
    }

    private JPanel getPhenotypesSection() {
        JPanel section = createSectionTemplate("Phenotypes", "icon/patient/fingerprint_scan-25.png");
        return section;
    }

    private void addSection(JPanel section, JPanel content) {
        addSection(section, content, 1, 1);
    }
    
    private void addSection(JPanel section, JPanel content, int spanx) {
        addSection(section, content, spanx, 1);
    }
    
    private void addSection(JPanel section, JPanel content, int spanx, int spany) {
        content.add(section, String.format("align 0 0, span %d %d", spanx, spany));
    }

    private JPanel getCohortSection() {
        JPanel section = createSectionTemplate("Cohort", "icon/patient/conference-26.png");
        return section;
    }

    private JPanel getPedigreeSection() {
        JPanel section = createSectionTemplate("Pedigree", "icon/patient/genealogy-25.png");
        return section;
    }

    private JPanel getNotesSection() {
        JPanel section = createSectionTemplate("Notes", "icon/patient/note-26.png");
        return section;
    }
}
