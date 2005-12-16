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

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.SessionContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPFactory;

public abstract class AbstractMessageReceiver implements MessageReceiver {
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String SCOPE = "scope";
    protected SOAPFactory fac;

    /**
     * Method makeNewServiceObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        try {
            String nsURI = msgContext.getEnvelope().getNamespace().getName();

            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
                fac = OMAbstractFactory.getSOAP12Factory();
            } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
                fac = OMAbstractFactory.getSOAP11Factory();
            } else {
                throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
            }

            AxisService service =
                    msgContext.getOperationContext().getServiceContext().getAxisService();
            ClassLoader classLoader = service.getClassLoader();
            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);

            if (implInfoParam != null) {
                Class implClass = Class.forName(((String) implInfoParam.getValue()).trim(), true,
                        classLoader);

                return implClass.newInstance();
            } else {
                throw new AxisFault(Messages.getMessage("paramIsNotSpecified", "SERVICE_CLASS"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public SOAPFactory getSOAPFactory() {
        return fac;
    }

    /**
     * Method getTheImplementationObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        AxisService service =
                msgContext.getOperationContext().getServiceContext().getAxisService();
        Parameter scopeParam = service.getParameter(SCOPE);
        String serviceName = service.getName();

        if ((scopeParam != null) && Constants.SESSION_SCOPE.equals(scopeParam.getValue())) {
            SessionContext sessionContext = msgContext.getSessionContext();

            synchronized (sessionContext) {
                Object obj = sessionContext.getProperty(serviceName);

                if (obj == null) {
                    obj = makeNewServiceObject(msgContext);
                    sessionContext.setProperty(serviceName, obj);
                }

                return obj;
            }
        } else if ((scopeParam != null)
                && Constants.APPLICATION_SCOPE.equals(scopeParam.getValue())) {
            ConfigurationContext globalContext = msgContext.getConfigurationContext();

            synchronized (globalContext) {
                Object obj = globalContext.getProperty(serviceName);

                if (obj == null) {
                    obj = makeNewServiceObject(msgContext);
                    globalContext.setProperty(serviceName, obj);
                }

                return obj;
            }
        } else {
            return makeNewServiceObject(msgContext);
        }
    }
}
