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

import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class AddNumbersProtocolHandler implements javax.xml.ws.handler.soap.SOAPHandler<SOAPMessageContext> {

    HandlerTracker tracker = HandlerTracker.getHandlerTracker(AddNumbersProtocolHandler.class);
    
    public void close(MessageContext messagecontext) {
        tracker.close(messagecontext);
    }

    public Set<QName> getHeaders() {
        tracker.getHeaders();
        return null;
    }
    
    public boolean handleFault(SOAPMessageContext messagecontext) {
        tracker.handleFault(messagecontext);
        return true;
    }

    public boolean handleMessage(SOAPMessageContext messagecontext) {
        // Ensure that the expected headers are present
        JAXBContext context;
        try {
            context = JAXBContext.newInstance("org.test.addnumbershandler");
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
        QName qName= new QName("http://org/test/addnumbershandler","myHeader");

        try {
            Object[] values = messagecontext.getHeaders(qName, context, true);

            if (values.length > 0) {
                if (values.length !=3 ||
                        !((JAXBElement)values[0]).getValue().equals("Good1") ||
                        !((JAXBElement)values[1]).getValue().equals("Good2") ||
                        !((JAXBElement)values[2]).getValue().equals("Bad")) {
                    throw new WebServiceException("Failed getting all of the headers:" + values);
                }

                values = messagecontext.getHeaders(qName, context, false);
                if (values.length !=2 ||
                        !((JAXBElement)values[0]).getValue().equals("Good1") ||
                        !((JAXBElement)values[1]).getValue().equals("Good2")) {
                    throw new WebServiceException("Failed getting the expected headers:" + values);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }


        tracker.handleMessage(messagecontext);
        return true;
    }

}
