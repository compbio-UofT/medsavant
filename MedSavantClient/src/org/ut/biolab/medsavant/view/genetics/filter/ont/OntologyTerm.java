package org.ut.biolab.medsavant.view.genetics.filter.ont;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ut.biolab.medsavant.view.component.Util.DataRetriever;

/**
 *
 * @author mfiume
 */
public class OntologyTerm {

    private String ID;
    private final Map<String, String> map;

    public OntologyTerm() {
        map = new HashMap<String, String>();
    }

    public String getID() {
        return this.ID;
    }

    public void addKVPair(String key, String value) {
        if (key.equals("id")) {
            this.ID = value;
        } else {
            map.put(key, value);
        }
    }

    public String getValueForKey(String key) {
        return map.get(key);
    }

    public String toString() {
        return this.ID;
    }
}
