package org.apache.axis2.jaxws.description;

import javax.xml.namespace.QName;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;

public interface ServiceDescription {
    public enum UpdateType {GET_PORT, ADD_PORT, CREATE_DISPATCH}
    
    public abstract EndpointDescription[] getEndpointDescriptions();

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

}