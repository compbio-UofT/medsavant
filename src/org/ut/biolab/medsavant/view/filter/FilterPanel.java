/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.filter;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import com.jidesoft.swing.RangeSlider;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import medsavant.db.BasicQuery;
import medsavant.db.ConnectionController;
import medsavant.db.Database;
import medsavant.db.table.TableSchema;
import medsavant.db.table.VariantTableSchema;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.filter.geneontology.*;

/**
 *
 * @author mfiume
 */
public class FilterPanel extends JPanel {

    private final ArrayList<FilterView> filterViews;
    private CollapsiblePanes contentPanel;
    private SelectQueryGO selectStatementGO;

    public FilterPanel() {
        this.setLayout(new BorderLayout());
        filterViews = new ArrayList<FilterView>();
        initGUI();
    }
    

    private void initGUI() {

        contentPanel = new CollapsiblePanes();
        contentPanel.setBackground(ViewUtil.getMenuColor());
        this.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        List<FilterView> fv;
        try {
            fv = getFilterViews();
            addFilterViews(fv);
        } catch (SQLException ex) {
            throw new FatalDatabaseException("Problem getting filters");
        }

        contentPanel.addExpansion();
    }

    public void addFilterViews(List<FilterView> filterViews) {
        for (FilterView view : filterViews) {
            addFilterView(view);
        }
    }

    private void addFilterView(FilterView view) {
        filterViews.add(view);
        CollapsiblePane cp = new CollapsiblePane(view.getTitle());
        try {
            cp.setCollapsed(true);
        } catch (PropertyVetoException ex) {
        }
        cp.setCollapsedPercentage(0);
        cp.setContentPane(view.getComponent());
        this.contentPanel.add(cp);
    }

    private List<FilterView> getFilterViews() throws SQLException {
        List<FilterView> views = new ArrayList<FilterView>();
        views.addAll(getVariantRecordFilterViews());
        views.add(getGOntologyFilterView()); 
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());
        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        // views.add(getGenderFilterView());
        // views.add(getAgeFilterView());

        return views;
    }
    
    /**
     * Return the filter view for the gene ontology filter.
     * @return 
     */
    private FilterView getGOntologyFilterView(){
        
        final JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        FilterView gontologyFilterView = new FilterView("Gene Ontology", container);
        // Add button to ask whether the person wants to see the tree.
//        final JButton buttonShowTree = new JButton("Show tree");
//        container.add(buttonShowTree);
//        buttonShowTree.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent e) { 
//                container.remove(buttonShowTree);
//                // show progress bar while loading data, and then show the tree.
//                loadData(container);
//            }
//        });
        
        // Start loading the information at the beginning.
        loadData(container);

        return gontologyFilterView;
    }
    
    /**
     * Load data once the user has indicated the wish to show the tree (ie, 
     * pressed on the button asking to show the tree).
     * @param container 
     */
    private void loadData(final JPanel container){
                
        final JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(false);
        container.add(progressBar);
        
          class Task extends SwingWorker{
              
              XTree xtree;

                @Override
                protected Object doInBackground() throws Exception {
                    setProgress(0);
                    // Create the mappings file at a certain destination 
                    // then show the tree.
                    String destination = CreateMappingsFile.getMappings();
                    xtree = XMLontology.makeTree(destination);
                    setProgress(100);
                    return xtree;
                }
            }

        try{

            final Task task = new Task();
            
            task.addPropertyChangeListener(new PropertyChangeListener() {

                // To detect progress with the loading of the tree.
                public void propertyChange(PropertyChangeEvent evt) {
                    
                    if ("progress".equals(evt.getPropertyName()) && 
                            (Integer)(evt.getNewValue()) == 100){

                        progressBar.setIndeterminate(false);
                        progressBar.setVisible(false);
                        container.remove(progressBar);
                        // When we're done loading the information, show tree.
                        showTree(container, task.xtree);
                    }   
                    else if ("state".equals(evt.getPropertyName()) && 
                            "STARTED".equals(evt.getNewValue() + "")){

                        progressBar.setIndeterminate(true);
                    }
                }
            });
            
            // Try to load the information into the tree (i.e., download XML 
            // file etc)
            task.execute();
        }
        // If we could not show the tree (load data), say that we could not.
        catch(Exception e){

            container.remove(progressBar);
            container.add(new JLabel("Could not display the tree"));
            System.out.println("Could not display the tree.");
        }
    }
    
    /**
     * Set up the panel so that the jtree component can be seen, along with the
     * button asking to "apply" a filter.
     * @param container
     * @param xtree 
     */
    private void showTree(JPanel container, XTree xtree){
            final JButton applyButton = new JButton("Apply");
            
        // Construct jtree from xtree that has been made.
        // Put tree in scrollpane, and scrollpane in panel.
        final JTree jTree = getTree(xtree);
        // Add a listener to the tree.  Note: tree can allow non-contiguous
        // multiple selections.
        jTree.addTreeSelectionListener(new TreeSelectionListener() {

            public void valueChanged(TreeSelectionEvent e) {
                // Iff no path has been selected, make the button non-clickable.
//                if (jTree.isSelectionEmpty()){
//                    applyButton.setEnabled(false);
//                }
//                else{
//                    applyButton.setEnabled(true);
//                }
                applyButton.setEnabled(true);
            }
        }); 

        JScrollPane scrollpane = new JScrollPane(jTree);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(Box.createVerticalBox());
        container.add(scrollpane);
        scrollpane.setAlignmentX(0F);

        JPanel bottomContainer = new JPanel();
        
        
//        bottomContainer.add(Box.createVerticalBox());
        JButton selectAll = ViewUtil.createHyperLinkButton("Select All");
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // select all the nodes in the tree.
                System.out.println("All nodes in the tree selected; not yet implemented");
            }
        });
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.add(selectAll);
        JButton selectNone = ViewUtil.createHyperLinkButton("Select None");
        selectNone.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // deselect any node in the tree.
                jTree.clearSelection();
                FilterController.removeFilter("position gene Ont");
            }
        });
        bottomContainer.add(selectNone);
        
        bottomContainer.add(Box.createGlue());
        bottomContainer.add(applyButton);
        bottomContainer.setAlignmentX(0F);

        container.add(bottomContainer);
        
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {

            // Note that an action could have been performed only if some tree
            // element has been selected.
            public void actionPerformed(ActionEvent e) {
                
                applyButton.setEnabled(false); 
                // Select query statement for GO.
                selectStatementGO = new SelectQueryGO();
                HashSet<String> locations = new HashSet<String>();
                System.out.println("Pressed apply for gene ontology filter");
                TreePath[] paths = jTree.getSelectionPaths();
                
                if (paths != null){
                    for (TreePath path: paths){
                        DefaultMutableTreeNode currNode = 
                                (DefaultMutableTreeNode)path.getLastPathComponent();
                        XNode currXnode = (XNode) currNode.getUserObject();
                        ArrayList<ArrayList<String>> arrayLocs = currXnode.getLocs();

                        for (ArrayList<String> arrayLoc: arrayLocs){
                            // Need to subtract 1 because of BED format.
                            Double formattedEnd = 
                                    Integer.parseInt(arrayLoc.get(3).trim()) - 1 + 0.0;
                            Double begin = Integer.parseInt(arrayLoc.get(2).trim()) + 0.0;
                            selectStatementGO.addCondition(arrayLoc.get(1), begin, formattedEnd);

                            String str = arrayLoc.get(1).trim() + "_" + 
                                    arrayLoc.get(2).trim() + "_" + formattedEnd;
                            locations.add(str);
                        }
                    }
                }
//                System.out.println(selectStatementGO);
                // If there are no conditions at all, do not display
                // anything (most intuitive). So, create bogus condition that 
                // will never be satisfied.
                if (selectStatementGO.getConditions().isEmpty() && paths != null){
                    selectStatementGO.addCondition("chr1", 23, 22); 
                }
                final HashMap<String, List<Range>> map = selectStatementGO.getConditions();
                
//                System.out.println(locations);
                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() {
                        
                        Condition[] conds = new Condition[map.keySet().size()];
                        int i = 0;
                       
                        for (String key: map.keySet()){
                            
                            List<ComboCondition> listInnerCond = 
                                    new ArrayList<ComboCondition>();
                            List<Range> ranges = map.get(key);
                            for (Range range: ranges){

                                BinaryCondition innerCond1 = BinaryCondition.greaterThan
                                        (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.POSITION_COL), range.getMin(), true);
                                BinaryCondition innerCond2 = BinaryCondition.lessThan
                                        (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.POSITION_COL), range.getMax(), true);
                                BinaryCondition[] condTogether = {innerCond1, innerCond2};
                                listInnerCond.add(ComboCondition.and(condTogether));
                            } // for each range for the chromosome of interest.
                            BinaryCondition chrCond = BinaryCondition.equalTo
                                    (Database.getInstance().getVariantTableSchema().getDBColumn(SelectQueryGO.CHROM_COL), key);
                            conds[i++] = ComboCondition.and(chrCond, ComboCondition.or(listInnerCond.toArray()));
                        } // for each chromosome.
                        return conds;
                    }

                    @Override
                    public String getName() {
                        return " position gene Ont";
                    }
                };
                System.out.println("Adding Filter" + f.getName());
                FilterController.addFilter(f);
            }
        });        
    }
    
    // Obtain the JTree component to be added to the panel, given the xtree.
    private JTree getTree(XTree xtree){
        
        // "dummy" root of the tree.
        XNode root = new XNode("...");
        root.setDescription("...");
//        root.setLocs(new ArrayList< ArrayList<String> >());

        DefaultMutableTreeNode actualRoot = new DefaultMutableTreeNode(root);
        // Add the nodes beneath the root node to this tree.
        addNodes(actualRoot, xtree);
        JTree jtree = new JTree(actualRoot);
        jtree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        return jtree;
    }
    
    // Add the nodes to form part of the jtree.
    private void addNodes(DefaultMutableTreeNode actualRoot, XTree xtree){
        
        // To contain the roots of the tree.
        Set<XNode> roots = xtree.getRootNodes();
        
        // Get the name of the children while going down the tree.
        TreeSet<XNode> children;
        
        // The child in consideration in context.
        DefaultMutableTreeNode child;
        
        // To contain the parent nodes (to be used when displaying) in question.
        List<DefaultMutableTreeNode> parentNodes = 
                new ArrayList<DefaultMutableTreeNode>();
        
        
        // To contain the children nodes in question.
        List<DefaultMutableTreeNode> childrenNodes = 
                new ArrayList<DefaultMutableTreeNode>();
        
        
        // Add all roots to the tree.
        for (XNode root: roots){
        
            // Connect the root to its children.
            child = new DefaultMutableTreeNode(root);
            actualRoot.add(child);
            
            // The future parents to be considered.
            parentNodes.add(child);
        }
        
        // While we still have children nodes...
        while(!parentNodes.isEmpty()){
            
            // Go through the tree in a breadth-first manner.
            for (DefaultMutableTreeNode parent: parentNodes){

                // Get the set of children, and have the parents accept their
                // children.
                children = xtree.getChildrenNodes
                        (((XNode)parent.getUserObject()).getIdentifier());

                for (XNode child2: children){

                    child = new DefaultMutableTreeNode(child2);
                    childrenNodes.add(child);
                    parent.add(child);
                }
            }

            // Now have the children become parents.
            parentNodes.clear();
            parentNodes.addAll(childrenNodes);
            childrenNodes.clear();           
        }

    }

    void listenToComponent(final JCheckBox c) {
        c.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //setApplyButtonEnabled(true);
            }
        });
    }

    private List<FilterView> getVariantRecordFilterViews() throws SQLException {
        List<FilterView> l = new ArrayList<FilterView>();

        System.out.println("Making filters");

        List<String> fieldNames = VariantRecordModel.getFieldNames();
        int numFields = fieldNames.size();

        for (int i = 0; i < numFields; i++) {

            final int fieldNum = i;
            Class c = VariantRecordModel.getFieldClass(i);

            final String columnAlias = fieldNames.get(i);

            if (columnAlias.equals(VariantTableSchema.ALIAS_ID) || columnAlias.equals(VariantTableSchema.ALIAS_INFORMATION) || columnAlias.equals(VariantTableSchema.ALIAS_FILTER)) {
                continue;
            }

            TableSchema table = Database.getInstance().getVariantTableSchema();
            DbColumn col = table.getDBColumn(columnAlias);
            boolean isNumeric = TableSchema.isNumeric(table.getColumnType(col));

            if (isNumeric) {
                Range extremeValues = BasicQuery.getExtremeValuesForColumn(ConnectionController.connect(), table, col);

                JPanel container = new JPanel();
                container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

                RangeSlider rs = new com.jidesoft.swing.RangeSlider();

                rs.setMinimum((int) Math.floor(extremeValues.getMin()));
                rs.setMaximum((int) Math.ceil(extremeValues.getMax()));

                rs.setMajorTickSpacing(5);
                rs.setMinorTickSpacing(1);

                rs.setLowValue((int) Math.floor(extremeValues.getMin()));
                rs.setHighValue((int) Math.ceil(extremeValues.getMax()));

                container.add(rs);
                container.add(Box.createVerticalBox());

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
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
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

                rs.addChangeListener(new ChangeListener() {

                    public void stateChanged(ChangeEvent e) {
                        applyButton.setEnabled(true);
                    }

                });

                container.add(applyButton);

                final FilterView fv = new FilterView(columnAlias,container);
                l.add(fv);
            } else {

                List<String> uniq = BasicQuery.getDistinctValuesForColumn(ConnectionController.connect(), table, col);

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
                                        results[i++] = BinaryCondition.equalTo(Database.getInstance().getVariantTableSchema().getDBColumn(columnAlias), s);
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
        }

        return l;
    }

    /*
    private Set<String> getUniqueValuesOfVariantRecordsAtField(int i) {
    Set<String> result = new TreeSet<String>();

    /**
     * TODO: this should query the database
     *
    List<VariantRecord> records;
    try {
    records = ResultController.getInstance().getFilteredVariantRecords();
    } catch (Exception ex) {
    Logger.getLogger(FilterPanel.class.getName()).log(Level.SEVERE, null, ex);
    DialogUtil.displayErrorMessage("Problem getting data.", ex);
    return null;
    }

    for (VariantRecord r : records) {
    Object o = VariantRecordModel.getValueOfFieldAtIndex(i, r);
    if (o == null) {
    result.add("<none>");
    } else {
    result.add(o.toString());
    }
    }

    return result;
    }
     * 
     */
}
