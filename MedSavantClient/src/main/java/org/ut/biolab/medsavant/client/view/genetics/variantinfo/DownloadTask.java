/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ut.biolab.medsavant.client.util.notification.VisibleMedSavantWorker;
import org.ut.biolab.medsavant.client.view.ViewController;

/**
 *
 * @author jim
 */
public abstract class DownloadTask extends VisibleMedSavantWorker<Void> {

    private static final Log LOG = LogFactory.getLog(DownloadTask.class);
    private final String URLStr;
    private final String destPath;
    private final long filesize;
    private long totalBytesRead = 0;
    private final String filename;
    private static final int BUFFER_SIZE = 4096;

    public enum DownloadState {

        RUNNING, FINISHED, CANCELLED, NOT_STARTED
    };
    private DownloadState downloadState = DownloadState.NOT_STARTED;

    @Override
    protected void jobDone() {
        super.jobDone(); 
        setDownloadState(DownloadState.FINISHED);
    }

    
    
    protected void setDownloadState(DownloadState downloadState) {
        DownloadState oldDownloadState = this.downloadState;
        this.downloadState = downloadState;
        firePropertyChange("downloadState", oldDownloadState, downloadState);
    }

    public DownloadState getDownloadState() {
        return downloadState;
    }

    private String extractFileFromURL() {
        return URLStr.substring(URLStr.lastIndexOf("/") + 1,
                URLStr.length());
    }

    public String getDestPath() {
        if (destPath.charAt(destPath.length() - 1) == File.separatorChar) {
            return destPath + filename;
        } else {
            return destPath + File.separator + filename;
        }
    }

    
    public DownloadTask(String URLStr, String destPath, String notificationTitle) throws IOException {
        //do not want download tasks to be interrupted by changing subsection views, so 
        //use the class name for the task.        
        super(DownloadTask.class.getSimpleName(), notificationTitle);

        this.URLStr = URLStr;
        this.destPath = destPath;
        showResultsOnFinish(false);

        int filesize;
        String filename;

        URL url = new URL(URLStr);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("File " + URLStr + "unavailable from server");
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

    public long getFileSize() {
        return filesize;
    }

    public long getTotalBytesSoFar() {
        return totalBytesRead;
    }

    public void cancelDownload() {
        setDownloadState(DownloadState.CANCELLED);
        cancel(true);
    }

    //Called when 'cancel' button is pushed from within super.cancel(true)
    @Override
    protected void cancelJob() {
        super.cancelJob(); 
        setDownloadState(DownloadState.CANCELLED);
    }

    
    
    @Override
    protected Void runInBackground() {
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
            int op = -1;
           
            while ((bytesRead = inputStream.read(buffer)) != -1 && !isCancelled()) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;                             
                
                //x1000 to force more frequent updates of status message.
                percentCompleted = (int) (totalBytesRead * 1000 / filesize);

                if (op != percentCompleted) {
                    setProgress(totalBytesRead / (double) filesize);
                    setStatusMessage(Math.round((totalBytesRead / 1024.0 / 1024.0)) + "M of " + Math.round((filesize / 1024.0 / 1024.0)) + "M");
                }

                op = percentCompleted;

            }

            outputStream.close();

            httpConn.disconnect();

        } catch (IOException ex) {
            LOG.error(ex);
            cancelDownload();
        }catch(Exception ex){
            LOG.error("Unexpected exception: "+ex);                        
            cancelDownload();
        }
        return null;
    }
}