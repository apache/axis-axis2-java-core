package org.apache.axis2.jaxws.addressing.factory;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.EndpointReference;

/**
 * This class represents factories that can be use to create instances of
 * {@link EndpointReference} that can ultimately be converted into instances
 * of {@link javax.xml.ws.EndpointReference} that are suitable to be returned
 * via the appropriate JAX-WS 2.1 API methods.
 * 
 */
public interface Axis2EndpointReferenceFactory {
    /**
     * Create an instance of <code>EndpointReference</code> with the specified address.
     * 
     * @param address the address URI to use. It cannot be null.
     * @return an instance of <code>EndpointReference</code>.
     */
    public EndpointReference createEndpointReference(String address);
    
    /**
     * Create an instance of <code>EndpointReference</code> that targets the endpoint
     * identified by the specified WSDL service name and endpoint name.
     * 
     * @param serviceName the WSDL service name
     * @param endpoint the WSDL port name
     * @return an instance of <code>EndpointReference</code> that targets the specified
     * endpoint
     */
    public EndpointReference createEndpointReference(QName serviceName, QName endpoint);
    
    /**
     * Create an instance of <code>EndpointReference</code>. If the address is specified
     * then it will be used. If the address is null, but the WSDL service name and port
     * name are specified then they will be used to target the specified endpoint. Either
     * the address URI, or the WSDL service name and port name must be specified.
     * 
     * @param address the address URI to use, if specified
     * @param serviceName the WSDL service name, if specified
     * @param portName the WSDL port name, if specified
     * @param wsdlDocumentLocation the URI from where the WSDL for the endpoint can be
     * retrieved, if specified.
     * @param addressingNamespace the intended WS-Addressing namespace that the <code>
     * EndpointRefence</code> should comply with.
     * @return an instance of <code>EndpointReference</code>.
     */
    public EndpointReference createEndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace);
}