/*
 *    Copyright 2012 University of Toronto
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

package org.ut.biolab.medsavant.ontology;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.model.OntologyTerm;
import org.ut.biolab.medsavant.model.OntologyType;
import org.ut.biolab.medsavant.util.RemoteFileCache;


/**
 * Parses an OBO (Open Biomedical Ontology) file and loads it into a database table.
 *
 * @author tarkvara
 */
public class OBOParser {
    
    private static final Log LOG = LogFactory.getLog(OBOParser.class);

    private final OntologyType ontology;
    private int lineNum;


    public OBOParser(OntologyType ont) {
        this.ontology = ont;
    }

    Map<String, OntologyTerm> load(URL source) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(RemoteFileCache.getCacheFile(source)));

        Map<String, OntologyTerm> terms = new HashMap<String, OntologyTerm>();
        String line;
        while ((line = reader.readLine()) != null) {
            lineNum++;
            if (line.equals("[Term]")) {
                OntologyTerm t = parseNextTerm(reader);
                if (t != null) {
                    if (t.getID() == null || t.getName() == null) {
                        LOG.info(String.format("Defective ontology term at line %d: %s/%s", lineNum, t.getID(), t.getName()));
                        break;
                    } else {
                        if (t.getID().startsWith("results")) {
                            LOG.info(String.format("Loading \"%s\" at line %d.", line, lineNum));
                        }
                        terms.put(t.getID(), t);
                    }
                }
            }
        }

        return terms;
    }

    private OntologyTerm parseNextTerm(BufferedReader reader) throws IOException {

        String line;
        String id = null, name = null, description = null;
        List<String> altIDs = new ArrayList<String>();
        List<String> parentIDs = new ArrayList<String>();
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("[")) {
                // We've hit the next term (or typedef).  Rewind to the beginning of the line.
                reader.reset();
                break;
            } else {
                lineNum++;
                if (line.length() > 0 && line.charAt(0) != '!') {
                    int colonPos = line.indexOf(":");
                    if (colonPos > 0) {
                        String key = line.substring(0, colonPos);
                        String value = line.substring(colonPos + 1);
                        int commentPos = indexOf(value, '!');
                        if (commentPos > 0) {
                            value = value.substring(0, commentPos);
                        }
                        value = value.trim();
                        if (key.equals("id")) {
                            id = value;
                        } else if (key.equals("alt_id")) {
                            altIDs.add(value);
                        } else if (key.equals("name")) {
                            name = value;
                        } else if (key.equals("def")) {
                            // Def consists of a quote-delimited string followed by some (ignored) reference material.
                            int quotePos = indexOf(value, '\"');
                            if (quotePos >= 0) {
                                value = value.substring(quotePos + 1);
                                quotePos = indexOf(value, '\"');
                                if (quotePos >= 0) {
                                    value = value.substring(0, quotePos);
                                    description = value.replace("\\", "");
                                }
                            }
                        } else if (key.equals("is_a")) {
                            parentIDs.add(value);
                        } else if (key.equals("is_obsolete") && value.equals("true")) {
                            return null;
                        }
                    }
                }
                reader.mark(10);    // So we can rewind if the line is the next [Term].
            }
        }
        return new OntologyTerm(ontology, id, name, description, altIDs.toArray(new String[0]), parentIDs.toArray(new String[0]));
    }
    
    /**
     * Like the normal String.indexOf method, but pays attention to escape characters.
     * @param s the string to be searched
     * @param c the character we're looking for
     */
    private static int indexOf(String s, char c) {
        int pos = s.indexOf(c);
        while (pos > 0 && s.charAt(pos - 1) == '\\') {
            pos = s.indexOf(c, pos + 1);
        }
        return pos;
    }
}
