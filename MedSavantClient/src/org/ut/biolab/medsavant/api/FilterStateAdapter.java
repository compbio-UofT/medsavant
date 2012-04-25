/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.api;

import java.util.Map;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState;

/**
 *
 * @author Andrew
 */
public class FilterStateAdapter extends FilterState {
    
    public FilterStateAdapter(MedSavantFilterPlugin p, Map<String, String> values){
        super(FilterType.PLUGIN, p.getTitle(), p.getClass().getPackage().getName(), values);
    }
    
}
