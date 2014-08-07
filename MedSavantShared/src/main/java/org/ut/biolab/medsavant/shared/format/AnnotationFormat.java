/**
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.shared.format;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.medsavant.api.annotation.TabixAnnotation;
import org.medsavant.api.filestorage.MedSavantFile;
import org.medsavant.api.variantstorage.MedSavantField;
import org.medsavant.api.common.MedSavantSecurityException;
import org.medsavant.api.common.MedSavantServerContext;
import org.medsavant.api.filestorage.MedSavantFileDirectoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Andrew
 */
public class AnnotationFormat implements Serializable, TabixAnnotation {

    public static final String ANNOTATION_FORMAT_DEFAULT = "VCF";
    public static final String ANNOTATION_FORMAT_CUSTOM_VCF = "VCF Info";
    private static final String FIELD_DELIMITER = "\t";

    private final String program;
    private final String version;
    private final String referenceName;
    //private final String path;
    private final boolean hasRef;
    private final boolean hasAlt;
    private final MedSavantField[] fields;
    private final CustomField[] customFields; //for legacy compatibility.  A copy of the above 'fields' variable.
    private final AnnotationType type;
    private final boolean isEndInclusive;

    private final MedSavantFile tabixFile;
    private final MedSavantFile tabixIndex;

    /**
     * This constructor is deprecated, but is included for compatibility with
     * MedSavant legacy.
     *
     * @param program
     * @param version
     * @param referenceName
     * @param path
     * @param hasRef
     * @param hasAlt
     * @param type
     * @param isEndInclusive
     * @param fields
     * @param tabixFile
     * @param tabixIndex
     * @deprecated
     */
    @Deprecated
    public AnnotationFormat(
            String program,
            String version,
            String referenceName,
            boolean hasRef,
            boolean hasAlt,
            AnnotationType type,
            boolean isEndInclusive,
            CustomField[] fields,
            MedSavantFile tabixFile,
            MedSavantFile tabixIndex) {

        this.fields = fields;
        /*
        if (fields != null) {
            this.fields = new MedSavantField[fields.length];
            for (int i = 0; i < fields.length; ++i) {
                this.fields[i] = fields[i];
            }
        } else {
            this.fields = null;
        }*/
        this.program = program;
        this.version = version;
        this.referenceName = referenceName;
        //this.path = path;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.customFields = fields;
        this.type = type;
        this.isEndInclusive = isEndInclusive;
        this.tabixFile = null;
        this.tabixIndex = null;
    }

    /**
     *
     * @param program
     * @param version
     * @param referenceName
     * @param path
     * @param hasRef
     * @param hasAlt
     * @param type
     * @param isEndInclusive
     * @param fields
     * @param tabixFile
     * @param tabixIndex
     */
    public AnnotationFormat(
            String program,
            String version,
            String referenceName,
            boolean hasRef,
            boolean hasAlt,
            AnnotationType type,
            boolean isEndInclusive,
            MedSavantField[] fields,
            MedSavantFile tabixFile,
            MedSavantFile tabixIndex) {
        this.fields = fields;
        this.program = program;
        this.version = version;
        this.referenceName = referenceName;
        //this.path = null;
        this.hasRef = hasRef;
        this.hasAlt = hasAlt;
        this.customFields = null;
        this.type = type;
        this.isEndInclusive = isEndInclusive;
        this.tabixFile = null;
        this.tabixIndex = null;
    }

    public AnnotationFormat(MedSavantServerContext context, MedSavantFile tabixFile, MedSavantFile tabixIndex, MedSavantFile xmlFile) throws IOException, MedSavantSecurityException {
        this.tabixFile = tabixFile;
        this.tabixIndex = tabixIndex;
        try {
            //BufferedReader in;            
            InputStream is = context.getMedSavantFileDirectory().getInputStream(null, xmlFile);           
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);

            doc.getDocumentElement().normalize();

            this.hasRef = doc.getDocumentElement().getAttribute("hasref").equals("true");
            this.hasAlt = doc.getDocumentElement().getAttribute("hasalt").equals("true");
            this.isEndInclusive = doc.getDocumentElement().getAttribute("isEndInclusive").isEmpty() ? false : true;
            this.version = doc.getDocumentElement().getAttribute("version");
            this.program = doc.getDocumentElement().getAttribute("program");
            this.referenceName = doc.getDocumentElement().getAttribute("reference");
            this.type = AnnotationFormat.AnnotationType.fromString(doc.getDocumentElement().getAttribute("type"));

        //String prefix = program + "_" + version.replaceAll("\\.", "_") + "_";
            //get custom columns
            NodeList fields = doc.getElementsByTagName("field");
            CustomField[] annotationFields = new CustomField[fields.getLength()];

            for (int i = 0; i < fields.getLength(); i++) {
                Element field = (Element) (fields.item(i));
                annotationFields[i] = new CustomField(
                        field.getAttribute("name"),
                        field.getAttribute("type"),
                        field.getAttribute("filterable").equals("true"),
                        field.getAttribute("alias"),
                        field.getAttribute("description"),
                        field.getAttribute("tags"),
                        false);
            }
            this.customFields = annotationFields;
            this.fields = annotationFields;
        } catch (SAXException se) {
            throw new IOException("Error reading xmlFile "+xmlFile.getName(), se);
        } catch (ParserConfigurationException pce) {
            throw new IOException("Error reading xmlFile "+xmlFile.getName(), pce);
        } catch (MedSavantFileDirectoryException mfde){
            throw new IOException("Error reading xmlFile "+xmlFile.getName(), mfde);
        }
    }

    @Override
    public int getNumNonDefaultFields() {
        return fields.length;
    }

    @Override
    public boolean hasRef() {
        return hasRef;
    }

    @Override
    public boolean hasAlt() {
        return hasAlt;
    }

    @Override
    public boolean isPositionAnnotation() {
        return (this.getType() == AnnotationType.POSITION);
    }

    @Override
    public boolean isIntervalAnnotation() {
        return (this.getType() == AnnotationType.INTERVAL);
    }

    @Override
    public String getFieldDelimiter() {
        return FIELD_DELIMITER;
    }

    @Override
    public String getProgram() {
        return program;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getReferenceName() {
        return referenceName;
    }

    @Deprecated
    public AnnotationType getType() {
        return type;
    }

    @Override
    public boolean isEndInclusive() {
        return isEndInclusive;
    }

    @Override
    public String toString() {
        return "AnnotationFormat{" + "program=" + program + ", version=" + version + ", referenceName=" + referenceName  + ", hasRef=" + hasRef + ", hasAlt=" + hasAlt + ", fields=" + fields + ", type=" + type + ", isEndInclusive=" + isEndInclusive + '}';
    }

    @Override
    public MedSavantFile getTabixFile() {
        return this.tabixFile;
    }

    @Override
    public MedSavantFile getTabixFileIndex() {
        return this.tabixIndex;
    }

    @Override
    public int getNumDefaultFields(boolean hasEnd) {
        int numDefaultFields = 2; //chromosome and position are ALWAYS present.

        if (hasRef()) {
            numDefaultFields++;
        }

        if (hasAlt()) {
            numDefaultFields++;
        }

        return hasEnd ? (numDefaultFields + 1) : numDefaultFields;
    }

    @Override
    public MedSavantField[] getFields() {
        return fields;
    }

    public static enum AnnotationType {

        POSITION,
        INTERVAL;

        public static AnnotationType fromInt(int type) {
            switch (type) {
                case 0:
                    return AnnotationType.POSITION;
                case 1:
                    return AnnotationType.INTERVAL;
                default:
                    return null;
            }
        }

        public static int toInt(AnnotationType type) {
            switch (type) {
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

    @Deprecated
    public CustomField[] getCustomFields() {
        return customFields;
    }

    @Deprecated
    public static AnnotationFormat getDefaultAnnotationFormat() {
        return new AnnotationFormat(ANNOTATION_FORMAT_DEFAULT, ANNOTATION_FORMAT_DEFAULT, "0", true, true, AnnotationType.POSITION, false, BasicVariantColumns.REQUIRED_VARIANT_FIELDS, null, null);
    }

    @Deprecated
    public static AnnotationFormat getCustomFieldAnnotationFormat(CustomField[] customFields) {
        return new AnnotationFormat(ANNOTATION_FORMAT_CUSTOM_VCF, ANNOTATION_FORMAT_CUSTOM_VCF, "0", true, true, AnnotationType.POSITION, false, customFields, null, null);
    }
}
