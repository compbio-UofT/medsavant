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
package org.ut.biolab.medsavant.client.view.genetics.inspector;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyVetoException;
import javax.swing.JPanel;

import com.jidesoft.pane.CollapsiblePane;
import com.jidesoft.pane.CollapsiblePanes;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JLabel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.view.util.ViewUtil;

/**
 *
 * @author mfiume, rammar
 */
public abstract class CollapsibleInspector extends JPanel implements Inspector {
    private static final Log LOG = LogFactory.getLog(CollapsibleInspector.class);
    private final JPanel container;
    private final CollapsiblePanes panesContainer;
    private final JPanel messageContainer;

    final static String MESSAGEPANEL = "msg";
    final static String PANESPANEL = "panes";

    public CollapsibleInspector() {

        container = ViewUtil.getClearPanel();
        container.setLayout(new CardLayout());

        messageContainer = ViewUtil.getClearPanel();
        messageContainer.setBorder(ViewUtil.getBigBorder());
        messageContainer.setLayout(new BorderLayout());

        panesContainer = new CollapsiblePanes();
        panesContainer.addExpansion();

        this.setLayout(new BorderLayout());
        this.add(container, BorderLayout.CENTER);

        container.add(panesContainer,PANESPANEL);
        container.add(messageContainer,MESSAGEPANEL);

        switchToMessage();
    }

    public final void switchToMessage() {
        CardLayout cl = (CardLayout)(container.getLayout());
        cl.show(container, MESSAGEPANEL);
    }

    public void setMessage(String msg, String helpTitle, String helpMessage) {
        JPanel messagePanel = ViewUtil.getClearPanel();//new JPanel();
        messagePanel.setBorder(ViewUtil.getHugeBorder());
        ViewUtil.applyVerticalBoxLayout(messagePanel);
        JLabel h2 = new JLabel(msg);
        messagePanel.add(Box.createVerticalGlue());
        messagePanel.add(ViewUtil.centerHorizontally(h2));
        messagePanel.add(Box.createVerticalStrut(3));
        messagePanel.add(ViewUtil.centerHorizontally(ViewUtil.getHelpButton(helpTitle, helpMessage)));
        messagePanel.add(Box.createVerticalGlue());
        setMessage(messagePanel);
    }

    public void setMessage(JPanel msg) {
        messageContainer.removeAll();
        messageContainer.add(msg,BorderLayout.CENTER);
    }

    public final void switchToPanes() {
        CardLayout cl = (CardLayout)(container.getLayout());
        cl.show(container, PANESPANEL);
    }

    @Override
    public abstract String getName();

    @Override
    public JPanel getContent() {
        return this;
    }

     protected void addSubInspector(SubInspector ipan) {
         addSubInspector(ipan,false);
     }

     protected void addComponent(Component c) {
         panesContainer.remove(panesContainer.getComponentCount() - 1);
         panesContainer.add(c);
         panesContainer.addExpansion();
     }


    protected void addSubInspector(SubInspector ipan, boolean collapsed) {

        // remove the previous expansion
        panesContainer.remove(panesContainer.getComponentCount() - 1);

        CollapsiblePane p = new CollapsiblePane(ipan.getName());
        ipan.setPaneParent(p);

        try {
            p.setCollapsed(collapsed);
        } catch (PropertyVetoException ex) {
        }
        p.setStyle(CollapsiblePane.PLAIN_STYLE);
        p.setLayout(new BorderLayout());
		p.setFocusPainted(false);

        LOG.debug("Adding subinspector...");
        p.add(ipan.getInfoPanel(), BorderLayout.CENTER);
        panesContainer.add(p);
        panesContainer.addExpansion();


    }
}
