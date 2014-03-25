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

import javax.swing.JFrame;
import pedviz.algorithms.Sugiyama;
import pedviz.graph.Graph;
import pedviz.loader.CsvGraphLoader;
import pedviz.view.GraphView2D;
import pedviz.view.rules.ShapeRule;
import pedviz.view.symbols.SymbolSexFemale;
import pedviz.view.symbols.SymbolSexMale;

/**
 *
 * @author mfiume
 */
public class PedVizExample {

    public static void main(String[] args) {

        // Step 1
        Graph graph = new Graph();
        CsvGraphLoader loader = new CsvGraphLoader("/Users/mfiume/Desktop/pedigree1.csv", ",");
        loader.setSettings("PID", "MOM", "DAD");
        loader.load(graph);

        // Step 2
        Sugiyama s = new Sugiyama(graph);
        s.run();

        // Creates a frame
        JFrame frame = new JFrame("Tutorials - Example 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Step 3
        GraphView2D view = new GraphView2D(s.getLayoutedGraph());
        frame.add(view.getComponent());

        //Step 4 - define rules
        view.addRule(new ShapeRule("sex", "1", new SymbolSexMale()));
        view.addRule(new ShapeRule("sex", "2", new SymbolSexFemale()));

        frame.setVisible(true);
    }
}
