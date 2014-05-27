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
package org.ut.biolab.medsavant.client.util;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.Gene;
import org.ut.biolab.medsavant.shared.model.GeneSet;

/**
 * Swing worker which fetches genes for a given gene set. Used by both
 * GenesDetailedView and RegionWizard. Note that this implementation fetches
 * genes, not transcripts.
 *
 * @author tarkvara
 */
public abstract class GeneFetcher extends MedSavantWorker<Gene[]> {

    private final GeneSet selectedSet;

    public GeneFetcher(GeneSet selectedSet, String page) {
        super(page);
        this.selectedSet = selectedSet;
    }

    @Override
    protected Gene[] doInBackground() throws Exception {
        return MedSavantClient.GeneSetManager.getGenes(LoginController.getSessionID(), this.selectedSet);
    }

    @Override
    protected void showSuccess(Gene[] result) {
        // List<Object[]> list = new ArrayList<Object[]>();
        Object[][] data = new Object[result.length][];
        for (int i = 0; i < result.length; i++) {
            Gene g = result[i];
            data[i] = new Object[]{g.getName(), g.getChrom(), g.getStart(), g.getEnd(), g.getCodingStart(), g.getCodingEnd()};
        }
        setData(data);
    }

    /**
     * Do something with the data we've just retrieved. Typically populating a
     * list.
     *
     * @param data data collected from genes
     */
    public abstract void setData(Object[][] data);
}
