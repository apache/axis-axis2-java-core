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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * If the service implementation has an init method with 1 or 2 message context as its parameters, then
 * the DependencyManager calls the init method with appropriate parameters.
 */
public class DependencyManager {
    private static final Log log = LogFactory.getLog(DependencyManager.class);
    public final static String SERVICE_INIT_METHOD = "init";
    public final static String SERVICE_START_METHOD = "startUp";
    public final static String SERVICE_DESTROY_METHOD = "destroy";

    public static void initServiceClass(Object obj,
                                        ServiceContext serviceContext) {
        try {
            Class classToLoad = obj.getClass();
            // We can not call classToLoad.getDeclaredMethed() , since there
            //  can be insatnce where mutiple services extends using one class
            // just for init and other reflection methods
            Method method =
                    classToLoad.getMethod(SERVICE_INIT_METHOD, new Class[]{ServiceContext.class});
            if (method != null) {
                method.invoke(obj, new Object[]{serviceContext});
            }
        } catch (SecurityException e) {
            log.info("Exception trying to call " + SERVICE_INIT_METHOD, e);
        } catch (IllegalArgumentException e) {
            log.info("Exception trying to call " + SERVICE_INIT_METHOD, e);
        } catch (IllegalAccessException e) {
            log.info("Exception trying to call " + SERVICE_INIT_METHOD, e);
        } catch (InvocationTargetException e) {
            log.info("Exception trying to call " + SERVICE_INIT_METHOD, e);
        } catch (NoSuchMethodException e) {
            log.debug("Exception trying to call " + SERVICE_INIT_METHOD, e);
        }
    }

    /**
     * To init all the services in application scope
     *
     * @param serviceGroupContext
     * @throws AxisFault
     */
    public static void initService(ServiceGroupContext serviceGroupContext) throws AxisFault {
        AxisServiceGroup serviceGroup = serviceGroupContext.getDescription();
        Iterator serviceItr = serviceGroup.getServices();
        while (serviceItr.hasNext()) {
            AxisService axisService = (AxisService) serviceItr.next();
            ServiceContext serviceContext = ContextFactory.createServiceContext(
                    serviceGroupContext, axisService);
            AxisService service = serviceContext.getAxisService();
            ClassLoader classLoader = service.getClassLoader();
            Parameter implInfoParam = service.getParameter(Constants.SERVICE_CLASS);
            if (implInfoParam != null) {
                try {
                    Class implClass = Loader.loadClass(
                            classLoader,
                            ((String) implInfoParam.getValue()).trim());
                    Object serviceImpl = implClass.newInstance();
                    serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceImpl);
                    initServiceClass(serviceImpl, serviceContext);
                } catch (Exception e) {
                    new AxisFault(e);
                }
            }
        }
    }

    /**
     * To startup service when user puts load-on-startup parameter
     */


    public static void destroyServiceObject(ServiceContext serviceContext) {
        try {
            Object obj = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
            if (obj != null) {
                Class classToLoad = obj.getClass();

                // We can not call classToLoad.getDeclaredMethed() , since there
                //  can be insatnce where mutiple services extends using one class
                // just for init and other reflection methods
                Method[] methods = classToLoad.getMethods();

                for (int i = 0; i < methods.length; i++) {
                    if (SERVICE_DESTROY_METHOD.equals(methods[i].getName())
                            && (methods[i].getParameterTypes().length == 1)
                            && (methods[i].getParameterTypes()[0] == ServiceContext.class)) {
                        methods[i].invoke(obj, new Object[]{serviceContext});
                        break;
                    }
                }
            }
        } catch (SecurityException e) {
            log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
        } catch (IllegalArgumentException e) {
            log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
        } catch (IllegalAccessException e) {
            log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
        } catch (InvocationTargetException e) {
            log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
        }
    }
}
