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
package javax.xml.ws;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Executor;

public class Service {
    public enum Mode {
        MESSAGE, PAYLOAD }

    protected Service(URL wsdlDocumentLocation, QName serviceName) {
        _delegate = Provider.provider().createServiceDelegate(wsdlDocumentLocation, serviceName, getClass());
    }

    public <T> T getPort(QName portName, Class<T> serviceEndpointInterface) {
        return (T) _delegate.getPort(portName, serviceEndpointInterface);
    }

    public <T> T getPort(Class<T> serviceEndpointInterface) {
        return (T) _delegate.getPort(serviceEndpointInterface);
    }

    public void addPort(QName portName, String bindingId, String endpointAddress) {
        _delegate.addPort(portName, bindingId, endpointAddress);
    }

    public <T>Dispatch<T> createDispatch(QName portName, Class<T> type, Mode mode) {
        return _delegate.createDispatch(portName, type, mode);
    }

    public Dispatch<java.lang.Object> createDispatch(QName portName, JAXBContext context, Mode mode) {
        return _delegate.createDispatch(portName, context, mode);
    }

    public QName getServiceName() {
        return _delegate.getServiceName();
    }

    public Iterator<javax.xml.namespace.QName> getPorts() {
        return _delegate.getPorts();
    }

    public URL getWSDLDocumentLocation() {
        return _delegate.getWSDLDocumentLocation();
    }

    public HandlerResolver getHandlerResolver() {
        return _delegate.getHandlerResolver();
    }

    public void setHandlerResolver(HandlerResolver handlerResolver) {
        _delegate.setHandlerResolver(handlerResolver);
    }

    public Executor getExecutor() {
        return _delegate.getExecutor();
    }

    public void setExecutor(Executor executor) {
        _delegate.setExecutor(executor);
    }

    public static Service create(URL wsdlDocumentLocation, QName serviceName) {
        return new Service(wsdlDocumentLocation, serviceName);
    }

    public static Service create(QName serviceName) {
        return new Service(null, serviceName);
    }

    private ServiceDelegate _delegate;
}
