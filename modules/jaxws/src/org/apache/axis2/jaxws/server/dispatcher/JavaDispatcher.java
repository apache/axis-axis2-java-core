/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.server.dispatcher;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JavaDispatcher is an abstract class that can be extended to implement
 * an EndpointDispatcher to a Java object.  
 */
public abstract class JavaDispatcher implements EndpointDispatcher {

    private static final Log log = LogFactory.getLog(JavaDispatcher.class);
    
    protected Class serviceImplClass;
    protected Object serviceInstance;
    
    protected JavaDispatcher(Class impl, Object serviceInstance){
    	this.serviceImplClass = impl;
    	this.serviceInstance = serviceInstance;
    }
    
    public abstract MessageContext invoke(MessageContext mc)
        throws Exception;
    
    public Class getServiceImplementationClass() {
        return serviceImplClass;
    }
    
    protected Object createServiceInstance() {
        if (log.isDebugEnabled()) {
            log.debug("Creating new instance of service endpoint");
        }
        
        if (serviceImplClass == null) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "JavaDispErr1"));
        }
        
        Object instance = null;
        try {
            instance = serviceImplClass.newInstance();
        } catch (IllegalAccessException e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "JavaDispErr2", serviceImplClass.getName()));
        } catch (InstantiationException e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage(
                    "JavaDispErr2", serviceImplClass.getName()));
        }
        
        return instance;
    }

}
