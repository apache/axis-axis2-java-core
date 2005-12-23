/*
*  Copyright 2004,2005 The Apache Software Foundation.
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


package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.SOAPAddress;
import org.apache.wsdl.extensions.SOAPOperation;

import javax.xml.namespace.QName;
import java.net.URL;
import java.util.Iterator;

public class WSDLMEPClientBuilder {
    private boolean isoneway = false;
    private ConfigurationContext configurationContext;
    private WSDLDescription description;

    public WSDLMEPClientBuilder(String clienthome) throws AxisFault {
        try {
            configurationContext =
                    new ConfigurationContextFactory().buildConfigurationContext(clienthome);
        } catch (DeploymentException e) {
            throw new AxisFault(e);
        }
    }

    public MEPClient createMEPClient(String operationname) throws AxisFault {
        return createMEPClient(null, null, operationname);
    }

    public MEPClient createMEPClient(QName endpointname, String operationname) throws AxisFault {
        return createMEPClient(null, endpointname, operationname);
    }

    public MEPClient createMEPClient(QName servicename, QName endpointname, String operationname)
            throws AxisFault {
        if (description == null) {
            throw new AxisFault(
                    "You need to call public void defineDescription(URL wsdlurl before this method)");
        }

        WSDLService service = findService(servicename);
        AxisService serviceDesc = new AxisService();
        WSDLEndpoint endpoint = findEndpoint(endpointname, service);
        EndpointReference toepr = null;
        Options op = new Options();
        Iterator elements = endpoint.getExtensibilityElements().iterator();

        while (elements.hasNext()) {
            Object obj = elements.next();

            System.out.println("Extension = " + obj);

            if (obj instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) obj;

                System.out.println(soapAddress.getLocationURI());
                toepr = new EndpointReference(soapAddress.getLocationURI());
            }
        }

        if (toepr != null) {
            op.setTo(toepr);
        } else {
            throw new AxisFault("To Address not found");
        }

        WSDLBinding binding = endpoint.getBinding();

        // let us configure the complete AxisService out of this, not the current the Operation only
        Iterator bindings = binding.getBindingOperations().values().iterator();

        while (bindings.hasNext()) {
            WSDLBindingOperation wsdlbop = (WSDLBindingOperation) bindings.next();
            WSDLOperation wsdlop = wsdlbop.getOperation();
            AxisOperation axisOp = AxisOperationFactory.getAxisOperation(findMEP(wsdlop));

            axisOp.setName(wsdlop.getName());
            serviceDesc.addOperation(axisOp);
        }

        // TODO: This part is compelte mess .. I think we need to look closly at the ServiceGroups  ..time been this works
        configurationContext.getAxisConfiguration().addService(serviceDesc);

        AxisServiceGroup serviceGroup =
                new AxisServiceGroup(configurationContext.getAxisConfiguration());
        ServiceGroupContext serviceGroupContext = new ServiceGroupContext(configurationContext,
                serviceGroup);
        ServiceContext serviceContext = new ServiceContext(serviceDesc, serviceGroupContext);
        WSDLOperation wsdlop = getOperation(operationname, endpoint);
        WSDLBindingOperation bop = binding.getBindingOperation(wsdlop.getName());
        Iterator elments = bop.getExtensibilityElements().iterator();

        while (elments.hasNext()) {
            Object obj = elments.next();

            if (obj instanceof SOAPOperation) {
                SOAPOperation soapOp = (SOAPOperation) obj;

                op.setSoapAction(soapOp.getSoapAction());

                break;
            }
        }

        MEPClient mepclient = null;

        if ((wsdlop.getInputMessage() != null) && (wsdlop.getOutputMessage() != null) && !isoneway) {
            mepclient = new InOutMEPClient(serviceContext);
        } else if ((wsdlop.getInputMessage() != null) || isoneway) {
            mepclient = new InOnlyMEPClient(serviceContext);
        } else {
            throw new AxisFault("Unknown MEP");
        }

        mepclient.setClientOptions(op);

        return mepclient;
    }

    public void defineDescription(URL wsdlurl) throws AxisFault {
        try {
            WOMBuilder buider = WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1);
            WSDLVersionWrapper vw = buider.build(wsdlurl.openStream());

            description = vw.getDescription();
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private WSDLEndpoint findEndpoint(QName endpointname, WSDLService service) throws AxisFault {
        WSDLEndpoint endpoint = null;

        if (endpointname == null) {
            Iterator endpoints = service.getEndpoints().values().iterator();

            if (endpoints.hasNext()) {
                endpoint = (WSDLEndpoint) endpoints.next();
            } else {
                throw new AxisFault("No Endpoint Found in Service, " + service.getName());
            }
        } else {
            endpoint = service.getEndpoint(endpointname);
        }

        if (endpoint == null) {
            throw new AxisFault("Endpoint Not found");
        }

        return endpoint;
    }

    private int findMEP(WSDLOperation wsdlOp) throws AxisFault {
        if (wsdlOp.getInputMessage() == null) {
            throw new AxisFault("Unsupported MEP");
        }

        if (wsdlOp.getOutputMessage() == null) {
            return WSDLConstants.MEP_CONSTANT_IN_ONLY;
        } else {
            return WSDLConstants.MEP_CONSTANT_IN_OUT;
        }
    }

    private WSDLService findService(QName serviceName) throws AxisFault {
        WSDLService service = null;

        if (serviceName == null) {
            Iterator services = description.getServices().values().iterator();

            if (services.hasNext()) {
                service = (WSDLService) services.next();
            } else {
                throw new AxisFault("No service found");
            }
        } else {
            service = description.getService(serviceName);
        }

        if (service == null) {
            throw new AxisFault("No service found");
        }

        return service;
    }

    public WSDLDescription getDescription() {
        return description;
    }

    private WSDLOperation getOperation(String operation, WSDLEndpoint endpoint) throws AxisFault {
        WSDLInterface wsdlinterface = endpoint.getBinding().getBoundInterface();
        Iterator operations = wsdlinterface.getAllOperations().values().iterator();

        while (operations.hasNext()) {
            WSDLOperation wsdlOp = (WSDLOperation) operations.next();

            if (wsdlOp.getName().getLocalPart().equals(operation)) {
                return wsdlOp;
            }
        }

        throw new AxisFault("Operation Not found");
    }

    public void setIsoneway(boolean isoneway) {
        this.isoneway = isoneway;
    }
}
