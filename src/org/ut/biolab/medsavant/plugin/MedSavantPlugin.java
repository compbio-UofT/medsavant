/*
 *    Copyright 2010-2011 University of Toronto
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

package org.ut.biolab.medsavant.plugin;


/**
 * Base class for all Savant plugins.  Not much here yet.
 *
 * @author tarkvara
 */
public abstract class MedSavantPlugin {
    
    private PluginDescriptor descriptor;

    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Called by the PluginController after instantiating the plugin.
     * @param desc descriptor from which this plugin was created
     */
    public void setDescriptor(PluginDescriptor desc) {
        descriptor = desc;
    }

    public String getTitle() {
        return descriptor.getName();
    }
}
