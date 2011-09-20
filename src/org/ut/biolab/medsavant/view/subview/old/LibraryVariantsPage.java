/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview.old;

import java.awt.event.ActionEvent;
import fiume.table.SearchableTablePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.controller.LibraryVariantsController;
import org.ut.biolab.medsavant.model.record.FileRecord;
import org.ut.biolab.medsavant.util.ExtensionFileFilter;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.util.view.PeekingPanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class LibraryVariantsPage { 
/* extends SubSectionView implements ChangeListener {
    private final JPanel panel;
    private final SearchableTablePanel stp;


  public LibraryVariantsPage(SectionView parent) {
      super(parent);
      
      panel = new JPanel();
      panel.setLayout(new BorderLayout());

      LibraryVariantDetailPanel detailPane = new LibraryVariantDetailPanel();
      PeekingPanel detailView = new PeekingPanel("Details", BorderLayout.SOUTH, detailPane, true);
      panel.add(detailView, BorderLayout.NORTH);

      stp = new SearchableTablePanel(new ArrayList(), FileRecord.getFieldNames(), FileRecord.getFieldClasses(), FileRecord.getDefaultColumns());
      stp.getTable().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
      panel.add(stp, BorderLayout.CENTER);

      LibraryVariantsController.getInstance().addChangeListener(this);

      updateLibrary();
    }

    private class LibraryVariantDetailPanel extends JPanel {

        public LibraryVariantDetailPanel() {
            this.setBackground(new Color(20,20,20));
        }
    }

    public String getName() {
        return "Variants";
    }

    public JPanel getView() {
        return panel;
    }

    public Component[] getBanner() {
        JPanel p = ViewUtil.createClearPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
        p.add(Box.createHorizontalGlue());
        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Add Variant Set");
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                fc.addChoosableFileFilter(new ExtensionFileFilter("vcf"));
                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }
                String path = fc.getSelectedFile().getAbsolutePath();
                LibraryVariantsController.getInstance().addFileRecord(
                        new FileRecord(
                            path,
                            (new Date()).toLocaleString()
                            ));
                //setPath(fc.getSelectedFile().getAbsolutePath());
                //JDialog d = new AddLibraryVariantsDialog(null,true);
                //d.setVisible(true);
            }

        });
        p.add(addButton);
        JButton removeButton = new JButton("Remove");

        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int viewrow = stp.getTable().getSelectedRow();
                int modelRow;
                for (modelRow = 0; modelRow < stp.getTable().getModel().getRowCount(); modelRow++) {
                    if (stp.getTable().convertRowIndexToView(modelRow) == viewrow) {
                        break;
                    }
                }
                
                System.out.println("Raw row: " + viewrow);
                System.out.println("Model row: " + stp.getTable().convertRowIndexToModel(viewrow));
                System.out.println("View row: " + stp.getTable().convertRowIndexToView(viewrow));
                System.out.println("My row: " + modelRow);
                
                int row = stp.getTable().convertRowIndexToModel(viewrow);

                LibraryVariantsController.getInstance().removeRecordAtIndex(modelRow);
            }

        });

        p.add(removeButton);
        return p;
    }

    private void updateLibrary() {
        List<FileRecord> r = LibraryVariantsController.getInstance().getFileRecords();
        stp.updateData(Util.getFileRecordVector(r));
    }

    public void stateChanged(ChangeEvent e) {
        updateLibrary();
    }

     * 
     */
}
