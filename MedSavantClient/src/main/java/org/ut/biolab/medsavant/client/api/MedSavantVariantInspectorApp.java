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
package org.ut.biolab.medsavant.client.api;

import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.client.view.genetics.inspector.SubInspector;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;


/**
 * Plugin which displays its contents in a JPanel managed by the Savant user-interface.
 * The canonical example is our own data table plugin.
 *
 * @author mfiume
 */
public abstract class MedSavantVariantInspectorApp extends MedSavantApp {

    public abstract void setVariantRecord(VariantRecord r);
    public abstract SubInspector getSubInspector();

}
