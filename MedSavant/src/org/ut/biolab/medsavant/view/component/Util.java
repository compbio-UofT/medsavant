/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.component;

import java.util.ArrayList;
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

    static List<List> doubleVectorToDoubleList(Vector data) {
        List<List> result = new ArrayList<List>();
        for (Object v : data) {
            List l = new ArrayList<Object>();
            for (Object o : (Vector) v) {
                l.add(o);
            }
            result.add(l);
        }
        return result;
    }

}
