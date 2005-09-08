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
package org.apache.axis2.rpc;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.databinding.DeserializationContext;
import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class RPCInOutMessageReceiver extends AbstractInOutSyncMessageReceiver {
    public static final String RPCMETHOD_PROPERTY = "rpc.method";

    public RPCInOutMessageReceiver() {
    }

    public void invokeBusinessLogic(MessageContext inMessage,
                                    MessageContext outMessage) throws AxisFault {
        SOAPEnvelope env = inMessage.getEnvelope();
        SOAPBody body = env.getBody();
        OMElement rpcElement = body.getFirstElement();

        /**
         * Locate method descriptor using QName or action
         */
        OperationContext oc = inMessage.getOperationContext();
        RPCMethod method = (RPCMethod)oc.getProperty(RPCMETHOD_PROPERTY);
        if (method == null) {
            throw new AxisFault("Couldn't find RPCMethod in OperationContext");
        }

        Method javaMethod = method.getJavaMethod();
        Object [] arguments = null;
        Object targetObject = this.getTheImplementationObject(inMessage);
        DeserializationContext dserContext = new DeserializationContext();

        // Run each argument (sub-element) through the appropriate deser
        Iterator args = rpcElement.getChildElements();
        Map elementCounts = new HashMap();
        while (args.hasNext()) {
            OMElement arg = (OMElement) args.next();
            QName qname = arg.getQName();
            RPCParameter param = method.getParameter(qname);
            if (param == null) {
                // unknown parameter.  Fault or continue depending on
                // strictness configuration.
                continue;
            }
            Integer count = (Integer)elementCounts.get(qname);
            if (count == null) count = new Integer(0);
            elementCounts.put(qname, new Integer(count.intValue() + 1));
            Deserializer dser = param.getDeserializer(count.intValue());
            // Got a recognized param, so feed this through the deserializer
            try {
                dserContext.deserialize(arg.getXMLStreamReader(), dser);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }
        }

        // OK, now we're done with the children.  If this is SOAP 1.2, we're
        // finished.  If it's SOAP 1.1, there may be multirefs which still
        // need to be deserialized after the RPC element.
        if (dserContext.isIncomplete()) {
            try {
                dserContext.processRest(rpcElement);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            if (dserContext.isIncomplete()) {
                throw new AxisFault("Unresolved multirefs!");
            }
        }

        arguments = new Object [method.getNumInParams()];
        Iterator params = method.getInParams();
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = ((RPCParameter)params.next()).getValue();
        }

        Object returnValue = null;
        try {
            returnValue = javaMethod.invoke(targetObject, arguments);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

        // The response parameter, if any, is where the return value should go
        RPCParameter responseParam = method.getResponseParameter();
        if (responseParam != null) {
            responseParam.setValue(returnValue);
        }

        // Now make the response message.
        try {
            SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = factory.createSOAPEnvelope();
            SOAPBody respBody = factory.createSOAPBody(responseEnv);

            // Just need to create this, since it automatically links itself
            // to the response body and will therefore get serialize()d at
            // the appropriate time.
            new RPCResponseElement(method, respBody);

            outMessage.setEnvelope(responseEnv);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
