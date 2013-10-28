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
package org.ut.biolab.medsavant.server.ontology;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ut.biolab.medsavant.shared.model.OntologyTerm;
import org.ut.biolab.medsavant.shared.model.OntologyType;
import org.ut.biolab.medsavant.shared.util.RemoteFileCache;


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
