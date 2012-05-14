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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.List;

import org.ut.biolab.medsavant.format.AnnotationFormat;
import org.ut.biolab.medsavant.model.Annotation;

/**
 *
 * @author mfiume
 */
public interface AnnotationQueryUtilAdapter extends Remote {

    public List<Annotation> getAnnotations(String sid) throws SQLException, RemoteException;
    public Annotation getAnnotation(String sid,int annotation_id) throws SQLException, RemoteException;
    public int[] getAnnotationIds(String sid,int projectId, int referenceId) throws SQLException, RemoteException;
    public AnnotationFormat getAnnotationFormat(String sid,int annotationId) throws SQLException;
    public int addAnnotation(String sid,String program, String version, int referenceid, String path, boolean hasRef, boolean hasAlt, int type) throws SQLException, RemoteException;
    public void addAnnotationFormat(String sid,int annotationId, int position, String columnName, String columnType, boolean isFilterable, String alias, String description) throws SQLException, RemoteException;
}
