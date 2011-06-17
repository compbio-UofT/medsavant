/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview.genetics;

import java.awt.Color;

/**
 *
 * @author mfiume
 */
public class RangeAnnotation {
    private final long end;
    private final long start;
    private final Color color;

    public RangeAnnotation(long start, long end, Color c) {
        this.start = start;
        this.end = end;
        this.color = c;
    }

    public Color getColor() {
        return color;
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }
}
