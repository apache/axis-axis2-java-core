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

package org.apache.axis2.jaxws.samples.handler;

import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

public class LoggingSOAPHandler implements SOAPHandler<SOAPMessageContext> {

    private PrintStream out;

    public LoggingSOAPHandler() {
        setLogStream(System.out);
    }

    protected final void setLogStream(PrintStream ps) {
        out = ps;
    }

    public void init(Map c) {
        System.out.println("LoggingHandler : init() Called....");
    }

    public Set<QName> getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext smc) {
        System.out.println("LoggingHandler : handleMessage Called....");
        logToSystemOut(smc);
        return true;
    }

    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("LoggingHandler : handleFault Called....");
        logToSystemOut(smc);
        return true;
    }

    // nothing to clean up
    public void close(MessageContext messageContext) {
        System.out.println("LoggingHandler : close() Called....");
    }

    // nothing to clean up
    public void destroy() {
        System.out.println("LoggingHandler : destroy() Called....");
    }

    /*
     * Check the MESSAGE_OUTBOUND_PROPERTY in the context
     * to see if this is an outgoing or incoming message.
     * Write a brief message to the print stream and
     * output the message. The writeTo() method can throw
     * SOAPException or IOException
     */
    protected void logToSystemOut(SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean)
                smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        out.println("===============================================");
        if (outboundProperty.booleanValue()) {
            out.println("Outbound message:");
        } else {
            out.println("Inbound message:");
        }

        SOAPMessage message = smc.getMessage();
        try {
            message.writeTo(out);
            out.println();
        } catch (Exception e) {
            out.println("Exception in handler: " + e);
        }
        out.println("===============================================");
    }
}
