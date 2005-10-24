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

package org.apache.axis2.clientapi;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.description.OutInOperationDescription;
import org.apache.axis2.description.OperationDescriptionFactory;
import org.apache.axis2.om.OMElement;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 *    Message Sender is the class simmiler to the Call, one that provides much simpler API
 *    to users to work with. 
 */
public class MessageSender extends InOnlyMEPClient {

    protected static OperationDescription operationTemplate;
    /**
     * Service context of the Service this MessageSender handles, compare this with the Call, simpler method.
     * @param service
     */
    public MessageSender(ServiceContext service) {
        super(service);
    }

    public MessageSender() throws AxisFault {
        super(assumeServiceContext(null));
    }

    /**
     * This constrctor is to take repository as aragumnet and build the Configurationcontext using that
     * @param repo repository location
     * @throws AxisFault
     */

    public MessageSender(String repo) throws AxisFault {
        super(assumeServiceContext(repo));
    }
    /**
     * Send the SOAP Message, the actual worker
     * @param opName
     * @param toSend
     * @throws AxisFault
     */
    public void send(String opName, OMElement toSend) throws AxisFault {
        OperationDescription axisOp = serviceContext.getServiceConfig()
                .getOperation(opName);
        if (axisOp == null) {
            //todo I just assumed mep is alwas in-out , this has to improve : Deepal
            axisOp = new OutInOperationDescription(new QName(opName));
            serviceContext.getServiceConfig().addOperation(axisOp);

            axisOp = OperationDescriptionFactory.getOperetionDescription(WSDLConstants.MEP_CONSTANT_IN_ONLY);
            axisOp.setName(new QName(opName));
            axisOp.setRemainingPhasesInFlow(
                    operationTemplate.getRemainingPhasesInFlow());
            axisOp.setPhasesOutFlow(operationTemplate.getPhasesOutFlow());
            axisOp.setPhasesInFaultFlow(
                    operationTemplate.getPhasesInFaultFlow());
            axisOp.setPhasesOutFaultFlow(
                    operationTemplate.getPhasesOutFaultFlow());
            serviceContext.getServiceConfig().addOperation(axisOp);
        }
        super.send(axisOp, prepareTheSOAPEnvelope(toSend));
    }

    /**
     * create a default service Context if the users are not intersted in the lower levels of control
     * @return
     * @throws AxisFault
     */
    private static ServiceContext assumeServiceContext(String repo) throws AxisFault {
        ConfigurationContext sysContext = null;
        if (ListenerManager.configurationContext == null) {
            ConfigurationContextFactory efac = new ConfigurationContextFactory();
            sysContext = efac.buildClientConfigurationContext(repo);
        } else {
            sysContext = ListenerManager.configurationContext;
        }

        //create new service
        QName assumedServiceName = new QName("AnonymousService");
        ServiceDescription axisService = new ServiceDescription(assumedServiceName);





        //we will assume a Service and operations
//        operationTemplate = new OperationDescription(new QName("TemplateOperation"));
        operationTemplate = new   OutInOperationDescription(new QName("TemplateOperation"));

        PhasesInfo info =((AxisConfigurationImpl)sysContext.getAxisConfiguration()).getPhasesinfo();
        //to set the operation flows
        if(info != null){
            info.setOperationPhases(operationTemplate);
        }
        axisService.addOperation(operationTemplate);
        sysContext.getAxisConfiguration().addService(axisService);
        ServiceGroupContext serviceGroupContext = axisService.getParent().getServiceGroupContext(sysContext);
        return serviceGroupContext.getServiceContext(assumedServiceName.getLocalPart());
    }
    
    public Object get(String key) {
        return serviceContext.getProperty(key);
    }

    public void set(String key, Object value) {
        serviceContext.getEngineContext().setProperty(key, value);
    }
    
}
