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
package org.ut.biolab.medsavant.client.view.list;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.jidesoft.grid.TableModelWrapperUtils;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;

/**
 *
 * @author mfiume
 */
public class SplitScreenView extends JPanel {

    private final DetailedView detailedView;
    private final ListView listView;

    public SplitScreenView(DetailedListModel model, DetailedView view) {
        this(model, view, new DetailedListEditor());
    }

    public SplitScreenView(DetailedListModel model, DetailedView view, DetailedListEditor editor) {
        detailedView = view;

        setLayout(new BorderLayout());

        listView = new ListView(view.getPageName(), model, view, editor);
        detailedView.setSplitScreenParent(this);
        
        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    listView, detailedView);
            p.setBorder(BorderFactory.createEmptyBorder());
            p.setDividerSize(0);
            p.setDividerLocation(230);
        add(p,BorderLayout.CENTER);
    }

    public void refresh() {
        listView.refreshList();
    }

    public Object[][] getList() {
        return listView.data;
    }

    public void selectInterval(int start, int end){
        start = TableModelWrapperUtils.getRowAt(listView.stp.getTable().getModel(), start);
        end = TableModelWrapperUtils.getRowAt(listView.stp.getTable().getModel(), end);
        listView.stp.getTable().getSelectionModel().setSelectionInterval(start, end);
        listView.stp.scrollToIndex(start);
    }
}
