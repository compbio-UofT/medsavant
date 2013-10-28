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
package org.ut.biolab.medsavant.client.variant;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.jidesoft.dialog.ButtonEvent;
import com.jidesoft.dialog.ButtonNames;
import com.jidesoft.wizard.WizardDialog;
import java.awt.Color;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;

/**
 * Code shared by VariantWorkers which update the variant tables, either by
 * uploading variants or by removing them. Unlike PublicationWorkers, these 1)
 * grab a database lock, and 2) enable the publishing UI upon success.
 *
 * @author tarvara
 */
public abstract class UpdateWorker extends VariantWorker {

    private static final Log LOG = LogFactory.getLog(UpdateWorker.class);
    protected int updateID = -1;

    protected UpdateWorker(String activity, WizardDialog wizard, JLabel progressLabel, JProgressBar progressBar, JButton workButton) {
        super(activity, wizard, progressLabel, progressBar, workButton);
        wizard.getCurrentPage().fireButtonEvent(ButtonEvent.DISABLE_BUTTON, ButtonNames.BACK);
    }

    @Override
    protected void showSuccess(Void result) {
        super.showSuccess(result);

        wizard.getCurrentPage().fireButtonEvent(ButtonEvent.ENABLE_BUTTON, ButtonNames.NEXT);

    }

    @Override
    protected void showFailure(Throwable ex) {
        super.showFailure(ex);
        if (ex instanceof InterruptedException) {
        }
    }
}
