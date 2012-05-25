/*
 *    Copyright 2011-2012 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class ClientMiscUtils extends MiscUtils {
    private static final Log LOG = LogFactory.getLog(ClientMiscUtils.class);
    public static final String GENDER_MALE = "Male";
    public static final String GENDER_FEMALE = "Female";
    public static final String GENDER_UNKNOWN = "Undesignated";

    public static String genderToString(int gender) {
        switch(gender) {
            case 1:
                return GENDER_MALE;
            case 2:
                return GENDER_FEMALE;
            default:
                return GENDER_UNKNOWN;
        }
    }

    public static int stringToGender(String gender) {
        if(gender.equals(GENDER_MALE)) {
            return 1;
        } else if (gender.equals(GENDER_FEMALE)) {
            return 2;
        } else {
            return 0;
        }
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static double getDouble(Object o) {
        if(o instanceof Double) {
            return (Double)o;
        } else if (o instanceof Integer) {
            return ((Integer)o).doubleValue();
        } else if (o instanceof Long) {
            return ((Long)o).doubleValue();
        } else if (o instanceof Float) {
            return ((Float)o).doubleValue();
        } else {
            return -1;
        }
    }

    /**
     * Displays an error message to the user appropriately.
     *
     * @param ex 
     */
    public static void reportError(String message, Throwable t) {
        message = String.format(message, getMessage(t));
        LOG.error(message, t);
        if (!checkSQLException(t)) {
            DialogUtils.displayException("MedSavant", message, t);
        }
    }

    public static boolean checkSQLException(Throwable t) {
        if ((t instanceof SQLException) && (t.getMessage().contains("Unknown column") || t.getMessage().contains("doesn't exist"))) {
            DialogUtils.displayErrorMessage("<html>It appears that the database structure has been modified.<br>Please log back in for the changes to take effect.</html>", t);
            LoginController.logout();
            return true;
        }
        return false;
    }

    public static Map<Object, List<String>> modifyGenderMap(Map<Object, List<String>> original) {
        Map<Object, List<String>> result = new HashMap<Object, List<String>>();
        for (Object key : original.keySet()) {
            String s;
            if (key instanceof Long || key instanceof Integer) {
                s = ClientMiscUtils.genderToString(ClientMiscUtils.safeLongToInt((Long) key));
            } else {
                s = ClientMiscUtils.GENDER_UNKNOWN;
            }
            if (result.get(s) == null) {
                result.put(s, original.get(key));
            } else {
                result.get(s).addAll(original.get(key));
            }
        }
        return result;
    }
}
