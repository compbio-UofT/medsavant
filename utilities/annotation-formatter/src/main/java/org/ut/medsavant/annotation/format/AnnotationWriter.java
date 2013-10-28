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
package org.ut.medsavant.annotation.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.broad.tabix.TabixWriter.Conf;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author mfiume
 */
public class AnnotationWriter
{
    private File dataOutFile;

    private boolean isInterval;

    private String[] fieldNames;

    private int numFields;

    private BufferedWriter writer;

    private ArrayList<FieldTypeHypothesis> hypotheses;

    private String name;

    private String reference;

    private String version;

    private final File xmlOutFile;

    public static final Conf TSV_CONF = new Conf(0, 1, 2, 3, '#', 0);

    public AnnotationWriter(File dataOutFile, File xmlOutFile)
    {
        this.dataOutFile = dataOutFile;
        this.xmlOutFile = xmlOutFile;
    }

    public void setIsInterval(boolean isInterval)
    {
        this.isInterval = isInterval;
    }

    public void setFieldNames(String[] fieldNames) throws Exception
    {
        this.fieldNames = fieldNames;
        this.numFields = fieldNames.length;
        checkFieldLength(fieldNames.length);

        this.hypotheses = new ArrayList<FieldTypeHypothesis>();
        for (String fieldName : fieldNames) {
            this.hypotheses.add(new FieldTypeHypothesis(fieldName));
        }
    }

    private void checkFieldLength(int num) throws Exception
    {
        if (this.numFields != num) {
            throw new Exception("Field length mismatch. Line " + this.lineNumber + " is " + num + " != "
                + this.numFields);
        }
    }

    int lineNumber;

    public void openWriter() throws IOException
    {
        this.writer = new BufferedWriter(new FileWriter(this.dataOutFile));
        this.lineNumber = 0;
    }

    public void closeWriter() throws IOException, ParserConfigurationException, TransformerConfigurationException,
        TransformerConfigurationException, TransformerException
    {
        if (this.writer != null) {
            this.writer.close();
        }
    }

    public void addLine(String[] line) throws IOException, Exception
    {
        this.lineNumber++;
        try {
            checkFieldLength(line.length);
        } catch (Exception e) {
            System.err.print("Error processing line ");
            MedSavantAnnotationFormatter.output(line, System.err);
            throw e;
        }

        for (int i = 0; i < this.numFields; i++) {
            this.hypotheses.get(i).addEvidence(line[i]);
            if (this.writer != null) {
                this.writer.write(line[i]);
                this.writer.write(i == (this.numFields - 1) ? "\n" : "\t");
            }
        }
    }

    public void writeXMLSummary() throws ParserConfigurationException, TransformerConfigurationException,
        TransformerException
    {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root elements
        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("annotation");
        doc.appendChild(rootElement);

        addAttributeToElement(doc, rootElement, "program", this.name);
        addAttributeToElement(doc, rootElement, "version", this.version);
        addAttributeToElement(doc, rootElement, "reference", translateReference(this.reference));
        addAttributeToElement(doc, rootElement, "type", "interval");
        addAttributeToElement(doc, rootElement, "hasref", "false");
        addAttributeToElement(doc, rootElement, "hasalt", "false");

        for (FieldTypeHypothesis h : this.hypotheses) {

            if (h.fieldName.equals("Chromosome") || h.fieldName.equals("Start") || h.fieldName.equals("Stop")) {
                continue;
            }

            Element fieldElement = doc.createElement("field");
            rootElement.appendChild(fieldElement);

            String type = h.hypothesizeType();
            String filterable = "true";
            if (type.startsWith("VARCHAR") && h.maxLength > 15) {
                filterable = "false";
            }

            addAttributeToElement(doc, fieldElement, "name", h.fieldName.replace(" ", "_"));
            addAttributeToElement(doc, fieldElement, "type", type);
            addAttributeToElement(doc, fieldElement, "filterable", filterable);
            addAttributeToElement(doc, fieldElement, "alias", h.fieldName);
            addAttributeToElement(doc, fieldElement, "description", "");

        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(this.xmlOutFile);

        // Output to console for testing
        // StreamResult result = new StreamResult(System.out);

        transformer.transform(source, result);
    }

    private void addAttributeToElement(Document d, Element e, String att, String val)
    {
        Attr a = d.createAttribute(att);
        a.setValue(val);
        e.setAttributeNode(a);
    }

    void setName(String name)
    {
        name = name.replaceAll(" ", "_");
        if (name.contains(",")) {
            this.name = name.substring(0, name.indexOf(","));
        } else {
            this.name = name;
        }
    }

    void setReference(String r)
    {
        this.reference = r;
    }

    private String translateReference(String reference)
    {
        if (reference.equals("GRCh_37,Chromosome,Homo sapiens")) {
            return "hg19";
        }

        return reference;
    }

    void setVersion(String v)
    {
        this.version = v;
    }

    private class FieldTypeHypothesis
    {

        private boolean canFloat = false;

        private boolean canInt = true;

        private int maxLength;

        private final String fieldName;

        private FieldTypeHypothesis(String fieldName)
        {
            this.fieldName = fieldName;
        }

        private void addEvidence(String s)
        {
            if (s.isEmpty()) {
                return;
            }

            if (this.canInt) {
                try {
                    Integer.parseInt(s);
                    if (s.contains(".")) {
                        this.canFloat = true;
                    }
                } catch (Exception e) {
                    this.canInt = false;
                }
            }

            this.maxLength = this.maxLength < s.length() ? s.length() : this.maxLength;
        }

        public String hypothesizeType()
        {
            if (this.canFloat) {
                return typeLengthString("FLOAT");
            } else if (this.canInt) {
                return typeLengthString("INTEGER", this.maxLength);
            } else {
                return typeLengthString("VARCHAR", this.maxLength);
            }
        }

        private String typeLengthString(String type, int length)
        {
            return type + "(" + length + ")";
        }

        private String typeLengthString(String type)
        {
            return type;
        }
    }
}
