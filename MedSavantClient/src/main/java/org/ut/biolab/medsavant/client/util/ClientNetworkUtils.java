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
package org.ut.biolab.medsavant.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.ut.biolab.medsavant.MedSavantClient;
import org.ut.biolab.medsavant.client.api.Listener;
import org.ut.biolab.medsavant.client.login.LoginController;
import org.ut.biolab.medsavant.shared.serverapi.NetworkManagerAdapter;
import org.ut.biolab.medsavant.shared.util.NetworkUtils;

/**
 * @author Andrew
 */
public class ClientNetworkUtils extends NetworkUtils {
    /**
     * Download a file in the background. Notification events will be sent to the supplied listener.
     * 
     * @param u the HTTP URL to be downloaded
     * @param destDir destination directory for the file
     * @param fileName the destination file within <code>destDir</code>; use <code>null</code> to infer the name from
     *        the URL
     * @param monitor will receive DownloadEvents
     */
    public static void downloadFile(final URL u, final File destDir, final String fileName,
        final DownloadMonitor monitor) {
        new Thread("NetworkUtils.downloadFile") {
            double totalBytes;

            @Override
            public void run() {
                try {
                    HttpURLConnection httpConn = (HttpURLConnection) u.openConnection();
                    this.totalBytes = httpConn.getContentLength();

                    File destFile =
                        new File(destDir, fileName != null ? fileName
                            : org.ut.biolab.medsavant.client.util.ClientMiscUtils.getFilenameFromPath(u.getPath()));
                    OutputStream out = new FileOutputStream(destFile);
                    InputStream in = openStream(u);
                    fireDownloadEvent(monitor, new DownloadEvent(DownloadEvent.Type.STARTED));
                    byte[] buf = new byte[BUF_SIZE];
                    long bytesSoFar = 0;
                    int bytesRead;
                    while ((bytesRead = in.read(buf)) != -1 && !monitor.isCancelled()) {
                        out.write(buf, 0, bytesRead);
                        if (this.totalBytes > 0.0) {
                            bytesSoFar += bytesRead;
                            monitor.handleEvent(new DownloadEvent(bytesSoFar / this.totalBytes));
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

    public static int copyFileToServer(File file) throws IOException, InterruptedException {
        NetworkManagerAdapter netMgr = MedSavantClient.NetworkManager;
        int streamID = -1;
        InputStream stream = null;

        try {
            streamID =
                netMgr.openWriterOnServer(LoginController.getInstance().getSessionID(), file.getName(), file.length());
            stream = new FileInputStream(file);

            int numBytes;
            byte[] buf = null;
            while ((numBytes = Math.min(stream.available(), NetworkManagerAdapter.BLOCK_SIZE)) > 0) {
                if (buf == null || numBytes != buf.length) {
                    buf = new byte[numBytes];
                }
                stream.read(buf);
                netMgr.writeToServer(LoginController.getInstance().getSessionID(), streamID, buf);
            }
        } finally {
            if (streamID >= 0) {
                netMgr.closeWriterOnServer(LoginController.getInstance().getSessionID(), streamID);
            }
            if (stream != null) {
                stream.close();
            }
        }

        return streamID;
    }

    /**
     * Copy a file from the server. The provided <code>streamID</code> will have been assigned during an earlier server
     * call, so this method is not responsible for opening the stream.
     * 
     * @param streamID assigned by server
     * @param destFile path to file where we're dumping server output
     */
    public static void copyFileFromServer(int streamID, File destFile) throws IOException, InterruptedException {
        NetworkManagerAdapter netMgr = MedSavantClient.NetworkManager;

        OutputStream stream = null;
        try {
            stream = new FileOutputStream(destFile);
            byte[] buf;
            while ((buf = netMgr.readFromServer(LoginController.getInstance().getSessionID(), streamID)) != null) {
                stream.write(buf);
                stream.flush();
            }
        } finally {
            netMgr.closeReaderOnServer(LoginController.getInstance().getSessionID(), streamID);
            if (stream != null) {
                stream.close();
            }
        }
    }
}
