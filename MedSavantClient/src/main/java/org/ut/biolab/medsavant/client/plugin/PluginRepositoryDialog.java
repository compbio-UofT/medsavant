/*
 *    Copyright 2010-2012 University of Toronto
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
package org.ut.biolab.medsavant.client.plugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.jidesoft.grid.TreeTable;
import com.jidesoft.swing.TableSearchable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.ut.biolab.medsavant.client.settings.DirectorySettings;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.view.dialog.DownloadDialog;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.TreeBrowserEntry;
import org.ut.biolab.medsavant.client.view.util.TreeBrowserModel;


/**
 *
 * @author mfiume
 */
public class PluginRepositoryDialog extends JDialog {
    private static final Log LOG = LogFactory.getLog(PluginRepositoryDialog.class);
    private static final TableCellRenderer FILE_RENDERER = new FileRowCellRenderer();

    private TreeTable table;

    /**
     * Instantiate a plugin repository browser and let the user select from it.
     * Typically this is invoked from the PluginManagerDialog.
     *
     * @param parent parent window
     * @param title window title
     * @param buttonText text of button (typically "Install")
     * @param xmlFile plugin.xml file which defines repository entries
     */
    PluginRepositoryDialog(Window parent, String title, String buttonText, File xmlFile) throws JDOMException, IOException {
        super(parent, title, Dialog.ModalityType.APPLICATION_MODAL);

        setResizable(true);
        setLayout(new BorderLayout());
        add(getCenterPanel(getDownloadTreeRows(xmlFile)), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        bottomBar.add(cancelButton);

        JButton installButton = new JButton(buttonText);
        installButton.putClientProperty( "JButton.buttonType", "default" );
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadSelectedItem(false);
            }
        });
        bottomBar.add(installButton);

        add(bottomBar, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(800, 500));
        pack();

        setLocationRelativeTo(parent);
        ClientMiscUtils.registerCancelButton(cancelButton);
    }

    private void downloadSelectedItem(boolean ignoreBranchSelected) {
        TreeBrowserEntry r = (TreeBrowserEntry) table.getRowAt(table.getSelectedRow());
        if (r != null && r.isLeaf()) {
            DownloadDialog dd = new DownloadDialog(this, true);
            dd.downloadFile(r.getURL(), DirectorySettings.getPluginsDirectory(), null);

            // When the download is complete, we hide the dialog.  This makes its
            // behaviour more parallel to Install from File.
            setVisible(false);
            if (dd.getDownloadedFile() != null) {
                try {
                    AppController.getInstance().installPlugin(dd.getDownloadedFile());
                } catch (Throwable x) {
                    DialogUtils.displayException("Installation Error", String.format("<html>Unable to install <i>%s</i>: %s.</html>", dd.getDownloadedFile().getName(), x), x);
                }
            }
        } else {
            if (!ignoreBranchSelected) {
                DialogUtils.displayMessage("Please select a file");
            }
        }
    }

    private static TreeBrowserEntry parseDocumentTreeRow(Element root) {
        if (root.getName().equals("branch")) {
            List<TreeBrowserEntry> children = new ArrayList<TreeBrowserEntry>();
            for (Object o : root.getChildren()) {
                Element c = (Element) o;
                children.add(parseDocumentTreeRow(c));
            }
            return new TreeBrowserEntry(root.getAttributeValue("name"), children);
        } else if (root.getName().equals("leaf")) {
            try {
                return new TreeBrowserEntry(root.getAttributeValue("name"), root.getChildText("type"), root.getChildText("description"), new URL(root.getChildText("url")), root.getChildText("size"));
            } catch (MalformedURLException ex) {
                LOG.error("Error parsing plugin index.", ex);
            }
        }
        return null;
    }

    public static class FileRowCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof TreeBrowserEntry) {
                TreeBrowserEntry fileRow = (TreeBrowserEntry) value;
                JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                        fileRow.getName(),
                        isSelected, hasFocus, row, column);
                label.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
                return label;
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }

    private static List<TreeBrowserEntry> getDownloadTreeRows(File f) throws JDOMException, IOException {
        List<TreeBrowserEntry> roots = new ArrayList<TreeBrowserEntry>();
        Document d = new SAXBuilder().build(f);
        Element root = d.getRootElement();
        TreeBrowserEntry treeroot = parseDocumentTreeRow(root);
        roots.add(treeroot);
        return roots;
    }

    public final Component getCenterPanel(List<TreeBrowserEntry> roots) {
        table = new TreeTable(new TreeBrowserModel(roots) {
            @Override
            public String[] getColumnNames() {
                return new String[] { "Name" };
            }
        });
        table.setSortable(true);
        table.setRespectRenderPreferredHeight(true);

        // configure the TreeTable
        table.setExpandAllAllowed(true);
        table.setShowTreeLines(false);
        table.setSortingEnabled(false);
        table.setRowHeight(18);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //table.expandAll();
        table.expandFirstLevel();
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    downloadSelectedItem(true);
                }
            }
        });

        // do not select row when expanding a row.
        table.setSelectRowWhenToggling(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(200);
        //table.getColumnModel().getColumn(1).setPreferredWidth(300);
        //table.getColumnModel().getColumn(2).setPreferredWidth(50);
        //table.getColumnModel().getColumn(3).setPreferredWidth(100);
        //table.getColumnModel().getColumn(4).setPreferredWidth(50);

        table.getColumnModel().getColumn(0).setCellRenderer(FILE_RENDERER);

        // add searchable feature
        TableSearchable searchable = new TableSearchable(table) {

            @Override
            protected String convertElementToString(Object item) {
                if (item instanceof TreeBrowserEntry) {
                    return ((TreeBrowserEntry) item).getType();
                }
                return super.convertElementToString(item);
            }
        };
        searchable.setMainIndex(0); // only search for name column

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(800, 500));
        return panel;
    }
}
