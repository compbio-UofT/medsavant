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

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.event.GeneSelectionChangedListener;
import org.ut.biolab.medsavant.view.genetics.variantinfo.BasicGeneSubInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.GeneManiaInfoSubPanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.OntologySubInspector;
import org.ut.biolab.medsavant.view.genetics.variantinfo.SubInspector;


/**
 *
 * @author mfiume
 */
public class GeneInspector extends CollapsibleInspector {

    private static GeneInspector instance;

    private static List<GeneSelectionChangedListener> listeners = new ArrayList<GeneSelectionChangedListener>();

    boolean isShown = true;

    public static GeneInspector getInstance() {
        if (instance == null) {
            instance = new GeneInspector();
        }
        return instance;
    }
    private Gene selectedGene;

    private GeneInspector() {
        addSubInfoPanel(new BasicGeneSubInspector());
        //addSubInfoPanel(new GeneManiaInfoSubPanel());
        //addSubInfoPanel(new OntologySubInspector());
    }

    @Override
    public String getName() {
        return "Gene Inspector";
    }

    public void setGene(Gene g) {
        selectedGene = g;
        geneSelectionChanged(g);
    }

    public static void addGeneSelectionChangedListener(GeneSelectionChangedListener l) {
        listeners.add(l);
    }

    public void geneSelectionChanged(Gene r) {
        if (isShown) {
            for (GeneSelectionChangedListener l : listeners) {
                l.geneSelectionChanged(r);
            }
        }
        selectedGene = r;
    }

    @Override
    protected final void addSubInfoPanel(SubInspector panel) {
        super.addSubInfoPanel(panel);
        listeners.add((GeneSelectionChangedListener)panel);
    }
}