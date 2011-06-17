/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.gadget;

import com.jidesoft.dashboard.Gadget;
import fiume.table.SearchableTablePanel;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.controller.ResultController;
import org.ut.biolab.medsavant.images.IconFactory;
import org.ut.biolab.medsavant.model.record.VariantRecordModel;
import org.ut.biolab.medsavant.util.Util;
import org.ut.biolab.medsavant.view.subview.genetics.ChartPanel;
import org.ut.biolab.medsavant.view.filter.FilterPanel;

/**
 *
 * @author mfiume
 */
public class GadgetFactory {

    public static Gadget createFilterGadget() {
        GenericGadget g = new GenericGadget("Filters",new GadgetContentGenerator() {

            public JComponent generateGadgetContent() {
                return new FilterPanel();
            }
        });
        g.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER));
        g.setLargeIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.FILTER));
        return g;
    }

    public static Gadget createResultsGadget() {
        GenericGadget g = new GenericGadget("Results",new GadgetContentGenerator() {
            public JComponent generateGadgetContent() {
                Vector records;
                JPanel p;
                try {
                    records = Util.convertVariantRecordsToVectors(ResultController.getInstance().getFilteredVariantRecords());
                    p = new SearchableTablePanel(records, VariantRecordModel.getFieldNames(), VariantRecordModel.getFieldClasses());
                } catch (Exception ex) {
                    Logger.getLogger(GadgetFactory.class.getName()).log(Level.SEVERE, null, ex);
                    p = new JPanel();
                    p.add(new JLabel("Problem retrieving data"));
                }
                return p;
            }
        });
        g.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.RESULTS));
        g.setLargeIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.RESULTS));
        return g;
    }

    public static Gadget createChartGadget() {
        GenericGadget g = new GenericGadget("Chart",new GadgetContentGenerator() {
            public JComponent generateGadgetContent() {
                return new ChartPanel();
            }
        });
        g.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART));
        g.setLargeIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.CHART));
        return g;
    }




}
