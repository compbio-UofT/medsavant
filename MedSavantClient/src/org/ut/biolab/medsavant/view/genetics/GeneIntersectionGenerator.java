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

package org.ut.biolab.medsavant.view.genetics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComboBox;

import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.inspector.GeneInspector;


/**
 *
 * @author mfiume
 */
public class GeneIntersectionGenerator implements VariantSelectionChangedListener {

    private static GeneIntersectionGenerator instance;

    private Collection<Gene> genes;

    private List<JComboBox> boxes = new ArrayList<JComboBox>();


    public static GeneIntersectionGenerator getInstance() {
        if (instance == null) {
            instance = new GeneIntersectionGenerator();
            TablePanel.addVariantSelectionChangedListener(instance);
        }
        return instance;
    }

    private GeneIntersectionGenerator() {

    }

    public JComboBox getGeneDropDown() {
        JComboBox b = new JComboBox();
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeneInspector.getInstance().setGene((Gene)((JComboBox)e.getSource()).getSelectedItem());
            }
        });
        boxes.add(b);
        return b;
    }

    @Override
    public void variantSelectionChanged(VariantRecord r) {
        try {

            if (genes == null) {
                genes = GeneSetController.getInstance().getCurrentGenes();
            }

            Gene g0 = null;
            for (JComboBox b: boxes) {
                b.removeAllItems();

                for (Gene g: genes) {
                    if (g0 == null) {
                        g0 = g;
                    }
                    if (g.getChrom().equals(r.getChrom()) && r.getPosition() > g.getStart() && r.getPosition() < g.getEnd()) {
                        b.addItem(g);
                    }
                }
            }
            if (g0 != null) {
                GeneInspector.getInstance().setGene(g0);
            }
        } catch (Exception ex) {
            ClientMiscUtils.reportError("Error fetching genes: %s", ex);
        }
    }
}
