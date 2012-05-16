/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;
import java.util.Collections;
import org.genemania.plugin.FileUtils;
import org.genemania.plugin.apps.AbstractPluginApp;
import org.genemania.plugin.cytoscape.NullCytoscapeUtils;
import org.genemania.plugin.data.DataSetManager;
import org.genemania.plugin.data.lucene.LuceneDataSetFactory;
import java.io.File;
import java.util.*;
import org.genemania.domain.Gene;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.InteractionNetworkGroup;
import org.genemania.domain.Organism;
import org.genemania.dto.EnrichmentEngineRequestDto;
import org.genemania.dto.EnrichmentEngineResponseDto;
import org.genemania.dto.RelatedGenesEngineRequestDto;
import org.genemania.dto.RelatedGenesEngineResponseDto;
import org.genemania.exception.ApplicationException;
import org.genemania.plugin.data.DataSet;
import org.genemania.exception.DataStoreException;
import org.genemania.plugin.GeneMania;
import org.xml.sax.SAXException;
import org.genemania.plugin.model.SearchOptions;
import org.genemania.plugin.parsers.Query;
import org.genemania.type.CombiningMethod;
import org.genemania.util.NullProgressReporter;
import org.apache.log4j.Logger;
import org.genemania.dto.*;
import org.genemania.exception.ApplicationException;
import org.genemania.engine.Mania2;
import org.genemania.engine.cache.DataCache;
import org.genemania.engine.cache.MemObjectCache;
import org.genemania.engine.cache.SynchronizedObjectCache;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.apps.QueryRunner;
/**
 *
 * @author khushi
 */
public class GenemaniaInfoRetriever {
    private String geneName; 
    private final String DATA_PATH= "gmdata-2012-01-06-core";
    private final int GENE_LIMIT = 5;
    private DataSetManager dataSetManager;
    private DataSet data;
    private Organism human;
    private Gene gene;
    private Mania2 mania;
    private DataCache cache;
    private NetworkUtils networkUtils;
    private static final int MIN_CATEGORIES = 10;
    private static final double Q_VALUE_THRESHOLD = 0.1;
    
    public GenemaniaInfoRetriever(String geneName){
        this.geneName=geneName;
    }
    
    public List<Gene> reactToGene() throws SAXException, DataStoreException, ApplicationException{
            if (validGene()){
                return getRelatedGenesByScore();      
            }
            return null;
     }
    
    private ArrayList<Gene> getRelatedGenesByScore() throws ApplicationException, DataStoreException{
        SearchOptions options =runGeneManiaAlgorithm();
        final Map<Gene, Double> scores = options.getScores();
	ArrayList<Gene> relatedGenes = new ArrayList<Gene>(scores.keySet());
	Collections.sort(relatedGenes, new Comparator<Gene>() {
		public int compare(Gene gene1, Gene gene2) {
			return -Double.compare(scores.get(gene1), scores.get(gene2));
		}
	});
        return relatedGenes;
    }
    
    private SearchOptions runGeneManiaAlgorithm() throws ApplicationException, DataStoreException{
                RelatedGenesEngineRequestDto request = createRequest();
		RelatedGenesEngineResponseDto response = runQuery(request);
		
		EnrichmentEngineRequestDto enrichmentRequest = createEnrichmentRequest(response);
		EnrichmentEngineResponseDto enrichmentResponse = computeEnrichment(enrichmentRequest);

		List<String> queryGenes = new ArrayList<String>();
                queryGenes.add(geneName);
		SearchOptions options = networkUtils.createSearchOptions(human, request, response, enrichmentResponse, data, queryGenes);
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
    
    private EnrichmentEngineResponseDto computeEnrichment(EnrichmentEngineRequestDto request) throws ApplicationException {
		if (request == null) {
			return null;
		}
		return mania.computeEnrichment(request);
	}
    private RelatedGenesEngineRequestDto createRequest() throws ApplicationException {
		RelatedGenesEngineRequestDto request = new RelatedGenesEngineRequestDto();
		request.setNamespace(GeneMania.DEFAULT_NAMESPACE);
		request.setOrganismId(human.getId());
                Map<InteractionNetworkGroup, Collection<InteractionNetwork>> networks = getHumanNetworks();
		request.setInteractionNetworks(collapseNetworks(networks));
		Set<Long> nodes = new HashSet<Long>();
                nodes.add(data.getCompletionProvider(human).getNodeId(geneName));
                request.setPositiveNodes(nodes);
		request.setLimitResults(GENE_LIMIT);
		request.setCombiningMethod(CombiningMethod.AUTOMATIC_SELECT);
		//Do we want the option of z scores?
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
    protected Collection<Collection<Long>> collapseNetworks(Map<InteractionNetworkGroup, Collection<InteractionNetwork>> networks) {
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
     
    private void initialize() throws SAXException, DataStoreException, ApplicationException{
        dataSetManager = new DataSetManager();
        dataSetManager.addDataSetFactory(new LuceneDataSetFactory<Object, Object, Object>(dataSetManager, null, new FileUtils(), new NullCytoscapeUtils<Object, Object, Object>(), null), Collections.emptyMap());      
        data = dataSetManager.open(new File (DATA_PATH));
        human= getHumanOrganism(data);
        networkUtils = new NetworkUtils();
        cache = new DataCache(new SynchronizedObjectCache(new MemObjectCache(data.getObjectCache(NullProgressReporter.instance(), false))));
        mania = new Mania2(cache);
    }
    
    private Map<InteractionNetworkGroup, Collection<InteractionNetwork>> getHumanNetworks(){
        Map<InteractionNetworkGroup, Collection<InteractionNetwork>> groupMembers = new HashMap<InteractionNetworkGroup, Collection<InteractionNetwork>>();
        Collection<InteractionNetworkGroup> groups = human.getInteractionNetworkGroups();
	for (InteractionNetworkGroup group : groups) {
            List<InteractionNetwork> networkMembers = new ArrayList<InteractionNetwork>();
            Collection<InteractionNetwork> networks = group.getInteractionNetworks();
			for (InteractionNetwork network : networks) {
                            networkMembers.add(network);
                        }
            if (networkMembers.size() > 0) {
				groupMembers.put(group, networkMembers);
            }
        }
        return groupMembers;
    }
    
    private boolean validGene() throws SAXException, DataStoreException, ApplicationException{
         initialize();
         gene = data.getCompletionProvider(human).getGene(geneName);
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
     
     public static void main (String[] args){
         //Some genes to test with: SHANK1, FOXP1, BRCA1
         GenemaniaInfoRetriever g= new GenemaniaInfoRetriever("BRCA1");
         try{
             List<Gene> relatedGenes = g.reactToGene();
             ListIterator<Gene> relatedGenesIterator = relatedGenes.listIterator();
             while (relatedGenesIterator.hasNext())
                 System.out.println(relatedGenesIterator.next().getSymbol());
         }
         catch (Exception e){
             System.err.println(e.getMessage());
         }
     }
     
}
