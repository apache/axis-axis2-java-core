package org.apache.axis2.jaxws.description;

import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;

/**
 * A ServiceDescription corresponds to a Service under which there can be a
 * collection of enpdoints. In WSDL 1.1 terms, then, a ServiceDescription
 * corresponds to a wsdl:Service under which there are one or more wsdl:Port
 * entries. The ServiceDescription is the root of the metdata abstraction
 * Description hierachy.
 * 
 * The Description hierachy is:
 * <pre>
 * ServiceDescription
 *     EndpointDescription[]
 *         EndpointInterfaceDescription
 *             OperationDescription[]
 *                 ParameterDescription[]
 *                 FaultDescription[]
 *
 * <b>ServiceDescription details</b>
 * 
 *     CORRESPONDS TO:      
 *         On the Client: The JAX-WS Service class or generated subclass.
 *         
 *         On the Server: The Service implementation.  Note that there is a 1..1 
 *         correspondence between a ServiceDescription and EndpointDescription 
 *         on the server side.
 *        
 *     AXIS2 DELEGATE:      None
 *     
 *     CHILDREN:            1..n EndpointDescription
 *     
 *     ANNOTATIONS:
 *         None
 *     
 *     WSDL ELEMENTS:
 *         service
 *         
 *  </pre>       
 */

public interface ServiceDescription {
    public abstract EndpointDescription[] getEndpointDescriptions();
    
    public abstract Collection<EndpointDescription> getEndpointDescriptions_AsCollection();

    public abstract EndpointDescription getEndpointDescription(QName portQName);

    /**
     * Return the EndpointDescriptions corresponding to the SEI class.  Note that
     * Dispatch endpoints will never be returned because they do not have an associated SEI.
     * @param seiClass
     * @return
     */
    public abstract EndpointDescription[] getEndpointDescription(Class seiClass);

    public abstract ConfigurationContext getAxisConfigContext();

    public abstract ServiceClient getServiceClient(QName portQName);

    public abstract QName getServiceQName();
    
    /**
     * Returns a list of the ports for this serivce.  The ports returned are the
     * - Ports declared ports for this Service.  They can be delcared in the WSDL
     *   or via annotations.
     * - Dynamic ports added to the service
     * @return
     */
    public List<QName> getPorts();
    
    public ServiceRuntimeDescription getServiceRuntimeDesc(String name);
    public void setServiceRuntimeDesc(ServiceRuntimeDescription ord);

}