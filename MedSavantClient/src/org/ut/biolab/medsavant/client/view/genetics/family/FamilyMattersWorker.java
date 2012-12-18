package org.ut.biolab.medsavant.client.view.genetics.family;

import org.ut.biolab.medsavant.client.view.Notification;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broad.tabix.TabixWriter;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.geneset.GeneSetController;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Cohort;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.SimplePatient;
import org.ut.biolab.medsavant.client.project.ProjectController;
import org.ut.biolab.medsavant.shared.util.ChromosomeComparator;
import org.ut.biolab.medsavant.client.util.DataRetriever;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.client.view.component.StripyTable;
import org.ut.biolab.medsavant.client.view.genetics.family.FamilyMattersOptionView.IncludeExcludeStep;
import org.ut.biolab.medsavant.client.view.genetics.inspector.ComprehensiveInspector;
import org.ut.biolab.medsavant.client.view.genetics.variantinfo.SimpleVariant;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public class FamilyMattersWorker extends MedSavantWorker<TreeMap<SimpleFamilyMattersVariant, Set<String>>> {

    private static final Log LOG = LogFactory.getLog(FamilyMattersWorker.class);
    private final List<IncludeExcludeStep> steps;
    private final File inFile;
    //private JProgressBar progressBar;
    //private JFrame frame;
    //private JLabel label;
    private Notification jobModel;
    private Locks.DialogLock completionLock;

    public FamilyMattersWorker(List<IncludeExcludeStep> steps, File inFile) {
        super(FamilyMattersPage.PAGE_NAME);
        this.steps = steps;
        this.inFile = inFile;
    }

    public void setUIComponents(Notification m) {
        this.jobModel = m;
    }

    public void setLabelText(String t) {
        this.jobModel.setStatusMessage(t);
    }

    @Override
    protected void showProgress(double fract) {
        this.jobModel.setProgress(fract);
    }

    @Override
    protected void showSuccess(final TreeMap<SimpleFamilyMattersVariant, Set<String>> result) {

        setLabelText("Preparing results");

        String pageName = FamilyMattersPage.PAGE_NAME;
        String[] columnNames = new String[]{"Chromosome", "Position", "Reference", "Alternate", "Type", "Samples", "Genes"};
        Class[] columnClasses = new Class[]{String.class, String.class, String.class, String.class, String.class, String.class, String.class};

        final List<SimpleFamilyMattersVariant> variants = new ArrayList<SimpleFamilyMattersVariant>(result.keySet());

        DataRetriever<Object[]> retriever = new DataRetriever<Object[]>() {
            @Override
            public List<Object[]> retrieve(int start, int limit) throws Exception {


                List<Object[]> rows = new ArrayList<Object[]>();

                for (int i = 0; i < limit && (i + start) < variants.size(); i++) {


                    SimpleFamilyMattersVariant v = variants.get(i + start);
                    if (i == 0) {
                        System.out.println(v);
                    }

                    String geneStr = "";
                    for (SimpleFamilyMattersGene g : v.getGenes()) {
                        geneStr += g.name + ", ";
                    }
                    if (v.getGenes().size() > 0) {
                        geneStr = geneStr.substring(0, geneStr.length() - 2);

                        rows.add(new Object[]{
                                    v.chr,
                                    v.pos,
                                    v.ref,
                                    v.alt,
                                    v.type,
                                    result.get(v).toString(),//.replaceAll("\"", ""),//.replaceFirst("\[", "").replaceFirst("\]", ""),
                                    geneStr});
                    }
                }

                return rows;
            }

            @Override
            public int getTotalNum() {
                LOG.info("Number of results " + result.keySet().size());
                return result.keySet().size();
            }

            @Override
            public void retrievalComplete() {
            }
        };

        final SearchableTablePanel stp =
                new SearchableTablePanel(
                pageName,
                columnNames,
                columnClasses,
                new int[]{},
                true,
                false,
                100,
                true,
                SearchableTablePanel.TableSelectionType.ROW,
                1000,
                retriever);

        final ComprehensiveInspector vip = new ComprehensiveInspector(true,false,false,true,true,true);

        stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

                LOG.info("Value changed " + e.getSource());

                if (e.getValueIsAdjusting()) {
                    return;
                }

                int first = stp.getActualRowAcrossAllPages(e.getLastIndex());

                SimpleFamilyMattersVariant fmv = variants.get(first);
                SimpleVariant v = new SimpleVariant(fmv.chr, fmv.pos, fmv.ref, fmv.alt, fmv.type);
                vip.setSimpleVariant(v);
            }
        });

        JDialog f = new JDialog();
        f.setTitle("Cohort Analysis Results");

        LOG.info("Showing table of results");

        JPanel aligned = new JPanel();
        aligned.setLayout(new BorderLayout());
        aligned.setBorder(null);
        aligned.setPreferredSize(new Dimension(450, 999));
        aligned.setBackground(Color.white);
        aligned.add(vip,BorderLayout.CENTER);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(stp, BorderLayout.CENTER);
        p.add(aligned, BorderLayout.EAST);

        f.add(p);

        stp.forceRefreshData();
        f.setPreferredSize(new Dimension(600, 600));
        f.setMinimumSize(new Dimension(600, 600));

        if (completionLock != null) {
            synchronized (completionLock) {
                completionLock.setResultsFrame(f);
                completionLock.notify();
            }
        }
    }

    void setCompletionLock(Locks.DialogLock lock) {
        this.completionLock = lock;
    }

    private static class VariantGeneComparator implements Comparator {

        private final boolean useGeneStart;
        private static final ChromosomeComparator cc = new ChromosomeComparator();

        public VariantGeneComparator(boolean useGeneStart) {
            this.useGeneStart = useGeneStart;
        }

        @Override
        public int compare(Object v, Object g) {
            if (v instanceof SimpleFamilyMattersVariant && g instanceof SimpleFamilyMattersGene) {

                SimpleFamilyMattersVariant v0 = (SimpleFamilyMattersVariant) v;
                SimpleFamilyMattersGene g0 = (SimpleFamilyMattersGene) g;
                int c = cc.compare(v0.chr, g0.chr);
                if (c == 0) {
                    if (useGeneStart) {
                        return v0.pos < g0.start ? -1 : v0.pos == g0.start ? 0 : 1;
                    } else {
                        return v0.pos < g0.end ? -1 : v0.pos == g0.end ? 0 : 1;
                    }
                } else {
                    return c;
                }
            } else {
                LOG.error("Cannot compare given objects");
                return 0;
            }
        }
    };

    @Override
    protected TreeMap<SimpleFamilyMattersVariant, Set<String>> doInBackground() throws Exception {

        setLabelText("Parsing variants");

        int stepNumber = 0;

        /**
         * Map variants to samples
         * NB: keys in a TreeMap are sorted
         */
        TreeMap<SimpleFamilyMattersVariant, Set<String>> variantToSampleMap = readVariantToSampleMap(inFile);

        LOG.info("Unique variants " + variantToSampleMap.keySet().size());

        for (SimpleFamilyMattersVariant v : variantToSampleMap.keySet()) {
            if (variantToSampleMap.get(v) == null) {
                throw new Exception("nwtf for " + v);
            }
        }

        List<SimpleFamilyMattersVariant> variants = new ArrayList<SimpleFamilyMattersVariant>(variantToSampleMap.keySet());

        this.setLabelText("Sorting genes");
        Collection<Gene> msGenes = GeneSetController.getInstance().getCurrentGenes();
        List<SimpleFamilyMattersGene> genes = new ArrayList<SimpleFamilyMattersGene>();
        for (Gene g : msGenes) {
            genes.add(new SimpleFamilyMattersGene(g.getChrom(), g.getStart(), g.getCodingEnd(), g.getName()));
        }
        Collections.sort(genes);
        //this.setLabelText("Done sorting genes");

        Comparator startComparator = new VariantGeneComparator(true);
        Comparator endComparator = new VariantGeneComparator(false);

        int zeroIndex = variants.size();

        TreeMap<SimpleFamilyMattersGene, Set<SimpleFamilyMattersVariant>> genesToVariantsMap = new TreeMap<SimpleFamilyMattersGene, Set<SimpleFamilyMattersVariant>>();

        this.setLabelText("Mapping variants to genes");
        for (SimpleFamilyMattersGene g : genes) {
            int s = Collections.binarySearch(variants, g, startComparator);
            int e = Collections.binarySearch(variants, g, endComparator);

            if (s == zeroIndex) {
                s = 0;
            }
            if (e == zeroIndex) {
                e = 0;
            }

            if (s < 0) {
                s = s * -1;
            }
            if (e < 0) {
                e = e * -1;
            }

            if (s > variants.size()) {
                continue;
            }
            if (e > variants.size()) {
                e = variants.size();
            }

            Set<SimpleFamilyMattersVariant> variantsMappingToGene = new HashSet<SimpleFamilyMattersVariant>();

            for (int i = s; i < e; i++) {
                SimpleFamilyMattersVariant v = variants.get(i);
                v.addGene(g);
                variantsMappingToGene.add(v);
            }

            genesToVariantsMap.put(g, variantsMappingToGene);
        }

        for (FamilyMattersOptionView.IncludeExcludeStep step : steps) {

            ++stepNumber;

            LOG.info("Getting gene to sample map");
            Map<SimpleFamilyMattersGene, Set<String>> geneToSampleMap = getGeneToSampleMap(variantToSampleMap);

            Set<SimpleFamilyMattersVariant> allExcludedVariants = new HashSet<SimpleFamilyMattersVariant>();

            int criteriaNumber = 0;

            for (FamilyMattersOptionView.IncludeExcludeCriteria criterion : step.getCriteria()) {

                ++criteriaNumber;

                LOG.info("Executing criteria #" + criteriaNumber + " of " + step.getCriteria().size() + " of step #" + stepNumber);
                LOG.info(criterion);

                setLabelText("Executing criteria #" + criteriaNumber + " of " + step.getCriteria().size() + " of step #" + stepNumber);

                List<String> dnaIDsInCohort = MedSavantClient.CohortManager.getDNAIDsForCohort(LoginController.getInstance().getSessionID(), criterion.getCohort().getId());
                Set<String> setOfDNAIDs = new HashSet<String>(dnaIDsInCohort);

                Set<SimpleFamilyMattersVariant> excludedVariantsFromThisStep;
                int frequencyThreshold = getFrequencyThresholdForCriterion(criterion);

                LOG.info("Threshold is " + frequencyThreshold);
                LOG.info("Threshold type is " + criterion.getFrequencyType());

                FamilyMattersOptionView.IncludeExcludeCriteria.AggregationType at = criterion.getAggregationType();
                if (at == FamilyMattersOptionView.IncludeExcludeCriteria.AggregationType.Variant) {
                    LOG.info("Type is variant");
                    excludedVariantsFromThisStep = (Set<SimpleFamilyMattersVariant>) ((Object) flagObjectsForRemovalByCriterion((Map<Object, Set<String>>) ((Object) variantToSampleMap), frequencyThreshold, criterion.getFrequencyType(), setOfDNAIDs));
                } else {
                    LOG.info("Type is gene");

                    LOG.info("Size of gene map is " + geneToSampleMap.keySet().size());

                    Set<SimpleFamilyMattersGene> genesToRemove = (Set<SimpleFamilyMattersGene>) ((Object) flagObjectsForRemovalByCriterion((Map<Object, Set<String>>) ((Object) geneToSampleMap), frequencyThreshold, criterion.getFrequencyType(), setOfDNAIDs));

                    LOG.info("Excluding " + genesToRemove.size() + " genes");

                    excludedVariantsFromThisStep = new HashSet<SimpleFamilyMattersVariant>();
                    for (SimpleFamilyMattersGene g : genesToRemove) {
                        Set<SimpleFamilyMattersVariant> variantsToExclude = genesToVariantsMap.get(g);
                        excludedVariantsFromThisStep.addAll(variantsToExclude);
                    }
                }

                LOG.info("Excluding " + excludedVariantsFromThisStep.size() + " variants from this step");
                allExcludedVariants.addAll(excludedVariantsFromThisStep);
            }

            for (SimpleFamilyMattersVariant v : allExcludedVariants) {
                variantToSampleMap.remove(v);
            }
        }

        return variantToSampleMap;
    }

    private void writeVariantToSampleMap(Map<SimpleFamilyMattersVariant, Set<String>> map, File outFile) throws IOException {
        CSVWriter w = new CSVWriter(new FileWriter(outFile));

        for (SimpleFamilyMattersVariant v : map.keySet()) {
            String[] arr = new String[]{map.get(v).toString(), v.chr, v.pos + "", v.ref, v.alt, v.type};
            w.writeNext(arr);
        }

        w.close();
    }

    private TreeMap<SimpleFamilyMattersVariant, Set<String>> readVariantToSampleMap(File f) throws FileNotFoundException, IOException {
        TreeMap<SimpleFamilyMattersVariant, Set<String>> map = new TreeMap<SimpleFamilyMattersVariant, Set<String>>();

        CSVReader r = new CSVReader(new FileReader(f));

        String[] line = new String[0];

        while ((line = readNext(r)) != null) {
            SimpleFamilyMattersVariant v = variantFromLine(line);

            String sample = line[0];

            Set<String> samples;
            if (map.containsKey(v)) {
                samples = map.get(v);
            } else {
                samples = new HashSet<String>();
            }
            samples.add(sample);
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

    // line in format: "8243_0000","chr17","6115","G","C","SNP","Hetero"
    private SimpleFamilyMattersVariant variantFromLine(String[] line) {
        return new SimpleFamilyMattersVariant(line[1], Integer.parseInt(line[2]), line[3], line[4], line[5]);
    }

    private int getFrequencyThresholdForCriterion(FamilyMattersOptionView.IncludeExcludeCriteria criterion) throws SQLException, RemoteException {
        FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType ft = criterion.getFrequencyType(); // all, no, some
        FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyCount fc = criterion.getFequencyCount(); // count, percent
        Cohort c = criterion.getCohort();
        int frequencyThreshold = criterion.getFreqAmount();

        List<SimplePatient> patientsInCohort = MedSavantClient.CohortManager.getIndividualsInCohort(
                LoginController.getInstance().getSessionID(),
                ProjectController.getInstance().getCurrentProjectID(),
                c.getId());

        if (ft.equals(FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.ALL)) {
            frequencyThreshold = patientsInCohort.size();
        } else if (ft.equals(FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.NO)) {
            frequencyThreshold = 0;
        } else if (fc.equals(FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyCount.Percent)) {
            frequencyThreshold = (int) Math.round(frequencyThreshold/100.0 * patientsInCohort.size());
        }

        return frequencyThreshold;
    }

    /**
     * Flags objects in map for removal if the occurrence in a given set
     * of DNA ids is either above, below, or equal to a frequency threshold
     * @param map A map of object to sample ids relating to the object
     * @param frequencyThreshold The threshold for the cutoff
     * @param t Either ALL, NO, AT_LEAST, AT_MOST
     * @param setOfDNAIDs The set of DNA ids to consider
     * @return The set of objects to be removed
     */
    private Set<Object> flagObjectsForRemovalByCriterion(Map<Object, Set<String>> map, int frequencyThreshold, FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType t, Set<String> setOfDNAIDs) {
        Set<Object> removeThese = new HashSet<Object>();
        for (Object o : map.keySet()) {

            // value contains ALL samples having object as key, need to assess
            // only for this cohort
            int numberOfObjectsSamplesInCohort = 0;
            try {
                for (String s : map.get(o)) {
                    if (setOfDNAIDs.contains(s)) {
                        numberOfObjectsSamplesInCohort++;
                    }
                }
            } catch (NullPointerException npe) {
                if (map.get(o) == null) { System.out.println("No entry for object in map"); }
                System.err.println(o);
                throw npe;
            }

            if (t == FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.ALL || t == FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.NO) {
                if (numberOfObjectsSamplesInCohort != frequencyThreshold) {
                    removeThese.add(o);
                }
            } else if (t == FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.AT_LEAST) {
                if (numberOfObjectsSamplesInCohort < frequencyThreshold) {
                    removeThese.add(o);
                }
            } else if (t == FamilyMattersOptionView.IncludeExcludeCriteria.FrequencyType.AT_MOST) {
                if (numberOfObjectsSamplesInCohort > frequencyThreshold) {
                    removeThese.add(o);
                }
            }
        }
        return removeThese;
    }

    private boolean stepIncludesGeneCriterion(FamilyMattersOptionView.IncludeExcludeStep step) {
        for (FamilyMattersOptionView.IncludeExcludeCriteria c : step.getCriteria()) {
            if (c.getAggregationType() == FamilyMattersOptionView.IncludeExcludeCriteria.AggregationType.Gene) {
                return true;
            }
        }
        return false;
    }

    private Map<SimpleFamilyMattersGene, Set<String>> getGeneToSampleMap(Map<SimpleFamilyMattersVariant, Set<String>> variantToSampleMap) {
        Map<SimpleFamilyMattersGene, Set<String>> geneToSampleMap = new HashMap<SimpleFamilyMattersGene, Set<String>>();
        for (SimpleFamilyMattersVariant v : variantToSampleMap.keySet()) {
            Set<SimpleFamilyMattersGene> genes = v.getGenes();
            for (SimpleFamilyMattersGene g : genes) {
                Set<String> newSamples = variantToSampleMap.get(v);
                if (geneToSampleMap.containsKey(g)) {
                    geneToSampleMap.get(g).addAll(newSamples);
                } else {
                    geneToSampleMap.put(g, newSamples);
                }
            }
        }
        return geneToSampleMap;
    }

    private Map<SimpleFamilyMattersGene, Set<SimpleFamilyMattersVariant>> mapVariantsToGenesAndViceVersa(Set<SimpleFamilyMattersVariant> variants) throws SQLException, RemoteException {
        Collection<Gene> genes = GeneSetController.getInstance().getCurrentGenes();
        Map<SimpleFamilyMattersGene, Set<SimpleFamilyMattersVariant>> genesToVariantsMap = new HashMap<SimpleFamilyMattersGene, Set<SimpleFamilyMattersVariant>>();

        for (SimpleFamilyMattersVariant v : variants) {
            for (Gene g : genes) {
                if (v.chr.equals(g.getChrom()) && g.getStart() < v.pos && g.getEnd() > v.pos) {
                    SimpleFamilyMattersGene sg = new SimpleFamilyMattersGene(g.getChrom(), g.getStart(), g.getEnd(), g.getName());
                    v.addGene(sg);

                    Set<SimpleFamilyMattersVariant> variantSet;
                    if (genesToVariantsMap.containsKey(sg)) {
                        variantSet = genesToVariantsMap.get(sg);
                    } else {
                        variantSet = new HashSet<SimpleFamilyMattersVariant>();
                    }
                    variantSet.add(v);
                    genesToVariantsMap.put(sg, variantSet);
                }
            }
        }

        return genesToVariantsMap;
    }

    private int binarySearchToVariant(long end, List<SimpleFamilyMattersVariant> variants) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
