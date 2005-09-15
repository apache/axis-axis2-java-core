package org.apache.axis2.om;

import junit.framework.TestCase;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
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

public class OMDocumentTest extends TestCase {
    private String sampleXML = "<?xml version='1.0' encoding='utf-8'?>" +
            "<!--This is some comments at the start of the document-->" +
            "<?PITarget PIData?>" +
            "<Axis2>" +
            "    <ProjectName>The Apache Web Sevices Project</ProjectName>" +
            "</Axis2>";

    public void testOMDocument() throws XMLStreamException {
        // read the string in to the builder
        OMDocument omDocument = getSampleOMDocument(sampleXML);

        // serialise it to a string
        String outXML = "";
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        OMOutputImpl output = new OMOutputImpl(outStream, false);
        omDocument.serialize(output);
        output.flush();
        outXML = new String(outStream.toByteArray());
        System.out.println("outXML = " + outXML);

        // again load that to another builder
        OMDocument secondDocument = getSampleOMDocument(outXML);

        // check for the comment and the PI
        boolean commentFound = false;
        boolean piFound = false;
        Iterator children = secondDocument.getChildren();
        while (children.hasNext()) {
            OMNode omNode = (OMNode) children.next();
            if (omNode instanceof OMComment) {
                commentFound = true;
            } else if (omNode instanceof OMProcessingInstruction) {
                piFound = true;
            } else if (omNode instanceof OMElement && !commentFound && !piFound) {
               fail("OMElement should come after Comment and PI");

            }
        }
        assertTrue(commentFound && piFound);


    }

    private OMDocument getSampleOMDocument(String xml) {
        try {
            XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
            StAXOMBuilder builder = new StAXOMBuilder(xmlStreamReader);
            return builder.getDocument();
        } catch (XMLStreamException e) {
            throw new UnsupportedOperationException();
        }
    }

//    private OMDocument getSampleOMDocument() {
//        OMFactory omFactory = OMAbstractFactory.getOMFactory();
//        OMDocument omDocument = omFactory.createOMDocument();
//        omFactory.createOMComment(omDocument, "This is some comments at the start of the document");
//        omDocument.setCharsetEncoding("utf-8");
//        omFactory.createOMProcessingInstruction(omDocument, "PITarget", "PIData");
//
//        OMElement documentElement = omFactory.createOMElement("Axis2", null, omDocument);
//        omDocument.setDocumentElement(documentElement);
//        omFactory.createOMElement("ProjectName", null, documentElement);
//        documentElement.getFirstElement().setText("The Apache Web Sevices Project");
//
//        return omDocument;
//    }

}
