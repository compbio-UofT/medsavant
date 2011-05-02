/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import org.ut.biolab.medsavant.view.dialog.AddLibraryVariantsDialog;
import java.awt.event.ActionEvent;
import org.ut.biolab.medsavant.model.LibraryVariantsRecordModel;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.LibraryVariantsController;
import org.ut.biolab.medsavant.model.record.FileRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsPage implements Page {
    private final JPanel panel;
    private final SearchableTablePanel stp;


  public LibraryVariantsPage() {
      panel = new JPanel();
      panel.setLayout(new BorderLayout());

      stp = new SearchableTablePanel(new ArrayList(), FileRecordModel.getFieldNames(), FileRecordModel.getFieldClasses());
      panel.add(stp, BorderLayout.CENTER);

      updateLibrary();
    }

    public String getName() {
        return "Variants";
    }

    public Component getView() {
        return panel;
    }

    public Component getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Open File");
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }
                //setPath(fc.getSelectedFile().getAbsolutePath());
                //JDialog d = new AddLibraryVariantsDialog(null,true);
                //d.setVisible(true);
            }

        });
        p.add(addButton);
        JButton removeButton = new JButton("Remove");
        p.add(removeButton);
        return p;
    }

    private void updateLibrary() {
        List<FileRecordModel> r = LibraryVariantsController.getInstance().getFileRecords();
        stp.updateData(Util.getFileRecordVector(r));
    }

}
