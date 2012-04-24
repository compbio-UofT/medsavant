/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.record;

import org.ut.biolab.medsavant.model.Chromosome;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class Genome {

    private final List<Chromosome> chromosomes;

    public Genome(List<Chromosome> chrs) {
        this.chromosomes = chrs;
    }

    public List<Chromosome> getChromosomes() {
        return chromosomes;
    }
}
