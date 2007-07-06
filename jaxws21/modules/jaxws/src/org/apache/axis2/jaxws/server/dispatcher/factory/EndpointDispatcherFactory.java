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
package org.apache.axis2.jaxws.server.dispatcher.factory;

import org.apache.axis2.jaxws.server.dispatcher.EndpointDispatcher;
import org.apache.axis2.jaxws.server.dispatcher.JavaBeanDispatcher;
import org.apache.axis2.jaxws.server.dispatcher.ProviderDispatcher;

import javax.xml.ws.Provider;

public class EndpointDispatcherFactory {

    public EndpointDispatcherFactory() {
        super();
    }

    public EndpointDispatcher createEndpointDispatcher(Class serviceImplClass,
                                                       Object serviceInstance) {
        //		 TODO:  This check should be based on the EndpointDescription processing of annotations
        //        It is left this way for now because some tests have an @WebService annotation on
        //        Provider-based endpoints as a pre-existing workaround.
        if (Provider.class.isAssignableFrom(serviceImplClass)) {
            return new ProviderDispatcher(serviceImplClass, serviceInstance);
        } else {
            return new JavaBeanDispatcher(serviceImplClass, serviceInstance);
        }
    }

}
