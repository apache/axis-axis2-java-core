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
package org.apache.axis.receivers;

import java.lang.reflect.Method;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.engine.MessageSender;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLService;

/**
 * This is a Simple java Provider.
 */
public class RawXMLProvider extends AbstractInOutReceiver implements MessageReceiver {
    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Field scope
     */
    private String scope;

    /**
     * Field method
     */
    private Method method;

    /**
     * Field classLoader
     */
    private ClassLoader classLoader;

    /**
     * Constructor RawXMLProvider
     */
    public RawXMLProvider() {
        scope = Constants.APPLICATION_SCOPE;
    }

    /**
     * Method makeNewServiceObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        try {
            AxisService service = msgContext.getServiceContext().getServiceConfig();
            classLoader = service.getClassLoader();
            Class implClass = service.getServiceClass();
            return implClass.newInstance();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Method getTheImplementationObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    public Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getServiceContext().getServiceConfig();
        QName serviceName = service.getName();
        if (Constants.APPLICATION_SCOPE.equals(scope)) {
            return makeNewServiceObject(msgContext);
        } else if (Constants.SESSION_SCOPE.equals(scope)) {
            SessionContext sessionContext = msgContext.getSessionContext();
            Object obj = sessionContext.get(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                sessionContext.put(serviceName, obj);
            }
            return obj;
        } else if (Constants.GLOBAL_SCOPE.equals(scope)) {
            SessionContext globalContext = msgContext.getSessionContext();
            Object obj = globalContext.get(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                globalContext.put(serviceName, obj);
            }
            return obj;
        } else {
            throw new AxisFault("unknown scope " + scope);
        }
    }

    public void recieve(MessageContext msgContext) throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class ImplClass = obj.getClass();

            AxisOperation op = msgContext.getOperationContext().getOperationConfig();
            if (op == null) {
                throw new AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }
            Class[] parameters = method.getParameterTypes();
            if ((parameters != null)
                && (parameters.length == 1)
                && OMElement.class.getName().equals(parameters[0].getName())) {
                OMElement methodElement = msgContext.getEnvelope().getBody().getFirstElement();

                OMElement parmeter = null;
                SOAPEnvelope envelope = null;
                MessageContext msgContext1 = new MessageContext(msgContext);

                if (WSDLService.STYLE_DOC.equals(msgContext.getMessageStyle())) {
                    parmeter = methodElement;
                    Object[] parms = new Object[] { parmeter };

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);
                    envelope = OMFactory.newInstance().getDefaultEnvelope();
                    envelope.getBody().setFirstChild(result);

                } else if (WSDLService.STYLE_RPC.equals(msgContext.getMessageStyle())) {
                    parmeter = methodElement.getFirstElement();
                    Object[] parms = new Object[] { parmeter };

                    // invoke the WebService
                    OMElement result = (OMElement) method.invoke(obj, parms);
                    OMFactory fac = OMFactory.newInstance();
                    envelope = OMFactory.newInstance().getDefaultEnvelope();

                    OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
                    OMElement responseMethodName = fac.createOMElement(methodName + "Response", ns);
                    responseMethodName.addChild(result);
                    envelope.getBody().addChild(responseMethodName);
                } else {
                    throw new AxisFault("Unknown style ");
                }
                msgContext1.setEnvelope(envelope);
                MessageSender sender = new MessageSender(msgContext.getEngineContext());
                sender.send(msgContext1);
            } else {
                throw new AxisFault(
                    "Raw Xml provider supports only the methods bearing the signature public OMElement "
                        + "&lt;method-name&gt;(OMElement) where the method name is anything");
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }

}
