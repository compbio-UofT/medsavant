/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ut.biolab.medsavant.view.subview;

import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author mfiume
 */
public abstract class SectionView {

    private JTabbedPane pane;

    public abstract String getName();

    public abstract Icon getIcon();

    public abstract SubSectionView[] getSubSections();

    public abstract JPanel[] getPersistentPanels();

    public Component[] getSectionMenuComponents() { return null; }

}
