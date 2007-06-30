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
package org.apache.axis2.jaxws.description;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;

/**
 * A ServiceDescription corresponds to a Service under which there can be a collection of enpdoints.
 * In WSDL 1.1 terms, then, a ServiceDescription corresponds to a wsdl:Service under which there are
 * one or more wsdl:Port entries. The ServiceDescription is the root of the metdata abstraction
 * Description hierachy.
 * <p/>
 * The Description hierachy is:
 * <pre>
 * ServiceDescription
 *     EndpointDescription[]
 *         EndpointInterfaceDescription
 *             OperationDescription[]
 *                 ParameterDescription[]
 *                 FaultDescription[]
 * <p/>
 * <b>ServiceDescription details</b>
 * <p/>
 *     CORRESPONDS TO:
 *         On the Client: The JAX-WS Service class or generated subclass.
 * <p/>
 *         On the Server: The Service implementation.  Note that there is a 1..1
 *         correspondence between a ServiceDescription and EndpointDescription
 *         on the server side.
 * <p/>
 *     AXIS2 DELEGATE:      None
 * <p/>
 *     CHILDREN:            1..n EndpointDescription
 * <p/>
 *     ANNOTATIONS:
 *         None
 * <p/>
 *     WSDL ELEMENTS:
 *         service
 * <p/>
 *  </pre>
 */

public interface ServiceDescription {
    public abstract EndpointDescription[] getEndpointDescriptions();

    public abstract Collection<EndpointDescription> getEndpointDescriptions_AsCollection();

    public abstract EndpointDescription getEndpointDescription(QName portQName);

    /**
     * Return the EndpointDescriptions corresponding to the SEI class.  Note that Dispatch endpoints
     * will never be returned because they do not have an associated SEI.
     *
     * @param seiClass
     * @return
     */
    public abstract EndpointDescription[] getEndpointDescription(Class seiClass);

    public abstract ConfigurationContext getAxisConfigContext();

    public abstract ServiceClient getServiceClient(QName portQName);

    public abstract QName getServiceQName();

    /**
     * Returns a list of the ports for this serivce.  The ports returned are the - Ports declared
     * ports for this Service.  They can be delcared in the WSDL or via annotations. - Dynamic ports
     * added to the service
     *
     * @return
     */
    public List<QName> getPorts();

    public ServiceRuntimeDescription getServiceRuntimeDesc(String name);

    public void setServiceRuntimeDesc(ServiceRuntimeDescription ord);
    
    public boolean isServerSide();

}