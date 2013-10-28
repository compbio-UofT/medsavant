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
package org.ut.biolab.medsavant.shared.format;

import java.io.Serializable;



/**
 *
 * @author Andrew
 */
public class AnnotationFormat implements Serializable {

    public static final String ANNOTATION_FORMAT_DEFAULT = "Standard Variant Conditions";
    public static final String ANNOTATION_FORMAT_CUSTOM_VCF = "VCF Conditions";

    private final String program;
    private final String version;
    private final String referenceName;
    private final String path;
    private final boolean hasRef;
    private final boolean hasAlt;
    private final CustomField[] fields;
    private final AnnotationType type;
    private final boolean isEndInclusive;

    public AnnotationFormat(
            String program,
            String version,
            String referenceName,
            String path,
            boolean hasRef,
            boolean hasAlt,
            AnnotationType type,
            boolean isEndInclusive,
            CustomField[] fields){
        this.program = program;
        this.version = version;
        this.referenceName = referenceName;
        this.path = path;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.fields = fields;
        this.type = type;
        this.isEndInclusive = isEndInclusive;
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

    public boolean isEndInclusive() {
        return isEndInclusive;
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
        return "AnnotationFormat{" + "program=" + program + ", version=" + version + ", referenceName=" + referenceName + ", path=" + path + ", hasRef=" + hasRef + ", hasAlt=" + hasAlt + ", fields=" + fields + ", type=" + type + ", isEndInclusive=" + isEndInclusive  + '}';
    }

    public static AnnotationFormat getDefaultAnnotationFormat() {
        return new AnnotationFormat(ANNOTATION_FORMAT_DEFAULT, ANNOTATION_FORMAT_DEFAULT, "0", "", true, true, AnnotationType.POSITION, false, BasicVariantColumns.REQUIRED_VARIANT_FIELDS);
    }

    public static AnnotationFormat getCustomFieldAnnotationFormat(CustomField[] customFields) {
        return new AnnotationFormat(ANNOTATION_FORMAT_CUSTOM_VCF, ANNOTATION_FORMAT_CUSTOM_VCF, "0", "", true, true, AnnotationType.POSITION, false, customFields);
    }
}
