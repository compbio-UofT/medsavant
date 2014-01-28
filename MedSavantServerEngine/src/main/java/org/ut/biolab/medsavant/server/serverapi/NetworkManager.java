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
package org.ut.biolab.medsavant.server.serverapi;

import java.io.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.shared.serverapi.NetworkManagerAdapter;
import org.ut.biolab.medsavant.shared.util.DirectorySettings;
import org.ut.biolab.medsavant.shared.util.MiscUtils;


/**
 *
 * @author mfiume
 */
public class NetworkManager extends MedSavantServerUnicastRemoteObject implements NetworkManagerAdapter {

    private static final Log LOG = LogFactory.getLog(NetworkManager.class);

    private static NetworkManager instance;

    private int counter = 0;
    private final HashMap<String, InboundStreamInfo> inboundMap = new HashMap<String, InboundStreamInfo>();
    private final HashMap<String, OutboundStreamInfo> outboundMap = new HashMap<String, OutboundStreamInfo>();
    private byte[] outboundBuffer = new byte[BLOCK_SIZE];

    public static synchronized NetworkManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    private NetworkManager() throws RemoteException {
    }

    @Override
    public int openWriterOnServer(String sessID, String name, long fileLen) throws IOException {

        int transferID = counter;

        LOG.info("Opening writer on server, transfer ID is " + transferID);

        counter++;

        String fileName = (new File(name)).getName();

        File outFile = new File(DirectorySettings.getTmpDirectory(),fileName);
        if(!outFile.canWrite()){
            if(!outFile.getParentFile().mkdirs()){
                String s = "Cannot write file "+outFile.getAbsolutePath()+" because parent directories could not be created.";
                LOG.error(s);
                throw new IOException(s);
            }
        }
        int i = 0;
        while (outFile.exists()) {
            i++;
            String incrementedFn = i + "-" + fileName;
            outFile = new File(DirectorySettings.getTmpDirectory(),incrementedFn);
        }
        inboundMap.put(sessID + transferID, new InboundStreamInfo(outFile, name, fileLen));

        LOG.info("File will transfer to " + outFile.getAbsolutePath());

        return transferID;
    }

    @Override
    public void writeToServer(String sessID, int transferID, byte[] buf) throws IOException, InterruptedException {
        InboundStreamInfo info = inboundMap.get(sessID + transferID);
        info.stream.write(buf);
        info.bytesWritten += buf.length;
        makeProgress(sessID, String.format("Uploading %s to server...", info.sourceName), (double)info.bytesWritten / info.length);
    }

    /**
     * Close the stream.  We keep the <code>StreamInfo</code> around because we may still want to get the transfer ID.
     * @param sessID uniquely identifies the client
     * @param transferID identifies the stream which is being closed
     * @throws IOException
     */
    @Override
    public void closeWriterOnServer(String sessID, int transferID) throws IOException {

        InboundStreamInfo info = inboundMap.get(sessID + transferID);
        info.stream.close();
        info.stream = null;

        LOG.info("Closed transfer with ID " + transferID);
    }

    /**
     * Get the path to the local file corresponding to the given transfer.
     * @param sessID uniquely identifies the client
     * @param transferID identifies the transfer whose name we want
     */
    public File getFileByTransferID(String sessID, int transferID) {
        for (String key : inboundMap.keySet()) {
            LOG.info("\t" + key + " " + inboundMap.get(key).file.getAbsolutePath());
        }
        LOG.info("Looking up file with ID " + sessID + transferID);
        return inboundMap.get(sessID + transferID).file;
    }

    /**
     * Get the name of the remote source file corresponding to the given transfer.
     * @param sessID uniquely identifies the client
     * @param transferID identifies the transfer whose name we want
     */
    public String getSourceNameByTransferID(String sessID, int transferID) {
        return inboundMap.get(sessID + transferID).sourceName;
    }

    public int openReaderOnServer(String sessID, File f) throws IOException {
        int transferID = counter;
        counter++;

        outboundMap.put(sessID + transferID, new OutboundStreamInfo(f));

        return transferID;
    }

    @Override
    public byte[] readFromServer(String sessID, int transferID) throws IOException, InterruptedException {
        OutboundStreamInfo info = outboundMap.get(sessID + transferID);
        int numBytes = info.stream.available();
        if (numBytes > 0) {
            if (numBytes != outboundBuffer.length) {
                outboundBuffer = new byte[numBytes];
            }
            info.stream.read(outboundBuffer);
            info.bytesRead += outboundBuffer.length;
            makeProgress(sessID, "Downloading from server...", (double)info.bytesRead / info.length);
            return outboundBuffer;
        }
        return null;
    }

    /**
     * Close the stream.  We don't need the <code>StreamInfo</code> any more, so we remove it from the map.
     * @param sessID uniquely identifies the client
     * @param transferID identifies the stream which is being closed
     */
    @Override
    public void closeReaderOnServer(String sessID, int transferID) throws IOException {
        OutboundStreamInfo info = outboundMap.remove(sessID + transferID);
        info.stream.close();
    }

    /**
     * Keeps track of all information about a given stream transmitting data from client to local file.
     */
    private class InboundStreamInfo {
        final File file;
        final String sourceName;
        final long length;
        OutputStream stream;
        long bytesWritten = 0;

        InboundStreamInfo(File f, String name, long len) throws FileNotFoundException {
            file = f;
            sourceName = name;
            length = len;
            stream = new FileOutputStream(f);
        }
    }


    /**
     * Keeps track of all information about a given stream being transmitted from local file to client.
     */
    private class OutboundStreamInfo {
        final File file;
        final long length;
        InputStream stream;
        long bytesRead = 0;

        OutboundStreamInfo(File f) throws FileNotFoundException {
            file = f;
            length = f.length();
            stream = new FileInputStream(f);
        }
    }
}
