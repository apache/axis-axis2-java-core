/*
 * Copyright 2004,2005 The Apache Software Foundation.
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


package org.apache.axis2.jaxws.description;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.xml.handler.HandlerChainsType;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.handler.PortInfo;
import javax.xml.ws.soap.SOAPBinding;

/**
 * An EndpointDescription corresponds to a particular Service Implementation. It can correspond to
 * either either a client to that impl or the actual service impl.
 * <p/>
 * The EndpointDescription contains information that is relevant to both a Provider-based and
 * SEI-based (aka Endpoint-based or Java-based) enpdoints. SEI-based endpoints (whether they have an
 * explicit or implcit SEI) will have addtional metadata information in an
 * EndpointInterfaceDescription class and sub-hierachy; Provider-based endpoitns do not have such a
 * hierachy.
 * <p/>
 * <pre>
 * <b>EndpointDescription details</b>
 * <p/>
 *     CORRESPONDS TO:      The endpoint (both Client and Server)
 * <p/>
 *     AXIS2 DELEGATE:      AxisService
 * <p/>
 *     CHILDREN:            0..1 EndpointInterfaceDescription
 * <p/>
 *     ANNOTATIONS:
 *         WebService [181]
 *         WebServiceProvider [224]
 *             ServicMode [224]
 *         BindingType [224]
 * <p/>
 *     WSDL ELEMENTS:
 *         port
 * <p/>
 *  </pre>
 */

public interface EndpointDescription {

    public static final String AXIS_SERVICE_PARAMETER =
            "org.apache.axis2.jaxws.description.EndpointDescription";
    public static final String DEFAULT_CLIENT_BINDING_ID = SOAPBinding.SOAP11HTTP_BINDING;

    public abstract AxisService getAxisService();

    public abstract ServiceClient getServiceClient();

    public abstract ServiceDescription getServiceDescription();

    public abstract EndpointInterfaceDescription getEndpointInterfaceDescription();

    /**
     * Returns the JAX-WS handler PortInfo object for this endpoint.
     *
     * @return PortInfo
     */
    public abstract PortInfo getPortInfo();

    public abstract boolean isProviderBased();

    public abstract boolean isEndpointBased();

    public abstract String getName();

    public abstract String getTargetNamespace();

    /**
     * Returns the binding type FOR A SERVER.  This is based on the BindingType annotation and/or
     * the WSDL. This will return the default binding (SOAP11) if no annotation was specified on the
     * server. This should NOT be called on the client since it will always return the default
     * binding. Use getClientBindingID() on clients.
     *
     * @return
     */
    public abstract String getBindingType();

    public abstract HandlerChainsType getHandlerChain();

    /**
     * Set the binding type FOR A CLIENT.  The BindingType annotation is not valid on the client per
     * the JAX-WS spec.  The value can be set via addPort(...) for a Dispatch client or via TBD for
     * a Proxy client.
     */
    public abstract void setClientBindingID(String clientBindingID);

    /**
     * Return the binding type FOR A CLIENT.  This will return the default client binding type if
     * called on the server.  Use getBindingType() on servers.
     *
     * @return String representing the client binding type
     * @see setClientBindingID();
     */
    public abstract String getClientBindingID();

    public void setEndpointAddress(String endpointAddress);

    public abstract String getEndpointAddress();

    //public abstract List<String> getHandlerList();
    public abstract QName getPortQName();

    public abstract QName getServiceQName();

    public abstract Service.Mode getServiceMode();
}