/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

/**
 *
 * @author khushi
 */
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

import org.genemania.plugin.proxies.NodeProxy;

class NodeProxyImpl implements NodeProxy<CyNode> {
private final Reference<CyNode> reference;
    NodeProxyImpl(CyNode node) {
		reference = new WeakReference<CyNode>(node);
	}

	@Override
	public String getIdentifier() {
		return getProxied().getIdentifier();
	}


	protected CyAttributes getAttributes() {
		return Cytoscape.getNodeAttributes();
	}

    public CyNode getProxied() {
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
