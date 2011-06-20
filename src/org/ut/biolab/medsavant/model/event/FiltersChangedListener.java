/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.model.event;

import java.sql.SQLException;
import medsavant.exception.AccessDeniedDatabaseException;
import medsavant.exception.FatalDatabaseException;

/**
 *
 * @author mfiume
 */
public interface FiltersChangedListener {

    public void filtersChanged() throws SQLException, FatalDatabaseException, AccessDeniedDatabaseException;

}
