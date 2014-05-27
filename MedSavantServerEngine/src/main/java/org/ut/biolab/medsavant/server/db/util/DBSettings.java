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
package org.ut.biolab.medsavant.server.db.util;

/**
 *
 * @author mfiume
 */
public class DBSettings {
    public static String getVariantViewName(int projectID, int refID) {
        return "view_variants_" + projectID+"_"+refID;
    }
         
    public static String getVariantSubsetViewName(int projectID, int refID){
        return getVariantViewName(projectID, refID) + "_subset";
    }
    
    public static String getVariantTableName(int projectId, int referenceId, int updateId){
        return "z_variant_proj" + projectId + "_ref" + referenceId + "_update" + updateId;
    }

    @Deprecated
    public static String getVariantStagingTableName(int projectId, int referenceId, int updateId){
        return "z_variant_staging_proj" + projectId + "_ref" + referenceId + "_update" + updateId;
    }

    public static String createAnnotationFormatTableName(int annotationId){
        return "z_annotation_format" + annotationId;
    }

    public static String createPatientTableName(int projectId){
        return "z_patient_proj" + projectId;
    }

    public static String createPatientFormatTableName(int projectId){
        return "z_patient_format" + projectId;
    }

}
