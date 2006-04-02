package org.apache.axis2.rpc.receivers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.receivers.AbstractInMessageReceiver;
import org.apache.ws.commons.om.OMElement;

import java.lang.reflect.Method;
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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class RPCInOnlyMessageReceiver extends AbstractInMessageReceiver {

    private Method method;

    public void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
        try {
            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();
            DependencyManager.configureBusinessLogicProvider(obj, inMessage, null);

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();

            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();

            if (!service.getSchematargetNamespace().equals(methodElement.getNamespace().getName())) {
                throw new AxisFault("namespace mismatch require " +
                        service.getSchematargetNamespace() +
                        " found " + methodElement.getNamespace().getName());
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }


            Object[] objectArray = processRequest(methodElement);
            method.invoke(obj, objectArray);
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private Object[] processRequest(OMElement methodElement) throws AxisFault {
        Class[] parameters = method.getParameterTypes();
        return BeanUtil.deserialize(methodElement, parameters);
    }
}
