package org.ut.biolab.medsavant.shared.model.solr;

import org.apache.solr.client.solrj.beans.Field;
import org.ut.biolab.medsavant.shared.format.AnnotationFormat;
import org.ut.biolab.medsavant.shared.model.Annotation;

/**
 * Adapter class for Annotation objects.
 */
public class SearcheableAnnotation {

    private Annotation annotation;

    private int id;
    private String program;
    private String version;
    private int referenceID;
    private String referenceName;
    private String dataPath;
    private AnnotationFormat.AnnotationType type;
    private boolean isEndInclusive = false;

    public SearcheableAnnotation() { }

    public SearcheableAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    @Field("id")
    public void setId(int id) {
        this.id = id;
    }

    @Field("program")
    public void setProgram(String program) {
        this.program = program;
    }

    @Field("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @Field("reference_id")
    public void setReferenceID(int referenceID) {
        this.referenceID = referenceID;
    }

    @Field("reference_name")
    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    @Field("data_path")
    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    @Field("type")
    public void setType(AnnotationFormat.AnnotationType type) {
        this.type = type;
    }

    @Field("end_inclusive")
    public void setEndInclusive(boolean endInclusive) {
        isEndInclusive = endInclusive;
    }

    public Annotation getAnnotation() {
        this.annotation = new Annotation(id, program, version, referenceID, referenceName,dataPath,type, isEndInclusive);
        return annotation;
    }

    public int getId() {
        return id;
    }

    public String getProgram() {
        return program;
    }

    public String getVersion() {
        return version;
    }

    public int getReferenceID() {
        return referenceID;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getDataPath() {
        return dataPath;
    }

    public AnnotationFormat.AnnotationType getType() {
        return type;
    }

    public boolean isEndInclusive() {
        return isEndInclusive;
    }
}
