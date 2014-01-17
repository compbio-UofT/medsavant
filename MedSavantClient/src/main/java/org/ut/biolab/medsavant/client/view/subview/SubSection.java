/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
public abstract class SubSection {

    private final MultiSection parent;
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

    public SubSection(MultiSection parent, String page) {
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

    public MultiSection getParent() {
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
