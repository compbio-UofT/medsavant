/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.swing.RangeSlider;
import com.jidesoft.utils.SwingWorker;
import fiume.vcf.VariantRecord;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.db.QueryUtil;
import org.ut.biolab.medsavant.db.ConnectionController;
import org.ut.biolab.medsavant.db.MedSavantDatabase;
import org.ut.biolab.medsavant.db.table.TableSchema;
import org.ut.biolab.medsavant.db.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.view.filter.pathways.PathwaysPanel;
import org.ut.biolab.medsavant.view.filter.pathways.PathwaysTab;
import org.ut.biolab.medsavant.view.genetics.filter.FilterView.FilterViewType;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ChromosomeComparator;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel implements FiltersChangedListener {
    
    private final ArrayList<FilterView> filterViews;
    private CollapsiblePanes filterContainer;
    private JLabel status;
    private JPanel contentPlaceholder;
    private final HashMap<String, CollapsiblePane> filterToCPMap;
    
    public enum FilterWidgetType { INT, FLOAT, STRING, BOOLEAN };

    public FilterPanel() throws NonFatalDatabaseException {
        this.setName("Filters");
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        filterToCPMap = new HashMap<String,CollapsiblePane>();
        FilterController.addFilterListener(this);
        initGUI();
    }

    private void initGUI() throws NonFatalDatabaseException {

        JPanel statusPanel = ViewUtil.getBannerPanel();
        status = new JLabel();

        try {
            status.setText(ViewUtil.numToString(QueryUtil.getNumRowsInTable(
                    ConnectionController.connect(),
                    MedSavantDatabase.getInstance().getVariantTableSchema().getTable())) + " variants in table");
        } catch (SQLException ex) {
        }

        ViewUtil.clear(status);
        status.setFont(ViewUtil.getMediumTitleFont());
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(status);
        statusPanel.add(Box.createHorizontalGlue());
        this.add(statusPanel, BorderLayout.SOUTH);

        
        contentPlaceholder = new JPanel();
        contentPlaceholder.setBackground(ViewUtil.getMenuColor());
        contentPlaceholder.setLayout(new BorderLayout());
        contentPlaceholder.add(new WaitPanel("Generating filters"), BorderLayout.CENTER);
        this.add(contentPlaceholder,BorderLayout.CENTER);
        
        (new FilterViewGenerator()).execute();
        
        this.setPreferredSize(new Dimension(400, 999));
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }

    private synchronized void addFilterView(final FilterView view) {
        filterViews.add(view);
        final CollapsiblePane cp = new CollapsiblePane(view.getTitle());
        try {
            cp.setCollapsed(true);
        } catch (PropertyVetoException ex) {
        }
        cp.setCollapsedPercentage(0);
        cp.setContentPane(view.getComponent());
        
        if(view.getFilterViewType().equals(FilterViewType.FRAME)){
            cp.addMouseListener(new MouseListener() {

                public void mouseClicked(MouseEvent e) {
                    view.getFrame().setVisible(true);
                }

                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
        }
        
        filterToCPMap.put(view.getTitle(),cp);
        
        this.filterContainer.add(cp);
    }

    private void updatePaneEmphasis() {
        Set<String> filters = FilterController.getFilterSet(FilterController.getCurrentFilterSetID()).keySet();
        
        for (String p : this.filterToCPMap.keySet()) {
            this.filterToCPMap.get(p).setEmphasized(false);
        }
        for (String s : filters) {
            this.filterToCPMap.get(s).setEmphasized(true);
        }
        
        
    }

    public class FilterViewGenerator extends SwingWorker {

        private List<FilterView> getFilterViews() throws SQLException, NonFatalDatabaseException {     
            List<FilterView> views = new ArrayList<FilterView>();
                       
            //cohort filters
            long nano1 = System.nanoTime();
            views.add(CohortFilterView.getCohortFilterView());
            views.add(GeneListFilterView.getFilterView());

            //filters on individuals  
            long nano2 = System.nanoTime();            
            views.add(FamilyFilter.getFamilyFilterView());
            views.add(GenderFilterView2.getGenderFilterView());
            views.add(EthnicityFilterView.getEthnicityFilterView());
            views.add(IQVerbal.getFilterView());
            views.add(IQPerformance.getFilterView());
            views.add(IQFullScore.getFilterView());
               
            //variant table filters
            long nano3 = System.nanoTime();
            views.addAll(getVariantRecordFilterViews());
            
            //custom filters
            long nano4 = System.nanoTime();
            views.add(GOFilter.getGOntologyFilterView());
            views.add(HPOFilter.getHPOntologyFilterView());                     
            views.add(new FilterView("WikiPathways", new PathwaysPanel()));
            long nano5 = System.nanoTime();
            
            /*System.out.println();
            System.out.println("A: " + (nano2 - nano1));
            System.out.println("B: " + (nano3 - nano2));
            System.out.println("C: " + (nano4 - nano3));
            System.out.println("D: " + (nano5 - nano4));
            System.out.println("TOTAL: " + (nano5-nano1));
            System.out.println();*/
  
            //save and dispose of cache
            FilterCache.saveAndDispose();
            
            return views;
        }

        @Override
        protected Object doInBackground() throws Exception {
            return getFilterViews();
        }
        
        protected void done() {
            try {
                
                filterContainer = new CollapsiblePanes();
                filterContainer.setBackground(ViewUtil.getMenuColor());

                JScrollPane p1 = new JScrollPane(filterContainer);
                p1.setBorder(null);

                contentPlaceholder.removeAll();
                contentPlaceholder.add(p1, BorderLayout.CENTER);

                List<FilterView> views = (List<FilterView>) get();
                addFilterViews(views);
                filterContainer.addExpansion();
                
                contentPlaceholder.updateUI();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

    void listenToComponent(final JCheckBox c) {

        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }

    private List<FilterView> getVariantRecordFilterViews() throws SQLException, NonFatalDatabaseException {


        List<FilterView> l = new ArrayList<FilterView>();

        System.out.println("Making filters");

        List<String> fieldNames = VariantRecordModel.getFieldNames();
        int numFields = fieldNames.size();

        for (int i = 0; i < numFields; i++) {

            final int fieldNum = i;

            final String columnAlias = fieldNames.get(i);

            if (columnAlias.equals("Information")) {
                continue;
            }

            // Don't make filters for some fields
            if (
                    columnAlias.equals(VariantTableSchema.ALIAS_ID) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GENOMEID) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_FILTER) || 
                    //columnAlias.equals(VariantTableSchema.ALIAS_DNAID) || // remove this later
                    columnAlias.equals(VariantTableSchema.ALIAS_PIPELINEID) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_END) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_MQ0) || 
                    columnAlias.equals(VariantTableSchema.ALIAS_MQ) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_AA) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_NS) || 
                    columnAlias.equals(VariantTableSchema.ALIAS_CIGAR) || 
                    columnAlias.equals(VariantTableSchema.ALIAS_SOMATIC) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_VALIDATED) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_AN) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_BQ) ||
                    //columnAlias.equals(VariantTableSchema.ALIAS_GT) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GPHASED) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GDP) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GFT) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GLHOMOREF) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GLHET) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GLHOMOALT) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GQ) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_HQA) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_HQB) ||
                    
                    
                    columnAlias.equals(VariantTableSchema.ALIAS_snp_id) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_name) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_name2) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_spliceInfo) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_changesAA) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_referenceAA) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_variantAA) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_PfamHit) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_nt1) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_nt2) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_o_acc) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_codingCoordStr) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_proteinCoordStr) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_acc) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_position_a) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_position_b) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_mrnaCoord) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_codonCoord) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_frame) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_pph2_FDR) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_pph2_FPR) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_pph2_TPR) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_IdPSNP) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_IdPmax) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_IdQmin) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_MinDJnc) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_IdQmin)
                    ) {
                continue;
            }
            
            long start = System.currentTimeMillis();

            TableSchema table = MedSavantDatabase.getInstance().getVariantTableSchema();
            DbColumn col = table.getDBColumn(columnAlias);
            FilterWidgetType fwt;
            if(TableSchema.isInt(table.getColumnType(col))){
                fwt = FilterWidgetType.INT;
            } else if (TableSchema.isFloat(table.getColumnType(col))){
                fwt = FilterWidgetType.FLOAT;
            } else if (TableSchema.isBoolean(table.getColumnType(col))){
                fwt = FilterWidgetType.BOOLEAN;
            } else {
                fwt = FilterWidgetType.STRING;
            }

            //special cases (TODO: messy...)
            if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
                fwt = FilterWidgetType.STRING;
            } else if (columnAlias.equals(VariantTableSchema.ALIAS_Transv) || 
                    columnAlias.equals(VariantTableSchema.ALIAS_pph2_prob)){
                fwt = FilterWidgetType.BOOLEAN;
            }

            if (fwt == FilterWidgetType.INT || fwt == FilterWidgetType.FLOAT) {
                
                Range extremeValues = null;
                
                if (columnAlias.equals(VariantTableSchema.ALIAS_POSITION)) {
                    extremeValues = new Range(1,250000000);
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_SB)) {
                    extremeValues = new Range(-100,100);
                } else {
                    extremeValues = FilterCache.getDefaultValuesRange(columnAlias);
                    if(extremeValues == null){
                        //System.out.println(columnAlias + " - retrieving");
                        extremeValues = QueryUtil.getExtremeValuesForColumn(ConnectionController.connect(), table, col);
                    } else {
                        //System.out.println(columnAlias + " - found cache");
                    }
                    FilterCache.addDefaultValues(columnAlias, extremeValues);
                }
                
                if (columnAlias.equals(VariantTableSchema.ALIAS_DP)) {
                    extremeValues = new Range(Math.min(0, extremeValues.getMin()),extremeValues.getMax());
                }
            
                JPanel container = new JPanel();
                container.setBorder(ViewUtil.getMediumBorder());
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                final RangeSlider rs = new com.jidesoft.swing.RangeSlider();

                final int min = (int) Math.floor(extremeValues.getMin());
                final int max = (int) Math.ceil(extremeValues.getMax());

                rs.setMinimum(min);
                rs.setMaximum(max);

                rs.setMajorTickSpacing(5);
                rs.setMinorTickSpacing(1);

                rs.setLowValue(min);
                rs.setHighValue(max);

                JPanel rangeContainer = new JPanel();
                rangeContainer.setLayout(new BoxLayout(rangeContainer, BoxLayout.X_AXIS));

                final JTextField frombox = new JTextField(ViewUtil.numToString(min));
                final JTextField tobox = new JTextField(ViewUtil.numToString(max));

                final JLabel fromLabel = new JLabel(ViewUtil.numToString(min));
                final JLabel toLabel = new JLabel(ViewUtil.numToString(max));

                rangeContainer.add(fromLabel);
                rangeContainer.add(rs);
                rangeContainer.add(toLabel);

                container.add(frombox);
                container.add(tobox);
                container.add(rangeContainer);
                container.add(Box.createVerticalBox());
                
                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);

                rs.addMouseListener(new MouseListener() {

                    public void mouseClicked(MouseEvent e) {}
                    public void mousePressed(MouseEvent e) {}
                    public void mouseReleased(MouseEvent e) {
                        frombox.setText(ViewUtil.numToString(rs.getLowValue()));
                        tobox.setText(ViewUtil.numToString(rs.getHighValue()));
                    }
                    public void mouseEntered(MouseEvent e) {}
                    public void mouseExited(MouseEvent e) {}
                });
                
                tobox.addKeyListener(new KeyListener() {

                    public void keyTyped(KeyEvent e) {
                        
                    }

                    public void keyPressed(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
                        try {
                            int num = (int) Math.ceil(getNumber(tobox.getText().replaceAll(",", "")));
                            rs.setHighValue(num);
                            applyButton.setEnabled(true);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            tobox.requestFocus();
                        }       
                    }
                    
                });
                
                frombox.addKeyListener(new KeyListener() {

                    public void keyTyped(KeyEvent e) {
                        
                    }

                    public void keyPressed(KeyEvent e) {
                    }

                    public void keyReleased(KeyEvent e) {
                        try {
                            int num = (int) Math.floor(getNumber(frombox.getText().replaceAll(",", "")));
                            rs.setLowValue(num);
                            applyButton.setEnabled(true);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            frombox.requestFocus();
                        }
                    }
                    
                });
                
                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        Range acceptableRange = new Range(getNumber(frombox.getText().replaceAll(",", "")), getNumber(tobox.getText().replaceAll(",", "")));

                        if (min == acceptableRange.getMin() && max == acceptableRange.getMax()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[2];
                                    results[0] = BinaryCondition.greaterThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), getNumber(frombox.getText().replaceAll(",", "")), true);
                                    results[1] = BinaryCondition.lessThan(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), getNumber(tobox.getText().replaceAll(",", "")), true);

                                    Condition[] resultsCombined = new Condition[1];
                                    resultsCombined[0] = ComboCondition.and(results);

                                    return resultsCombined;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
                                }
                            };
                            //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                            System.out.println("Adding filter: " + f.getName());
                            FilterController.addFilter(f);
                        }

                        //TODO: why does this not work? Freezes GUI
                        //apply.setEnabled(false);
                    }
                });

                rs.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        applyButton.setEnabled(true);
                    }
                });

                JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
                selectAll.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        rs.setLowValue(min);
                        rs.setHighValue(max);
                        frombox.setText(ViewUtil.numToString(min));
                        tobox.setText(ViewUtil.numToString(max));
                    }
                });

                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                bottomContainer.add(selectAll);
                bottomContainer.add(Box.createHorizontalGlue());
                bottomContainer.add(applyButton);

                container.add(bottomContainer);

                final FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);

            } else if (fwt == FilterWidgetType.BOOLEAN) {

                List<String> uniq = new ArrayList<String>();
                uniq.add("True");
                uniq.add("False");

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);
                final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        final List<String> acceptableValues = new ArrayList<String>();

                        if (boxes.get(0).isSelected()) {
                            acceptableValues.add("1");
                        }
                        if (boxes.get(1).isSelected()) {
                            acceptableValues.add("0");
                        }

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
                                }
                            };
                            //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                            System.out.println("Adding filter: " + f.getName());
                            FilterController.addFilter(f);
                        }

                        //TODO: why does this not work? Freezes GUI
                        //apply.setEnabled(false);
                    }
                });

                for (String s : uniq) {
                    JCheckBox b = new JCheckBox(s);
                    b.setSelected(true);
                    b.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            AbstractButton abstractButton =
                                    (AbstractButton) e.getSource();
                            ButtonModel buttonModel = abstractButton.getModel();
                            boolean pressed = buttonModel.isPressed();
                            if (pressed) {
                                applyButton.setEnabled(true);
                            }
                            //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
                        }
                    });
                    b.setAlignmentX(0F);
                    container.add(b);
                    boxes.add(b);
                }

                JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
                selectAll.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(true);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectAll);

                JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

                selectNone.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(false);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectNone);

                bottomContainer.add(Box.createGlue());

                bottomContainer.add(applyButton);

                bottomContainer.setAlignmentX(0F);
                container.add(bottomContainer);

                FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);


            } else if (fwt == FilterWidgetType.STRING) {

                Connection conn = ConnectionController.connect();

                final List<String> uniq;
                
                if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(VariantRecord.ALIAS_ZYGOSITY));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_CHROM)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "chr1","chr2","chr3","chr4","chr5","chr6","chr7","chr8",
                                "chr9","chr10","chr11","chr12","chr13","chr14","chr15","chr16",
                                "chr17","chr18","chr19","chr20","chr21","chr22","chrX","chrY"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_uorfChange)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","+1","-1"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_functionalClass)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","missense","nonsense","readthrough","silent"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_inCodingRegion)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","true","false"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_positionType)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","CDS","intron","utr3","utr5"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_referenceCodon)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","AAA","AAC","AAG","AAT","ACA","ACC","ACG","ACT","AGA","AGC","AGG","AGT","ATA","ATC","ATG","ATT","CAA","CAC","CAG","CAT","CCA","CCC","CCG","CCT","CGA","CGC","CGG","CGT","CTA","CTC","CTG","CTT","GAA","GAC","GAG","GAT","GCA","GCC","GCG","GCT","GGA","GGC","GGG","GGT","GTA","GTC","GTG","GTT","TAA","TAC","TAG","TAT","TCA","TCC","TCG","TCT","TGA","TGC","TGG","TGT","TTA","TTC","TTG","TTT"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_variantCodon)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","AAA","AAC","AAG","AAT","ACA","ACC","ACG","ACT","AGA","AGC","AGG","AGT","ATA","ATC","ATG","ATT","CAA","CAC","CAG","CAT","CCA","CCC","CCG","CCT","CGA","CGC","CGG","CGT","CTA","CTC","CTG","CTT","GAA","GAC","GAG","GAT","GCA","GCC","GCG","GCT","GGA","GGC","GGG","GGT","GTA","GTC","GTG","GTT","TAA","TAC","TAG","TAT","TCA","TCC","TCG","TCT","TGA","TGC","TGG","TGT","TTA","TTC","TTG","TTT"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_pph2_class)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{"","              none","           neutral","       deleterious"
                            
                                }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_AC)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "1","2"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_AF)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "0.50","1.00"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_prediction)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{"","            benign","           unknown"," possibly damaging"," probably damaging"
                            
                                }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_transcriptStrand)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "","+","-"
                            }));
                } else if (columnAlias.equals(VariantTableSchema.ALIAS_REFERENCE)
                        || columnAlias.equals(VariantTableSchema.ALIAS_ALTERNATE)) {
                    uniq = new ArrayList<String>();
                    uniq.addAll(Arrays.asList(
                            new String[]{
                                "A","C","G","T"
                            }));
                } 
                else {
                    List<String> tmp = FilterCache.getDefaultValues(columnAlias);
                    if(tmp == null){
                        tmp = QueryUtil.getDistinctValuesForColumn(conn, table, col);
                        //System.out.println(columnAlias + " - retrieving");
                    } else {
                        //System.out.println(columnAlias + " - found cache");
                    }
                    FilterCache.addDefaultValues(columnAlias, tmp);
                    uniq = tmp;
                              
                    /*
                    System.out.println();
                    for (String val : uniq) {
                        System.out.print("\"" + val + "\",");
                    }
                    System.out.println();
                     * 
                     */
                }
                
                if (columnAlias.equals(VariantTableSchema.ALIAS_CHROM)) {
                    Collections.sort(uniq,new ChromosomeComparator());
                }
                
                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                JPanel bottomContainer = new JPanel();
                bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));

                final JButton applyButton = new JButton("Apply");
                applyButton.setEnabled(false);
                final List<JCheckBox> boxes = new ArrayList<JCheckBox>();

                applyButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {

                        applyButton.setEnabled(false);

                        final List<String> acceptableValues = new ArrayList<String>();
                        for (JCheckBox b : boxes) {
                            if (b.isSelected()) {
                                acceptableValues.add(b.getText());
                            }
                        }

                        if (acceptableValues.size() == boxes.size()) {
                            FilterController.removeFilter(VariantRecordModel.getFieldNameForIndex(fieldNum));
                        } else {
                            Filter f = new QueryFilter() {

                                @Override
                                public Condition[] getConditions() {
                                    Condition[] results = new Condition[acceptableValues.size()];
                                    int i = 0;
                                    for (String s : acceptableValues) {
                                        if(columnAlias.equals(VariantTableSchema.ALIAS_GT)){
                                            results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), uniq.indexOf(s));
                                        } else {
                                            results[i++] = BinaryCondition.equalTo(MedSavantDatabase.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);                                           
                                        }
                                    }
                                    return results;
                                }

                                @Override
                                public String getName() {
                                    return columnAlias;
                                }
                            };
                            //Filter f = new VariantRecordFilter(acceptableValues, fieldNum);
                            System.out.println("Adding filter: " + f.getName());
                            FilterController.addFilter(f);
                        }

                        //TODO: why does this not work? Freezes GUI
                        //apply.setEnabled(false);
                    }
                });

                for (String s : uniq) {
                    JCheckBox b = new JCheckBox(s);
                    b.setSelected(true);
                    b.addChangeListener(new ChangeListener() {

                        public void stateChanged(ChangeEvent e) {
                            AbstractButton abstractButton =
                                    (AbstractButton) e.getSource();
                            ButtonModel buttonModel = abstractButton.getModel();
                            boolean pressed = buttonModel.isPressed();
                            if (pressed) {
                                applyButton.setEnabled(true);
                            }
                            //System.out.println("Changed: a=" + armed + "/p=" + pressed + "/s=" + selected);
                        }
                    });
                    b.setAlignmentX(0F);
                    container.add(b);
                    boxes.add(b);
                }

                JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
                selectAll.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(true);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectAll);

                JButton selectNone = ViewUtil.createHyperLinkButton("Select None");

                selectNone.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        for (JCheckBox c : boxes) {
                            c.setSelected(false);
                            applyButton.setEnabled(true);
                        }
                    }
                });
                bottomContainer.add(selectNone);

                bottomContainer.add(Box.createGlue());

                bottomContainer.add(applyButton);

                bottomContainer.setAlignmentX(0F);
                container.add(bottomContainer);

                FilterView fv = new FilterView(columnAlias, container);
                l.add(fv);
            }
            
            // Get elapsed time in milliseconds
            long elapsedTimeMillis = System.currentTimeMillis()-start;

            // Get elapsed time in seconds
            float elapsedTimeSec = elapsedTimeMillis/1000F;
            
            //System.out.println("Took " + elapsedTimeSec + " seconds to make " + columnAlias + " filter");
        }

        return l;
    }

    private void setStatus(String status) {
        this.status.setText(status);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        setStatus(ViewUtil.numToString(QueryUtil.getNumFilteredVariants(ConnectionController.connect())) + " records pass filters");
        updatePaneEmphasis();
    }
    
    public double getNumber(String s) {
        return Double.parseDouble(s);
    }
}
