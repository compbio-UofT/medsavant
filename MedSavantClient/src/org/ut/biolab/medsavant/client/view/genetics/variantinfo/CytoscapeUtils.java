/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import cytoscape.*;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.*;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.*;
import java.awt.Color;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.genemania.domain.Gene;
import org.genemania.domain.Interaction;
import org.genemania.domain.InteractionNetwork;
import org.genemania.domain.Node;
import org.genemania.plugin.EdgeAttributeProvider;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.NetworkUtils;
import org.genemania.plugin.Strings;
import org.genemania.plugin.cytoscape2.CompatibilityImpl;
import org.genemania.plugin.cytoscape2.support.Compatibility;
import org.genemania.plugin.cytoscape26.Cy26Compatibility;
import org.genemania.plugin.data.DataSet;
import org.genemania.plugin.model.AnnotationEntry;
import org.genemania.plugin.model.SearchOptions;
import org.genemania.plugin.proxies.EdgeProxy;
import org.genemania.plugin.proxies.NetworkProxy;
import org.genemania.plugin.proxies.NodeProxy;

/**
 *
 * @author khushi
 */
public class CytoscapeUtils {
    private final Map<CyNetwork, NetworkProxy<CyNetwork, CyNode, CyEdge>> networkProxies;
    protected final NetworkUtils networkUtils;
    static final String HIGHLIGHT_ATTRIBUTE = "highlight"; //$NON-NLS-1$
    static final String RAW_WEIGHTS_ATTRIBUTE = "raw weights"; //$NON-NLS-1$
    static final String MAX_WEIGHT_ATTRIBUTE = "normalized max weight"; //$NON-NLS-1$
    static final String NETWORK_NAMES_ATTRIBUTE = "networks"; //$NON-NLS-1$
    private static final String EDGE_TYPE_INTERACTION = "interaction";
    static final String RANK_ATTRIBUTE = "rank"; //$NON-NLS-1$
	static final String TYPE_ATTRIBUTE = "type"; //$NON-NLS-1$
	static final String GENEMANIA_NETWORK_TYPE = "genemania"; //$NON-NLS-1$
	static final String ORGANISM_NAME_ATTRIBUTE = "organism"; //$NON-NLS-1$
	static final String DATA_VERSION_ATTRIBUTE = "data version"; //$NON-NLS-1$
	static final String NETWORKS_ATTRIBUTE = "source-networks"; //$NON-NLS-1$
	static final String COMBINING_METHOD_ATTRIBUTE = "combining method"; //$NON-NLS-1$
	static final String SEARCH_LIMIT_ATTRIBUTE = "search limit"; //$NON-NLS-1$
	static final String ANNOTATIONS_ATTRIBUTE = "annotations"; //$NON-NLS-1$
	static final String LOG_SCORE_ATTRIBUTE = "log score"; //$NON-NLS-1$
	static final String SCORE_ATTRIBUTE = "score"; //$NON-NLS-1$
	static final String NODE_TYPE_RESULT = "result"; //$NON-NLS-1$
	static final String NODE_TYPE_QUERY = "query"; //$NON-NLS-1$
	static final String ANNOTATION_ID_ATTRIBUTE = "annotations"; //$NON-NLS-1$
	static final String ANNOTATION_NAME_ATTRIBUTE = "annotation name"; //$NON-NLS-1$
	static final String GENE_NAME_ATTRIBUTE = "gene name"; //$NON-NLS-1$
	static final String NODE_TYPE_ATTRIBUTE = "node type"; //$NON-NLS-1$
	static final Color QUERY_COLOR = new Color(131, 143, 166);
	static final Color RESULT_COLOR = new Color(255, 255, 255);
        static final String NETWORK_GROUP_NAME_ATTRIBUTE = "data type"; //$NON-NLS-1$
        	protected static final double MINIMUM_NODE_SIZE = 10;
	protected static final double MAXIMUM_NODE_SIZE = 40;
	protected static final double MINIMUM_EDGE_WIDTH = 1;
	protected static final double MAXIMUM_EDGE_WIDTH = 6;
    private final Map<String, Reference<CyEdge>> edges;
    private final Map<CyEdge, EdgeProxy<CyEdge, CyNode>> edgeProxies;
    private final Map<CyNode, NodeProxy<CyNode>> nodeProxies;
    private Compatibility compatibility;
    public CytoscapeUtils(NetworkUtils networkUtils){
        networkProxies = new WeakHashMap<CyNetwork, NetworkProxy<CyNetwork, CyNode, CyEdge>>();
        this.networkUtils= networkUtils;
        compatibility = createCompatibility(new CytoscapeVersion());
        edges = new WeakHashMap<String, Reference<CyEdge>>();
        nodeProxies = new WeakHashMap<CyNode, NodeProxy<CyNode>>();
        edgeProxies = new WeakHashMap<CyEdge, EdgeProxy<CyEdge, CyNode>>();
    }
    private Compatibility createCompatibility(CytoscapeVersion version) {
		String[] parts = version.getMajorVersion().split("[.]");
		if (!parts[0].equals("2")) {
			throw new RuntimeException("This plugin is only compatible with Cytoscape 2.X");
		}

		int minorVersion = Integer.parseInt(parts[1]);
		if (minorVersion < 8) {
			return new Cy26Compatibility();
		}
		return new CompatibilityImpl();
	}
    public void applyVisualization(CyNetwork network, Map<Long, Double> scores, Map<String, Color> colors, double[] extrema) {
		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CalculatorCatalog catalog = manager.getCalculatorCatalog();
		String styleName = getVisualStyleName(network);
		VisualStyle style = catalog.getVisualStyle(styleName);
		if (style == null) {
			style = new VisualStyle(styleName);
			catalog.addVisualStyle(style);
		}

		NodeAppearanceCalculator nodeAppearance = style.getNodeAppearanceCalculator();
		nodeAppearance.setCalculator(createNodeColourCalculator(network));
		nodeAppearance.setCalculator(createNodeLabelCalculator());

		nodeAppearance.setCalculator(createNodeSizeCalculator(network, scores, MINIMUM_NODE_SIZE, MAXIMUM_NODE_SIZE));

		EdgeAppearanceCalculator edgeAppearance = style.getEdgeAppearanceCalculator();
		edgeAppearance.setCalculator(createEdgeColourCalculator(network, colors));
		edgeAppearance.setCalculator(createEdgeOpacityCalculator(network));

		edgeAppearance.setCalculator(createEdgeWidthCalculator(network, extrema[0], extrema[1], MINIMUM_EDGE_WIDTH, MAXIMUM_EDGE_WIDTH));

		VisualPropertyType.NODE_LABEL_POSITION.setDefault(style, compatibility.createDefaultNodeLabelPosition());
		VisualPropertyType.NODE_SHAPE.setDefault(style, NodeShape.ELLIPSE);

		manager.setVisualStyle(style);

		CyNetworkView networkView = getNetworkView(network);
		networkView.setVisualStyle(styleName);

		manager.applyNodeAppearances(network, networkView);
	}
    private Calculator createNodeSizeCalculator(CyNetwork network, Map<Long, Double> scores, double minSize, double maxSize) {
		VisualPropertyType type = VisualPropertyType.NODE_SIZE;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		ContinuousMapping mapping = compatibility.createContinuousMapping(defaultObject, SCORE_ATTRIBUTE, network, Compatibility.MappingType.NODE);

		double[] values = networkUtils.sortScores(scores);
		mapping.setInterpolator(new LinearNumberToNumberInterpolator());
		mapping.addPoint(values[0], new BoundaryRangeValues(minSize, minSize, minSize));
		mapping.addPoint(values[values.length - 1], new BoundaryRangeValues(maxSize, maxSize, maxSize));
		return new BasicCalculator(String.format("nodeSize-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
    private Calculator createEdgeWidthCalculator(CyNetwork network, double minWeight, double maxWeight, double minSize, double maxSize) {
		VisualPropertyType type = VisualPropertyType.EDGE_LINE_WIDTH;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		ContinuousMapping mapping = compatibility.createContinuousMapping(defaultObject, MAX_WEIGHT_ATTRIBUTE, network, Compatibility.MappingType.EDGE);
		mapping.setInterpolator(new LinearNumberToNumberInterpolator());
		mapping.addPoint(minWeight, new BoundaryRangeValues(minSize, minSize, minSize));
		mapping.addPoint(maxWeight, new BoundaryRangeValues(maxSize, maxSize, maxSize));
		return new BasicCalculator(String.format("edgeWidth-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
    private Calculator createNodeLabelCalculator() {
		VisualPropertyType type = VisualPropertyType.NODE_LABEL;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		PassThroughMapping mapping = compatibility.createPassThroughMapping(defaultObject, GENE_NAME_ATTRIBUTE);
		return new BasicCalculator("Gene name calculator", mapping, type); //$NON-NLS-1$
	}
    private Calculator createEdgeColourCalculator(CyNetwork network, Map<String, Color> colors) {
		VisualPropertyType type = VisualPropertyType.EDGE_COLOR;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, NETWORK_GROUP_NAME_ATTRIBUTE, network, Compatibility.MappingType.EDGE);
		for (Map.Entry<String, Color> entry : colors.entrySet()) {
			mapping.putMapValue(entry.getKey(), entry.getValue());
		}
		return new BasicCalculator(String.format("edgeColour-%s", escapeTitle(network.getTitle())), mapping, type); //$NON-NLS-1$
	}
    private String escapeTitle(String title) {
		return title.replaceAll("[.]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}
    private Calculator createEdgeOpacityCalculator(CyNetwork network) {
		VisualPropertyType type = VisualPropertyType.EDGE_OPACITY;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, HIGHLIGHT_ATTRIBUTE, network, Compatibility.MappingType.EDGE);
		mapping.putMapValue(1, 255);
		mapping.putMapValue(0, 64);
		return new BasicCalculator("Dynamic calculator", mapping, type); //$NON-NLS-1$
	}
    public CyNetworkView getNetworkView(CyNetwork network) {
		String id = network.getIdentifier();
		if (!Cytoscape.viewExists(id)) {
			return Cytoscape.createNetworkView(network);
		}
		return Cytoscape.getNetworkView(id);
	}
    protected String getVisualStyleName(CyNetwork network) {
		NetworkProxy<CyNetwork, CyNode, CyEdge> proxy = getNetworkProxy(network);
		return proxy.getTitle().replace(".", ""); //$NON-NLS-1$ //$NON-NLS-2$;
	}
	private Calculator createNodeColourCalculator(CyNetwork network) {
		VisualPropertyType type = VisualPropertyType.NODE_FILL_COLOR;
		Object defaultObject = type.getDefault(Cytoscape.getVisualMappingManager().getVisualStyle());
		DiscreteMapping mapping = compatibility.createDiscreteMapping(defaultObject, NODE_TYPE_ATTRIBUTE, network, Compatibility.MappingType.NODE);

		mapping.putMapValue(NODE_TYPE_QUERY, QUERY_COLOR);
		mapping.putMapValue(NODE_TYPE_RESULT, RESULT_COLOR);
		return new BasicCalculator("Type-based calculator", mapping, type); //$NON-NLS-1$
	}


    public CyNetwork createNetwork(DataSet data, String name, SearchOptions options, EdgeAttributeProvider attributeProvider) {
		CyNetwork currentNetwork = createNetwork(name);
		NetworkProxy<CyNetwork, CyNode, CyEdge> networkProxy = getNetworkProxy(currentNetwork);
		networkProxy.setAttribute(TYPE_ATTRIBUTE, GENEMANIA_NETWORK_TYPE);
		networkProxy.setAttribute(DATA_VERSION_ATTRIBUTE, data.getVersion());
		networkProxy.setAttribute(ORGANISM_NAME_ATTRIBUTE, options.getOrganism().getName());
		networkProxy.setAttribute(NETWORKS_ATTRIBUTE, serializeNetworks(options));
		networkProxy.setAttribute(COMBINING_METHOD_ATTRIBUTE, options.getCombiningMethod().getCode());
		networkProxy.setAttribute(SEARCH_LIMIT_ATTRIBUTE, options.getSearchLimit());
		networkProxy.setAttribute(ANNOTATIONS_ATTRIBUTE, serializeAnnotations(options));

		Map<Long, Collection<Interaction>> sources = networkUtils.createInteractionMap(options.getSourceInteractions());

		for (InteractionNetwork network : networkUtils.createSortedList(options.getNetworkWeights())) {
			Collection<Interaction> sourceInteractions = sources.get(network.getId());
			if (sourceInteractions == null || sourceInteractions.size() == 0) {
				continue;
			}
			buildGraph(currentNetwork, sourceInteractions, network, attributeProvider, options);
		}

		// Add all query genes in case they don't show up in the results
		for (Gene gene : options.getQueryGenes().values()) {
			Node node = gene.getNode();
			getNode(currentNetwork, node, getSymbol(gene));
		}

		decorateNodes(currentNetwork, options);
		return currentNetwork;
	}
    private String serializeAnnotations(SearchOptions options) {
		StringWriter writer = new StringWriter();

		JsonFactory jsonFactory = new MappingJsonFactory();
		try {
			JsonGenerator generator = jsonFactory.createJsonGenerator(writer);

			generator.writeStartArray();
			List<AnnotationEntry> enrichmentSummary = options.getEnrichmentSummary();
			for (AnnotationEntry entry : enrichmentSummary) {
				generator.writeStartObject();
				generator.writeFieldName("name"); //$NON-NLS-1$
				generator.writeString(entry.getName());
				generator.writeFieldName("description"); //$NON-NLS-1$
				generator.writeString(entry.getDescription());
				generator.writeFieldName("qValue"); //$NON-NLS-1$
				generator.writeNumber(entry.getQValue());
				generator.writeFieldName("sample"); //$NON-NLS-1$
				generator.writeNumber(entry.getSampleOccurrences());
				generator.writeFieldName("total"); //$NON-NLS-1$
				generator.writeNumber(entry.getTotalOccurrences());
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.close();
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
			return ""; //$NON-NLS-1$
		}
		return writer.toString();
	}
    	private String serializeNetworks(SearchOptions options) {
		JsonFactory factory = new MappingJsonFactory();
		StringWriter writer = new StringWriter();

		try {
			JsonGenerator generator = factory.createJsonGenerator(writer);
			generator.writeStartArray();
			Map<InteractionNetwork, Double> networkWeights = options.getNetworkWeights();
			for (Map.Entry<InteractionNetwork, Double> entry : networkWeights.entrySet()) {
				generator.writeStartObject();
				InteractionNetwork network = entry.getKey();
				generator.writeFieldName("group"); //$NON-NLS-1$
				generator.writeString(options.getGroup(network.getId()).getName());
				generator.writeFieldName("name"); //$NON-NLS-1$
				generator.writeString(network.getName());
				generator.writeFieldName("weight"); //$NON-NLS-1$
				generator.writeNumber(entry.getValue());
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.close();
		} catch (IOException e) {
			LogUtils.log(getClass(), e);
			return ""; //$NON-NLS-1$
		}
		return writer.toString();
	}


	public NodeProxy<CyNode> getNodeProxy(CyNode node) {
		if (node == null) {
			return null;
		}
		if (nodeProxies.containsKey(node)) {
			return nodeProxies.get(node);
		}
		NodeProxy<CyNode> proxy = createNodeProxy(node);
		nodeProxies.put(node, proxy);
		return proxy;
	}
        protected NodeProxy<CyNode> createNodeProxy(CyNode node) {
		return new NodeProxyImpl(node);
	}
    /**
	 * Decorates the nodes in the active NETWORK with the results of
	 * the GeneMANIA algorithm.  For example, scores are assigned to the
	 * nodes.
	 * @param currentNetwork
	 *
	 * @param options
	 * @param queryGenes
	 */
private void decorateNodes(CyNetwork currentNetwork, SearchOptions options) {
		// Assign scores.
		Map<Long, Gene> queryGenes = options.getQueryGenes();
		Map<Gene, Double> scores = options.getScores();

		for (Map.Entry<Gene, Double> entry : scores.entrySet()) {
			double score = entry.getValue();
			Node node = entry.getKey().getNode();

			CyNode cyNode = getNode(currentNetwork, node, getSymbol(queryGenes.get(node)));
			NodeProxy<CyNode> nodeProxy = getNodeProxy(cyNode);

			nodeProxy.setAttribute(LOG_SCORE_ATTRIBUTE, Math.log(score));
			nodeProxy.setAttribute(SCORE_ATTRIBUTE, score);
			String type;
			if (queryGenes.containsKey(node.getId())) {
				type = NODE_TYPE_QUERY;
			} else {
				type = NODE_TYPE_RESULT;
			}

			Collection<AnnotationEntry> nodeAnnotations = options.getAnnotations(node.getId());
			if (nodeAnnotations != null) {
				List<String> annotationIds = new ArrayList<String>();
				List<String> annotationNames = new ArrayList<String>();
				for (AnnotationEntry annotation : nodeAnnotations) {
					annotationIds.add(annotation.getName());
					annotationNames.add(annotation.getDescription());
				}
				nodeProxy.setAttribute(ANNOTATION_ID_ATTRIBUTE, annotationIds);
				nodeProxy.setAttribute(ANNOTATION_NAME_ATTRIBUTE, annotationNames);
			}

			nodeProxy.setAttribute(NODE_TYPE_ATTRIBUTE, type);
		}
	}
    @SuppressWarnings("unchecked")
	private void buildGraph(CyNetwork currentNetwork, Collection<Interaction> interactions, InteractionNetwork network, EdgeAttributeProvider attributeProvider, SearchOptions options) {
		Map<Long, Gene> queryGenes = options.getQueryGenes();
		Map<InteractionNetwork, Double> weights = options.getNetworkWeights();

		Double networkWeight = weights.get(network);
		if (networkWeight == null) {
			networkWeight = 1.0;
		}

		for (Interaction interaction : interactions) {
			Node fromNode = interaction.getFromNode();
			CyNode from = getNode(currentNetwork, fromNode, getSymbol(queryGenes.get(fromNode.getId())));

			Node toNode = interaction.getToNode();
			CyNode to = getNode(currentNetwork, toNode, getSymbol(queryGenes.get(toNode.getId())));

			String edgeLabel = attributeProvider.getEdgeLabel(network);
			CyEdge edge = getEdge(from, to, EDGE_TYPE_INTERACTION, edgeLabel, currentNetwork);
			EdgeProxy<CyEdge, CyNode> edgeProxy = getEdgeProxy(edge);

			Object edgeId = edgeProxy.getIdentifier();
			Double rawWeight = (double) interaction.getWeight();

			options.addSourceNetwork(edgeId, network);
			Double weight = rawWeight * networkWeight;

			List<String> networkNames = edgeProxy.getAttribute(NETWORK_NAMES_ATTRIBUTE, List.class);
			if (networkNames == null) {
				networkNames = new ArrayList<String>();
			}
			networkNames.add(network.getName());
			edgeProxy.setAttribute(NETWORK_NAMES_ATTRIBUTE, networkNames);

			List<Double> edgeWeights = edgeProxy.getAttribute(RAW_WEIGHTS_ATTRIBUTE, List.class);
			if (edgeWeights == null) {
				edgeWeights = new ArrayList<Double>();
			}
			edgeWeights.add((double) interaction.getWeight());
			edgeProxy.setAttribute(RAW_WEIGHTS_ATTRIBUTE, edgeWeights);

			Double oldWeight = edgeProxy.getAttribute(MAX_WEIGHT_ATTRIBUTE, Double.class);
			if (oldWeight == null || oldWeight < weight) {
				edgeProxy.setAttribute(MAX_WEIGHT_ATTRIBUTE, weight);
			}

			edgeProxy.setAttribute(HIGHLIGHT_ATTRIBUTE, 1);
			for (Map.Entry<String, Object> entry : attributeProvider.getAttributes(network).entrySet()) {
				edgeProxy.setAttribute(entry.getKey(), entry.getValue());
			}
		}
	}

        private String getSymbol(Gene gene) {
		if (gene == null) {
			return null;
		}
		return gene.getSymbol();
	}


	protected CyEdge getEdge(CyNode from, CyNode to, String type, String label, CyNetwork network) {
		CyEdge edge = Cytoscape.getCyEdge(from, to, Semantics.INTERACTION, label, true);
		network.addEdge(edge);
		edges.put(edge.getIdentifier(), new WeakReference<CyEdge>(edge));
		return edge;
	}

	public EdgeProxy<CyEdge, CyNode> getEdgeProxy(CyEdge edge) {
		if (edge == null) {
			return null;
		}
		if (edgeProxies.containsKey(edge)) {
			return edgeProxies.get(edge);
		}
		EdgeProxy<CyEdge, CyNode> proxy = createEdgeProxy(edge);
		edgeProxies.put(edge, proxy);
		return proxy;
	}
	protected EdgeProxy<CyEdge, CyNode> createEdgeProxy(CyEdge edge) {
		return new EdgeProxyImpl(edge);
	}
        protected CyNetwork createNetwork(String title) {
		return Cytoscape.createNetwork(title, false);
	}
        /**
	 * Returns the <code>NODE</code> that corresponds to the given
	 * <code>Node</code>.  If the <code>NODE</code> does not already
	 * exist, a new one is created.
	 *
	 * @param node
	 * @param preferredSymbol
	 * @return
	 */
	public CyNode getNode(CyNetwork network, Node node, String preferredSymbol) {
		String id = getNodeId(network, node);
		CyNode target = getNode(id, network);
		if (target != null) {
			return target;
		}

		String name;
		if (preferredSymbol == null) {
			Gene gene = networkUtils.getPreferredGene(node);
			if (gene == null) {
				name = Strings.missingGeneName;
			} else {
				name = gene.getSymbol();
			}
		} else {
			name = preferredSymbol;
		}

		target = createNode(id, network);


		return target;
	}
       protected CyNode createNode(String id, CyNetwork network) {
		CyNode node = Cytoscape.getCyNode(id, true);
		network.addNode(node);
		return node;
	}

        protected CyNode getNode(String id, CyNetwork network) {
		return Cytoscape.getCyNode(id);
	}
        protected String getNodeId(CyNetwork network, Node node) {
		NetworkProxy<CyNetwork, CyNode, CyEdge> proxy = getNetworkProxy(network);
		return String.format("%s-%s", filterTitle(proxy.getTitle()), node.getName()); //$NON-NLS-1$
	}

        public NetworkProxy<CyNetwork, CyNode, CyEdge> getNetworkProxy(CyNetwork network) {
		if (network == null) {
			return null;
		}
		if (networkProxies.containsKey(network)) {
			return networkProxies.get(network);
		}
		NetworkProxy<CyNetwork, CyNode, CyEdge> proxy = createNetworkProxy(network);
		networkProxies.put(network, proxy);
		return proxy;
	}

        protected NetworkProxy<CyNetwork, CyNode, CyEdge> createNetworkProxy(CyNetwork network) {
		return new NetworkProxyImpl(network);
	}

        @SuppressWarnings("unchecked")
	static <T> T getAttributeInternal(CyAttributes attributes, String id, String name, Class<T> type) {
		if (type.equals(String.class)) {
			return (T) attributes.getStringAttribute(id, name);
		} else if (type.equals(Long.class)) {
			return (T) attributes.getIntegerAttribute(id, name);
		} else if (type.equals(Integer.class)) {
			return (T) attributes.getIntegerAttribute(id, name);
		} else if (type.equals(Double.class)) {
			return (T) attributes.getDoubleAttribute(id, name);
		} else if (type.equals(Boolean.class)) {
			return (T) attributes.getBooleanAttribute(id, name);
		} else if (type.equals(List.class)) {
			return (T) attributes.getListAttribute(id, name);
		}
		return (T) attributes.getAttribute(id, name);
	}

        static void setAttributeInternal(CyAttributes attributes, String identifier, String key, Object value) {
		if (value == null) {
			if (!attributes.hasAttribute(identifier, key)) {
				return;
			}
			attributes.deleteAttribute(identifier, key);
			return;
		}

		if (value instanceof Long) {
			attributes.setAttribute(identifier, key, ((Long) value).intValue());
		} else if (value instanceof Integer) {
			attributes.setAttribute(identifier, key, (Integer) value);
		} else if (value instanceof String) {
			attributes.setAttribute(identifier, key, (String) value);
		} else if (value instanceof Double) {
			attributes.setAttribute(identifier, key, (Double) value);
		} else if (value instanceof Boolean) {
			attributes.setAttribute(identifier, key, (Boolean) value);
		} else if (value instanceof List) {
			attributes.setListAttribute(identifier, key, (List) value);
		}
	}

        private String filterTitle(String title) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0 ; i < title.length(); i++) {
			char character = title.charAt(i);
			if (Character.isLetterOrDigit(character)) {
				builder.append(character);
			} else {
				builder.append("_"); //$NON-NLS-1$
			}
		}
		return builder.toString();
	}
}
