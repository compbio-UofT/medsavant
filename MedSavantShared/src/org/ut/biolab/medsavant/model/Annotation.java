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

package org.ut.biolab.medsavant.model;

import java.io.Serializable;
import java.net.URL;

import org.ut.biolab.medsavant.format.AnnotationFormat.AnnotationType;


/**
 * @author mfiume
 */
public class Annotation implements Serializable {

    private final int id;
    private final String program;
    private final String version;
    private final int referenceID;
    private final String referenceName;
    private final String dataPath;
    private final AnnotationType type;

    public Annotation(int id, String program, String version, int refID, String refName, String dataPath, AnnotationType type) {
        this.id = id;
        this.program = program;
        this.version = version;
        this.referenceID = refID;
        this.referenceName = refName;
        this.dataPath = dataPath;
        this.type = type;
    }

    public int getID() {
        return id;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getProgram() {
        return program;
    }

    public int getReferenceID() {
        return referenceID;
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

    public AnnotationType getAnnotationType(){
        return type;
    }

    @Override
    public String toString() {
        return getProgram() + " (v" + getVersion() + ", " + getReferenceName() + ")";
    }

}
