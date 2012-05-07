package org.ut.biolab.medsavant.view.genetics.variantinfo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SearchVariantInfoPanel extends VariantInfoPanel {

    private final String name;
    private ButtonGroup bg;
    private JRadioButton pmButton;
    private JRadioButton googleButton;
    private JRadioButton scholarButton;

    public SearchVariantInfoPanel() {
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

        ViewUtil.applyMenuStyleInset(p);
        ViewUtil.applyVerticalBoxLayout(p);


        p.add(buttonPanel);
        p.add(ViewUtil.center(searchContainer));

        return p;
    }

    @Override
    public void setInfoFor(VariantRecord r) {
        if (r.getDbSNPID() == null || r.getDbSNPID().equals("")) {
            field.setText(r.getChrom() + " " + r.getPosition());
        } else {
            field.setText(r.getDbSNPID());
        }
    }

    private JRadioButton makeWhite(String string) {
        JRadioButton b = new JRadioButton(string);
        b.setForeground(Color.white);
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
            DialogUtils.displayErrorMessage("Problem searching", ex);
            Logger.getLogger(SearchVariantInfoPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
