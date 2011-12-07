/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.util;

import org.ut.biolab.medsavant.api.Listener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Andrew
 */
public class NetworkUtils {
    
    /**
     * Download a file in the background.  Notification events will be sent to the
     * supplied listener.
     *
     * @param u the HTTP URL to be downloaded
     * @param destDir destination directory for the file
     * @param fileName the destination file within <code>destDir</code>; use <code>null</code> to infer the name from the URL
     * @param monitor will receive DownloadEvents
     */
    public static void downloadFile(final URL u, final File destDir, final String fileName, final DownloadMonitor monitor) {
        new Thread("NetworkUtils.downloadFile") {
            double totalBytes;

            @Override
            public void run() {
                try {
                    HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
                    totalBytes = httpConn.getContentLength();

                    File destFile = new File(destDir, fileName != null ? fileName : org.ut.biolab.medsavant.db.util.MiscUtils.getFilenameFromPath(u.getPath()));
                    OutputStream out = new FileOutputStream(destFile);
                    InputStream in = org.ut.biolab.medsavant.db.util.NetworkUtils.openStream(u);
                    fireDownloadEvent(monitor, new DownloadEvent(DownloadEvent.Type.STARTED));
                    byte[] buf = new byte[org.ut.biolab.medsavant.db.util.NetworkUtils.BUF_SIZE];
                    long bytesSoFar = 0;
                    int bytesRead;
                    while ((bytesRead = in.read(buf)) != -1 && !monitor.isCancelled()) {
                        out.write(buf, 0, bytesRead);
                        if (totalBytes > 0.0) {
                            bytesSoFar += bytesRead;
                            monitor.handleEvent(new DownloadEvent(bytesSoFar / totalBytes));
                        }
                    }
                    fireDownloadEvent(monitor, new DownloadEvent(destFile));
                } catch (IOException x) {
                    fireDownloadEvent(monitor, new DownloadEvent(x));
                }
            }
        }.start();
    }

    private static void fireDownloadEvent(final Listener<DownloadEvent> listener, final DownloadEvent e) {
        org.ut.biolab.medsavant.db.util.MiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                listener.handleEvent(e);
            }
        });
    }
    
}
