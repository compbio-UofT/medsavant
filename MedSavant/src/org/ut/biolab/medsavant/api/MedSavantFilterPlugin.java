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

package org.ut.biolab.medsavant.api;

import java.util.Map;
import javax.swing.JPanel;
import org.ut.biolab.medsavant.plugin.MedSavantPlugin;


/**
 * Plugin which implements filtering.
 *
 * @author mfiume
 */
public abstract class MedSavantFilterPlugin extends MedSavantPlugin {

    /**
     * This method is called once during the lifecycle of each instance of the plugin
     * filter to give the filter instance a chance to set up its user interface.
     */
    public abstract void init(JPanel panel, int queryID);
    
    /**
     * This method is called once during the lifecycle of an instance of the plugin
     * filter to give the filter instance a chance to clean up after itself.
     */
    public abstract void cleanup(int queryID);
    
    public abstract FilterStateAdapter saveState(int queryID);
    
    public abstract void loadState(Map<String, String> values, int queryID);
    
}
