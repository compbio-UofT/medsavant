/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.ontology;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.jidesoft.grid.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.aggregate.AggregatePanel;
import org.ut.biolab.medsavant.filter.FilterController;
import org.ut.biolab.medsavant.geneset.GeneSetController;
import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.model.Gene;
import org.ut.biolab.medsavant.model.OntologyTerm;
import org.ut.biolab.medsavant.model.OntologyType;
import org.ut.biolab.medsavant.project.ProjectController;
import org.ut.biolab.medsavant.reference.ReferenceController;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.util.ThreadController;
import org.ut.biolab.medsavant.view.genetics.GeneticsFilterPage;


/**
 *
 * @author mfiume, tarkvara
 */
public class OntologyAggregatePanel extends AggregatePanel {

    private static final Log LOG = LogFactory.getLog(OntologyAggregatePanel.class);

    private JComboBox chooser;
    private JProgressBar progress;
    private TreeTable tree;

    private MedSavantWorker termFetcher;
    private VariantFetcher variantFetcher;

    public OntologyAggregatePanel(String page) {
        super(page);
        setLayout(new GridBagLayout());

        chooser = new JComboBox(OntologyListItem.DEFAULT_ITEMS);
        chooser.setMaximumSize(new Dimension(400, chooser.getMaximumSize().height));
        progress = new JProgressBar();
        progress.setPreferredSize(new Dimension(600, progress.getMaximumSize().height));
        progress.setStringPainted(true);

        JPanel banner = new JPanel();
        banner.setLayout(new GridBagLayout());
        banner.setBackground(new Color(245,245,245));
        banner.setBorder(BorderFactory.createTitledBorder("Ontology"));

        tree = new TreeTable();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        banner.add(chooser, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        banner.add(progress, gbc);

        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        add(banner, gbc);

        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(tree), gbc);

        chooser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (termFetcher != null) {
                    termFetcher.cancel(true);
                    termFetcher = null;
                }
                recalculate();
            }
        });

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    createPopup().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }

    @Override
    public void recalculate() {
        if (chooser != null && chooser.getSelectedItem() != null) {
            // We only want to cancel the VariantFetcher.
            if (variantFetcher != null) {
                variantFetcher.cancel(true);
                variantFetcher = null;
            }
            
            // It's quite possible that we've successfully fetched all the terms, in which case the dead worker tells us that
            // we don't need to do it again.
            if (termFetcher == null) {
                tree.setModel(new OntologyTreeModel(null));

                progress.setIndeterminate(true);
                progress.setVisible(true);
                termFetcher = new MedSavantWorker<OntologyTerm[]>(pageName) {
                    @Override
                    protected OntologyTerm[] doInBackground() throws Exception {
                        return MedSavantClient.OntologyManager.getAllTerms(LoginController.sessionId, ((OntologyListItem)chooser.getSelectedItem()).getType());
                    }

                    @Override
                    protected void showProgress(double fraction) {
                    }

                    @Override
                    protected void showSuccess(OntologyTerm[] result) {
                        tree.setModel(new OntologyTreeModel(result));
                        TableColumn col = tree.getColumnModel().getColumn(3);
                        col.setCellRenderer(new NodeProgressRenderer());
                        progress.setVisible(false);
                    }
                };

                termFetcher.execute();
            } else {
                // Already have our terms.  Just reset and start a new variant fetcher.
                if (tree.getModel() != null) {
                    for (int i = 0; i < tree.getRowCount(); i++) {
                        OntologyNode rowNode = (OntologyNode)tree.getRowAt(i);
                        rowNode.resetCount();
                    }
                    OntologyTreeModel actualModel = (OntologyTreeModel)TableModelWrapperUtils.getActualTableModel(tree.getModel());
                    actualModel.geneCounts.clear();
                    actualModel.fireTableDataChanged();
                    variantFetcher = new VariantFetcher(actualModel);
                    variantFetcher.execute();
                }
            }
        }
    }

    private JPopupMenu createPopup() {
        JPopupMenu menu = new JPopupMenu();

        SortableTreeTableModel model = (SortableTreeTableModel)tree.getModel();
        final int[] selRows = tree.getSelectedRows();

        JMenuItem posItem = new JMenuItem(String.format("<html>Filter by %s</html>", selRows.length == 1 ? "Ontology Term <i>" + model.getValueAt(selRows[0], 0) + "</i>" : "Selected Ontology Terms"));
        posItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                ThreadController.getInstance().cancelWorkers(pageName);

                List<OntologyTerm> terms = new ArrayList<OntologyTerm>();
                SortableTreeTableModel model = (SortableTreeTableModel)tree.getModel();
                for (int r: selRows) {
                    OntologyNode rowNode = (OntologyNode)model.getRowAt(r);
                    terms.add(rowNode.term);
                }

                OntologyType ont = terms.get(0).getOntology();
                GeneticsFilterPage.getSearchBar().loadFilters(OntologyFilterView.wrapState(OntologyFilter.ontologyToTitle(ont), ont, terms, false));
            }

        });
        menu.add(posItem);

        return menu;
    }


    /**
     * Class which provides a tree-like data-structure for all terms within a given ontology.
     */
    private class OntologyTreeModel extends TreeTableModel {

        private final OntologyTerm[] allTerms;
        private final Map<OntologyTerm, OntologyTerm[]> allChildren = new HashMap<OntologyTerm, OntologyTerm[]>();
        private final Map<String, Integer> geneCounts = new HashMap<String, Integer>();

        public OntologyTreeModel(OntologyTerm[] terms) {
            allTerms = terms;

            if (terms != null) {
                for (OntologyTerm t: terms) {
                    if (t.getParentIDs() == null) {
                        OntologyNode node = new OntologyNode(t, this);
                        addRow(node);
                    }
                }
                variantFetcher = new VariantFetcher(this);
                variantFetcher.execute();

            }
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "ID";
                case 1:
                    return "Name";
                case 2:
                    return "Definition";
                case 3:
                    return "Variant Count";
            }
            return null;
        }

        private OntologyTerm[] getChildTerms(OntologyTerm term) {
            if (!allChildren.containsKey(term)) {
                OntologyTerm[] children = term.getChildren(allTerms);
                allChildren.put(term, children);
                return children;
            }
            return allChildren.get(term);
        }

    }

    /**
     * Class which represents a single term within the tree-model.
     */
    private class OntologyNode extends DefaultExpandableRow {

        private static final int COUNT_COLUMN = 3;

        private final OntologyTerm term;
        private final OntologyTreeModel model;
        private Set<String> uncountedGenes = new HashSet<String>();
        private int totalGenes;
        private int count;

        private OntologyNode(OntologyTerm t, OntologyTreeModel m) {
            term = t;
            model = m;
            addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (evt.getPropertyName().equals(DefaultExpandableRow.PROPERTY_EXPANDED)) {
                        if (isExpanded()) {
                            // If we add the children in the actual propertyChange method, they get added twice.  A JIDE bug?
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    OntologyTerm[] childTerms = model.getChildTerms(term);
                                    if (childTerms.length > 0 && getChildrenCount() == 0) {
                                        synchronized (model) {
                                            for (OntologyTerm t: childTerms) {
                                                model.addRow(OntologyNode.this, new OntologyNode(t, model));
                                            }
                                        }
                                    }
                                    // We use a new thread to push the new nodes onto the VariantFetcher in order to avoid
                                    // blocking the AWT thread.
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            for (Object child: getChildren()) {
                                                variantFetcher.push((OntologyNode)child);
                                            }
                                        }
                                    }.start();
                                }
                            });
                        }
                    }
                }
            });
        }

        @Override
        public Object getValueAt(int col) {
            switch (col) {
                case 0:
                    return term.getID();
                case 1:
                    return term.getName();
                case 2:
                    return term.getDef();
                case COUNT_COLUMN:
                    return count;
            }
            return null;
        }

        @Override
        public boolean hasChildren() {
            return model.getChildTerms(term).length > 0;
        }

        private void increment(String gene, Integer n) {
            if (uncountedGenes != null && uncountedGenes.contains(gene)) {
                if (n != null) {
                    count += n;
                }
                uncountedGenes.remove(gene);
                if (uncountedGenes.isEmpty()) {
                    uncountedGenes = null;
                }
                model.fireTableCellUpdated(model.getRowIndex(this), COUNT_COLUMN);
            }
        }
        
        private void resetCount() {
            LOG.info("Reset count for " + term);
            uncountedGenes = new HashSet<String>();
            count = 0;
        }
    }

    private class NodeProgressRenderer extends JPanel implements TableCellRenderer {

        private JLabel label;
        private JProgressBar bar;

        NodeProgressRenderer() {
            setLayout(new GridBagLayout());
            label = new JLabel() {
                @Override
                public Dimension getMinimumSize() {
                    return new Dimension(90, super.getMinimumSize().height);
                }
            };
            bar = new JProgressBar();
            bar.putClientProperty("JComponent.sizeVariant", "mini");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.weightx = 0.5;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 3, 3, 3);
            add(label, gbc);
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(bar, gbc);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object val, boolean selected, boolean focus, int row, int col) {
            OntologyNode node = (OntologyNode)((TreeTable)table).getRowAt(row);
            if (node.uncountedGenes == null) {
                // Fully loaded.
                label.setText(val.toString());
                bar.setVisible(false);
            } else {
                if (node.uncountedGenes.isEmpty()) {
                    // Still waiting to get our list of genes.
                    label.setText("Loading...");
                    bar.setIndeterminate(true);
                    bar.setStringPainted(false);
                    bar.setVisible(true);
                } else {
                    double prog = 1.0 - (double)node.uncountedGenes.size() / node.totalGenes;
                    label.setText("≥ " + val);
                    bar.setIndeterminate(false);
                    bar.setValue((int)(prog * 100.0));
                    bar.setString(String.format("%.1f%%", prog * 100.0));
                    bar.setStringPainted(true);
                    bar.setVisible(true);
                }
            }

            return this;
        }
    }

    private class VariantFetcher extends MedSavantWorker<Void> {

        private final Stack<OntologyNode> nodeStack = new Stack<OntologyNode>();
        private final Stack<String> geneStack = new Stack<String>();
        private final OntologyTreeModel model;

        private VariantFetcher(OntologyTreeModel m) {
            super(pageName);
            model = m;
            for (int i = 0; i < model.getRowCount(); i++) {
                push((OntologyNode)model.getRowAt(i));
            }
        }

        @Override
        protected void showProgress(double fraction) {
        }

        @Override
        protected void showSuccess(Void result) {
        }

        @Override
        protected Void doInBackground() throws Exception {
            while (!isCancelled()) {
                do {
                    while (!nodeStack.empty() && !isCancelled()) {
                        synchronized (nodeStack) {
                            fetchGenesForNodes();
                        }
                    }
                    if (!geneStack.empty()) {
                        String geneName = geneStack.pop();
                        Integer result = model.geneCounts.get(geneName);
                        if (result == null) {
                            Gene gene = GeneSetController.getInstance().getGene(geneName);
                            if (gene != null) {
                                result = MedSavantClient.VariantManager.getVariantCountInRange(
                                        LoginController.sessionId,
                                        ProjectController.getInstance().getCurrentProjectID(),
                                        ReferenceController.getInstance().getCurrentReferenceID(),
                                        FilterController.getInstance().getAllFilterConditions(),
                                        gene.getChrom(),
                                        gene.getStart(),
                                        gene.getEnd());
                            } else {
                                LOG.info(geneName + " referenced in " + model.allTerms[0].getOntology() + " not found in current gene set.");
                                result = 0;
                            }
                            model.geneCounts.put(geneName, result);
                        }

                        synchronized (model) {
                            for (Object node: model.getRows()) {
                                ((OntologyNode)node).increment(geneName, result);
                            }
                        }
                    }
                } while (!geneStack.empty() && !isCancelled());
                synchronized (nodeStack) {
                    nodeStack.wait();
                }
            }
            throw new InterruptedException();
        }

        private void fetchGenesForNodes() throws InterruptedException, SQLException, RemoteException {
            OntologyTerm[] terms = new OntologyTerm[nodeStack.size()];
            for (int i = 0; i < terms.length; i++) {
                terms[i] = nodeStack.get(i).term;
            }
            Map<OntologyTerm, String[]> genes = MedSavantClient.OntologyManager.getGenesForTerms(LoginController.sessionId, terms, ReferenceController.getInstance().getCurrentReferenceName());
            for (OntologyNode node: nodeStack) {
                String[] nodeGenes = genes.get(node.term);
                if (nodeGenes != null) {
                    node.uncountedGenes.addAll(Arrays.asList(nodeGenes));
                    node.totalGenes = nodeGenes.length;

                    for (String g: nodeGenes) {
                        Integer geneCount = model.geneCounts.get(g);
                        if (geneCount != null) {
                            // Already counted.
                            node.increment(g, geneCount);
                        } else {
                            push(g);
                        }
                    }
                } else {
                    // No genes for this term.  Mark it null so we know we're done.
                    node.uncountedGenes = null;
                    model.fireTableCellUpdated(model.getRowIndex(node), OntologyNode.COUNT_COLUMN);
                }
            }
            nodeStack.clear();
        }

        private void push(OntologyNode node) {
            synchronized (nodeStack) {
                nodeStack.push(node);
                nodeStack.notify();
            }
        }

        private void push(String g) {
            geneStack.push(g);
        }
    }
}
