/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseLifecycleManager {
    
    private static final Log log = LogFactory.getLog(BaseLifecycleManager.class);
    
    protected Object instance;
    
    public void invokePostConstruct() throws LifecycleException {
        if (instance == null) {
            throw new LifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
        }
        Method method = getPostConstructMethod();
        if (method != null) {
            invokePostConstruct(method);
        }
    }

    protected void invokePostConstruct(Method method) throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking Method with @PostConstruct annotation");
        }
        invokeMethod(method, null);
        if (log.isDebugEnabled()) {
            log.debug("Completed invoke on Method with @PostConstruct annotation");
        }
    }

    public void invokePreDestroy() throws LifecycleException {
        if (instance == null) {
            throw new LifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
        }
        Method method = getPreDestroyMethod();
        if (method != null) {
            invokePreDestroy(method);
        }
    }

    protected void invokePreDestroy(Method method) throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking Method with @PreDestroy annotation");
        }
        invokeMethod(method, null);
        if (log.isDebugEnabled()) {
            log.debug("Completed invoke on Method with @PreDestroy annotation");
        }
    }

    protected void invokeMethod(Method m, Object[] params) throws LifecycleException {
        try {
            m.invoke(instance, params);
        } catch (InvocationTargetException e) {
            throw new LifecycleException(e);
        } catch (IllegalAccessException e) {
            throw new LifecycleException(e);
        }
    }

    protected Method getPostConstructMethod() {
        // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 

        //return Method with @PostConstruct Annotation.
        if (instance != null) {
            Class endpointClazz = instance.getClass();
            Method[] methods = endpointClazz.getMethods();

            for (Method method : methods) {
                if (isPostConstruct(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    protected Method getPreDestroyMethod() {
        // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 
        //return Method with @PreDestroy Annotation
        if (instance != null) {
            Class endpointClazz = instance.getClass();
            Method[] methods = endpointClazz.getMethods();

            for (Method method : methods) {
                if (isPreDestroy(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    protected boolean isPostConstruct(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            return PostConstruct.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }

    protected boolean isPreDestroy(Method method) {
        Annotation[] annotations = method.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            return PreDestroy.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }
   
}
