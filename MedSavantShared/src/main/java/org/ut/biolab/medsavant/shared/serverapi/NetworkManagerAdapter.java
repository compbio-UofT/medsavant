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
package org.ut.biolab.medsavant.shared.serverapi;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.ut.biolab.medsavant.shared.model.ProgressStatus;


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
