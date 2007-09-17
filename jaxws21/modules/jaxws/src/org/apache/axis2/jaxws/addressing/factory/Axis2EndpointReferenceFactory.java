package org.apache.axis2.jaxws.addressing.factory;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;

public interface Axis2EndpointReferenceFactory {
    public EndpointReference createEndpointReference(String address);
    
    public EndpointReference createEndpointReference(QName serviceName, QName endpoint);
    
    public EndpointReference createEndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace);
}