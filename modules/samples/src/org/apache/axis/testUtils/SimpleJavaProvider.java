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
 
package org.apache.axis.testUtils;

import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.MessageReceiver;
import org.apache.axis.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a Simple java Provider.
 */

public class SimpleJavaProvider extends AbstractInOutSyncMessageReceiver implements MessageReceiver {
    protected Log log = LogFactory.getLog(getClass());
    protected String scope;
    protected Method method;
    // protected ClassLoader classLoader;

    public SimpleJavaProvider() {
        scope = Constants.APPLICATION_SCOPE;

    }


    public Object[] deserializeParameters(MessageContext msgContext,
                                          Method method)
            throws AxisFault {
        XMLStreamReader xpp =
                msgContext.getEnvelope().getBody().getFirstElement().getXMLStreamReader();
        Class[] parms = method.getParameterTypes();
        Object[] objs = new Object[parms.length];

        for (int i = 0; i < parms.length; i++) {
            if (int.class.equals(parms[i]) || Integer.class.equals(parms[i])) {
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


    public void receive(MessageContext msgContext) throws AxisFault {
    }
    /* (non-Javadoc)
     * @see org.apache.axis.receivers.AbstractInOutSyncMessageReceiver#invokeBusinessLogic(org.apache.axis.context.MessageContext)
     */
    public MessageContext invokeBusinessLogic(MessageContext msgContext,MessageContext newmsgctx)
        throws AxisFault {
            throw new UnsupportedOperationException();
            //TODO fix this
//            try {
//                //get the implementation class for the Web Service 
//                Object obj = getTheImplementationObject(msgContext);
//
//                //find the WebService method  
//                Class ImplClass = obj.getClass();
//                OperationDescription op = msgContext.getOperationContext().getAxisOperation();
//                String methodName = op.getName().getLocalPart();
//                Method[] methods = ImplClass.getMethods();
//                for (int i = 0; i < methods.length; i++) {
//                    if (methods[i].getName().equals(methodName)) {
//                        this.method = methods[i];
//                        break;
//                    }
//                }
//                //deserialize (XML-> java)
//                Object[] parms = deserializeParameters(msgContext, method);
//                //invoke the WebService 
//                Object result = method.invoke(obj, parms);
//                Encoder outobj = new SimpleTypeEncoder(result);
//                SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
//                SOAPEnvelope responseEnvelope = fac.getDefaultEnvelope();
//
//                OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
//                OMElement responseMethodName =
//                        fac.createOMElement(methodName + "Response", ns);
//                responseEnvelope.getBody().addChild(responseMethodName);
//                OMElement returnelement =
//                        fac.createOMElement(methodName + "Return", ns);
//                responseMethodName.addChild(returnelement);
//
//                returnelement.setBuilder(new ObjectToOMBuilder(returnelement, outobj));
//                returnelement.declareNamespace(OMConstants.ARRAY_ITEM_NSURI,
//                        OMConstants.ARRAY_ITEM_NS_PREFIX);
//                msgContext.setEnvelope(responseEnvelope);
//                return msgContext;
//            } catch (SecurityException e) {
//                throw AxisFault.makeFault(e);
//            } catch (IllegalArgumentException e) {
//                throw AxisFault.makeFault(e);
//            } catch (IllegalAccessException e) {
//                throw AxisFault.makeFault(e);
//            } catch (InvocationTargetException e) {
//                throw AxisFault.makeFault(e);
//            } catch (Exception e) {
//                throw AxisFault.makeFault(e);
//            }
    }

}
