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
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AnyContentType;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.MessageInformationHeaders;
import org.apache.axis2.addressing.ServiceName;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.util.TestUtil;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.llom.util.XMLComparator;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.soap.impl.llom.builder.StAXSOAPModelBuilder;

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
        epr.setInterfaceName(
                new QName("http://www.from.org/service/port/",
                        "Port",
                        "portNS"));
        epr.setServiceName(
                new ServiceName(
                        new QName("http://www.from.org/service/",
                                "Service",
                                "serviceNS"),
                        "port"));

        AnyContentType anyContentType = new AnyContentType();
        for (int i = 0; i < 5; i++) {
            anyContentType.addReferenceValue(
                    new QName(Submission.WSA_NAMESPACE, "Reference" + i),
                    "Value " + i * 100);

        }

        epr.setReferenceParameters(anyContentType);

        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();

        defaultEnvelope.getHeader().declareNamespace(Submission.WSA_NAMESPACE,
                "wsa");
        MessageContext msgCtxt = new MessageContext(null);
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
        msgCtxt = new MessageContext(null);

        AnyContentType referenceValues = new AnyContentType();

        EndpointReference epr = new EndpointReference("http://www.from.org/service/");
        referenceValues.addReferenceValue(new QName("Reference2"),
                "Value 200");
        epr.setReferenceParameters(referenceValues);
        msgCtxt.setFrom(epr);

        epr = new EndpointReference("http://www.to.org/service/");
        referenceValues = new AnyContentType();
        referenceValues.addReferenceValue(
                new QName("http://reference.org", "Reference4", "myRef"),
                "Value 400");
        referenceValues.addReferenceValue(
                new QName("http://reference.org", "Reference3", "myRef"),
                "Value 300");
        epr.setReferenceParameters(referenceValues);

        epr.setServiceName(
                new ServiceName(
                        new QName("http://www.from.org/service/",
                                "Service",
                                "serviceNS"),
                        "port"));

        epr.setInterfaceName(
                new QName("http://www.from.org/service/port/",
                        "Port",
                        "portNS"));
        msgCtxt.setTo(epr);

        epr =
                new EndpointReference("http://www.replyTo.org/service/");
        msgCtxt.setReplyTo(epr);

        msgCtxt.setMessageID("123456-7890");
        msgCtxt.setWSAAction("http://www.actions.org/action");

        org.apache.axis2.addressing.RelatesTo relatesTo = new org.apache.axis2.addressing.RelatesTo(
                "http://www.relatesTo.org/service/", "TestRelation");
        msgCtxt.setRelatesTo(relatesTo);

        msgCtxt.setEnvelope(
                OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
        outHandler.invoke(msgCtxt);

        XMLComparator xmlComparator = new XMLComparator();
        assertTrue(
                xmlComparator.compare(msgCtxt.getEnvelope(),
                        testUtil.getOMBuilder("OutHandlerTest.xml")
                .getDocumentElement()));
    }
}
