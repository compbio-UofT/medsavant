/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.genetics.inspector;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.api.MedSavantGeneInspectorApp;
import org.ut.biolab.medsavant.client.api.MedSavantVariantInspectorApp;
import org.ut.biolab.medsavant.client.api.MedSavantVariantSearchApp;
import org.ut.biolab.medsavant.client.plugin.AppDescriptor;
import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.medsavant.client.plugin.AppController;
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
    private MedSavantVariantInspectorApp[] appVariantInspectors;
    private SocialVariantSubInspector socialSubInspector;
    private OtherIndividualsVariantSubInspector otherIndividualsVariantSubInspector;
    private OtherIndividualsGeneSubInspector otherIndividualsDetailedSubInspector;
    private List<Listener<Object>> selectionListeners = new LinkedList<Listener<Object>>();
    private MedSavantGeneInspectorApp[] appGeneInspectors;

    public void addSelectionListener(Listener<Object> selectionListener) {
        selectionListeners.add(selectionListener);
    }

    public DetailedVariantSubInspector getDetailedVariantSubInspector() {
        return detailedVariantSubInspector;
    }

    public SocialVariantSubInspector getSocialSubInspector() {
        return socialSubInspector;
    }

    String VARIANT_HELP_TITLE = "How to use the Variant Inspector";
    String VARIANT_HELP_TEXT = "The Variant Inspector shows detailed information about the variant selected from the Spreadsheet.";
    String GENE_HELP_TITLE = "How to use the Gene Inspector";
    String GENE_HELP_TEXT = "The Gene Inspector shows detailed information about the gene intersecting the variant selected from the Spreadsheet. If multiple genes intersect a variant, you can change which gene is being inspected via the Variant Inspector.";

    private void createSubInspectors(
            boolean createSimpleVariantInspector,
            boolean createDetailedVariantInspector,
            boolean createSocialVariantInspector,
            boolean createGeneSubInspector,
            boolean createOntologySubInspector,
            boolean createGeneManiaInspector,
            boolean createOtherIndividualsInspector,
            boolean createAppVariantInspectors,
            boolean createAppGeneInspectors,
            SplitScreenPanel splitScreenPanel) {

        // Assemble the variant inspector
        variantCollapsibleInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Variant";
            }
        };

        variantCollapsibleInspector.setMessage("No variant selected", VARIANT_HELP_TITLE, VARIANT_HELP_TEXT);
        variantCollapsibleInspector.switchToMessage();

        // Assemble the gene inspector
        geneCollapsibleInspector = new CollapsibleInspector() {
            @Override
            public String getName() {
                return "Gene";
            }
        };

        geneCollapsibleInspector.setMessage("No gene selected", GENE_HELP_TITLE, GENE_HELP_TEXT);

        variantCollapsibleInspector.addComponent(ViewUtil.getHelpButton(VARIANT_HELP_TITLE, VARIANT_HELP_TEXT));

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

        if (createAppVariantInspectors) {
            loadVariantInspectorApps();
            for (MedSavantVariantInspectorApp app : appVariantInspectors) {
                variantCollapsibleInspector.addSubInspector(app.getSubInspector());
            }
        }

        geneCollapsibleInspector.addComponent(ViewUtil.getHelpButton(GENE_HELP_TITLE, GENE_HELP_TEXT));

        // Gene
        if (createGeneSubInspector) {
            geneSubInspector = new GeneSubInspector();
            geneCollapsibleInspector.addSubInspector(geneSubInspector);
        }

        if (createOtherIndividualsInspector) { //TODO: Change to createOtherIndividualsDetailedSubInspector
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

        if (createAppGeneInspectors) {
            loadGeneInspectorApps();
            for (MedSavantGeneInspectorApp app : appGeneInspectors) {
                geneCollapsibleInspector.addSubInspector(app.getSubInspector());
            }
        }

        // Assemble everything
        addTabPanel(ComprehensiveInspector.InspectorEnum.VARIANT, variantCollapsibleInspector);
        addTabPanel(ComprehensiveInspector.InspectorEnum.GENE, geneCollapsibleInspector);

    }

    private void loadVariantInspectorApps() {
        List<MedSavantVariantInspectorApp> results = new LinkedList<MedSavantVariantInspectorApp>();

        for (AppDescriptor ad : AppController.getInstance().getDescriptors()) {
            MedSavantApp ap = AppController.getInstance().getPlugin(ad.getID());
            if (ap instanceof MedSavantVariantInspectorApp) {
                results.add((MedSavantVariantInspectorApp) ap);
            }
        }

        this.appVariantInspectors = results.toArray(new MedSavantVariantInspectorApp[results.size()]);
    }

    private void loadGeneInspectorApps() {
        List<MedSavantGeneInspectorApp> results = new LinkedList<MedSavantGeneInspectorApp>();

        for (AppDescriptor ad : AppController.getInstance().getDescriptors()) {
            MedSavantApp ap = AppController.getInstance().getPlugin(ad.getID());
            if (ap instanceof MedSavantGeneInspectorApp) {
                results.add((MedSavantGeneInspectorApp) ap);
            }
        }

        this.appGeneInspectors = results.toArray(new MedSavantGeneInspectorApp[results.size()]);
    }

    public CollapsibleInspector getVariantInspector() {
        return variantCollapsibleInspector;
    }

    public CollapsibleInspector getGeneInspector() {
        return geneCollapsibleInspector;
    }
    private MedSavantWorker<Object> variantRecordSetterThread;
    private VariantRecord currentVariantRecord;

    public void setVariantRecord(final VariantRecord r) {

        currentVariantRecord = r;

        if (r == null) {
            getVariantInspector().setMessage("No variant selected", VARIANT_HELP_TITLE, VARIANT_HELP_TEXT);
            getVariantInspector().switchToMessage();
            return;
        }

        //System.out.println("Setting variant Record");
        if (variantRecordSetterThread == null || variantRecordSetterThread.isDone()) {
            this.switchToVariantInspector();
            final SimpleVariant sv = new SimpleVariant(r.getChrom(), r.getPosition(), r.getRef(), r.getAlt(), r.getType().toString());

            final ComprehensiveInspector instance = this;

            instance.getVariantInspector().setMessage(new WaitPanel("Getting detailed variant information..."));
            instance.getVariantInspector().switchToMessage();
            variantRecordSetterThread = new MedSavantWorker<Object>(SubInspector.PAGE_NAME) {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Object result) {
                    instance.getVariantInspector().switchToPanes();
                }

                @Override
                protected Object doInBackground() throws Exception {
                    VariantRecord r;
                    do {
                        r = currentVariantRecord;

                        instance.simpleVariantInspector.setSimpleVariant(sv);
                        if (detailedVariantSubInspector != null) {
                            instance.detailedVariantSubInspector.setVariantRecord(r);
                        }
                        if (instance.otherIndividualsVariantSubInspector != null) {
                            instance.otherIndividualsVariantSubInspector.handleEvent(sv);
                        }
                        if (instance.socialSubInspector != null) {
                            instance.socialSubInspector.handleEvent(r);
                        }

                        if (appVariantInspectors != null) {
                            for (MedSavantVariantInspectorApp app : appVariantInspectors) {
                                app.setVariantRecord(r);
                            }
                        }

                    } while (currentVariantRecord != r);

                    return null;
                }
            };
            variantRecordSetterThread.execute();
        } else {
            //System.out.println("Reusing existing thread...");
        }

    }

    public void setGene(final Gene gene) {

        if (gene == null) {
            getGeneInspector().setMessage("No gene selected", GENE_HELP_TITLE, GENE_HELP_TEXT);
            getGeneInspector().switchToMessage();
            return;
        }

        this.switchToGeneInspector();

        this.getGeneInspector().setMessage(new WaitPanel("Getting detailed gene information..."));
        this.getGeneInspector().switchToMessage();

        new MedSavantWorker<Object>(SubInspector.PAGE_NAME) {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Object result) {
                getGeneInspector().switchToPanes();
            }

            @Override
            protected Object doInBackground() throws Exception {
                if (geneSubInspector != null) {
                    geneSubInspector.handleEvent(gene);
                }
                if (ontologySubInspector != null) {
                    ontologySubInspector.handleEvent(gene);
                }
                if (geneManiaInspector != null) {
                    geneManiaInspector.handleEvent(gene);
                }

                if (otherIndividualsDetailedSubInspector != null) {
                    otherIndividualsDetailedSubInspector.handleEvent(gene);
                }

                if (appGeneInspectors != null) {
                    for (MedSavantGeneInspectorApp app : appGeneInspectors) {
                        app.setGene(gene);
                    }
                }

                return null;
            }
        }.execute();

    }
    private MedSavantWorker<Object> variantSetterThread;
    private SimpleVariant currentSimpleVariant;

    public synchronized void setSimpleVariant(final SimpleVariant sv) {
        currentSimpleVariant = sv;
        if (variantSetterThread == null || variantSetterThread.isDone()) {

            final ComprehensiveInspector instance = this;

            instance.getVariantInspector().setMessage(new WaitPanel("Getting detailed variant information..."));
            instance.getVariantInspector().switchToMessage();
            variantSetterThread = new MedSavantWorker<Object>(SubInspector.PAGE_NAME) {
                @Override
                protected void showProgress(double fract) {
                }

                @Override
                protected void showSuccess(Object result) {
                    instance.getVariantInspector().switchToPanes();
                }

                @Override
                protected Object doInBackground() throws Exception {
                    SimpleVariant sv;
                    do {
                        sv = currentSimpleVariant;
                        if (sv != null) {
                            if (instance.simpleVariantInspector != null) {
                                instance.simpleVariantInspector.setSimpleVariant(sv);
                            }
                            if (instance.otherIndividualsVariantSubInspector != null) {
                                instance.otherIndividualsVariantSubInspector.handleEvent(sv);
                            }
                        }
                        // TODO hide detailed inspector and social inspector
                    } while (currentSimpleVariant != sv);
                    return null;
                }
            };
            variantSetterThread.execute();
        }
    }
    private MedSavantWorker<Void> eventHandlerThread;
    private Object currentEvent;

    @Override
    public synchronized void handleEvent(final Object event) {
        //  System.out.println("handleEvent " + System.currentTimeMillis());

        currentEvent = event;

        if (eventHandlerThread == null || eventHandlerThread.isDone()) {
            eventHandlerThread = new MedSavantWorker<Void>(SubInspector.PAGE_NAME) {
                @Override
                protected Void doInBackground() throws Exception {
                    do {
                        if (event instanceof Gene) {
                            setGene((Gene) event);
                        } else if (event instanceof SimpleVariant) {
                            setSimpleVariant((SimpleVariant) event);
                        } else if (event instanceof VariantRecord) {
                            setVariantRecord((VariantRecord) event);
                        }
                        for (Listener<Object> l : selectionListeners) {
                            l.handleEvent(event);
                        }
                    } while (currentEvent != event);
                    return null;
                }

                @Override
                protected void showSuccess(Void result) {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
            eventHandlerThread.execute();

        }
    }

    private enum InspectorEnum {

        VARIANT, GENE
    };
    private EnumMap<ComprehensiveInspector.InspectorEnum, Integer> inspectorsToTabIndexMap = new EnumMap<ComprehensiveInspector.InspectorEnum, Integer>(ComprehensiveInspector.InspectorEnum.class
    );

    public ComprehensiveInspector(
            boolean createSimpleVariantInspector,
            boolean createDetailedVariantInspector,
            boolean createSocialVariantInspector,
            boolean createGeneSubInspector,
            boolean createOntologySubInspector,
            boolean createGeneManiaInspector,
            boolean createOtherIndividualsInspector,
            boolean createVariantAppInspectors,
            boolean createAppGeneInspectors,
            SplitScreenPanel splitScreenPanel) {

        setTabPlacement(JTabbedPane.TOP);
        setBorder(ViewUtil.getBigBorder());

        createSubInspectors(
                createSimpleVariantInspector,
                createDetailedVariantInspector,
                createSocialVariantInspector,
                createGeneSubInspector,
                createOntologySubInspector,
                createGeneManiaInspector,
                createOtherIndividualsInspector,
                createVariantAppInspectors,
                createAppGeneInspectors,
                splitScreenPanel);

        if (createSimpleVariantInspector) {
            simpleVariantInspector.setGeneListener(this);
            /*
            this.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent ce) {
                    int selectedIndex = getSelectedIndex();
                    if (getTitleAt(selectedIndex).equals("Gene")) {
                        if (simpleVariantInspector.getSelectedGene() != null) {
                            setGene(simpleVariantInspector.getSelectedGene());
                        }

                    } else if (getTitleAt(selectedIndex).equals("Variant")) {
                        if (simpleVariantInspector.getSimpleVariant() != null) {
                            setSimpleVariant(simpleVariantInspector.getSimpleVariant());
                        }
                    }
                }
            });
            */
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
        JScrollPane jsp = ViewUtil.getClearBorderlessScrollPane(inspector.getContent());
        jsp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        addTab(inspector.getName(), null, jsp, inspector.getName());
    }
}
