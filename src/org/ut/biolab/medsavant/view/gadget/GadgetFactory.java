/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.dashboard.Gadget;
import fiume.table.SearchableTablePanel;
import java.util.Vector;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.model.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.gadget.chart.ChartPanel;
import org.ut.biolab.medsavant.view.gadget.filter.FilterPanel;

/**
 *
 * @author mfiume
 */
public class GadgetFactory {

    public static Gadget createFilterGadget() {
        return new GenericGadget("Filters",new GadgetContentGenerator() {
            public JComponent generateGadgetContent() {
                return new FilterPanel();
            }
        });
    }

    public static Gadget createResultsGadget() {
        return new GenericGadget("Results",new GadgetContentGenerator() {
            public JComponent generateGadgetContent() {
                Vector records = Util.getVariantRecordsVector(ResultController.getVariantRecords());
                JPanel p = new SearchableTablePanel(records, VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses());
                return p;
            }
        });
    }

    public static Gadget createChartGadget() {
        return new GenericGadget("Chart",new GadgetContentGenerator() {
            public JComponent generateGadgetContent() {
                return new ChartPanel();
            }
        });
    }




}
