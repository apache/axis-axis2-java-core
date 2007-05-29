/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package javax.xml.ws;

import javax.xml.ws.spi.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public abstract class Endpoint {

    public Endpoint() {
    }

    public static Endpoint create(Object implementor) {
        return create(null, implementor);
    }

    public static Endpoint create(String bindingId, Object implementor) {
        return Provider.provider().createEndpoint(bindingId, implementor);
    }

    public abstract Binding getBinding();

    public abstract Object getImplementor();

    public abstract void publish(String s);

    public static Endpoint publish(String address, Object implementor) {
        return Provider.provider().createAndPublishEndpoint(address, implementor);
    }

    public abstract void publish(Object obj);

    public abstract void stop();

    public abstract boolean isPublished();

    public abstract List<javax.xml.transform.Source> getMetadata();

    public abstract void setMetadata(List<javax.xml.transform.Source> list);

    public abstract Executor getExecutor();

    public abstract void setExecutor(Executor executor);

    public abstract Map<java.lang.String, java.lang.Object> getProperties();

    public abstract void setProperties(Map<java.lang.String, java.lang.Object> map);

    //TODO
    public abstract EndpointReference getEndpointReference(org.w3c.dom.Element... referenceParameters);

    //TODO
    public abstract <T extends EndpointReference> T getEndpointReference(Class<T> clazz, org.w3c.dom.Element... referenceParameters);

    public static final String WSDL_SERVICE = "javax.xml.ws.wsdl.service";
    public static final String WSDL_PORT = "javax.xml.ws.wsdl.port";
}
