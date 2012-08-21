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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import org.ut.biolab.medsavant.MedSavantClient;

import org.ut.biolab.medsavant.api.Listener;
import org.ut.biolab.medsavant.login.LoginController;

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
        ClientMiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                listener.handleEvent(e);
            }
        });
    }


    public static int copyFileToServer(File file) throws FileNotFoundException, IOException, SQLException {

        int id = MedSavantClient.NetworkManager.openFileWriterOnServer(LoginController.sessionId);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        int numLines = 0;
        while ((line = br.readLine()) != null) {
            numLines++;
            MedSavantClient.NetworkManager.writeLineToServer(LoginController.sessionId, id, line);
        }

        br.close();
        MedSavantClient.NetworkManager.closeFileWriterOnServer(LoginController.sessionId, id);

        return id;
    }

    public static void copyFileFromServer(int fileID, String absolutePath) throws IOException, SQLException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(absolutePath)));
        MedSavantClient.NetworkManager.openFileReaderOnServer(LoginController.sessionId, fileID);
        String line = "";
        while ((line = MedSavantClient.NetworkManager.readLineFromServer(LoginController.sessionId, fileID)) != null) {
            bw.write(line + "\n");
        }
        MedSavantClient.NetworkManager.closeFileReaderOnServer(LoginController.sessionId, fileID);
        bw.close();
    }
}
