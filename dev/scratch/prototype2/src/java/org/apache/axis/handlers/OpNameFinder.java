/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.handlers;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetaData;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.*;

import javax.xml.namespace.QName;

public class OpNameFinder extends AbstractHandler {

    public static final QName NAME = new QName("http://axis.ws.apache.org", "OpNameFinder");


    public OpNameFinder() {
        init(new HandlerMetaData(NAME));
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        int style = msgContext.getMessageStyle();


        if (style == Constants.SOAP_STYLE_RPC_ENCODED || style == Constants.SOAP_STYLE_RPC_LITERAL) {
            SOAPEnvelope envelope = msgContext.getEnvelope();
            SOAPBody body = envelope.getBody();
            OMNode node = body.getFirstChild();
            while (node != null) {
                int type = node.getType();
                if (type == OMNode.ELEMENT_NODE) {
                    OMElement bodyChild = (OMElement) node;
                    msgContext.setSoapOperationElement(bodyChild);
                    OMNamespace omns = bodyChild.getNamespace();
                    if (omns != null) {
                        String ns = omns.getName();
                        if (ns != null) {
                            QName opName = new QName(ns, bodyChild.getLocalName());
                            AxisService service = msgContext.getService();
                            AxisOperation op = service.getOperation(opName);
                            if (op != null) {
                                msgContext.setOperation(op);
                            } else {
                                throw new AxisFault(opName + " operation not found");
                            }
                            break;
                        }

                    } else {
                        throw new AxisFault("SOAP Body must be NS Qualified");
                    }
                }
                node = node.getNextSibling();
            }
        }
    }
}
