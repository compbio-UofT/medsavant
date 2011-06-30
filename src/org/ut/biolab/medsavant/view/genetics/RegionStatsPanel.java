/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.genetics;

import java.sql.SQLException;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.exception.FatalDatabaseException;
import org.ut.biolab.medsavant.exception.NonFatalDatabaseException;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;



/**
 *
 * @author Nirvana Nursimulu
 */
public class RegionStatsPanel extends JPanel implements FiltersChangedListener{

    public void filtersChanged() throws SQLException, FatalDatabaseException, NonFatalDatabaseException {
        
        // do something here.
    }
    

    
}
