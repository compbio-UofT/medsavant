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
package org.ut.biolab.medsavant.client.view.component;

import com.jidesoft.list.FilterableCheckBoxList;
import com.jidesoft.list.QuickListFilterField;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.component.SelectableListView.SelectionEvent;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class GenericStringChooser extends JDialog implements Listener<SelectionEvent> {

    private QuickListFilterField field;
    protected FilterableCheckBoxList filterableList;
    private final List<String> options;
    private final ArrayList<Listener<SelectionEvent>> listeners;

    public GenericStringChooser(List<String> options, String title) {
        super();
        this.setTitle(title);
        this.options = options;
        initUI();
        this.setLocationRelativeTo(null);
        listeners = new ArrayList<Listener<SelectionEvent>>();
    }

    private void initUI() {

        Container contentPane = this.getContentPane();
        contentPane.setPreferredSize(new Dimension(350,400));
        contentPane.setLayout(new BorderLayout());

        JPanel innerPanel = new JPanel();
        innerPanel.setBorder(ViewUtil.getMediumBorder());
        innerPanel.setLayout(new BorderLayout());
        contentPane.add(innerPanel, BorderLayout.CENTER);

        SelectableListView<String> strContainer = new SelectableListView<String>();
        strContainer.setAvailableValues(options);
        strContainer.initContentPanel();
        strContainer.addListener(this);

        innerPanel.add(strContainer, BorderLayout.CENTER);

        this.pack();
    }

    public void addListener(Listener<SelectionEvent> l) {
        listeners.add(l);
    }

    @Override
    public void handleEvent(SelectionEvent event) {
        System.out.println("GSC caught change, dispatching to " + listeners.size() + " listeners");
        this.setVisible(false);
        for (Listener l : listeners) {
            l.handleEvent(event);
        }
    }
}
