package org.apache.axis2.context;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.AxisOperation;

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
public class ContextFactory {

    public static MessageContext createMessageContext(
            ConfigurationContext configurationContext) {
        MessageContext msgContext = new MessageContext();
        msgContext.setConfigurationContext(configurationContext);
        return msgContext;
    }

    public static ServiceGroupContext createServiceGroupContext(
            ConfigurationContext configurationContext,
            AxisServiceGroup serviceGroup) {
        return new ServiceGroupContext(configurationContext, serviceGroup);
    }

    public static ServiceContext createServiceContext(ServiceGroupContext serviceGroupContext,
                                                      AxisService service) throws AxisFault {
        return serviceGroupContext.getServiceContext(service);
    }
    public static OperationContext createOperationContext(AxisOperation axisOperation,
                                                          ServiceContext serviceContext){
        return new OperationContext(axisOperation,serviceContext);
    }
}
