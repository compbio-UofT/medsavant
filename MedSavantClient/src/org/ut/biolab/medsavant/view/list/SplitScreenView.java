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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jidesoft.grid.TableModelWrapperUtils;
import com.jidesoft.utils.SwingWorker;

import org.ut.biolab.medsavant.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.view.component.Util;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class SplitScreenView extends JPanel {

    private final DetailedListModel detailedListModel;
    private ListView listView;
    private DetailedView detailedView;
    //TODO: handle limits better!
    private static final int limit = 10000;
    private final DetailedListEditor detailEditer;

    /*
    public DetailedView getDetailedView() {
    return detailedView;
    }

    public DetailedListModel getListModel() {
    return detailedListModel;
    }
     *
     */
    private static class ListView extends JPanel {

        private static final String CARD_WAIT = "wait";
        private static final String CARD_SHOW = "show";
        private final DetailedListModel listModel;
        private final CardLayout cl;
        private List<Object[]> list;
        private final JPanel showCard;
        private final DetailedView detailedView;
        private ListViewTablePanel stp;
        //private int limit = 10000;
        private RowSelectionGrabber selectionGrabber;
        private final DetailedListEditor detailedEditer;
        private JPanel buttonPanel;

        private ListView(DetailedListModel listModel, DetailedView detailedView, final DetailedListEditor detailedEditer) {
            this.listModel = listModel;
            this.detailedView = detailedView;
            this.detailedEditer = detailedEditer;

            cl = new CardLayout();
            this.setLayout(cl);

            WaitPanel wp = new WaitPanel("Getting list");
            wp.setBackground(ViewUtil.getTertiaryMenuColor());
            this.add(wp, CARD_WAIT);
            showCard = new JPanel();


            this.add(showCard, CARD_SHOW);

            buttonPanel = ViewUtil.getClearPanel();
            ViewUtil.applyHorizontalBoxLayout(buttonPanel);

            buttonPanel.setBorder(ViewUtil.getMediumBorder());
            buttonPanel.add(Box.createHorizontalGlue());

            if (detailedEditer.doesImplementAdding()) {

                JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
                butt.setToolTipText("Add");
                butt.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        detailedEditer.addItems();
                        refreshList();
                    }
                });
                buttonPanel.add(butt);
                buttonPanel.add(ViewUtil.getSmallSeparator());
            }

            if (detailedEditer.doesImplementDeleting()) {
                JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                butt.setToolTipText("Remove selected");
                butt.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        detailedEditer.deleteItems(selectionGrabber.getSelectedItems());
                        refreshList();
                    }
                });
                buttonPanel.add(butt);
                buttonPanel.add(ViewUtil.getSmallSeparator());
            }

            if (detailedEditer.doesImplementEditing()) {
                JLabel butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT));
                butt.setToolTipText("Edit selected");
                butt.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (selectionGrabber.getSelectedItems().size() > 0) {
                            detailedEditer.editItems(selectionGrabber.getSelectedItems().get(0));
                            refreshList();
                        } else {
                            DialogUtils.displayMessage("Choose one item to edit");
                        }
                    }
                });
                buttonPanel.add(butt);
            }

            buttonPanel.add(Box.createHorizontalGlue());

            showWaitCard();
            fetchList();
        }

        private void showWaitCard() {
            cl.show(this, CARD_WAIT);
        }

        private void showShowCard() {
            cl.show(this, CARD_SHOW);
        }

        private synchronized void setList(List<Object[]> list) {
            this.list = list;
            updateShowCard();
            showShowCard();
        }

        public void refreshList() {
            showWaitCard();
            fetchList();
        }

        private void fetchList() {

            SwingWorker sw = new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    return listModel.getList(limit);
                }

                @Override
                protected void done() {
                    List<Object[]> list;
                    try {
                        list = (List<Object[]>) get();
                        setList(list);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };

            sw.execute();
        }

        private void updateShowCard() {
            showCard.removeAll();

            showCard.setLayout(new BorderLayout());
            showCard.setBackground(ViewUtil.getTertiaryMenuColor());
            showCard.setBorder(ViewUtil.getBigBorder());

            final List<Object[]> data = list;
            List<String> columnNames = listModel.getColumnNames();
            List<Class> columnClasses = listModel.getColumnClasses();
            List<Integer> columnVisibility = listModel.getHiddenColumns();

            stp = new ListViewTablePanel(Util.listToVector(data), columnNames, columnClasses, columnVisibility) {

                @Override
                public void forceRefreshData() {
                    refreshList();
                }
            };

            selectionGrabber = new RowSelectionGrabber(stp.getTable(), data);

            stp.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {

                    if(e.getValueIsAdjusting()) return;

                    List<Object[]> selectedItems = selectionGrabber.getSelectedItems();
                    if (selectedItems.size() == 1) {
                        detailedView.setSelectedItem(selectedItems.get(0));
                    } else {
                        detailedView.setMultipleSelections(selectedItems);
                    }

                }
            });

            stp.getTable().addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(SwingUtilities.isRightMouseButton(e)) {
                        int row = stp.getTable().rowAtPoint(e.getPoint());
                        stp.getTable().getSelectionModel().setSelectionInterval(row, row);
                        detailedView.setRightClick(e);
                    }
                }
            });

            stp.getTable().getSelectionModel().setSelectionInterval(0, 0);

            showCard.add(stp, BorderLayout.CENTER);

            showCard.add(buttonPanel, BorderLayout.SOUTH);

        }

        public RowSelectionGrabber getSelectionGrabber() {
            return selectionGrabber;
        }
    }

    /*
     * public void addButton(JButton b) {
    buttonPanel.add(b);
    buttonPanel.updateUI();
    }*/
    public SplitScreenView(DetailedListModel lm, DetailedView view) {
        this(lm, view, new DetailedListEditor() {

            @Override
            public void addItems() {
            }

            @Override
            public void editItems(Object[] i) {
            }

            @Override
            public void deleteItems(List<Object[]> i) {
            }
        });
    }

    public SplitScreenView(DetailedListModel lm, DetailedView view, DetailedListEditor detailEditer) {
        this.detailedListModel = lm;
        this.detailedView = view;
        this.detailEditer = detailEditer;
        initGUI();
        detailedView.setSplitScreenParent(this);
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());

        listView = new ListView(detailedListModel, detailedView, detailEditer);

        PeekingPanel pp = new PeekingPanel("List", BorderLayout.EAST, (JComponent)listView, true, 330);
        pp.setToggleBarVisible(false);
        this.add(pp, BorderLayout.WEST);
        this.add(detailedView, BorderLayout.CENTER);
    }

    public void refresh() {
        listView.refreshList();
    }

    public List<Object[]> getList() {
        return listView.list;
    }

    public void selectInterval(int start, int end){
        start = TableModelWrapperUtils.getRowAt(listView.stp.getTable().getModel(), start);
        end = TableModelWrapperUtils.getRowAt(listView.stp.getTable().getModel(), end);
        listView.stp.getTable().getSelectionModel().setSelectionInterval(start, end);
        listView.stp.scrollToIndex(start);
    }

}
