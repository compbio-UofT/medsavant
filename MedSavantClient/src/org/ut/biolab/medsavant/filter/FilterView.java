/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.api.FilterStateAdapter;


/**
 *
 * @author mfiume
 */
public abstract class FilterView extends JPanel {

    private final String title;
    protected final int queryID;

    public FilterView(String title, int queryID) {
        this.title = title;
        this.queryID = queryID;
    }

    public String getTitle() {
        return title;
    }
    
    public abstract FilterStateAdapter saveState();

    /**
     * Give derived classes a chance to clean up when the filter instance is being removed.
     */
    public void cleanup() {
    }

    /**
     * Many filters have a single parameter called "value", which can have multiple values.  This method packs them all into a string
     * array, suitable for passing to the <c>FilterState</c> constructor.
     *
     * @param applied the values which are applied (e.g. ["chr1", "chr2"])
     */
    public static List<String> wrapValues(Collection applied) {
        List<String> result = new ArrayList<String>();
        if (applied != null && !applied.isEmpty()) {
            for (Object val: applied) {
                result.add(val.toString());
            }
        }
        return result;
    }

}
