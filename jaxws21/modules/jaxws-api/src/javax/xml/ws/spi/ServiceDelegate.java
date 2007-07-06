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
package javax.xml.ws.spi;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.HandlerResolver;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Executor;

public abstract class ServiceDelegate {

    protected ServiceDelegate() {
    }

    public abstract <T> T getPort(QName qname, Class<T> class1);

    public abstract <T> T getPort(Class<T> class1);

    //TODO
    public abstract <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features);
    
    //TODO
    public abstract <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features);
    
    //TODO
    public abstract <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features);
    
    public abstract void addPort(QName qname, String bindingId, String s);

    public abstract <T>Dispatch<T> createDispatch(QName qname, Class<T> class1, Service.Mode mode);

    public abstract Dispatch<Object> createDispatch(QName qname, JAXBContext jaxbcontext, javax.xml.ws.Service.Mode mode);

    //TODO
    public abstract <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Service.Mode mode, WebServiceFeature... features);
    
    //TODO
    public abstract <T> Dispatch<T> createDispatch(EndpointReference endpointReference, Class<T> type, Service.Mode mode, WebServiceFeature... features);
    
    //TODO
    public abstract Dispatch<Object> createDispatch(QName portName, JAXBContext context, Service.Mode mode, WebServiceFeature... features);
    
    //TODO
    public abstract Dispatch<Object> createDispatch(EndpointReference endpointReference, JAXBContext context, Service.Mode mode, WebServiceFeature... features);
    
    public abstract QName getServiceName();

    public abstract Iterator<QName> getPorts();

    public abstract URL getWSDLDocumentLocation();

    public abstract HandlerResolver getHandlerResolver();

    public abstract void setHandlerResolver(HandlerResolver handlerresolver);

    public abstract Executor getExecutor();

    public abstract void setExecutor(Executor executor);
}
