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

/*
* Reflection based RPCMessageReceiver , request will be processed by looking at the method signature
* of the invocation method
*/

package org.apache.axis2.rpc.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

public class RPCMessageReceiver extends AbstractInOutSyncMessageReceiver {


    private Method method;
    private static Log log = LogFactory.getLog(RPCInOnlyMessageReceiver.class);

    /**
     * reflect and get the Java method
     * - for each i'th param in the java method
     * - get the first child's i'th child
     * -if the elem has an xsi:type attr then find the deserializer for it
     * - if not found,
     * lookup deser for th i'th param (java type)
     * - error if not found
     * - deserialize & save in an object array
     * - end for
     * <p/>
     * - invoke method and get the return value
     * <p/>
     * - look up serializer for return value based on the value and type
     * <p/>
     * - create response msg and add return value as grand child of <soap:body>
     *
     * @param inMessage
     * @param outMessage
     * @throws AxisFault
     */

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();
            DependencyManager.configureBusinessLogicProvider(obj,
                    inMessage.getOperationContext());

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();
            AxisMessage inaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            String messageNameSpace = null;
            if (inaxisMessage != null) {
                messageNameSpace = inaxisMessage.getElementQName().getNamespaceURI();
            }

            OMNamespace namespace = methodElement.getNamespace();
            if (namespace == null || !messageNameSpace.equals(namespace.getName())) {
                throw new AxisFault("namespace mismatch require " +
                        messageNameSpace +
                        " found " + methodElement.getNamespace().getName());
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }


            Object[] objectArray = RPCUtil.processRequest(methodElement, method);
            Object resObject;
            try {
                resObject = method.invoke(obj, objectArray);
            } catch (Exception e) {
                throw new AxisFault(e.getMessage());
            }
            SOAPFactory fac = getSOAPFactory(inMessage);

            // Handling the response
            AxisMessage outaxisMessage = op.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            if (inaxisMessage != null) {
                messageNameSpace = outaxisMessage.getElementQName().getNamespaceURI();
            }

            OMNamespace ns = fac.createOMNamespace(messageNameSpace,
                    service.getSchematargetNamespacePrefix());
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            OMElement bodyContent = null;

            if (resObject instanceof Object[]) {
                QName resName = new QName(service.getSchematargetNamespace(),
                        method.getName() + "Response",
                        service.getSchematargetNamespacePrefix());
                OMElement bodyChild = RPCUtil.getResponseElement(resName, (Object[]) resObject);
                envelope.getBody().addChild(bodyChild);
            } else {
                RPCUtil.processResponse(fac, resObject, bodyContent, ns, envelope, method);
            }

            outMessage.setEnvelope(envelope);

        } catch (Exception e) {
            String msg = "Exception occurred while trying to invoke service method " +
                         method.getName();
            log.error(msg, e);
            throw new AxisFault(msg, e);
        }
    }


}
