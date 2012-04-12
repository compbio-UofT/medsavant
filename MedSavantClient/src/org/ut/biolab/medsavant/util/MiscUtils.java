/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.view.util.DialogUtils;

/**
 *
 * @author Andrew
 */
public class MiscUtils {

    public static String GENDER_MALE = "Male";
    public static String GENDER_FEMALE = "Female";
    public static String GENDER_UNKNOWN = "Undesignated";

    /*
    public static void setFrameVisibility(String frameKey, boolean isVisible, DockingManager m) {
        DockableFrame f = m.getFrame(frameKey);
        if (isVisible) {
            m.showFrame(frameKey);
        } else {
            m.hideFrame(frameKey);
        }
    }
     *
     */

    public static String genderToString(int gender){
        switch(gender){
            case 1:
                return GENDER_MALE;
            case 2:
                return GENDER_FEMALE;
            default:
                return GENDER_UNKNOWN;
        }
    }

    public static int stringToGender(String gender){
        if(gender.equals(GENDER_MALE)){
            return 1;
        } else if (gender.equals(GENDER_FEMALE)){
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

    public static double getDouble(Object o){
        if(o instanceof Double){
            return (Double)o;
        } else if (o instanceof Integer){
            return ((Integer)o).doubleValue();
        } else if (o instanceof Long){
            return ((Long)o).doubleValue();
        } else if (o instanceof Float){
            return ((Float)o).doubleValue();
        } else {
            return -1;
        }
    }

    public static void checkSQLException(SQLException e){

        e.printStackTrace();

        if(e instanceof MySQLSyntaxErrorException && (e.getMessage().contains("Unknown column") || e.getMessage().contains("doesn't exist"))){
            DialogUtils.displayErrorMessage("<HTML>It appears that the database structure has been modified. <BR>Please log back in for the changes to take effect.</HTML>", e);
            LoginController.logout();
        }
    }

    public static Map<Object, List<String>> modifyGenderMap(Map<Object, List<String>> original) {
        Map<Object, List<String>> result = new HashMap<Object, List<String>>();
        for (Object key : original.keySet()) {
            String s;
            if (key instanceof Long || key instanceof Integer) {
                s = MiscUtils.genderToString(MiscUtils.safeLongToInt((Long) key));
            } else {
                s = MiscUtils.GENDER_UNKNOWN;
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
