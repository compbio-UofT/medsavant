/*
 *    Copyright 2011 University of Toronto
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

package medsavant.pathways;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;
import org.ut.biolab.medsavant.api.ProjectUtils;
import org.ut.biolab.medsavant.model.event.FiltersChangedListener;


/**
 * Filter plugin which lets us use the WikiPathways site to select genes for filtering.
 *
 * @author AndrewBrook
 */
public class PathwaysPlugin extends MedSavantFilterPlugin {
    private Map<Integer, FilterInstance> instances = new HashMap<Integer, FilterInstance>();
    
    /**
     * Create the user-interface which appears within the panel.
     *
     * @param host provided by MedSavant to host our plugin
     */
    @Override
    public void init(JPanel panel, int queryID) {
        instances.put(queryID, new FilterInstance(panel, queryID));
    }

    /**
     * Clean up when we know this instance of the plugin is no longer needed.
     */
    public void cleanup(int queryID) {
        instances.get(queryID).cleanup();
        instances.remove(queryID);
    }

    /**
     * Title under which this filter will appear in MedSavant's filter section.
     */
    @Override
    public String getTitle() {
        return "WikiPathways";
    }
}
