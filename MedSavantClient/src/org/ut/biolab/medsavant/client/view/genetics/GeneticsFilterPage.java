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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.subview.SectionView;
import org.ut.biolab.medsavant.client.view.subview.SubSectionView;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSectionView {

    private static final Log LOG = LogFactory.getLog(GeneticsFilterPage.class);

    private JPanel view;
    private static SearchBar panel;

    public GeneticsFilterPage(SectionView parent) {
        super(parent, "Search Bar");

        panel = SearchBar.getInstance();
        ReferenceController.getInstance().addListener(new Listener<ReferenceEvent>() {
            @Override
            public void handleEvent(ReferenceEvent event) {
                if (event.getType() == ReferenceEvent.Type.CHANGED && panel != null) {
                    panel.clearAll();
                    panel.refreshSubPanels();
                }
            }
        });
    }

    @Override
    public JPanel getView() {
        if (view == null) {
            view = ViewUtil.getClearPanel();
            view.setName(pageName);
            view.setLayout(new BorderLayout());
            view.add(panel,BorderLayout.CENTER);

            //if (history != null) FilterController.removeFilterListener(history);
            //history = new FilterProgressPanel();
            //view.add(new PeekingPanel("History", BorderLayout.EAST, history, true), BorderLayout.WEST);

            // uncomment the next line to show the master SQL statement
            //view.add(new PeekingPanel("SQL", BorderLayout.SOUTH, new FilterSQLPanel(), true), BorderLayout.NORTH);
        } else {
            panel.refreshSubPanels();
        }

        return view;
    }

    public static SearchBar getSearchBar() {
        return panel;
    }


}
