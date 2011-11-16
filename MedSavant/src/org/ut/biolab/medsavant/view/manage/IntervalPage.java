package org.ut.biolab.medsavant.view.manage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jidesoft.utils.SwingWorker;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.controller.ReferenceController;
import org.ut.biolab.medsavant.controller.ThreadController;
import org.ut.biolab.medsavant.db.model.RegionSet;
import org.ut.biolab.medsavant.db.util.query.RegionQueryUtil;
import org.ut.biolab.medsavant.importfile.BedFormat;
import org.ut.biolab.medsavant.importfile.FileFormat;
import org.ut.biolab.medsavant.importfile.ImportDelimitedFile;
import org.ut.biolab.medsavant.importfile.ImportFileView;
import org.ut.biolab.medsavant.view.list.DetailedListEditor;
import org.ut.biolab.medsavant.view.list.SplitScreenView;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.GenericProgressDialog;

/**
 *
 * @author mfiume
 */
public class IntervalPage extends SubSectionView {

    int importID = 0;
    SplitScreenView view;
    
    public IntervalPage(SectionView parent) { 
        super(parent);
    }
    
    public String getName() {
        return "Region Lists";
    }

    public JPanel getView(boolean update) {
        view = new SplitScreenView(
                new IntervalListModel(), 
                new IntervalDetailedView(),
                new IntervalDetailedListEditor());
        
        return view;
    }
    
    public Component[] getBanner() {
        Component[] result = new Component[0];
        //result[0] = getAddCohortButton();
        return result;
    }
    
    @Override
    public void viewDidLoad() {
    }

    @Override
    public void viewDidUnload() {
        ThreadController.getInstance().cancelWorkers(getName());
    }
    
}
