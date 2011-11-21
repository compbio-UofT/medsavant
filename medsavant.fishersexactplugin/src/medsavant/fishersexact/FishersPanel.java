/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FishersPanel.java
 *
 * Created on 12-Sep-2011, 5:01:04 PM
 */
package medsavant.fishersexact;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.model.structure.TableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.db.util.query.VariantQueryUtil;
import org.ut.biolab.medsavant.view.dialog.IndeterminateProgressDialog;


/**
 *
 * @author Andrew
 */
public class FishersPanel extends JPanel {

    private List<ColumnPanel> columnPanels = new ArrayList<ColumnPanel>();
    private List<CustomField> columns = new ArrayList<CustomField>();
    
    /** Creates new form FishersPanel */
    public FishersPanel() {
        initComponents();
        generateColumnList();
        
        this.columnPanel.setLayout(new BoxLayout(columnPanel, BoxLayout.Y_AXIS));        
        
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JLabel label1 = new JLabel("Choose dependent column:");
        label1.setPreferredSize(new Dimension(300,30));
        p1.add(label1);
        p1.add(Box.createHorizontalStrut(15));
        p1.add(new JLabel("Cutoff:"));
        p1.add(Box.createHorizontalGlue());
        this.columnPanel.add(p1);
        
        addColumnPanel(false);     
        
        JPanel p2 = new JPanel(new BorderLayout());
        JLabel label2 = new JLabel("Choose columns to test:");
        label2.setPreferredSize(new Dimension(200,30));
        p2.add(label2, BorderLayout.WEST);
        this.columnPanel.add(p2);

        this.addColumnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.addColumnButton.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                addColumnPanel(true);
            }
        });
        
        addColumnPanel(false);
                
        table.setModel(createTableModel(new Object[0][]));
    }
    
    private DefaultTableModel createTableModel(Object[][] data){
        String[] columnNames = new String[]{"Field Name", "P-Value"};
        return new DefaultTableModel(data, columnNames);
    }
    
    private void addColumnPanel(boolean removable){      
        ColumnPanel p = new ColumnPanel(removable);
        this.columnPanel.add(p);
        columnPanels.add(p);
        this.updateUI();       
    }
    
    private void removeColumnPanel(ColumnPanel p){
        this.columnPanel.remove(p);
        columnPanels.remove(p);
        this.updateUI();
    }
    
    private void addColumnsToCombo(JComboBox combo){
        
        for(CustomField field : columns){
            combo.addItem(field);
        }
        if(!columns.isEmpty()){
            combo.setSelectedIndex(0);
        }
    }
    
    private void generateColumnList(){       
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(CustomField field : af.getCustomFields()){
                ColumnType type = field.getColumnType();
                if(type == ColumnType.BOOLEAN || type == ColumnType.DECIMAL || type == ColumnType.FLOAT || type == ColumnType.INTEGER){
                    columns.add(field);
                }
            }
        }
    }
        
    private void run() throws NonFatalDatabaseException, SQLException{
        
        final IndeterminateProgressDialog dialog = new IndeterminateProgressDialog(
                "Performing Test", 
                "Performing Fisher's Exact test. Please wait.", 
                true);
        
        Thread thread = new Thread() {
            @Override
            public void run() {

                TableSchema tableSchema = ProjectController.getInstance().getCurrentVariantTableSchema();
                int projectid = ProjectController.getInstance().getCurrentProjectId();
                int referenceid = ReferenceController.getInstance().getCurrentReferenceId();              
                Condition[][] filterConditions = FilterController.getQueryFilterConditions();

                BigDecimal[] p_values = new BigDecimal[columnPanels.size()-1];

                int a, b, c, d;    

                String d_name = columnPanels.get(0).getValue().getColumnName();        
                Double d_cutoff = columnPanels.get(0).getCutoff();

                Condition[] d_conditions = null;
                try {
                    d_conditions = generateConditions(tableSchema, d_name, d_cutoff);
                } catch (Exception ex) {
                    dialog.close();  
                    return;
                }

                Condition[][] queryConditions = new Condition[filterConditions.length][];
                for(int i = 0; i < filterConditions.length; i++){
                    queryConditions[i] = new Condition[filterConditions[i].length + 2];
                    System.arraycopy(filterConditions[i], 0, queryConditions[i], 0, filterConditions[i].length);
                }

                for(int i = 1; i < columnPanels.size(); i++){

                    String name = columnPanels.get(i).getValue().getColumnName();
                    Double cutoff = columnPanels.get(i).getCutoff();

                    Condition[] conditions = null;
                    try {
                        conditions = generateConditions(tableSchema, name, cutoff);
                        
                        addConditionsForQuery(queryConditions, filterConditions, d_conditions[0], conditions[0]);
                        a = VariantQueryUtil.getNumFilteredVariants(projectid, referenceid, queryConditions);
                        addConditionsForQuery(queryConditions, filterConditions, d_conditions[1], conditions[0]);
                        b = VariantQueryUtil.getNumFilteredVariants(projectid, referenceid, queryConditions);
                        addConditionsForQuery(queryConditions, filterConditions, d_conditions[0], conditions[1]);
                        c = VariantQueryUtil.getNumFilteredVariants(projectid, referenceid, queryConditions);
                        addConditionsForQuery(queryConditions, filterConditions, d_conditions[1], conditions[1]);
                        d = VariantQueryUtil.getNumFilteredVariants(projectid, referenceid, queryConditions);

                        if(a > 5000 || b > 5000 || c > 5000 || d > 5000){ //TODO: This is an arbitrary maximum. Should test to find an actual reasonable limit. 
                            p_values[i-1] = null;
                            continue;
                        }
                        
                        p_values[i-1] = FishersTest.fishersExact(a, b, c, d);
                        
                    } catch (Exception ex) {
                        Logger.getLogger(FishersPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

                //String[] columnNames = new String[]{"Field Name", "P-Value"};
                Object[][] rowData = new Object[columnPanels.size()-1][2];
                for(int i = 1; i < columnPanels.size(); i++){
                    rowData[i-1][0] = columnPanels.get(i).getValue();
                    if(p_values[i-1] != null){
                        rowData[i-1][1] = p_values[i-1];
                    } else {
                        rowData[i-1][1] = "System too big to compute";
                    }
                }        
                table.setModel(createTableModel(rowData));

                dialog.close();  
            }
        };
        thread.start(); 
        dialog.setVisible(true);
        
    }
    
    private void addConditionsForQuery(Condition[][] queryConditions, Condition[][] filterConditions, Condition d_condition, Condition condition){
        for(int subQuery = 0; subQuery < queryConditions.length; subQuery++){
            int start = filterConditions[subQuery].length;
            queryConditions[subQuery][start] = d_condition;
            queryConditions[subQuery][start+1] = condition;
        }
    }
    
    private Condition[] generateConditions(TableSchema table, String alias, Double cutoff) throws Exception{
        Condition[] conditions = new Condition[2];
        DbColumn col = table.getDBColumn(alias);
        ColumnType type = table.getColumnType(col);
        
        if(TableSchema.isNumeric(type) && cutoff == null) {
            JOptionPane.showMessageDialog(null, "You must specify a cutoff value for non-boolean fields.", "Error", JOptionPane.ERROR_MESSAGE);
            throw new Exception();
        }
        
        if(TableSchema.isBoolean(type)){
            conditions[0] = BinaryCondition.equalTo(col, Boolean.FALSE);
            conditions[1] = BinaryCondition.equalTo(col, Boolean.TRUE);
        } else if (TableSchema.isInt(type)){
            conditions[0] = BinaryCondition.lessThan(col, (int)cutoff.doubleValue(), true);
            conditions[1] = BinaryCondition.greaterThan(col, (int) cutoff.doubleValue(), false);
        } else {
            conditions[0] = BinaryCondition.lessThan(col, cutoff.doubleValue(), true);
            conditions[1] = BinaryCondition.greaterThan(col, cutoff.doubleValue(), false);
        }
        return conditions;
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel3 = new javax.swing.JLabel();
        addColumnButton = new javax.swing.JLabel();
        columnPanel = new javax.swing.JPanel();
        runButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel3.setText("Fisher's Exact Test");

        addColumnButton.setFont(new java.awt.Font("Tahoma", 1, 10));
        addColumnButton.setText("+ Add another column");

        javax.swing.GroupLayout columnPanelLayout = new javax.swing.GroupLayout(columnPanel);
        columnPanel.setLayout(columnPanelLayout);
        columnPanelLayout.setHorizontalGroup(
            columnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 452, Short.MAX_VALUE)
        );
        columnPanelLayout.setVerticalGroup(
            columnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 24, Short.MAX_VALUE)
        );

        runButton.setText("Perform Test");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 447, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(columnPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addColumnButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(runButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(columnPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addColumnButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(runButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
        try {
            run();
        } catch (NonFatalDatabaseException ex) {
            Logger.getLogger(FishersPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(FishersPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_runButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addColumnButton;
    private javax.swing.JPanel columnPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton runButton;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    
    class ColumnPanel extends JPanel {
        
        private JTextField cutoffField;
        private JComboBox combo;
        private JButton removeButton;
        private Component[] boxes = new Component[]{Box.createHorizontalStrut(15)};
        
        public ColumnPanel(boolean removable){
           
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            
            combo = new JComboBox();
            combo.setPreferredSize(new Dimension(300, 22));    
            combo.setSize(new Dimension(300, 22));    
            combo.setMaximumSize(new Dimension(300, 22));    
            combo.setAlignmentX(0);
            addColumnsToCombo(combo);
            combo.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if(e.getStateChange() == ItemEvent.SELECTED){
                        setNewItem((CustomField)e.getItem());
                    }                   
                }
            });            
            this.add(combo);

            this.add(boxes[0]);
            
            cutoffField = new JTextField();
            cutoffField.setPreferredSize(new Dimension(150,22));
            cutoffField.setSize(new Dimension(150,22));
            cutoffField.setMaximumSize(new Dimension(150,22));
            this.add(cutoffField);
            
            //setRangeVisible(false);
            
            this.add(Box.createRigidArea(new Dimension(15, 30)));
            
            if(removable){
                addRemoveButton();
            }        
            
            this.add(Box.createHorizontalGlue());
            if(combo.getItemCount() > 0){
                combo.setSelectedIndex(0);
                setNewItem((CustomField)combo.getSelectedItem());
            }
        }
        
        private void addRemoveButton(){
            removeButton = new JButton("Remove");
            final ColumnPanel instance = this;
            removeButton.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    removeColumnPanel(instance);
                }
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
                public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
            });
            this.add(removeButton);
        }
        
        private void setRangeVisible(boolean visible){
            cutoffField.setEnabled(visible);
        }
        
        private void setNewItem(CustomField field){
            setRangeVisible(field.isNumeric());
        }
        
        public CustomField getValue(){
            return (CustomField)combo.getSelectedItem();
        }
        
        public Double getCutoff(){   
            try {
                return Double.parseDouble(this.cutoffField.getText());
            } catch (Exception e){
                return null;
            }
        }
        
    }
    
}
