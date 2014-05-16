/**
 * Copyright (c) 2014 Marc Fiume <mfiume@cs.toronto.edu>
 * Unauthorized use of this file is strictly prohibited.
 * 
 * All rights reserved. No warranty, explicit or implicit, provided.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, TITLE AND NON-INFRINGEMENT. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDERS OR ANYONE DISTRIBUTING THE SOFTWARE BE LIABLE
 * FOR ANY DAMAGES OR OTHER LIABILITY, WHETHER IN CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package org.ut.biolab.medsavant.shared.appapi;

import javax.swing.JPanel;
import org.ut.biolab.medsavant.shared.vcf.VariantRecord;


/**
 * Plugin which displays its contents in a JPanel managed by the Savant user-interface.
 * The canonical example is our own data table plugin.
 *
 * @author mfiume
 */
public abstract class MedSavantVariantInspectorApp extends MedSavantApp {
    public abstract void setVariantRecord(VariantRecord r);    
    public abstract String getName();
    public abstract JPanel getInfoPanel();
}
