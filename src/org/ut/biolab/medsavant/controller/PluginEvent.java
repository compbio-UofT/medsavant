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

package org.ut.biolab.medsavant.controller;

import org.ut.biolab.medsavant.plugin.MedSavantPlugin;


/**
 * Event which is fired by the PluginController when our plugin list changes.
 *
 * @author tarkvara
 */
public class PluginEvent {
    public enum Type {
        LOADED,
        QUEUED_FOR_REMOVAL,
        ERROR
    }

    private final Type type;
    private final String pluginID;

    /**
     * Constructor invoked by PluginController.
     */
    public PluginEvent(Type type, String pluginID) {
        this.type = type;
        this.pluginID = pluginID;
    }

    public Type getType() {
        return type;
    }

    /**
     * ID of plugin which was being loaded.
     */
    public String getID() {
        return pluginID;
    }

    /**
     * Plugin which was being loaded.  Will be null if the plugin did not successfully load.
     */
    public MedSavantPlugin getPlugin() {
        return PluginController.getInstance().getPlugin(pluginID);
    }
}
