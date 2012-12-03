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
package org.ut.biolab.medsavant.client.view.list;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.jidesoft.grid.TableModelWrapperUtils;

import org.ut.biolab.medsavant.client.view.util.PeekingPanel;


/**
 *
 * @author mfiume
 */
public class SplitScreenView extends JPanel {

    private final DetailedView detailedView;
    private final MasterView masterView;

    public SplitScreenView(DetailedListModel model, DetailedView view) {
        this(model, view, new DetailedListEditor());
    }

    public SplitScreenView(DetailedListModel model, DetailedView view, DetailedListEditor editor) {
        detailedView = view;

        setLayout(new BorderLayout());

        masterView = new MasterView(view.getPageName(), model, view, editor);

        PeekingPanel pp = new PeekingPanel("List", BorderLayout.EAST, masterView, true, 330);
        pp.setToggleBarVisible(false);
        add(pp, BorderLayout.WEST);
        add(detailedView, BorderLayout.CENTER);
        detailedView.setSplitScreenParent(this);
    }

    public void refresh() {
        masterView.refreshList();
    }

    public Object[][] getList() {
        return masterView.data;
    }

    public void selectInterval(int start, int end){
        start = TableModelWrapperUtils.getRowAt(masterView.stp.getTable().getModel(), start);
        end = TableModelWrapperUtils.getRowAt(masterView.stp.getTable().getModel(), end);
        masterView.stp.getTable().getSelectionModel().setSelectionInterval(start, end);
        masterView.stp.scrollToIndex(start);
    }
}
