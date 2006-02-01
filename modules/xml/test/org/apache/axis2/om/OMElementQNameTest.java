/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */
package org.apache.ws.commons.om;

import org.apache.ws.commons.om.util.ElementHelper;

import javax.xml.namespace.QName;

/**
 * created 03-Nov-2005 11:46:32
 */

public class OMElementQNameTest extends OMTestCase {

    OMElement element;

    private static final String WSA= "http://schemas.xmlsoap.org/ws/2004/03/addressing";
    private static final String SOAPENV = "http://schemas.xmlsoap.org/soap/envelope/";

    public OMElementQNameTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        element = OMTestUtils.getOMBuilder(
                        getTestResourceFile(TestConstants.SOAP_SOAPMESSAGE1))
                        .getDocumentElement();
    }

    public void testSimpleQName() throws Exception {
        QName result = element.resolveQName("wsa:To");
        assertEquals(WSA,result.getNamespaceURI());
        assertEquals("wsa", result.getPrefix());
        assertEquals("To", result.getLocalPart());
    }

    public void testDefaultQName() throws Exception {
        QName result = element.resolveQName("localonly");
        assertEquals(SOAPENV, result.getNamespaceURI());
        assertEquals("soapenv", result.getPrefix());
        assertEquals("localonly", result.getLocalPart());
    }

    public void testDefaultQNameCanBeLocal() throws Exception {
        ElementHelper helper=new ElementHelper(element);
        QName result = helper.resolveQName("localonly",false);
        assertEquals("", result.getNamespaceURI());
        assertEquals("localonly", result.getLocalPart());
    }

    public void testNoLocal() throws Exception {
        assertResolvesToNull("wsa:");
    }

    public void testNoMatch() throws Exception {
        assertResolvesToNull("wsa2005:To");
    }

    public void testNothing() throws Exception {
        assertResolvesToNull(":");
    }



    private void assertResolvesToNull(String qname) {
        QName result = element.resolveQName(qname);
        assertNull("Expected "+qname+" to resolve to null",result);
    }

}
