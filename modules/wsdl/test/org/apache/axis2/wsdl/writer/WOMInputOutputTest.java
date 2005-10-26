package org.apache.axis2.wsdl.writer;

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.custommonkey.xmlunit.XMLTestCase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

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

public class WOMInputOutputTest extends XMLTestCase {
    private WSDLVersionWrapper wsdlVersionWrapper;
    private String testWSDL = "test-resources/interoptestdoclitparameters.wsdl";

    Log logger = LogFactory.getLog(getClass());

    protected void setUp() throws Exception {

    }

    public void testInputOutput() throws XMLStreamException {
        try {
            // create the WOM from the given WSDL.
            WOMBuilder builder = WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1);
            wsdlVersionWrapper = builder.build(testWSDL);

            // serialize wom and get the xml.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            WOMWriter womWriter = WOMWriterFactory.createWriter(WSDLConstants.WSDL_1_1);
            womWriter.writeWOM(wsdlVersionWrapper, byteArrayOutputStream);
            String actualWSDL = new String(byteArrayOutputStream.toByteArray());

            // compare that with the original xml file.
            Document actualWSDLDocument = newDocument(actualWSDL);

            // feeding file directly in to DOM gives me an error. Lets try to read it first to
            // a String and feed

            Document expectedWSDLDocument = newDocument(getFileContentsAsString(testWSDL));

            System.out.println(getChildrenCount(actualWSDL));
//            System.out.println(getChildrenCount(testWSDL));

//            assertXMLEqual(actualWSDLDocument, expectedWSDLDocument);

        } catch (WSDLException e) {
            logger.error("Error occured in WOMInputOutputTest.testInputOutput() ", e);
            fail();
        } catch (WriterException e) {
            logger.error("Error occured in WOMInputOutputTest.testInputOutput() ", e);
            fail();
        } catch (ParserConfigurationException e) {
            logger.error("Error occured in WOMInputOutputTest.testInputOutput() ", e);
            fail();
        } catch (SAXException e) {
            logger.error("Error occured in WOMInputOutputTest.testInputOutput() ", e);
            fail();
        } catch (IOException e) {
            logger.error("Error occured in WOMInputOutputTest.testInputOutput() ", e);
            fail();
        }
    }

    public Document newDocument(String xml)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(new ByteArrayInputStream(xml.getBytes()));
    }

    public Document newDocument(File wsdlFile)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(wsdlFile);
    }

    public String getFileContentsAsString(String filePath) throws IOException {
        String returnString = "";

        BufferedReader in = new BufferedReader(new FileReader(filePath));
        String str;
        while ((str = in.readLine()) != null) {
            returnString += str;
        }
        in.close();
        return returnString;
    }

    public int getChildrenCount(String xml) throws XMLStreamException {
        ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());
        OMElement documentElement = new StAXOMBuilder(bais).getDocumentElement();
        int omElementCount = 0;
        int omTextCount = 0;
        Iterator childrenIter = documentElement.getChildren();
        while (childrenIter.hasNext()) {
            Object o = childrenIter.next();
            if (o instanceof OMElement) {
                omElementCount++;
            } else if (o instanceof OMText) {
                omTextCount++;
            }
        }

        System.out.println("omElementCount = " + omElementCount);
        System.out.println("omTextCount = " + omTextCount);

        return omElementCount + omTextCount;
    }

}
