package org.ut.biolab.medsavant.view.genetics.inspector;

import com.jidesoft.grid.SortableTable;
import com.jidesoft.grid.TableModelWrapperUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.db.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.Settings;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.StarredVariant;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.SQLUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.TablePanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.SubInspector;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class SocialVariantSubInspector extends SubInspector implements VariantSelectionChangedListener {
    private VariantRecord selectedVariant;
    private final JTextArea ta = new JTextArea();

    public SocialVariantSubInspector() {
        TablePanel.addVariantSelectionChangedListener(this);
    }

    @Override
    public String getName() {
        return "Comment";
    }

    @Override
    public JPanel getInfoPanel() {
        JPanel p = ViewUtil.getClearPanel();
        ViewUtil.applyVerticalBoxLayout(p);

        ta.setBorder(ViewUtil.getTinyLineBorder());
        ta.setRows(3);
        p.add(ta);

        JButton submit = new JButton("Submit");
        ViewUtil.makeSmall(submit);
        p.add(ViewUtil.alignRight(submit));

        submit.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                System.out.println("Commenting on " + selectedVariant);

                List<StarredVariant> list = new ArrayList<StarredVariant>();

                StarredVariant sv = new StarredVariant(
                        selectedVariant.getFileID(),
                        selectedVariant.getUploadID(),
                        selectedVariant.getVariantID(),
                            LoginController.getInstance().getUserName(),
                            ta.getText(),
                            SQLUtils.getCurrentTimestamp());

                list.add(sv);
                try {
                    MedSavantClient.VariantManager.addStarredVariants(LoginController.sessionId, ProjectController.getInstance().getCurrentProjectID(), ReferenceController.getInstance().getCurrentReferenceID(), list);
                } catch (SQLException ex) {
                    Logger.getLogger(SocialVariantSubInspector.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(SocialVariantSubInspector.class.getName()).log(Level.SEVERE, null, ex);
                }

                ta.setText("");
            }

        });

        return p;
    }

     private JButton createStarVariantsItem() {

        JButton item = new JButton("Star");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                /*

                String description = "";
                while (true) {
                    description = JOptionPane.showInputDialog("Add a description (500 char limit):", description.substring(0, Math.min(description.length(), 500)));
                    if (description == null) {
                        return;
                    }
                    if (description.length() <= 500) {
                        break;
                    }
                }

                List<StarredVariant> list = new ArrayList<StarredVariant>();
                for (int i = 0; i < finalActualSelected.length; i++) {
                    int row = selected[i];
                    int actualRow = finalActualSelected[i];
                    StarredVariant sv = new StarredVariant(
                            (Integer) table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_UPLOAD_ID),
                            (Integer) table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_FILE_ID),
                            (Integer) table.getModel().getValueAt(row, DefaultVariantTableSchema.INDEX_OF_VARIANT_ID),
                            LoginController.getInstance().getUserName(),
                            description,
                            SQLUtils.getCurrentTimestamp());
                    list.add(sv);
                    if (!starMap.containsKey(actualRow)) {
                        starMap.put(actualRow, new ArrayList<StarredVariant>());
                    }
                    removeStarForUser(actualRow);
                    starMap.get(actualRow).add(sv);
                }
                try {
                    int numStarred = MedSavantClient.VariantManager.addStarredVariants(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            list);
                    if (numStarred < list.size()) {
                        JOptionPane.showMessageDialog(
                                null,
                                "<HTML>" + (list.size() - numStarred) + " out of " + list.size() + " variants were not marked. <BR>The total number of marked variants cannot exceed " + Settings.NUM_STARRED_ALLOWED + ".</HTML>",
                                "Out of Space",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    LOG.error("Error adding star.", ex);
                } catch (RemoteException ex) {
                    LOG.error("Error adding star.", ex);
                }

                //add to view
                for (Integer i : finalActualSelected) {
                    tablePanel.addSelectedRow(i);
                }
                tablePanel.repaint();
                *
                */
            }
        });

        return item;
    }



      private JButton createUnstarVariantItem() {

        JButton item = new JButton("Unmark");
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                /*
                StarredVariant sv = null;
                for (StarredVariant current : starMap.get(row)) {
                    if (current.getUser().equals(LoginController.getInstance().getUserName())) {
                        sv = current;
                        break;
                    }
                }

                try {
                    MedSavantClient.VariantManager.unstarVariant(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectID(),
                            ReferenceController.getInstance().getCurrentReferenceID(),
                            sv.getUploadId(),
                            sv.getFileId(),
                            sv.getVariantId(),
                            LoginController.getInstance().getUserName());
                } catch (SQLException ex) {
                    LOG.error("Error removing star.", ex);
                } catch (RemoteException ex) {
                    LOG.error("Error removing star.", ex);
                }

                //remove from view
                List<StarredVariant> list = starMap.get(row);
                if (list.size() == 1) {
                    tablePanel.removeSelectedRow(row);
                    tablePanel.repaint();
                }
                removeStarForUser(row);
                *
                */
            }
        });

        return item;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        this.selectedVariant = r;
        ta.setText("");
    }


}
