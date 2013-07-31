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
package org.ut.biolab.medsavant.client.view.genetics.inspector;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.SplitScreenPanel;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.DetailedVariantSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GeneManiaSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.GeneSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.OntologySubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariantSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SocialVariantSubInspector;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.OtherIndividualsGeneSubInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.OtherIndividualsVariantSubInspector;

/**
 *
 * @author mfiume
 */
public class ComprehensiveInspector extends JTabbedPane implements Listener<Object> {

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
    private CollapsibleInspector variantCollapsibleInspector;
    private CollapsibleInspector geneCollapsibleInspector;
    private OntologySubInspector ontologySubInspector;
    private GeneManiaSubInspector geneManiaInspector;
    private SocialVariantSubInspector socialSubInspector;
    private OtherIndividualsVariantSubInspector otherIndividualsVariantSubInspector;
    private OtherIndividualsGeneSubInspector otherIndividualsDetailedSubInspector;
    private List<Listener<Object>> selectionListeners = new LinkedList<Listener<Object>>();
    
    
    public void addSelectionListener(Listener<Object> selectionListener){
        selectionListeners.add(selectionListener);
    }
    
    public DetailedVariantSubInspector getDetailedVariantSubInspector() {
        return detailedVariantSubInspector;
    }

    public SocialVariantSubInspector getSocialSubInspector() {
        return socialSubInspector;
    }

    private void createSubInspectors(
            boolean createSimpleVariantInspector,
            boolean createDetailedVariantInspector,
            boolean createSocialVariantInspector,
            boolean createGeneSubInspector,
            boolean createOntologySubInspector,
            boolean createGeneManiaInspector,
            boolean createOtherIndividualsInspector,
            SplitScreenPanel splitScreenPanel) {


        // Assemble the variant inspector
        variantCollapsibleInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Variant";
            }
        };

        variantCollapsibleInspector.setMessage("No variant selected");
        variantCollapsibleInspector.switchToMessage();


        // Assemble the gene inspector
        geneCollapsibleInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Gene";
            }
        };

        geneCollapsibleInspector.setMessage("No gene selected");


        // Variant
        if (createSimpleVariantInspector) {
            simpleVariantInspector = new SimpleVariantSubInspector();
            variantCollapsibleInspector.addSubInspector(simpleVariantInspector);
            
        }

        if (createDetailedVariantInspector) {
            detailedVariantSubInspector = new DetailedVariantSubInspector();
            variantCollapsibleInspector.addSubInspector(detailedVariantSubInspector);
        }

        if (createSocialVariantInspector) {
            socialSubInspector = new SocialVariantSubInspector();
            variantCollapsibleInspector.addSubInspector(socialSubInspector);
        }

        if (createOtherIndividualsInspector) {
            otherIndividualsVariantSubInspector = new OtherIndividualsVariantSubInspector(splitScreenPanel);
            otherIndividualsVariantSubInspector.setVariantSelectionListener(this);            
            variantCollapsibleInspector.addSubInspector(otherIndividualsVariantSubInspector);           
        }
        // Gene
        if (createGeneSubInspector) {
            geneSubInspector = new GeneSubInspector();
            geneCollapsibleInspector.addSubInspector(geneSubInspector);
        }
        
        if(createOtherIndividualsInspector){ //TODO: Change to createOtherIndividualsDetailedSubInspector
            otherIndividualsDetailedSubInspector = new OtherIndividualsGeneSubInspector(splitScreenPanel);
            otherIndividualsDetailedSubInspector.setVariantSelectionListener(this);
            geneCollapsibleInspector.addSubInspector(otherIndividualsDetailedSubInspector);
        }

        if (createOntologySubInspector) {
            ontologySubInspector = new OntologySubInspector();
            geneCollapsibleInspector.addSubInspector(ontologySubInspector);
        }

        if (createGeneManiaInspector) {
            geneManiaInspector = new GeneManiaSubInspector();
            geneCollapsibleInspector.addSubInspector(geneManiaInspector);
            geneManiaInspector.setGeneListener(this);
        }

        // Assemble everything
        addTabPanel(ComprehensiveInspector.InspectorEnum.VARIANT, variantCollapsibleInspector);
        addTabPanel(ComprehensiveInspector.InspectorEnum.GENE, geneCollapsibleInspector);

    }

    public CollapsibleInspector getVariantInspector() {
        return variantCollapsibleInspector;
    }

    public CollapsibleInspector getGeneInspector() {
        return geneCollapsibleInspector;
    }

    public void setVariantRecord(final VariantRecord r) {
        this.switchToVariantInspector();
        final SimpleVariant sv = new SimpleVariant(r.getChrom(), r.getPosition(), r.getRef(), r.getAlt(), r.getType().toString());

        final ComprehensiveInspector instance = this;

        instance.getVariantInspector().setMessage(new WaitPanel("Getting detailed variant information..."));
        instance.getVariantInspector().switchToMessage();
        new MedSavantWorker<Object>(ComprehensiveInspector.class.toString()) {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Object result) {
                instance.getVariantInspector().switchToPanes();
            }

            @Override
            protected Object doInBackground() throws Exception {
                instance.simpleVariantInspector.setSimpleVariant(sv);
                if(detailedVariantSubInspector != null){
                    instance.detailedVariantSubInspector.setVariantRecord(r);
                }
                if(instance.otherIndividualsVariantSubInspector != null){
                    instance.otherIndividualsVariantSubInspector.handleEvent(sv);
                }
                if(instance.socialSubInspector != null){
                    instance.socialSubInspector.handleEvent(r);
                }
                return null;
            }
        }.execute();       

    }

    public void setGene(final Gene gene) {
        this.switchToGeneInspector();
        this.getGeneInspector().setMessage(new WaitPanel("Getting detailed gene information..."));
        this.getGeneInspector().switchToMessage();

        new MedSavantWorker<Object>(ComprehensiveInspector.class.toString()) {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Object result) {
                getGeneInspector().switchToPanes();
            }

            @Override
            protected Object doInBackground() throws Exception {
                if(geneSubInspector != null){
                    geneSubInspector.handleEvent(gene);
                }
                if(ontologySubInspector != null){
                    ontologySubInspector.handleEvent(gene);
                }
                if(geneManiaInspector != null){
                    geneManiaInspector.handleEvent(gene);
                }
                if(otherIndividualsDetailedSubInspector != null){
                    otherIndividualsDetailedSubInspector.handleEvent(gene);
                }

                return null;
            }
        }.execute();

    }

    public void setSimpleVariant(final SimpleVariant sv) {

        final ComprehensiveInspector instance = this;

        instance.getVariantInspector().setMessage(new WaitPanel("Getting detailed variant information..."));
        instance.getVariantInspector().switchToMessage();
        new MedSavantWorker<Object>(ComprehensiveInspector.class.toString()) {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Object result) {
                instance.getVariantInspector().switchToPanes();
            }

            @Override
            protected Object doInBackground() throws Exception {
                if(instance.simpleVariantInspector != null){
                    instance.simpleVariantInspector.setSimpleVariant(sv);
                }
                if(instance.otherIndividualsVariantSubInspector != null){
                    instance.otherIndividualsVariantSubInspector.handleEvent(sv);
                }
                // TODO hide detailed inspector and social inspector

                return null;
            }
        }.execute();
    }

    @Override
    public void handleEvent(Object event) {
        if (event instanceof Gene) {
            setGene((Gene) event);
        } else if (event instanceof SimpleVariant) {
            this.setSimpleVariant((SimpleVariant) event);
        } else if (event instanceof VariantRecord) {                     
            this.setVariantRecord((VariantRecord) event);
        } 
        for(Listener<Object> l : selectionListeners){
            l.handleEvent(event);
        }
    }

    private enum InspectorEnum {

        VARIANT, GENE
    };
    private EnumMap<ComprehensiveInspector.InspectorEnum, Integer> inspectorsToTabIndexMap = new EnumMap<ComprehensiveInspector.InspectorEnum, Integer>(ComprehensiveInspector.InspectorEnum.class);
        
    public ComprehensiveInspector(
            boolean createSimpleVariantInspector,
            boolean createDetailedVariantInspector,
            boolean createSocialVariantInspector,
            boolean createGeneSubInspector,
            boolean createOntologySubInspector,
            boolean createGeneManiaInspector,
            boolean createOtherIndividualsInspector,
            SplitScreenPanel splitScreenPanel) {

        setTabPlacement(JTabbedPane.TOP);
        setBorder(ViewUtil.getBigBorder());
        setBackground(ViewUtil.getTertiaryMenuColor());

        createSubInspectors(
                createSimpleVariantInspector,
                createDetailedVariantInspector,
                createSocialVariantInspector,
                createGeneSubInspector,
                createOntologySubInspector,
                createGeneManiaInspector,
                createOtherIndividualsInspector,
                splitScreenPanel);
        
        if(createSimpleVariantInspector){
            simpleVariantInspector.setGeneListener(this);
            this.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    int selectedIndex = getSelectedIndex();
                    if (getTitleAt(selectedIndex).equals("Gene")) {
                        setGene(simpleVariantInspector.getSelectedGene());
                    }else if(getTitleAt(selectedIndex).equals("Variant")){
                        setSimpleVariant(simpleVariantInspector.getSimpleVariant());
                    }
                }
            });
        }
    }

    public void switchToGeneInspector() {
        switchToInspector(ComprehensiveInspector.InspectorEnum.GENE);
    }

    public void switchToVariantInspector() {
        switchToInspector(ComprehensiveInspector.InspectorEnum.VARIANT);
    }

    private void switchToInspector(ComprehensiveInspector.InspectorEnum i) {
        this.setSelectedIndex(this.inspectorsToTabIndexMap.get(i));
    }

    private void addTabPanel(ComprehensiveInspector.InspectorEnum i, Inspector inspector) {
        inspectorsToTabIndexMap.put(i, this.getTabCount());
        addTab(inspector.getName(), null, ViewUtil.getClearBorderlessScrollPane(inspector.getContent()), inspector.getName());
    }
}
