/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.client.view.list.DetailedView;
import org.ut.biolab.medsavant.client.view.list.SimpleDetailedListModel;
import org.ut.biolab.medsavant.client.view.list.SplitScreenView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.list.DefaultNiceListColorScheme;
import org.ut.biolab.medsavant.client.view.util.list.NiceList;
import org.ut.biolab.medsavant.client.view.util.list.NiceListItem;

/**
 *
 * @author mfiume
 */
class QueryConditionDialog extends JDialog {

    private Map<String, List<String>> categoryToConditionNameMap;
    private String chosenConditionName;
    private NiceList conditionNameList;

    public QueryConditionDialog(Map<String, List<String>> categoryToConditionNameMap) {
        super(MedSavantFrame.getInstance(), "Choose a Search Condition", true);
        this.categoryToConditionNameMap = categoryToConditionNameMap;
        initDialog();
    }

    private void initDialog() {

        SplitScreenView view = new SplitScreenView(
                new SimpleDetailedListModel<String>("Condition Category") {
                    @Override
                    public String[] getData() throws Exception {
                        Set<String> keys = categoryToConditionNameMap.keySet();
                        List<String> keysList = new ArrayList<String>(keys);
                        Collections.sort(keysList);
                        Collections.reverse(keysList);
                        String[] result = new String[keysList.size()];
                        int counter = 0;
                        for (String k : keysList) {
                            result[counter++] = k;
                        }
                        return result;
                    }
                },
                new DetailedView("Category") {
            
                    private void setCategory(String category) {
                        this.removeAll();
                        conditionNameList = new NiceList();

                        List<String> itemNames = categoryToConditionNameMap.get(category);
                        Collections.sort(itemNames);

                        for (String s : itemNames) {
                            NiceListItem item = new NiceListItem(s);
                            conditionNameList.addItem(item);
                        }

                        JPanel p = new JPanel();
                        p.setBackground(conditionNameList.getColorScheme().getBackgroundColor());
                        p.setLayout(new BorderLayout());
                        JScrollPane scroll = ViewUtil.getClearBorderlessScrollPane(conditionNameList);
                        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        p.add(scroll, BorderLayout.CENTER);

                        this.setLayout(new BorderLayout());
                        
                        JPanel searchPanel = new JPanel();
                        searchPanel.setLayout(new MigLayout("fillx"));
                        searchPanel.setBorder(ViewUtil.getBottomLineBorder());
                        searchPanel.add(conditionNameList.getSearchBar(),"width 300, center");
                        searchPanel.setBackground(conditionNameList.getColorScheme().getBackgroundColor());
                        
                        this.add(searchPanel, BorderLayout.NORTH);
                        this.add(p, BorderLayout.CENTER);
                        this.updateUI();

                        conditionNameList.addMouseListener(new MouseAdapter() {
                            public void mouseClicked(MouseEvent evt) {
                                JList list = (JList) evt.getSource();
                                if (evt.getClickCount() == 2) {
                                    chooseSelectedItem();
                                }
                            }
                        });

                    }

                    @Override
                    public void setSelectedItem(Object[] selectedRow) {
                        String category = (String) selectedRow[0];
                        setCategory(category);
                    }

                    @Override
                    public void setMultipleSelections(List<Object[]> selectedRows) {
                    }
                },
                new DetailedListEditor() {
                });

        view.setListColorScheme(new DefaultNiceListColorScheme());

        //p.add(list.getSearchBar(), BorderLayout.NORTH);
        //p.add(ViewUtil.getClearBorderlessScrollPane(list), BorderLayout.CENTER);
        this.setContentPane(view);

        this.setSize(new Dimension(800, 600));
        this.setLocationRelativeTo(MedSavantFrame.getInstance());
    }

    private void chooseSelectedItem() {
        if (conditionNameList != null) {
            chosenConditionName = (String) ((NiceListItem) conditionNameList.getSelectedValue()).getItem();
        } else {
            chosenConditionName = null;
        }
        
        this.setVisible(false);
    }
    
    String getChosenConditionName() {
        return chosenConditionName;
    }

}
