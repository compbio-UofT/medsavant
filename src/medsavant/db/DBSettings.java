/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package medsavant.db;

/**
 *
 * @author mfiume
 */
public class DBSettings {

    public static final String DRIVER = "com.mysql.jdbc.Driver";

    public static final String DB_USER_NAME = "root";
    public static final String DB_PASSWORD = "";
    public static final String DB_HOST = "localhost";
    public static final String DB_PORT = "5029";
    public static final String DB_NAME = "medsavant";
    public static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    //public static final String TABLE_VARIANT_ANNOTATION = "annotation_variant";
    //public static final String TABLE_VARIANT = "variant";
}
