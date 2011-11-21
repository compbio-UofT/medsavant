/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.charts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.format.CustomField;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase;
import org.ut.biolab.medsavant.db.api.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.db.model.structure.TableSchema.ColumnType;
import org.ut.biolab.medsavant.view.genetics.charts.SummaryChart.ChartAxis;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChartView extends JPanel {

    private SummaryChart sc;
    private JComboBox chartChooser;
    private Map<String, ChartMapGenerator> mapGenerators;
    private JCheckBox bPie;
    private JCheckBox bSort;
    private JCheckBox bLogY;
    private JCheckBox bLogX;
    private String pageName;
    private boolean init = false;

    public ChartView(String pageName) {
        this.pageName = pageName;
        mapGenerators = new HashMap<String, ChartMapGenerator>();
        initGUI();
        this.chartChooser.setSelectedItem(MedSavantDatabase.DefaultvariantTableSchema.getFieldAlias(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM));
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
        initToolBar();
        initCards();  
        initBottomBar();       
        init = true;
        chartChooser.setSelectedIndex(0);
    }

    private void initToolBar() {

        JPanel toolbar = ViewUtil.getSubBannerPanel("Chart");
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));

        chartChooser = new JComboBox();
        toolbar.add(chartChooser);

        chartChooser.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if(!init) return;
                String alias = (String) chartChooser.getSelectedItem();
                ChartMapGenerator cmg = mapGenerators.get(alias);
                sc.setChartMapGenerator(cmg);
                bLogX.setEnabled(cmg.isNumeric());
                if (bSort == null) { return; }
                if (alias.equals(MedSavantDatabase.DefaultvariantTableSchema.getFieldAlias(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM))) {
                    bSort.setEnabled(false);
                    sc.setIsSortedKaryotypically(true);
                } else {
                    bSort.setEnabled(true);
                    sc.setIsSortedKaryotypically(false);
                }
                
            }
        });

        ButtonGroup rg = new ButtonGroup();

        JRadioButton b1 = new JRadioButton("All");
        JRadioButton b2 = new JRadioButton("Cohort");

        rg.add(b1);
        rg.add(b2);

        //toolbar.add(ViewUtil.getMediumSeparator());

        //toolbar.add(ViewUtil.clear(b1));
        //toolbar.add(ViewUtil.clear(b2));

        toolbar.add(Box.createHorizontalGlue());

        b1.setSelected(true);

        this.add(toolbar, BorderLayout.NORTH);
    }

    private void initCards() {
        initAllCard();
        addCMGs();
    }

    private void initAllCard() {

        JPanel h1 = new JPanel();
        h1.setLayout(new GridLayout(1, 1));

        sc = new SummaryChart(pageName);

        h1.add(sc, BorderLayout.CENTER);

        this.add(h1, BorderLayout.CENTER);
    }

    private void addCMG(ChartMapGenerator cmg) {
        mapGenerators.put(cmg.getName(), cmg);
        chartChooser.addItem(cmg.getName());    
    }

    private void addCMGs() {
        
        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for(AnnotationFormat af : afs){
            for(CustomField field : af.getCustomFields()){
                ColumnType type = field.getColumnType();
                if(field.isFilterable() &&
                        (type.equals(ColumnType.VARCHAR) || type.equals(ColumnType.BOOLEAN) || type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER))){
                    addCMG(VariantFieldChartMapGenerator.createVariantChart(field));
                    if(type == ColumnType.BOOLEAN || (type == ColumnType.INTEGER && Integer.parseInt(field.getColumnLength()) == 1)) System.out.println(field.getAlias());
                }
            }
        }
        for(CustomField field : ProjectController.getInstance().getCurrentPatientFormat()){
            ColumnType type = field.getColumnType();
            if(field.isFilterable() &&
                        (type.equals(ColumnType.VARCHAR) || type.equals(ColumnType.BOOLEAN) || type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER))){
                addCMG(VariantFieldChartMapGenerator.createPatientChart(field));
            }
        }
        
    }

    private void initBottomBar() {
        JPanel bottomToolbar = ViewUtil.getSecondaryBannerPanel();
        bottomToolbar.setBorder(ViewUtil.getTinyLineBorder());
        bottomToolbar.setLayout(new BoxLayout(bottomToolbar, BoxLayout.X_AXIS));

        bottomToolbar.add(Box.createHorizontalGlue());

        //bottomToolbar.add(chartChooser);

        bPie = new JCheckBox("Pie chart");
        bSort = new JCheckBox("Sort by frequency");
        bLogY = new JCheckBox("Log scale Y axis");
        bLogX = new JCheckBox("Log scale X axis");        

        bPie.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIsPie(!sc.isPie());
            }
        });
        
        bSort.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIsSorted(!sc.isSorted());
            }
        });
         
        bLogY.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIsLogScale(!sc.isLogScaleY(), ChartAxis.Y);
            }
        });
        
        bLogX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setIsLogScale(!sc.isLogScaleX(), ChartAxis.X);
            }
        });
        
        bottomToolbar.add(ViewUtil.getMediumSeparator());

        bottomToolbar.add(ViewUtil.clear(bPie));
        bottomToolbar.add(ViewUtil.clear(bSort));
        bottomToolbar.add(ViewUtil.clear(bLogY));
        bottomToolbar.add(ViewUtil.clear(bLogX));

        bottomToolbar.add(Box.createHorizontalGlue());

        // b1.setSelected(true);

        this.add(bottomToolbar, BorderLayout.SOUTH);
    }

    public void setIsPie(boolean b) {
        if (bPie.isEnabled()) {
            sc.setIsPie(!sc.isPie());
            bPie.setSelected(sc.isPie());
        }
    }
    
    public void setIsSorted(boolean b) {
        if (bSort.isEnabled()) {
            sc.setIsSorted(!sc.isSorted());
            bSort.setSelected(sc.isSorted());
        }
    }
    
    public void setIsLogScale(boolean b, ChartAxis axis){
        if((axis == ChartAxis.Y && !bLogY.isEnabled()) || (axis == ChartAxis.X && !bLogX.isEnabled())) return;
        sc.setIsLogScale(b, axis);
        bLogY.setSelected(sc.isLogScaleY());
        bLogX.setSelected(sc.isLogScaleX());
    }
    
    /*public void setIsLogScaleY(boolean b) {
        if (bLogY.isEnabled()) {
            sc.setIsLogScaleY(!sc.isLogScaleY());
            bLogY.setSelected(sc.isLogScaleY());
            if(sc.isLogScaleY()){
                sc.setIsLogScaleX(false);
                bLogX.setSelected(false);
            }
        }
    }
    
    public void setIsLogScaleX(boolean b) {
        if (bLogX.isEnabled()) {
            sc.setIsLogScaleX(!sc.isLogScaleX());
            bLogX.setSelected(sc.isLogScaleX());
            if(sc.isLogScaleX()){
                sc.setIsLogScaleY(false);
                bLogY.setSelected(false);
            }
        }
    }*/
    
    public void updateIfRequired(){
        if(sc != null){
            sc.updateIfRequired();
        }
    }
    
    public void cleanUp(){
        FilterController.removeFilterListener(sc);
    }
}
