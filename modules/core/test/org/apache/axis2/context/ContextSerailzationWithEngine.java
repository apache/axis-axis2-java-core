package org.apache.axis2.context;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.miheaders.RelatesTo;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.ServiceGroupDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Sep 21, 2005
 * Time: 2:17:36 PM
 */
public class ContextSerailzationWithEngine extends TestCase {
    String repo ="./test-resources/deployment/serviceGroupRepo";


    final String SERVICE_NAME = "service1";
    final String OPERATION_NAME = "op1";
    final String SERVICE_GROUP_NAME = "serviceGroup";
    final String SERVICE_GROUP_CONTEXT_ID = "serviceGroupCtxId";
    final String MSG1_ID = "msgid1";
    final String MSG2_ID = "msgid2";

    AxisConfiguration axisConfiguration;
    AxisConfiguration newaxisConfiguration;
    ServiceGroupDescription serviceGroupDescription;
    ServiceDescription serviceDescription;
    OperationDescription operationDescription;

    QName serviceDescQName = new QName (SERVICE_NAME);
    QName operationDescName = new QName (OPERATION_NAME);

    public void testSerialization(){
        try {
            ConfigurationContextFactory builder = new ConfigurationContextFactory();
            ConfigurationContext configurationContext = builder.buildConfigurationContext(repo);
            axisConfiguration =configurationContext.getAxisConfiguration();

            serviceGroupDescription = axisConfiguration.getServiceGroup(SERVICE_GROUP_NAME);
            serviceDescription = axisConfiguration.getService(SERVICE_NAME);
            ServiceGroupContext serviceGroupContext = serviceDescription.getParent().getServiceGroupContext(configurationContext);
            serviceGroupContext.setId(SERVICE_GROUP_CONTEXT_ID);
            configurationContext.registerServiceGroupContext(serviceGroupContext);
            ServiceContext serviceContext = serviceGroupContext.getServiceContext(serviceDescription.getName().getLocalPart());

            operationDescription = serviceDescription.getOperation(operationDescName);
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

            AxisEngine engine = new AxisEngine(configurationContext);
            engine.serialize();

            configurationContext = null;

            /////////////////////////////

            ConfigurationContext newConfigContext = builder.buildConfigurationContext(repo);
            newaxisConfiguration = newConfigContext.getAxisConfiguration();


            assertFalse(newConfigContext.getOperationContextMap().isEmpty());

            ServiceGroupContext serviceGroupcontext1 = newConfigContext.fillServiceContextAndServiceGroupContext(inMessage);
            assertNotNull (serviceGroupcontext1);

            serviceGroupcontext1 = newConfigContext.fillServiceContextAndServiceGroupContext(outMessage);
            assertNotNull (serviceGroupcontext1);

            ServiceContext serviceContext1 = serviceGroupContext.getServiceContext(SERVICE_NAME);
            assertNotNull(serviceContext1);

            OperationContext operationContext1 = newConfigContext.getOperationContext(MSG1_ID);
            assertNotNull(operationContext1);

            assertNotNull(operationContext1.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN));
            assertNotNull(operationContext1.getMessageContext(WSDLConstants.MESSAGE_LABEL_OUT));


            //Assertions to check weather description hierarchy is set correctly.
            AxisConfiguration axisConfiguration1 = newConfigContext.getAxisConfiguration();
            assertNotNull(axisConfiguration1);

            assertNotNull(operationContext1.getAxisOperation());
            assertNotNull(serviceGroupcontext1.getDescription());
            assertNotNull(serviceContext1.getServiceConfig());




        } catch (DeploymentException e) {
            fail("This can not fail by DeploymentException");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail("This can not fail by AxisFault");
        }
    }
}
