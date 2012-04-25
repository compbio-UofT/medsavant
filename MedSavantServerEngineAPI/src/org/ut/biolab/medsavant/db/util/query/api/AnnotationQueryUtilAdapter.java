package org.ut.biolab.medsavant.db.util.query.api;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.model.Annotation;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public interface AnnotationQueryUtilAdapter extends Remote {

    public List<Annotation> getAnnotations(String sid) throws SQLException, RemoteException;
    public Annotation getAnnotation(String sid,int annotation_id) throws SQLException, RemoteException;
    public int[] getAnnotationIds(String sid,int projectId, int referenceId) throws SQLException, RemoteException;
    public AnnotationFormat getAnnotationFormat(String sid,int annotationId) throws SQLException, IOException, ParserConfigurationException, SAXException, RemoteException;
    public int addAnnotation(String sid,String program, String version, int referenceid, String path, boolean hasRef, boolean hasAlt, int type) throws SQLException, RemoteException;
    public void addAnnotationFormat(String sid,int annotationId, int position, String columnName, String columnType, boolean isFilterable, String alias, String description) throws SQLException, RemoteException;
}
