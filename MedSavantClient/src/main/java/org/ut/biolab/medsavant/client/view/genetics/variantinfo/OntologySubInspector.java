/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 * Sub-inspector which displays ontology information about the current gene.
 *
 * @author tarkvara
 */
public class OntologySubInspector extends SubInspector implements Listener<Gene> {

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
            panel = ViewUtil.getClearPanel();
            panel.setLayout(new GridBagLayout());

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
    public void handleEvent(Gene g) {
        retrieveTerms(g);
    }

    private void retrieveTerms(final Gene g) {
        linkButton.setEnabled(false);   // Due to lack of an initially-selected term.
        if (g != null) {
            new MedSavantWorker<OntologyTerm[]>(PAGE_NAME) {

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
                    panel.updateUI();
                }

                @Override
                protected OntologyTerm[] doInBackground() throws Exception {
                    return MedSavantClient.OntologyManager.getTermsForGene(LoginController.getSessionID(), null, g.getName());
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
