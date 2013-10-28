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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 * Class which identifies a particular set of genes (e.g. hg18/RefSeq).
 *
 * @author tarkvara
 */
public class GeneSet implements Serializable {
    /** The associated genome (e.g. hg18) */
    private final String genome;

    /** The type of gene set (e.g. RefSeq) */
    private final String type;

    /** The number of genes in this set. */
    private final int size;

    public GeneSet(String genome, String type, int size) {
        this.genome = genome;
        this.type = type;
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("%s – %s Genes", genome, type);
    }

    public String getReference() {
        return genome;
    }

    public String getType() {
        return type;
    }

    public int getSize() {
        return size;
    }
}
