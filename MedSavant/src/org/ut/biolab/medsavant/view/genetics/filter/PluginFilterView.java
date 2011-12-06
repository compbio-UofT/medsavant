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
package org.ut.biolab.medsavant.view.genetics.filter;

import javax.swing.JPanel;

import org.ut.biolab.medsavant.api.MedSavantFilterPlugin;

/**
 * Create a FilterView which presents the user-interface for a filter plugin.
 * 
 * @author tarkvara
 */
public class PluginFilterView {
    
    public static FilterView getFilterView(final MedSavantFilterPlugin plugin, final int queryID) {
        return new FilterView(plugin.getTitle(), getContentPanel(plugin, queryID)) {
            @Override
            public void cleanup() {
                plugin.cleanup(queryID);
            }

            @Override
            public FilterState saveState() {
                return plugin.saveState(queryID);
            }

        };
    }
    
    private static JPanel getContentPanel(MedSavantFilterPlugin plugin, int queryID) {
        JPanel container = new JPanel();
        plugin.init(container, queryID);
        return container;
    }
  
}
