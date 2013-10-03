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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.ut.biolab.medsavant.client.util.ThreadController;
import org.ut.biolab.medsavant.client.view.Menu;
import org.ut.biolab.medsavant.client.view.ViewController;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume
 */
public abstract class SubSectionView {

    private final SectionView parent;
    protected final String pageName;
    protected boolean loaded;
    private boolean updateRequired = true;
    private JButton undockButton;
    private JFrame undockedFrame;

    public enum DockState {

        UNDOCKED, UNDOCKING, DOCKED
    };

    public void focusUndockedFrame() {
        undockedFrame.toFront();
    }
    private DockState dockState = DockState.DOCKED;

    public DockState getDockState() {
        return dockState;
    }

    private void dock(JFrame undockedFrame) {
        undockButton.setText("Undock Genome Browser");

        //undockButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.UNDOCK));
        undockedFrame.dispose();
        undockedFrame = null;
        dockState = DockState.DOCKED;
        ViewController.getInstance().refreshView();
    }

    private JFrame undock() {
        dockState = DockState.UNDOCKING;
        undockButton.setText("Dock Genome Browser");
        //undockButton.setIcon(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.DOCK));
        //undockButton = new JButton("Undock Genome Browser");

        undockedFrame = new JFrame("Savant Browser");

        undockedFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        undockedFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                dock(undockedFrame);
            }
        });


        JPanel cc = new JPanel();
        cc.setBackground(ViewUtil.getBGColor());
        cc.setLayout(new BorderLayout());
        undockedFrame.add(cc);

        Menu menu = ViewController.getInstance().getMenu();
        menu.refreshSubSection(cc, this);

        undockedFrame.pack();
        undockedFrame.setLocationRelativeTo(null);
        undockedFrame.setVisible(true);
        undockedFrame.repaint();

        dockState = DockState.UNDOCKED;

        ViewController.getInstance().refreshView();
        return undockedFrame;
    }

    protected JButton getUndockButton() {
        if (undockButton == null) {
            //ImageIcon img = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.UNDOCK);
            undockButton = new JButton("Undock Genome Browser");
            undockButton.putClientProperty( "JButton.buttonType", "segmentedTextured" );
            undockButton.addActionListener(new ActionListener() {
                private JFrame undockedFrame;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    if (dockState == DockState.DOCKED) {
                        undockedFrame = undock();
                    } else if (dockState == DockState.UNDOCKED) {
                        dock(undockedFrame);
                    }
                }
            });
        }
        return undockButton;
    }

    public void setUpdateRequired(boolean required) {
        updateRequired = required;
    }

    public boolean isUpdateRequired() {
        return updateRequired;
    }

    public SubSectionView(SectionView parent, String page) {
        this.parent = parent;
        pageName = page;
    }

    /**
     * @return the view that should be shown docked in the main content panel
     * within the MedSavant frame. Differs from getView() only if this view is
     * undocked.
     */
    public JPanel getDockedView() {
        if (dockState != DockState.UNDOCKED) {
            return getView();
        } else {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(Box.createHorizontalGlue());

            p.add(new JLabel(pageName + " view is undocked into a separate window..."));

            p.add(Box.createHorizontalGlue());
            return p;
        }
    }

    /**
     * @return the view corresponding to this subsection.
     */
    public abstract JPanel getView();

    /* Clears the selection in this subsectionview, if applicable.*/
    public void clearSelection(){
        //by default, do nothing.
    }

    public Component[] getSubSectionMenuComponents() {
        return null;
    }

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
