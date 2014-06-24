/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.app.mendelclinic.controller;

import org.ut.biolab.medsavant.client.view.app.builtin.task.BackgroundTaskWorker;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.apache.commons.lang3.StringUtils;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.client.patient.pedigree.PedigreeFields;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.MedSavantExceptionHandler;
import org.ut.biolab.medsavant.client.view.component.SplitScreenPanel;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;

import org.ut.biolab.medsavant.app.mendelclinic.controller.MendelWorker.SimplePatientSet;
import org.ut.biolab.medsavant.app.mendelclinic.model.Locks;
import org.ut.biolab.medsavant.app.mendelclinic.model.MendelGene;
import org.ut.biolab.medsavant.app.mendelclinic.model.MendelVariant;
import org.ut.biolab.medsavant.app.mendelclinic.view.MendelPanel;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.IncludeExcludeStep;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel;
import static org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel.AUTOSOMAL_DOMINANT;
import static org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel.AUTOSOMAL_RECESSIVE;
import static org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel.DE_NOVO;
import static org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel.X_LINKED_DOMINANT;
import static org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.InheritanceStep.InheritanceModel.X_LINKED_RECESSIVE;
import org.ut.biolab.medsavant.app.mendelclinic.view.OptionView.ZygosityStep;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.util.PeekingPanelContainer;
import org.ut.biolab.medsavant.shared.model.SessionExpiredException;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord.Zygosity;

/**
 *
 * @author mfiume
 */
public class MendelWorker extends MedSavantWorker<TreeMap<MendelVariant, SimplePatientSet>> implements PedigreeFields {

    private final List<IncludeExcludeStep> steps;
    private final File inFile;
    private Locks.DialogLock completionLock;
    private final InheritanceStep inheritanceStep;
    private final ZygosityStep zygosityStep;
    private static int runNumer = 0;
    private BackgroundTaskWorker taskWorker;

    public MendelWorker(BackgroundTaskWorker taskWorker, List<IncludeExcludeStep> steps, ZygosityStep zygosityStep, InheritanceStep model, File inFile) {
        super("Mendel " + (++runNumer));
        this.taskWorker = taskWorker;
        this.steps = steps;
        this.zygosityStep = zygosityStep;
        this.inheritanceStep = model;
        this.inFile = inFile;
    }

    @Override
    protected void showSuccess(final TreeMap<MendelVariant, SimplePatientSet> result) {

        taskWorker.addLog("Preparing results...");

        String pageName = MendelPanel.PAGE_NAME;
        String[] columnNames = new String[]{"Chromosome", "Start", "End", "Reference", "Alternate", "Type", "Samples", "Genes"};
        Class[] columnClasses = new Class[]{String.class, String.class,  String.class, String.class, String.class, String.class, String.class, String.class};

        System.out.println(result.keySet().size() + " variants in result set");

        final List<MendelVariant> variants = new ArrayList<MendelVariant>(result.keySet());

        System.out.println(variants.size() + " variants to be shown");
        //for (MendelVariant v : variants) {
        //    System.out.println(v);
        //}

        DataRetriever<Object[]> retriever = new DataRetriever<Object[]>() {
            @Override
            public List<Object[]> retrieve(int start, int limit) throws Exception {

                System.out.println("Showing " + limit + " results starting from " + start + " of " + variants.size());
                List<Object[]> rows = new ArrayList<Object[]>();

                for (int i = 0; i < limit && (i + start) < variants.size(); i++) {

                    MendelVariant v = variants.get(i + start);

                    //System.out.println("\t" + (i + start) + ". " + v);
                    String geneStr = "";
                    for (MendelGene g : v.getGenes()) {
                        geneStr += g.name + ", ";
                    }
                    if (v.getGenes().size() > 0) {
                        geneStr = geneStr.substring(0, geneStr.length() - 2);
                    }

                    rows.add(new Object[]{
                        v.chr,
                        v.start_pos,
                        v.end_pos,
                        v.ref,
                        v.alt,
                        v.type,
                        result.get(v).toString(),//.replaceAll("\"", ""),//.replaceFirst("\[", "").replaceFirst("\]", ""),
                        geneStr});

                }

                return rows;
            }

            @Override
            public int getTotalNum() {
                //LOG.info("Number of results " + result.keySet().size());
                return variants.size();
            }

            @Override
            public void retrievalComplete() {
            }
        };

        final SearchableTablePanel stp
                = new SearchableTablePanel(
                        pageName,
                        columnNames,
                        columnClasses,
                        new int[]{5},
                        true,
                        false,
                        500,
                        true,
                        SearchableTablePanel.TableSelectionType.ROW,
                        1000,
                        retriever);

        final SplitScreenPanel ssp = new SplitScreenPanel(stp);
        final ComprehensiveInspector vip = new ComprehensiveInspector(true, false, false, true, true, true, true, true, true, ssp);

        vip.addSelectionListener(new Listener<Object>() {
            @Override
            public void handleEvent(Object event) {
                stp.getTable().clearSelection();
            }
        });

        stp.scrollSafeSelectAction(new Runnable() {
            @Override
            public void run() {
                if (stp.getTable().getSelectedRow() != -1) {

                    int index = stp.getActualRowAcrossAllPages(stp.getTable().getSelectedRow());

                    MendelVariant fmv = variants.get(index);
                    //SimpleVariant(String chr, long start_pos, long end_pos, String ref, String alt, String type) {
                    SimpleVariant v = new SimpleVariant(fmv.chr, fmv.start_pos, fmv.end_pos, fmv.ref, fmv.alt, fmv.type);
                    vip.setSimpleVariant(v);
                }
            }
        });

        JDialog f = new JDialog(MedSavantFrame.getInstance(), "Mendel Results");
        f.setLocationRelativeTo(MedSavantFrame.getInstance());

        JPanel aligned = new JPanel();
        aligned.setLayout(new BorderLayout());
        aligned.setBorder(null);
        aligned.setPreferredSize(new Dimension(450, 999));
        aligned.add(vip, BorderLayout.CENTER);

        PeekingPanelContainer p = new PeekingPanelContainer(ssp);
        p.addPeekingPanel("Inspector", BorderLayout.EAST, aligned, true);

        f.add(p);

        stp.forceRefreshData();
        f.setPreferredSize(new Dimension(600, 600));
        f.setMinimumSize(new Dimension(600, 600));
        
        taskWorker.addLog("Done");

        if (completionLock != null) {
            synchronized (completionLock) {
                completionLock.setResultsFrame(f);
                completionLock.notify();
            }
        }
    }

    public void setCompletionLock(Locks.DialogLock lock) {
        this.completionLock = lock;
    }

    private void filterForCompoundHeterozygote(TreeMap<MendelVariant, SimplePatientSet> variantToSampleMap, TreeMap<MendelGene, Set<MendelVariant>> genesToVariantsMap, List<SimpleFamily> families) {
        // a list of variants to eventually remove
        List<MendelVariant> variantsToRemove = new ArrayList<MendelVariant>();

        int counter = 0;
        int announceEvery = 100;

        int total = variantToSampleMap.keySet().size();

        // consider each variant in turn
        for (MendelVariant variant : variantToSampleMap.keySet()) {

            if (counter % announceEvery == 0) {
                double percent = ((double) counter) * 100 / total;
                System.out.println("Processed " + counter + " of " + variantToSampleMap.keySet().size() + " (" + percent + "%) variants");
            }
            counter++;

            boolean passesForAllFamilies = true;

            // test the model on each family
            for (SimpleFamily fam : families) {

                boolean passesForFamily = false;

                for (MendelGene gene : variant.getGenes()) {

                    for (MendelVariant otherVariantInGene : genesToVariantsMap.get(gene)) {
                        if (variant == otherVariantInGene) {
                            continue;
                        }

                        passesForFamily = passesForFamily || testVariantsforCompountHeterozygousInFamily(variant, otherVariantInGene, variantToSampleMap.get(variant), variantToSampleMap.get(otherVariantInGene), fam);

                    }

                }

                passesForAllFamilies = passesForAllFamilies && passesForFamily;
            }

            // record the ones that don't pass
            if (!passesForAllFamilies) {
                variantsToRemove.add(variant);
            }
        }

        // remove the ones that don't pass
        for (MendelVariant v : variantsToRemove) {
            variantToSampleMap.remove(v);
        }
    }

    private boolean testVariantsforCompountHeterozygousInFamily(MendelVariant variant1, MendelVariant variant2, SimplePatientSet possessors1, SimplePatientSet possessors2, SimpleFamily fam) {

        if (possessors1 == null || possessors2 == null) {
            return false;
        }

        boolean evidenceInFamily = false;

        //System.out.println("Comparing " + variant1 + " (" + possessors1 + ") to " + variant2 + " (" + possessors2 + ")");
        // remove otherwise non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            // affected, should have both as het
            if (p.isAffected()) {

                String dnaID = p.getDnaID();

                // affecteds should have variant entries at both sites
                if (!possessors1.containsDNAID(dnaID) || !possessors2.containsDNAID(dnaID)) {
                    return false;
                }

                Zygosity variant1ThisZygosity = possessors1.getZygosityForDNAID(dnaID);
                Zygosity variant2ThisZygosity = possessors2.getZygosityForDNAID(dnaID);

                // affecteds should be het at both sites
                if (variant1ThisZygosity != Zygosity.Hetero || variant2ThisZygosity != Zygosity.Hetero) {
                    return false;
                }

                // each parents should have one
                String momDNAId = p.getMomDNAID();
                String dadDNAId = p.getDadDNAID();
                if (momDNAId != null && dadDNAId != null
                        && ( // both have first
                        (possessors1.containsDNAID(momDNAId) && possessors1.containsDNAID(dadDNAId))
                        // neither has first
                        || (!possessors1.containsDNAID(momDNAId) && !possessors1.containsDNAID(dadDNAId))
                        // both have second
                        || (possessors2.containsDNAID(momDNAId) && possessors2.containsDNAID(dadDNAId))
                        // neither has second
                        || (!possessors2.containsDNAID(momDNAId) && !possessors2.containsDNAID(dadDNAId)))) {
                    return false;
                }

                if (p.areBothParentsLinked()) {

                    boolean momHasOne = possessors1.containsDNAID(momDNAId) || possessors2.containsDNAID(momDNAId);
                    boolean dadHasOne = possessors1.containsDNAID(dadDNAId) || possessors2.containsDNAID(dadDNAId);

                    // each has exactly one, since 0 and 2 count checks were done above
                    if (momHasOne && dadHasOne) {
                        evidenceInFamily = true;
                    }
                }
            } else {

                // should not be anything but het at these positions
                String dnaID = p.getDnaID();

                if (possessors1.containsDNAID(dnaID) && possessors1.getZygosityForDNAID(dnaID) != Zygosity.Hetero) {
                    return false;
                }
                if (possessors2.containsDNAID(dnaID) && possessors2.getZygosityForDNAID(dnaID) != Zygosity.Hetero) {
                    return false;
                }

            }
        }

        return evidenceInFamily;
    }

    private TreeSet<MendelGene> getGeneSet() throws SQLException, RemoteException {
        Collection<Gene> msGenes = GeneSetController.getInstance().getCurrentGenes();
        TreeSet<MendelGene> genes = new TreeSet<MendelGene>();
        for (Gene g : msGenes) {
            MendelGene simpleGene = new MendelGene(g.getChrom(), g.getCodingStart(), g.getCodingEnd(), g.getName());
            genes.add(simpleGene);
        }
        return genes;
    }

    private TreeMap<MendelGene, Set<MendelVariant>> associateGenesAndVariants(TreeSet<MendelVariant> variants, TreeSet<MendelGene> genes) {

        //LOG.info("associate Genes And Variants");
        long startTime = System.currentTimeMillis();

        Map<String, Set<MendelVariant>> chromToVariantMap = new HashMap<String, Set<MendelVariant>>();
        for (MendelVariant v : variants) {
            Set<MendelVariant> set;
            if (chromToVariantMap.containsKey(v.chr)) {
                set = chromToVariantMap.get(v.chr);
            } else {
                set = new HashSet<MendelVariant>();
                chromToVariantMap.put(v.chr, set);
            }
            set.add(v);
        }

        TreeMap<MendelGene, Set<MendelVariant>> map = new TreeMap<MendelGene, Set<MendelVariant>>();

        int total = genes.size();
        double counter = 0.0;

        taskWorker.addLog("Associating genes and variants...");

        for (MendelGene g : genes) {
            //LOG.info("processing " + g.name);

            counter += 1;
            double p = counter * 100 / total;

            if (!chromToVariantMap.containsKey(g.chr)) {
                continue;
            }

            Set<MendelVariant> set = new HashSet<MendelVariant>();
            for (MendelVariant v : chromToVariantMap.get(g.chr)) {
                if (intersects(g, v)) {
                    set.add(v);
                    v.addGene(g);
                }
            }
            map.put(g, set);

        }

        long endTime = System.currentTimeMillis();

        System.out.println("Connecting genes to variants took " + (endTime - startTime) / 1000 + " seconds");

        return map;
    }

    private boolean intersects(long start1, long end1, long start2, long end2) {        
        if (start1 < start2) {
            if (end1 < start2) {
                return false;
            }
        } else if (start2 < start1) {
            if (end2 < end1) {
                return false;
            }
        }
        return true;
    }

    private boolean intersects(MendelGene g, MendelVariant v) {
        SimpleVariant sv;
        if(g.chr.equals(v.chr)){
            return intersects(g.start, g.end, v.start_pos, v.end_pos);            
        }else{
            return false;
        }
        
        //return (g.chr.equals(v.chr) && g.start <= v.start_pos && v.start_pos <= g.end);            
    }

    public class SimplePatientSet extends HashSet<SimplePatient> {

        private final HashMap<String, SimplePatient> dnaIDs;
        boolean immutable = false;

        public SimplePatientSet() {
            super();
            this.dnaIDs = new HashMap<String, SimplePatient>();
        }

        public Collection<SimplePatient> getPatients() {
            return dnaIDs.values();
        }

        @Override
        public String toString() {
            String[] toJoin = new String[dnaIDs.size()];

            int counter = 0;
            for (String s : dnaIDs.keySet()) {
                SimplePatient p = dnaIDs.get(s);
                toJoin[counter] = p.getDnaID() + " (" + p.zygosity + ")";
                counter++;
            }

            return StringUtils.join(toJoin, ", ");
        }

        @Override
        public boolean add(SimplePatient o) {
            if (immutable) {
                throw new UnsupportedOperationException("Can't modify to an immutable set");
            }
            this.dnaIDs.put(o.getDnaID(), o);
            return super.add(o);
        }

        @Override
        public boolean remove(Object o) {
            if (immutable) {
                throw new UnsupportedOperationException("Can't modify to an immutable set");
            }

            if (o instanceof SimplePatient) {
                this.dnaIDs.remove(((SimplePatient) o).getDnaID());
            }
            return super.remove(o);
        }

        @Override
        public void clear() {
            if (immutable) {
                throw new UnsupportedOperationException("Can't modify to an immutable set");
            }
            super.clear();
        }

        public void makeImmutable() {
            immutable = true;
        }

        public boolean containsDNAID(String dnaID) {
            return dnaIDs.containsKey(dnaID);
        }

        private SimplePatient getPatientGenotypeForDNAID(String dnaID) {
            return dnaIDs.get(dnaID);
        }

        private Zygosity getZygosityForDNAID(String dnaID) {
            return getPatientGenotypeForDNAID(dnaID).getZygosity();
        }
    }

    private void filterForSimpleModels(InheritanceModel model, TreeMap<MendelVariant, SimplePatientSet> variantToSampleMap, List<SimpleFamily> families) {

        // a list of variants to eventually remove
        List<MendelVariant> variantsToRemove = new ArrayList<MendelVariant>();

        // consider each variant in turn
        for (MendelVariant variant : variantToSampleMap.keySet()) {
            boolean passesForAllFamilies = true;

            // test the model on each family
            for (SimpleFamily fam : families) {
                boolean passesForFamily = false;
                switch (model) {
                    case AUTOSOMAL_DOMINANT:
                        passesForFamily = testVariantforAutosmalDominantInFamily(variant, variantToSampleMap.get(variant), fam);
                        break;
                    case AUTOSOMAL_RECESSIVE:
                        passesForFamily = testVariantforAutosmalRecessiveInFamily(variant, variantToSampleMap.get(variant), fam);
                        break;
                    case X_LINKED_DOMINANT:
                        passesForFamily = testVariantforXLinkedDominantInFamily(variant, variantToSampleMap.get(variant), fam);
                        break;
                    case X_LINKED_RECESSIVE:
                        passesForFamily = testVariantforXLinkedRecessiveInFamily(variant, variantToSampleMap.get(variant), fam);
                        break;
                    case DE_NOVO:
                        passesForFamily = testVariantforDeNovoInFamily(variant, variantToSampleMap.get(variant), fam);
                        break;
                    default:
                        throw new UnsupportedOperationException("Unkown model: " + model.toString());
                }
                passesForAllFamilies = passesForAllFamilies && passesForFamily;
            }

            // record the ones that don't pass
            if (!passesForAllFamilies) {
                variantsToRemove.add(variant);
            }
        }

        System.out.println("Done running model" + model);

        // remove the ones that don't pass
        for (MendelVariant v : variantsToRemove) {
            variantToSampleMap.remove(v);
        }
    }

    private boolean testVariantforDeNovoInFamily(MendelVariant variant, SimplePatientSet possessors, SimpleFamily fam) {

        boolean evidenceInFamily = false;

        // remove non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            // affected, parents should not have it
            if (p.isAffected()) {

                // someone who's affected in the family has it
                evidenceInFamily = true;

                // both parents shouldn't have it
                String momDNAId = p.getMomDNAID();
                String dadDNAId = p.getDadDNAID();
                if (momDNAId != null && possessors.containsDNAID(p.getMomDNAID())) {
                    return false;
                }
                if (dadDNAId != null && possessors.containsDNAID(p.getDadDNAID())) {
                    return false;
                }

            } else {

                // shouldnt have unaffected posessors
                if (possessors.containsDNAID(p.getDnaID())) {
                    return false;
                }
            }
        }

        return evidenceInFamily;
    }

    private boolean testVariantforXLinkedDominantInFamily(MendelVariant variant, SimplePatientSet possessors, SimpleFamily fam) {

        // remove non-x chrs
        String simpleChr = variant.chr.toLowerCase();
        if (!simpleChr.equals("chrX") && !simpleChr.equals("X")) {
            return false;
        }

        boolean evidenceInFamily = false;

        // remove otherwise non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            // all affected individuals should have it
            if (p.isAffected() != possessors.containsDNAID(p.getDnaID())) {
                return false;
            }

            if (p.isAffected() && possessors.containsDNAID(p.getDnaID())) {
                evidenceInFamily = true;
            }

            if (p.areBothParentsLinked()) {
                // one of the parents should have it
                if (!possessors.containsDNAID(p.getMomDNAID()) && !possessors.containsDNAID(p.dadDNAID)) {
                    return false;
                }
            }
        }
        return evidenceInFamily;
    }

    private boolean testVariantforXLinkedRecessiveInFamily(MendelVariant variant, SimplePatientSet possessors, SimpleFamily fam) {

        // remove non-x chrs
        String simpleChr = variant.chr.toLowerCase();
        if (!simpleChr.equals("chrX") && !simpleChr.equals("X")) {
            return false;
        }

        boolean evidenceInFamily = false;

        // remove otherwise non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            if (p.getGender() == Gender.FEMALE) {

                // affected, should have it and be homozygous, and in both parents
                if (p.isAffected()) {

                    // should have it
                    if (!possessors.containsDNAID(p.getDnaID())) {
                        return false;
                    }

                    Zygosity z = possessors.getZygosityForDNAID(p.getDnaID());

                    // should be homozygous
                    if (z != Zygosity.HomoAlt) { // assuming homoref isn't causal
                        return false;
                    }

                    // both parents should have it
                    String momDNAId = p.getMomDNAID();
                    String dadDNAId = p.getDadDNAID();
                    if (momDNAId != null && !possessors.containsDNAID(p.getMomDNAID())) {
                        return false;
                    }
                    if (dadDNAId != null && !possessors.containsDNAID(p.getDadDNAID())) {
                        return false;
                    }

                    if (p.areBothParentsLinked()) {
                        evidenceInFamily = true;
                    }

                } else {

                    // shouldnt have unaffected female homozygotes
                    if (possessors.containsDNAID(p.getDnaID()) && (possessors.getZygosityForDNAID(p.getDnaID()) == Zygosity.HomoAlt)) { // assuming homoref isn't causal
                        return false;
                    }
                }
            } else if (p.getGender() == Gender.MALE) {

                // affected, should have it and be heterozygous, and in one of the parents
                if (p.isAffected()) {

                    // should have it
                    if (!possessors.containsDNAID(p.getDnaID())) {
                        return false;
                    }

                    Zygosity z = possessors.getZygosityForDNAID(p.getDnaID());

                    // should be heterozygous
                    if (z != Zygosity.Hetero) {
                        return false;
                    }

                    // one parent should have it
                    String momDNAId = p.getMomDNAID();
                    String dadDNAId = p.getDadDNAID();

                    boolean atLeastOneParentHasIt = false;
                    if (momDNAId != null && possessors.containsDNAID(p.getMomDNAID())) {
                        atLeastOneParentHasIt = true;
                    }
                    if (dadDNAId != null && possessors.containsDNAID(p.getDadDNAID())) {
                        atLeastOneParentHasIt = true;
                    }

                    // if parents are linked
                    if (p.areBothParentsLinked()) {

                        // follows model
                        if (atLeastOneParentHasIt) {
                            evidenceInFamily = true;

                            // doesn't follow model, neither parent has it
                        } else {
                            return false;
                        }
                    }

                } else {

                    // shouldnt have unaffected heterozygous males
                    if (possessors.containsDNAID(p.getDnaID()) && (possessors.getZygosityForDNAID(p.getDnaID()) == Zygosity.Hetero)) {
                        return false;
                    }
                }
            }
        }

        return evidenceInFamily;
    }

    private boolean testVariantforAutosmalDominantInFamily(MendelVariant variant, SimplePatientSet possessors, SimpleFamily fam) {

        // remove sex chrs
        String simpleChr = variant.chr.toLowerCase();
        if (simpleChr.equals("chrX") || simpleChr.equals("X") || simpleChr.equals("chrY") || simpleChr.equals("Y")) {
            return false;
        }

        boolean evidenceInFamily = false;

        // remove otherwise non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            // all affected individuals should have it
            if (p.isAffected() != possessors.containsDNAID(p.getDnaID())) {
                return false;
            }

            if (p.isAffected() && possessors.containsDNAID(p.getDnaID())) {
                evidenceInFamily = true;
            }

            if (p.areBothParentsLinked()) {
                // one of the parents should have it
                if (!possessors.containsDNAID(p.getMomDNAID()) && !possessors.containsDNAID(p.dadDNAID)) {
                    return false;
                }
            }
        }
        return evidenceInFamily;
    }

    private boolean testVariantforAutosmalRecessiveInFamily(MendelVariant variant, SimplePatientSet possessors, SimpleFamily fam) {

        // remove sex chrs
        String simpleChr = variant.chr.toLowerCase();
        if (simpleChr.equals("chrX") || simpleChr.equals("X") || simpleChr.equals("chrY") || simpleChr.equals("Y")) {
            return false;
        }

        boolean evidenceInFamily = false;

        // remove otherwise non-compliant variants
        for (SimplePerson p : fam.getFamilyMembers()) {

            // affected, should have it and be homozygous, and in both parents
            if (p.isAffected()) {

                // should have it
                if (!possessors.containsDNAID(p.getDnaID())) {
                    //LOG.info(variant + " = F : affected but don't have this " + possessors);
                    return false;
                }

                Zygosity z = possessors.getZygosityForDNAID(p.getDnaID());

                // should be homozygous
                if (z != Zygosity.HomoAlt) { // assuming homoref isn't causal
                    //LOG.info(variant + " = F : affected but not homozygous " + possessors);
                    return false;
                }

                // both parents should have it
                String momDNAId = p.getMomDNAID();
                String dadDNAId = p.getDadDNAID();
                if (momDNAId != null && !possessors.containsDNAID(p.getMomDNAID())) {
                    //LOG.info(variant + " = F : mother doesn't have it " + possessors);
                    return false;
                }
                if (dadDNAId != null && !possessors.containsDNAID(p.getDadDNAID())) {
                    //LOG.info(variant + " = F : father doesn't have it " + possessors);
                    return false;
                }

                if (p.areBothParentsLinked()) {
                    evidenceInFamily = true;
                }

            } else {

                // shouldnt have unaffected homozygotes
                if (possessors.containsDNAID(p.getDnaID()) && (possessors.getZygosityForDNAID(p.getDnaID()) == Zygosity.HomoAlt)) { // assuming homoref isn't causal
                    //LOG.info(variant + " = F : there's an unaffected homozygote " + possessors);
                    return false;
                }
            }
        }

        return evidenceInFamily;
    }

    @Override
    protected TreeMap<MendelVariant, SimplePatientSet> doInBackground() throws Exception {

        TreeMap<MendelVariant, SimplePatientSet> variantToSampleMap;
        TreeMap<MendelGene, Set<MendelVariant>> genesToVariantsMap;
        TreeSet<MendelVariant> variants;
        TreeSet<MendelGene> genes;

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;

        /**
         * Map variants to samples NB: keys in a TreeMap are sorted
         */
        taskWorker.addLog("Parsing variants...");
        variantToSampleMap = readVariantToSampleMap(inFile);

        // get sorted lists of variants and genes
        variants = new TreeSet<MendelVariant>(variantToSampleMap.keySet());
        taskWorker.addLog("Parsing genes...");
        genes = getGeneSet();

        // map variants to genes and vice versa
        taskWorker.addLog("Associating variants with genes...");
        genesToVariantsMap = associateGenesAndVariants(variants, genes);

        System.out.println("Number of variants (grouped by position):\t" + variants.size());
        System.out.println("Number of genes:\t" + genes.size());

        Set<MendelGene> allExcludedGenes = new HashSet<MendelGene>();

        boolean requiresVariantsToBeGenic = false; // true if there's a gene term

        // perform steps in serial
        int stepNumber = 0;
        for (OptionView.IncludeExcludeStep step : steps) {

            ++stepNumber;

            System.out.println("Getting gene to sample map");
            Map<MendelGene, SimplePatientSet> geneToSampleMap = getGeneToSampleMap(variantToSampleMap, allExcludedGenes);
            System.out.println("Size of gene map is " + geneToSampleMap.keySet().size());

            Set<MendelVariant> allExcludedVariants = new HashSet<MendelVariant>();

            int criteriaNumber = 0;

            for (OptionView.IncludeExcludeCriteria criterion : step.getCriteria()) {

                ++criteriaNumber;

                //bw.write("# executing criteria " + criteriaNumber + " of " + step.getCriteria().size() + " of step #" + stepNumber + "\n");
                System.out.println("Executing criteria #" + criteriaNumber + " of " + step.getCriteria().size() + " of step #" + stepNumber);
                System.out.println(criterion);

                taskWorker.addLog("Executing criteria #" + criteriaNumber + " of " + step.getCriteria().size() + " of step #" + stepNumber + "...");

                Set<String> setOfDNAIDs = criterion.getDNAIDs(); //TODO: write method
                //List<String> dnaIDsInCohort = MedSavantClient.CohortManager.getDNAIDsForCohort(LoginController.getSessionID(), criterion.getCohort().getId());

                Set<MendelVariant> excludedVariantsFromThisStep = new HashSet<MendelVariant>();

                int frequencyThreshold = getFrequencyThresholdForCriterion(criterion);

                OptionView.IncludeExcludeCriteria.AggregationType at = criterion.getAggregationType();
                if (at == OptionView.IncludeExcludeCriteria.AggregationType.Variant) {
                    excludedVariantsFromThisStep = (Set<MendelVariant>) ((Object) flagObjectsForRemovalByCriterion((Map<Object, Set<SimplePatient>>) ((Object) variantToSampleMap), frequencyThreshold, criterion.getFrequencyType(), setOfDNAIDs));
                } else {

                    Set<MendelGene> includedGenesFromThisStep = (Set<MendelGene>) ((Object) flagObjectsForKeepsByCriterion((Map<Object, Set<SimplePatient>>) ((Object) geneToSampleMap), frequencyThreshold, criterion.getFrequencyType(), setOfDNAIDs));

                    HashSet<MendelVariant> keptVariantsFromThisStep = new HashSet<MendelVariant>();
                    for (MendelGene gene : includedGenesFromThisStep) {
                        keptVariantsFromThisStep.addAll(genesToVariantsMap.get(gene));
                    }

                    excludedVariantsFromThisStep = new HashSet<MendelVariant>();
                    for (MendelVariant v : variantToSampleMap.keySet()) {
                        if (!keptVariantsFromThisStep.contains(v)) {
                            excludedVariantsFromThisStep.add(v);
                        }

                        requiresVariantsToBeGenic = true;
                    }

                    // queue excluded genes for removal in next steps
                    for (MendelGene g : geneToSampleMap.keySet()) {
                        if (!includedGenesFromThisStep.contains(g)) {
                            allExcludedGenes.add(g);
                        }
                    }
                }

                //bw.write("# excluding " + excludedVariantsFromThisStep.size() + " variants from this step\n");
                System.out.println("Excluding " + excludedVariantsFromThisStep.size() + " variants from this step");

                int currentNumExcluded = allExcludedVariants.size();
                allExcludedVariants.addAll(excludedVariantsFromThisStep);
                int afterNumExcluded = allExcludedVariants.size();
                int numSeenBefore = excludedVariantsFromThisStep.size() - (afterNumExcluded - currentNumExcluded);
                System.out.println(numSeenBefore + " of these were already excluded previously");

            }

            // remove variants
            for (MendelVariant v : allExcludedVariants) {
                variantToSampleMap.remove(v);
            }
        }

        getGeneToSampleMap(variantToSampleMap, allExcludedGenes);

        if (zygosityStep.isSet()) {
            taskWorker.addLog("Running zygosity filter...");
            System.out.println("Running zygosity filter...");
            Zygosity zyg = zygosityStep.getZygosity();

            List<Object> variantsToRemove = new ArrayList<Object>();

            // consider each variant
            for (MendelVariant v : variantToSampleMap.keySet()) {

                SimplePatientSet patients = variantToSampleMap.get(v);
                List<Object> patientsToRemove = new ArrayList<Object>();

                for (SimplePatient p : patients.getPatients()) {
                    // queue nonmatching patients for deletion
                    if (p.zygosity != zyg) {
                        patientsToRemove.add(p);

                    }
                }

                // actually remove them
                for (Object o : patientsToRemove) {
                    patients.remove(o);
                }

                // queue variant for deletion if there's no patients have it anymore
                if (patients.dnaIDs.isEmpty()) {
                    variantsToRemove.add(v);
                }
            }

            // remove variants with no patients left
            for (Object o : variantsToRemove) {
                variantToSampleMap.remove(o);
            }

        }

        InheritanceModel model = inheritanceStep.getInheritanceModel();

        taskWorker.addLog("Running inheritance model " + model + "...");
        System.out.println("Running inheritance model " + model + "...");
        System.out.println("VariantSampleMap keysize: " + variantToSampleMap.keySet().size());
        // adjust for inheritance model
        if (model != InheritanceModel.ANY) {

            Set<String> familyIDs = inheritanceStep.getFamilies();
            List<SimpleFamily> families = getSimpleFamiliesFromIDs(familyIDs);

            if (model == InheritanceModel.AUTOSOMAL_DOMINANT
                    || model == InheritanceModel.AUTOSOMAL_RECESSIVE
                    || model == InheritanceModel.X_LINKED_DOMINANT
                    || model == InheritanceModel.X_LINKED_RECESSIVE
                    || model == InheritanceModel.DE_NOVO) {
                filterForSimpleModels(model, variantToSampleMap, families);

            } else if (model == InheritanceModel.COMPOUND_HETEROZYGOTE) {
                filterForCompoundHeterozygote(variantToSampleMap, genesToVariantsMap, families);
            }
        }

        if (requiresVariantsToBeGenic) {
            List<MendelVariant> toRemove = new ArrayList<MendelVariant>();
            for (MendelVariant v : variantToSampleMap.keySet()) {
                if (v.getGenes().isEmpty()) {
                    toRemove.add(v);
                }
            }
            for (MendelVariant v : toRemove) {
                variantToSampleMap.remove(v);
            }
        }

        System.out.println("Got " + variantToSampleMap.keySet().size() + " results in bg");

        return variantToSampleMap;
    }

    private List<SimpleFamily> getSimpleFamiliesFromIDs(Set<String> familyIDs) throws SQLException, RemoteException, SessionExpiredException {
        List<SimpleFamily> families = new ArrayList<SimpleFamily>();
        for (String familyID : familyIDs) {
            families.add(getSimpleFamilyFromID(familyID));
        }
        return families;
    }

    private SimpleFamily getSimpleFamilyFromID(String familyID) throws SQLException, RemoteException, SessionExpiredException {

        try {
            List<Object[]> results = MedSavantClient.PatientManager.getFamily(LoginController.getSessionID(), ProjectController.getInstance().getCurrentProjectID(), familyID);

            SimpleFamily fam = new SimpleFamily();
            for (Object[] o : results) {

                for (Object m : o) {
                    System.out.print(m + "\t");
                }
                System.out.println();

                Integer genderNum = (Integer) o[4];
                Gender gender = Gender.values()[genderNum];

                SimplePerson p = new SimplePerson(
                        (String) o[1], // mom
                        (String) o[2], // dad
                        (String) o[6], // dnaID
                        (Integer) o[5] == 1, // affected
                        gender);
                fam.addPerson(p);
            }

            return fam;

        } catch (SessionExpiredException ex) {
            MedSavantExceptionHandler.handleSessionExpiredException(ex);
            return null;

        }

    }

    public class SimpleFamily {

        private Map<String, SimplePerson> familyMembers; // dna id to person map

        public SimpleFamily() {
            this.familyMembers = new HashMap<String, SimplePerson>();
        }

        public void addPerson(SimplePerson p) {
            familyMembers.put(p.getDnaID(), p);
        }

        public List<SimplePerson> getFamilyMembers() {
            return new ArrayList<SimplePerson>(familyMembers.values());
        }

        private SimplePerson getPersonByID(String dnaID) {
            return familyMembers.get(dnaID);
        }
    }

    public class SimplePerson {

        private String momDNAID;
        private String dadDNAID;
        private String dnaID;
        private boolean affected;
        private Gender gender;

        public SimplePerson(String momDNAID, String dadDNAID, String dnaID, boolean affected, Gender gender) {
            this.momDNAID = (momDNAID == null || momDNAID.equals("")) ? null : momDNAID;
            this.dadDNAID = (dadDNAID == null || dadDNAID.equals("")) ? null : dadDNAID;
            this.dnaID = dnaID;
            this.affected = affected;
            this.gender = gender;
        }

        public String getMomDNAID() {
            return momDNAID;
        }

        public String getDadDNAID() {
            return dadDNAID;
        }

        public Gender getGender() {
            return gender;
        }

        public String getDnaID() {
            return dnaID;
        }

        public boolean isAffected() {
            return affected;
        }

        public boolean areBothParentsLinked() {
            return this.momDNAID != null && this.dadDNAID != null;
        }

        @Override
        public String toString() {
            return "SimplePerson{" + "momDNAID=" + momDNAID + ", dadDNAID=" + dadDNAID + ", dnaID=" + dnaID + '}';
        }

        private boolean atLeastOneParentIsLinked() {
            if (momDNAID != null || dadDNAID != null) {
                return true;
            }
            return false;
        }
    }

    private void writeVariantToSampleMap(Map<MendelVariant, Set<String>> map, File outFile) throws IOException {
        CSVWriter w = new CSVWriter(new FileWriter(outFile));

        for (MendelVariant v : map.keySet()) {
            String[] arr = new String[]{map.get(v).toString(), v.chr, v.start_pos + "", v.end_pos + "", v.ref, v.alt, v.type};
            w.writeNext(arr);
        }

        w.close();
    }

    // this is how it's encoded in the db, unknown = 0, male = 1, female = 2
    public static enum Gender {

        UNKNOWN, MALE, FEMALE
    };

    protected class SimplePatient implements Comparable {

        private String dnaID;
        private Zygosity zygosity;

        public SimplePatient(String dnaID, Zygosity zygosity) {//, Gender gender) {
            this.dnaID = dnaID;
            this.zygosity = zygosity;
        }

        public String getDnaID() {
            return dnaID;
        }

        public Zygosity getZygosity() {
            return zygosity;
        }

        @Override
        public String toString() {
            return dnaID + " (" + zygosity.toString() + ")";
        }

        @Override
        public int compareTo(Object t) {
            if (t instanceof SimplePatient) {
                return ((SimplePatient) t).getDnaID().compareTo(dnaID);
            } else {
                return -1;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.dnaID != null ? this.dnaID.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimplePatient other = (SimplePatient) obj;
            if ((this.dnaID == null) ? (other.dnaID != null) : !this.dnaID.equals(other.dnaID)) {
                return false;
            }
            return true;
        }
    }

    private TreeMap<MendelVariant, SimplePatientSet> readVariantToSampleMap(File f) throws FileNotFoundException, IOException {
        TreeMap<MendelVariant, SimplePatientSet> map = new TreeMap<MendelVariant, SimplePatientSet>();

        CSVReader r = new CSVReader(new FileReader(f), '\t', '\"');

        String[] line = new String[0];

        while ((line = readNext(r)) != null) {
            MendelVariant v = variantFromLine(line);

            String sample = line[0];

            Zygosity zygosity;
            // sometimes Zygosity is blank
            try {
                zygosity = Zygosity.valueOf(line[7]);
            } catch(Exception e) {
                zygosity = Zygosity.Missing;
            }
            

            SimplePatient pg = new SimplePatient(sample, zygosity);

            SimplePatientSet samples;
            if (map.containsKey(v)) {
                samples = map.get(v);
            } else {
                samples = new SimplePatientSet();
            }

            samples.add(pg);

            map.put(v, samples);
        }

        return map;
    }

    private String[] readNext(CSVReader recordReader) throws IOException {
        String[] line = recordReader.readNext();
        if (line == null) {
            return null;
        } else {
            line[line.length - 1] = removeNewLinesAndCarriageReturns(line[line.length - 1]);
        }

        return line;
    }

    private String removeNewLinesAndCarriageReturns(String next) {

        next = next.replaceAll("\n", "");
        next = next.replaceAll("\r", "");

        return next;
    }

    private void clear(String[] subsetLines) {
        for (int i = 0; i < subsetLines.length; i++) {
            subsetLines[i] = null;
        }
    }

    // line in format: "8243_0000","chr17","6115", "6115", "G","C","SNP","Hetero"
    private MendelVariant variantFromLine(String[] line) {
        return new MendelVariant(line[1], Integer.parseInt(line[2]), Integer.parseInt(line[3]), line[4], line[5], line[6]);
    }

    private int getFrequencyThresholdForCriterion(OptionView.IncludeExcludeCriteria criterion) throws SQLException, RemoteException {
        OptionView.IncludeExcludeCriteria.FrequencyType ft = criterion.getFrequencyType(); // all, no, some
        OptionView.IncludeExcludeCriteria.FrequencyCount fc = criterion.getFequencyCount(); // count, percent

        Set<String> dnaIDs = criterion.getDNAIDs();
        int frequencyThreshold = criterion.getFreqAmount();

        if (ft.equals(OptionView.IncludeExcludeCriteria.FrequencyType.ALL)) {
            frequencyThreshold = dnaIDs.size();
        } else if (ft.equals(OptionView.IncludeExcludeCriteria.FrequencyType.NO)) {
            frequencyThreshold = 0;
        } else if (fc.equals(OptionView.IncludeExcludeCriteria.FrequencyCount.Percent)) {
            frequencyThreshold = (int) Math.round(frequencyThreshold / 100.0 * dnaIDs.size());
        }

        return frequencyThreshold;
    }

    /**
     * Flags objects in map for removal if the occurrence in a given set of DNA
     * ids is either above, below, or equal to a frequency threshold
     *
     * @param map A map of object to sample ids relating to the object
     * @param frequencyThreshold The threshold for the cutoff
     * @param t Either ALL, NO, AT_LEAST, AT_MOST
     * @param setOfDNAIDs The set of DNA ids to consider
     * @return The set of objects to be removed
     */
    private Set<Object> flagObjectsForRemovalByCriterion(Map<Object, Set<SimplePatient>> map, int frequencyThreshold, OptionView.IncludeExcludeCriteria.FrequencyType t, Set<String> setOfDNAIDs) {
        Set<Object> removeThese = new HashSet<Object>();

        int removed = 0;

        for (Object o : map.keySet()) {

            // value contains ALL samples having object as key, need to assess
            // only for this cohort
            //int numberOfObjectsSamplesInCohort = 0;
            Set<String> patientsInCohortThatHaveIt = new HashSet<String>();
            try {
                for (SimplePatient s : map.get(o)) {
                    if (setOfDNAIDs.contains(s.getDnaID())) {
                        patientsInCohortThatHaveIt.add(s.getDnaID());
                        //numberOfObjectsSamplesInCohort++;
                    }
                }
            } catch (NullPointerException npe) {
                if (map.get(o) == null) {
                    System.out.println("No entry for object in map");
                }
                System.err.println(o);
                throw npe;
            }

            int numberOfObjectsSamplesInCohort = patientsInCohortThatHaveIt.size();

            if (t == OptionView.IncludeExcludeCriteria.FrequencyType.ALL || t == OptionView.IncludeExcludeCriteria.FrequencyType.NO) {
                if (numberOfObjectsSamplesInCohort != frequencyThreshold) {
                    //System.out.println("Removing " + o + " - " + numberOfObjectsSamplesInCohort + " NOT " + t + " " + frequencyThreshold);

                    removeThese.add(o);
                    removed++;
                    continue;
                }
            } else if (t == OptionView.IncludeExcludeCriteria.FrequencyType.AT_LEAST) {
                if (numberOfObjectsSamplesInCohort < frequencyThreshold) {
                    //System.out.println("Removing " + o + " - " + numberOfObjectsSamplesInCohort + " NOT " + t + " " + frequencyThreshold);

                    removeThese.add(o);
                    removed++;
                    continue;
                }
            } else if (t == OptionView.IncludeExcludeCriteria.FrequencyType.AT_MOST) {
                if (numberOfObjectsSamplesInCohort > frequencyThreshold) {
                    //System.out.println("Removing " + o + " - " + numberOfObjectsSamplesInCohort + " NOT " + t + " " + frequencyThreshold);

                    removeThese.add(o);
                    removed++;
                    continue;
                }
            }
        }

        System.out.println(removed + " items will be removed");

        return removeThese;
    }

    private Set<Object> flagObjectsForKeepsByCriterion(Map<Object, Set<SimplePatient>> map, int frequencyThreshold, OptionView.IncludeExcludeCriteria.FrequencyType t, Set<String> setOfDNAIDs) {
        Set<Object> keepThese = new HashSet<Object>();

        int kept = 0;

        for (Object o : map.keySet()) {

            if (o instanceof MendelGene) {
                MendelGene gene = (MendelGene) o;
                if (gene.name.equals("NOTCH2")) {
                    Set<SimplePatient> patientsWithNotch = map.get(gene);
                    System.out.println(patientsWithNotch.size() + " patients have NOTCH2");
                    System.out.print("\t");
                    for (SimplePatient p : patientsWithNotch) {
                        System.out.print(p + ", ");
                    }
                    System.out.println();
                }
            }

            // value contains ALL samples having object as key, need to assess
            // only for this cohort
            //int numberOfObjectsSamplesInCohort = 0;
            Set<String> patientsInCohortThatHaveIt = new HashSet<String>();
            try {
                for (SimplePatient s : map.get(o)) {
                    if (setOfDNAIDs.contains(s.getDnaID())) {
                        patientsInCohortThatHaveIt.add(s.dnaID);
                        //numberOfObjectsSamplesInCohort++;
                    }
                }
            } catch (NullPointerException npe) {
                if (map.get(o) == null) {
                    System.err.println("No entry for object in map");
                }
                System.err.println(o);
                throw npe;
            }

            int numberOfObjectsSamplesInCohort = patientsInCohortThatHaveIt.size();

            /*
             * DEBUG
             */
            if (o instanceof MendelGene) {
                MendelGene g = (MendelGene) o;
                if (g.name.equals("NOTCH2")) {
                    System.out.println(g + " has " + numberOfObjectsSamplesInCohort + " in cohort");
                }
            }

            if (t == OptionView.IncludeExcludeCriteria.FrequencyType.ALL || t == OptionView.IncludeExcludeCriteria.FrequencyType.NO) {
                if (numberOfObjectsSamplesInCohort != frequencyThreshold) {
                    continue;
                }
            } else if (t == OptionView.IncludeExcludeCriteria.FrequencyType.AT_LEAST) {
                if (numberOfObjectsSamplesInCohort < frequencyThreshold) {
                    continue;
                }
            } else if (t == OptionView.IncludeExcludeCriteria.FrequencyType.AT_MOST) {
                if (numberOfObjectsSamplesInCohort > frequencyThreshold) {
                    continue;
                }
            }

            keepThese.add(o);

            kept++;
        }

        System.out.println(kept + " items will be kept");

        return keepThese;
    }

    private boolean stepIncludesGeneCriterion(OptionView.IncludeExcludeStep step) {
        for (OptionView.IncludeExcludeCriteria c : step.getCriteria()) {
            if (c.getAggregationType() == OptionView.IncludeExcludeCriteria.AggregationType.Gene) {
                return true;
            }
        }
        return false;
    }

    /**
     * Constructs a map from gene to a set of patients (with genotype and other
     * information)
     *
     * @param variantToSampleMap The map from variant to patient set
     * @return The map from gene to patient set
     */
    private Map<MendelGene, SimplePatientSet> getGeneToSampleMap(Map<MendelVariant, SimplePatientSet> variantToSampleMap, Set<MendelGene> excludedGenes) {

        // create an empty map
        Map<MendelGene, SimplePatientSet> geneToSampleMap = new HashMap<MendelGene, SimplePatientSet>();

        // go through each variant, finding the intersecting gene, and adding to the map
        for (MendelVariant v : variantToSampleMap.keySet()) {

            // get genes intersecting this variant
            Set<MendelGene> genes = v.getGenes();
            Set<MendelGene> genesThatWereExcluded = new HashSet<MendelGene>();

            // go through each of these genes, and add the patient set from the variant map
            // to the gene map
            for (MendelGene g : genes) {

                if (excludedGenes.contains(g)) {
                    genesThatWereExcluded.add(g);
                    continue;
                }

                // get the samples that have this variant
                SimplePatientSet newSamples = variantToSampleMap.get(v);

                SimplePatientSet setFromMap;

                if (geneToSampleMap.containsKey(g)) {
                    setFromMap = geneToSampleMap.get(g);
                } else {
                    setFromMap = new SimplePatientSet();
                    geneToSampleMap.put(g, setFromMap);
                }

                // add patients to gene map
                for (SimplePatient p : newSamples) {
                    setFromMap.add(new SimplePatient(p.dnaID, p.zygosity));
                }
            }

            // remove association of excluded genes from variants
            for (MendelGene g : genesThatWereExcluded) {
                genes.remove(g);
            }
        }
        return geneToSampleMap;
    }

}
