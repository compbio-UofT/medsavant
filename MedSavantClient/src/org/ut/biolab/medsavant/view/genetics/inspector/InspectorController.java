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

import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticGeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.stat.StaticVariantInspector;
import java.util.EnumMap;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.view.genetics.variantinfo.DetailedVariantSubInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GeneSubInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.SimpleVariantSubInspector;

import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class InspectorController extends JTabbedPane {

    /**
     * Full width of the inspector panel (borders included).
     */
    public static final int INSPECTOR_WIDTH = 380;
    /**
     * Width of the inspector panel without borders. The 80 is determined
     * empirically.
     */
    public static final int INSPECTOR_INNER_WIDTH = INSPECTOR_WIDTH - 80;
    private GeneSubInspector geneSubInspector;
    private DetailedVariantSubInspector detailedVariantSubInspector;
    private SimpleVariantSubInspector simpleVariantInspector;

    private void createSubInspectors() {
        simpleVariantInspector = new SimpleVariantSubInspector(this);

        detailedVariantSubInspector = new DetailedVariantSubInspector(this);

        geneSubInspector = new GeneSubInspector(this);

        CollapsibleInspector vInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Variant";
            }
        };
        vInspector.addSubInspector(simpleVariantInspector);
        vInspector.addSubInspector(detailedVariantSubInspector);

        CollapsibleInspector gInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Gene";
            }
        };
        gInspector.addSubInspector(geneSubInspector);

        addTabPanel(InspectorController.InspectorEnum.VARIANT, vInspector);
        addTabPanel(InspectorController.InspectorEnum.GENE, gInspector);
    }

    public GeneSubInspector getGeneSubInspector() {
        return geneSubInspector;
    }

    public DetailedVariantSubInspector getDetailedVariantSubInspector() {
        return detailedVariantSubInspector;
    }

    public SimpleVariantSubInspector getSimpleVariantInspector() {
        return simpleVariantInspector;
    }

    private enum InspectorEnum {

        VARIANT, GENE
    };
    private static InspectorController instance;
    private EnumMap<InspectorController.InspectorEnum, Integer> inspectorsToTabIndexMap = new EnumMap<InspectorController.InspectorEnum, Integer>(InspectorController.InspectorEnum.class);

    public InspectorController() {

        setTabPlacement(JTabbedPane.TOP);
        setBorder(ViewUtil.getBigBorder());
        setBackground(ViewUtil.getTertiaryMenuColor());

        createSubInspectors();
    }

    public void switchToGeneInspector() {
        switchToInspector(InspectorController.InspectorEnum.GENE);
    }

    public void switchToVariantInspector() {
        switchToInspector(InspectorController.InspectorEnum.VARIANT);
    }

    private void switchToInspector(InspectorController.InspectorEnum i) {
        this.setSelectedIndex(this.inspectorsToTabIndexMap.get(i));
    }

    private void addTabPanel(InspectorController.InspectorEnum i, Inspector inspector) {
        inspectorsToTabIndexMap.put(i, this.getTabCount());
        addTab(inspector.getName(), null, ViewUtil.getClearBorderlessScrollPane(inspector.getContent()), inspector.getName());
    }
}
