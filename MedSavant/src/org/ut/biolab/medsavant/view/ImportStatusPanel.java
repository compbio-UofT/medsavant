/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.sql.SQLException;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.ut.biolab.medsavant.controller.LoginController;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil;
import org.ut.biolab.medsavant.db.util.query.AnnotationLogQueryUtil.Status;
import org.ut.biolab.medsavant.model.event.LoginEvent;
import org.ut.biolab.medsavant.model.event.LoginListener;
import org.ut.biolab.medsavant.view.images.IconFactory;
import org.ut.biolab.medsavant.view.images.ImagePanel;
import org.ut.biolab.medsavant.view.util.ViewUtil;

/**
 *
 * @author Andrew
 */
public class ImportStatusPanel extends JPanel implements LoginListener {

    private static int INTERVAL = 1000 * 60;
    private JPanel imagePanel;
    private JLabel label;
    private ImportStatusChecker worker;
    private Status currentStatus = null;
    
    public ImportStatusPanel(){
        ViewUtil.setBoxXLayout(this);
        this.setOpaque(false);
        this.setPreferredSize(new Dimension(100,20));
        imagePanel = ViewUtil.getClearPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setPreferredSize(new Dimension(15,20));
        imagePanel.setMaximumSize(new Dimension(15,20));
        imagePanel.setOpaque(false);
        this.add(imagePanel);
        this.add(ViewUtil.getMediumSeparator());
        label = new JLabel("");
        this.add(label);
        this.add(Box.createHorizontalGlue());
        LoginController.addLoginListener(this);
    }
    
    private void setStatus(Status status){
        ImageIcon im = null;
        
        this.setVisible(status != null);
        if(status == null || status == currentStatus) return;

        switch(status) {
            case COMPLETE:
                label.setText("No pending updates");
                im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.GREEN);
                this.setToolTipText("All recent changes and imports have been marked as completed.");
                break;
            case PENDING:
            case PREPROCESS:
            case INPROGRESS:
                label.setText("You have pending updates");
                im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.ORANGE);
                this.setToolTipText("You have updates queued for completion by server.");
                break;
            case ERROR:
                label.setText("An error occurred while performing one of your updates");
                im = IconFactory.getInstance().getIcon(IconFactory.StandardIcon.RED);
                this.setToolTipText(
                        "<HTML>One of your recent updates has resulted in an error and was not completed. <BR>"
                        + "Your administrator has been notified and may need to help you resolve this issue.</HTML>");
                break;
        }
        imagePanel.removeAll();
        if(im != null){
            ImagePanel image = new ImagePanel(im.getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH), 15, 15);
            imagePanel.add(image, BorderLayout.NORTH);
        }
        
        this.updateUI();
    }   
    
    /* 
     * Start a new worker for a new user
     */
    public void loginEvent(LoginEvent evt) {
        if(evt.getType() == LoginEvent.EventType.LOGGED_IN){           
            (worker = new ImportStatusChecker()).execute();            
        } else if (evt.getType() == LoginEvent.EventType.LOGGED_OUT && worker != null) {
            worker.cancel(true);
            setStatus(null);
        }  
    }

    private class ImportStatusChecker extends SwingWorker {

        public ImportStatusChecker() {}

        @Override
        protected Object doInBackground() throws Exception {
            
            boolean interrupted = false;
            while (!interrupted) {

                String user = LoginController.getUsername();
                try {
                    setStatus(AnnotationLogQueryUtil.getUserPriorityStatus(user));
                } catch (SQLException ex){
                    setStatus(null);
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
