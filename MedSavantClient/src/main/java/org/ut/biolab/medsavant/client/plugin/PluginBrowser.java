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
package org.ut.biolab.medsavant.client.plugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.jidesoft.grid.HierarchicalTable;
import com.jidesoft.grid.HierarchicalTableComponentFactory;
import com.jidesoft.grid.HierarchicalTableModel;

import org.ut.biolab.medsavant.client.api.Listener;


/**
 * Class which lets user select which plugins are available.
 *
 * @author mfiume, vwilliams, tarkvara
 */
public class PluginBrowser extends HierarchicalTable {

    private ProgramTableModel tableModel;
    private AppController pluginController = AppController.getInstance();

    public PluginBrowser() {
        tableModel = new ProgramTableModel();
        pluginController.addListener(tableModel);

        setModel(tableModel);
        setPreferredScrollableViewportSize(new Dimension(600, 400));
        setHierarchicalColumn(-1);
        setSingleExpansion(true);
        setName("Program Table");
        setShowGrid(false);
        setRowHeight(24);
        getTableHeader().setPreferredSize(new Dimension(0, 0));
        getColumnModel().getColumn(0).setPreferredWidth(500);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        HierarchicalTableComponentFactory factory = new HierarchicalTableComponentFactory() {
            @Override
            public Component createChildComponent(HierarchicalTable table, Object value, int row) {
                if (value instanceof AppDescriptor) {
                    return new ProgramPanel((AppDescriptor)value);
                }
                return null;
            }

            @Override
            public void destroyChildComponent(HierarchicalTable table, Component component, int row) {
            }
        };
        setComponentFactory(factory);
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int row = getSelectedRow();
                if (row != -1) {
                    expandRow(row);
                }
            }
        });
    }

    /**
     * Panel which displays full information about a single plugin.
     */
    class ProgramPanel extends JPanel {
        AppDescriptor program;

        public ProgramPanel(AppDescriptor program) {
            this.program = program;
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.gridheight = 2;
            add(new JLabel(program.getName()), gbc);

            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridheight = 1;
            gbc.weightx = 0.0;
            add(new JLabel("Version: " + program.getVersion()), gbc);

            gbc.gridy = 1;
            final JLabel statusLabel = new JLabel("Status: " + pluginController.getPluginStatus(program.getID()));
            add(statusLabel, gbc);

            JButton removeButton = new JButton("Uninstall");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JButton)e.getSource()).setEnabled(false);
                    pluginController.queuePluginForRemoval(ProgramPanel.this.program.getID());
                    statusLabel.setText("Status: " + pluginController.getPluginStatus(ProgramPanel.this.program.getID()));
                }
            });
            if (pluginController.isPluginQueuedForRemoval(program.getID())) {
                removeButton.setEnabled(false);
            }
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridheight = 2;
            add(removeButton, gbc);
        }
    }

    class ProgramTableModel extends AbstractTableModel implements HierarchicalTableModel, Listener<PluginEvent> {
        List<AppDescriptor> descriptors;

        ProgramTableModel() {
            descriptors = pluginController.getDescriptors();
        }

        @Override
        public int getRowCount() {
            return descriptors.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return descriptors.get(rowIndex).getName();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public boolean hasChild(int row) {
            return true;
        }

        @Override
        public boolean isExpandable(int row) {
            return true;
        }

        @Override
        public boolean isHierarchical(int row) {
            return false;
        }

        @Override
        public Object getChildValueAt(int row) {
            return descriptors.get(row);
        }

        @Override
        public void handleEvent(PluginEvent event) {
            descriptors = pluginController.getDescriptors();
            fireTableDataChanged();
        }
    }
}
