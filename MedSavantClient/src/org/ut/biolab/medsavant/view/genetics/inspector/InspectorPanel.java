package org.ut.biolab.medsavant.view.genetics.inspector;

import java.util.EnumMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;
import org.ut.biolab.medsavant.view.genetics.inspector.Inspector;
import org.ut.biolab.medsavant.view.genetics.inspector.VariantInspector;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class InspectorPanel extends JTabbedPane {

    private static InspectorPanel instance;

    private enum InspectorEnum { Variant, Gene };
    private EnumMap<InspectorEnum,Integer> inspectorsToTabIndexMap = new EnumMap<InspectorEnum,Integer>(InspectorEnum.class);

    public static InspectorPanel getInstance() {
        if (instance == null) {
            instance = new InspectorPanel();
        }
        return instance;
    }

    private InspectorPanel() {

        this.setTabPlacement(JTabbedPane.TOP);
        this.setBorder(ViewUtil.getBigBorder());
        this.setBackground(ViewUtil.getTertiaryMenuColor());

        addTabPanel(InspectorEnum.Variant,VariantInspector.getInstance());
        addTabPanel(InspectorEnum.Gene,GeneInspector.getInstance());

    }

    public void switchToGeneInspector() {
        switchToInspector(InspectorEnum.Gene);
    }

    public void switchToVariantInspector() {
        switchToInspector(InspectorEnum.Variant);
    }

    private void switchToInspector(InspectorEnum i) {
        this.setSelectedIndex(this.inspectorsToTabIndexMap.get(i));
    }

     private void addTabPanel(InspectorEnum i, Inspector tabPanel) {
        this.inspectorsToTabIndexMap.put(i, this.getTabCount());
        this.addTab(tabPanel.getName(), null, ViewUtil.getClearBorderlessScrollPane(tabPanel.getContent()), tabPanel.getName());
    }

     /*
       private class TabPanel extends JPanel {

        Icon _icon;
        String _title;
        JComponent _component;

        public TabPanel(String title, Icon icon, JComponent component) {
            _title = title;
            _icon = icon;
            _component = component;
        }

        public Icon getIcon() {
            return _icon;
        }

        public void setIcon(Icon icon) {
            _icon = icon;
        }

        public String getTitle() {
            return _title;
        }

        public void setTitle(String title) {
            _title = title;
        }

        public JComponent getComponent() {
            return _component;
        }

        public void setComponent(JComponent component) {
            _component = component;
        }
    }

*/


}
