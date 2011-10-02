/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view.manage;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import org.ut.biolab.medsavant.db.util.DBSettings;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Status;
import org.ut.biolab.medsavant.db.util.query.LogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.ProjectQueryUtil;
import org.ut.biolab.medsavant.log.ClientLogger;
import org.ut.biolab.medsavant.view.component.SearchableTablePanel;
import org.ut.biolab.medsavant.view.subview.SectionView;
import org.ut.biolab.medsavant.view.subview.SubSectionView;
import org.ut.biolab.medsavant.view.util.ViewUtil;
import org.ut.biolab.medsavant.view.util.WaitPanel;

/**
 *
 * @author mfiume
 */
public class ServerLogPage extends SubSectionView {

    private static String CARDNAME_WAIT = "0";
    private static String CARDNAME_CLIENT = "1";
    private static String CARDNAME_ANNOTATION = "2";
    private static String CARDNAME_SERVER = "3";
    private JPanel panel;
    private JPanel menuPanel;
    private JPanel listPanel;
    private boolean clientTableRefreshed = false;
    private boolean serverTableRefreshed = false;
    private boolean annotationTableRefreshed = false;
    private SearchableTablePanel clientTable;
    private SearchableTablePanel serverTable;
    private SearchableTablePanel annotationTable;
    private static final List<String> clientColumnNames = Arrays.asList(new String[]{"User", "Type", "Description", "Time"});
    private static final List<String> serverColumnNames = Arrays.asList(new String[]{"Type", "Description", "Time"});
    private static final List<String> annotationsColumnNames = Arrays.asList(new String[]{"Project", "Reference", "Action", "Status", "Time", "Restart"});
    private static final List<Class> clientColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class, String.class});
    private static final List<Class> serverColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class});
    private static final List<Class> annotationsColumnClasses = Arrays.asList(new Class[]{String.class, String.class, String.class, String.class, String.class, JButton.class});
    private String currentCard;
    private WaitPanel waitPanel;
    private SwingWorker clientCardUpdater;
    private SwingWorker serverCardUpdater;
    private SwingWorker annotationCardUpdater;

    private class ClientCardUpdater extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                ResultSet rs = LogQueryUtil.getClientLog();
                Vector v = new Vector();
                while (rs.next()) {
                    Vector r = new Vector();
                    r.add(rs.getString(DBSettings.FIELDNAME_LOG_USER));
                    r.add(rs.getString(DBSettings.FIELDNAME_LOG_EVENT));
                    r.add(rs.getString(DBSettings.FIELDNAME_LOG_DESCRIPTION));
                    r.add(rs.getTimestamp(DBSettings.FIELDNAME_LOG_TIMESTAMP));
                    v.add(r);
                }
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                return v;
            } catch (SQLException ex) {
                ClientLogger.log(ServerLogPage.class, ex.getLocalizedMessage(), Level.SEVERE);
                return null;
            }
        }

        @Override
        protected void done() {
            try {
                Vector v = (Vector) get();
                if (v == null) {
                    return;
                }
                clientTable.updateData(v);
                changeToCard(CARDNAME_CLIENT);
            } catch (java.util.concurrent.CancellationException ex0) {
            } catch (Exception ex) {
                waitPanel.setComplete();
                waitPanel.setStatus("Problem getting log");
                showWaitPanel();
            }
        }
    };

    private class ServerCardUpdater extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                ResultSet rs = LogQueryUtil.getServerLog();
                Vector v = new Vector();
                while (rs.next()) {
                    Vector r = new Vector();
                    r.add(rs.getString(DBSettings.FIELDNAME_LOG_EVENT));
                    r.add(rs.getString(DBSettings.FIELDNAME_LOG_DESCRIPTION));
                    r.add(rs.getTimestamp(DBSettings.FIELDNAME_LOG_TIMESTAMP));
                    v.add(r);
                }
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                return v;
            } catch (SQLException ex) {
                ClientLogger.log(ServerLogPage.class, ex.getLocalizedMessage(), Level.SEVERE);
                return null;
            }
        }

        @Override
        protected void done() {
            try {
                Vector v = (Vector) get();
                if (v == null) {
                    return;
                }
                serverTable.updateData(v);
                changeToCard(CARDNAME_SERVER);
            } catch (java.util.concurrent.CancellationException ex0) {
            } catch (Exception ex) {
                waitPanel.setComplete();
                waitPanel.setStatus("Problem getting log");
                showWaitPanel();
            }
        }
    };

    private class AnnotationCardUpdater extends SwingWorker {

        @Override
        protected Object doInBackground() throws Exception {
            try {
                ResultSet rs = LogQueryUtil.getAnnotationLog();
                Vector v = new Vector();
                while (rs.next()) {

                    Status status = AnnotationLogQueryUtil.intToStatus(rs.getInt(4));

                    JButton button = new JButton("Retry");
                    button.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent ae) {
                            JOptionPane.showMessageDialog(null, "Clicked");
                        }
                    });

                    Vector r = new Vector();
                    r.add(rs.getString(1));
                    r.add(rs.getString(2));
                    r.add(AnnotationLogQueryUtil.intToAction(rs.getInt(3)));
                    r.add(status);

                    try {
                        r.add(rs.getTimestamp(5));
                    } catch (Exception e) {
                        r.add(null);
                    }

                    if (status != Status.ERROR) {
                        r.add(new JPanel());
                    } else {
                        r.add(button);
                    }

                    v.add(r);
                }
                if (Thread.currentThread().isInterrupted()) {
                    return null;
                }
                return v;
            } catch (SQLException ex) {
                ClientLogger.log(ServerLogPage.class, ex.getLocalizedMessage(), Level.SEVERE);
                return null;
            }
        }

        @Override
        protected void done() {
            try {
                Vector v = (Vector) get();
                if (v == null) {
                    return;
                }
                annotationTable.updateData(v);
                changeToCard(CARDNAME_ANNOTATION);
            } catch (java.util.concurrent.CancellationException ex0) {
            } catch (Exception ex) {
                waitPanel.setComplete();
                waitPanel.setStatus("Problem getting log");
                showWaitPanel();
            }
        }
    };

    public ServerLogPage(SectionView parent) {
        super(parent);
    }

    public String getName() {
        return "Logs";
    }

    public JPanel getView(boolean update) {
        if (panel == null) {
            setPanel();
        }
        return panel;
    }

    public void setPanel() {
        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        menuPanel = new JPanel();
        ViewUtil.applyHorizontalBoxLayout(menuPanel);

        ButtonGroup bg = new ButtonGroup();

        JRadioButton b1 = new JRadioButton("Client");
        JRadioButton b2 = new JRadioButton("Server");
        JRadioButton b3 = new JRadioButton("Annotations");

        bg.add(b1);
        bg.add(b2);
        bg.add(b3);


        listPanel = new JPanel();
        listPanel.setLayout(new CardLayout());

        listPanel.add(getWaitPanel(), CARDNAME_WAIT);
        listPanel.add(getClientCard(), CARDNAME_CLIENT);
        listPanel.add(getServerCard(), CARDNAME_SERVER);
        listPanel.add(getAnnotationCard(), CARDNAME_ANNOTATION);

        panel.add(menuPanel, BorderLayout.NORTH);
        panel.add(listPanel, BorderLayout.CENTER);

        b1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_CLIENT);

            }
        });
        b2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_SERVER);
            }
        });
        b3.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                changeToCard(CARDNAME_ANNOTATION);
            }
        });


        b1.setSelected(true);
        this.changeToCard(CARDNAME_CLIENT);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                refreshCurrentCard();
            }
        });

        menuPanel.add(Box.createHorizontalGlue());
        menuPanel.add(b1);
        menuPanel.add(b2);
        menuPanel.add(b3);
        menuPanel.add(refreshButton);
        menuPanel.add(Box.createHorizontalGlue());

    }

    private void refreshCurrentCard() {
        if (currentCard.equals(CARDNAME_CLIENT)) {
            refreshClientCard();
        } else if (currentCard.equals(CARDNAME_SERVER)) {
            refreshServerCard();
        } else if (currentCard.equals(CARDNAME_ANNOTATION)) {
            refreshAnnotationCard();
        }
    }

    private synchronized void changeToCard(String cardname) {

        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, cardname);
        this.currentCard = cardname;

        if (cardname.equals(CARDNAME_CLIENT)) {
            if (!clientTableRefreshed) {
                this.refreshClientCard();
                clientTableRefreshed = true;
            }
        } else if (cardname.equals(CARDNAME_SERVER)) {
            if (!serverTableRefreshed) {
                this.refreshServerCard();
                serverTableRefreshed = true;
            }
        } else if (cardname.equals(CARDNAME_ANNOTATION)) {
            if (!annotationTableRefreshed) {
                this.refreshAnnotationCard();
                annotationTableRefreshed = true;
            }
        }
    }

    private synchronized void showWaitPanel() {
        CardLayout cl = (CardLayout) (listPanel.getLayout());
        cl.show(listPanel, CARDNAME_WAIT);
    }
    private int limit = 1000;

    private JPanel getClientCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        clientTable = new SearchableTablePanel(new Vector(), clientColumnNames, clientColumnClasses, new ArrayList<Integer>(), limit);
        p.add(clientTable, BorderLayout.CENTER);
        return p;
    }

    private JPanel getServerCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        serverTable = new SearchableTablePanel(new Vector(), serverColumnNames, serverColumnClasses, new ArrayList<Integer>(), limit);
        p.add(serverTable, BorderLayout.CENTER);
        return p;
    }

    private JPanel getAnnotationCard() {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        annotationTable = new SearchableTablePanel(new Vector(), annotationsColumnNames, annotationsColumnClasses, new ArrayList<Integer>(), limit);
        annotationTable.getTable().getColumn("Restart").setCellRenderer(new JTableButtonRenderer());
        annotationTable.getTable().addMouseListener(new JTableButtonMouseListener(annotationTable.getTable()));
        p.add(annotationTable, BorderLayout.CENTER);
        return p;
    }

    private void refreshClientCard() {
        if (clientCardUpdater != null) {
            clientCardUpdater.cancel(true);
        }
        clientCardUpdater = new ClientCardUpdater();

        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        this.clientCardUpdater.execute();
    }

    private void refreshServerCard() {
        if (serverCardUpdater != null) {
            serverCardUpdater.cancel(true);
        }
        serverCardUpdater = new ServerCardUpdater();

        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        this.serverCardUpdater.execute();
    }

    private void refreshAnnotationCard() {
        if (annotationCardUpdater != null) {
            annotationCardUpdater.cancel(true);
        }
        annotationCardUpdater = new AnnotationCardUpdater();

        waitPanel.setIndeterminate();
        waitPanel.setStatus("");
        showWaitPanel();
        this.annotationCardUpdater.execute();
    }

    @Override
    public Component[] getBanner() {
        return null;
    }

    @Override
    public void viewLoading() {
    }

    @Override
    public void viewDidUnload() {
    }

    private WaitPanel getWaitPanel() {
        waitPanel = new WaitPanel("Getting log...");
        return waitPanel;
    }

    public class JTableButtonRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            if (value instanceof JButton) {
                JButton button = (JButton) value;
                if (isSelected) {
                    button.setForeground(table.getForeground());
                    button.setBackground(table.getSelectionBackground());
                } else {
                    button.setForeground(table.getForeground());
                    button.setBackground(UIManager.getColor("Button.background"));
                }
                return button;
            }
            return (Component) value;
        }
    }

    public class JTableButtonMouseListener extends MouseAdapter {

        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row = e.getY() / table.getRowHeight();

            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton) value).doClick();
                }
            }
        }
    }
}
