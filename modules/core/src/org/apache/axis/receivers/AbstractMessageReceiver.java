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

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.description.Parameter;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;

import javax.xml.namespace.QName;

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
            ServiceDescription service =
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
        ServiceDescription service =
            msgContext.getOperationContext().getServiceContext().getServiceConfig();

        Parameter scopeParam = service.getParameter(SCOPE);
        QName serviceName = service.getName();
        if (scopeParam != null &&  Constants.SESSION_SCOPE.equals(scopeParam.getValue())) {
            SessionContext sessionContext = msgContext.getSessionContext();
            synchronized(sessionContext){
                Object obj = sessionContext.getProperty(serviceName.getLocalPart());
                if (obj == null) {
                    obj = makeNewServiceObject(msgContext);
                    sessionContext.setProperty(serviceName.getLocalPart(), obj);
                }
                return obj;
            }
        } else if (scopeParam != null &&  Constants.APPLICATION_SCOPE.equals(scopeParam.getValue())) {
            SessionContext globalContext = msgContext.getSessionContext();
            synchronized(globalContext){
                Object obj = globalContext.getProperty(serviceName.getLocalPart());
                if (obj == null) {
                    obj = makeNewServiceObject(msgContext);
                    globalContext.setProperty(serviceName.getLocalPart(), obj);
                }
                return obj;
            }
        } else {
            return makeNewServiceObject(msgContext);
        }
    }
}
