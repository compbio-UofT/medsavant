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

import javax.swing.JPanel;


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

    public abstract FilterState saveState();

    /**
     * Give derived classes a chance to clean up when the filter instance is being removed.
     */
    public void cleanup() {
    }
}
