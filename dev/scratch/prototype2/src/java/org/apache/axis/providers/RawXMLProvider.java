/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.providers;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMUtils;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * This is a Simple java Provider.
 */

public class RawXMLProvider extends AbstractProvider implements Provider {
    protected Log log = LogFactory.getLog(getClass());

    private String message;
    private String scope;
    private Method method;
    private ClassLoader classLoader;

    public RawXMLProvider() {
        scope = Constants.APPLICATION_SCOPE;

    }

    protected Object makeNewServiceObject(MessageContext msgContext)
            throws AxisFault {
        try {
            AxisService service = msgContext.getService();
            classLoader = service.getClassLoader();
            Class implClass = service.getServiceClass();
            return implClass.newInstance();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public Object getTheImplementationObject(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getService();
        QName serviceName = service.getName();
        if (Constants.APPLICATION_SCOPE.equals(scope)) {
            return makeNewServiceObject(msgContext);
        } else if (Constants.SESSION_SCOPE.equals(scope)) {
            SessionContext sessionContext = msgContext.getSessionContext();
            Object obj = sessionContext.get(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                sessionContext.put(serviceName, obj);
            }
            return obj;
        } else if (Constants.GLOBAL_SCOPE.equals(scope)) {
            SessionContext globalContext = msgContext.getSessionContext();
            Object obj = globalContext.get(serviceName);
            if (obj == null) {
                obj = makeNewServiceObject(msgContext);
                globalContext.put(serviceName, obj);
            }
            return obj;
        } else {
            throw new AxisFault("unknown scope " + scope);
        }

    }


    public MessageContext invoke(MessageContext msgContext) throws AxisFault {
        try {
            //get the implementation class for the Web Service 
            Object obj = getTheImplementationObject(msgContext);
            
            //find the WebService method  
            Class ImplClass = obj.getClass();
            String methodName = msgContext.getOperation().getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }

            OMElement methodElement = OMUtils.getFirstChildElement(msgContext.getEnvelope().getBody());
            OMElement parmeter = OMUtils.getFirstChildElement(methodElement);

            Object[] parms = new Object[]{parmeter};
            //invoke the WebService 
            OMElement result = (OMElement) method.invoke(obj, parms);
            MessageContext msgContext1 = new MessageContext(msgContext.getGlobalContext().getRegistry(), msgContext.getProperties(),msgContext.getSessionContext());

            SOAPEnvelope envelope = OMFactory.newInstance().getDefaultEnvelope();
            envelope.getBody().setFirstChild(result);
            msgContext1.setEnvelope(envelope);

            return msgContext1;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public void revoke(MessageContext msgContext) {
        log.info("I am Speaking Provider revoking :)");
    }
}
