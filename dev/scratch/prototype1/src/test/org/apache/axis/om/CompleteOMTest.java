/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.om;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.om.impl.OMAttributeImpl;
import org.apache.axis.om.impl.OMElementImpl;
import org.apache.axis.om.impl.OMTextImpl;
import org.apache.axis.om.impl.streamwrapper.OMXPPWrapper;
import org.apache.axis.om.soap.SOAPMessage;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * This test case tests the basic expectations of the engine from the OM.
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class CompleteOMTest extends AbstractTestCase{

    SOAPMessage message;

    public CompleteOMTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        File file = getTestResourceFile("soap/sample1.xml");
        FileInputStream in = new FileInputStream(file);
        
        XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
        pf.setNamespaceAware(true);
        XmlPullParser  parser = pf.newPullParser();
        parser.setInput(new InputStreamReader(in));
        
        OMXMLParserWrapper parserWrapper = new OMXPPWrapper(parser);
        message = parserWrapper.getSOAPMessage();
    }

    
    /**
     * Sometime the hasNext() in the childeren iterator is true yet the next() is null
     */

    public void testNullInChilderen(){
        isNullChildrenThere(message.getEnvelope());
    }
    
    /**
     * the message is completly namesapce qulified so all the OMElements got to have namespace values not null
     *
     */
    public void test4MissingNamespaces(){
        isNameSpacesMissing(message.getEnvelope());
    }
    
    public void isNullChildrenThere(OMElement omeleent){
        Iterator it = omeleent.getChildren();
        while(it.hasNext()){
            OMNode node = (OMNode)it.next();
            assertNotNull(node);
            if(node.getType() == OMNode.ELEMENT_NODE){
                isNullChildrenThere((OMElement)node);
            }
        }
    }

    public void isNameSpacesMissing(OMElement omeleent){
        OMNamespace omns = omeleent.getNamespace();
        assertNotNull(omns);
        assertNotNull(omns.getValue());
        Iterator it = omeleent.getChildren();
        while(it.hasNext()){
            OMNode node = (OMNode)it.next();
            
            if(node != null && node.getType() == OMNode.ELEMENT_NODE){
                isNameSpacesMissing((OMElement)node);
            }
        }
    }

    public void testRootNotCompleteInPartialBuild() throws Exception {
        OMElement root= message.getEnvelope();
        assertFalse("Root should not be complete",root.isComplete());
    }

    /**
     * Assumption - The fed XML has at least two children under the root element
     * @throws Exception
     */
    public void testFirstChildDetach() throws Exception {
        OMElement root= message.getEnvelope();
        assertFalse("Root should not be complete",root.isComplete());
        OMNode oldFirstChild = root.getFirstChild();
        assertNotNull(oldFirstChild);
        oldFirstChild.detach();

        OMNode newFirstChild = root.getFirstChild();
        assertNotNull(newFirstChild);

        assertNotSame(oldFirstChild,newFirstChild);
    }

    public void testAdditionOfaCompletelyNewElement() throws Exception {

        OMElement root= message.getEnvelope();

        OMNamespace soapenv= root.resolveNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        OMNamespace wsa= root.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");
        if (wsa==null)
            wsa= root.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");

        //Assumption - A RelatesTo Element does not exist in the input document
        OMElement relatesTo= new OMElementImpl("RelatesTo", wsa);  //todo isn't this wrong? this should come from the factory
        relatesTo.insertAttribute(new OMAttributeImpl("RelationshipType", null, "wsa:Reply", relatesTo));
        relatesTo.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0", relatesTo));
        relatesTo.addChild(new OMTextImpl(relatesTo, "uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));
        relatesTo.setComplete(true);

        root.addChild(relatesTo);

        QName name = new QName(wsa.getValue(),"RelatesTo",wsa.getPrefix());

        Iterator children = root.getChildrenWithName(name);
        //this should contain only one child!
        if (children.hasNext()){
            OMElement newlyAddedElement = (OMElement)children.next();

            assertNotNull(newlyAddedElement);

            assertEquals(newlyAddedElement.getLocalName(),"RelatesTo");
            //todo put the assert statements here
        }else{
            assertFalse("New child not added",true);
        }

    }

    //Todo need to fix this
    public void test4() throws Exception {

//        OMElementImpl root= new OMElementImpl("Envelope", null);
//        OMNamespace soapenv= root.createNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
//        root.setNamespace(soapenv);
//        OMElement header= new OMElementImpl("Header", soapenv);
//        header.setComplete(true);
//        root.addChild(header);
//        OMNamespace xsd= root.createNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
//        OMNamespace xsi= root.createNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
//        OMNamespace wsa= root.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");
//
//        OMElement messageID= new OMElementImpl("MessageID", wsa);
//        messageID.addChild(new OMTextImpl("uuid:920C5190-0B8F-11D9-8CED-F22EDEEBF7E5"));
//        messageID.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));
//
//        OMElement from= new OMElementImpl("From", wsa);
//        OMElement address= new OMElementImpl("Address", null);
//        address.createNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null);
//        address.setNamespace(
//                address.resolveNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", null));
//        address.addChild(new OMTextImpl("http://schemas.xmlsoap.org/ws/2004/03/addressing/role/anonymous"));
//        from.addChild(address);
//        //	FIXME:	cannot reuse the attribute object created earlier. linked list--
//        from.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));
//
//        OMElement to= new OMElementImpl("To", wsa);
//        to.addChild(new OMTextImpl("http://localhost:8081/axis/services/BankPort"));
//        to.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));
//
//        OMElement relatesTo= new OMElementImpl("RelatesTo", wsa);
//        relatesTo.insertAttribute(new OMAttributeImpl("RelationshipType", null, "wsa:Reply"));
//        relatesTo.insertAttribute(new OMAttributeImpl("mustUnderstand", soapenv, "0"));
//        relatesTo.addChild(new OMTextImpl("uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));
//
//        header.addChild(from);
//        from.insertSiblingAfter(relatesTo);
//        from.insertSiblingBefore(to);
//        to.insertSiblingBefore(messageID);
//
//        //print(root,new PrintStream(new FileOutputStream(OUT_FILE_NAME)));
    }
}
