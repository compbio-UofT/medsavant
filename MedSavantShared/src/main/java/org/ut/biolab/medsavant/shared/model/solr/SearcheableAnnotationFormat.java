package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.format.CustomField;

/**
 * Adapter class for AnnotationFormat entities.
 */
public class SearcheableAnnotationFormat {

    private AnnotationFormat annotationFormat;
    private String program;
    private String version;
    private String referenceName;
    private String path;
    private boolean hasRef;
    private boolean hasAlt;
    private CustomField[] fields;
    private AnnotationFormat.AnnotationType type;
    private boolean isEndInclusive;

    public SearcheableAnnotationFormat() {  }

    public SearcheableAnnotationFormat(AnnotationFormat annotationFormat) {
        this.annotationFormat = annotationFormat;
    }

    public AnnotationFormat getAnnotationFormat() {
        this.annotationFormat = new AnnotationFormat(program,version,referenceName,path, hasRef, hasAlt,type,isEndInclusive,fields);
        return annotationFormat;
    }

    @Field("program")
    public void setProgram(String program) {
        this.program = program;
    }

    @Field("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @Field("reference_name")
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Field("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Field("has_ref")
    public void setHasRef(boolean hasRef) {
        this.hasRef = hasRef;
    }

    @Field("has_alt")
    public void setHasAlt(boolean hasAlt) {
        this.hasAlt = hasAlt;
    }

    @Field("custom_fields")
    public void setFields(CustomField[] fields) {
        this.fields = fields;
    }

    @Field("type")
    public void setType(AnnotationFormat.AnnotationType type) {
        this.type = type;
    }

    @Field("end_inclusive")
    public void setEndInclusive(boolean endInclusive) {
        isEndInclusive = endInclusive;
    }

    public String getProgram() {
        return program;
    }

    public String getVersion() {
        return version;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getPath() {
        return path;
    }

    public boolean isHasRef() {
        return hasRef;
    }

    public boolean isHasAlt() {
        return hasAlt;
    }

    public CustomField[] getFields() {
        return fields;
    }

    public AnnotationFormat.AnnotationType getType() {
        return type;
    }

    public boolean isEndInclusive() {
        return isEndInclusive;
    }
}
