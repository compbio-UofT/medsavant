package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class HistoryPanel extends JPanel implements FiltersChangedListener {

    Color bg = new Color(129, 139, 154);
    private final ProgressPanel pp;
    private final JLabel labelVariantsRemaining;
    private final JLabel labelVariantsTotal;

    public HistoryPanel() {
        this.setBackground(bg);
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(1),ViewUtil.getBigBorder()));
        this.setLayout(new BorderLayout());

        labelVariantsRemaining = new JLabel("");
        labelVariantsTotal = new JLabel("");

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(infoPanel);
        infoPanel.add(labelVariantsRemaining);
        infoPanel.add(Box.createHorizontalStrut(5));
        infoPanel.add(labelVariantsTotal);
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        this.add(infoPanel,BorderLayout.NORTH);

        pp = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        this.add(pp, BorderLayout.CENTER);

        //PeekingPanel detailView = new PeekingPanel("History", BorderLayout.NORTH, new FilterProgressPanel(), true,400);
        //this.add(detailView, BorderLayout.SOUTH);
        JPanel bottomPanel = ViewUtil.getClearPanel();
        bottomPanel.add(new JButton("Show History"));

        this.add(bottomPanel,BorderLayout.SOUTH);

        FilterController.addFilterListener(this);

        setMaxValues();
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    setNumLeft(MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            FilterController.getQueryFilterConditions()));


                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    Logger.getLogger(FilterProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(FilterProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        thread.start();
    }

    private void setNumLeft(int num) {
        labelVariantsRemaining.setText("PASSING: " + ViewUtil.numToString(num));
        pp.animateToValue(num);
    }

    private void setMaxValues() {

        int maxRecords = -1;

        try {
            maxRecords = MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                    LoginController.sessionId,
                    ProjectController.getInstance().getCurrentProjectId(),
                    ReferenceController.getInstance().getCurrentReferenceId(),
                    FilterController.getQueryFilterConditions());
        } catch (SQLException ex) {
            MiscUtils.checkSQLException(ex);
        } catch (Exception ex) {
            Logger.getLogger(FilterProgressPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (maxRecords != -1) {
            pp.setMaxValue(maxRecords);
            pp.setToValue(maxRecords);
            labelVariantsTotal.setText("TOTAL: " + ViewUtil.numToString(maxRecords));
        }
    }
}
