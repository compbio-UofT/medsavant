/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.aggregate;

import javax.swing.JPanel;

/**
 *
 * @author tarkvara
 */
abstract class AggregatePanel extends JPanel {
    abstract void recalculate();
}
