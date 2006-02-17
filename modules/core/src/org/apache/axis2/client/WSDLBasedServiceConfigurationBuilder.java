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

package org.apache.axis2.client;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.SOAPAddress;
import org.apache.wsdl.extensions.SOAPOperation;

public class WSDLBasedServiceConfigurationBuilder {
	private ConfigurationContext configurationContext;

	private WSDLDescription description;

	public WSDLBasedServiceConfigurationBuilder(WSDLDescription wsdlDesc,
			ConfigurationContext configctx) {
		description = wsdlDesc;
		configurationContext = configctx;
	}

	public AxisService buildAxisService(QName servicename, String endpointname,Options clientOptions)throws AxisFault {
		WSDLService service = findService(servicename);
		AxisService serviceDesc = new AxisService();
		
		//TODO we just pick first end point
		WSDLEndpoint endpoint = findEndpoint(null, service);
		
		Iterator elements = endpoint.getExtensibilityElements().iterator();

		while (elements.hasNext()) {
			Object obj = elements.next();
			if (obj instanceof SOAPAddress) {
				SOAPAddress soapAddress = (SOAPAddress) obj;
				clientOptions.setTo(new EndpointReference(soapAddress.getLocationURI()));
			}
		}

		WSDLBinding binding = endpoint.getBinding();

		// let us configure the complete AxisService out of this, not the
		// current the Operation only
		Iterator bindings = binding.getBindingOperations().values().iterator();

		while (bindings.hasNext()) {
			WSDLBindingOperation wsdlbop = (WSDLBindingOperation) bindings
					.next();
			serviceDesc.addOperation(configureOperation(wsdlbop));
		}

		return serviceDesc;
	}
	
	
	private AxisOperation configureOperation(WSDLBindingOperation bindingOperation) throws AxisFault{
		WSDLOperation wsdlop = bindingOperation.getOperation();
		AxisOperation axisOp = AxisOperationFactory.getAxisOperation(findMEP(wsdlop));

		axisOp.setName(wsdlop.getName());
		
		
		
		
		Iterator elments = bindingOperation.getExtensibilityElements().iterator();

		while (elments.hasNext()) {
			Object obj = elments.next();

			if (obj instanceof SOAPOperation) {
				SOAPOperation soapOp = (SOAPOperation) obj;
				//TODO put soap action to right place
				//axisOp.setSoapAction(soapOp.getSoapAction());
				break;
			}
			
			//TODO set style 
		}
		return axisOp;	
	}
	

	private WSDLEndpoint findEndpoint(QName endpointname, WSDLService service)
			throws AxisFault {
		WSDLEndpoint endpoint;

		if (endpointname == null) {
			Iterator endpoints = service.getEndpoints().values().iterator();

			if (endpoints.hasNext()) {
				endpoint = (WSDLEndpoint) endpoints.next();
			} else {
				throw new AxisFault("No Endpoint Found in Service, "
						+ service.getName());
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
		WSDLService service;

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
}
