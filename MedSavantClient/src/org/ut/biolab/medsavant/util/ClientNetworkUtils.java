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

package org.ut.biolab.medsavant.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;

import org.ut.biolab.medsavant.api.Listener;

/**
 *
 * @author Andrew
 */
public class ClientNetworkUtils extends NetworkUtils {
    
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

                    File destFile = new File(destDir, fileName != null ? fileName : org.ut.biolab.medsavant.util.ClientMiscUtils.getFilenameFromPath(u.getPath()));
                    OutputStream out = new FileOutputStream(destFile);
                    InputStream in = openStream(u);
                    fireDownloadEvent(monitor, new DownloadEvent(DownloadEvent.Type.STARTED));
                    byte[] buf = new byte[BUF_SIZE];
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
        org.ut.biolab.medsavant.util.ClientMiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                listener.handleEvent(e);
            }
        });
    }
    
    public static File copyFileFromRemoteStream(RemoteInputStream ris) throws IOException{
        InputStream istream = RemoteInputStreamClient.wrap(ris);
        FileOutputStream ostream = null;
        File tempFile;

        try {

            tempFile = File.createTempFile("sentFile_", ".dat");
            ostream = new FileOutputStream(tempFile);
            System.out.println("Writing file " + tempFile);

            byte[] buf = new byte[1024];

            int bytesRead = 0;
            while ((bytesRead = istream.read(buf)) >= 0) {
                ostream.write(buf, 0, bytesRead);
            }
            ostream.flush();

            System.out.println("Finished writing file " + tempFile);

        } finally {
            try {
                if (istream != null) {
                    istream.close();
                }
            } finally {
                if (ostream != null) {
                    ostream.close();
                }
            }
        }
        
        return tempFile;
    }    
}
