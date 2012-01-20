package org.ut.biolab.medsavant.db.variants.upload.api;

import com.healthmarketscience.rmiio.RemoteInputStream;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import org.ut.biolab.medsavant.db.format.CustomField;

/**
 *
 * @author mfiume
 */
public interface VariantManagerAdapter extends Remote
{

  public int uploadVariants(String sid, RemoteInputStream[] fileStreams, int projectId, int referenceId, String[][] variantTags) throws RemoteException, IOException, Exception;
  //public void annotateVariants(String sid, int projectID, int referenceID, int updateID) throws RemoteException;
  public void publishVariants(String sid, int projectID, int referenceID, int updateID) throws Exception;
  public void publishVariants(String sid, int projectID) throws Exception;
  public int updateTable(String sid, int projectId, int referenceId, int[] annotationIds, List<CustomField> variantFields) throws Exception;

}

