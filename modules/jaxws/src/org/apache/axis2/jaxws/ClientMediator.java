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
package org.apache.axis2.jaxws;

import java.lang.reflect.Proxy;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.jaxws.client.JAXBDispatch;
import org.apache.axis2.jaxws.client.XMLDispatch;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.util.WSDLWrapper;
/*
 * This class acts as a mediator to creating Proxy or Dispatch implementation when Client makes a call to Service.
 * Class creates AxisService, ServiceClient etc stores them in AxisRouter that the Dispatch or Proxy can use
 * to call Axis Engine.
 */
public class ClientMediator {
	private JAXWSClientContext clientContext = null;

	public ClientMediator() {
		super();
	}

    public <T> JAXBDispatch<T> createJAXBDispatch(JAXWSClientContext<T> clientContext){

        if (clientContext == null) {
            throw ExceptionFactory.makeWebServiceException(
                    "Internal Error ... JAXWSClientContext not found");
        }
        this.clientContext = clientContext;
        /*
         * create Axis Controller, this will route all the calls from Dispatch to
         * Axis Engine eiter using ServiceClient or instantiating
         * AxisEngine.
         */
        try{
            AxisController axisController = buildAxisController();
            axisController.setClientContext(clientContext);
            
            JAXBDispatch<T> dispatch = new JAXBDispatch<T>(axisController);
            dispatch.setMode(clientContext.getServiceMode());
            dispatch.setJAXBContext(clientContext.getJAXBContext());
            return dispatch;
        }catch(AxisFault e){
            throw new WebServiceException(e.getMessage());
        }
    }
    
    public <T> XMLDispatch<T> createXMLDispatch(JAXWSClientContext<T> clientContext){

		if (clientContext == null) {
			throw new WebServiceException(
					"Internal Error ... JAXWSClientContext not found");
		}
		this.clientContext = clientContext;
		/*
		 * create Axis Controller, this will route all the calls from Dispatch to
		 * Axis Engine eiter using ServiceClient or instantiating
		 * AxisEngine.
		 */
		try{
			AxisController axisController = buildAxisController();
			axisController.setClientContext(clientContext);
			XMLDispatch<T> dispatch = new XMLDispatch<T>(axisController);
            dispatch.setMode(clientContext.getServiceMode());
			return dispatch;
		}catch(AxisFault e){
			throw new WebServiceException(e.getMessage());
		}
	}

	// Add required parameter to this method.
    public <T> T createProxy(JAXWSClientContext<T> clientContext, ServiceDelegate delegate) {
		//proxy is now create from ServiceDelegate.getport
		return null;
		
	}

	private ConfigurationContext getAxisConfigContext() {
		try {
            ClientConfigurationFactory factory = ClientConfigurationFactory.newInstance(); 
            ConfigurationContext configCtx = factory.getClientConfigurationContext();
            return configCtx;
		} catch (Exception e) {
			throw ExceptionFactory.makeWebServiceException(e);
		}
	}

	private AxisService getAxisService(ServiceClient axisClient) {
		return axisClient.getAxisService();
	}

	/*
     * If a WSDL is present, create an AxisOperation for each of the operations
     * that exist in that WSDL.
	 */
    private AxisOperation getAxisOperation(AxisService service){
		QName portName = clientContext.getPort().getPortName();
		QName serviceName = clientContext.getPort().getServiceName();
		WSDLWrapper wsdlContext = clientContext.getWsdlContext();

        if(wsdlContext!=null){
			String operation = wsdlContext.getOperationName(serviceName, portName);
			return service.getOperation(new QName(operation));
			
		}
		else{
			return service.getOperation(ServiceClient.ANON_OUT_IN_OP);
		}		
	}
	
	private PortData getPortInfo() {
		return clientContext.getPort();
	}

	private QName getPortName() {
		return getPortInfo().getPortName();
	}

	private ServiceClient getServiceClient(ConfigurationContext axisConfig)
			throws AxisFault {
	
		return new ServiceClient(axisConfig, 
                clientContext.getServiceDescription().getAxisService());

	}

	private ServiceContext getServiceContext(AxisService service,
			ServiceGroupContext groupContext) throws AxisFault {
		return groupContext.getServiceContext(service);
	}

	private ServiceGroupContext getServiceGroupContext(
			ConfigurationContext configContext, AxisService service) {
		return new ServiceGroupContext(configContext,
				(AxisServiceGroup) service.getParent());
	}

	private QName getServiceName() {
		return getPortInfo().getServiceName();
	}

	private AxisController buildAxisController() throws AxisFault {
		/* Create AxisController to send calls to AxisEngine
		 * There is one AxisController per Dispatch or Proxy.
		 */
		AxisController controller = new AxisController();
		/* Create all Web Service information from WSDL and store them in Axis API's 
		 * AxisService, AxisOperation etc.
		 * Store these in AxisController. When Dispatch or Proxy receive Request from 
		 * JAX-WS Client AxisController will use them to call AxisEngine and respond to 
		 * Dispatch or Proxy. 
		 */
		ConfigurationContext axisConfig = getAxisConfigContext();
		ServiceClient serviceClient = getServiceClient(axisConfig);
		AxisService axisService = getAxisService(serviceClient);
		ServiceGroupContext groupContext = getServiceGroupContext(axisConfig, axisService);
		ServiceContext serviceContext = getServiceContext(axisService, groupContext);

        controller.setConfigContext(axisConfig);
		controller.setServiceClient(serviceClient);
		controller.setAxisService(axisService);
		controller.setGroupContext(groupContext);
		controller.setServiceContext(serviceContext);
		return controller;
	}
}
