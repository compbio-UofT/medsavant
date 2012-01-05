package org.ut.biolab.medsavant.db.variants.upload.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author mfiume
 */
public interface UploadVariantsAdapter extends Remote
{

  public int uploadVariants(String sid, RemoteInputStream[] fileStreams, int projectId, int referenceId) throws RemoteException, IOException, Exception;

  public void annotateVariants(String sid, int projectID, int referenceID, int updateID) throws RemoteException;

}

