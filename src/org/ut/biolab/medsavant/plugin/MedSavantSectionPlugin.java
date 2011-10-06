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

import org.ut.biolab.medsavant.view.subview.SectionView;


/**
 * Plugin which displays its contents in a JPanel managed by the Savant user-interface.
 * The canonical example is our own data table plugin.
 *
 * @author mfiume
 */
public abstract class MedSavantSectionPlugin extends MedSavantPlugin {

    /**
     * This method is called once during application life cycle to allow a third-party
     * plugin to initialize and show itself.
     */
    public abstract SectionView getView();
    
}
