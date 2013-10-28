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
package org.ut.biolab.medsavant.shared.model;

import java.io.Serializable;


/**
 *
 * @author Andrew
 */
public class ProjectDetails implements Serializable {

    private final int projectID;
    private final int referenceID;
    private final int updateID;
    private final boolean published;
    private final String projectName;
    private final String referenceName;
    private final int[] annotationIDs;

    public ProjectDetails(int projID, int refID, int updID, boolean published, String projName, String refNam, int[] annIDs) {
        this.projectID = projID;
        this.referenceID = refID;
        this.updateID = updID;
        this.published = published;
        this.referenceName = refNam;
        this.annotationIDs = annIDs;
        this.projectName = projName;
    }

    public int getProjectID() {
        return projectID;
    }

    public int getReferenceID() {
        return referenceID;
    }

    public int getUpdateID() {
        return updateID;
    }

    public int[] getAnnotationIDs(){
        return annotationIDs;
    }

    public boolean isPublished(){
        return published;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getProjectName() {
        return projectName;
    }

    public int getNumAnnotations() {
        return annotationIDs.length;
    }
}
