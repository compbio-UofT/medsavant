/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.list;

import java.awt.event.ActionEvent;
import org.ut.biolab.medsavant.view.util.WaitPanel;
import com.jidesoft.utils.SwingWorker;
import org.ut.biolab.medsavant.view.component.Util;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.ut.biolab.medsavant.view.component.ListViewTablePanel;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.PeekingPanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

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

            this.add(new WaitPanel("Getting list"), CARD_WAIT);
            showCard = new JPanel();
            this.add(showCard, CARD_SHOW);

            buttonPanel = new JPanel();
            ViewUtil.applyHorizontalBoxLayout(buttonPanel);

            buttonPanel.setBorder(ViewUtil.getSmallBorder());
            buttonPanel.add(Box.createHorizontalGlue());

            final ListView instance = this;

            if (detailedEditer.doesImplementAdding()) {

                JButton butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ADD_ON_TOOLBAR));
                butt.setToolTipText("Add");
                butt.addActionListener(
                        new ActionListener() {

                            public void actionPerformed(ActionEvent ae) {
                                detailedEditer.addItems();
                                instance.refreshList();
                            }
                        });
                buttonPanel.add(butt);
                buttonPanel.add(ViewUtil.getSmallSeparator());
            }

            if (detailedEditer.doesImplementDeleting()) {
                JButton butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.REMOVE_ON_TOOLBAR));
                butt.setToolTipText("Remove selected");
                butt.addActionListener(
                        new ActionListener() {

                            public void actionPerformed(ActionEvent ae) {
                                detailedEditer.deleteItems(selectionGrabber.getSelectedItems());
                                instance.refreshList();
                            }
                        });
                buttonPanel.add(butt);
                buttonPanel.add(ViewUtil.getSmallSeparator());
            }

            if (detailedEditer.doesImplementEditing()) {
                JButton butt = ViewUtil.createIconButton(IconFactory.getInstance().getIcon(IconFactory.StandardIcon.EDIT));
                butt.setToolTipText("Edit selected");
                butt.addActionListener(
                        new ActionListener() {

                            public void actionPerformed(ActionEvent ae) {
                                if (selectionGrabber.getSelectedItems().size() > 0) {
                                    detailedEditer.editItems(selectionGrabber.getSelectedItems().get(0));
                                    instance.refreshList();
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

        this.add(new PeekingPanel("List", BorderLayout.EAST, listView, true, 320), BorderLayout.WEST);
        this.add(detailedView, BorderLayout.CENTER);
    }

    public void refresh() {
        listView.refreshList();
    }
}
