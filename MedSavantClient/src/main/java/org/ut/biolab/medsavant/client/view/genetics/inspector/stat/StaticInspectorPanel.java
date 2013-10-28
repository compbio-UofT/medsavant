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
package org.ut.biolab.medsavant.client.view.genetics.inspector.stat;

import java.util.EnumMap;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.client.view.genetics.inspector.Inspector;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;


/**
 *
 * @author mfiume
 */
public class StaticInspectorPanel extends JTabbedPane {

    /** Full width of the inspector panel (borders included). */
    public static final int INSPECTOR_WIDTH = 380;

    /** Width of the inspector panel without borders.  The 80 is determined empirically. */
    public static final int INSPECTOR_INNER_WIDTH = INSPECTOR_WIDTH - 80;

    private enum InspectorEnum { VARIANT, GENE };

    private static StaticInspectorPanel instance;

    private EnumMap<InspectorEnum, Integer> inspectorsToTabIndexMap = new EnumMap<InspectorEnum,Integer>(InspectorEnum.class);

    public static StaticInspectorPanel getInstance() {
        if (instance == null) {
            instance = new StaticInspectorPanel();
        }
        return instance;
    }

    private StaticInspectorPanel() {

        setTabPlacement(JTabbedPane.TOP);
        setBorder(ViewUtil.getBigBorder());
        setBackground(ViewUtil.getTertiaryMenuColor());

        addTabPanel(InspectorEnum.VARIANT,StaticVariantInspector.getInstance());
        addTabPanel(InspectorEnum.GENE,StaticGeneInspector.getInstance());

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
