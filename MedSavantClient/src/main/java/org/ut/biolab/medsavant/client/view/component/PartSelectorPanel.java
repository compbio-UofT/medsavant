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

package org.ut.biolab.medsavant.client.view.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;


/**
 * Implements the part selector UI, which presents two lists, allowing you to move parts from one list to the other.
 *
 * @author tarkvara
 */
@Deprecated
public class PartSelectorPanel extends JPanel {
    private ListViewTablePanel leftList, rightList;

    public PartSelectorPanel(ListViewTablePanel left, ListViewTablePanel right) {
        this.leftList = left;
        this.rightList = right;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(left, gbc);
        left.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    leftList.moveSelectedItems(rightList);
                    //ListViewTablePanel.moveSelectedItems(leftList, rightList);
                }
            }
        });

        JButton addButton = new JButton("Add →");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                leftList.moveSelectedItems(rightList);
                //ListViewTablePanel.moveSelectedItems(leftList, rightList);
            }
        });
        gbc.gridx = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.SOUTH;
        add(addButton, gbc);

        JButton removeButton = new JButton("← Remove");
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                rightList.moveSelectedItems(leftList);
                //moveSelectedItems(rightList, leftList);
            }
        });
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        add(removeButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(right, gbc);
        right.getTable().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    rightList.moveSelectedItems(leftList);
                    //moveSelectedItems(rightList, leftList);
                }
            }
        });
    }

   
}
