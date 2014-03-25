/*
 * Copyright (C) 2014 University of Toronto, Computational Biology Lab.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.ut.biolab.medsavant.client.patient.pedigree;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import org.ut.biolab.medsavant.client.view.font.FontFactory;

import pedviz.view.NodeView;
import pedviz.view.symbols.Symbol2D;

/**
 * Draws the symbol for genotpyes.
 *
 * @author Luki
 *
 */
public class NameSymbol extends Symbol2D {

    private boolean showId = true;
    private boolean patientLabelsShown;

    /**
     * Creates a new GenotypeSymbol with the given collection of traits.
     *
     * @param traits Collection of traits.
     */
    public NameSymbol(boolean patientLabelsShown) {
        this.patientLabelsShown = patientLabelsShown;
    }

    @Override
    public void drawSymbol(Graphics2D g, Point2D.Float position, float size,
            Color border, Color fill, NodeView nodeview) {

        if (patientLabelsShown) {
            float top = position.y + (size / 2f) + 0.5f;

            Font oldfont = g.getFont();
            g.setColor(border);
            Font font = new Font(FontFactory.getGeneralFont().getFamily(), Font.PLAIN, 1);
            //Font font = new Font("default", 0, 1);
            g.setFont(font);
            float height = g.getFontMetrics().getHeight();

            float y = top + height;
            if (showId) {
                String text = nodeview.getNode().getId().toString();
                float width = g.getFontMetrics().stringWidth(text);
                g.drawString(text, (position.x - width / 2f),
                        (float) (y - (((int) (size * 0.5f)) / 2.0f)));
                y += height;
            }
            g.setFont(oldfont);
        }

    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public java.lang.Float getHeight() {
        return 0.5f + 4.0f * ((showId ? 1f : 0f));
    }
}
