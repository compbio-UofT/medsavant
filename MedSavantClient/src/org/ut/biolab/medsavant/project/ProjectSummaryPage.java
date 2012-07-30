/*
 *    Copyright 2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.project;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.awt.*;
import java.rmi.RemoteException;
import java.sql.SQLException;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author tarkvara
 */
public class ProjectSummaryPage extends SubSectionView {

    private static final Log LOG = LogFactory.getLog(ProjectSummaryPage.class);
    String projectName;
    int projectID;

    ProjectSummaryPage(SectionView parent) {
        super(parent);
        projectName = ProjectController.getInstance().getCurrentProjectName();
        projectID = ProjectController.getInstance().getCurrentProjectID();
    }

    @Override
    public String getName() {
        return "Summary";
    }

    @Override
    public JPanel getView(boolean update) {
        JPanel view = new JPanel();
        ViewUtil.applyVerticalBoxLayout(view);
        //view.setLayout(new GridBagLayout());

        JLabel title = new JLabel(ProjectController.getInstance().getCurrentProjectName() + " Project");
        title.setBorder(ViewUtil.getBigBorder());
        title.setOpaque(false);
        title.setFont(ViewUtil.getMediumTitleFont());

        CollapsiblePanes panes = new CollapsiblePanes();

        panes.add(createPatientSummary());
        panes.add(createVariantSummary());
        panes.add(createReferenceSummary());

        panes.addExpansion();

        view.add(title);
        view.add(panes);

        /*
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(30, 30, 30, 30);

        view.add(title, gbc);
        gbc.insets.top = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        view.add(patientSummary, gbc);
        view.add(variantSummary, gbc);
        gbc.weighty = 1.0;
        view.add(referenceSummary, gbc);
        *
        */
        return view;
    }

    private static JComponent checkException(String capt, ValueFetcher fetcher) {

        JComponent comp;

        try {
            comp = fetcher.getValue();
        } catch (Exception ex) {
            LOG.error("Unable to fetch value for " + capt, ex);
            comp = new JLabel(String.format("<html><font color=\"red\">Unable to fetch value for %s: %s</font>", capt, ClientMiscUtils.getMessage(ex)));
        }

        return comp;
    }

    private CollapsiblePane createPatientSummary() {

        CollapsiblePane p = new CollapsiblePane("Patients");
        p.setCollapsible(false);

        p.getContentPane().setLayout(new BorderLayout());
        KeyValuePairPanel kvp = new KeyValuePairPanel();
        p.add(kvp,BorderLayout.CENTER);


        ValueFetcher pFetcher = new ValueFetcher() {
            @Override
            public JComponent getValue() throws SQLException, RemoteException {
                return new JLabel(ViewUtil.numToString(MedSavantClient.PatientManager.getPatients(LoginController.sessionId, projectID).size()));
            }
        };

        ValueFetcher cFetcher = new ValueFetcher() {
            @Override
            public JComponent getValue() throws SQLException, RemoteException {
                return new JLabel(ViewUtil.numToString(MedSavantClient.CohortManager.getCohorts(LoginController.sessionId, projectID).length));
            }
        };

        String key = "Patients";

        kvp.addKey(key);
        kvp.setValue(key, checkException(key,pFetcher));

        key = "Cohorts";
        kvp.addKey(key);
        kvp.setValue(key, checkException(key,cFetcher));

        return p;
    }

    private JPanel createVariantSummary() {


        CollapsiblePane p = new CollapsiblePane("Variants");
        p.setCollapsible(false);

        p.getContentPane().setLayout(new BorderLayout());
        KeyValuePairPanel kvp = new KeyValuePairPanel();
        p.add(kvp,BorderLayout.CENTER);

        ValueFetcher vfetcher = new ValueFetcher() {
            @Override
            public JComponent getValue() throws SQLException, RemoteException {
                return new JLabel(ViewUtil.numToString(ResultController.getInstance().getTotalVariantCount()));
            }
        };

        String key = "Variants";
        kvp.addKey(key);
        kvp.setValue(key, checkException(key,vfetcher));

        return p;
    }

    private JPanel createReferenceSummary() {

        CollapsiblePane p = new CollapsiblePane("References");
        p.setCollapsible(false);

        p.getContentPane().setLayout(new BorderLayout());
        KeyValuePairPanel kvp = new KeyValuePairPanel();
        p.add(kvp,BorderLayout.CENTER);

        ValueFetcher rfetcher = new ValueFetcher() {
            @Override
            public JComponent getValue() throws Exception {

                String[] refs = ReferenceController.getInstance().getReferenceNames();

                String s = "";
                for (String ref: refs) {
                    s += ref + ",";
                }
                if (s.charAt(s.length()-1) == ',') {
                    s = s.substring(0,s.length()-1);
                }
                return new JLabel(s);
            }
        };

        String key = "References";
        kvp.addKey(key);
        kvp.setValue(key, checkException(key,rfetcher));

        key = "Current Reference";
        kvp.addKey(key);
        kvp.setValue(key, ReferenceController.getInstance().getCurrentReferenceName());

        return p;
    }


    static class Cartouche extends JPanel {
        private final GridBagConstraints KEY_CONSTRAINTS = new GridBagConstraints();
        private final GridBagConstraints VALUE_CONSTRAINTS = new GridBagConstraints();

        Cartouche() {
            setOpaque(false);
            setLayout(new GridBagLayout());

            KEY_CONSTRAINTS.anchor = GridBagConstraints.EAST;
            KEY_CONSTRAINTS.insets = new Insets(15, 90, 15, 3);
            VALUE_CONSTRAINTS.gridwidth = GridBagConstraints.REMAINDER;
            VALUE_CONSTRAINTS.anchor = GridBagConstraints.WEST;
            VALUE_CONSTRAINTS.insets = new Insets(15, 3, 15, 90);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();

            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);
            g.setColor(Color.GRAY);
            g.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);
        }

        /**
         * Add display for a caption and value.  Handles exceptions if the ValueFetcher is
         * doing database access or suchlike.
         *
         * @param capt caption text (without trailing colon)
         * @param fetcher object which fetches the actual value
         */
        void addRow(String capt, ValueFetcher fetcher) {
            try {
                add(new JLabel(capt + ":"), KEY_CONSTRAINTS);
                add(fetcher.getValue(), VALUE_CONSTRAINTS);
            } catch (Exception ex) {
                LOG.error("Unable to fetch value for " + capt, ex);
                add(new JLabel(String.format("<html><font color=\"red\">Unable to fetch value for %s: %s</font>", capt, ClientMiscUtils.getMessage(ex))), VALUE_CONSTRAINTS);
            }
            KEY_CONSTRAINTS.insets.top = 0;
            VALUE_CONSTRAINTS.insets.top = 0;
        }

        /**
         * Add display for a caption and value when the value is just a simple string.
         *
         * @param capt caption text (without trailing colon)
         * @param fetcher object which fetches the actual value
         */
        void addRow(String capt, String val) {
            if (capt != null) {
                add(new JLabel(capt + ":"), KEY_CONSTRAINTS);
            }
            add(new JLabel(val), VALUE_CONSTRAINTS);
            KEY_CONSTRAINTS.insets.top = 0;
            VALUE_CONSTRAINTS.insets.top = 0;
        }
    }

    static abstract class ValueFetcher {
        abstract JComponent getValue() throws Exception;
    }
}
