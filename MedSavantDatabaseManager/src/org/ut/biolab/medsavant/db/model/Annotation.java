package org.ut.biolab.medsavant.db.model;

import org.ut.biolab.medsavant.db.format.AnnotationFormat;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import org.broad.tabix.TabixReader;
import org.ut.biolab.medsavant.db.format.AnnotationFormat.AnnotationType;
import org.ut.biolab.medsavant.db.util.query.AnnotationQueryUtil;
import org.xml.sax.SAXException;

/**
 *
 * @author mfiume
 */
public class Annotation {
        
    final private int id;
    final private String program;
    final private String version;
    final private String reference;
    final private String dataPath;
    final private AnnotationType type;
    private TabixReader reader;

    public Annotation(int id, String program, String version, String reference, String dataPath, AnnotationType type) {
        this.id = id;
        this.program = program;
        this.version = version;
        this.reference = reference;
        this.dataPath = dataPath;
        this.type = type;
    }
    
    public AnnotationFormat getAnnotationFormat() throws SQLException, IOException, ParserConfigurationException, SAXException {
        return AnnotationQueryUtil.getAnnotationFormat(id); 
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

    public String getReference() {
        return reference;
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
        return "Annotation{" + "version=" + version + ", reference=" + reference + ", dataPath=" + dataPath + ", type=" + type + '}';
    }
    
}
