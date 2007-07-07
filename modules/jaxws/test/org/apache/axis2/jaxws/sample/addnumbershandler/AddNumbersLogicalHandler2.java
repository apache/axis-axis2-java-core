/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.MessageContext;

import org.apache.axis2.jaxws.handler.LogicalMessageContext;

public class AddNumbersLogicalHandler2 implements javax.xml.ws.handler.LogicalHandler<LogicalMessageContext> {

    public void close(MessageContext messagecontext) {
        
    }

    public boolean handleFault(LogicalMessageContext messagecontext) {
        Boolean outbound = (Boolean)messagecontext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outbound) {  // outbound response if we're on the server
            LogicalMessage msg = messagecontext.getMessage();
            String st = getStringFromSourcePayload(msg.getPayload());
            st = st.replaceFirst("blarg", "AddNumbersLogicalHandler2 was here");
            st = st.replaceFirst("I don't like the value 99", "AddNumbersLogicalHandler2 was here");
            msg.setPayload(new StreamSource(new ByteArrayInputStream(st.getBytes())));
        }
        return true;
    }

    public boolean handleMessage(LogicalMessageContext messagecontext) {
    	return true;
    }
    
    private static String getStringFromSourcePayload(Source payload) {
        try {

            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer trans = factory.newTransformer();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(baos);

            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.transform(payload, result);

            return new String(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
