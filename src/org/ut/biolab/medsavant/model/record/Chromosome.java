/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.record;

/**
 *
 * @author mfiume
 */
public class Chromosome {
    private final long centromerepos;
    private final String name;
    private final String shortname;
    private final long length;

    public Chromosome(String name, String shortname, long centromerepos, long length) {
        this.name = name;
        this.shortname = shortname;
        this.length = length;
        this.centromerepos = centromerepos;
    }

    public long getCentromerepos() {
        return centromerepos;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String getShortname() {
        return shortname;
    }

}
