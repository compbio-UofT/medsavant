/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.CytoscapeVersion;
import java.awt.Color;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import org.apache.log4j.Logger;
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
import org.genemania.plugin.cytoscape2.CytoscapeUtilsImpl;
import org.genemania.plugin.cytoscape2.support.Compatibility;
import org.genemania.plugin.cytoscape26.Cy26Compatibility;
import org.genemania.plugin.data.Colour;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSetFactory;
import org.genemania.plugin.model.SearchOptions;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;
import org.genemania.type.CombiningMethod;
import org.genemania.util.NullProgressReporter;
import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.xml.sax.SAXException;
/**
 *
 * @author khushi
 */
public class GenemaniaInfoRetriever {

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

    public static class NoRelatedGenesInfoException extends Exception {

        public NoRelatedGenesInfoException() {
            super("No network information found for this gene(s) in GeneMANIA");
        }
    }
    private List<String> genes;
    private final String DATA_PATH= DirectorySettings.getCacheDirectory().getAbsolutePath()+"/" + "gmdata";
    private final int DEFAULT_GENE_LIMIT = 50;
    private final CombiningMethod DEFAULT_COMBINING_METHOD = CombiningMethod.AVERAGE;
    private final String[] DEFAULT_NETWORKS = {"Genetic interactions", "Shared protein domains", "Other", "Pathway", "Physical interactions", "Co-localization", "Predicted", "Co-expression"};
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
    private SearchOptions options;
    private static Map<Long, Integer> sequenceNumbers;
    private CytoscapeUtils cytoscapeUtils;
    private RelatedGenesEngineResponseDto response;
	static {
		sequenceNumbers = new HashMap<Long, Integer>();
	}

    public GenemaniaInfoRetriever() throws Exception {
        if (new File(DirectorySettings.getCacheDirectory().getAbsolutePath() + "/done.txt").exists()) {
            initialize();
        } else {
            throw new Exception("Data not found.");
        }
    }

    //returns invalid genes that were not set
    public void setGenes(List<String> geneNames){
        try {
            this.genes = getValidGenes(geneNames);
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(GenemaniaInfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<String> getGenes(){
        return genes;
    }

    public void setGeneLimit(int geneLimit){
        this.geneLimit = geneLimit;
    }

    public void setCombiningMethod(CombiningMethod cm){
        this.combiningMethod = cm;
    }

    public List<String> getRelatedGeneNamesByScore() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException{
        System.err.println("getting NAMES" + System.currentTimeMillis());
        List<String> geneNames = new ArrayList<String>();
        Iterator<Gene> itr = getRelatedGenesByScore().iterator();
        while (itr.hasNext()){
            geneNames.add(itr.next().getSymbol());
        }
        System.err.println("got NAMES" + System.currentTimeMillis());
        return geneNames;
    }

    public List<Gene> getRelatedGenesByScore() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException{
        System.err.println("getting related genes"+ System.currentTimeMillis());
        options =runGeneManiaAlgorithm();
        System.err.println("done running algorithm"+ System.currentTimeMillis());
        final Map<Gene, Double> scores = options.getScores();
	ArrayList<Gene> relatedGenes = new ArrayList<Gene>(scores.keySet());
	System.err.println("sorting genes by score"+ System.currentTimeMillis());
        Collections.sort(relatedGenes, new Comparator<Gene>() {
		public int compare(Gene gene1, Gene gene2) {
			return -Double.compare(scores.get(gene1), scores.get(gene2));
		}
	});
        System.err.println("got related genes"+ System.currentTimeMillis());
        return relatedGenes;
    }

    public NetworkUtils getNetworkUtils(){
        return networkUtils;
    }
    public CyNetwork getGraph(){
        EdgeAttributeProvider provider = createEdgeAttributeProvider(data, options);
        Compatibility compatibility = createCompatibility(new CytoscapeVersion());
        CytoscapeUtilsImpl cy = new CytoscapeUtilsImpl(networkUtils, compatibility);
        CyNetwork network = cy.createNetwork(data, getNextNetworkName(human), options, provider);

        computeGraphCache(network, options,networks.keySet());


		//cytoscapeUtils.registerSelectionListener(network, manager, plugin);
		cytoscapeUtils.applyVisualization(network, filterGeneScores(computeGeneScores(response), options), computeColors(data, human), computeEdgeWeightExtrema(response));
		return network;
    }
    private Map<Long, Double> filterGeneScores(Map<Long, Double> scores, SearchOptions options) {
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
		double[] extrema = new double[] { 1, 0 };
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
    private Map<String, Color> computeColors(DataSet data, Organism organism) {
		Map<String, Color> colors = new HashMap<String, Color>();
		Collection<InteractionNetworkGroup> groups = organism.getInteractionNetworkGroups();
		for (InteractionNetworkGroup group : groups) {
			Colour color = data.getColor(group);
			colors.put(group.getName(), new Color(color.getRgb()));
		}
		return colors;
	}
    private Map<Long, Double> computeGeneScores(RelatedGenesEngineResponseDto result) {
		Map<Long, Double> scores = new HashMap<Long, Double>();
		for (NetworkDto network : result.getNetworks()) {
			for (InteractionDto interaction : network.getInteractions()) {
				NodeDto node1 = interaction.getNodeVO1();
				scores.put(node1.getId(), node1.getScore());
				NodeDto node2 = interaction.getNodeVO2();
				scores.put(node2.getId(), node2.getScore());
			}
		}
		return scores;
	}

    void computeGraphCache(CyNetwork currentNetwork, SearchOptions config, Collection<InteractionNetworkGroup> selectedGroups) {
		// Build edge cache
		InteractionNetworkGroup currentGroup = new InteractionNetworkGroup();
		NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = cytoscapeUtils.getNetworkProxy(currentNetwork);
		for (CyEdge edge : networkProxy.getEdges()) {
			EdgeProxy<CyEdge, CyNode> edgeProxy = cytoscapeUtils.getEdgeProxy(edge);
			String name = edgeProxy.getAttribute(org.genemania.plugin.cytoscape.CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, String.class);
			int groupId = config.getGroup(name);
			currentGroup.setId(groupId);
			config.addEdge(currentGroup, edgeProxy.getIdentifier());
		}

		// Build node cache
		for (Gene gene : config.getScores().keySet()) {
			Node node = gene.getNode();
			CyNode cyNode = cytoscapeUtils.getNode(currentNetwork, node, null);
			NodeProxy<CyNode> nodeProxy = cytoscapeUtils.getNodeProxy(cyNode);
			config.addNode(node, nodeProxy.getIdentifier());
		}

		// Cache selected networks
		applyDefaultSelection(config, selectedGroups);

		// Enable composite network by default
		config.setEnabled(-1, true);
	}
    private void applyDefaultSelection(SearchOptions config, Collection<InteractionNetworkGroup> selectedGroups) {
		Set<String> targetGroups = new HashSet<String>();
		targetGroups.add("coloc"); //$NON-NLS-1$
		targetGroups.add("coexp"); //$NON-NLS-1$

		// By default, disable colocation/coexpression networks.
		Set<String> retainedGroups = new HashSet<String>();
		for (InteractionNetworkGroup group : selectedGroups) {
			String code = group.getCode();
			boolean enabled = !targetGroups.remove(code);

			if (enabled) {
				retainedGroups.add(code);
			}
			config.setEnabled(group.getId(), enabled);
		}

		// If we only have colocation/coexpression networks, enabled them.
		if (retainedGroups.size() == 0) {
			for (InteractionNetworkGroup group : selectedGroups) {
				config.setEnabled(group.getId(), true);
			}
		}
	}

    private static String getNextNetworkName(Organism organism) {
		long id = organism.getId();
		int sequenceNumber;
		if (sequenceNumbers.containsKey(id)) {
			sequenceNumber = sequenceNumbers.get(id) + 1;
		} else {
			sequenceNumber = 1;
		}
		sequenceNumbers.put(id, sequenceNumber);
		return String.format(Strings.retrieveRelatedGenesNetworkName_label, organism.getName(), sequenceNumber);
	}

    private EdgeAttributeProvider createEdgeAttributeProvider(DataSet data, SearchOptions options) {
		final Map<Long, InteractionNetworkGroup> groupsByNetwork = options.getGroups();
		List<InteractionNetworkGroup> groups = networkUtils.collectGroups(options);
		final Map<Long, Integer> ranks = new HashMap<Long, Integer>();
		for (int rank = 0; rank < groups.size(); rank++) {
			ranks.put(groups.get(rank).getId(), rank);
		}

		return new EdgeAttributeProvider() {
			public Map<String, Object> getAttributes(InteractionNetwork network) {
				HashMap<String, Object> attributes = new HashMap<String, Object>();
				long id = network.getId();
				if (id == -1) {
//					attributes.put(CytoscapeUtils.NETWORK_GROUP_ID_ATTRIBUTE, -1);
				} else {
					InteractionNetworkGroup group = groupsByNetwork.get(id);
					if (group != null) {
						int rank = ranks.get(groupsByNetwork.get(network.getId()).getId());
						attributes.put(org.genemania.plugin.cytoscape.CytoscapeUtils.RANK_ATTRIBUTE, rank);
						attributes.put(org.genemania.plugin.cytoscape.CytoscapeUtils.NETWORK_GROUP_NAME_ATTRIBUTE, group.getName());
					}
				}
				return attributes;
			}

			public String getEdgeLabel(InteractionNetwork network) {
				long id = network.getId();
				if (id == -1) {
					return "combined"; //$NON-NLS-1$
				} else {
					InteractionNetworkGroup group = groupsByNetwork.get(id);
					if (group != null) {
						return group.getName();
					}
					return "unknown"; //$NON-NLS-1$
				}
			}
		};
	}

    private SearchOptions runGeneManiaAlgorithm() throws ApplicationException, DataStoreException, NoRelatedGenesInfoException{
                RelatedGenesEngineRequestDto request = createRequest();
		response = runQuery(request);

		EnrichmentEngineRequestDto enrichmentRequest = createEnrichmentRequest(response);
		EnrichmentEngineResponseDto enrichmentResponse = computeEnrichment(enrichmentRequest);

		SearchOptions options = networkUtils.createSearchOptions(human, request, response, enrichmentResponse, data, genes);
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
                if(request.getNodes().size() ==0)
                    throw new NoRelatedGenesInfoException();
		return mania.computeEnrichment(request);
	}
    private RelatedGenesEngineRequestDto createRequest() throws ApplicationException {
		RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setOrganismId(human.getId());
                request.setInteractionNetworks(collapseNetworks(networks));
		Set<Long> nodes = new HashSet<Long>();
                for(String geneName: genes){
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
        try{
            dataSetManager = new DataSetManager();
            dataSetManager.addDataSetFactory(new LuceneDataSetFactory<Object, Object, Object>(dataSetManager, null, new FileUtils(), new NullCytoscapeUtils<Object, Object, Object>(), null), Collections.emptyMap());
                data = dataSetManager.open(new File (DATA_PATH));

            human= getHumanOrganism(data);
            networkUtils = new NetworkUtils();
            cytoscapeUtils = new CytoscapeUtils(networkUtils);
            cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
            mania = new Mania2(cache);
            setGeneLimit(DEFAULT_GENE_LIMIT);
            setCombiningMethod(DEFAULT_COMBINING_METHOD);
            setNetworks(new HashSet<String>(Arrays.asList(DEFAULT_NETWORKS)));


        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setNetworks(Set<String> n){
        Map<InteractionNetworkGroup, Collection<InteractionNetwork>> groupMembers = new HashMap<InteractionNetworkGroup, Collection<InteractionNetwork>>();
        Collection<InteractionNetworkGroup> groups = human.getInteractionNetworkGroups();
	Set<String> notHandled = n;
        for (InteractionNetworkGroup group : groups) {
            if(n.contains(group.getName())){
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
        networks= groupMembers;
    }

    public Map<InteractionNetworkGroup, Collection<InteractionNetwork>> getNetworks(){
        return networks;
    }

    public int getGeneLimit(){
        return geneLimit;
    }

    public CombiningMethod getCombiningMethod(){
        return combiningMethod;
    }

    public static List<String> getValidGenes(List<String> genes) throws SAXException, DataStoreException, ApplicationException{
        List<String> validGenes = new ArrayList();
        for (String geneName: genes){
            if (validGene(geneName))
                validGenes.add(geneName);
        }
        return validGenes;
    }

    private static boolean validGene(String geneName) throws SAXException, DataStoreException, ApplicationException{
        Gene gene = data.getCompletionProvider(human).getGene(geneName);
        if (gene==null)
           return false;
        return true;

     }

     private Organism getHumanOrganism(DataSet data) throws DataStoreException{
        String human = "H. Sapiens";
        human= human.toLowerCase();
        List<Organism> organisms = data.getMediatorProvider().getOrganismMediator().getAllOrganisms();
        for (Organism organism : organisms) {
                 String organismName = organism.getName();
                 String organismAlias = organism.getAlias();
                 if(organismName.toLowerCase().equals(human)|| organismAlias.toLowerCase().equals(human)){
                     return organism;
                 }
        }
        return null;
     }
}