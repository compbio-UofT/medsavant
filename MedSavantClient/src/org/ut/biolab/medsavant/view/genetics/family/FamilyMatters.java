package org.ut.biolab.medsavant.view.genetics.family;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broad.igv.feature.genome.Genome;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Cohort;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.GeneSet;
import org.ut.biolab.medsavant.model.SimplePatient;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.StripyTable;
import org.ut.biolab.medsavant.view.genetics.family.FamilyOperation.IncludeExcludeCriteria;
import org.ut.biolab.medsavant.view.genetics.family.FamilyOperation.IncludeExcludeCriteria.AggregationType;
import org.ut.biolab.medsavant.view.genetics.family.FamilyOperation.IncludeExcludeCriteria.FrequencyCount;
import org.ut.biolab.medsavant.view.genetics.family.FamilyOperation.IncludeExcludeCriteria.FrequencyType;
import org.ut.biolab.medsavant.view.genetics.family.FamilyOperation.IncludeExcludeStep;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import savant.api.data.PointRecord;
import savant.util.swing.DocumentViewer;

/**
 *
 * @author mfiume
 */
public class FamilyMatters {

    private static final Log LOG = LogFactory.getLog(FamilyMatters.class);
    private final List<IncludeExcludeStep> steps;
    private final File inFile;

    public FamilyMatters(File inFile, List<IncludeExcludeStep> steps) {

        this.steps = steps;
        this.inFile = inFile;

    }

    public void runSteps() {

        final JFrame frame = new JFrame("");
        JPanel panel = new JPanel();
        panel.setBorder(ViewUtil.getBigBorder());
        ViewUtil.applyVerticalBoxLayout(panel);
        final JLabel label = new JLabel("status");
        final JProgressBar progress = new JProgressBar();
        progress.setMinimum(0);
        progress.setMaximum(100);
        panel.add(ViewUtil.centerHorizontally(label));
        panel.add(Box.createHorizontalStrut(5));
        panel.add(progress);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
        label.setText("Parsing variants ...");

        new MedSavantWorker<Map<SimpleVariant, Set<String>>>(FamilyMatters.class.getCanonicalName()) {
            @Override
            protected void showProgress(double fract) {
            }

            @Override
            protected void showSuccess(Map<SimpleVariant, Set<String>> result) {
                // TODO: generate report

                //DocumentViewer.getInstance().addDocument(result.getAbsolutePath());
                //DocumentViewer.getInstance().setVisible(true);

                label.setText("Preparing results");

                Object[][] resultsArray = new Object[result.keySet().size()][7];
                int row = 0;

                List<SimpleVariant> variants = new ArrayList<SimpleVariant>(result.keySet());
                Collections.sort(variants);

                for (SimpleVariant v : variants) {
                    String geneStr = "";
                    for (SimpleGene g : v.genes) {
                        geneStr += g.name + ", ";
                    }
                    if (v.genes.size() > 0) {
                        geneStr = geneStr.substring(0, geneStr.length() - 2);

                        resultsArray[row++] = new Object[]{
                            v.chr,
                            v.pos,
                            v.ref,
                            v.alt,
                            v.type,
                            result.get(v).toString().replaceAll("\"", "").replaceFirst("[", "").replaceFirst("]", ""),
                            geneStr};
                    }
                }

                frame.setVisible(false);

                StripyTable t = new StripyTable(resultsArray, new String[]{"Chromosome", "Position", "Reference", "Alternate", "Type", "Samples", "Genes"}) {
                    public boolean isCellEditable(int rowIndex, int vColIndex) {
                        return false;
                    }
                };

                JDialog f = new JDialog();
                f.setModal(true);
                f.getContentPane()
                        .add(ViewUtil.getClearBorderlessScrollPane(t));
                f.setPreferredSize(
                        new Dimension(600, 600));
                f.setMinimumSize(
                        new Dimension(600, 600));
                f.setVisible(
                        true);

            }

            @Override
            protected Map<SimpleVariant, Set<String>> doInBackground() throws Exception {

                int stepNumber = 0;

                Map<SimpleVariant, Set<String>> variantToSampleMap = readVariantToSampleMap(inFile);
                LOG.info("Unique variants " + variantToSampleMap.keySet().size());

                File uniq = new File(inFile.getAbsolutePath() + ".uniq");
                LOG.info("Unique file is " + uniq.getAbsolutePath());
                writeVariantToSampleMap(variantToSampleMap, uniq);

                boolean variantsMappedToGenes = false;

                // always map variants to genes
                // TODO: for each genes, binary search range of variants, then assign gene to range
                mapVariantsToGenes(variantToSampleMap.keySet());

                // filter the map
                for (IncludeExcludeStep step : steps) {

                    Map<SimpleGene, Set<String>> geneToSampleMap = getGeneToSampleMap(variantToSampleMap);


                    // only map variants to genes if required
                    /*
                     * Map<SimpleGene, Set<String>> geneToSampleMap = null;

                    if (stepIncludesGeneCriterion(step)) {
                        if (true || !variantsMappedToGenes) {
                            mapVariantsToGenes(variantToSampleMap.keySet());
                            variantsMappedToGenes = true;
                        }
                        geneToSampleMap = getGeneToSampleMap(variantToSampleMap);
                    }
                    */

                    Set<SimpleVariant> allExcludedVariants = new HashSet<SimpleVariant>();

                    ++stepNumber;
                    System.out.println("Executing step " + stepNumber + " of " + steps.size());

                    int criteriaNumber = 0;

                    for (IncludeExcludeCriteria criterion : step.getCriteria()) {

                        ++criteriaNumber;

                        LOG.info("Executing criteria " + criteriaNumber + " of " + step.getCriteria().size() + " of step " + stepNumber);
                        LOG.info(criterion);

                        label.setText("Executing criteria " + criteriaNumber + " of " + step.getCriteria().size() + " of step " + stepNumber);

                        List<String> dnaIDsInCohort = MedSavantClient.CohortManager.getDNAIDsForCohort(LoginController.sessionId, criterion.getCohort().getId());
                        Set<String> setOfDNAIDs = new HashSet<String>(dnaIDsInCohort);


                        LOG.info("Samples in cohort " + criterion.getCohort().getName());
                        for (String sample : setOfDNAIDs) {
                            System.out.println(sample);
                        }

                        Set<SimpleVariant> excludedVariantsFromThisStep;
                        int frequencyThreshold = getFrequencyThresholdForCriterion(criterion);

                        LOG.info("Threshold is " + frequencyThreshold);

                        AggregationType at = criterion.getAggregationType();
                        if (at == AggregationType.Variant) {
                            LOG.info("Type is variant");
                            excludedVariantsFromThisStep = (Set<SimpleVariant>) ((Object) flagObjectsForRemovalByCriterion((Map<Object, Set<String>>) ((Object) variantToSampleMap), frequencyThreshold, criterion.getFequencyType(), setOfDNAIDs));
                        } else {
                            LOG.info("Type is gene");
                            Set<SimpleGene> toExclude = (Set<SimpleGene>) ((Object) flagObjectsForRemovalByCriterion((Map<Object, Set<String>>) ((Object) geneToSampleMap), frequencyThreshold, criterion.getFequencyType(), setOfDNAIDs));
                            excludedVariantsFromThisStep = new HashSet<SimpleVariant>();
                            for (SimpleGene g : toExclude) {
                                for (SimpleVariant v : g.getVariants()) {
                                    excludedVariantsFromThisStep.add(v);
                                }
                            }
                        }

                        LOG.info("Excluding " + excludedVariantsFromThisStep.size() + " variants from this step");
                        allExcludedVariants.addAll(excludedVariantsFromThisStep);
                    }

                    for (SimpleVariant v : allExcludedVariants) {
                        variantToSampleMap.remove(v);
                    }
                }

                // TODO: write output
                File outfile = new File(inFile.getAbsoluteFile() + ".fam");
                LOG.info("Writing to outfile " + outfile.getAbsolutePath());
                writeVariantToSampleMap(variantToSampleMap, outfile);
                LOG.info("Done writing to outfile " + outfile.getAbsolutePath());

                return variantToSampleMap;
            }

            private void writeVariantToSampleMap(Map<SimpleVariant, Set<String>> map, File outFile) throws IOException {
                CSVWriter w = new CSVWriter(new FileWriter(outFile));

                for (SimpleVariant v : map.keySet()) {
                    String[] arr = new String[]{map.get(v).toString(), v.chr, v.pos + "", v.ref, v.alt, v.type};
                    w.writeNext(arr);
                }

                w.close();
            }

            private Map<SimpleVariant, Set<String>> readVariantToSampleMap(File f) throws FileNotFoundException, IOException {
                Map<SimpleVariant, Set<String>> map = new HashMap<SimpleVariant, Set<String>>();

                CSVReader r = new CSVReader(new FileReader(f));

                String[] line = new String[0];

                while ((line = readNext(r)) != null) {
                    SimpleVariant v = variantFromLine(line);
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
            private SimpleVariant variantFromLine(String[] line) {
                return new SimpleVariant(line[1], Integer.parseInt(line[2]), line[3], line[4], line[5]);
            }

            private int getFrequencyThresholdForCriterion(IncludeExcludeCriteria criterion) throws SQLException, RemoteException {
                FrequencyType ft = criterion.getFequencyType(); // all, no, some
                FrequencyCount fc = criterion.getFequencyCount(); // count, percent
                Cohort c = criterion.getCohort();
                int frequencyThreshold = criterion.getFreqAmount();

                List<SimplePatient> patientsInCohort = MedSavantClient.CohortManager.getIndividualsInCohort(
                        LoginController.sessionId,
                        ProjectController.getInstance().getCurrentProjectID(),
                        c.getId());

                if (ft.equals(FrequencyType.ALL)) {
                    frequencyThreshold = patientsInCohort.size();
                } else if (ft.equals(FrequencyType.NO)) {
                    frequencyThreshold = 0;
                } else if (fc.equals(FrequencyCount.Percent)) {
                    frequencyThreshold = (int) Math.round(frequencyThreshold * patientsInCohort.size());
                }

                return frequencyThreshold;
            }

            private Set<Object> flagObjectsForRemovalByCriterion(Map<Object, Set<String>> map, int frequencyThreshold, FrequencyType t, Set<String> setOfDNAIDs) {
                Set<Object> removeThese = new HashSet<Object>();
                for (Object o : map.keySet()) {

                    // value contains ALL samples having object as key, need to assess
                    // only for this cohort
                    int numberOfObjectsSamplesInCohort = 0;
                    for (String s : map.get(o)) {
                        if (setOfDNAIDs.contains(s)) {
                            numberOfObjectsSamplesInCohort++;
                        }
                    }
                    //System.out.println(numberOfObjectsSamplesInCohort);

                    if (t == FrequencyType.ALL || t == FrequencyType.NO) {
                        if (numberOfObjectsSamplesInCohort != frequencyThreshold) {
                            removeThese.add(o);
                        }
                    } else if (t == FrequencyType.AT_LEAST) {
                        if (numberOfObjectsSamplesInCohort < frequencyThreshold) {
                            removeThese.add(o);
                        }
                    } else if (t == FrequencyType.AT_MOST) {
                        if (numberOfObjectsSamplesInCohort > frequencyThreshold) {
                            removeThese.add(o);
                        }
                    }
                }
                return removeThese;
            }

            private boolean stepIncludesGeneCriterion(IncludeExcludeStep step) {
                for (IncludeExcludeCriteria c : step.getCriteria()) {
                    if (c.getAggregationType() == AggregationType.Gene) {
                        return true;
                    }
                }
                return false;
            }

            private Map<SimpleGene, Set<String>> getGeneToSampleMap(Map<SimpleVariant, Set<String>> variantToSampleMap) {
                Map<SimpleGene, Set<String>> geneToSampleMap = new HashMap<SimpleGene, Set<String>>();
                for (SimpleVariant v : variantToSampleMap.keySet()) {
                    Set<SimpleGene> genes = v.getGenes();
                    for (SimpleGene g : genes) {
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

            private void mapVariantsToGenes(Set<SimpleVariant> variants) throws SQLException, RemoteException {
                Collection<Gene> genes = GeneSetController.getInstance().getCurrentGenes();

                for (SimpleVariant v : variants) {
                    for (Gene g : genes) {
                        if (v.chr.equals(g.getChrom()) && g.getStart() < v.pos && g.getEnd() > v.pos) {
                            v.addGene(new SimpleGene(g.getChrom(), g.getStart(), g.getEnd(), g.getName()));
                        }
                    }
                }
            }
        }
                .execute();



    }

    private static class SimpleGene {

        public final String chr;
        public final long start;
        public final long end;
        public final String name;
        private Set<SimpleVariant> variants;

        public SimpleGene(String chr, long start, long end, String name) {
            this.chr = chr;
            this.start = start;
            this.end = end;
            this.name = name;
            variants = new HashSet<SimpleVariant>();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 17 * hash + (this.chr != null ? this.chr.hashCode() : 0);
            hash = 17 * hash + (int) (this.start ^ (this.start >>> 32));
            hash = 17 * hash + (int) (this.end ^ (this.end >>> 32));
            hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
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
            final SimpleGene other = (SimpleGene) obj;
            if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
                return false;
            }
            if (this.start != other.start) {
                return false;
            }
            if (this.end != other.end) {
                return false;
            }
            if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        public void addVariant(SimpleVariant v) {
            variants.add(v);
        }

        private Set<SimpleVariant> getVariants() {
            return variants;
        }
    }

    private static class SimpleVariant implements Comparable {

        public final String chr;
        public final long pos;
        public final String ref;
        public final String alt;
        public final String type;
        private Set<SimpleGene> genes;

        public SimpleVariant(String chr, long pos, String ref, String alt, String type) {
            this.chr = chr;
            this.pos = pos;
            this.ref = ref;
            this.alt = alt;
            this.type = type;
            this.genes = new HashSet<SimpleGene>();
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.chr != null ? this.chr.hashCode() : 0);
            hash = 59 * hash + (int) (this.pos ^ (this.pos >>> 32));
            hash = 59 * hash + (this.ref != null ? this.ref.hashCode() : 0);
            hash = 59 * hash + (this.alt != null ? this.alt.hashCode() : 0);
            hash = 59 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }

        public Set<SimpleGene> getGenes() {
            return genes;
        }

        public void addGene(SimpleGene gene) {
            this.genes.add(gene);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final SimpleVariant other = (SimpleVariant) obj;
            if ((this.chr == null) ? (other.chr != null) : !this.chr.equals(other.chr)) {
                return false;
            }
            if (this.pos != other.pos) {
                return false;
            }
            if ((this.ref == null) ? (other.ref != null) : !this.ref.equals(other.ref)) {
                return false;
            }
            if ((this.alt == null) ? (other.alt != null) : !this.alt.equals(other.alt)) {
                return false;
            }
            if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Object o) {
            if (!(o instanceof SimpleVariant)) {
                return -1;
            }
            SimpleVariant other = (SimpleVariant) o;
            int chromCompare = (new Genome.ChromosomeComparator()).compare(this.chr, other.chr);
            if (chromCompare != 0) {
                return chromCompare;
            }
            return ((Long) this.pos).compareTo(other.pos);
        }
    }
}
