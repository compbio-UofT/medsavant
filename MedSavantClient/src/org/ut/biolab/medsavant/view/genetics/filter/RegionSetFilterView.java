/*
 *    Copyright 2011 University of Toronto
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

package org.ut.biolab.medsavant.view.genetics.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.FilterController;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RangeCondition;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.model.QueryFilter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState.FilterType;

/**
 *
 * @author mfiume
 */
class RegionSetFilterView extends FilterView {
    private static final Log LOG = LogFactory.getLog(RegionSetFilterView.class);

    public static final String FILTER_NAME = "Region Set";
    public static final String FILTER_ID = "region_set";
    private static final String REGION_SET_NONE = "None";

    private Integer appliedID;
    private ActionListener al;
    private JComboBox regionsCombo;

    public RegionSetFilterView(FilterState state, int queryID) throws SQLException, RemoteException {
        this(queryID, new JPanel());
        if (state.getValues().get("value") != null) {
            applyFilter(Integer.parseInt(state.getValues().get("value")));
        }
    }

    public RegionSetFilterView(int queryID, JPanel container) throws SQLException, RemoteException {
        super(FILTER_NAME, container, queryID);
        createContentPanel(container);
    }

    public final void applyFilter(int regionSetID) {
        for (int i = 0; i < regionsCombo.getItemCount(); i++) {
            if (regionsCombo.getItemAt(i) instanceof RegionSet && ((RegionSet)regionsCombo.getItemAt(i)).getID() == regionSetID) {
                regionsCombo.setSelectedIndex(i);
                al.actionPerformed(new ActionEvent(this, 0, null));
                return;
            }
        }
    }

    private List<RegionSet> getDefaultValues() throws SQLException, RemoteException {
        return MedSavantClient.RegionSetManager.getRegionSets(LoginController.sessionId);
    }

    private void createContentPanel(JPanel p) throws SQLException, RemoteException {

        p.setLayout(new BorderLayout());
        p.setMaximumSize(new Dimension(1000,80));

        regionsCombo = new JComboBox();
        regionsCombo.setMaximumSize(new Dimension(1000,30));

        regionsCombo.addItem(REGION_SET_NONE);
        List<RegionSet> geneLists = getDefaultValues();
        for (RegionSet set : geneLists) {
            regionsCombo.addItem(set);
        }

        final JButton applyButton = new JButton("Apply");
        applyButton.setEnabled(false);

        al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                applyButton.setEnabled(false);

                Filter f = new QueryFilter() {

                    @Override
                    public Condition[] getConditions() throws SQLException, RemoteException {

                        if (regionsCombo.getSelectedItem().equals(REGION_SET_NONE)) {
                            return new Condition[0];
                        }

                        RegionSet regionSet = (RegionSet) regionsCombo.getSelectedItem();
                        appliedID = regionSet.getID();

                        List<GenomicRegion> regions = MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.sessionId, regionSet.getID(), Integer.MAX_VALUE);
                        Map<String, List<Range>> rangeMap = GenomicRegion.mergeGenomicRegions(regions);
                        Condition[] results = new Condition[rangeMap.size()];
                        int i = 0;
                        for (String chrom : rangeMap.keySet()) {

                            Condition[] tmp = new Condition[2];

                            //add chrom condition
                            tmp[0] = BinaryConditionMS.equalTo(
                                    ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_CHROM),
                                    chrom);

                            //create range conditions
                            List<Range> ranges = rangeMap.get(chrom);
                            Condition[] rangeConditions = new Condition[ranges.size()];
                            for (int j = 0; j < ranges.size(); j++) {
                                rangeConditions[j] = new RangeCondition(
                                        ProjectController.getInstance().getCurrentVariantTableSchema().getDBColumn(DefaultVariantTableSchema.COLUMNNAME_OF_POSITION),
                                        (long)ranges.get(j).getMin(),
                                        (long)ranges.get(j).getMax());
                            }

                            //add range conditions
                            tmp[1] = ComboCondition.or(rangeConditions);

                            results[i] = ComboCondition.and(tmp);

                            i++;
                        }

                        return results;
                    }

                    @Override
                    public String getName() {
                        return FILTER_NAME;
                    }


                    @Override
                    public String getId() {
                        return FILTER_ID;
                    }
                };
                LOG.info(String.format("Adding filter: %s.", f.getName()));
                FilterController.addFilter(f, getQueryId());
            }
        };
        applyButton.addActionListener(al);

        regionsCombo.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                applyButton.setEnabled(true);
            }
        });



        JPanel bottomContainer = new JPanel();
        bottomContainer.setLayout(new BoxLayout(bottomContainer, BoxLayout.X_AXIS));
        bottomContainer.setMaximumSize(new Dimension(1000,30));

        bottomContainer.add(Box.createHorizontalGlue());
        bottomContainer.add(applyButton);


        p.add(regionsCombo, BorderLayout.CENTER);
        p.add(bottomContainer, BorderLayout.SOUTH);

    }

    @Override
    public FilterState saveState() {
        Map<String, String> map = new HashMap<String, String>();
        if (appliedID != null) map.put("value", Integer.toString(appliedID));
        return new FilterState(FilterType.REGION_LIST, FILTER_NAME, FILTER_ID, map);
    }
}
