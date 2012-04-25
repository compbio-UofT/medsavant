/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.model.Notification;
import org.ut.biolab.medsavant.model.ProjectDetails;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.util.DialogUtils;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class NotificationPanel extends JPanel implements LoginListener {
    
    private static int INTERVAL = 1000 * 60; //one minute
    private JPanel numPanel;
    private JLabel numLabel;
    private JLabel label;
    private NotificationChecker worker;
    private List<Notification> list;
    private JPopupMenu popup;
    
    private static Color ALERT_COLOUR = new Color(200,0,0);
    
    public NotificationPanel(){
        ViewUtil.setBoxXLayout(this);
        this.setOpaque(false);
        this.setVisible(false);
        
        this.setPreferredSize(new Dimension(90,20));
        this.setMaximumSize(new Dimension(90,20));

        numPanel = new JPanel();
        numPanel.setLayout(new BorderLayout());
        numPanel.setPreferredSize(new Dimension(17,17));
        numPanel.setMaximumSize(new Dimension(17,17));
        numPanel.setBackground(ALERT_COLOUR);
        
        numLabel = new JLabel();
        numLabel.setHorizontalAlignment(JLabel.CENTER);
        numLabel.setForeground(Color.white);
        numLabel.setFont(numLabel.getFont().deriveFont(Font.BOLD, 10));
        numPanel.add(numLabel, BorderLayout.CENTER);
        
        this.add(numPanel);
        
        this.add(ViewUtil.getMediumSeparator());
        label = new JLabel("");
        this.add(label);
        this.add(Box.createHorizontalGlue());
        LoginController.addLoginListener(this);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
               
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup();
            }
        });
        
    }
    
    private void showPopup(){
        showPopup(0);
    }
    
    private void showPopup(final int start){
        popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Color.lightGray, 1));
        
        
        boolean headerAdded = false;
        if(list == null){
            popup.add(new NotificationIcon(null, null));
        } else {
            
            //add notifications
            for(int i = start; i < Math.min(start+5, list.size()); i++){
                popup.add(new NotificationIcon(list.get(i), popup));
                if(i != Math.min(start+5, list.size())-1){                  
                    popup.add(createSeparator());
                }
            }
            
            //add page header
            if(list.size() > 5){
                JPanel header = new JPanel();
                header.setMinimumSize(new Dimension(1, 15));
                header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
                if(start >=5){
                    JLabel prevButton = ViewUtil.createLabelButton("  Prev Page  ");
                    prevButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showPopup(start-5);
                        }
                    });
                    header.add(prevButton);
                }                    
                header.add(Box.createHorizontalGlue());
                if(start+5<list.size()){
                    JLabel nextButton = ViewUtil.createLabelButton("  Next Page  ");
                    nextButton.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            showPopup(start+5);
                        }
                    });
                    header.add(nextButton);
                }
                popup.add(createSeparator());
                popup.add(header);                
                headerAdded = true;
            }
        }
        
        int offset = -Math.min(5, list.size()-start) * (NotificationIcon.height + 2) -3 - (headerAdded ? 16 : 0);
        popup.show(this, 0, offset);
    }
    
    private JSeparator createSeparator(){
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setMaximumSize(new Dimension(1000,1));
        sep.setBackground(Color.white);
        sep.setForeground(Color.lightGray);
        return sep;
    }
    
    private void setNotifications(List<Notification> list){
        
        this.list = list;
        
        //deal with error or init
        if(list == null || list.isEmpty()){
            this.setVisible(false);
            return;
        }
        
        this.setVisible(true);
        numLabel.setText(Integer.toString(list.size()));
        label.setText("Notification" + (list.size() > 1 ? "s" : ""));
               
    }
    
    /* 
     * Start a new worker for a new user
     */
    public void loginEvent(LoginEvent evt) {
        if(evt.getType() == LoginEvent.EventType.LOGGED_IN){           
            (worker = new NotificationChecker()).execute();            
        } else if (evt.getType() == LoginEvent.EventType.LOGGED_OUT && worker != null) {
            worker.cancel(true);
            setNotifications(null);
        }  
    }

    private class NotificationChecker extends SwingWorker {

        public NotificationChecker() {}

        @Override
        protected Object doInBackground() throws Exception {
            
            boolean interrupted = false;
            String user = LoginController.getUsername();
            while (!interrupted) {

                try {
                    setNotifications(MedSavantClient.NotificationQueryUtilAdapter.getNotifications(LoginController.sessionId, user));
                } catch (SQLException ex){
                    setNotifications(null);
                }
                    
                Thread.sleep(INTERVAL);
                if (Thread.interrupted()) {
                    interrupted = true;
                }
            }
            return null;
        }
    }
    
}

class NotificationIcon extends JPanel {

    public static int width = 260;
    public static int height = 80;
    
    public NotificationIcon(final Notification n, final JPopupMenu p){
        
        this.setPreferredSize(new Dimension(width, height));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.white);
        this.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(240,240,240));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.white);
            }
        });
        
        if(n == null){
            this.add(new JLabel("<HTML><P>You have no notifications</P></HTML>"), BorderLayout.CENTER);
            return;
        }
        
        this.add(new JLabel("<HTML><P>" + n.getMessage() + "</P></HTML>"), BorderLayout.CENTER);
        
        switch(n.getType()){
            case PUBLISH:
                
                JLabel publishButton = new JLabel("Click to publish or remove");
                publishButton.setFont(publishButton.getFont().deriveFont(Font.BOLD));
                this.add(publishButton, BorderLayout.SOUTH);
                this.addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        
                        p.setVisible(false);
                        
                        //get db lock
                        try {            
                            if(!MedSavantClient.SettingsQueryUtilAdapter.getDbLock(LoginController.sessionId)){
                                DialogUtils.displayMessage("Cannot make changes", "Another user is making changes to the database. You must wait until this user has finished. ");
                                return;
                            }
                        } catch (Exception ex) {
                            DialogUtils.displayErrorMessage("Error getting database lock", ex);
                            return;
                        }
                        
                        Object[] options = new Object[]{"Publish", "Delete (Undo Changes)", "Cancel"};
                        int option = JOptionPane.showOptionDialog(null, "<HTML>Publishing this table will log all users out of MedSavant.<BR>Are you sure you want to proceed?</HTML>", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
                        
                        ProjectDetails event = (ProjectDetails)n.getData();
                        if (option == JOptionPane.NO_OPTION){
                            try {
                                MedSavantClient.VariantManagerAdapter.cancelPublish(LoginController.sessionId, event.getProjectId(), event.getReferenceId(), event.getUpdateId());
                            } catch (Exception ex) {
                                Logger.getLogger(NotificationIcon.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else if (option == JOptionPane.YES_OPTION){                          
                            try {
                                MedSavantClient.VariantManagerAdapter.publishVariants(LoginController.sessionId, event.getProjectId(), event.getReferenceId(), event.getUpdateId());
                            } catch (Exception ex) {
                                Logger.getLogger(NotificationIcon.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        
                        //release lock
                        try {
                            MedSavantClient.SettingsQueryUtilAdapter.releaseDbLock(LoginController.sessionId);
                        } catch (Exception ex) {
                            Logger.getLogger(NotificationPanel.class.getName()).log(Level.SEVERE, null, ex);
                        } 
                    }
                });                
        }
        
    }

}

