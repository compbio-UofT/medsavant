/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ut.biolab.medsavant.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class AnnotationFormat implements Serializable {
    
    public static enum AnnotationType {POSITION, INTERVAL};
    
    public static AnnotationType intToAnnotationType(int type){
        switch(type){
            case 0:
                return AnnotationType.POSITION;
            case 1:
                return AnnotationType.INTERVAL;
            default:
                return null;
        }
    }
    
    public static int annotationTypeToInt(AnnotationType type){
        switch(type){
            case POSITION:
                return 0;
            case INTERVAL:
                return 1;
            default:
                return -1;
        }
    }
    
    public static String annotationTypToString(AnnotationType type) {
        switch(type) {
            case POSITION:
                return "Position";
            case INTERVAL:
                return "Interval";
            default:
                return "";
        }
    }
    
    public static int annotationTypeToInt(String type){
        return annotationTypeToInt(AnnotationType.valueOf(type.toUpperCase()));
    }
    
    private boolean hasRef;
    private boolean hasAlt;
    private String program;
    private String version;
    private int referenceId;
    private String path;
    private AnnotationType type;
    private List<CustomField> fields = new ArrayList<CustomField>();
    
    public AnnotationFormat(String program, String version, int referenceId, String path, boolean hasRef, boolean hasAlt, AnnotationType type, List<CustomField> fields){
        this.program = program;
        this.version = version;
        this.referenceId = referenceId;
        this.path = path;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.fields = fields;
        this.type = type;
    }
    
    public String generateSchema(){
        String result = "";
        
        //add custom columns
        for(int i = 0; i < fields.size(); i++){
            CustomField field = fields.get(i);
            String columnName = field.getColumnName();
            String columnType = field.getColumnTypeString();
            result += "`" + columnName + "` " + columnType + " DEFAULT NULL,";
        }

        return result;
    }
    
    public int getNumNonDefaultFields(){
        return fields.size();
    }
    
    public boolean hasRef(){
        return hasRef;
    }
    
    public boolean hasAlt(){
        return hasAlt;
    }
    
    public List<CustomField> getCustomFields(){
        return fields;
    }
    
    public String getProgram(){
        return program;
    }
    
    public String getVersion(){
        return version;
    }
    
}
