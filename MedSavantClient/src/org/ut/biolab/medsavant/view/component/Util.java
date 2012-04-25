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
 * @author mfiume, Andrew
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
    
    public interface DataRetriever {
        public abstract List<Object[]> retrieve(int start, int limit);
        public abstract int getTotalNum();
        public abstract void retrievalComplete();
    }
    
    public static DataRetriever createPrefetchedDataRetriever(final List data){
        return new DataRetriever() {
            public List<Object[]> retrieve(int start, int limit) {
               return data.subList(start, Math.min(start+limit, data.size()));
            }
            public int getTotalNum() {
                return data.size();
            }
            public void retrievalComplete(){};
        };
    }

}
