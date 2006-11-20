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

package org.apache.rampart;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.ws.security.WSConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.FileInputStream;
import java.util.Iterator;

import junit.framework.TestCase;

public class MessageBuilderTestBase extends TestCase {

    public MessageBuilderTestBase() {
        super();
    }

    public MessageBuilderTestBase(String arg0) {
        super(arg0);
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws AxisFault
     */
    protected MessageContext getMsgCtx() throws Exception {
        MessageContext ctx = new MessageContext();
        
        ctx.setConfigurationContext(new ConfigurationContext(new AxisConfiguration()));
        AxisService axisService = new AxisService("TestService");
        ServiceContext serviceContext = new ServiceContext(axisService, 
                new ServiceGroupContext(null, null));
        ctx.setServiceContext(serviceContext);
        ctx.setAxisService(axisService);
        ctx.setAxisOperation(new OutInAxisOperation(new QName("http://rampart.org", "test")));
        Options options = new Options();
        options.setAction("urn:testOperation");
        ctx.setOptions(options);

        XMLStreamReader reader =
                XMLInputFactory.newInstance().
                        createXMLStreamReader(new FileInputStream("test-resources/policy/soapmessage.xml"));
        ctx.setEnvelope(new StAXSOAPModelBuilder(reader, null).getSOAPEnvelope());
        return ctx;
    }

    protected Policy loadPolicy(String xmlPath) throws Exception {
        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    protected void verifySecHeader(Iterator qnameList, SOAPEnvelope env) {
        Iterator secHeaderChildren =
                env.getHeader().
                        getFirstChildWithName(new QName(WSConstants.WSSE_NS,
                                                        WSConstants.WSSE_LN)).getChildElements();

        while (secHeaderChildren.hasNext()) {
            OMElement element = (OMElement) secHeaderChildren.next();
            if (qnameList.hasNext()) {
                if (!element.getQName().equals(qnameList.next())) {
                    fail("Incorrect Element" + element);
                }
            } else {
                fail("Extra child in the security header: " + element.toString());
            }
        }

        if (qnameList.hasNext()) {
            fail("Incorrect number of children in the security header: " +
                 "next expected element" + qnameList.next().toString());
        }
    }

}
