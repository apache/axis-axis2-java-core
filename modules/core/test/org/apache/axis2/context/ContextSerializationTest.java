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

package org.apache.axis2.context;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.description.InOutOperationDescrition;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This TestCase check weather the context serialization and deserialization happens correctly.
 */
public class ContextSerializationTest extends TestCase {

	final String SERVICE_NAME = "service1";
	final String OPERATION_NAME = "operation1";
	final String SERVICE_GROUP_NAME = "serviceGroupName";
	final String SERVICE_GROUP_CONTEXT_ID = "serviceGroupCtxId";
	final String MSG1_ID = "msgid1";
	final String MSG2_ID = "msgid2";
	
	File file = new File ("target/tempfile.tmp");
	
	AxisConfiguration axisConfiguration;
	ServiceGroupDescription serviceGroupDescription;
	ServiceDescription serviceDescription;
	OperationDescription operationDescription;
	
	QName serviceDescQName = new QName (SERVICE_NAME);
	QName operationDescName = new QName (OPERATION_NAME);
	
	
	protected void setUp() throws Exception {
		//Initializing descriptions
		axisConfiguration = new AxisConfigurationImpl ();
		serviceGroupDescription = new ServiceGroupDescription (axisConfiguration);
		serviceGroupDescription.setServiceGroupName(SERVICE_GROUP_NAME);
		serviceDescription = new ServiceDescription (serviceDescQName);
		operationDescription = new InOutOperationDescrition (operationDescName);
		
		//Creating links
		axisConfiguration.addServiceGroup(serviceGroupDescription);
		axisConfiguration.addService(serviceDescription);
		serviceGroupDescription.addService(serviceDescription);
		serviceDescription.addOperation(operationDescription);
		
		if (file.exists()) {
			file.delete();
		}
		
		//creating a temp file to serializeAndConsume
		File dir = new File ("target");
		if (!dir.isDirectory())
			throw new AxisFault ("Target diractory is not found");
		
		file.createNewFile();
	}
	
	public void testSerialization ()throws AxisFault,IOException, ClassNotFoundException{
		
		//Setting contexts.
		ConfigurationContext configurationContext = new ConfigurationContext(axisConfiguration);
        ServiceGroupContext serviceGroupContext = serviceDescription.getParent().getServiceGroupContext(configurationContext);
        serviceGroupContext.setId(SERVICE_GROUP_CONTEXT_ID);
        configurationContext.registerServiceGroupContext(serviceGroupContext);
        ServiceContext serviceContext = serviceGroupContext.getServiceContext(serviceDescription.getName().getLocalPart());
        
        //setting message contexts
        MessageContext inMessage = new MessageContext(configurationContext);
        MessageContext outMessage = new MessageContext(configurationContext);
        inMessage.setMessageID(MSG1_ID);
        outMessage.setMessageID(MSG2_ID);
        outMessage.setRelatesTo(new RelatesTo (MSG1_ID));
        inMessage.setServiceGroupContextId(SERVICE_GROUP_CONTEXT_ID);
        outMessage.setServiceGroupContextId(SERVICE_GROUP_CONTEXT_ID);
        inMessage.setServiceGroupContext(serviceGroupContext);
        outMessage.setServiceGroupContext(serviceGroupContext);
        inMessage.setServiceContext(serviceContext);
        outMessage.setServiceContext(serviceContext);
        inMessage.setOperationDescription(operationDescription);
        outMessage.setOperationDescription(operationDescription);
       
        OperationContext operationContext = operationDescription.findOperationContext(inMessage,serviceContext);
        operationContext.addMessageContext(outMessage);
        outMessage.setOperationContext(operationContext);
    
        configurationContext.registerOperationContext(inMessage.getMessageID(),operationContext);
        configurationContext.registerOperationContext(outMessage.getMessageID(),operationContext);
      
        
        //serializing
		ObjectOutputStream out = new ObjectOutputStream (
				new FileOutputStream (file));
		
		if (configurationContext==null) 
			throw new AxisFault ("Configuration Context is null");
		
		out.writeObject(configurationContext);
		out.close();
		
		
		
		//deserializing
		ObjectInputStream in = new ObjectInputStream (
				new FileInputStream (file));
		
		Object obj = in.readObject();
		if (!(obj instanceof ConfigurationContext))
			throw new AxisFault ("Invalid read");
		
		configurationContext = null;
		configurationContext = (ConfigurationContext) obj;
		
		//calling 'init' to set descriptions
		configurationContext.init(axisConfiguration);
		
		
		//Assertions to check weather context hierarchy is set correctly.
		assertFalse(configurationContext.getOperationContextMap().isEmpty());
		
		ServiceGroupContext serviceGroupcontext1 = configurationContext.fillServiceContextAndServiceGroupContext(inMessage);
		assertNotNull (serviceGroupcontext1);
		
		serviceGroupcontext1 = configurationContext.fillServiceContextAndServiceGroupContext(outMessage);
		assertNotNull (serviceGroupcontext1);
		
		ServiceContext serviceContext1 = serviceGroupContext.getServiceContext(SERVICE_NAME);
		assertNotNull(serviceContext1);
		
		OperationContext operationContext1 = configurationContext.getOperationContext(MSG1_ID);
		assertNotNull(operationContext1);
		
		assertNotNull(operationContext1.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE));
		assertNotNull(operationContext1.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT_VALUE));
		
		
		//Assertions to check weather description hierarchy is set correctly.
		AxisConfiguration axisConfiguration1 = configurationContext.getAxisConfiguration();
		assertNotNull(axisConfiguration1);
		
		assertNotNull(operationContext1.getOperationDescription());
		assertNotNull(serviceGroupcontext1.getDescription());
		assertNotNull(serviceContext1.getServiceConfig());
			
	}
	
	
	protected void tearDown() throws Exception {
		if (file.exists()) 
			file.delete();
	}
	
	
}
