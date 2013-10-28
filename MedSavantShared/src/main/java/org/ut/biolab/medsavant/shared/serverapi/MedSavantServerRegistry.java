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

/**
 *
 * @author mfiume
 */
public interface MedSavantServerRegistry {

    public static final String SESSION_MANAGER = "SessionManager";

    public static final String CUSTOM_TABLES_MANAGER = "CustomTablesManager";
    public static final String ANNOTATION_LOG_MANAGER = "AnnotationLogManager";
    public static final String ANNOTATION_MANAGER = "AnnotationManager";
    public static final String COHORT_MANAGER = "CohortManager";
    public static final String GENE_SET_MANAGER = "GeneSetManager";
    public static final String LOG_MANAGER = "LogManager";
    public static final String ONTOLOGY_MANAGER = "OntologyManager";
    public static final String NETWORK_MANAGER = "NetworkManager";
    public static final String PATIENT_MANAGER = "PatientManager";
    public static final String PROJECT_MANAGER = "ProjectManager";
    public static final String QUERY_MANAGER = "QueryManager";
    public static final String REFERENCE_MANAGER = "ReferenceManager";
    public static final String REGION_SET_MANAGER = "RegionSetManager";
    public static final String SETTINGS_MANAGER = "SettingsManager";
    public static final String USER_MANAGER = "UserManager";
    public static final String VARIANT_MANAGER = "VariantManager";
    public static final String DB_UTIL_MANAGER = "DBUtilManager";
    public static final String SETUP_MANAGER = "SetupManager";
    public static final String NOTIFICATION_MANAGER = "NotificationManager";
}
