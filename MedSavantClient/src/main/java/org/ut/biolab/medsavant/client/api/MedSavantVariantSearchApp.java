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

package org.ut.biolab.medsavant.client.api;

import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.plugin.MedSavantApp;
import org.ut.biolab.mfiume.query.medsavant.complex.ComprehensiveConditionGenerator;


/**
 * Plugin which implements filtering.
 *
 * @author mfiume
 */
public abstract class MedSavantVariantSearchApp extends MedSavantApp {

    /**
     * This method is called once during the lifecycle of each instance of the plugin
     * filter to give the filter instance a chance to set up.
     */
    public abstract void init();

    public abstract ComprehensiveConditionGenerator getSearchConditionGenerator();

}
