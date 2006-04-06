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
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * If the service implementation has an init method with 1 or 2 message context as its parameters, then
 * the DependencyManager calls the init method with appropriate parameters.
 */
public class DependencyManager {
    private final static String MESSAGE_CONTEXT_INJECTION_METHOD = "setOperationContext";
    private final static String SERVICE_INIT_METHOD = "init";
    private final static String SERVICE_DESTROY_METHOD = "destroy";

    public static void configureBusinessLogicProvider(Object obj,
                                                      OperationContext opCtx)
            throws AxisFault {
        try {
            Class classToLoad = obj.getClass();
            Method[] methods = classToLoad.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (MESSAGE_CONTEXT_INJECTION_METHOD.equals(methods[i].getName())
                        && (methods[i].getParameterTypes().length == 1)
                        && (methods[i].getParameterTypes()[0] == OperationContext.class)) {
                    methods[i].invoke(obj, new Object[]{opCtx});
                    break;
                }
            }
        } catch (SecurityException e) {
            throw new AxisFault(e);
        } catch (IllegalArgumentException e) {
            throw new AxisFault(e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e);
        } catch (InvocationTargetException e) {
            throw new AxisFault(e);
        }
    }

    public static void initServiceClass(Object obj,
                                        ServiceContext serviceContext) throws AxisFault {
        try {
            Class classToLoad = obj.getClass();
            Method[] methods = classToLoad.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (SERVICE_INIT_METHOD.equals(methods[i].getName())
                        && (methods[i].getParameterTypes().length == 1)
                        && (methods[i].getParameterTypes()[0] == ServiceContext.class)) {
                    methods[i].invoke(obj, new Object[]{serviceContext});
                    break;
                }
            }
        } catch (SecurityException e) {
            throw new AxisFault(e);
        } catch (IllegalArgumentException e) {
            throw new AxisFault(e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e);
        } catch (InvocationTargetException e) {
            throw new AxisFault(e);
        }
    }

    public static void destroyServiceClass(ServiceContext serviceContext) throws AxisFault {
        try {
            Object obj = serviceContext.getProperty(ServiceContext.SERVICE_CLASS);
            if (obj != null) {
                Class classToLoad = obj.getClass();
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
            throw new AxisFault(e);
        } catch (IllegalArgumentException e) {
            throw new AxisFault(e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e);
        } catch (InvocationTargetException e) {
            throw new AxisFault(e);
        }

    }

}
