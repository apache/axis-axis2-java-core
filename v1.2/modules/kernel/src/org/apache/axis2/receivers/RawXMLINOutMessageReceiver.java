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


package org.apache.axis2.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;

import java.lang.reflect.Method;

/**
 * The RawXMLINOutMessageReceiver MessageReceiver hands over the raw request received to
 * the service implementation class as an OMElement. The implementation class is expected
 * to return back the OMElement to be returned to the caller. This is a synchronous
 * MessageReceiver, and finds the service implementation class to invoke by referring to
 * the "ServiceClass" parameter value specified in the service.xml and looking at the
 * methods of the form OMElement <<methodName>>(OMElement request)
 *
 * @see RawXMLINOnlyMessageReceiver
 * @see RawXMLINOutAsyncMessageReceiver
 */
public class RawXMLINOutMessageReceiver extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {

    private Method findOperation(AxisOperation op, Class implClass) {
        String methodName = op.getName().getLocalPart();
        Method[] methods = implClass.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName) &&
                    methods[i].getParameterTypes().length == 1 &&
                    OMElement.class.getName().equals(
                            methods[i].getParameterTypes()[0].getName()) &&
                    OMElement.class.getName().equals(methods[i].getReturnType().getName())) {
                return methods[i];
            }
        }

        return null;
    }

    /**
     * Invokes the bussiness logic invocation on the service implementation class
     *
     * @param msgContext    the incoming message context
     * @param newmsgContext the response message context
     * @throws AxisFault on invalid method (wrong signature) or behaviour (return null)
     */
    public void invokeBusinessLogic(MessageContext msgContext, MessageContext newmsgContext)
            throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class implClass = obj.getClass();

            AxisOperation opDesc = msgContext.getAxisOperation();
            Method method = findOperation(opDesc, implClass);

            if (method != null) {
                OMElement result = (OMElement) method.invoke(
                        obj, new Object[]{msgContext.getEnvelope().getBody().getFirstElement()});
                SOAPFactory fac = getSOAPFactory(msgContext);
                SOAPEnvelope envelope = fac.getDefaultEnvelope();

                if (result != null) {
                    envelope.getBody().addChild(result);
                }

                newmsgContext.setEnvelope(envelope);

            } else {
                throw new AxisFault(Messages.getMessage("methodDoesNotExistInOut"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
