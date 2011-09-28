/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import java.sql.SQLException;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.oldcontroller.FilterController;
import org.ut.biolab.medsavant.db.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.db.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;

/**
 *
 * @author AndrewBrook
 */
public class MedSwingWorker extends SwingWorker implements FiltersChangedListener {
    
    public MedSwingWorker(){
        FilterController.addActiveFilterListener(this);        
    }

    @Override
    protected Object doInBackground() throws Exception { 
        return null; 
    }

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        if(!this.isDone()){
            this.cancel(true);
        }    
    }
    
}
