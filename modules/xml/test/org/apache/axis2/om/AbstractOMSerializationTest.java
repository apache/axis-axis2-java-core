/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.om;

import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AbstractOMSerializationTest extends XMLTestCase {

    protected boolean ignoreXMLDeclaration = true;
    protected boolean ignoreDocument = false;
    protected Log log = LogFactory.getLog(getClass());


    /**
     * @param xmlString - remember this is not the file path. this is the xml string
     */
    public Diff getDiffForComparison(String xmlString) throws Exception {
        return getDiffForComparison(new ByteArrayInputStream(xmlString.getBytes()));
    }

    public Diff getDiffForComparison(File xmlFile) throws Exception {
        return getDiffForComparison(new FileInputStream(xmlFile));
    }

    public String getSerializedOM(String xmlString) throws Exception {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xmlString.getBytes());
            StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                    createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                            factory.createXMLStreamReader(byteArrayInputStream));
            staxOMBuilder.setDoDebug(true);
            OMElement rootElement = staxOMBuilder.getDocumentElement();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            OMOutputFormat format = new OMOutputFormat();
            format.setIgnoreXMLDeclaration(ignoreXMLDeclaration);

            ((OMDocument) rootElement.getParent()).serialize(baos, format);

            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw e;
        }
    }

    public Diff getDiffForComparison(InputStream inStream) throws Exception {

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();

            StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                    createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                            factory.createXMLStreamReader(inStream));
            staxOMBuilder.setDoDebug(true);
            OMElement rootElement = staxOMBuilder.getDocumentElement();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            if (ignoreDocument) {
                rootElement.serialize(baos);
            } else {
                ((OMDocument) rootElement.getParent()).serialize(baos);
            }

            InputSource resultXML = new InputSource(new InputStreamReader(
                    new ByteArrayInputStream(baos.toByteArray())));

            Document dom2 = newDocument(resultXML);
            Document dom1 = newDocument(inStream);

            return compareXML(dom1, dom2);
        } catch (XMLStreamException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (ParserConfigurationException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (SAXException e) {
            fail(e.getMessage());
            throw new Exception(e);
        } catch (IOException e) {
            fail(e.getMessage());
            throw new Exception(e);
        }
    }

    public Document newDocument(InputSource in)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    public Document newDocument(InputStream in)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(in);
    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public String writeXmlFile(Document doc) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result result = new StreamResult(baos);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
            return new String(baos.toByteArray());
        } catch (TransformerConfigurationException e) {
            log.error(e.getMessage(), e);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
        }
        return null;

    }

}
