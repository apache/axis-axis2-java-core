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

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.AbstractTestCase;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;

/**
 * This test case tests the basic expectations of the engine from the OM.
 */
public class OMTest extends AbstractTestCase{
    private SOAPEnvelope envelope;
    private OMFactory fac;
    /**
     * Constructor.
     */
    public OMTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        File file = getTestResourceFile("soap/sample1.xml");
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
        fac = OMFactory.newInstance();
        OMXMLParserWrapper builder = new StAXSOAPModelBuilder(fac,parser);
        envelope = (SOAPEnvelope) builder.getDocumentElement();
    }

    
    /**
     * Sometime the hasNext() in the childeren iterator is true yet the next() is null
     */

    public void testNullInChilderen(){
        isNullChildrenThere(envelope);
    }
    
    /**
     * the envelope is completly namesapce qulified so all the OMElements got to have namespace values not null
     *
     */
    public void test4MissingNamespaces(){
        isNameSpacesMissing(envelope);
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
        assertNotNull(omns.getName());
        Iterator it = omeleent.getChildren();
        while(it.hasNext()){
            OMNode node = (OMNode)it.next();
            
            if(node != null && node.getType() == OMNode.ELEMENT_NODE){
                isNameSpacesMissing((OMElement)node);
            }
        }
    }

     public void testRootNotCompleteInPartialBuild() throws Exception {
         assertFalse("Root should not be complete",envelope.isComplete());
    }

    /**
     * Assumption - The fed XML has at least two children under the root element
     * @throws Exception
     */
    public void testFirstChildDetach() throws Exception {
        OMElement root= envelope;
        assertFalse("Root should not be complete",root.isComplete());
        OMNode oldFirstChild = root.getFirstChild();
        assertNotNull(oldFirstChild);
        oldFirstChild.detach();

        OMNode newFirstChild = root.getFirstChild();
        assertNotNull(newFirstChild);

        assertNotSame(oldFirstChild,newFirstChild);
    }

    //todo this is wrong correct this
    public void testAdditionOfaCompletelyNewElement() throws Exception {

//        OMElement root= envelope;
//
//        OMNamespace soapenv= root.findInScopeNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
//        OMNamespace wsa= root.findInScopeNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");
//        if (wsa==null)
//            wsa= root.declareNamespace("http://schemas.xmlsoap.org/ws/2004/03/addressing", "wsa");
//
//        //Assumption - A RelatesTo Element does not exist in the input document
//        OMElement relatesTo= fac.createOMElement ("RelatesTo", wsa);
//        relatesTo.insertAttribute(fac.createOMAttribute("RelationshipType", null, "wsa:Reply", relatesTo));
//        relatesTo.insertAttribute(fac.createOMAttribute("mustUnderstand", soapenv, "0", relatesTo));
//        relatesTo.addChild(fac.createText(relatesTo, "uuid:3821F4F0-D020-11D8-A10A-E4EE6425FCB0"));
//        relatesTo.setComplete(true);
//
//        root.addChild(relatesTo);
//
//        QName name = new QName(wsa.getName(),"RelatesTo",wsa.getPrefix());
//
//        Iterator children = root.getChildrenWithName(name);
//        //this should contain only one child!
//        if (children.hasNext()){
//            OMElement newlyAddedElement = (OMElement)children.next();
//
//            assertNotNull(newlyAddedElement);
//
//            assertEquals(newlyAddedElement.getLocalName(),"RelatesTo");
//            //todo put the other assert statements here
//        }else{
//            assertFalse("New child not added",true);
//        }

    }
}
