/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.datasource.jaxb;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.Constants;

import junit.framework.TestCase;

/**
 * Test that the JAXB Payload streaming can be disabled. 
 */
public class JAXBCustomBuilderDisableStreamingTests extends TestCase {
    
    public void testDisableJAXBPayloadStreaming() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Disable the JAXB Payload streaming
        msgCtx.setProperty(Constants.JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING, new Boolean(false));
        jaxbDSC.setMessageContext(msgCtx);
        assertThat(jaxbCB.accepts(mock(SOAPBody.class), 3, "ns", "lp")).isFalse();
    }
    
    public void testDisableJAXBPayloadStreamingWithHighFidelity() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Disable the JAXB Payload streaming
        msgCtx.setProperty(Constants.JAXWS_PAYLOAD_HIGH_FIDELITY, new Boolean(true));
        jaxbDSC.setMessageContext(msgCtx);
        assertThat(jaxbCB.accepts(mock(SOAPBody.class), 3, "ns", "lp")).isFalse();
    }
    
    public void testDisableJAXBPayloadStreamingWithHighFidelityParameter() throws Exception {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        AxisService service = new AxisService();
        msgCtx.setAxisService(service);
        service.addParameter(Constants.JAXWS_PAYLOAD_HIGH_FIDELITY, "true");
        
        jaxbDSC.setMessageContext(msgCtx);
        assertThat(jaxbCB.accepts(mock(SOAPBody.class), 3, "ns", "lp")).isFalse();
    }
    
    public void testDefaultJAXBPayloadStreaming() {
        
        JAXBDSContext jaxbDSC = new JAXBDSContext(null, null);
        JAXBCustomBuilder jaxbCB = new JAXBCustomBuilder(jaxbDSC);
        MessageContext msgCtx = new MessageContext();
        // Do NOT Disable the JAXB Payload streaming; the default should be ON
        // msgCtx.setProperty(Constants.JAXWS_ENABLE_JAXB_PAYLOAD_STREAMING, new Boolean(false));
        jaxbDSC.setMessageContext(msgCtx);
        assertThat(jaxbCB.accepts(mock(SOAPBody.class), 3, "ns", "lp")).isTrue();
    }
}
