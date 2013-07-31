/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_ALT;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_CHROM;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_CUSTOM_INFO;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_DBSNP_ID;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_DNA_ID;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_FILE_ID;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_FILTER;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_GT;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_POSITION;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_QUAL;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_REF;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_UPLOAD_ID;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_VARIANT_ID;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_VARIANT_TYPE;
import static org.ut.biolab.medsavant.shared.format.BasicVariantColumns.INDEX_OF_ZYGOSITY;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;

/**
 * A subinspector listing other individuals, with two buttons for family and
 * cohort buttons that launch an aggregate pane in a split screen view. The
 * splitscreen provider can be specified with setSplitter(ScreenSplitter ...).
 * Otherwise, it is assumed that the current subsectionview is a ScreenSplitter.
 * Exceptions will result if setSplitter is not called or the current
 * subsectionview is not a ScreenSplitter implementer.
 *
 * @author jim
 */
public abstract class OtherIndividualsSubInspector extends SubInspector {

    private static final Log LOG = LogFactory.getLog(OtherIndividualsSubInspector.class);
    protected static final int MAXIMIUM_VARIANTS_TO_FETCH = 1000;
    private static final int ROW_HEIGHT = 24;
    protected static final Dimension PREFERRED_SIZE = new Dimension(ComprehensiveInspector.INSPECTOR_INNER_WIDTH, 300);
    protected Map<String, Set<VariantRecord>> dnaIDVariantMap;
    private Set<VariantRecord> variantRecords;
    private MedSavantWorker<Void> variantFetcherThread;
    private OtherIndividualsTableModel otherIndividualsTableModel = new OtherIndividualsTableModel();
    private HierarchicalTable hTable;
    private JPanel infoPanel;
    private Listener<Object> variantSelectionListener;
    private SimpleVariant simpleVariant;
    private JPanel innerPanel;
    private boolean aggregatePaneUpdated = false;
    private SplitScreenPanel splitScreenPanel;
    private VariantFrequencyAggregatePane aggregatePane;// = new VariantFrequencyAggregatePane();

    private String firstCol;
    protected final Set<VariantRecord> getVariantRecords(String dnaID) {
        if (dnaIDVariantMap != null) {
            return dnaIDVariantMap.get(dnaID);
        }
        return null;
    }

    private void openAggregatePane(String column) {
        if (!aggregatePaneUpdated) {
            aggregatePane.setVariantRecords(variantRecords);
        }

        // if (!aggregatePane.isSplit()) {
        
        aggregatePane.splitScreen();
        // }

        aggregatePaneUpdated = true;       
        aggregatePane.groupBy(column);
        firstCol = column;
    }

    public OtherIndividualsSubInspector(SplitScreenPanel sp){
        this.splitScreenPanel = sp;
    }
    protected final void init(VariantFrequencyAggregatePane ap) {
        this.aggregatePane = ap;
        ap.setSplitScreenPanel(splitScreenPanel);
        
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        initHierarchicalTable();
        JScrollPane jsp = new JScrollPane(hTable);
        jsp.setPreferredSize(PREFERRED_SIZE);
        innerPanel.add(jsp);
        innerPanel.setPreferredSize(PREFERRED_SIZE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton cohortButton = new JButton("← " + aggregatePane.getTitle("Cohort"));
        cohortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                openAggregatePane("Cohort");
            }
        });

        JButton familyButton = new JButton("← " + aggregatePane.getTitle(BasicPatientColumns.FAMILY_ID.getAlias()));
        familyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                openAggregatePane(BasicPatientColumns.FAMILY_ID.getAlias());
            }
        });
        buttonPanel.add(cohortButton);
        buttonPanel.add(familyButton);
        innerPanel.add(buttonPanel);
        firstCol = "Cohort";
    }

    //clears the selection in the main pane and sets all subinspectors to the dnaID
    protected void selectVariant(VariantRecord vr) {
        System.out.println("OtherIndividualsSubInspector.selectVariant");
        if (variantSelectionListener != null) {
            // System.out.println("Clearing selection");
            //ViewController.getInstance().getCurrentSubSectionView().clearSelection();
            variantSelectionListener.handleEvent(vr);
        }

    }

    /**
     * Adds a listener for whenever a different variant is selected via this
     * SubInspector. 
     *
     * @see VariantRecord
     */
    public void setVariantSelectionListener(Listener<Object> l) {
        this.variantSelectionListener = l;
        this.aggregatePane.setVariantSelectionListener(l);
    }

    protected abstract JPanel getIndividualSummaryPanel(String dnaID);

    private HierarchicalTable initHierarchicalTable() {
        hTable = new HierarchicalTable();
        hTable.setModel(otherIndividualsTableModel);
        hTable.setHierarchicalColumn(-1);
        hTable.setName("Individuals");
        hTable.setRowHeight(ROW_HEIGHT);
        hTable.setSingleExpansion(true);
        hTable.setShowGrid(false);
        hTable.setTableHeader(null);
        hTable.setSelectInsertedRows(false);
        hTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        hTable.setComponentFactory(new HierarchicalTableComponentFactory() {
            @Override
            public Component createChildComponent(HierarchicalTable table, Object value, int row) {
                if (value instanceof String) {
                    return getIndividualSummaryPanel((String) value); //new VariantSummaryPanel((String) value);
                }
                return null;
            }

            @Override
            public void destroyChildComponent(HierarchicalTable table, Component component, int row) {
                //do Nothing
            }
        });

        hTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = hTable.getSelectedRow();
                if (row != -1) {
                    hTable.expandRow(row);
                }
            }
        });
        return hTable;
    }

    private synchronized void buildDnaIDVariantMap(List<Object[]> values) {
        //dnaIDVariantMap = new HashMap<String, Map<String, VariantRecord>>();
        dnaIDVariantMap = new HashMap<String, Set<VariantRecord>>();
        variantRecords = new HashSet<VariantRecord>();
        for (final Object[] row : values) {
            String dnaID = (String) row[BasicVariantColumns.INDEX_OF_DNA_ID];
            String ref = (String) row[BasicVariantColumns.INDEX_OF_REF];
            String alt = (String) row[BasicVariantColumns.INDEX_OF_ALT];

            VariantRecord r = new VariantRecord(
                    (Integer) row[INDEX_OF_UPLOAD_ID],
                    (Integer) row[INDEX_OF_FILE_ID],
                    (Integer) row[INDEX_OF_VARIANT_ID],
                    (Integer) ReferenceController.getInstance().getCurrentReferenceID(),
                    (Integer) 0, // pipeline ID
                    (String) row[INDEX_OF_DNA_ID],
                    (String) row[INDEX_OF_CHROM],
                    (Integer) row[INDEX_OF_POSITION],
                    (String) row[INDEX_OF_DBSNP_ID],
                    (String) row[INDEX_OF_REF],
                    (String) row[INDEX_OF_ALT],
                    (Float) row[INDEX_OF_QUAL],
                    (String) row[INDEX_OF_FILTER],
                    (String) row[INDEX_OF_CUSTOM_INFO],
                    new Object[]{});

            String type = (String) row[INDEX_OF_VARIANT_TYPE];
            String zygosity = (String) row[INDEX_OF_ZYGOSITY];
            String genotype = (String) row[INDEX_OF_GT];

            r.setType(VariantRecord.VariantType.valueOf(type));
            try {
                r.setZygosity(VariantRecord.Zygosity.valueOf(zygosity));
            } catch (Exception ex) {
            }
            r.setGenotype(genotype);

            Set<VariantRecord> m = dnaIDVariantMap.get(dnaID);
            if (m == null) {
                m = new HashSet<VariantRecord>();
            }
            m.add(r);
            //m.put(type, r);
            dnaIDVariantMap.put(dnaID, m);
            variantRecords.add(r);
        }
    }


    public void handleEvent(Object event) {        
        if (setObject(event)) {              
            updateSelection();
        }else if(aggregatePane.isSplit()){
            openAggregatePane(firstCol);
        }
    }

    public abstract boolean setObject(Object event);

    
    protected void updateSelection() {
      //  System.out.println(getClass().getName()+" updateSelection");
      

        aggregatePaneUpdated = false;
        infoPanel.removeAll();
        infoPanel.add(new WaitPanel("Loading DNA IDs..."));
        if (aggregatePane.isSplit()) {
            aggregatePane.showWaitPanel("Loading DNA IDs...");
        }
        if (variantFetcherThread != null && variantFetcherThread.getState() == StateValue.STARTED) {
            variantFetcherThread.cancel(true);
        }

        variantFetcherThread = new MedSavantWorker<Void>(this.getClass().getName()) {
            @Override
            protected void showSuccess(Void result) {
                otherIndividualsTableModel.setValues(dnaIDVariantMap.keySet());
                if (aggregatePane.isSplit()) {

                    openAggregatePane(firstCol);
                    //AggregatePane is currently visible, so we automatially update it.
                    //aggregatePane.setVariantRecords(variantRecords);                    
                }
                
                infoPanel.removeAll();
                infoPanel.add(innerPanel);
                infoPanel.revalidate();
                infoPanel.repaint();
                infoPanel.updateUI();
            }

            @Override
            protected Void doInBackground() throws Exception {
                buildDnaIDVariantMap(getQueryResults());
                return null;
            }
        };
        variantFetcherThread.execute();
    }

    protected abstract List<Object[]> getQueryResults();

    @Override
    public JPanel getInfoPanel() {
        return infoPanel;
    }
}
