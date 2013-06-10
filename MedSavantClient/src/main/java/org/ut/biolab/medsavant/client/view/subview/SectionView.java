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
import javax.swing.ImageIcon;
import javax.swing.JPanel;


/**
 *
 * @author mfiume
 */
public abstract class SectionView {

    private final String name;

    protected SectionView(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public abstract SubSectionView[] getSubSections();

    public JPanel[] getPersistentPanels() {
        return null;
    }

    public Component[] getSectionMenuComponents() {
        return null;
    }

    public abstract ImageIcon getIcon();
}
