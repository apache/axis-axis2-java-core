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

package interop.doclit;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;
import org.apache.axis.testUtils.SimpleJavaProvider;
import org.apache.axis.testUtils.SimpleTypeEncoder;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Feb 10, 2005
 * Time: 3:10:05 PM
 */
public class InteropProvider extends SimpleJavaProvider {

    public Object[] deserializeParameters(MessageContext msgContext, Method method)
        throws AxisFault {
        XMLStreamReader xpp = msgContext.getSoapOperationElement().getPullParser(true);
        Class[] parms = method.getParameterTypes();
        Object[] objs = new Object[parms.length];

        try {
            int event = xpp.next();
            while (XMLStreamConstants.START_ELEMENT != event
                && XMLStreamConstants.END_ELEMENT != event) {
                event = xpp.next();
            }
            //now we are at the opearion element event
            event = xpp.next();
            while (XMLStreamConstants.START_ELEMENT != event
                && XMLStreamConstants.END_ELEMENT != event) {
                event = xpp.next();
            }
            //now we are at the parameter element event

            if (XMLStreamConstants.END_ELEMENT == event) {
                return null;
            } else {
                for (int i = 0; i < parms.length; i++) {
                    if (int.class.equals(parms[i])) {
                        objs[i] = new Integer(SimpleTypeEncodingUtils.deserializeInt(xpp));
                    } else if (String.class.equals(parms[i])) {
                        objs[i] = SimpleTypeEncodingUtils.deserializeString(xpp);
                    } else if (String[].class.equals(parms[i])) {
                        objs[i] = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
                    } else if (SOAPStruct.class.equals(parms[i])) {
                        SOAPStructEncoder enc = new SOAPStructEncoder();
                        objs[i] = enc.deSerialize(xpp);
                    }  else {
                        throw new UnsupportedOperationException("UnSupported type "+parms[i]);
                    }
                }
                return objs;

            }
        } catch (Exception e) {
            throw new AxisFault("Exception", e);
        }
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

            WSDLInteropTestDocLitPortType benchmark = (WSDLInteropTestDocLitPortType) obj;
            Object result = null;
            if ("echoVoid".equals(methodName)) {
               benchmark.echoVoid();
            } else if ("echoString".equals(methodName)) {
                result = benchmark.echoString((String) parms[0]);
            } else if ("echoStringArray".equals(methodName)) {
                result = benchmark.echoStringArray((String[]) parms[0]);
            } else if ("echoStruct".equals(methodName)) {
                result = benchmark.echoStruct((SOAPStruct) parms[0]);
            }
            Encoder outobj = null;
            if(result != null){
                if (result instanceof String || result instanceof String[]) {
                    outobj = new SimpleTypeEncoder(result);
                } else if (result instanceof SOAPStruct) {
                    outobj = new SOAPStructEncoder((SOAPStruct) result);
                } 
            }

            OMFactory fac = OMFactory.newInstance();
            SOAPEnvelope responseEnvelope = fac.getDefaultEnvelope();

            OMNamespace ns = fac.createOMNamespace("http://soapinterop.org/WSDLInteropTestDocLit", "res");
            OMElement responseMethodName = fac.createOMElement(methodName + "Response", ns);
            responseEnvelope.getBody().addChild(responseMethodName);
            if(result != null){
                OMElement returnelement = fac.createOMElement(methodName + "Return", ns);
                responseMethodName.addChild(returnelement);
                returnelement.setBuilder(new ObjectToOMBuilder(returnelement, outobj));
                returnelement.declareNamespace(OMConstants.ARRAY_ITEM_NSURI, "arrays");
                returnelement.declareNamespace("http://soapinterop.org/WSDLInteropTestDocLit", "s");
            }
            msgContext.setEnvelope(responseEnvelope);

            return msgContext;

        } catch (SecurityException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalArgumentException e) {
            throw AxisFault.makeFault(e);
        } catch (java.rmi.RemoteException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
