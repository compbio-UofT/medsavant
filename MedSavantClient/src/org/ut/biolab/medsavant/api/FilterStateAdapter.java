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

package org.ut.biolab.medsavant.api;

import java.util.Map;
import org.ut.biolab.medsavant.model.Filter;
import org.ut.biolab.medsavant.view.genetics.filter.FilterState;


/**
 *
 * @author Andrew
 */
public class FilterStateAdapter extends FilterState {
    
    public FilterStateAdapter(MedSavantFilterPlugin p, Map<String, String> values){
        super(Filter.Type.PLUGIN, p.getTitle(), p.getClass().getPackage().getName(), values);
    }
    
}
