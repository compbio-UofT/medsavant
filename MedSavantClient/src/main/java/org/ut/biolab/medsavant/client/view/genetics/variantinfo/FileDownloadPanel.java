/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author jim
 */
public class FileDownloadPanel extends JPanel implements PropertyChangeListener {    
    private JLabel heading;
    private JProgressBar progressBar;    
    private DownloadTask fileDownloadTask;
    
    private void init(){
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        
        //add(new JLabel("Downloading GeneMANIA"));      
        add(heading);
        add(progressBar);
        updateLabel(0);
    }
    
    private String getMegs(long bytes){
      String m = Integer.toString((int)Math.round((double)bytes / 1e5));      
      return m.substring(0, m.length()-1) + "." + m.charAt(m.length()-1) + "M";            
    }
    
    private void updateLabel(long bytesFetched){       
        String s = getMegs(bytesFetched) +" / " + getMegs(fileDownloadTask.getFileSize());
        //info.setText(getMegs(bytesFetched) +" / " + getMegs(fileDownloadTask.getFileSize()));
        progressBar.setString(s);
        repaint();
    }
    
    protected void setLabel(String s){
        progressBar.setString(s);
        repaint();
    }
    
    public FileDownloadPanel(String heading, DownloadTask dt) throws IOException {
        super();
        this.fileDownloadTask = dt;
        this.heading = new JLabel(heading);
        init();        
        fileDownloadTask.addPropertyChangeListener(this);         
    }
    
    public void download(){        
        fileDownloadTask.execute();
    }
           
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {                 
            int progress = (Integer) evt.getNewValue();
            progressBar.setValue(progress);              
            updateLabel(fileDownloadTask.getTotalBytesSoFar());            
        }
    }
    
    
  
    
}
