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
package org.ut.biolab.medsavant.client.view.component;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.shared.model.Gene;

/**
 * Convenience class for a gene selection panel, with support for drag-and-drop
 * to and/or from other gene selection panels.
 */
public class GeneSelectionPanel extends ListViewTablePanel {

    private static final Log LOG = LogFactory.getLog(GeneSelectionPanel.class);
    private static final String[] COLUMN_NAMES = new String[]{"Name", "Chromosome", "Start", "End"};
    private static final Class[] COLUMN_CLASSES = new Class[]{String.class, String.class, Integer.class, Integer.class};
    private final DataFlavor geneSetFlavor;
    private final boolean exportEnabled;
    private final boolean importEnabled;

    public GeneSelectionPanel(boolean dragSource, boolean dragTarget) {
        super(new Object[0][0], COLUMN_NAMES, COLUMN_CLASSES, new int[0]);
        setFontSize(10);
        geneSetFlavor = new DataFlavor(Set.class, "GeneSet");

        getTable().setDragEnabled(dragSource || dragTarget);
        getTable().setDropMode(DropMode.ON);

        exportEnabled = dragSource;
        importEnabled = dragTarget;

        if (dragSource || dragTarget) {
            getTable().setTransferHandler(new GeneSetTransferHandler());
            getTable().setFillsViewportHeight(true);
        }
    }

    public Set<Gene> getSelectedGenes() {
        Set<Gene> selectedGenes = new CopyOnWriteArraySet<Gene>();

        for (int row : getSelectedRows()) {
            Object[] rowData = getRowData(row);
            String geneName = (String) rowData[0];
            String chrom = (String) rowData[1];
            int start = (Integer) rowData[2];
            int end = (Integer) rowData[3];

            Gene g = new Gene(geneName, chrom, start, end);
            selectedGenes.add(g);
        }

        return selectedGenes;
    }

    @Override
    //Use gene name as a key to identify rows.  
    protected Object getKey(Object[] row) {
        return row[0];
    }

    protected void dragAndDropAddGenes(Set<Gene> geneSet) {
        geneSet.removeAll(keyRowIndexMap.keySet());


        Object[][] newData = new Object[geneSet.size()][];
        int i = 0;
        for (Gene gene : geneSet) {
            newData[i++] = new Object[]{
                gene.getName(),
                gene.getChrom(),
                new Integer(gene.getStart()),
                new Integer(gene.getEnd())
            };
        }

        addRows(newData);
    }

    protected void dragAndDropRemoveKeys(Set<Object> keySet) {
        removeRows(keySet);
    }

    private class GeneSetTransferHandler extends TransferHandler {

        @Override
        public int getSourceActions(JComponent c) {
            if (exportEnabled) {
                return MOVE;
            } else {
                return NONE;
            }

        }

        @Override
        public Transferable createTransferable(JComponent c) {
            //return new StringSelection(c.getSelection());
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{geneSetFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor df) {
                    return df.equals(geneSetFlavor);
                }

                @Override
                public Object getTransferData(DataFlavor df) throws UnsupportedFlavorException, IOException {
                    if (!isDataFlavorSupported(df)) {
                        return false;
                    }
                    return getSelectedGenes();
                }
            };
        } //end createTransferable

        @Override
        public void exportDone(JComponent c, Transferable t, int action) {
            if (action == MOVE) {
                dragAndDropRemoveKeys(getSelectedKeys());
            }
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            return importEnabled && support.isDataFlavorSupported(geneSetFlavor)
                    && ((MOVE & support.getSourceDropActions()) == MOVE);
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            Transferable t = support.getTransferable();
            try {
                Set<Gene> genes = (Set<Gene>) (t.getTransferData(geneSetFlavor));
                dragAndDropAddGenes(genes);
                return true;
            } catch (UnsupportedFlavorException ex) {
                LOG.error("Unsupported drag-and-drop transfer flavor: " + ex);
            } catch (IOException ex) {
                LOG.error(ex);
            }
            return false;
        }
    } //end ExportTransferHandler
}