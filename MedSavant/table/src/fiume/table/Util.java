/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fiume.table;

import java.util.List;
import java.util.Vector;

/**
 *
 * @author mfiume
 */
public class Util {

    public static Vector listToVector(List l) {
        Vector v = new Vector(l.size());
        v.addAll(l);
        return v;
    }

}
