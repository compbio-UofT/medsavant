/*
 *    Copyright 2011-2012 University of Toronto
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

package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class SearchSubInspector extends SubInspector implements Listener<VariantRecord> {
    private static final Log LOG = LogFactory.getLog(SearchSubInspector.class);
    private final String name;
    private ButtonGroup bg;
    private JRadioButton pmButton;
    private JRadioButton googleButton;
    private JRadioButton scholarButton;

    public SearchSubInspector() {
        this.name = "Search";
    }
    private JTextField field;

    @Override
    public String getName() {
        return this.name;
    }

    public boolean showHeader() {
        return false;
    }

    @Override
    public JPanel getInfoPanel() {

        bg = new ButtonGroup();

        JPanel buttonPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(buttonPanel);

        googleButton = makeWhite("Google");
        googleButton.setSelected(true);
        googleButton.setOpaque(false);
        buttonPanel.add(googleButton);

        scholarButton = makeWhite("Scholar");
        scholarButton.setOpaque(false);
        buttonPanel.add(scholarButton);

        pmButton = makeWhite("PubMed");
        pmButton.setOpaque(false);
        buttonPanel.add(pmButton);

        field = new JTextField();
        field.setPreferredSize(new Dimension(200, 22));
        field.setMaximumSize(new Dimension(200, 22));

        JPanel searchContainer = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(searchContainer);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                performSearchAction();
            }

        });

        searchContainer.add(field);
        searchContainer.add(searchButton);

        JPanel p = new JPanel();//ViewUtil.getClearPanel();

        p.setBorder(ViewUtil.getMediumBorder());
        ViewUtil.applyVerticalBoxLayout(p);


        p.add(buttonPanel);
        p.add(ViewUtil.centerHorizontally(searchContainer));

        return p;
    }

    private JRadioButton makeWhite(String string) {
        JRadioButton b = new JRadioButton(string);
        //b.setForeground(Color.white);
        bg.add(b);
        return b;
    }


    private void performSearchAction() {
        try {
            if (this.googleButton.isSelected()) {
                Searcher.searchGoogle(field.getText());
            } else if (this.scholarButton.isSelected()) {
                Searcher.searchGoogleScholar(field.getText());
            } else if (this.pmButton.isSelected()) {
                Searcher.searchPubmed(field.getText());
            }
        } catch (Exception ex) {
            LOG.error("Error searching.", ex);
            DialogUtils.displayErrorMessage("Problem searching", ex);
        }
    }

    @Override
    public void handleEvent(VariantRecord r) {
        if (r.getDbSNPID() == null || r.getDbSNPID().equals("")) {
            field.setText(r.getChrom() + " " + r.getPosition());
        } else {
            field.setText(r.getDbSNPID());
        }
    }
}
