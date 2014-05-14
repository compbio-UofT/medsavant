/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.client.view.list;

import com.explodingpixels.macwidgets.MacIcons;
import com.explodingpixels.macwidgets.SourceListControlBar;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.client.view.login.LoginController;
import org.ut.biolab.medsavant.shared.model.ProgressStatus;
import org.ut.biolab.medsavant.client.util.ClientMiscUtils;
import org.ut.biolab.medsavant.client.util.MedSavantWorker;
import org.ut.biolab.medsavant.client.view.images.IconFactory;
import org.ut.biolab.medsavant.client.view.util.DialogUtils;
import org.ut.biolab.medsavant.client.view.util.ViewUtil;
import org.ut.biolab.medsavant.client.view.component.WaitPanel;
import org.ut.biolab.medsavant.client.view.util.list.NiceList;
import org.ut.biolab.medsavant.client.view.util.list.NiceListColorScheme;
import org.ut.biolab.medsavant.client.view.util.list.NiceListItem;

/**
 *
 * @author tarkvara
 */
public class ListView extends JPanel {

    private static final Log LOG = LogFactory.getLog(ListView.class);
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

    private NiceList list;

    //private JPanel buttonPanel;
    private final SourceListControlBar controlBar;
    private boolean searchBarEnabled = false;
    private final WaitPanel wp;
    private NiceListColorScheme listColorScheme;

    public ListView(String page, DetailedListModel model, DetailedView view, DetailedListEditor editor) {
        pageName = page;
        detailedModel = model;
        detailedView = view;
        detailedEditor = editor;

        setLayout(new CardLayout());

        wp = new WaitPanel("Getting list");
        add(wp, CARD_WAIT);

        showCard = new JPanel();
        add(showCard, CARD_SHOW);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BorderLayout());
        errorMessage = new JLabel("An error occurred:");
        errorPanel.add(errorMessage, BorderLayout.NORTH);

        add(errorPanel, CARD_ERROR);

        controlBar = new SourceListControlBar();

        if (detailedEditor.doesImplementAdding()) {

            controlBar.createAndAddButton(MacIcons.PLUS, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detailedEditor.addItems();
                    if (detailedEditor.doesRefreshAfterAdding()) {
                        refreshList();
                    }
                }

            });
        }

        if (detailedEditor.doesImplementImporting()) {

            controlBar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.IMPORT), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detailedEditor.importItems();
                    if (detailedEditor.doesImplementImporting()) {
                        refreshList();
                    }
                }

            });
        }

        if (detailedEditor.doesImplementExporting()) {

            controlBar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EXPORT), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detailedEditor.exportItems();
                    refreshList();
                }

            });
        }
        
        if (detailedEditor.doesImplementDeleting()) {

            controlBar.createAndAddButton(MacIcons.MINUS, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detailedEditor.deleteItems(getSelectedRows());
                    // In some cases, such as removing/publishing variants, the deleteItems() method may have logged us out.

                    if (detailedEditor.doesRefreshAfterDeleting()) {
                        refreshList();
                    }
                }

            });
        }

        if (detailedEditor.doesImplementEditing()) {

            controlBar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.GEAR), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (getSelectedRows().size() > 0) {
                        detailedEditor.editItem(getSelectedRows().get(0));
                        if (detailedEditor.doesRefreshAfterEditing()) {
                            refreshList();
                        }
                    } else {
                        DialogUtils.displayMessage("Please choose one item to edit.");
                    }
                }

            });
        }

        // Only for SavedFiltersPanel
        if (detailedEditor.doesImplementLoading()) {

            controlBar.createAndAddButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.LOAD_ON_TOOLBAR), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    detailedEditor.loadItems(getSelectedRows());
                }

            });

        }

        showWaitCard();
        fetchList();
    }

    public SourceListControlBar getControlBar() {
        return this.controlBar;
    }

    private List<Object[]> getSelectedRows() {
        List<NiceListItem> items = list.getSelectedItems();
        List<Object[]> selectedRows = new ArrayList<Object[]>();
        for (NiceListItem item : items) {
            selectedRows.add((Object[]) item.getItem());
        }
        return selectedRows;
    }

    private void showWaitCard() {
        //((CardLayout) getLayout()).show(this, CARD_WAIT);
    }

    private void showShowCard() {
        ((CardLayout) getLayout()).show(this, CARD_SHOW);
        this.updateUI();
    }

    private void showErrorCard(String message) {
        errorMessage.setText(String.format("<html><font color=\"#ff0000\">An error occurred:<br><font size=\"-2\">%s</font></font></html>", message));
        ((CardLayout) getLayout()).show(this, CARD_ERROR);
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

    boolean isFetching = false;
    final Object fetch = new Object();

    private void fetchList() {

        isFetching = true;
        new MedSavantWorker<Object[][]>(pageName) {
            @Override
            protected Object[][] doInBackground() throws Exception {
                try {
                    Object[][] result = detailedModel.getList(LIMIT);
                    return result;
                } catch (Throwable t) {
                    t.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void showProgress(double ignored) {
            }

            @Override
            protected void showSuccess(Object[][] result) {
                //System.out.println("Fetched new list");
                setList(result);
                isFetching = false;
                synchronized (fetch) {
                    fetch.notifyAll();
                }
                if (result.length == 0) {
                    detailedView.setSelectedItem(new Object[]{});
                }
            }

            @Override
            protected ProgressStatus checkProgress() {
                return new ProgressStatus("Working", 0.5);
            }
        }.execute();
    }

    private void updateShowCard() {
        showCard.removeAll();

        showCard.setLayout(new BorderLayout());

        String[] columnNames = detailedModel.getColumnNames();
        Class[] columnClasses = detailedModel.getColumnClasses();
        int[] columnVisibility = detailedModel.getHiddenColumns();

        int firstVisibleColumn = 0;
        while (columnVisibility.length > 0 && firstVisibleColumn == columnVisibility[firstVisibleColumn]) {
            firstVisibleColumn++;
        }

        Set<NiceListItem> selectedItems;
        if (list != null) {
            selectedItems = new HashSet<NiceListItem>(list.getSelectedItems());
        } else {
            selectedItems = new HashSet<NiceListItem>(); // empty set, for simplicity of not having to null check later on
        }

        list = new NiceList();
        if (listColorScheme != null) {
            list.setColorScheme(listColorScheme);
        }
        list.startTransaction();

        List<Integer> selectedIndicies = new ArrayList<Integer>();

        int counter = 0;
        for (Object[] row : data) {
            NiceListItem nli = new NiceListItem(row[firstVisibleColumn].toString(), row);
            list.addItem(nli);

            if (selectedItems.contains(nli)) {
                selectedIndicies.add(counter);
            }
            counter++;
        }

        /*
         int[] selectedIndiciesArray = new int[selectedIndicies.size()];
        
         System.out.println("Reselecting "  + selectedIndicies.size() + " items");
         for (int i = 0; i < selectedIndicies.size();i++) {
         System.out.println("Reselecting "  + list.getItem(selectedIndicies.get(i)).toString() + " at index " + selectedIndicies.get(i));
         selectedIndiciesArray[i] = selectedIndicies.get(i);
         }*/
        list.endTransaction();

        wp.setBackground(list.getColorScheme().getBackgroundColor());

        if (detailedView != null) {
            list.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {

                    if (!e.getValueIsAdjusting()) {
                        List<Object[]> selectedItems = getSelectedRows();
                        if (selectedItems.size() == 1) {
                            detailedView.setSelectedItem(selectedItems.get(0));
                        } else {
                            detailedView.setMultipleSelections(selectedItems);
                        }
                    }
                }
            });

            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        JPopupMenu popup = detailedView.createPopup();
                        if (popup != null) {
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            });
        }

        if (selectedIndicies.isEmpty()) {
            list.selectItemAtIndex(0);
        } else {
            list.selectItemsAtIndicies(selectedIndicies);
        }

        JScrollPane jsp = ViewUtil.getClearBorderlessScrollPane(list);
        jsp.setHorizontalScrollBar(null);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new MigLayout("wrap, fillx"));

        topPanel.add(ViewUtil.getEmphasizedLabel(pageName.toUpperCase()));
        topPanel.setBackground(list.getColorScheme().getBackgroundColor());

        if (searchBarEnabled) {
            topPanel.add(list.getSearchBar(), "growx 1.0");
        }
        showCard.add(topPanel, BorderLayout.NORTH);

        showCard.add(jsp, BorderLayout.CENTER);

        showCard.add(controlBar.getComponent(), BorderLayout.SOUTH);

    }

    public void setSearchBarEnabled(boolean b) {
        searchBarEnabled = b;
    }

    void selectItemWithKey(final String key) {
        
        System.out.println("Selecting item with key " + key);
        
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (isFetching) {
                    try {
                        synchronized (fetch) {
                            fetch.wait();
                        }
                    } catch (InterruptedException ex) {
                    }
                }
                list.selectItemWithKey(key);
            }

        }).start();
    }

    void selectItemAtIndex(final int i) {
        //System.out.println("Selecting item at index " + i);
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (isFetching) {
                    try {
                        synchronized (fetch) {
                            fetch.wait();
                        }
                    } catch (InterruptedException ex) {
                    }
                }
                list.selectItemAtIndex(i);
            }

        }).start();

    }
    
    public void setSelectedItemText(String text) {
        NiceListItem item = list.getSelectedItems().get(0);
        item.setLabel(text);
        list.updateUI();
    }


    void setColorScheme(NiceListColorScheme cs) {
        listColorScheme = cs;
        if (list != null) {
            updateShowCard();
        }
    }
}
