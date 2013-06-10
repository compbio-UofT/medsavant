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

package org.ut.biolab.medsavant.client.view.subview;

import java.awt.Component;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.util.ThreadController;


/**
 *
 * @author mfiume
 */
public abstract class SubSectionView {

    private final SectionView parent;
    protected final String pageName;
    protected boolean loaded;
    private boolean updateRequired = true;

    public void setUpdateRequired(boolean required){
        updateRequired = required;
    }

    public boolean isUpdateRequired(){
        return updateRequired;
    }

    public SubSectionView(SectionView parent, String page) {
        this.parent = parent;
        pageName = page;
    }

    public abstract JPanel getView();

    public Component[] getSubSectionMenuComponents() { return null; }

    public String getPageName() {
        return pageName;
    }

    public SectionView getParent() {
        return parent;
    }

    /**
     * Give derived classes a chance to initialise themselves after loading.
     */
    public void viewDidLoad() {
        loaded = true;
    }

    /**
     * Provide cleanup when unloading the view.
     */
    public void viewDidUnload() {
        loaded = false;
        ThreadController.getInstance().cancelWorkers(pageName);
    }
}
