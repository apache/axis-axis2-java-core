package org.apache.axis2.jaxws.description;

import org.apache.axis2.jaxws.util.WSDLWrapper;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ServiceDescriptionWSDL {
    // TODO: Remove these and replace with appropraite get* methods for WSDL information
    public abstract WSDLWrapper getWSDLWrapper();

    public abstract WSDLWrapper getGeneratedWsdlWrapper();

    public Service getWSDLService();

    public Map getWSDLPorts();

    /**
     * Return a collection of WSDL ports under this service which use the portType QName.
     *
     * @param portTypeQN
     * @return
     */
    public List<Port> getWSDLPortsUsingPortType(QName portTypeQN);

    /**
     * Return a subset of the collection of WSDL ports which specify a SOAP 1.1 or 1.2 address.
     *
     * @param wsdlPorts
     * @return
     */
    public List<Port> getWSDLPortsUsingSOAPAddress(List<Port> wsdlPorts);

    public abstract URL getWSDLLocation();

    public Definition getWSDLDefinition();

    public boolean isWSDLSpecified();

}