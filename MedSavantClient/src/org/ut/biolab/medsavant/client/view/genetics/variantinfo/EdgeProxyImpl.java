/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

/**
 *
 * @author khushi
 */
import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import org.genemania.plugin.proxies.EdgeProxy;

class EdgeProxyImpl implements EdgeProxy<CyEdge, CyNode> {
    private final Reference<CyEdge> reference;
    EdgeProxyImpl(CyEdge edge) {
		reference = new WeakReference<CyEdge>(edge);
	}

	@Override
	public String getIdentifier() {
		return getProxied().getIdentifier();
	}

	@Override
	public CyNode getSource() {
		return (CyNode) getProxied().getSource();
	}

	@Override
	public CyNode getTarget() {
		return (CyNode) getProxied().getTarget();
	}


	protected CyAttributes getAttributes() {
		return Cytoscape.getEdgeAttributes();
	}
        @Override
	public CyEdge getProxied() {
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