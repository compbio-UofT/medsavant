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
package org.ut.biolab.medsavant.client.view.genetics.inspector.stat;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;

import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.view.genetics.inspector.CollapsibleInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.BasicGeneSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GeneManiaSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.OntologySubInspector;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class StaticGeneInspector extends CollapsibleInspector {

    private static StaticGeneInspector instance;
    private static List<Listener<Gene>> listeners = new ArrayList<Listener<Gene>>();
    boolean isShown = true;
    private static final Log LOG = LogFactory.getLog(StaticGeneInspector.class);

    public static StaticGeneInspector getInstance() {
        if (instance == null) {
            instance = new StaticGeneInspector();
        }
        return instance;
    }
    private Gene selectedGene;

    private StaticGeneInspector() {

        JPanel messagePanel = new JPanel();
        //messagePanel.setBackground(Color.white);
        messagePanel.setBorder(ViewUtil.getHugeBorder());
        ViewUtil.applyVerticalBoxLayout(messagePanel);

        JLabel h1 = new JLabel("No Gene Selected");
        h1.setFont(ViewUtil.getMediumTitleFont());

        String m = "<html><div style=\"text-align: center;\">Choose one from the dropdown box in the Variant Inspector and then click the Inspect button</div></html>";
        JLabel h2 = new JLabel(m);
        h2.setPreferredSize(new Dimension(190, 300));
        h2.setMinimumSize(new Dimension(190, 300));
        h2.setBackground(Color.red);

        messagePanel.add(ViewUtil.centerHorizontally(h1));
        messagePanel.add(Box.createVerticalStrut(10));
        messagePanel.add(ViewUtil.centerHorizontally(h2));

        this.setMessage(messagePanel);


        addSubInspector(new BasicGeneSubInspector());
        addSubInspector(new OntologySubInspector());
        addSubInspector(new GeneManiaSubInspector());

    }

    @Override
    public String getName() {
        return "Gene Inspector";
    }

    public void setGene(Gene g) {
        if (g == null) {
            this.switchToMessage();
        } else {
            this.switchToPanes();
        }
        LOG.debug("Setting gene to " + g.getName());
        selectedGene = g;
        geneSelectionChanged(g);
    }

    public static void addGeneSelectionChangedListener(Listener<Gene> l) {
        listeners.add(l);
    }

    public void geneSelectionChanged(Gene r) {
        if (isShown) {
            for (Listener<Gene> l : listeners) {
                l.handleEvent(r);
            }
        }
        selectedGene = r;
    }

    @Override
    protected final void addSubInspector(SubInspector panel) {
        super.addSubInspector(panel);
        listeners.add((Listener<Gene>) panel);
    }
}