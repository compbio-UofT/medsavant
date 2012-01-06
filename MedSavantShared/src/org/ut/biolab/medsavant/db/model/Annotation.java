package org.ut.biolab.medsavant.db.model;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import org.broad.tabix.TabixReader;
import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import org.ut.biolab.medsavant.db.format.AnnotationFormat.AnnotationType;

/**
 * @author mfiume
 */
public class Annotation implements Serializable {

    final private int id;
    final private String program;
    final private String version;
    final private int referenceId;
    final private String referenceName;
    final private String dataPath;
    final private AnnotationType type;
    private TabixReader reader;

    public Annotation(int id, String program, String version, int referenceId, String referenceName, String dataPath, AnnotationType type) {
        this.id = id;
        this.program = program;
        this.version = version;
        this.referenceId = referenceId;
        this.referenceName = referenceName;
        this.dataPath = dataPath;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getProgram() {
        return program;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public String getVersion() {
        return version;
    }

    public boolean isInterval() {
        return type == AnnotationType.INTERVAL;
    }

    public TabixReader getReader() throws IOException {
        if (reader == null) {
            reader = new TabixReader(this.getDataPath());
        }
        return reader;
    }

    @Override
    public String toString() {
        return "Annotation{" + "version=" + version + ", reference=" + referenceName + ", dataPath=" + dataPath + ", type=" + type + '}';
    }

}
