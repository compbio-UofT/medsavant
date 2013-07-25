/*
 *    Copyright 2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import com.jidesoft.converter.ConverterContext;
import com.jidesoft.grid.TableHeaderPopupMenuInstaller;
import com.jidesoft.pivot.AggregateTable;
import com.jidesoft.pivot.AggregateTableHeader;
import com.jidesoft.pivot.AggregateTablePopupMenuCustomizer;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_COLLAPSE;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_COLLAPSE_ALL;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_EXPAND;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_EXPAND_ALL;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_GROUP;
import static com.jidesoft.pivot.AggregateTablePopupMenuCustomizer.CONTEXT_MENU_UNGROUP;
import com.jidesoft.pivot.IPivotDataModel;
import com.jidesoft.pivot.PivotConstants;
import com.jidesoft.pivot.PivotField;
import com.jidesoft.pivot.PivotValueProvider;
import com.jidesoft.pivot.SummaryCalculator;
import com.jidesoft.pivot.Value;
import com.jidesoft.pivot.Values;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableColumnModelEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.apache.commons.lang3.ArrayUtils;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.component.KeyValuePairPanel;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.GeneticsTablePage;
import org.ut.biolab.medsavant.client.view.genetics.TablePanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
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
import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.mfiume.query.QueryViewController;

/**
 * Displays a table of other individuals that possess a variant at the same chromosome and position
 * as the VariantRecord passed to the 'handleEvent' method of this Listener.
 */
public class OtherIndividualsSubInspector extends SubInspector implements Listener<VariantRecord> {

    private static final int MAXIMIUM_VARIANTS_TO_FETCH = 1000;
    //Icon to show on 'link' buttons, which can be clicked to load up other variants in sub inspectors.
    private static final ImageIcon LINK_BUTTON_ICON = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.INSPECTOR);
    private static final int BUTTON_COLUMN_PREFERRED_WIDTH = LINK_BUTTON_ICON.getIconWidth() + 20;
    
    //The cohort and family entries that correspond to all individuals. (i.e. all distinct dnaIds in the database).
    private static final Cohort ALL_INDIVIDUALS_COHORT = new Cohort(-1, "All Individuals");
    private static final String ALL_INDIVIDUALS_FAMILY = "All Individuals";
    
    //Various settings that control appearance.
    private static final Font TABLE_FONT = KeyValuePairPanel.KEY_FONT;    
    private static final Font TABLE_FONT_LARGE = ViewUtil.detailFontPlain;
    private static final Dimension PREFERRED_SIZE = new Dimension(ComprehensiveInspector.INSPECTOR_INNER_WIDTH, 300);
    
    //Column names used in the displayed table. 
    private static final String COHORT_COLUMN_NAME = "Cohort";
    private static final String FAMILY_COLUMN_NAME = BasicPatientColumns.FAMILY_ID.getAlias();
    private static final String REF_COLUMN_NAME = "Ref.";
    private static final String ALT_COLUMN_NAME = "Alt.";
    private static final String ZYGOSITY_COLUMN_NAME = BasicVariantColumns.ZYGOSITY.getAlias();
    private static final String DNAID_COLUMN_NAME = BasicVariantColumns.DNA_ID.getAlias();
    
    //Lists valid groupings of columns.  Currently, either Cohort or Family MUST be first.
    private static final String[][] GROUPBYS = {
        {COHORT_COLUMN_NAME, FAMILY_COLUMN_NAME, REF_COLUMN_NAME, ALT_COLUMN_NAME, ZYGOSITY_COLUMN_NAME},
        {FAMILY_COLUMN_NAME, COHORT_COLUMN_NAME, REF_COLUMN_NAME, ALT_COLUMN_NAME, ZYGOSITY_COLUMN_NAME}
    };
    
    //Initial column titles for the table.  The far right column is blank: this is where the buttons go.
    private static final String[] columnNames = ArrayUtils.add(ArrayUtils.add(GROUPBYS[0], DNAID_COLUMN_NAME), "");
    
    //Set of all DNAIds that have a variant at the currently selected chromosome and position.
    private Set<String> individualsWithVariantAtThisPosition;
    
    private String[] aggregateColumns = GROUPBYS[0]; //The current grouping of columns.
    private AggregateTableHeader header;
    private AggregateTable aggregateTable;
    private SubInspectorTableModel tableModel = new SubInspectorTableModel();
    private JPanel innerPanel;
    private JPanel outerPanel;
    private JPanel splitScreenButtonPanel;
    private boolean isSplitScreen = false;
    private static final Log LOG = LogFactory.getLog(OtherIndividualsSubInspector.class);
   
    //Anything >= this column index cannot be grouped, and cannot be moved.
    private final int dnaIDIndex = columnNames.length - 2;
    
    //column index of the 'link' buttons in the table.
    private final int buttonIndex = columnNames.length - 1;
    
    //Used to prevent rearrangement of the first column, and all columns 
    //with index >= dnaIDIndex.  
    private int fromColumnIndex = -1;
    private int toColumnIndex = -1;
    
    
    private MapRefresher mapRefresher; //Thread that refreshes the maps below by invoking server methods.    
    private final Map<String, Set<Cohort>> dnaIDCohortMap = new HashMap<String, Set<Cohort>>();
    private final Map<Cohort, Set<String>> cohortDNAIDMap = new HashMap<Cohort, Set<String>>();       
    private final Map<String, Set<String>> dnaIDFamilyIDMap = new HashMap<String, Set<String>>();
    private final Map<String, Set<String>> familyIDdnaIDMap = new HashMap<String, Set<String>>();
    
    private Listener<Object> variantSelectionListener;

    ///////////////////////PUBLIC
    
    //Constructor
    public OtherIndividualsSubInspector() {        
        TablePanel.addVariantSelectionChangedListener(this);
        refreshMaps();


        splitScreenButtonPanel = new JPanel();
        splitScreenButtonPanel.setLayout(new BoxLayout(splitScreenButtonPanel, BoxLayout.X_AXIS));

        final JButton splitScreenButton = new JButton("Split");
        splitScreenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (!isSplitScreen) {
                    splitScreen();
                    splitScreenButton.setText("Unsplit");
                } else {
                    unsplitScreen();
                    splitScreenButton.setText("Split");
                }

            }
        });

        splitScreenButtonPanel.add(Box.createHorizontalGlue());
        splitScreenButtonPanel.add(splitScreenButton);

        innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.add(new WaitPanel("Loading DNA IDs..."));

        outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.add(splitScreenButtonPanel);
        outerPanel.add(innerPanel);

    }
    
    @Override
    public String getName() {
        return "Other Individuals with Variant";
    }

    /**
     * Adds a listener for whenever a different variant is selected via this 
     * SubInspector.  The listener's handleEvent method will be invoked with
     * the VariantRecord corresponding to the selection.
     * 
     * @see VariantRecord
     */
    public void setVariantSelectionListener(Listener<Object> l) {
        this.variantSelectionListener = l;
    }
    
    
    /**
     * Removes most of this SubInspector (except the title and the split-screen toggle button)
     * and makes a split screen view in the main spreadsheet view.  top half: spreadsheet;
     * bottom half: this SubInspector.
     */
    public void splitScreen() {
        if (!isSplitScreen) {
            SubSectionView ssv = ViewController.getInstance().getCurrentSubSectionView();
            if (ssv instanceof GeneticsTablePage) {
                ((GeneticsTablePage) ssv).splitScreen(innerPanel);
                isSplitScreen = true;
                outerPanel.removeAll();
                outerPanel.add(splitScreenButtonPanel);
                aggregateTable.setFont(TABLE_FONT_LARGE);
                header.setFont(TABLE_FONT_LARGE);
                outerPanel.revalidate();
                outerPanel.repaint();
            }

        }
    }

    /**
     * Reverses the effects of splitScreen - i.e. it unsplits the screen.
     * @see splitScreen
     */
    public void unsplitScreen() {
        if (isSplitScreen) {
            SubSectionView ssv = ViewController.getInstance().getCurrentSubSectionView();
            if (ssv instanceof GeneticsTablePage) {
                ((GeneticsTablePage) ssv).unsplitScreen();
                isSplitScreen = false;
                aggregateTable.setFont(TABLE_FONT);
                header.setFont(TABLE_FONT);
                outerPanel.removeAll();
                outerPanel.add(splitScreenButtonPanel);
                outerPanel.add(innerPanel);
                outerPanel.revalidate();
                outerPanel.repaint();

            }
        }
    }

    @Override
    public JPanel getInfoPanel() {
        return outerPanel;      
    }

    /**
     * Handles the selection of a variantRecord.     
     */
    @Override
    public void handleEvent(VariantRecord variantRecord) {
        
        innerPanel.removeAll();
        innerPanel.add(new WaitPanel("Loading DNA IDs..."));
        LOG.debug("variantRecord position is " + variantRecord.getPosition());
        
        String chrom = variantRecord.getChrom();

        int start = variantRecord.getPosition().intValue();
        int end = start;
        Long endPos = variantRecord.getEndPosition();
        if (endPos != null) {
            end = (int) variantRecord.getEndPosition().intValue();
        }
       
        final String alt = null;
        final GenomicRegion gr = new GenomicRegion("", chrom, start, end);

        new MedSavantWorker<Void>(this.getClass().getName()) {
            @Override
            protected void showSuccess(Void result) {
                aggregateTable = new AggregateTable(tableModel) {
                    
                    @Override
                    //Necessary to enable buttons within the table.
                    public boolean isCellEditable(int rowIndex, int colIndex) {
                        return colIndex == buttonIndex;
                    }

                    //"Disables" selection within the aggregateTable.
                    @Override
                    public TableCellRenderer getCellRenderer(final int rowIndex, final int columnIndex) {
                        final TableCellRenderer renderer = super.getCellRenderer(rowIndex, columnIndex);
                        return new TableCellRenderer() {
                            @Override
                            public Component getTableCellRendererComponent(JTable jtable, final Object o, boolean bln, boolean bln1, int i, int i1) {                               
                                return renderer.getTableCellRendererComponent(jtable, o, false, false, i, i1);                                                              
                            }
                        };
                    }

                    //This overridden method, together with the custom mouse listener on the 
                    //AggregateTable header, disallows moving the first column, and columns
                    //>= dnaIDIndex                    
                    @Override
                    protected AggregateTable.DraggingHandler createDraggingColumnPropertyChangeListener() {
                        return new AggregateTable.DraggingHandler() {
                            @Override
                            public void columnMoved(TableColumnModelEvent e) {
                                if (fromColumnIndex == -1) {
                                    fromColumnIndex = e.getFromIndex();
                                }
                                toColumnIndex = e.getToIndex();
                            }
                        };
                    }
                };

                header = new AggregateTableHeader(aggregateTable);
                header.addMouseListener(new MouseAdapter() {
                    //Disable moving the first column, or columns with index
                    //>=dnaIDIndex
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (toColumnIndex != -1
                                && ((toColumnIndex == 0 || fromColumnIndex == 0)
                                || (toColumnIndex >= dnaIDIndex || fromColumnIndex >= dnaIDIndex))) {
                            aggregateTable.moveColumn(toColumnIndex, fromColumnIndex);
                            String msg = "This column cannot be moved.  Click the 'Options' button to change the first column.";
                            DialogUtils.displayMessage(msg);
                        }
                        fromColumnIndex = -1;
                        toColumnIndex = -1;
                    }
                });
                
                header.setAutoFilterEnabled(false);
                header.setReorderingAllowed(true);                
                header.setFont(TABLE_FONT);                
                aggregateTable.setTableHeader(header);                
                aggregateTable.setAutoResizeMode(AggregateTable.AUTO_RESIZE_ALL_COLUMNS);
                aggregateTable.setFont(TABLE_FONT);

                //Setup a custom "summary".  This is what calculates frequencies when cells are
                //collapsed
                PivotField f = aggregateTable.getAggregateTableModel().getField(BasicVariantColumns.DNA_ID.getAlias());
                f.setSummaryType(PivotConstants.SUMMARY_RESERVED_MAX + 1);          
                aggregateTable.getAggregateTableModel().setSummaryCalculator(new SummaryCalculator() {
                    private Set<String> collapsedDNAIDs = new HashSet<String>();
                    private Values lastRowValues;
                    private int valueCount = 0;
                    private final int SUMMARY_FREQ = PivotConstants.SUMMARY_RESERVED_MAX + 1;

                    @Override
                    public void addValue(PivotValueProvider dataModel, PivotField field, Values rowValues, Values columnValues, Object object) {
                        //this gets called multiple times for all the cells that disappear when 
                        //something is collapsed.  
                        // row[0] is the value of the corresponding first column: e.g. a Cohort or Family. 
                        // columnValues can be ignored (blank)
                        // Object is the value of the item in the cell that disappeared. (i.e. those cells which are being summarized)
                        // field.getName() is the column name corresponding to Object

                        // Useful Debugging code:                      
                        //if(field.getName().equals(BasicVariantColumns.DNA_ID.getAlias())){

                        /*
                         System.out.println("==========");
                         System.out.println("Field : " + field.getName());
                         System.out.println("Row values: ");

                         for (int i = 0; i < rowValues.getCount(); ++i) {
                         System.out.println("\trow[" + i + "] = " + rowValues.getValueAt(i).getValue());

                         }

                         System.out.println("Column values: ");
                         for (int i = 0; i < columnValues.getCount(); ++i) {
                         System.out.println("\tcol[" + i + "] = " + columnValues.getValueAt(i).getValue());
                         }

                         System.out.println("Object: ");
                         System.out.println("\t" + object);
                         System.out.println("==========");
                         */

                        // }
                        if (field.getName().equals(BasicVariantColumns.DNA_ID.getAlias())) {
                            collapsedDNAIDs.add((String) object);
                            lastRowValues = rowValues;
                        } else {
                            lastRowValues = null;
                        }
                        valueCount++;
                    }

                    //Should never be called
                    @Override                   
                    public void addValue(Object o) {
                        LOG.error("Unexpected method invocation in OtherIndividualsSubInspector (1)");                        
                        //throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    //Should never be called
                    @Override
                    public void addValue(IPivotDataModel ipdm, PivotField pf, int i, int i1, Object o) {
                        LOG.error("Unexpected method invocation in OtherIndividualsSubInspector (2)");
                        //throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    //Should never be called
                    @Override
                    public void addValue(PivotValueProvider pvp, PivotField pf, Object o) {
                        LOG.error("Unexpected method invocation in OtherIndividualsSubInspector (3)");
                        //throw new UnsupportedOperationException("Not supported yet."); 
                    }

                    @Override
                    public void clear() {
                        collapsedDNAIDs.clear();
                        valueCount = 0;
                        lastRowValues = null;
                    }

                    @Override
                    public Object getSummaryResult(int type) {                        
                        //if null, then we're not in the DNAId column.  Return null
                        //to show a blank in this cell
                        if (lastRowValues == null) { 
                            return null;
                        }
                        
                        int numIndividuals = getNumberOfIndividualsInGroup(lastRowValues.getValueAt(0));

                        return new Frequency(collapsedDNAIDs.size(), numIndividuals);
                    }

                    private int getNumberOfIndividualsInGroup(Value v) {
                        if (aggregateColumns[0].equals("Cohort")) {
                            //LOG.debug("Getting number of individuals in group " + v.getValue());
                            Set<String> dnaIds = cohortDNAIDMap.get((Cohort) v.getValue());
                            //for (String id : dnaIds) {
                                //LOG.debug("\tGot id " + id);
                            //}
                            return cohortDNAIDMap.get((Cohort) v.getValue()).size();
                        } else if (aggregateColumns[0].equals(BasicPatientColumns.FAMILY_ID.getAlias())) {
                            return familyIDdnaIDMap.get((String) v.getValue()).size();
                        } else {
                            LOG.error("Invalid first column");
                            return -1;
                        }
                    }

                    @Override
                    public long getCount() {
                        return valueCount;
                    }

                    @Override
                    public int getNumberOfSummaries() {
                        return 1;
                    }

                    @Override
                    public String getSummaryName(Locale locale, int i) {
                        return "Frequency";
                    }

                    @Override
                    public int[] getAllowedSummaries(Class<?> type) {
                        return new int[]{SUMMARY_FREQ};
                    }

                    @Override
                    public int[] getAllowedSummaries(Class<?> type, ConverterContext cc) {
                        return new int[]{SUMMARY_FREQ};
                    }
                });


                //Sets up the context menu for clicking column headers.  This will probably not be used
                //frequently.  Limit the available operations to collapsing, expanding, and grouping.
                TableHeaderPopupMenuInstaller installer = new TableHeaderPopupMenuInstaller(aggregateTable);
                installer.addTableHeaderPopupMenuCustomizer(new AggregateTablePopupMenuCustomizer() {
                    @Override
                    public void customizePopupMenu(JTableHeader header, JPopupMenu popup, int clickingColumn) {
                        super.customizePopupMenu(header, popup, clickingColumn);
                        for (int i = 0; i < popup.getComponentCount(); i++) {
                            String menuItemName = popup.getComponent(i).getName();
                            if (!(CONTEXT_MENU_COLLAPSE.equals(menuItemName)
                                    || CONTEXT_MENU_COLLAPSE_ALL.equals(menuItemName)
                                    || CONTEXT_MENU_EXPAND.equals(menuItemName)
                                    || CONTEXT_MENU_EXPAND_ALL.equals(menuItemName)
                                    || CONTEXT_MENU_GROUP.equals(menuItemName)
                                    || CONTEXT_MENU_UNGROUP.equals(menuItemName))) {

                                popup.remove(popup.getComponent(i));

                            }
                        }
                    }
                });

                aggregateTable.getAggregateTableModel().setSummaryMode(true);
                aggregateTable.aggregate(aggregateColumns);
                aggregateTable.setShowContextMenu(false);       
                
                expandAllButLast();
                setupButtonColumn();


                innerPanel.removeAll();
        
                JScrollPane jsp = new JScrollPane(aggregateTable);
                jsp.setPreferredSize(PREFERRED_SIZE);
                final JButton groupByToggleButton = new JButton("Show Fraction of...");

                groupByToggleButton.addMouseListener(new MouseAdapter() {                    
                    @Override
                    public void mouseClicked(MouseEvent me) {                        
                        aggregateTable.revalidate();
                        aggregateTable.repaint();
                        JPopupMenu popupMenu = new JPopupMenu();
                        for (String[] aggregate : GROUPBYS) {
                            popupMenu.add(new GroupByMenuItem(aggregate));
                        }

                        popupMenu.show(groupByToggleButton, me.getX(), me.getY());
                    }
                });

                innerPanel.add(jsp);
                JPanel bp = new JPanel();
                bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
                bp.add(groupByToggleButton);
                bp.add(Box.createHorizontalGlue());
                innerPanel.add(bp);                
                innerPanel.revalidate();

                innerPanel.repaint();
            }

            @Override
            protected Void doInBackground() throws Exception {               
                QueryViewController qvc = SearchBar.getInstance().getQueryViewController();               
                List<Object[]> results = qvc.restrictToRegion(gr, alt, MAXIMIUM_VARIANTS_TO_FETCH);                
                tableModel.setValues(results);
                return null;
            }
        }.execute();
    }
    
    
    ///////////////////////PRIVATE METHODS
    private void setAggregateColumns(String[] ac) {
        this.aggregateColumns = ac;
    }
    
    //fully expands all cells in the table except for the cell immediately preceding the
    //DNAId, which is collapsed.  Thus each row of the table corresponds to a unique
    //(Cohort, Family, Ref, Alt, Zygosity) tuple.
    private void expandAllButLast(){        
        aggregateTable.expandAll();       
        for (int rowIndex = 0; rowIndex <= aggregateTable.getAggregateTableModel().getRowCount(); ++rowIndex) {
            aggregateTable.collapse(rowIndex, dnaIDIndex-1); 
        }
    }
    
    //associates a family with a dnaId, and vice versa
    private void addFamilyToDnaId(String family, String dnaID) {
        Set<String> familyIds = dnaIDFamilyIDMap.get(dnaID);
        if (familyIds == null) {
            familyIds = new HashSet<String>();
        }
        familyIds.add(family);
        dnaIDFamilyIDMap.put(dnaID, familyIds);        

        Set<String> dnaIDs = familyIDdnaIDMap.get(family);
        if (dnaIDs == null) {
            dnaIDs = new HashSet<String>();
        }
        dnaIDs.add(dnaID);        

        familyIDdnaIDMap.put(family, dnaIDs);
    }

    //associates a cohort with a dnaId, and vice versa
    private void addCohortToDnaId(Cohort cohort, String dnaId) {
        Set<Cohort> cohorts = dnaIDCohortMap.get(dnaId);
        if (cohorts == null) {
            cohorts = new HashSet<Cohort>();
        }
        cohorts.add(cohort);
        dnaIDCohortMap.put(dnaId, cohorts);

        Set<String> dnaIDs = cohortDNAIDMap.get(cohort);
        if (dnaIDs == null) {
            dnaIDs = new HashSet<String>();
        }
        dnaIDs.add(dnaId);
        cohortDNAIDMap.put(cohort, dnaIDs);
    }

    private void refreshMaps() {        
        if (mapRefresher != null && mapRefresher.isAlive()) {
            LOG.info("Interrupting map refresher");
            mapRefresher.interrupt();
        }
        mapRefresher = new MapRefresher();
        mapRefresher.start();        
    }
    
     //clears the selection in the main pane and sets all subinspectors to the dnaID
    private void selectVariant(VariantRecord vr) {
        if (variantSelectionListener != null) {
            ViewController.getInstance().getCurrentSubSectionView().clearSelection();
            variantSelectionListener.handleEvent(vr);
        }

    }
   
    // Sets up the column with index 'buttonIndex' so that it 
    // can accomadate buttons.
    private void setupButtonColumn() {
        new ButtonColumn(aggregateTable, buttonIndex);
        aggregateTable.getColumnModel().getColumn(buttonIndex).setPreferredWidth(BUTTON_COLUMN_PREFERRED_WIDTH + 5);
        aggregateTable.getColumnModel().getColumn(buttonIndex).setMaxWidth(BUTTON_COLUMN_PREFERRED_WIDTH + 5);
        aggregateTable.getColumnModel().getColumn(buttonIndex).setMinWidth(BUTTON_COLUMN_PREFERRED_WIDTH + 5);
    }
    
    
    /////////////////////// INNER CLASSES
    
    //An item in the popup menu that controls what fraction is displayed.
    //(e.g. fraction of cohort vs fraction of family)
    private class GroupByMenuItem extends JMenuItem implements ActionListener {
        private String[] aggregateColumns;

        public GroupByMenuItem(String[] aggregateColumns, String title) {
            setText(title);
            this.aggregateColumns = aggregateColumns;
            addActionListener(this);
        }
        
        public GroupByMenuItem(String[] aggregateColumns) {            
            this(aggregateColumns, aggregateColumns[0]);            
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            aggregateTable.aggregate(aggregateColumns);
            setAggregateColumns(aggregateColumns);
            setupButtonColumn();
            expandAllButLast();
            aggregateTable.revalidate();
            aggregateTable.repaint();
        }
    };

    //Model for the displayed table.
    private class SubInspectorTableModel extends AbstractTableModel {

        private List<Object[]> tableData = new ArrayList<Object[]>();

        @Override
        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        @Override
        public int getRowCount() {
            return tableData.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Object r = tableData.get(row)[col];            
            //sanity check: r should not be null
            if(r == null){
                LOG.debug("Null table entry for variant");
                return "-";
            }
            return r;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            tableData.get(row)[col] = value;
            fireTableCellUpdated(row, col);
        }

        //Initializes the table with values fetched from the database
        public void setValues(List<Object[]> values) {
            individualsWithVariantAtThisPosition = new HashSet<String>();
            tableData = new ArrayList<Object[]>(values.size());

            int rowIndex = 0;
            for (final Object[] row : values) {
                String dnaID = (String) row[BasicVariantColumns.INDEX_OF_DNA_ID];

                individualsWithVariantAtThisPosition.add(dnaID);

                Set<Cohort> cohorts = dnaIDCohortMap.get(dnaID);
                Set<String> familyIds = dnaIDFamilyIDMap.get(dnaID);

                //sanity checks: it should not be possible for either cohorts or 
                //familyIds to be null, as all dnaIds should map to 
                //ALL_INDIVIDUALS_COHORT or ALL_INDIVIDUALS_FAMILY, at the very
                //least.
                if(cohorts == null){
                    LOG.error("Cohorts is null for dnaID "+dnaID);
                    cohorts = new HashSet<Cohort>();
                    cohorts.add(ALL_INDIVIDUALS_COHORT);
                }
                
                if(familyIds == null){
                    LOG.error("Family ID is null for dnaID "+dnaID);
                    familyIds = new HashSet<String>();
                    familyIds.add(ALL_INDIVIDUALS_FAMILY);
                }

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
                
                for (Cohort cohort : cohorts) {
                    for (String familyId : familyIds) {
                        tableData.add(new Object[]{
                            cohort,
                            familyId,
                            ref,
                            alt,
                            zygosity,
                            dnaID,
                            r
                        });
                    }
                }               
            }
            this.fireTableDataChanged();
        }
    };
    
    //Thread that refreshes the four one-to-many mappings
    //(dnaId->cohort, dnaId->Family, family->dnaId, cohort->dnaId)
    private class MapRefresher extends Thread {
        @Override
        public void run() {
            synchronized (dnaIDCohortMap) {
                dnaIDCohortMap.clear();
                dnaIDFamilyIDMap.clear();
                try {
                    String sessionID = LoginController.getSessionID();
                    Cohort[] cohorts = MedSavantClient.CohortManager.getCohorts(sessionID, ProjectController.getInstance().getCurrentProjectID());
                    for (Cohort cohort : cohorts) {
                        List<String> dnaIds = MedSavantClient.CohortManager.getDNAIDsForCohort(sessionID, cohort.getId());
                        for (String dnaId : dnaIds) {
                            if (Thread.interrupted()) {
                                return;
                            }
                            addCohortToDnaId(cohort, dnaId);
                        }
                    }

                    Map<Object, List<String>> m = MedSavantClient.PatientManager.getDNAIDsForValues(sessionID, ProjectController.getInstance().getCurrentProjectID(), BasicPatientColumns.FAMILY_ID.getColumnName());
                    for (Map.Entry<Object, List<String>> e : m.entrySet()) {
                        for (String dnaId : e.getValue()) {
                            addFamilyToDnaId((String) e.getKey(), dnaId);                            
                            if (Thread.interrupted()) {
                                return;
                            }
                        }
                    }

                    //FOR DEBUGGING ONLY, with the crm9 database                    
                    //addFamilyToDnaId("110", "330-AD-01");
                    //addFamilyToDnaId("110", "330-DD-03");
                    //addFamilyToDnaId("111", "330-ED-05");
                    //addFamilyToDnaId("111", "330-GD-02");

                    LOG.debug("Counting unique " + BasicVariantColumns.DNA_ID.getColumnName() + " from table " + ProjectController.getInstance().getCurrentVariantTableName());
                    List<String> allDNAIds = MedSavantClient.DBUtils.getDistinctValuesForColumn(sessionID,
                            ProjectController.getInstance().getCurrentVariantTableName(),
                            BasicVariantColumns.DNA_ID.getColumnName(),
                            true); //caching=true

                    LOG.debug("# Distinct DNAIds in Variant Table: " + allDNAIds.size());
                    for (String dnaId : allDNAIds) {
                        addFamilyToDnaId(ALL_INDIVIDUALS_FAMILY, dnaId);
                        //LOG.debug("Mapping " + ALL_INDIVIDUALS_COHORT + " to " + dnaId);
                        addCohortToDnaId(ALL_INDIVIDUALS_COHORT, dnaId);
                    }
                } catch (SQLException se) {
                    LOG.error(se);
                } catch (RemoteException re) {
                    LOG.error(re);
                } catch (SessionExpiredException se) {
                    MedSavantExceptionHandler.handleSessionExpiredException(se);
                } catch (InterruptedException ie) {
                    LOG.error(ie);
                }
            }
        }
    };

    //Handles the calculation and formatting of frequencies from counts.
    private class Frequency {

        private int numerator, denominator;

        public Frequency(int numerator, int denominator) {
            this.numerator = numerator;
            this.denominator = denominator;
        }

        @Override
        public String toString() {
            double frac = numerator / (double) denominator;
            DecimalFormat df = new DecimalFormat("######.##");
            String ret = numerator + " / " + denominator + " (" + df.format(frac) + ")";
            return ret;
        }
    }

   
    /**
     * Modified from http://tips4java.wordpress.com/2009/07/12/table-button-column/
     * 
     * The ButtonColumn class provides a renderer and an editor that looks like
     * a JButton. The renderer and editor will then be used for a specified
     * column in the table. The TableModel will contain the String to be
     * displayed on the button.     
     *
     */
    public class ButtonColumn extends AbstractCellEditor
            implements TableCellRenderer, TableCellEditor, MouseListener {

        private JTable table;
        private JButton renderButton;
        private Object editorValue;
        private boolean isButtonColumnEditor;

        public ButtonColumn(JTable table, int column) {
            this.table = table;
            renderButton = new JButton();

            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
            table.removeMouseListener(this);
            table.addMouseListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable table, final Object value, boolean isSelected, int row, int column) {


            if (value instanceof VariantRecord) {
                JButton b = new JButton(LINK_BUTTON_ICON);
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        fireEditingStopped();
                        VariantRecord vr = (VariantRecord) value;
                        System.out.println("Got VariantRecord " + vr);
                        selectVariant(vr);
                    }
                });
                b.setToolTipText("View information for this variant and individual in Inspector");
                JPanel p = new JPanel();
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                p.setBackground(aggregateTable.getBackground());
                p.add(Box.createHorizontalGlue());
                p.add(b);
                p.add(Box.createHorizontalGlue());
                return p;
            } else {
                return new JLabel("");
            }
        }

        @Override
        public Object getCellEditorValue() {
            return editorValue;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            renderButton.setBackground(table.getBackground());
            if (value instanceof VariantRecord) {
                renderButton.setIcon(LINK_BUTTON_ICON);
                renderButton.setToolTipText("View information for this variant and individual in Inspector");
                JPanel p = new JPanel();
                p.setBackground(aggregateTable.getBackground());
                p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                p.add(Box.createHorizontalGlue());
                p.add(renderButton);
                p.add(Box.createHorizontalGlue());
                return p;
                //return renderButton;
            } else {
                return new JLabel("");
            }
        }

        /*
         *  When the mouse is pressed the editor is invoked. If you then then drag
         *  the mouse to another cell before releasing it, the editor is still
         *  active. Make sure editing is stopped when the mouse is released.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (table.isEditing()
                    && table.getCellEditor() == this) {
                isButtonColumnEditor = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (isButtonColumnEditor
                    && table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }

            isButtonColumnEditor = false;
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }
}
