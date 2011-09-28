/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.filter;

import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.utils.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ProjectController.ProjectListener;
import org.ut.biolab.medsavant.db.util.jobject.VariantQueryUtil;
import org.ut.biolab.medsavant.olddb.QueryUtil;
import org.ut.biolab.medsavant.olddb.ConnectionController;
import org.ut.biolab.medsavant.olddb.MedSavantDatabase;
import org.ut.biolab.medsavant.olddb.table.TableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantTableSchema;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.olddb.table.ModifiableColumn;
import org.ut.biolab.medsavant.olddb.table.PatientTableSchema;
import org.ut.biolab.medsavant.olddb.table.PhenotypeTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationGatkTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationPolyphenTableSchema;
import org.ut.biolab.medsavant.olddb.table.VariantAnnotationSiftTableSchema;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;
import org.ut.biolab.medsavant.view.filter.pathways.PathwaysPanel;
import org.ut.biolab.medsavant.view.genetics.filter.FilterView.FilterViewType;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume, AndrewBrook
 */
public class FilterPanel extends JPanel implements FiltersChangedListener, ProjectListener {
    
    private ArrayList<FilterView> filterViews;
    private CollapsiblePanes filterContainer;
    private JLabel status;
    private JPanel contentPlaceholder;
    private HashMap<String, CollapsiblePane> filterToCPMap;

    public void projectAdded(String projectName) {}

    public void projectRemoved(String projectName) {}

    public void projectChanged(String projectName) {
        refreshFilters();
    }

    public void projectTableRemoved(int projid, int refid) {}
    
    public enum FilterWidgetType { INT, FLOAT, STRING, BOOLEAN };

    public FilterPanel() throws NonFatalDatabaseException {
        this.setName("Filters");
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        filterToCPMap = new HashMap<String,CollapsiblePane>();
        FilterController.addFilterListener(this);
        initGUI();
        ProjectController.getInstance().addProjectListener(this);
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
            
            String tablename = ProjectController.getInstance().getCurrentTableName();
            
            views.add(VariantStringListFilterView.createFilterView(tablename, "dna_id"));
            views.add(VariantStringListFilterView.createFilterView(tablename, "chrom"));
            views.add(VariantNumericFilterView.createFilterView(tablename, "position")); 
            views.add(VariantNumericFilterView.createFilterView(tablename, "qual"));    
            views.add(VariantStringListFilterView.createFilterView(tablename, "ref")); 
            views.add(VariantStringListFilterView.createFilterView(tablename, "alt"));
            
            //COHORT FILTERS////////////////////////////////////////////////////
            /*views.add(CohortFilterView.getCohortFilterView());
            views.add(GeneListFilterView.getFilterView());

            //INDIVIDUAL FILTERS////////////////////////////////////////////////           
            PatientTableSchema patientTable = MedSavantDatabase.getInstance().getPatientTableSchema();
            PhenotypeTableSchema phenotypeTable = MedSavantDatabase.getInstance().getPhenotypeTableSchema();
            views.add(PatientStringListFilterView.createFilterView(patientTable, "Family ID", PatientTableSchema.ALIAS_FAMILYID));
            views.add(GenderFilterView2.getGenderFilterView());    
            for(ModifiableColumn m : patientTable.getModColumns()){
                if(m.isMandatory() || !m.isFilter())continue;
                FilterView f = createDnaIdFilter(patientTable, m);
                if(f != null) views.add(f);
            }
            for(ModifiableColumn m : phenotypeTable.getModColumns()){
                if(m.isMandatory() || !m.isFilter())continue;
                FilterView f = createDnaIdFilter(phenotypeTable, m);
                if(f != null) views.add(f);
            }      
            
            
            //views.add(PatientStringListFilterView.createFilterView("Ethnic Group", PatientTableSchema.ALIAS_ETHGROUP, true));
            //views.add(PatientNumericFilterView.createFilterView("IQ Verbal", PatientTableSchema.ALIAS_IQWVERB));
            //views.add(PatientNumericFilterView.createFilterView("IQ Performance", PatientTableSchema.ALIAS_IQWPERF));
            //views.add(PatientNumericFilterView.createFilterView("IQ Full Score", PatientTableSchema.ALIAS_IQWFULL));
            
            //VARIANT FILTERS///////////////////////////////////////////////////
            views.addAll(getVariantRecordFilterViews());
            
            //CUSTOM FILTERS////////////////////////////////////////////////////
            //views.add(GOFilter.getGOntologyFilterView());
            //views.add(HPOFilter.getHPOntologyFilterView());                     
            views.add(new FilterView("WikiPathways", new PathwaysPanel()));
  
            //save and dispose of cache
            FilterCache.saveAndDispose();*/
                        
            return views;
        }
        
        private FilterView createDnaIdFilter(TableSchema table, ModifiableColumn m){
            switch(m.getType()){
                case VARCHAR:
                    return PatientStringListFilterView.createFilterView(table, m.getShortName(), m.getShortName());
                case BOOLEAN:
                    return PatientBooleanFilterView.createFilterView(table, m.getShortName(), m.getShortName());
                case INTEGER:
                case FLOAT:
                case DECIMAL:
                    if(m.getLength()>1){
                        return PatientNumericFilterView.createFilterView(table, m.getShortName(), m.getShortName());
                    } else {
                        return PatientBooleanFilterView.createFilterView(table, m.getShortName(), m.getShortName()); //TINYINT
                    }
                case DATE:
                    System.err.println("Date field filters are not supported yet. ");
                default:
                    return null;
            }
        }
        
        @Override
        protected Object doInBackground() throws Exception {
            
            Object o = getFilterViews();
            return o;
        }
        
        protected void done() {
            try {
                contentPlaceholder.removeAll();
                
                Object o = get();

                filterContainer = new CollapsiblePanes();
                filterContainer.setBackground(ViewUtil.getMenuColor());

                JScrollPane p1 = new JScrollPane(filterContainer);
                p1.setBorder(null);
                
                contentPlaceholder.add(p1, BorderLayout.CENTER);
                
                List<FilterView> views = (List<FilterView>) o;
                addFilterViews(views);
                filterContainer.addExpansion();
                
                JPanel refreshPanel = new JPanel();
                JLabel refreshButton = new JLabel("Refresh Filters");            
                refreshPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                refreshPanel.addMouseListener(new MouseListener() {
                    public void mouseClicked(MouseEvent e) {
                        refreshFilters();
                    }
                    public void mousePressed(MouseEvent e) {}
                    public void mouseReleased(MouseEvent e) {}
                    public void mouseEntered(MouseEvent e) {}
                    public void mouseExited(MouseEvent e) {}
                });
                refreshPanel.add(refreshButton);
                filterContainer.add(refreshPanel);
                
                contentPlaceholder.updateUI();
            } catch (Error ex) {
                contentPlaceholder.add(ViewUtil.getMessagePanel("Problem getting filters"));
                //ex.printStackTrace();
                Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                contentPlaceholder.add(ViewUtil.getMessagePanel("Problem getting filters"));
                //ex.printStackTrace();
                Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void refreshFilters(){
        filterViews = new ArrayList<FilterView>();
        filterToCPMap = new HashMap<String,CollapsiblePane>();
        this.removeAll();
        try {
            initGUI();
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 

    void listenToComponent(final JCheckBox c) {

        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }
    
    /*private FilterView createVariantRecordFilter(TableSchema table, String columnAlias) throws SQLException, NonFatalDatabaseException {     
        DbColumn col = table.getDBColumn(columnAlias);
        FilterWidgetType fwt;
        if(TableSchema.isInt(table.getColumnType(col)) || TableSchema.isFloat(table.getColumnType(col))){
            return VariantNumericFilterView.createFilterView(table, columnAlias);
        } else if (TableSchema.isBoolean(table.getColumnType(col))){
            return VariantBooleanFilterView.createFilterView(table, columnAlias);
        } else {
            return VariantStringListFilterView.createFilterView(table, columnAlias);
        }
    }
    
    private List<FilterView> getVariantRecordFilterViews() throws SQLException, NonFatalDatabaseException {
        
        List<FilterView> l = new ArrayList<FilterView>();
        TableSchema table;
        List<String> fieldNames;
        
        table = MedSavantDatabase.getInstance().getVariantTableSchema();
        fieldNames = table.getFieldAliases();
        for(int i = 0; i < fieldNames.size(); i++){
            String columnAlias = fieldNames.get(i);
            if (    columnAlias.equals(VariantTableSchema.ALIAS_DBSNPID) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_GENOMEID) ||
                    columnAlias.equals(VariantTableSchema.ALIAS_FILTER) || 
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
                    columnAlias.equals(VariantTableSchema.ALIAS_CUSTOMINFO)) continue;
            FilterView f = createVariantRecordFilter(table, columnAlias);
            if(f != null) l.add(f);
        }
                
        table = MedSavantDatabase.getInstance().getVariantSiftTableSchema();
        fieldNames = table.getFieldAliases();
        for(int i = 0; i < fieldNames.size(); i++){
            String columnAlias = fieldNames.get(i);                       
            if (    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_GENOMEID) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_CHROM) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_POSITION) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_REF) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_ALT) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_NAME_SIFT) ||
                    columnAlias.equals(VariantAnnotationSiftTableSchema.ALIAS_NAME2_SIFT)) continue;
            FilterView f = createVariantRecordFilter(table, columnAlias);
            if(f != null) l.add(f);
        }
        
        table = MedSavantDatabase.getInstance().getVariantPolyphenTableSchema();
        fieldNames = table.getFieldAliases();
        for(int i = 0; i < fieldNames.size(); i++){
            String columnAlias = fieldNames.get(i);
            if (    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_ACC) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_ALT) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_CDNACOORD) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_CHROM) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_CODPOS) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_CPG) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_GENOMEID) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_IDPMAX) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_IDPSNP) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_IDQMIN) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_MINDJNC) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_OAA1) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_OAA2) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_OPOS) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_POS) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_POSITION) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2CLASS) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FDR) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2FPR) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2PROB) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PPH2TPR) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_PREDICTION) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_REF) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_SNPID) ||
                    columnAlias.equals(VariantAnnotationPolyphenTableSchema.ALIAS_TRANSV)) continue;
            FilterView f = createVariantRecordFilter(table, columnAlias);
            if(f != null) l.add(f);
        }
        
        table = MedSavantDatabase.getInstance().getVariantGatkTableSchema();
        fieldNames = table.getFieldAliases();
        for(int i = 0; i < fieldNames.size(); i++){
            String columnAlias = fieldNames.get(i);
            if(     columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_ALT) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_CHANGESAA) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_CHROM) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_CODINGCOORDSTR) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_CODONCOORD) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_FRAME) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_FUNCTIONALCLASS) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_GENOMEID) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_INCODINGREGION) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_MRNACOORD) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_NAME2_GATK) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_NAME_GATK) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_POSITION) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_POSITIONTYPE) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_PROTEINCOORDSTR) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_REF) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_REFERENCEAA) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_REFERENCECODON) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_SPLICEDIST) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFO) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_SPLICEINFOCOPY) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_TRANSCRIPTSTRAND) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_UORFCHANGE) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_VARIANTAA) ||
                    columnAlias.equals(VariantAnnotationGatkTableSchema.ALIAS_VARIANTCODON)) continue;
            FilterView f = createVariantRecordFilter(table, columnAlias);
            if(f != null) l.add(f);                    
        }

        return l;     
    }*/
    
    private void setStatus(String status) {
        this.status.setText(status);
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        //setStatus(ViewUtil.numToString(QueryUtil.getNumFilteredVariants(ConnectionController.connect())) + " records pass filters");
        setStatus(ViewUtil.numToString(VariantQueryUtil.getNumFilteredVariants(
                ProjectController.getInstance().getCurrentProjectId(), 
                ProjectController.getInstance().getCurrentReferenceId(), 
                FilterController.getQueryFilterConditions())));
        updatePaneEmphasis();
    }
   
}
