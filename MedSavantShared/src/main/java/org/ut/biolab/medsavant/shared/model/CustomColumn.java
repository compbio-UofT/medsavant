package org.ut.biolab.medsavant.shared.model;

import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 * Use to store the custom columns for patient/variant table.
 */
public class CustomColumn extends CustomField {

    private int projectId;
    private int referenceId;
    private int uploadId;
    private int position;
    private CustomColumnType entity;

    public CustomColumn(CustomField customField, ProjectDetails projectDetails, CustomColumnType entity, int position) {
        super(customField.getColumnName(),
                customField.getTypeString(),
                customField.isFilterable(),
                customField.getAlias(),
                customField.getDescription());
        this.projectId = projectDetails.getProjectID();
        this.referenceId = projectDetails.getReferenceID();
        this.uploadId = projectDetails.getReferenceID();
        this.position = position;
        this.entity = entity;
    }

    public CustomColumn(String name, String typeStr, boolean filterable, String alias, String description) {
        super(name, typeStr, filterable, alias, description);
    }

    public CustomColumn(String n, ColumnType t, int l, boolean autoInc, boolean notNull, boolean indexed, String dflt, boolean filterable, String alias, String description) {
        super(n, t, l, autoInc, notNull, indexed, dflt, filterable, alias, description);
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    public int getUploadId() {
        return uploadId;
    }

    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public CustomColumnType getEntity() {
        return entity;
    }

    public void setEntity(CustomColumnType entity) {
        this.entity = entity;
    }
}

