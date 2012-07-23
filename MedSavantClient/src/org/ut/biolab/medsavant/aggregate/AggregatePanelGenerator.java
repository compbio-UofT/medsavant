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

package org.ut.biolab.medsavant.aggregate;

import javax.swing.JPanel;


/**
 *
 * @author Nirvana Nursimulu
 */
abstract class AggregatePanelGenerator {
    
    /** Page for thread-management purposes. */
    protected final String pageName;

    protected boolean updateRequired;

    protected AggregatePanel panel;

    protected AggregatePanelGenerator(String page) {
        pageName = page;
    }

    public abstract String getName();
    
    /**
     * Get the panel for this aggregate, creating if necessary.
     */
    public AggregatePanel getPanel() {
        if (panel == null) {
            panel = generatePanel();
        } else {
            panel.recalculate();
        }
        return panel;
    }

    /**
     * Create the appropriate panel for this aggregate.
     */
    abstract AggregatePanel generatePanel();

    void run(boolean reset) {
        if (reset || updateRequired) {
            panel.recalculate();
        }
    }
    
    public void setUpdateRequired(boolean required) {
        updateRequired = required;
    }
}
