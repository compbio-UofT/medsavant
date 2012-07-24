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
package org.ut.biolab.medsavant.view.genetics.inspector;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.TablePanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.BasicVariantSubInspector;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class VariantInspector extends CollapsibleInspector implements VariantSelectionChangedListener {

    private static VariantInspector instance;
    private static List<VariantSelectionChangedListener> listeners = new ArrayList<VariantSelectionChangedListener>();
    private static VariantRecord record;

    public static VariantInspector getInstance() {
        if (instance == null) {
            instance = new VariantInspector();
        }
        return instance;
    }

    private VariantInspector() {

        JPanel messagePanel = new JPanel();
        //messagePanel.setBackground(Color.white);
        messagePanel.setBorder(ViewUtil.getHugeBorder());
        ViewUtil.applyVerticalBoxLayout(messagePanel);

        JLabel h1 = new JLabel("No Variant Selected");
        h1.setFont(ViewUtil.getMediumTitleFont());

        String m = "<html><div style=\"text-align: center;\">Choose one from the list on the left</div></html>";
        JLabel h2 = new JLabel(m);
        h2.setPreferredSize(new Dimension(190,300));
        h2.setMinimumSize(new Dimension(190,300));
        h2.setBackground(Color.red);

        messagePanel.add(ViewUtil.center(h1));
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(ViewUtil.center(h2));

        this.setMessage(messagePanel);

        TablePanel.addVariantSelectionChangedListener(this);
        this.addSubInspector(new BasicVariantSubInspector());
        this.addSubInspector(new SocialVariantSubInspector(),true);
        //this.addSubInfoPanel(new BasicGeneInfoSubPanel());
    }

    @Override
    public String getName() {
        return "Variant Inspector";
    }

    public static void addVariantSelectionChangedListener(VariantSelectionChangedListener l) {
        listeners.add(l);
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        if (r == null) {
            this.switchToMessage();
        } else {
            this.switchToPanes();
        }
        InspectorPanel.getInstance().switchToVariantInspector();
        for (VariantSelectionChangedListener l : listeners) {
            l.variantSelectionChanged(r);
        }
        record = r;
    }
}
