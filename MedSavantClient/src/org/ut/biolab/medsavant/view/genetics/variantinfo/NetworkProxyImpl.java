/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

/**
 *
 * @author khushi
 */
import giny.model.Edge;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.genemania.plugin.OneUseIterable;


import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.SelectFilter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import org.genemania.plugin.proxies.NetworkProxy;

class NetworkProxyImpl implements NetworkProxy<CyNetwork,CyNode, CyEdge>  {
private final Reference<CyNetwork> reference;	
    NetworkProxyImpl(CyNetwork network) {
		reference = new WeakReference<CyNetwork>(network);
	}

	
	protected CyAttributes getAttributes() {
		return Cytoscape.getNetworkAttributes();
	}

	@SuppressWarnings("unchecked")
	
	public Collection<CyEdge> getEdges() {
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for (CyEdge edge : new OneUseIterable<CyEdge>(getProxied().edgesIterator())) {
			edges.add(edge);
		}
		return edges;
	}

	@SuppressWarnings("unchecked")
	
	public Collection<CyNode> getNodes() {
		Set<CyNode> nodes = new HashSet<CyNode>();
		for (CyNode node : new OneUseIterable<CyNode>(getProxied().nodesIterator())) {
			nodes.add(node);
		}
		return nodes;
	}

	@SuppressWarnings("unchecked")
	
	public Set<CyEdge> getSelectedEdges() {
		Set<CyEdge> edges = new HashSet<CyEdge>();
		for (CyEdge edge : (Set<CyEdge>) getProxied().getSelectedEdges()) {
			edges.add(edge);
		}
		return edges;
	}

	
	public String getTitle() {
		return getProxied().getTitle();
	}

	
	public void setSelectedNode(CyNode node, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		filter.setSelected(node, selected);
	}

	
	public void setSelectedEdge(CyEdge edge, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		filter.setSelected(edge, selected);
	}

	
	public void setSelectedEdges(
			Collection<CyEdge> proxies, boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		Set<Edge> edges = new HashSet<Edge>();
		for (CyEdge proxy : proxies) {
			edges.add(proxy);
		}
		filter.setSelectedEdges(edges, selected);
	}

	
	public void setSelectedNodes(Collection<CyNode> proxies,
			boolean selected) {
		SelectFilter filter = getProxied().getSelectFilter();
		Set<giny.model.Node> nodes = new HashSet<giny.model.Node>();
		for (CyNode proxy : proxies) {
			nodes.add(proxy);
		}
		filter.setSelectedNodes(nodes, selected);
	}

	
	public void unselectAllEdges() {
		getProxied().getSelectFilter().unselectAllEdges();
	}

	
	public void unselectAllNodes() {
		getProxied().getSelectFilter().unselectAllNodes();
	}

	
	public String getIdentifier() {
		return getProxied().getIdentifier();
	}
	
	
	public Collection<String> getNodeAttributeNames() {
		return Arrays.asList(Cytoscape.getNodeAttributes().getAttributeNames());
	}
	
	
	public Collection<String> getEdgeAttributeNames() {
		return Arrays.asList(Cytoscape.getEdgeAttributes().getAttributeNames());
	}
        
        public CyNetwork getProxied() {
		return reference.get();
	}

   @Override
	public <U> U getAttribute(String name, Class<U> type) {
		return CytoscapeUtils.getAttributeInternal(getAttributes(), (String) getIdentifier(), name, type);
	}
   @Override
	public <U> void setAttribute(String name, U value) {
		CytoscapeUtils.setAttributeInternal(getAttributes(), (String) getIdentifier(), name, value);
	}
	
	@Override
	public Class<?> getAttributeType(String name) {
		switch (getAttributes().getType(name)) {
		case CyAttributes.TYPE_BOOLEAN:
			return Boolean.class;
		case CyAttributes.TYPE_FLOATING:
			return Double.class;
		case CyAttributes.TYPE_INTEGER:
			return Integer.class;
		case CyAttributes.TYPE_SIMPLE_LIST:
			return List.class;
		case CyAttributes.TYPE_STRING:
			return String.class;
		}
		return null;
	}
}
