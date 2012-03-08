package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.listener.ReferenceListener;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.component.ProgressPanel;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FilterEffectivenessPanel extends JLayeredPane implements FiltersChangedListener, ReferenceListener {

    Color bg = new Color(139, 149, 164);
    private final ProgressPanel pp;
    private final JLabel labelVariantsRemaining;
    private final JLabel labelVariantsTotal;
    private final FilterHistoryPanel historyPanel;
    private GridBagConstraints c;
    private WaitPanel waitPanel;
    private int waitCounter = 0;
    private JPanel panel;

    public FilterEffectivenessPanel() {
        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        
        this.setLayout(new GridBagLayout());
        
        panel = new JPanel();
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(ViewUtil.getTopLineBorder(),ViewUtil.getBigBorder()));
        panel.setLayout(new BorderLayout());
        this.add(panel, c, JLayeredPane.DEFAULT_LAYER);
        
        waitPanel = new WaitPanel("Applying Filters");
        waitPanel.setVisible(false);
        this.add(waitPanel, c, JLayeredPane.DRAG_LAYER);
        
        labelVariantsRemaining = new JLabel("");
        labelVariantsTotal = new JLabel("");

        JPanel infoPanel = ViewUtil.getClearPanel();
        ViewUtil.applyHorizontalBoxLayout(infoPanel);
        infoPanel.add(labelVariantsRemaining);
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.add(labelVariantsTotal);
        infoPanel.setBorder(ViewUtil.getMediumTopHeavyBorder());

        panel.add(infoPanel,BorderLayout.NORTH);

        pp = new ProgressPanel();
        //pp.setBorder(ViewUtil.getBigBorder());
        panel.add(pp, BorderLayout.CENTER);

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
        
        //disclaimer for count approximation
        JLabel countDisclaimer = new JLabel("* Large counts may be approximate");
        bottomPanel.add(ViewUtil.alignLeft(countDisclaimer), BorderLayout.SOUTH);

        panel.add(bottomPanel,BorderLayout.SOUTH);

        FilterController.addFilterListener(this);
        ReferenceController.getInstance().addReferenceListener(this);
       
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

        //final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
        //        "Applying Filter",
        //        "Filter is being applied. Please wait.",
        //        true);
        
        final FilterEffectivenessPanel instance = this;
        historyPanel.filtersChanged(instance);
        

        Thread thread = new Thread() {

            @Override
            public void run() {
                instance.showWaitCard();
                try {
                    int numLeft = ResultController.getInstance().getNumFilteredVariants();
                    instance.showShowCard();
                    //dialog.close();
                    setNumLeft(numLeft);
                } catch (NonFatalDatabaseException ex) {
                    instance.showShowCard();
                    Logger.getLogger(FilterHistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        };

        thread.start();
        
        
        //dialog.setVisible(true);

    }

    private void setNumLeft(int num) {
        labelVariantsRemaining.setText("PASSING: " + ViewUtil.numToString(num));
        pp.animateToValue(num);
    }

    private void setMaxValues() {
        
        labelVariantsRemaining.setText("Calculating...");
        updateUI();
        
        int maxRecords = -1;

        try {
            maxRecords = ResultController.getInstance().getNumTotalVariants();
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

    @Override
    public void referenceAdded(String name) {}

    @Override
    public void referenceRemoved(String name) {}

    @Override
    public void referenceChanged(String name) {
        historyPanel.reset();
        setMaxValues();
    }
    
    public synchronized void showWaitCard() {
        waitCounter++;
        waitPanel.setVisible(true);
        this.setLayer(waitPanel, JLayeredPane.DRAG_LAYER);
        waitPanel.repaint();
    }

    public synchronized void showShowCard() {
        waitCounter--;
        if(waitCounter <= 0){
            waitPanel.setVisible(false);
            waitCounter = 0;
        }
    }
}
