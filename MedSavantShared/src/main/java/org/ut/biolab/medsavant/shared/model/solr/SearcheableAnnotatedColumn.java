package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.model.AnnotatedColumn;

/**
 *
 */
public class SearcheableAnnotatedColumn {

    private AnnotatedColumn column;

    private int annotationId;
    private int position;
    private String name;
    private String type;
    private boolean filterable;
    private String alias;
    private String description;

    public SearcheableAnnotatedColumn() { }

    public SearcheableAnnotatedColumn(AnnotatedColumn column) {
        this.column = column;
    }

    @Field("annotation_id")
    public void setAnnotationId(int annotationId) {

        this.annotationId = annotationId;
    }

    @Field("position")
    public void setPosition(int position) {
        this.position = position;
    }

    @Field("name")
    public void setName(String name) {
        this.name = name;
    }

    @Field("type")
    public void setType(String type) {
        this.type = type;
    }

    @Field("filterable")
    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    @Field("alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Field("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public AnnotatedColumn getColumn() {
        column = new AnnotatedColumn(annotationId,position,name,type,filterable,alias,description);
        return column;
    }

    public int getAnnotationId() {
        return annotationId;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public String getAlias() {
        return alias;
    }

    public String getDescription() {
        return description;
    }
}
