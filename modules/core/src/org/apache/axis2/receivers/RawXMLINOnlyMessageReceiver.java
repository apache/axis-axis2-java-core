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


package org.apache.axis2.receivers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;

/**
 * This is a Simple java Provider.
 */
public class RawXMLINOnlyMessageReceiver extends AbstractInMessageReceiver
        implements MessageReceiver {

    /**
     * Field log
     */
    protected Log log = LogFactory.getLog(getClass());

    /**
     * Field scope
     */

    /**
     * Field method
     */
    private Method method;

    /**
     * Field classLoader
     */

    /**
     * Constructor RawXMLProvider
     */
    public RawXMLINOnlyMessageReceiver() {
    }

    public void invokeBusinessLogic(MessageContext msgContext) throws AxisFault {
        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            // find the WebService method
            Class ImplClass = obj.getClass();

            DependencyManager.configureBusinessLogicProvider(obj,
                    msgContext.getOperationContext());

            AxisOperation op = msgContext.getOperationContext().getAxisOperation();

            if (op == null) {
                throw new AxisFault(
                        "Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }

            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];

                    break;
                }
            }

            Class[] parameters = method.getParameterTypes();

            if ((parameters != null) && (parameters.length == 1)
                    && OMElement.class.getName().equals(parameters[0].getName())) {
                OMElement methodElement = msgContext.getEnvelope().getBody().getFirstElement();
                OMElement parmeter;
                parmeter = methodElement;
                Object[] parms = new Object[]{parmeter};
                // Need not have a return here
                try {
                    method.invoke(obj, parms);
                } catch (Exception e) {
                    throw new AxisFault(e.getMessage());
                }

            } else {
                throw new AxisFault(Messages.getMessage("rawXmlProivdeIsLimited"));
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
