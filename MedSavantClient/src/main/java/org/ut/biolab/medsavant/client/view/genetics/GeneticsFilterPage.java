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
package org.ut.biolab.medsavant.client.view.genetics;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.reference.ReferenceController;
import org.ut.biolab.medsavant.client.reference.ReferenceEvent;
import org.ut.biolab.medsavant.client.filter.SearchBar;
import org.ut.biolab.medsavant.client.view.subview.MultiSection;
import org.ut.biolab.medsavant.client.view.subview.SubSection;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GeneticsFilterPage extends SubSection {

    private static final Log LOG = LogFactory.getLog(GeneticsFilterPage.class);

    private JPanel view;
    private static SearchBar panel;

    public GeneticsFilterPage(MultiSection parent) {
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
