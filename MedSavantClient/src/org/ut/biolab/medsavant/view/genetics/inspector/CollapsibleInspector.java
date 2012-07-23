/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.view.genetics.inspector;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;

import org.ut.biolab.medsavant.view.genetics.variantinfo.SubInspector;


/**
 *
 * @author mfiume
 */
public abstract class CollapsibleInspector extends JPanel implements Inspector {
    private final CollapsiblePanes container;

    public CollapsibleInspector() {
        container = new CollapsiblePanes();
        container.addExpansion();
        this.setLayout(new BorderLayout());
        this.add(container,BorderLayout.NORTH);
    }

    @Override
    public abstract String getName();

    @Override
    public JPanel getContent() {
        return container;
    }

    protected void addSubInfoPanel(SubInspector ipan) {

        // remove the previous expansion
        container.remove(container.getComponentCount()-1);

        CollapsiblePane p = new CollapsiblePane(ipan.getName());
        p.setStyle(CollapsiblePane.PLAIN_STYLE);
        p.setLayout(new BorderLayout());
        p.add(ipan.getInfoPanel(),BorderLayout.CENTER);
        container.add(p);
        container.addExpansion();

    }

}
