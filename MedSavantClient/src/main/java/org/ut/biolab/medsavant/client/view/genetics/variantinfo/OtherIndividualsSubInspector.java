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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.view.genetics.TablePanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;


/**
 * Displays a small panel showing other individuals with the selected variant.
 * Under construction.
 */
public class OtherIndividualsSubInspector extends SubInspector implements Listener<VariantRecord> {

    private JPanel infoPanel;
    private JLabel msg = new JLabel();    
    private static final Log LOG = LogFactory.getLog(OtherIndividualsSubInspector.class);

    public OtherIndividualsSubInspector(){
        TablePanel.addVariantSelectionChangedListener(this);
    }
    
    @Override
    public String getName() {
        return "Other Individuals with Variant";        
    }

    @Override
    public JPanel getInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(msg);
        return infoPanel;
        
    }

    @Override
    public void handleEvent(VariantRecord variantRecord) {
        LOG.debug("variantRecord position is "+variantRecord.getPosition());
        //msg.setText("variantRecord position is "+variantRecord.getPosition());        
    }    
}
