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

package org.ut.biolab.medsavant.plugin;

/**
 * Exception thrown when the plugin cannot be installed because it is the wrong version.
 * Either a) it has the wrong SDK version, or b) a newer version of the same plugin is already installed,
 * or c) the file in question is not a plugin at all.
 * @author tarkvara
 */
public class PluginVersionException extends Exception {
    public PluginVersionException(String problem) {
        super(problem);
    }
}
