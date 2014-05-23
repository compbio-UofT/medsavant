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
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

//import cytoscape.CytoscapeVersion;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.genemania.domain.*;
import org.genemania.dto.*;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.exception.ApplicationException;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.*;
import org.genemania.plugin.cytoscape.NullCytoscapeUtils;
import org.genemania.plugin.cytoscape2.CompatibilityImpl;
import org.genemania.plugin.cytoscape2.support.Compatibility;
import org.genemania.plugin.cytoscape26.Cy26Compatibility;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSetFactory;
import org.genemania.plugin.model.SearchResult;
import org.genemania.type.CombiningMethod;
import org.genemania.util.NullProgressReporter;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.view.MedSavantFrame;
import org.ut.biolab.medsavant.shared.util.WebResources;
import org.xml.sax.SAXException;

/**
 *
 * @author khushi
 */
public class GenemaniaInfoRetriever {
/*
    private Compatibility createCompatibility(CytoscapeVersion cytoscapeVersion) {
        String[] parts = cytoscapeVersion.getMajorVersion().split("[.]");
        if (!parts[0].equals("2")) {
            throw new RuntimeException("This plugin is only compatible with Cytoscape 2.X");
        }

        int minorVersion = Integer.parseInt(parts[1]);
        if (minorVersion < 8) {
            return new Cy26Compatibility();
        }
        return new CompatibilityImpl();
    }
*/
    public static class NoRelatedGenesInfoException extends Exception {

        public NoRelatedGenesInfoException() {
            super("No network information found for this gene(s) in GeneMANIA");
        }
    }
    private List<String> genes;
    private static final int DEFAULT_GENE_LIMIT = 50;
    private static final CombiningMethod DEFAULT_COMBINING_METHOD = CombiningMethod.AVERAGE;
    private static final String[] DEFAULT_NETWORKS = {"Genetic interactions", "Shared protein domains", "Other", "Pathway", "Physical interactions", "Co-localization", "Predicted", "Co-expression"};
    private int geneLimit;
    private CombiningMethod combiningMethod;
    private Map<InteractionNetworkGroup, Collection<InteractionNetwork>> networks;
    private DataSetManager dataSetManager;
    private static DataSet data;
    private static Organism human;
    private Mania2 mania;
    private DataCache cache;
    private NetworkUtils networkUtils;
    private static final int MIN_CATEGORIES = 10;
    private static final double Q_VALUE_THRESHOLD = 0.1;
    private SearchResult options;
    private static Map<Long, Integer> sequenceNumbers;
    private RelatedGenesEngineResponseDto response;
    //private static String GM_URL = "http://compbio.cs.toronto.edu/savant/data/dropbox/genemania/gmdata.zip";
    private static final Log LOG = LogFactory.getLog(GenemaniaInfoRetriever.class);
    private static GeneManiaDownloadTask geneManiaDownloadTask;

    static {
        sequenceNumbers = new HashMap<Long, Integer>();
    }

    public static void extractGM(String pathToGMData) {
        String directoryPath = DirectorySettings.getCacheDirectory().getAbsolutePath();
        try {
            File data = new File(pathToGMData);
            ZipFile zipData = new ZipFile(data.getAbsolutePath());
            Enumeration entries = zipData.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    (new File(directoryPath + File.separator + entry.getName())).mkdirs();
                    continue;
                }

                IOUtils.copy(zipData.getInputStream(entry),
                        new FileOutputStream(directoryPath + File.separator + entry.getName()));
            }
            zipData.close();
            FileWriter fstream = new FileWriter(DirectorySettings.getGeneManiaDirectory() + File.separator + DirectorySettings.GENEMANIA_CHECK_FILE);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("This file indicates that the GeneMANIA data has finished downloading.");
            out.close();
            if (!data.delete()) {
                LOG.error("Couldn't delete GeneMANIA .zip: " + data.getAbsolutePath());
            }

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(GenemaniaInfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static class GeneManiaDownloadTask extends DownloadTask {

        public GeneManiaDownloadTask(String url, String dstPath, String msg) throws IOException {
            super(url, dstPath, msg);
        }

        @Override
        protected Void runInBackground() {
            super.runInBackground();
            if (isCancelled()) {
                return null;
            }
            setStatusMessage("Extracting GeneMANIA files...");
            GenemaniaInfoRetriever.extractGM(getDestPath());
            return null;
        }

        @Override
        public void jobDone() {
            super.jobDone();
            MedSavantFrame.getInstance().showNotficationMessage("GeneMANIA has finished downloading, and is ready to use.");
        }
    }

    public static synchronized boolean isGeneManiaDownloading() {
        boolean b = geneManiaDownloadTask != null && (!geneManiaDownloadTask.isDone()) && (!geneManiaDownloadTask.isCancelled());
        return b;
    }

    public static synchronized DownloadTask getGeneManiaDownloadTask() throws IOException {

        if (geneManiaDownloadTask == null || geneManiaDownloadTask.isCancelled()) {
            String dstPath = DirectorySettings.getCacheDirectory().getAbsolutePath();
            geneManiaDownloadTask = new GeneManiaDownloadTask(WebResources.GENEMANIA_DATA_URL.toString(), dstPath, "Downloading GeneMANIA...");
        }
        return geneManiaDownloadTask;
    }

    public GenemaniaInfoRetriever() throws IOException {
        if (DirectorySettings.isGeneManiaInstalled()) {
            initialize();
        } else {
            throw new IOException("GeneMANIA data not found, please download it first.");
        }
    }

    //returns invalid genes that were not set
    public void setGenes(List<String> geneNames) {
        try {
            this.genes = getValidGenes(geneNames);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(GenemaniaInfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<String> getGenes() {
        return genes;
    }

    public void setGeneLimit(int geneLimit) {
        this.geneLimit = geneLimit;
    }

    public void setCombiningMethod(CombiningMethod cm) {
        this.combiningMethod = cm;
    }

    public List<String> getRelatedGeneNamesByScore() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException {
        List<String> geneNames = new ArrayList<String>();
        Iterator<Gene> itr = getRelatedGenesByScore().iterator();
        while (itr.hasNext()) {
            geneNames.add(itr.next().getSymbol());
        }
        return geneNames;
    }

    public List<Gene> getRelatedGenesByScore() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException {
        options = runGeneManiaAlgorithm();
        final Map<Gene, Double> scores = options.getScores();
        ArrayList<Gene> relatedGenes = new ArrayList<Gene>(scores.keySet());

        Collections.sort(relatedGenes, new Comparator<Gene>() {
            public int compare(Gene gene1, Gene gene2) {
                return -Double.compare(scores.get(gene1), scores.get(gene2));
            }
        });
        return relatedGenes;
    }

    public NetworkUtils getNetworkUtils() {
        return networkUtils;
    }

    private Map<Long, Double> filterGeneScores(Map<Long, Double> scores, SearchResult options) {
        Map<Long, Gene> queryGenes = options.getQueryGenes();
        double maxScore = 0;
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            if (queryGenes.containsKey(entry.getKey())) {
                continue;
            }
            maxScore = Math.max(maxScore, entry.getValue());
        }

        Map<Long, Double> filtered = new HashMap<Long, Double>();
        for (Map.Entry<Long, Double> entry : scores.entrySet()) {
            long nodeId = entry.getKey();
            double score = entry.getValue();
            filtered.put(entry.getKey(), queryGenes.containsKey(nodeId) ? maxScore : score);
        }
        return filtered;
    }

    private double[] computeEdgeWeightExtrema(RelatedGenesEngineResponseDto response) {
        double[] extrema = new double[]{1, 0};
        for (NetworkDto network : response.getNetworks()) {
            for (InteractionDto interaction : network.getInteractions()) {
                double weight = interaction.getWeight() * network.getWeight();
                if (extrema[0] > weight) {
                    extrema[0] = weight;
                }
                if (extrema[1] < weight) {
                    extrema[1] = weight;
                }
            }
        }
        return extrema;
    }

    private SearchResult runGeneManiaAlgorithm() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException {

        RelatedGenesEngineRequestDto request = createRequest();
        response = runQuery(request);

        EnrichmentEngineRequestDto enrichmentRequest = createEnrichmentRequest(response);
        EnrichmentEngineResponseDto enrichmentResponse = computeEnrichment(enrichmentRequest);

        SearchResult options = networkUtils.createSearchOptions(human, request, response, enrichmentResponse, data, genes);
        return options;
    }

    private EnrichmentEngineRequestDto createEnrichmentRequest(RelatedGenesEngineResponseDto response) {
        if (human.getOntology() == null) {
            return null;
        }
        EnrichmentEngineRequestDto request = new EnrichmentEngineRequestDto();
        request.setProgressReporter(NullProgressReporter.instance());
        request.setMinCategories(MIN_CATEGORIES);
        request.setqValueThreshold(Q_VALUE_THRESHOLD);

        request.setOrganismId(human.getId());
        request.setOntologyId(human.getOntology().getId());

        Set<Long> nodes = new HashSet<Long>();
        for (NetworkDto network : response.getNetworks()) {
            for (InteractionDto interaction : network.getInteractions()) {
                nodes.add(interaction.getNodeVO1().getId());
                nodes.add(interaction.getNodeVO2().getId());
            }
        }
        request.setNodes(nodes);
        return request;
    }

    private EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request) throws ApplicationException, NoRelatedGenesInfoException {
        if (request == null) {
            return null;
        }
        if (request.getNodes().size() == 0) {
            throw new NoRelatedGenesInfoException();
        }
        return mania.computeEnrichment(request);
    }

    private RelatedGenesEngineRequestDto createRequest() throws ApplicationException {
        RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
        request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
        request.setOrganismId(human.getId());
        request.setInteractionNetworks(collapseNetworks(networks));
        Set<Long> nodes = new HashSet<Long>();
        for (String geneName : genes) {
            nodes.add(data.getCompletionProvider(human).getNodeId(geneName));
        }
        request.setPositiveNodes(nodes);
        request.setLimitResults(geneLimit);
        request.setCombiningMethod(combiningMethod);
        request.setScoringMethod(org.genemania.type.ScoringMethod.DISCRIMINANT);
        return request;
    }

    private RelatedGenesEngineResponseDto runQuery(RelatedGenesEngineRequestDto request) throws DataStoreException {
        try {
            request.setProgressReporter(NullProgressReporter.instance());
            RelatedGenesEngineResponseDto result;
            result = mania.findRelated(request);
            request.setCombiningMethod(result.getCombiningMethodApplied());
            networkUtils.normalizeNetworkWeights(result);
            return result;
        } catch (ApplicationException e) {
            Logger logger = Logger.getLogger(getClass());
            logger.error("Unexpected error", e); //$NON-NLS-1$                        
            return null;
        }
    }

    private Collection<Collection<Long>> collapseNetworks(Map<InteractionNetworkGroup, Collection<InteractionNetwork>> networks) {
        Collection<Collection<Long>> result = new ArrayList<Collection<Long>>();
        for (Map.Entry<InteractionNetworkGroup, Collection<InteractionNetwork>> entry : networks.entrySet()) {
            Collection<Long> groupMembers = new HashSet<Long>();
            for (InteractionNetwork network : entry.getValue()) {
                groupMembers.add(network.getId());
            }
            if (!groupMembers.isEmpty()) {
                result.add(groupMembers);
            }
        }
        return result;
    }

    private void initialize() {
        try {
            dataSetManager = new DataSetManager();
            dataSetManager.addDataSetFactory(new LuceneDataSetFactory<Object, Object, Object>(dataSetManager, null, new FileUtils(), new NullCytoscapeUtils<Object, Object, Object>(), null), Collections.emptyMap());
            data = dataSetManager.open(DirectorySettings.getGeneManiaDirectory());

            human = getHumanOrganism(data);
            networkUtils = new NetworkUtils();

            cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
            mania = new Mania2(cache);
            setGeneLimit(DEFAULT_GENE_LIMIT);
            setCombiningMethod(DEFAULT_COMBINING_METHOD);
            setNetworks(new HashSet<String>(Arrays.asList(DEFAULT_NETWORKS)));
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void setNetworks(Set<String> n) {
        Map<InteractionNetworkGroup, Collection<InteractionNetwork>> groupMembers = new HashMap<InteractionNetworkGroup, Collection<InteractionNetwork>>();
        Collection<InteractionNetworkGroup> groups = human.getInteractionNetworkGroups();
        Set<String> notHandled = n;
        for (InteractionNetworkGroup group : groups) {
            if (n.contains(group.getName())) {
                notHandled.remove(group.getName());
                List<InteractionNetwork> networkMembers = new ArrayList<InteractionNetwork>();
                Collection<InteractionNetwork> networks = group.getInteractionNetworks();
                for (InteractionNetwork network : networks) {

                    networkMembers.add(network);

                }
                if (networkMembers.size() > 0) {
                    groupMembers.put(group, networkMembers);
                }

            }
        }
        networks = groupMembers;
    }

    public Map<InteractionNetworkGroup, Collection<InteractionNetwork>> getNetworks() {
        return networks;
    }

    public int getGeneLimit() {
        return geneLimit;
    }

    public CombiningMethod getCombiningMethod() {
        return combiningMethod;
    }

    public static List<String> getValidGenes(List<String> genes) throws SAXException, DataStoreException, ApplicationException {
        List<String> validGenes = new ArrayList();
        for (String geneName : genes) {
            if (validGene(geneName)) {
                validGenes.add(geneName);
            }
        }
        return validGenes;
    }

    private static boolean validGene(String geneName) throws SAXException, DataStoreException, ApplicationException {
        Gene gene = data.getCompletionProvider(human).getGene(geneName);
        if (gene == null) {
            return false;
        }
        return true;

    }

    private Organism getHumanOrganism(DataSet data) throws DataStoreException {
        String human = "H. Sapiens";
        human = human.toLowerCase();
        List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
        for (Organism organism : organisms) {
            String organismName = organism.getName();
            String organismAlias = organism.getAlias();
            if (organismName.toLowerCase().equals(human) || organismAlias.toLowerCase().equals(human)) {
                return organism;
            }
        }
        return null;
    }
}