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
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.MultiParentClassLoader;

import java.lang.reflect.Method;
import java.net.URL;

public abstract class AbstractMessageReceiver implements MessageReceiver {
    public static final String SCOPE = "scope";
    protected String serviceTCCL = null;
    public static final String SAVED_TCCL = "_SAVED_TCCL_";
    public static final String SAVED_MC = "_SAVED_MC_";


    // Place to store previous values
    public class ThreadContextDescriptor {
        public ClassLoader oldClassLoader;
        public MessageContext oldMessageContext;
    }

    /**
     * Several pieces of information need to be available to the service
     * implementation class.  For one, the ThreadContextClassLoader needs
     * to be correct, and for another we need to give the service code
     * access to the MessageContext (getCurrentContext()).  So we toss these
     * things in TLS.
     *
     * @param msgContext
     */
    protected ThreadContextDescriptor
            setThreadContext(MessageContext msgContext) {
        ThreadContextDescriptor tc = new ThreadContextDescriptor();
        tc.oldMessageContext = (MessageContext) MessageContext.currentMessageContext.get();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        tc.oldClassLoader = contextClassLoader;

        AxisService service =
                msgContext.getAxisService();
        String serviceTCCL = (String) service.getParameterValue(
                Constants.SERVICE_TCCL);
        if (serviceTCCL != null) {
            serviceTCCL = serviceTCCL.trim().toLowerCase();


            if (serviceTCCL.equals(Constants.TCCL_COMPOSITE)) {
                Thread.currentThread().setContextClassLoader(
                        new MultiParentClassLoader(new URL[]{}, new ClassLoader[]{
                                msgContext.getAxisService().getClassLoader(),
                                contextClassLoader,
                        }));
            } else if (serviceTCCL.equals(Constants.TCCL_SERVICE)) {
                Thread.currentThread().setContextClassLoader(
                        msgContext.getAxisService().getClassLoader()
                );
            }
        }
        MessageContext.setCurrentMessageContext(msgContext);
        return tc;
    }

    protected void restoreThreadContext(ThreadContextDescriptor tc) {
        Thread.currentThread().setContextClassLoader(tc.oldClassLoader);
        MessageContext.currentMessageContext.set(tc.oldMessageContext);
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
                    msgContext.getAxisService();
            ClassLoader classLoader = service.getClassLoader();

            // allow alternative definition of makeNewServiceObject
            if (service.getParameter(Constants.SERVICE_OBJECT_SUPPLIER) != null) {
                Parameter serviceObjectParam =
                        service.getParameter(Constants.SERVICE_OBJECT_SUPPLIER);
                Class serviceObjectMaker = Loader.loadClass(classLoader, ((String)
                        serviceObjectParam.getValue()).trim());

                // Find static getServiceObject() method, call it if there   
                Method method = serviceObjectMaker.
                        getMethod("getServiceObject",
                                  new Class[]{AxisService.class});
                if (method != null) {
                    return method.invoke(serviceObjectMaker.newInstance(), new Object[]{service});
                }
            }

            Parameter implInfoParam = service.getParameter(Constants.SERVICE_CLASS);
            if (implInfoParam != null) {
                Class implClass = Loader.loadClass(
                        classLoader,
                        ((String) implInfoParam.getValue()).trim());

                return implClass.newInstance();
            } else {
                throw new AxisFault(
                        Messages.getMessage("paramIsNotSpecified", "SERVICE_OBJECT_SUPPLIER"));
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
        ServiceContext serviceContext = msgContext.getServiceContext();
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
