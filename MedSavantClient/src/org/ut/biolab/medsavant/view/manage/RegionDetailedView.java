/*
 *    Copyright 2011-2012 University of Toronto
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
package org.ut.biolab.medsavant.view.manage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import com.healthmarketscience.sqlbuilder.ComboCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.controller.ProjectController;
import org.ut.biolab.medsavant.db.MedSavantDatabase.DefaultVariantTableSchema;
import org.ut.biolab.medsavant.model.GenomicRegion;
import org.ut.biolab.medsavant.model.Range;
import org.ut.biolab.medsavant.model.RangeCondition;
import org.ut.biolab.medsavant.model.RegionSet;
import org.ut.biolab.medsavant.util.BinaryConditionMS;
import org.ut.biolab.medsavant.view.genetics.filter.FilterPanelSubItem;
import org.ut.biolab.medsavant.view.genetics.filter.FilterUtils;
import org.ut.biolab.medsavant.view.list.DetailedTableView;


/**
 *
 * @author mfiume
 */
public class RegionDetailedView extends DetailedTableView {
    private static final Log LOG = LogFactory.getLog(RegionDetailedView.class);

    private static final int LIMIT = 500;

    private int numRegionsInRegionList;
    private RegionSet selectedRegion;
    private static List<FilterPanelSubItem> filterPanels;
    
    
    public RegionDetailedView() {
        super("Regions in List", "Multiple lists (%d)", new String[] { "Region", "Chromosome", "Start", "End" });
    }

    @Override
    public void setRightClick(MouseEvent e) {
        JPopupMenu popup = createPopup(selectedRegion);
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public SwingWorker createWorker() {
        return new SwingWorker<List<GenomicRegion>, Void>() {

            @Override
            protected List<GenomicRegion> doInBackground() throws Exception {
                //numRegionsInRegionList = QueryUtil.getNumRegionsInRegionSet(regionName);
                //List<Vector> regionList = QueryUtil.getRegionNamesInRegionSet(regionName,limit);
                return MedSavantClient.RegionQueryUtilAdapter.getRegionsInRegionSet(LoginController.sessionId, selectedRegion.getId());
            }
            
            @Override
            protected void done() {
                //List<Object[]> list = new ArrayList<Object[]>();
                Object[][] list = null;
                try {
                    List<GenomicRegion> result = get();
                    list = new Object[result.size()][];
                    for (int i = 0; i < result.size(); i++) {
                        GenomicRegion r = result.get(i);
                        list[i] = new Object[] { r.getName(), r.getChrom(), r.getStart(), r.getEnd() };
                    }
                } catch (InterruptedException ignored) {
                } catch (ExecutionException x) {
                    LOG.error("Failed to retrieve reference genomes.", x);
                }
                setData(list);
            }
        };
    }
/*        @Override
        protected void showSuccess(List<String> result) {
            try {
                //setTitle(regionSet.getName() + " (" + numRegionsInRegionList + " regions)");
                listPane.setDescription(ViewUtil.numToString(numRegionsInRegionList));
                setRegionList(get());

            } catch (Exception ex) {
                return;
            }
        }*/

    @Override
    public void setSelectedItem(Object[] item) {
        selectedRegion = (RegionSet)item[0];
        super.setSelectedItem(item);
    }

    private JPopupMenu createPopup(final RegionSet set) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem("Filter by Region List");
            filter1Item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    try {
                        List<GenomicRegion> regions = MedSavantClient.RegionQueryUtilAdapter.getRegionsInRegionSet(LoginController.sessionId, set.getId());
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

                        removeExistingFilters();
                        filterPanels = FilterUtils.createAndApplyGenericFixedFilter(
                                "Region Lists - Filter by List",
                                regions.size() + " Region(s)",
                                ComboCondition.or(results));

                    } catch (Exception x) {
                        LOG.error("Error filtering region lists.", x);
                    }

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }

    private void removeExistingFilters() {
        if (filterPanels != null) {
            for (FilterPanelSubItem panel : filterPanels) {
                panel.removeThis();
            }
        }
    }
}
