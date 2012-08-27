/*
 *    Copyright 2012 University of Toronto
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
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.ut.biolab.medsavant.model.ProgressStatus;


/**
 * Public interface to utility methods for transmitting files to and from the server.
 *
 * @author mfiume
 */
public interface NetworkManagerAdapter extends Remote {

    /** By default, the network-manager transmits in 64kB blocks. */
    public static final int BLOCK_SIZE = 65536;

    /**
     * Check the status of a lengthy process, giving the user the option to cancel.
     */
    ProgressStatus checkProgress(String sessID, boolean userCancelled) throws RemoteException;

    /**
     * Open a stream for transmitting a file from the client to the server.
     * @param sessID uniquely identifies the client
     * @param name for display purposes, the name of the file being uploaded
     * @param fileLen length of file being uploaded, for purposes of progress
     * @return stream ID associated with this transmission
     */
    public int openWriterOnServer(String sessID, String name, long fileLen) throws IOException, RemoteException;

    public void writeToServer(String sessID, int fileID, byte[] buf) throws IOException, InterruptedException, RemoteException;
    public void closeWriterOnServer(String sessID, int fileID) throws IOException, RemoteException;

    public byte[] readFromServer(String sessID, int fileID) throws IOException, InterruptedException, RemoteException;
    public void closeReaderOnServer(String sessID, int fileID) throws IOException, RemoteException;

}
