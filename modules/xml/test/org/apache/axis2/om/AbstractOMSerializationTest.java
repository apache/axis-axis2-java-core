package org.apache.axis2.om;

import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
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
import java.io.*;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 *
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class AbstractOMSerializationTest extends XMLTestCase {

    protected boolean ignoreXMLDeclaration = true;
    protected boolean ignoreDocument = false;


    /**
     * @param xmlString - remember this is not the file path. this is the xml string
     */
    public Diff getDiffForComparison(String xmlString) throws Exception {
        return getDiff(new ByteArrayInputStream(xmlString.getBytes()));
    }

    public Diff getDiffForComparison(File xmlFile) throws Exception {
        return getDiff(new FileInputStream(xmlFile));
    }

    private Diff getDiff(InputStream inStream) throws Exception {

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
//            factory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);

            StAXOMBuilder staxOMBuilder = OMXMLBuilderFactory.
                    createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                            factory.createXMLStreamReader(inStream));
            staxOMBuilder.setDoDebug(true);
            OMElement rootElement = staxOMBuilder.getDocumentElement();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            OMOutputImpl omOutput = new OMOutputImpl(baos, false);
            omOutput.ignoreXMLDeclaration(ignoreXMLDeclaration);

            if (ignoreDocument) {
                rootElement.serializeWithCache(omOutput);
            } else {
                ((OMDocument) rootElement.getParent()).serializeWithCache(omOutput);
            }
            omOutput.flush();

            System.out.println("XML = " + new String(baos.toByteArray()));

            InputSource resultXML = new InputSource(new InputStreamReader(
                    new ByteArrayInputStream(baos.toByteArray())));

            Document dom1 = newDocument(inStream);
            Document dom2 = newDocument(resultXML);

            return compareXML(dom1, dom2);
//            assertXMLEqual(diff, true);
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


}
