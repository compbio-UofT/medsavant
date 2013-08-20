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

/**
 *
 * @author jim
 */
public abstract class DownloadTask extends SwingWorker {

    private final String URLStr;
    private final String destPath;
    private final long filesize;
    private long totalBytesRead = 0;
    private final String filename;
    private static final int BUFFER_SIZE = 4096;  

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
    
    public DownloadTask(String URLStr, String destPath) throws IOException {
        this.URLStr = URLStr;
        this.destPath = destPath;
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

    protected abstract void done();

    protected Object doInBackground() {        
      //  if(true){ try{ Thread.sleep(2000); }catch(Exception e){ System.out.println(e); } return null; }
        try {          
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

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                percentCompleted = (int) (totalBytesRead * 100 / filesize);

                setProgress(percentCompleted);
            }

            outputStream.close();
            
            httpConn.disconnect();
           
        } catch (IOException ex) {
            System.out.println(ex);
            setProgress(0);
            cancel(true);
        }
        return null;
    }
}