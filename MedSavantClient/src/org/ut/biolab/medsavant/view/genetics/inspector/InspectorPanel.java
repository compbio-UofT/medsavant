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

import java.util.EnumMap;
import javax.swing.JTabbedPane;

import org.ut.biolab.medsavant.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class InspectorPanel extends JTabbedPane {

    /** Full width of the inspector panel (borders included). */
    public static final int INSPECTOR_WIDTH = 380;
    
    /** Width of the inspector panel without borders.  The 80 is determined empirically. */
    public static final int INSPECTOR_INNER_WIDTH = INSPECTOR_WIDTH - 80;

    private enum InspectorEnum { VARIANT, GENE };

    private static InspectorPanel instance;

    private EnumMap<InspectorEnum, Integer> inspectorsToTabIndexMap = new EnumMap<InspectorEnum,Integer>(InspectorEnum.class);

    public static InspectorPanel getInstance() {
        if (instance == null) {
            instance = new InspectorPanel();
        }
        return instance;
    }

    private InspectorPanel() {

        setTabPlacement(JTabbedPane.TOP);
        setBorder(ViewUtil.getBigBorder());
        setBackground(ViewUtil.getTertiaryMenuColor());

        addTabPanel(InspectorEnum.VARIANT,VariantInspector.getInstance());
        addTabPanel(InspectorEnum.GENE,GeneInspector.getInstance());

    }

    public void switchToGeneInspector() {
        switchToInspector(InspectorEnum.GENE);
    }

    public void switchToVariantInspector() {
        switchToInspector(InspectorEnum.VARIANT);
    }

    private void switchToInspector(InspectorEnum i) {
        this.setSelectedIndex(this.inspectorsToTabIndexMap.get(i));
    }

    private void addTabPanel(InspectorEnum i, Inspector tabPanel) {
        inspectorsToTabIndexMap.put(i, this.getTabCount());
        addTab(tabPanel.getName(), null, ViewUtil.getClearBorderlessScrollPane(tabPanel.getContent()), tabPanel.getName());
    }
}
