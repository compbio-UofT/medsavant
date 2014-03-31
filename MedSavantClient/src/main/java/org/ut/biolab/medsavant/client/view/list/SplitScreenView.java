/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.list;

import com.explodingpixels.macwidgets.SourceListItem;
import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.jidesoft.grid.TableModelWrapperUtils;
import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.util.list.DefaultNiceListColorScheme;
import org.ut.biolab.medsavant.client.view.util.list.NiceListColorScheme;

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

        this.setBackground(Color.white);
        setLayout(new BorderLayout());

        listView = new ListView(view.getPageName(), model, view, editor);
        detailedView.setSplitScreenParent(this);

        JPanel listViewContainer = ViewUtil.getClearPanel();
        listViewContainer.setLayout(new BorderLayout());
        listViewContainer.setBorder(ViewUtil.getRightLineBorder());
        listViewContainer.add(listView, BorderLayout.CENTER);

        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listViewContainer, detailedView);
        p.setBorder(BorderFactory.createEmptyBorder());
        p.setDividerSize(0);
        p.setDividerLocation(230);
        add(p, BorderLayout.CENTER);
        
        listView.getControlBar().installDraggableWidgetOnSplitPane(p);
    }

    public void refresh() {
        listView.refreshList();
    }

    public Object[][] getList() {
        return listView.data;
    }
    
     public void setSearchBarEnabled(boolean b) {
        listView.setSearchBarEnabled(b);
    }

    public void selectItemWithKey(String key) {
        listView.selectItemWithKey(key);
    }

    public void selectItemAtIndex(int i) {
        listView.selectItemAtIndex(i);
    }

    public void setListColorScheme(NiceListColorScheme cs) {
        listView.setColorScheme(cs);
    }
}
