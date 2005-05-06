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

package org.apache.axis.receivers;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.description.AxisService;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;

public abstract class AbstractMessageReceiver implements MessageReceiver {
    public static final String SERVICE_CLASS = "ServiceClass";
    public static final String SCOPE = "scope";

    /**
     * Method makeNewServiceObject
     *
     * @param msgContext
     * @return
     * @throws AxisFault
     */
    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        try {
            AxisService service =
                msgContext.getOperationContext().getServiceContext().getServiceConfig();
            ClassLoader classLoader = service.getClassLoader();
            Parameter implInfoParam = service.getParameter(SERVICE_CLASS);
            if (implInfoParam != null) {
                Class implClass =
                    Class.forName((String) implInfoParam.getValue(), true, classLoader);
                return implClass.newInstance();
            } else {
                throw new AxisFault("SERVICE_CLASS parameter is not specified");
            }

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
      * Method getTheImplementationObject
      *
      * @param msgContext
      * @return
      * @throws AxisFault
      */
    protected Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        AxisService service =
            msgContext.getOperationContext().getServiceContext().getServiceConfig();

        Parameter scopeParam = service.getParameter(SCOPE);
        String scope = Constants.MESSAGE_SCOPE;
        QName serviceName = service.getName();
        if (Constants.MESSAGE_SCOPE.equals(scope)) {
            return makeNewServiceObject(msgContext);
        } else if (Constants.SESSION_SCOPE.equals(scope)) {
            SessionContext sessionContext = msgContext.getSessionContext();
            Object obj = sessionContext.getProperty(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                sessionContext.setProperty(serviceName, obj);
            }
            return obj;
        } else if (Constants.APPLICATION_SCOPE.equals(scope)) {
            SessionContext globalContext = msgContext.getSessionContext();
            Object obj = globalContext.getProperty(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                globalContext.setProperty(serviceName, obj);
            }
            return obj;
        } else {
            throw new AxisFault("unknown scope " + scope);
        }
    }
}
