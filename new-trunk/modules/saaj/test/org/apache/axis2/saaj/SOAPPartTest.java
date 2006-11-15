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
package org.apache.axis2.saaj;

import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.activation.DataHandler;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 
 */
public class SOAPPartTest extends TestCase {
    public void testGetContents() {
        try {
            ByteArrayInputStream ins = new ByteArrayInputStream(new byte[5]);
            DataHandler dh = new DataHandler(new AttachmentTest("t").new Src(ins, "text/plain"));
            InputStream in = dh.getInputStream();
            StreamSource ssrc = new StreamSource(in);

            SOAPPart sp = MessageFactory.newInstance().createMessage().getSOAPPart();
            sp.setContent(ssrc);

            Source ssrc2 = sp.getContent();
            if (ssrc2 == null) {
                fail("Contents were null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception " + e);
        }
    }

    public void testAddSource() {
        DOMSource domSource;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(new File("test-resources" + File.separator + "soap-part.xml"));
            domSource = new DOMSource(document);

            SOAPMessage message = MessageFactory.newInstance().createMessage();

            // Get the SOAP part and set its content to domSource
            SOAPPart soapPart = message.getSOAPPart();
            soapPart.setContent(domSource);
            message.saveChanges();

            SOAPHeader header = message.getSOAPHeader();
            if (header != null) {
                Iterator iter1 = header.getChildElements();
                System.out.println("Header contents:");
                getContents(iter1, "");
            }

            SOAPBody body = message.getSOAPBody();
            Iterator iter2 = body.getChildElements();
            System.out.println("Body contents:");
            getContents(iter2, "");

            /* SOAPEnvelope envelope = soapPart.getEnvelope();
            SOAPHeader header = envelope.getHeader();
            assertEquals("Header", header.getLocalName());
            assertEquals("soapenv", header.getPrefix());

            Node firstChild = header.getFirstChild();
//            assertEquals("Hello", firstChild.getLocalName());
//            assertEquals("shw", firstChild.getPrefix());

            SOAPBody body = envelope.getBody();
            assertEquals("Body", body.getLocalName());
            assertEquals("soapenv", body.getPrefix());

            for(Iterator iter=body.getChildElements(); iter.hasNext();){
                System.err.println("$$$ " + iter.next());
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected Exception : " + e);
        }
    }

    public void getContents(Iterator iterator, String indent) {
        System.err.println(iterator.hasNext());
        while (iterator.hasNext()) {
            Node node = (Node) iterator.next();
            SOAPElement element = null;
            Text text = null;

            if (node instanceof SOAPElement) {
                element = (SOAPElement) node;

                Name name = element.getElementName();
                System.out.println(indent + "Name is " + name.getQualifiedName());

                Iterator attrs = element.getAllAttributes();

                while (attrs.hasNext()) {
                    Name attrName = (Name) attrs.next();
                    System.out.println(indent + " Attribute name is " +
                                       attrName.getQualifiedName());
                    System.out.println(indent + " Attribute value is " +
                                       element.getAttributeValue(attrName));
                }

                Iterator iter2 = element.getChildElements();
                getContents(iter2, indent + " ");
            } else {
                text = (Text) node;

                String content = text.getValue();
                System.out.println(indent + "Content is: " + content);
            }
        }
    }
}
