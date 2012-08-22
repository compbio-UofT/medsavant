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

package org.ut.biolab.medsavant.serverapi;

import java.io.*;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ut.biolab.medsavant.db.connection.ConnectionController;
import org.ut.biolab.medsavant.db.connection.PooledConnection;
import org.ut.biolab.medsavant.model.UserLevel;
import org.ut.biolab.medsavant.server.MedSavantServerUnicastRemoteObject;
import org.ut.biolab.medsavant.util.DirectorySettings;


/**
 *
 * @author mfiume
 */
public class NetworkManager extends MedSavantServerUnicastRemoteObject implements NetworkManagerAdapter {

    private static final HashMap<Integer, BufferedWriter> writerMap = new HashMap<Integer,BufferedWriter>();
    private static final HashMap<Integer, BufferedReader> readerMap = new HashMap<Integer,BufferedReader>();

    private static final HashMap<Integer, String> inboundFileNameMap = new HashMap<Integer,String>();
    private static final HashMap<Integer, String> outboundFileNameMap = new HashMap<Integer,String>();

    private static NetworkManager instance;
    private static int counter = 0;

    public static synchronized NetworkManager getInstance() throws RemoteException {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }


    private NetworkManager() throws RemoteException {
    }

    @Override
    public int openFileWriterOnServer(String sessID) throws RemoteException, IOException {

        int fileID = counter;
        counter++;

        File outFile = File.createTempFile("sentfile",".medsavant",DirectorySettings.getTmpDirectory());

        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        writerMap.put(fileID, bw);
        inboundFileNameMap.put(fileID,outFile.getAbsolutePath());

        return fileID;
    }

    @Override
    public void writeLineToServer(String sessID, int fileIdentifier, String line) throws SQLException, RemoteException, IOException {
        writerMap.get(fileIdentifier).write(line + "\n");
    }

    @Override
    public void closeFileWriterOnServer(String sessID, int fileIdentifier) throws RemoteException, IOException {
        writerMap.get(fileIdentifier).close();
        writerMap.remove(fileIdentifier);
    }

    public static File getFileByTransferID(int id) {
        return new File(inboundFileNameMap.get(id));
    }

    public int registerFileForTransferToClient(File file) {
        int fileID = counter;
        counter++;
        outboundFileNameMap.put(fileID,file.getAbsolutePath());
        return fileID;
    }

    @Override
    public void openFileReaderOnServer(String sessID, int fileID) throws IOException, RemoteException {
        // TODO: for security purposes, make the key a combination of sessID x fileID
        BufferedReader br = new BufferedReader(new FileReader(outboundFileNameMap.get(fileID)));
        readerMap.put(fileID, br);
    }

    @Override
    public String readLineFromServer(String sessID, int fileID) throws IOException, SQLException, RemoteException {
        // TODO: for security purposes, fetch reader by sessID x fileID
        return readerMap.get(fileID).readLine();
    }

    @Override
    public void closeFileReaderOnServer(String sessID, int fileID) throws IOException, RemoteException {
        readerMap.get(fileID).close();
    }

}
