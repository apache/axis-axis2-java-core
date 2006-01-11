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
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
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

    /**
     * Method makeNewServiceObject.
     *
     * @param msgContext
     * @return Returns Object.
     * @throws AxisFault
     */
    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        try {
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

    public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getName();
        if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP12Factory();
        } else if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(nsURI)) {
            return OMAbstractFactory.getSOAP11Factory();
        } else {
            throw new AxisFault(Messages.getMessage("invalidSOAPversion"));
        }
    }

    /**
     * Method getTheImplementationObject.
     *
     * @param msgContext
     * @return Returns Object.
     * @throws AxisFault
     */
    protected Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        ServiceContext serviceContext = msgContext.getOperationContext().getServiceContext();
        Object serviceimpl = serviceContext.getServiceImpl();
        if (serviceimpl != null) {
            // since service impl is there in service context , take that from there
            return serviceimpl;
        } else {
            // create a new service impl class for that service
            serviceimpl = makeNewServiceObject(msgContext);
            serviceContext.setServiceImpl(serviceimpl);
            return serviceimpl;
        }
    }
}
