/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.client.view.genetics.variantinfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.view.Notification;
import org.ut.biolab.medsavant.client.view.NotificationsPanel;

/**
 *
 * @author jim
 */
public abstract class DownloadTask extends SwingWorker {
    private static final Log LOG = LogFactory.getLog(DownloadTask.class);
    private final String URLStr;
    private final String destPath;
    private final long filesize;
    private long totalBytesRead = 0;
    private final String filename;
    private static final int BUFFER_SIZE = 4096;  
    protected final Notification notification;   
    private int notificationId = 0;
    
    public enum DownloadState{RUNNING, FINISHED, CANCELLED, NOT_STARTED};
    private DownloadState downloadState = DownloadState.NOT_STARTED;
                
    protected void setDownloadState(DownloadState downloadState){
        switch(downloadState){
            case RUNNING:
                notification.setStatus(Notification.JobStatus.RUNNING);
                break;
            case FINISHED:
                notification.setStatus(Notification.JobStatus.FINISHED);
                break;
            case CANCELLED:
                notification.setStatus(Notification.JobStatus.CANCELLED);
                break;
        }
        
        DownloadState oldDownloadState = this.downloadState;
        this.downloadState = downloadState;
        this.firePropertyChange("downloadState", oldDownloadState, downloadState);
    }
    
    public DownloadState getDownloadState(){        
        return downloadState;
    }
    
    private String extractFileFromURL(){
        return URLStr.substring(URLStr.lastIndexOf("/") + 1,
                    URLStr.length());        
    }
        
    public String getDestPath(){
        if (destPath.charAt(destPath.length() - 1) == File.separatorChar) {
               return destPath + filename;
            } else {
                return destPath + "/" + filename;
            }
    }    
    
    public DownloadTask(String URLStr, String destPath, String notificationTitle) throws IOException {
        this.URLStr = URLStr;
        this.destPath = destPath;        
        this.notification = new Notification(notificationTitle) {	   	
	    @Override
	    public void cancelJob() {
                DownloadTask.this.cancelDownload();
	    }

            @Override
            protected void showResults() {
            }

            @Override
            protected void closeJob() {                
                NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).removeNotification(notificationId);
            }
            
	};
        notification.showResultsOnFinish(false);
       
	notificationId = NotificationsPanel.getNotifyPanel(NotificationsPanel.JOBS_PANEL_NAME).addNotification(notification.getView());
	
        
        int filesize;
        String filename;

        URL url = new URL(URLStr);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("File "+URLStr+"unavailable from server");
        }


        String disposition = httpConn.getHeaderField("Content-Disposition");        
        filesize = httpConn.getContentLength();

        if (disposition != null) {
            // extracts file name from header field
            int index = disposition.indexOf("filename=");
            if (index > 0) {
                filename = disposition.substring(index + 10,
                        disposition.length() - 1);
            } else {
                filename = extractFileFromURL();
            }
        } else {
            // extracts file name from URL
            filename = extractFileFromURL();
        }
        this.filename = filename;
        this.filesize = filesize;        
        httpConn.disconnect();     
    }
    
    public long getFileSize(){
        return filesize;        
    }
    
    public long getTotalBytesSoFar(){
        return totalBytesRead;
    }

    
    @Override
    protected final void done(){   
        if(isCancelled()){            
            setDownloadState(DownloadState.CANCELLED);            
        }
        doneDownload();        
    }
    
    protected void doneDownload(){
        setDownloadState(DownloadState.FINISHED);        
    }
          
    //called when user clicks 'results' button.
    public void showResults(){
        
    }

    public void cancelDownload(){        
        System.out.println("Cancelled job");
        cancel(true);
    }
    
    @Override
    protected Object doInBackground() {              
        try {          
            LOG.debug("Starting download of genemania");
            setDownloadState(DownloadState.RUNNING);            
            String saveFilePath = getDestPath();    
            URL url = new URL(URLStr);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            InputStream inputStream = httpConn.getInputStream();

            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = -1;
            totalBytesRead = 0;
            int percentCompleted = 0;
            int op=-1;
            while ((bytesRead = inputStream.read(buffer)) != -1 && !isCancelled()) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;               
                try{
                    int desiredKBPS = 3000;
                    long sleepTime = Math.round(BUFFER_SIZE*1000 / (desiredKBPS << 10));
                    Thread.sleep(sleepTime);
                }catch(InterruptedException e){
                    
                }
                //x1000 to force more frequent updates of status message.
                percentCompleted = (int) (totalBytesRead * 1000 / filesize);

                if(op != percentCompleted){
                    notification.setProgress(totalBytesRead/(double)filesize);                                        
                    notification.setStatusMessage(Math.round((totalBytesRead/1024.0/1024.0))+"M of "+Math.round((filesize/1024.0/1024.0))+"M");
                }
                
                op = percentCompleted;
            }

            outputStream.close();
            
            httpConn.disconnect();
           
        } catch (IOException ex) {
            LOG.error(ex);
            cancelDownload();    
        }
        return null;
    }
}