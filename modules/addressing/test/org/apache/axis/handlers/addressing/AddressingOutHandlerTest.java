package org.apache.axis.handlers.addressing;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.AnyContentType;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.addressing.ServiceName;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.OperationContextFactory;
import org.apache.axis.handlers.util.TestUtil;
import org.apache.axis.om.OMAbstractFactory;
import org.apache.axis.om.impl.llom.util.XMLComparator;
import org.apache.axis.soap.SOAPEnvelope;
import org.apache.axis.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.wsdl.WSDLConstants;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public class AddressingOutHandlerTest extends TestCase implements AddressingConstants {
    /**
     * Eran Chinthaka (chinthaka@apache.org)
     */
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

        msgCtxt = new MessageContext(null, null, null,null,OperationContextFactory.createMEPContext(WSDLConstants.MEP_CONSTANT_IN_OUT,false,null,null));

    }

    public void testAddToSOAPHeader() throws Exception {
        EndpointReference epr = new EndpointReference(WSA_FROM, "http://www.from.org/service/");
        epr.setPortType(new QName("http://www.from.org/service/port/", "Port", "portNS"));
        epr.setServiceName(new ServiceName(new QName("http://www.from.org/service/", "Service", "serviceNS"), "port"));

        AnyContentType anyContentType = new AnyContentType();
        for (int i = 0; i < 5; i++) {
            anyContentType.addReferenceValue(new QName("Reference" + i), "Value " + i * 100);

        }

        epr.setReferenceParameters(anyContentType);

        SOAPEnvelope defaultEnvelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();

        defaultEnvelope.getHeader().declareNamespace(WSA_NAMESPACE, "wsa");
        outHandler.addToSOAPHeader(epr, WSA_FROM, defaultEnvelope.getHeader());

        StAXSOAPModelBuilder omBuilder = testUtil.getOMBuilder("eprTest.xml");
        XMLComparator xmlComparator = new XMLComparator();
        assertTrue(xmlComparator.compare(omBuilder.getDocumentElement(), defaultEnvelope));
    }

//    public void testHeaderCreationFromMsgCtxtInformation() throws Exception {
//        MessageInformationHeadersCollection mIHeaders = new MessageInformationHeadersCollection();
//
//        AnyContentType referenceValues = new AnyContentType();
//
//        EndpointReference epr = new EndpointReference(WSA_FROM, "http://www.from.org/service/");
//        referenceValues.addReferenceValue(new QName("Reference2"), "Value 200");
//        epr.setReferenceParameters(referenceValues);
//        mIHeaders.setFrom(epr);
//
//        epr = new EndpointReference(WSA_TO, "http://www.to.org/service/");
//        referenceValues = new AnyContentType();
//        referenceValues.addReferenceValue(new QName("Reference1"), "Value 100");
//        epr.setReferenceProperties(referenceValues);
//        epr.setServiceName(new ServiceName(new QName("http://www.from.org/service/", "Service", "serviceNS"), "port"));
//        mIHeaders.setTo(epr);
//
//        epr = new EndpointReference(WSA_REPLY_TO, "http://www.replyTo.org/service/");
//        referenceValues = new AnyContentType();
//        referenceValues.addReferenceValue(new QName("Reference3"), "Value 300");
//        epr.setPortType(new QName("http://www.from.org/service/port/", "Port", "portNS"));
//        epr.setReferenceProperties(referenceValues);
//
//        referenceValues = new AnyContentType();
//        referenceValues.addReferenceValue(new QName("Reference4"), "Value 400");
//        epr.setReferenceParameters(referenceValues);
//        mIHeaders.setTo(epr);
//
//        mIHeaders.setMessageId("123456-7890");
//        mIHeaders.setAction("http://www.actions.org/action");
//
//        RelatesTo relatesTo = new RelatesTo("http://www.relatesTo.org/service/", "TestRelation");
//        mIHeaders.setRelatesTo(relatesTo);
//
//        msgCtxt.setMessageInformationHeaders(mIHeaders);
//        msgCtxt.setEnvelope(OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope());
//        outHandler.invoke(msgCtxt);
//
//        XMLComparator xmlComparator = new XMLComparator();
//        assertTrue(xmlComparator.compare(msgCtxt.getEnvelope(), testUtil.getOMBuilder("OutHandlerTest.xml").getDocumentElement()));
//    }
}
