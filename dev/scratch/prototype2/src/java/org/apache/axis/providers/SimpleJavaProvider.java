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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.encoding.Encoder;
import org.apache.axis.encoding.SimpleTypeEncoder;
import org.apache.axis.encoding.SimpleTypeEncodingUtils;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Constants;
import org.apache.axis.engine.Provider;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.ObjectToOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a Simple java Provider.
 */

public class SimpleJavaProvider extends AbstractProvider implements Provider {
    protected Log log = LogFactory.getLog(getClass());
    protected String scope;
    protected Method method;
   // protected ClassLoader classLoader;

    public SimpleJavaProvider() {
        scope = Constants.APPLICATION_SCOPE;

    }

    protected Object makeNewServiceObject(MessageContext msgContext)
        throws AxisFault {
        try {
            AxisService service = msgContext.getService();
            Class implClass = service.getServiceClass();
            return implClass.newInstance();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    public Object getTheImplementationObject(MessageContext msgContext)
        throws AxisFault {
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

    public Object[] deserializeParameters(
        MessageContext msgContext,
        Method method)
        throws AxisFault {
        //   org.TimeRecorder.BEFORE_DESERALIZE = System.currentTimeMillis();
        XMLStreamReader xpp =
            msgContext.getSoapOperationElement().getPullParser(true);
        Class[] parms = method.getParameterTypes();
        Object[] objs = new Object[parms.length];

        for (int i = 0; i < parms.length; i++) {
            if (int.class.equals(parms[i])) {
                objs[i] =
                    new Integer(SimpleTypeEncodingUtils.deserializeInt(xpp));
            } else if (String.class.equals(parms[i])) {
                objs[i] = SimpleTypeEncodingUtils.deserializeString(xpp);
            } else if (String[].class.equals(parms[i])) {
                objs[i] = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
            } else {
                throw new UnsupportedOperationException("Only int,String and String[] is supported yet");
            }
        }
        return objs;
    }


    public QName getName() {
        return name;
    }

    public MessageContext invoke(MessageContext msgContext) throws AxisFault {
        try {
            //get the implementation class for the Web Service 
            Object obj = getTheImplementationObject(msgContext);

            //find the WebService method  
            Class ImplClass = obj.getClass();
            AxisOperation op = msgContext.getOperation();
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }
            //deserialize (XML-> java)
            Object[] parms = deserializeParameters(msgContext, method);
            //invoke the WebService 
            Object result = method.invoke(obj, parms);
            Encoder outobj = new SimpleTypeEncoder(result);
            OMFactory fac = OMFactory.newInstance();
            SOAPEnvelope responseEnvelope = fac.getDefaultEnvelope();

            OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
            OMElement responseMethodName =
                fac.createOMElement(methodName + "Response", ns);
            responseEnvelope.getBody().addChild(responseMethodName);
            OMElement returnelement =
                fac.createOMElement(methodName + "Return", ns);
            responseMethodName.addChild(returnelement);

            returnelement.setBuilder(
                new ObjectToOMBuilder(returnelement, outobj));
            returnelement.declareNamespace(
                OMConstants.ARRAY_ITEM_NSURI,
                OMConstants.ARRAY_ITEM_NS_PREFIX);
            msgContext.setEnvelope(responseEnvelope);

            return msgContext;
        } catch (SecurityException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalArgumentException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalAccessException e) {
            throw AxisFault.makeFault(e);
        } catch (InvocationTargetException e) {
            throw AxisFault.makeFault(e);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
}
