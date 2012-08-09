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

package org.ut.biolab.medsavant.format;

import java.io.Serializable;

/**
 *
 * @author Andrew
 */
public class AnnotationFormat implements Serializable {

    private final String program;
    private final String version;
    private final String referenceName;
    private final String path;
    private final boolean hasRef;
    private final boolean hasAlt;
    private final CustomField[] fields;
    private final AnnotationType type;

    public AnnotationFormat(
            String program,
            String version,
            String referenceName,
            String path,
            boolean hasRef,
            boolean hasAlt,
            AnnotationType type,
            CustomField[] fields){
        this.program = program;
        this.version = version;
        this.referenceName = referenceName;
        this.path = path;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.fields = fields;
        this.type = type;
    }

    public String generateSchema(){
        String result = "";

        //add custom columns
        for(int i = 0; i < fields.length; i++){
            CustomField field = fields[i];
            String columnName = field.getColumnName();
            String columnType = field.getSQLFieldTypeString();
            result += "`" + columnName + "` " + columnType + " DEFAULT NULL,";
        }

        return result;
    }

    public int getNumNonDefaultFields(){
        return fields.length;
    }

    public boolean hasRef(){
        return hasRef;
    }

    public boolean hasAlt(){
        return hasAlt;
    }

    public CustomField[] getCustomFields(){
        return fields;
    }

    public String getProgram(){
        return program;
    }

    public String getVersion(){
        return version;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public AnnotationType getType() {
        return type;
    }

    public static enum AnnotationType {
        POSITION,
        INTERVAL;

        public static AnnotationType fromInt(int type) {
            switch (type){
                case 0:
                    return AnnotationType.POSITION;
                case 1:
                    return AnnotationType.INTERVAL;
                default:
                    return null;
            }
        }

        public static int toInt(AnnotationType type) {
            switch (type){
                case POSITION:
                    return 0;
                case INTERVAL:
                    return 1;
                default:
                    return -1;
            }
        }

        public static AnnotationType fromString(String s) {
            if (s.toLowerCase().equals("interval")) {
                return AnnotationType.INTERVAL;
            } else {
                return AnnotationType.POSITION;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case POSITION:
                    return "Position";
                case INTERVAL:
                    return "Interval";
                default:
                    return "";
            }
        }
    };

    @Override
    public String toString() {
        return "AnnotationFormat{" + "program=" + program + ", version=" + version + ", referenceName=" + referenceName + ", path=" + path + ", hasRef=" + hasRef + ", hasAlt=" + hasAlt + ", fields=" + fields + ", type=" + type + '}';
    }


}
