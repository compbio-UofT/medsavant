package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.util.MiscUtils;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FilterEffectivenessPanel extends JPanel implements FiltersChangedListener {

    Color bg = new Color(139, 149, 164);
    private final ProgressPanel pp;
    private final JLabel labelVariantsRemaining;
    private final JLabel labelVariantsTotal;
    private final FilterHistoryPanel historyPanel;

    public FilterEffectivenessPanel() {
        this.setBackground(bg);
        this.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getTopLineBorder(),ViewUtil.getBigBorder()));
        this.setLayout(new BorderLayout());

        labelVariantsRemaining = new JLabel("");
        labelVariantsTotal = new JLabel("");

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(infoPanel);
        infoPanel.add(labelVariantsRemaining);
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.add(labelVariantsTotal);
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        this.add(infoPanel,BorderLayout.NORTH);

        pp = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        this.add(pp, BorderLayout.CENTER);

        historyPanel = new FilterHistoryPanel();
        
        /*
        final JFrame historyFrame = new JFrame("Filter History");
        historyFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        historyFrame.add(historyPanel);
        historyFrame.pack();
        historyFrame.setPreferredSize(new Dimension(500,500));
         *
         */


        //PeekingPanel detailView = new PeekingPanel("History", BorderLayout.NORTH, new FilterProgressPanel(), true,400);
        //this.add(detailView, BorderLayout.SOUTH);
        JPanel bottomPanel = ViewUtil.getClearPanel();
        bottomPanel.setLayout(new BorderLayout());
        JToggleButton b = new JToggleButton("History");

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                historyPanel.setVisible(!historyPanel.isVisible());
                /*
                if (!historyFrame.isVisible()) {
                historyFrame.setVisible(true);
                }
                 *
                 */
            }

        });

        JPanel hisPanel = ViewUtil.alignRight(b);
        hisPanel.setBorder(ViewUtil.getMediumBorder());
        bottomPanel.add(hisPanel,BorderLayout.NORTH);

        bottomPanel.add(historyPanel,BorderLayout.CENTER);
        historyPanel.setVisible(false);

        this.add(bottomPanel,BorderLayout.SOUTH);

        FilterController.addFilterListener(this);

        labelVariantsRemaining.setText("Calculating...");
        updateUI();
        Thread t = new Thread(){
            @Override
            public void run() {
                setMaxValues();
            }
        };
        t.start();
    }

    @Override
    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {

        final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                "Applying Filter",
                "Filter is being applied. Please wait.",
                true);

        Thread thread = new Thread() {

            @Override
            public void run() {
                try {
                    int numLeft = MedSavantClient.VariantQueryUtilAdapter.getNumFilteredVariants(
                            LoginController.sessionId,
                            ProjectController.getInstance().getCurrentProjectId(),
                            ReferenceController.getInstance().getCurrentReferenceId(),
                            FilterController.getQueryFilterConditions());
                    dialog.close();
                    setNumLeft(numLeft);

                } catch (SQLException ex) {
                    MiscUtils.checkSQLException(ex);
                    Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                } catch (RemoteException ex) {
                    Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        thread.start();
        dialog.setVisible(true);

    }

    private void setNumLeft(int num) {
        labelVariantsRemaining.setText("PASSING: " + ViewUtil.numToString(num));
        pp.animateToValue(num);
    }

    private void setMaxValues() {
        
        int maxRecords = -1;

        try {
            maxRecords = ResultController.getInstance().getNumFilteredVariants();
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (maxRecords != -1) {
            pp.setMaxValue(maxRecords);
            pp.setToValue(maxRecords);
            labelVariantsTotal.setText("TOTAL: " + ViewUtil.numToString(maxRecords));

            setNumLeft(maxRecords);
        }
    }
}
