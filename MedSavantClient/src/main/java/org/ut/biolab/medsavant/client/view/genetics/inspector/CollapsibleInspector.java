/*
 *    Copyright 2012 University of Toronto
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
 * @author mfiume
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

        LOG.debug("Adding subinspector...");
        p.add(ipan.getInfoPanel(), BorderLayout.CENTER);
        panesContainer.add(p);
        panesContainer.addExpansion();


    }
}
