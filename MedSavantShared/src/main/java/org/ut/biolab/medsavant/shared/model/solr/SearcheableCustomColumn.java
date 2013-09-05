package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.db.ColumnType;
import org.ut.biolab.medsavant.shared.model.CustomColumn;
import org.ut.biolab.medsavant.shared.model.CustomColumnType;

/**
 *  Adapter class for indexing custom columns.
 */
public class SearcheableCustomColumn {

    private CustomColumn customColumn;

    public SearcheableCustomColumn() {
        customColumn = new CustomColumn();
    }

    public SearcheableCustomColumn(CustomColumn customColumn) {
        this.customColumn = customColumn;
    }

    @Field("project_id")
    public void setProjectId(int projectId) {
        this.customColumn.setProjectId(projectId);
    }

    @Field("reference_id")
    public void setReferenceId(int referenceId) {
        this.customColumn.setReferenceId(referenceId);
    }

    @Field("upload_id")
    public void setUploadId(long uploadId) {
        this.customColumn.setUploadId((int) uploadId);
    }

    @Field("position")
    public void setPosition(int position) {
        this.customColumn.setPosition(position);
    }

    @Field("entity_name")
    public void setEntity(String entity) {
        this.customColumn.setEntity(CustomColumnType.valueOf(entity));
    }

    @Field("name")
    public void setName(String name) {
        this.customColumn.setName(name);
    }

    @Field("type")
    public void setType(String type) {
        this.customColumn.setType(ColumnType.fromString(type));
    }
    @Field("filterable")
    public void setFilterable(boolean filterable) {
        this.customColumn.setFilterable(filterable);
    }

    @Field("alias")
    public void setAlias(String alias) {
        this.customColumn.setAlias(alias);
    }

    @Field("description")
    public void setDescription(String description) {
        this.customColumn.setDescription(description);
    }

    public int getProjectId() {
        return customColumn.getProjectId();
    }

    public int getReferenceId() {
        return customColumn.getReferenceId();
    }

    public int getUploadId() {
        return customColumn.getUploadId();
    }

    public int getPosition() {
        return customColumn.getPosition();
    }

    public boolean getFilterable() {
        return customColumn.isFilterable();
    }

    public String getName() {
        return customColumn.getColumnName();
    }

    public String getDescription() {
        return customColumn.getDescription();
    }

    public String getAlias() {
        return customColumn.getAlias();
    }

    public CustomColumnType getEntity() {
        return customColumn.getEntity();
    }

    public ColumnType getType() {
        return customColumn.getColumnType();
    }

    public CustomColumn getCustomColumn() {
        return customColumn;
    }


}

