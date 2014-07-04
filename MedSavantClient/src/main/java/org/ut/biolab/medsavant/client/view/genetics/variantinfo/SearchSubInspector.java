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

import org.medsavant.api.common.VariantRecord;
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
            field.setText(r.getChrom() + " " + r.getStartPosition()+"-"+r.getEndPosition());
        } else {
            field.setText(r.getDbSNPID());
        }
    }
}
