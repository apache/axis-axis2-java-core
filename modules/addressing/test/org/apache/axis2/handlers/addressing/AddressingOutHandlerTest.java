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

package org.apache.axis2.handlers.addressing;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.impl.llom.util.XMLComparator;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;

import javax.xml.namespace.QName;

public class AddressingOutHandlerTest extends TestCase implements AddressingConstants {
    private AddressingOutHandler outHandler;
    private MessageContext msgCtxt;
    private TestUtil testUtil;

    public AddressingOutHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        outHandler = new AddressingOutHandler();
        testUtil = new TestUtil();

    }

    public void testAddToSOAPHeader() throws Exception {
        EndpointReference replyTo = new EndpointReference("http://schemas.xmlsoap.org/ws/2004/08/addressing/role/anonymous");
        EndpointReference epr = new EndpointReference("http://www.to.org/service/");

        for (int i = 0; i < 5; i++) {
            epr.addReferenceParameter(
                    new QName(Submission.WSA_NAMESPACE, "Reference" + i, AddressingConstants.WSA_DEFAULT_PREFIX),
                    "Value " + i * 100);

        }


        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();

        MessageContext msgCtxt = new MessageContext();
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);
        msgCtxt.setTo(epr);
        msgCtxt.setReplyTo(replyTo);
        msgCtxt.setEnvelope(defaultEnvelope);
        outHandler.invoke(msgCtxt);

        StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder("eprTest.xml");

        XMLComparator xmlComparator = new XMLComparator();

        assertTrue(
                xmlComparator.compare(omBuilder.getDocumentElement(),
                        defaultEnvelope));

    }

    public void testHeaderCreationFromMsgCtxtInformation() throws Exception {
        msgCtxt = new MessageContext();

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr =
                new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLComparator xmlComparator = new XMLComparator();
        assertTrue(
                xmlComparator.compare(msgCtxt.getEnvelope(),
                        testUtil.getOMBuilder("OutHandlerTest.xml")
                .getDocumentElement()));
    }

    public void testMustUnderstandSupport() throws Exception {
        msgCtxt = new MessageContext();

        msgCtxt.setProperty(AddressingConstants.ADD_MUST_UNDERSTAND_TO_ADDRESSING_HEADERS, Boolean.TRUE);
        
        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        epr.addReferenceParameter(new QName("Reference2"),
                "Value 200");
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        epr.addReferenceParameter(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");

        msgCtxt.setTo(epr);
        msgCtxt.setProperty(WS_ADDRESSING_VERSION, Submission.WSA_NAMESPACE);

        epr =
                new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.addRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLComparator xmlComparator = new XMLComparator();
        assertTrue(
                xmlComparator.compare(msgCtxt.getEnvelope(),
                        testUtil.getOMBuilder("mustUnderstandTest.xml")
                .getDocumentElement()));
    }

    public void testDuplicateHeaders() throws AxisFault {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        EndpointReference duplicateEpr = new EndpointReference("http://whatever.duplicate.org");
        msgCtxt = new MessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.setTo(eprOne);
        outHandler.invoke(msgCtxt);

        // now the soap message within the msgCtxt must have a to header.
        // lets invoke twice and see
        msgCtxt.setTo(duplicateEpr);
        outHandler.invoke(msgCtxt);

        assertTrue(defaultEnvelope.getHeader().getFirstChildWithName(new QName("http://whatever.duplicate.org")) == null);


    }

    public void testDuplicateHeadersWithOverridingOn() throws AxisFault {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        msgCtxt = new MessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_TO, factory.createOMNamespace(
                Final.WSA_NAMESPACE, WSA_DEFAULT_PREFIX));
        soapHeaderBlock.setText("http://oldEPR.org");
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.setProperty(REPLACE_ADDRESSING_HEADERS, Boolean.TRUE);
        msgCtxt.setTo(eprOne);
        outHandler.invoke(msgCtxt);

        assertTrue("http://whatever.org".equals(defaultEnvelope.getHeader().getFirstChildWithName(new QName(WSA_TO)).getText()));


    }

    public void testDuplicateHeadersWithOverridingOff() throws AxisFault {

        // this will check whether we can add to epr, if there is one already.
        EndpointReference eprOne = new EndpointReference("http://whatever.org");
        msgCtxt = new MessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        SOAPHeaderBlock soapHeaderBlock =
                defaultEnvelope.getHeader().addHeaderBlock(WSA_TO, factory.createOMNamespace(
                Final.WSA_NAMESPACE, WSA_DEFAULT_PREFIX));
        soapHeaderBlock.setText("http://oldEPR.org");
        msgCtxt.setEnvelope(defaultEnvelope);

        msgCtxt.setProperty(REPLACE_ADDRESSING_HEADERS, Boolean.FALSE);
        msgCtxt.setTo(eprOne);
        outHandler.invoke(msgCtxt);

        assertTrue("http://oldEPR.org".equals(defaultEnvelope.getHeader().getFirstChildWithName(new QName(WSA_TO)).getText()));


    }
}
