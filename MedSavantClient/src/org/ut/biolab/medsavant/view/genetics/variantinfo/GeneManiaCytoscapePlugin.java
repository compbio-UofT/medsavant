/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics.variantinfo;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.layout.CyLayoutAlgorithm;
import cytoscape.layout.CyLayouts;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import cytoscape.view.NetworkViewManager;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import org.genemania.plugin.LogUtils;
import org.genemania.plugin.cytoscape2.layout.FilteredLayout;

/**
 *
 * @author khushi
 */
public class GeneManiaCytoscapePlugin  extends CytoscapePlugin{
    private boolean activated;
    public GeneManiaCytoscapePlugin(){
        activate();
    }
    public void activate(){
        // Make sure we don't activate twice.
        if (activated) {
				return;
			}
        GeneManiaAction action = new GeneManiaAction();
        Cytoscape.getDesktop().getCyMenus().addAction(action);
        
    }
    public class GeneManiaAction extends CytoscapeAction{
        public GeneManiaAction(){
            super("GeneMANIA");
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                GenemaniaInfoRetriever g = new GenemaniaInfoRetriever();
                CyNetwork network = g.getGraph();
                CytoscapeUtils cy = new CytoscapeUtils(g.getNetworkUtils());
                Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, null);
		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
                CyNetworkView view = cy.getNetworkView(network);
		CyLayoutAlgorithm layout = CyLayouts.getLayout(FilteredLayout.ID);
		if (layout == null) {
			layout = CyLayouts.getDefaultLayout();
		}
		layout.doLayout(view); 
                NetworkViewManager viewManager = Cytoscape.getDesktop().getNetworkViewManager();
		JInternalFrame frame = viewManager.getInternalFrame(view);
                JPanel p = new JPanel();
                p.add(frame);
		try {
			frame.setMaximum(true);
		} catch (PropertyVetoException e) {
			LogUtils.log(getClass(), e);
		}
            } catch (Exception ex) {
            Logger.getLogger(GeneManiaCytoscapePlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
}
