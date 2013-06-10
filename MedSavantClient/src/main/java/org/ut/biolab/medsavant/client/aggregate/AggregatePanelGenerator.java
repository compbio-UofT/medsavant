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

package org.ut.biolab.medsavant.client.aggregate;


/**
 *
 * @author Nirvana Nursimulu
 */
public abstract class AggregatePanelGenerator {

    protected boolean updateRequired;
    protected AggregatePanel panel;

    /**
     * Get the panel for this aggregate, creating if necessary.
     */
    public AggregatePanel getPanel() {
        if (panel == null) {
            panel = generatePanel();
        }
        return panel;
    }

    /**
     * Create the appropriate panel for this aggregate.
     */
    public abstract AggregatePanel generatePanel();

    /**
     * If the <code>updateRequired</code> flag has been set, tell the panel to recalculate.
     */
    public void updateIfRequired() {
        if (updateRequired) {
            panel.recalculate();
        }
    }

    public void setUpdateRequired(boolean required) {
        updateRequired = required;
    }
}
