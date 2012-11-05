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

import com.jidesoft.pane.CollapsiblePane;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.inspector.InspectorController;


/**
 *
 * @author mfiume
 */
public abstract class SubInspector {

    protected CollapsiblePane parent;
    private InspectorController controller;

    public abstract String getName();
    public abstract JPanel getInfoPanel();

    public SubInspector() {
    }

    public SubInspector(InspectorController c) {
        this.controller = c;
    }

    public void setPaneParent(CollapsiblePane p) {
        this.parent = p;
    }

    public InspectorController getInspectorController() {
        return controller;
    }


}
