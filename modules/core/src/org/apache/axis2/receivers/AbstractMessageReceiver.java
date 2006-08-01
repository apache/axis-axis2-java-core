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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.MultiParentClassLoader;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;

import java.lang.reflect.Method;
import java.net.URL;

public abstract class AbstractMessageReceiver implements MessageReceiver {
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String SERVICE_OBJECT_SUPPLIER = "ServiceObjectSupplier";
    public static final String SCOPE = "scope";

    protected void saveTCCL(MessageContext msgContext) {
        if (msgContext.getAxisService() != null &&
                msgContext.getAxisService().getClassLoader() != null) {
            Thread.currentThread().setContextClassLoader(new MultiParentClassLoader(new URL[]{}, new ClassLoader[]{
                    msgContext.getAxisService().getClassLoader(),
                    Thread.currentThread().getContextClassLoader(),
            }));
        }
    }

    protected void restoreTCCL() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        if (tccl != null && tccl instanceof MultiParentClassLoader) {
            Thread.currentThread().setContextClassLoader(((MultiParentClassLoader) tccl).getParents()[1]);
        }
    }

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

            // allow alternative definition of makeNewServiceObject
            if (service.getParameter(SERVICE_OBJECT_SUPPLIER) != null) {
                Parameter serviceObjectParam =
                        service.getParameter(SERVICE_OBJECT_SUPPLIER);
                Class serviceObjectMaker = Class.forName(((String)
                        serviceObjectParam.getValue()).trim(), true, classLoader);

                // Find static getServiceObject() method, call it if there   
                Method method = serviceObjectMaker.
                        getMethod("getServiceObject",
                                  new Class[] { MessageContext.class });
                if (method != null)
                    return method.invoke(null, new Object[] { msgContext });
            }

            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);
            if (implInfoParam != null) {
                Class implClass = Class.forName(((String) implInfoParam.getValue()).trim(), true,
                        classLoader);

                return implClass.newInstance();
            } else {
                throw new AxisFault(Messages.getMessage("paramIsNotSpecified", "SERVICE_OBJECT_SUPPLIER"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public SOAPFactory getSOAPFactory(MessageContext msgContext) throws AxisFault {
        String nsURI = msgContext.getEnvelope().getNamespace().getNamespaceURI();
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
        Object serviceimpl = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
        if (serviceimpl != null) {
            // since service impl is there in service context , take that from there
            return serviceimpl;
        } else {
            // create a new service impl class for that service
            serviceimpl = makeNewServiceObject(msgContext);
            //Service initialization
            DependencyManager.initServiceClass(serviceimpl,
                    msgContext.getServiceContext());
            serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceimpl);
            return serviceimpl;
        }
    }
}
