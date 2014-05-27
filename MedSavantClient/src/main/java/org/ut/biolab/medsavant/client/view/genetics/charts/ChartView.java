/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics.charts;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.BasicPatientColumns;
import org.ut.biolab.medsavant.shared.format.BasicVariantColumns;
import org.ut.biolab.medsavant.shared.format.CustomField;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.view.genetics.charts.SummaryChart.ChartAxis;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class ChartView extends JPanel implements BasicPatientColumns, BasicVariantColumns {

    private SummaryChart sc;
    private JComboBox chartChooser1;
    private JComboBox chartChooser2;
    private Map<String, ChartMapGenerator> mapGenerators;
    private JCheckBox bPie;
    private JCheckBox bSort;
    private JCheckBox bLogY;
    private JCheckBox bLogX;
    private JCheckBox bScatter;
    private String pageName;
    private boolean init = false;
    private JCheckBox bOriginal;
    private JPanel bottomToolbar;

    public ChartView(String pageName) throws RemoteException, SQLException {
        this.pageName = pageName;
        mapGenerators = new HashMap<String, ChartMapGenerator>();

        setLayout(new BorderLayout());
        initToolBar();
        initCards();
        initBottomBar();
        init = true;
        chartChooser1.setSelectedItem(DNA_ID.getAlias());
    }

    private void initToolBar() {

        JPanel toolbar = ViewUtil.getSubBannerPanel("Chart");
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));

        chartChooser1 = new JComboBox() {
            @Override
            public void addItem(Object anObject) {                
                int size = ((DefaultComboBoxModel) dataModel).getSize();
                Object obj;
                boolean added = false;
                for (int i = 0; i < size; i++) {
                    obj = dataModel.getElementAt(i);
                    int compare = anObject.toString().compareToIgnoreCase(obj.toString());
                    if (compare <= 0) { // if anObject less than or equal obj
                        super.insertItemAt(anObject, i);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    super.addItem(anObject);
                }
            }
        };
        toolbar.add(chartChooser1);

        chartChooser1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!init) {
                    return;
                }
                String alias = (String) chartChooser1.getSelectedItem();
                ChartMapGenerator cmg = mapGenerators.get(alias);
                if (alias.equals(CHROM.getAlias())) {
                    bSort.setEnabled(false);
                    sc.setIsSortedKaryotypically(true);
                } else if (cmg.isNumeric()) {
                    bSort.setEnabled(false);
                    sc.setIsSortedKaryotypically(false);
                } else {
                    bSort.setEnabled(true);
                    sc.setIsSortedKaryotypically(false);
                }
                sc.setChartMapGenerator(cmg);
                sc.setChartName(alias);
                //updateScatterAxes();
                sc.setScatterChartMapGenerator(mapGenerators.get((String) chartChooser2.getSelectedItem()));
                bLogX.setEnabled(cmg.isNumeric());

                sc.setUpdateRequired(true);
                sc.updateIfRequired();
            }
        });

        toolbar.add(Box.createHorizontalGlue());

        bScatter = new JCheckBox("Group By");
        bScatter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chartChooser2.setEnabled(bScatter.isSelected());
                sc.setIsScatterChart(bScatter.isSelected());

                bottomToolbar.setVisible(!bScatter.isSelected());

                sc.setUpdateRequired(true);
                sc.updateIfRequired();
            }
        });
        toolbar.add(bScatter);

        chartChooser2 = new JComboBox() {
            @Override
            public void addItem(Object anObject) {
                int size = ((DefaultComboBoxModel) dataModel).getSize();
                Object obj;
                boolean added = false;
                for (int i = 0; i < size; i++) {
                    obj = dataModel.getElementAt(i);
                    int compare = anObject.toString().compareToIgnoreCase(obj.toString());
                    if (compare <= 0) { // if anObject less than or equal obj
                        super.insertItemAt(anObject, i);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    super.addItem(anObject);
                }
            }
        };
        chartChooser2.setEnabled(false);
        toolbar.add(chartChooser2);

        chartChooser2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!init) {
                    return;
                }
                //updateScatterAxes();
                sc.setScatterChartMapGenerator(mapGenerators.get((String) chartChooser2.getSelectedItem()));

                sc.setUpdateRequired(true);
                sc.updateIfRequired();
                /*String alias = (String) chartChooser2.getSelectedItem();
                 ChartMapGenerator cmg = mapGenerators.get(alias);
                 if (alias.equals(VariantFormat.ALIAS_OF_CHROM)) {
                 bSort.setEnabled(false);
                 sc.setIsSortedKaryotypically(true);
                 } else if (cmg.isNumeric()) {
                 bSort.setEnabled(false);
                 sc.setIsSortedKaryotypically(false);
                 } else {
                 bSort.setEnabled(true);
                 sc.setIsSortedKaryotypically(false);
                 }
                 sc.setChartMapGenerator(cmg);
                 bLogX.setEnabled(cmg.isNumeric());*/
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

        //toolbar.add(Box.createHorizontalGlue());

        b1.setSelected(true);

        this.add(toolbar, BorderLayout.NORTH);
    }

    private void initCards() throws RemoteException, SQLException {
        initAllCard();
        addCMGs();
    }

    private void initAllCard() {

        JPanel h1 = new JPanel();
        h1.setLayout(new GridLayout(1, 1));

        sc = new SummaryChart(pageName);

        h1.add(sc, BorderLayout.CENTER);

        add(h1, BorderLayout.CENTER);
    }

    private void addCMG(String key, ChartMapGenerator cmg) {
        mapGenerators.put(key, cmg);
        chartChooser1.addItem(key);
        chartChooser2.addItem(key);
    }

    private void addCMG(ChartMapGenerator cmg) {
        addCMG(cmg.getName(), cmg);
    }

    private void addCMGs() throws RemoteException, SQLException {

        AnnotationFormat[] afs = ProjectController.getInstance().getCurrentAnnotationFormats();
        for (AnnotationFormat af : afs) {
            for (CustomField field : af.getCustomFields()) {
                ColumnType type = field.getColumnType();
                if (field.isFilterable()
                        && (type.equals(ColumnType.VARCHAR) || type.equals(ColumnType.BOOLEAN) || type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER))
                        && !(field.getColumnName().equals(FILE_ID.getColumnName()))
                        || field.getColumnName().equals(UPLOAD_ID.getColumnName())
                        || field.getColumnName().equals(DBSNP_ID.getColumnName())
                        || field.getColumnName().equals(VARIANT_ID.getColumnName())) {

                    String program = af.getProgram();
                    String name = field.getAlias() + " (" + program + ")";
                    addCMG(name, VariantFieldChartMapGenerator.createVariantChart(field));
                }
            }
        }
        for (CustomField field : ProjectController.getInstance().getCurrentPatientFormat()) {
            ColumnType type = field.getColumnType();
            if (field.isFilterable()
                    && (type.equals(ColumnType.VARCHAR) || type.equals(ColumnType.BOOLEAN) || type.equals(ColumnType.DECIMAL) || type.equals(ColumnType.FLOAT) || type.equals(ColumnType.INTEGER))
                    && !(field.getColumnName().equals(PATIENT_ID.getColumnName())
                    || field.getColumnName().equals(FAMILY_ID.getColumnName())
                    || field.getColumnName().equals(IDBIOMOM.getColumnName())
                    || field.getColumnName().equals(IDBIODAD.getColumnName()))) {
                addCMG(VariantFieldChartMapGenerator.createPatientChart(field));
            }
        }

    }

    private void initBottomBar() {
        bottomToolbar = ViewUtil.getSecondaryBannerPanel();
        bottomToolbar.setBorder(ViewUtil.getTopLineBorder());
        bottomToolbar.setLayout(new BoxLayout(bottomToolbar, BoxLayout.X_AXIS));

        bottomToolbar.add(Box.createHorizontalGlue());

        //bottomToolbar.add(chartChooser);

        bPie = new JCheckBox("Pie chart");
        bOriginal = new JCheckBox("Show original frequencies");
        bSort = new JCheckBox("Sort by frequency");
        bLogY = new JCheckBox("Log scale Y axis");
        bLogX = new JCheckBox("Log scale X axis");

        bPie.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsPie(!sc.isPie());
            }
        });

        bOriginal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDoesCompareToOriginal(!sc.doesCompareToOriginal());
            }
        });

        bSort.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsSorted(!sc.isSorted());
            }
        });

        bLogY.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsLogScale(!sc.isLogScaleY(), ChartAxis.Y);
            }
        });

        bLogX.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setIsLogScale(!sc.isLogScaleX(), ChartAxis.X);
            }
        });

        bottomToolbar.add(ViewUtil.getMediumSeparator());

        bottomToolbar.add(ViewUtil.clear(bPie));
        bottomToolbar.add(ViewUtil.clear(bOriginal));
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
            if (b) {
                bOriginal.setEnabled(false);
            } else {
                bOriginal.setEnabled(true);
            }
        }
    }

    public void setDoesCompareToOriginal(boolean b) {
        if (bOriginal.isEnabled()) {
            sc.setDoesCompareToOriginal(!sc.doesCompareToOriginal());
            bOriginal.setSelected(sc.doesCompareToOriginal());
        }
    }

    public void setIsSorted(boolean b) {
        if (bSort.isEnabled()) {
            sc.setIsSorted(b);
        }
    }

    public void setIsLogScale(boolean b, ChartAxis axis) {
        if ((axis == ChartAxis.Y && !bLogY.isEnabled()) || (axis == ChartAxis.X && !bLogX.isEnabled())) {
            return;
        }
        sc.setIsLogScale(b, axis);
        bLogY.setSelected(sc.isLogScaleY());
        bLogX.setSelected(sc.isLogScaleX());
    }

    /*public void setIsLogScaleY(boolean b) {
     if (bLogY.isEnabled()) {
     sc.setIsLogScaleY(!sc.isLogScaleY());
     bLogY.setSelected(sc.isLogScaleY());
     if (sc.isLogScaleY()) {
     sc.setIsLogScaleX(false);
     bLogX.setSelected(false);
     }
     }
     }

     public void setIsLogScaleX(boolean b) {
     if (bLogX.isEnabled()) {
     sc.setIsLogScaleX(!sc.isLogScaleX());
     bLogX.setSelected(sc.isLogScaleX());
     if (sc.isLogScaleX()) {
     sc.setIsLogScaleY(false);
     bLogY.setSelected(false);
     }
     }
     }*/
    public void updateIfRequired() {
        if (sc != null) {
            sc.updateIfRequired();
        }
    }

    public void setUpdateRequired(boolean required) {
        if (sc != null) {
            sc.setUpdateRequired(required);
        }
    }
}
