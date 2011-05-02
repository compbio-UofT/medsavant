/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import org.ut.biolab.medsavant.model.LibraryVariantsRecordModel;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Vector;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsPage implements Page {
    private final JPanel panel;


  public LibraryVariantsPage() {
      panel = new JPanel();
      panel.setLayout(new BorderLayout());

      SearchableTablePanel stp = new SearchableTablePanel(new Vector(), LibraryVariantsRecordModel.getFieldNames(), LibraryVariantsRecordModel.getFieldClasses());
      panel.add(stp, BorderLayout.CENTER);
    }

    public String getName() {
        return "Variants";
    }

    public Component getView() {
        return panel;
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setBackground(Color.black);
        return p;
    }

}
