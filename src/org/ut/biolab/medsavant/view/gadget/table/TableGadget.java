/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget.table;

import com.jidesoft.dashboard.AbstractGadget;
import com.jidesoft.dashboard.Gadget;
import com.jidesoft.dashboard.GadgetComponent;
import fiume.table.SearchableTablePanel;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.gadget.DockableFrameGadget;

/**
 *
 * @author mfiume
 */
public class TableGadget extends AbstractGadget {

    public TableGadget() {
        super("Table View");
    }

    public GadgetComponent createGadgetComponent() {
        final DockableFrameGadget gadget = new DockableFrameGadget(this);
        gadget.getContentPane().add(getContentPanel());
        return gadget;
    }

    private JPanel getContentPanel() {
        Vector records = Util.getVariantRecordsVector(ResultController.getVariantRecords());
        JPanel p = new SearchableTablePanel(records, VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses());
        return p;
    }

    public void disposeGadgetComponent(GadgetComponent gc) {
        return;
    }

}
