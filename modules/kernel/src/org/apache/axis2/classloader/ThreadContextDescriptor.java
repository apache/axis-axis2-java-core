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

package org.apache.axis2.classloader;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;

public class ThreadContextDescriptor {

    private ClassLoader oldClassLoader;
    private MessageContext oldMessageContext;

    public ClassLoader getOldClassLoader() {
        return oldClassLoader;
    }

    public void setOldClassLoader(ClassLoader oldClassLoader) {
        this.oldClassLoader = oldClassLoader;
    }

    public MessageContext getOldMessageContext() {
        return oldMessageContext;
    }

    public void setOldMessageContext(MessageContext oldMessageContext) {
        this.oldMessageContext = oldMessageContext;
    }

    public static ThreadContextDescriptor setThreadContext(final AxisService service) {
        ThreadContextDescriptor tc = new ThreadContextDescriptor();
        tc.oldMessageContext = (MessageContext) MessageContext.currentMessageContext.get();
        final ClassLoader contextClassLoader = getContextClassLoader_doPriv();
        tc.oldClassLoader = contextClassLoader;
        String serviceTCCL = (String) service.getParameterValue(Constants.SERVICE_TCCL);
        if (serviceTCCL != null) {
            serviceTCCL = serviceTCCL.trim().toLowerCase();

            if (serviceTCCL.equals(Constants.TCCL_COMPOSITE)) {
                final ClassLoader loader = (ClassLoader) AccessController
                        .doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return new MultiParentClassLoader(new URL[] {}, new ClassLoader[] {
                                        service.getClassLoader(), contextClassLoader });
                            }
                        });
                org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                Thread.currentThread().setContextClassLoader(loader);
                                return null;
                            }
                        });
            } else if (serviceTCCL.equals(Constants.TCCL_SERVICE)) {
                org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                Thread.currentThread().setContextClassLoader(
                                        service.getClassLoader());
                                return null;
                            }
                        });
            }
        }
        return tc;
    }

    /**
     * Several pieces of information need to be available to the service
     * implementation class. For one, the ThreadContextClassLoader needs to be
     * correct, and for another we need to give the service code access to the
     * MessageContext (getCurrentContext()). So we toss these things in TLS.
     * 
     * @param msgContext
     *            the current MessageContext
     * @return a ThreadContextDescriptor containing the old values
     */
    public static ThreadContextDescriptor setThreadContext(final MessageContext msgContext) {
        AxisService service = msgContext.getAxisService();
        ThreadContextDescriptor tc = setThreadContext(service);
        MessageContext.setCurrentMessageContext(msgContext);
        return tc;
    }

    private static ClassLoader getContextClassLoader_doPriv() {
        return (ClassLoader) org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
    }

}
