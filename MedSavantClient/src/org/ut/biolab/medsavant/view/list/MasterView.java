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

package org.ut.biolab.medsavant.view.list;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.login.LoginController;
import org.ut.biolab.medsavant.util.ClientMiscUtils;
import org.ut.biolab.medsavant.util.MedSavantWorker;
import org.ut.biolab.medsavant.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author tarkvara
 */
public class MasterView extends JPanel {

    private static final Log LOG = LogFactory.getLog(MasterView.class);

    //TODO: handle limits better!
    static final int LIMIT = 10000;

    private static final String CARD_WAIT = "wait";
    private static final String CARD_SHOW = "show";
    private static final String CARD_ERROR = "error";

    private final String pageName;
    private final DetailedListModel detailedModel;
    private final DetailedView detailedView;
    private final DetailedListEditor detailedEditor;

    Object[][] data;
    private final JPanel showCard;
    private final JLabel errorMessage;
    ListViewTablePanel stp;
    //private int limit = 10000;
    private RowSelectionGrabber selectionGrabber;
    private JPanel buttonPanel;

    public MasterView(String page, DetailedListModel model, DetailedView view, DetailedListEditor editor) {
        pageName = page;
        detailedModel = model;
        detailedView = view;
        detailedEditor = editor;

        setLayout(new CardLayout());

        WaitPanel wp = new WaitPanel("Getting list");
        wp.setBackground(ViewUtil.getTertiaryMenuColor());
        add(wp, CARD_WAIT);

        showCard = new JPanel();
        add(showCard, CARD_SHOW);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BorderLayout());
        errorMessage = new JLabel("An error occurred:");
        errorPanel.add(errorMessage, BorderLayout.NORTH);

        add(errorPanel, CARD_ERROR);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = GridBagConstraints.RELATIVE;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);

        // Only for SavedFiltersPanel
        if (detailedEditor.doesImplementLoading()) {
            JButton loadButton = ViewUtil.getTexturedButton("Load", null);
            ViewUtil.makeSmall(loadButton);
            loadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    detailedEditor.loadItems(selectionGrabber.getSelectedItems());
                }
            });

            gbc.gridwidth = GridBagConstraints.REMAINDER;
            buttonPanel.add(loadButton, gbc);
            gbc.gridx = GridBagConstraints.RELATIVE;
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.weightx = 0.0;
        }

        if (detailedEditor.doesImplementAdding()) {

            JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
            butt.setToolTipText("Add");
            butt.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    detailedEditor.addItems();
                    // In some cases, such as uploading/publishing variants, the addItems() method may have logged us out.
                    if (LoginController.getInstance().isLoggedIn()) {
                        refreshList();
                    }
                }
            });
            buttonPanel.add(butt, gbc);
        }

        if (detailedEditor.doesImplementImporting()) {

            JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.IMPORT));
            butt.setToolTipText("Import");
            butt.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    detailedEditor.importItems();
                    refreshList();
                }
            });
            buttonPanel.add(butt, gbc);
        }

        /*
        if (detailedEditor.doesImplementExporting()) {

            JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPORT));
            butt.setToolTipText("Export");
            butt.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    detailedEditor.exportItems();
                    refreshList();
                }
            });
            buttonPanel.add(butt, gbc);
        }
        */

        if (detailedEditor.doesImplementDeleting()) {
            JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
            butt.setToolTipText("Remove selected");
            butt.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    detailedEditor.deleteItems(selectionGrabber.getSelectedItems());
                    // In some cases, such as removing/publishing variants, the deleteItems() method may have logged us out.
                    if (LoginController.getInstance().isLoggedIn()) {
                        refreshList();
                    }
                }
            });
            buttonPanel.add(butt, gbc);
        }

        if (detailedEditor.doesImplementEditing()) {
            JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT));
            butt.setToolTipText("Edit selected");
            butt.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (selectionGrabber.getSelectedItems().size() > 0) {
                        detailedEditor.editItem(selectionGrabber.getSelectedItems().get(0));
                        refreshList();
                    } else {
                        DialogUtils.displayMessage("Please choose one item to edit.");
                    }
                }
            });
            buttonPanel.add(butt, gbc);
        }

        showWaitCard();
        fetchList();
    }

    private void showWaitCard() {
        ((CardLayout)getLayout()).show(this, CARD_WAIT);
    }

    private void showShowCard() {
        ((CardLayout)getLayout()).show(this, CARD_SHOW);
    }

    private void showErrorCard(String message) {
        errorMessage.setText(String.format("<html><font color=\"#ff0000\">An error occurred:<br><font size=\"-2\">%s</font></font></html>", message));
        ((CardLayout)getLayout()).show(this, CARD_ERROR);
    }

    private synchronized void setList(Object[][] list) {
        data = list;
        try {
            updateShowCard();
            showShowCard();
        } catch (Exception ex) {
            LOG.error("Unable to load list.", ex);
            showErrorCard(ClientMiscUtils.getMessage(ex));
        }
    }

    void refreshList() {
        showWaitCard();
        fetchList();
    }

    private void fetchList() {

        new MedSavantWorker<Object[][]>(pageName) {
            @Override
            protected Object[][] doInBackground() throws Exception {
                return detailedModel.getList(LIMIT);
            }

            @Override
            protected void showProgress(double ignored) {
            }

            @Override
            protected void showSuccess(Object[][] result) {
                setList(result);
            }
        }.execute();
    }

    private void updateShowCard() {
        showCard.removeAll();

        showCard.setLayout(new BorderLayout());
        showCard.setBackground(ViewUtil.getTertiaryMenuColor());
        showCard.setBorder(ViewUtil.getBigBorder());

        String[] columnNames = detailedModel.getColumnNames();
        Class[] columnClasses = detailedModel.getColumnClasses();
        int[] columnVisibility = detailedModel.getHiddenColumns();

        stp = new ListViewTablePanel(data, columnNames, columnClasses, columnVisibility) {

            @Override
            public void forceRefreshData() {
                refreshList();
            }
        };

        selectionGrabber = new RowSelectionGrabber(stp.getTable(), data);

        if (detailedView != null) {
            stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {

                    if (!e.getValueIsAdjusting()) {
                        List<Object[]> selectedItems = selectionGrabber.getSelectedItems();
                        if (selectedItems.size() == 1) {
                            detailedView.setSelectedItem(selectedItems.get(0));
                        } else {
                            detailedView.setMultipleSelections(selectedItems);
                        }
                    }
                }
            });

            stp.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popup = detailedView.createPopup();
                        if (popup != null) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
        }

        stp.getTable().getSelectionModel().setSelectionInterval(0, 0);

        showCard.add(stp, BorderLayout.CENTER);

        showCard.add(buttonPanel, BorderLayout.SOUTH);

    }

    public RowSelectionGrabber getSelectionGrabber() {
        return selectionGrabber;
    }
}
