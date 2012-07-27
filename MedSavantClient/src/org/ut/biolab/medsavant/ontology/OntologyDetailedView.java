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
package org.ut.biolab.medsavant.ontology;

import java.awt.event.MouseEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.model.*;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.list.DetailedTableView;


/**
 *
 * @author mfiume
 */
public class OntologyDetailedView extends DetailedTableView {
    private static final Log LOG = LogFactory.getLog(OntologyDetailedView.class);

    private Ontology selectedItem;

    public OntologyDetailedView() {
        super("", "Multiple ontologies (%d)", new String[] { "Name", "Type", "OBO URL", "Mapping URL" });
    }

    @Override
    public void setRightClick(MouseEvent e) {
/* TODO: right-click on ontology list
 * JPopupMenu popup = createPopup(selectedItem);
        popup.show(e.getComponent(), e.getX(), e.getY());*/
    }

    @Override
    public MedSavantWorker<Ontology[]> createWorker() {
        return new MedSavantWorker<Ontology[]>("Ontologies") {

            @Override
            protected Ontology[] doInBackground() throws Exception {
                return new Ontology[] { selectedItem };
            }

            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Ontology[] result) {
                Object[][] list = new Object[result.length][];
                for (int i = 0; i < result.length; i++) {
                    Ontology ont = result[i];
                    list[i] = new Object[] { ont.getName(), ont.getType(), ont.getOBOURL(), ont.getMappingURL() };
                }
                setData(list);
            }
        };
    }

    @Override
    public void setSelectedItem(Object[] item) {
        selectedItem = (Ontology)item[0];
        super.setSelectedItem(item);
    }

/*    private JPopupMenu createPopup(final Ontology ont) {
        JPopupMenu popupMenu = new JPopupMenu();

        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

            //Filter by patient
            JMenuItem filter1Item = new JMenuItem("Filter by Ontology");
            filter1Item.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    try {
                        GenomicRegion[] regions = MedSavantClient.RegionSetManager.getRegionsInSet(LoginController.sessionId, ont, Integer.MAX_VALUE);
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
                                regions.length + " Region(s)",
                                ComboCondition.or(results));

                    } catch (Exception x) {
                        LOG.error("Error filtering region lists.", x);
                    }

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }*/
}
