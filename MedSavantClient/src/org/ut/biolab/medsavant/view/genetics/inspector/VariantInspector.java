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

import java.util.ArrayList;
import java.util.List;

import org.ut.biolab.medsavant.model.event.VariantSelectionChangedListener;
import org.ut.biolab.medsavant.vcf.VariantRecord;
import org.ut.biolab.medsavant.view.genetics.TablePanel;
import org.ut.biolab.medsavant.view.genetics.variantinfo.BasicVariantSubInspector;

/**
 *
 * @author mfiume
 */
public class VariantInspector extends CollapsibleInspector implements VariantSelectionChangedListener {

    private static VariantInspector instance;
    private static List<VariantSelectionChangedListener> listeners = new ArrayList<VariantSelectionChangedListener>();
    private static VariantRecord record;

    private boolean isShown = true;

    public static VariantInspector getInstance() {
        if (instance == null) {
            instance = new VariantInspector();
        }
        return instance;
    }

    private VariantInspector() {
        TablePanel.addVariantSelectionChangedListener(this);
        this.addSubInfoPanel(new BasicVariantSubInspector());
        this.addSubInfoPanel(new SocialVariantSubInspector());
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
        if (isShown) {
            for (VariantSelectionChangedListener l : listeners) {
                l.variantSelectionChanged(r);
            }
        }
        record = r;
    }
}
