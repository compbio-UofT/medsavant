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
package org.ut.biolab.medsavant.client.region;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.ut.biolab.medsavant.client.filter.SearchBar;

import org.ut.biolab.medsavant.shared.model.GenomicRegion;
import org.ut.biolab.medsavant.shared.model.RegionSet;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.genetics.QueryUtils;
import org.ut.biolab.medsavant.client.view.list.DetailedTableView;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.mfiume.query.QueryViewController;
import org.ut.biolab.mfiume.query.value.encode.StringConditionEncoder;


/**
 *
 * @author mfiume
 */
public class RegionDetailedView extends DetailedTableView<RegionSet> {

    private final RegionController controller;

    public RegionDetailedView(String page) {
        super(page, "", "Multiple lists (%d)", new String[] { "Region", "Chromosome", "Start", "End" });
        controller = RegionController.getInstance();
    }

    @Override
    public MedSavantWorker createWorker() {
        return new MedSavantWorker<List<GenomicRegion>>(getPageName()) {

            @Override
            protected List<GenomicRegion> doInBackground() throws Exception {
                return controller.getRegionsInSet(selected.get(0));
            }

            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(List<GenomicRegion> result) {
                Object[][] list = new Object[result.size()][];
                for (int i = 0; i < result.size(); i++) {
                    GenomicRegion r = result.get(i);
                    list[i] = new Object[] { r.getName(), r.getChrom(), r.getStart(), r.getEnd() };
                }
                setData(list);
            }
        };
    }

    public JPopupMenu createTablePopup(final Object[][] selected){
        JPopupMenu menu = new JPopupMenu();        
        JMenuItem posItem = new JMenuItem(String.format("<html>Filter by %s</html>", selected.length == 1 ? "Region <i>" + selected[0][0] + "</i>" : "Selected Regions"));

        final List<RegionSet> selectedRegions = this.selected;
        posItem.addActionListener(new ActionListener() {                        
            @Override
            public void actionPerformed(ActionEvent ae) {       
                
                
                List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
                for(Object[] cols : selected){
                    String geneName = (String)cols[0];
                    String chrom = (String)cols[1];
                    Integer start = (Integer)cols[2];
                    Integer end = (Integer)cols[3];
                    regions.add(new GenomicRegion(geneName, chrom, start, end));                    
                }
                
                
                /* OLD
                 *  List<GenomicRegion> regions = new ArrayList<GenomicRegion>();
                TableModel model = tablePanel.getTable().getModel();
                
                List<SearchConditionItem> sciList = new ArrayList<SearchConditionItem>(selRows.length);
                                                            
                for (int r : selRows) {
                    String geneName = (String) model.getValueAt(r, 0);
                    String chrom = (String)model.getValueAt(r,1);
                    Integer start = (Integer) model.getValueAt(r, 2);
                    Integer end = (Integer) model.getValueAt(r, 3);
                                 
                    regions.add(new GenomicRegion(geneName, chrom, start, end));
                }
                 */
                
               QueryUtils.addQueryOnRegions(regions, selectedRegions);
               DialogUtils.displayMessage("Selected genomic regions have been added to query.  Click 'Variants' to review and execute search.");
            }
        });
        
        menu.add(posItem);
        return menu;
    }
    
    @Override
    public JPopupMenu createPopup() {
        
        JPopupMenu popupMenu = new JPopupMenu();
        
        if (ProjectController.getInstance().getCurrentVariantTableSchema() == null) {
            popupMenu.add(new JLabel("(You must choose a variant table before filtering)"));
        } else {

          //Filter by patient
            
            JMenuItem filter1Item = new JMenuItem(String.format("<html>Filter by %s</html>", selected.size() == 1 ? "Region List <i>" + selected.get(0) + "</i>" : "Selected Region Lists"));
            filter1Item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //GeneticsFilterPage.getSearchBar().loadFilters(RegionSetFilterView.wrapState(selected));
                     QueryViewController qvc = SearchBar.getInstance().getQueryViewController();
                         
                     List<String> regionSetNames = new ArrayList<String>(selected.size());
                     for(RegionSet rs : selected){
                         regionSetNames.add(rs.getName());
                     }
                     
                    String encodedConditions = StringConditionEncoder.encodeConditions(regionSetNames);
                    String description = StringConditionEncoder.getDescription(regionSetNames);
                                 
                    qvc.replaceFirstLevelItem("Region Set", encodedConditions, description);                                                                               
                    DialogUtils.displayMessage("Selected region sets have been added to query.  Click 'Variants' to review and execute search.");

                }
            });
            popupMenu.add(filter1Item);
        }

        return popupMenu;
    }
}
