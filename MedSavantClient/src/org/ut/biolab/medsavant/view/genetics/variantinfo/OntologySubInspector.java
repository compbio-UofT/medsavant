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

package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.OntologyTerm;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 * Sub-inspector which displays ontology information about the current gene.
 *
 * @author tarkvara
 */
public class OntologySubInspector extends SubInspector implements GeneSelectionChangedListener {

    private JPanel panel;
    private JList termBox;
    private JScrollPane termScroller;
    private JLabel noTermsLabel;
    private JButton linkButton;

    @Override
    public String getName() {
        return "Ontology";
    }

    @Override
    public JPanel getInfoPanel() {
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            panel.setBackground(Color.WHITE);

            JLabel keyLabel = KeyValuePairPanel.getKeyLabel("Terms");

            termBox = new JList();
            termBox.setFixedCellHeight(15);
            termBox.setFixedCellWidth(10);
            termBox.setVisibleRowCount(12);
            termBox.setFont(termBox.getFont().deriveFont(9.0f));
            termBox.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent lse) {
                    linkButton.setEnabled(lse.getFirstIndex() >= 0);
                }

            });
            termScroller = new JScrollPane(termBox, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            noTermsLabel = new JLabel("No matching terms.");
            noTermsLabel.setFont(KeyValuePairPanel.KEY_FONT);

            linkButton = ViewUtil.getTexturedButton("Link", IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LINKOUT));
            linkButton.setToolTipText("Lookup ontology term");
            linkButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    OntologyTerm ont = (OntologyTerm)termBox.getSelectedValue();
                    if (ont != null) {
                        try {
                            URL url = ont.getInfoURL();
                            if (url != null) {
                                try {
                                    Desktop.getDesktop().browse(url.toURI());
                                } catch (Exception ex) {
                                    ClientMiscUtils.reportError(String.format("Unable to open browser for %s: %%s", url), ex);
                                }
                            } else {
                                Toolkit.getDefaultToolkit().beep();
                            }
                        } catch (MalformedURLException ex) {
                            ClientMiscUtils.reportError(String.format("Unable to get URL for %s: %%s", ont.getID()), ex);
                        }
                    }
                }
            });

            GridBagConstraints gbc = getContentConstraints();
            gbc.gridx = 0;
            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(keyLabel, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(noTermsLabel, gbc);

            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            panel.add(linkButton, gbc);
        }
        return panel;
    }

    @Override
    public void geneSelectionChanged(Gene g) {
        retrieveTerms(g);
    }

    private void retrieveTerms(final Gene g) {
        linkButton.setEnabled(false);   // Due to lack of an initially-selected term.
        if (g != null) {
            new MedSavantWorker<OntologyTerm[]>("Ontology") {
                @Override
                protected void showProgress(double fraction) {
                }

                @Override
                protected void showSuccess(OntologyTerm[] result) {
                    GridBagConstraints gbc = getContentConstraints();
                    if (result.length > 0) {
                        termBox.setModel(new DefaultComboBoxModel(result)); // Yes, that's right.  We're using a combo-box model for a list.

                        panel.remove(noTermsLabel);
                        gbc.fill = GridBagConstraints.BOTH;
                        panel.add(termScroller, gbc);
                    } else {
                        panel.remove(termBox);
                        gbc.fill = GridBagConstraints.HORIZONTAL;
                        panel.add(noTermsLabel, gbc);
                    }
                    panel.validate();
                    panel.repaint();
                }

                @Override
                protected OntologyTerm[] doInBackground() throws Exception {
                    return MedSavantClient.OntologyManager.getTermsForGene(LoginController.sessionId, null, g.getName());
                }
            }.execute();
        } else {
            GridBagConstraints gbc = getContentConstraints();
            panel.remove(termBox);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(noTermsLabel, gbc);
        }
    }

    private GridBagConstraints getContentConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
}
