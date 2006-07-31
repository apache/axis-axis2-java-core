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

package org.apache.axis2.addressing;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.Map;


public class EndpointReferenceTypeTest extends TestCase {

    EndpointReference endpointReference;
    private String address = "htttp://wwww.openource.lk/~chinthaka";

    public static void main(String[] args) {
        TestRunner.run(EndpointReferenceTypeTest.class);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        endpointReference = new EndpointReference(address);
    }

    public void testGetAndSetAddress() {
        assertEquals("Address not set properly in the constructor",
                address,
                endpointReference.getAddress());

        String newAddress = "http://www.axis2.com";
        endpointReference.setAddress(newAddress);
        assertEquals("Address not set properly in the setter method",
                newAddress,
                endpointReference.getAddress());
    }

    public void testGetAndSetReferenceParameters() {
        for (int i = 0; i < 10; i++) {
            endpointReference.addReferenceParameter(
                    new QName("http://www.opensouce.lk/" + i, "" + i),
                    "value " + i * 50);
        }

        Map retrievedReferenceParameters = endpointReference.getAllReferenceParameters();
        for (int i = 0; i < 10; i++) {
            OMElement referenceParameter = (OMElement) retrievedReferenceParameters.get(
                    new QName("http://www.opensouce.lk/" + i, "" + i));
            assertEquals(
                    "Input value differs from what is taken out from AnyContentType",
                    referenceParameter.getText(),
                    "value " + i * 50);
        }
    }
    
    public void testHasAnonymousAddress(){
        // Default EndpointReference does not has 'anonymous address'
        assertFalse(endpointReference.hasAnonymousAddress());
        
        // EndpointReference with 2005/08 Anonymous address
        EndpointReference epr200508anon = new EndpointReference(AddressingConstants.Final.WSA_ANONYMOUS_URL);
        assertTrue(epr200508anon.hasAnonymousAddress());
        
        // EndpointReference with 2004/08 Anonymous address
        EndpointReference epr200408anon = new EndpointReference(AddressingConstants.Submission.WSA_ANONYMOUS_URL);
        assertTrue(epr200408anon.hasAnonymousAddress());
    }
    
    public void testToAndFromOM() throws Exception{
        OMFactory omf = OMAbstractFactory.getOMFactory();
        OMNamespace ns1 = omf.createOMNamespace("http://uri1","prefix1");
        OMAttribute attr1 = omf.createOMAttribute("attr1",ns1,"attr1value");
        OMNamespace ns2 = omf.createOMNamespace("http://uri2","prefix2");
        OMAttribute attr2 = omf.createOMAttribute("attr2",ns2,"attr2value");
        endpointReference.addAttribute(attr1);
        endpointReference.addAttribute(attr2);
        OMElement md1 = omf.createOMElement("md1", "http://mduri1", "md1prefix"); 
        OMElement md2 = omf.createOMElement("md2", "http://mduri2", "md2prefix");
        endpointReference.addMetaData(md1);
        endpointReference.addMetaData(md2);
        OMElement ext1 = omf.createOMElement("ext1", "http://exturi1", "ext1prefix"); 
        OMElement ext2 = omf.createOMElement("ext2", "http://exturi2", "ext2prefix");
        endpointReference.addExtensibleElement(ext1);
        endpointReference.addExtensibleElement(ext2);
        QName rp1Qname = new QName("http://rp1uri","refParm1","rp1prefix");
        QName rp2Qname = new QName("http://rp2uri","refParm2","rp2prefix");
        endpointReference.addReferenceParameter(rp1Qname,"rp1");
        endpointReference.addReferenceParameter(rp2Qname,"rp2");
        
        OMElement om = endpointReference.toOM("http://nsurl","localName","prefix");
        
        EndpointReference deser = new EndpointReference("");
        deser.fromOM(om);
        
        assertEquals(endpointReference.getAddress(),deser.getAddress());
        ArrayList attrs = deser.getAttributes();
        assertEquals(attr1,attrs.get(0));
        assertEquals(attr2,attrs.get(1));
        
        ArrayList metadata = deser.getMetaData();
        assertEquals(md1, metadata.get(0));
        assertEquals(md2, metadata.get(1));
        
        ArrayList extelts = deser.getExtensibleElements();
        assertEquals(ext1,extelts.get(0));
        assertEquals(ext2,extelts.get(1));
        
        Map m = deser.getAllReferenceParameters();
        assertEquals("rp1", ((OMElement)m.get(rp1Qname)).getText());
        assertEquals("rp2", ((OMElement)m.get(rp2Qname)).getText());
    }
}
