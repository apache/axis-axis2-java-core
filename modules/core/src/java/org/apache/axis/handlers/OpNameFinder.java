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
package org.apache.axis.handlers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.HandlerMetadata;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.SOAPBody;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.wsdl.WSDLService;

import javax.xml.namespace.QName;

public class OpNameFinder extends AbstractHandler {
    public static final QName NAME =
            new QName("http://axis.ws.apache.org", "OpNameFinder");

    public OpNameFinder() {
        init(new HandlerMetadata(NAME));
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        String style = msgContext.getMessageStyle();
        if (style.equals(WSDLService.STYLE_RPC)) {
            SOAPEnvelope envelope = msgContext.getEnvelope();
            SOAPBody body = envelope.getBody();
            OMElement bodyChild = body.getFirstElement();
            msgContext.setSoapOperationElement(bodyChild);
            QName opName =
            new QName(
                    bodyChild.getNamespaceName(),
                    bodyChild.getLocalName());
            AxisService service = msgContext.getService();
            AxisOperation op = service.getOperation(opName);
            if (op != null) {
                msgContext.setOperation(op);
            } else {
                throw new AxisFault(opName + " operation not found");
            }

        }
    }
}
