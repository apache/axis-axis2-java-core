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

import java.lang.reflect.Method;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.*;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;
import org.apache.axis.testUtils.SimpleJavaProvider;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;
import org.apache.wsdl.WSDLService;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Feb 10, 2005
 * Time: 3:10:05 PM
 */
public class InteropProvider extends SimpleJavaProvider {
    private int event;
    public Object[] deserializeParameters(
        MessageContext msgContext,
        Method method,
        XMLStreamReader xpp)
        throws AxisFault {
        try {
            Class[] parms = method.getParameterTypes();
            Object[] objs = new Object[parms.length];

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
                    } else {
                        throw new UnsupportedOperationException("UnSupported type " + parms[i]);
                    }
                }
                return objs;

            }

        } catch (Exception e) {
            throw new AxisFault("Exception", e);
        }
    }

    public MessageContext invokeBusinessLogic(MessageContext msgContext) throws AxisFault{
        try {
            if (WSDLService.STYLE_DOC.equals(msgContext.getMessageStyle())) {
                SOAPBody body = msgContext.getEnvelope().getBody();
                XMLStreamReader xpp = body.getXMLStreamReader();

                int event = xpp.next();
                while (XMLStreamConstants.START_ELEMENT != event
                    && XMLStreamConstants.END_ELEMENT != event) {
                    event = xpp.next();
                }
                event = xpp.next();
                while (XMLStreamConstants.START_ELEMENT != event
                    && XMLStreamConstants.END_ELEMENT != event) {
                    event = xpp.next();
                }
                //now we are at the parameters element event
                String methodName = xpp.getLocalName();
                int index = methodName.indexOf("Param");
                QName operationName = null;
                if (index <= 0) {
                    if ("echoVoid".equals(methodName)) {
                        operationName = new QName(methodName);
                    } else {
                        throw new AxisFault(
                            "first element in the Body should match methodName+Return the eleemntNam is"
                                + methodName);
                    }
                }else{
                    operationName = new QName(methodName.substring(0, index));                
                }


                AxisService service = msgContext.getServiceContext().getServiceConfig();
                if (operationName != null) {
                    AxisOperation op = service.getOperation(operationName);
                    if (op != null) {
                        msgContext.setOperationConfig(op);
                    } else {
                        throw new AxisFault("Operation not found " + operationName);
                    }
                } else {
                    throw new AxisFault(
                        "Operation Name not specifed the request String is " + methodName);
                }

                //get the implementation class for the Web Service
                Object obj = getTheImplementationObject(msgContext);

                //find the WebService method
                Class ImplClass = obj.getClass();
                AxisOperation op = msgContext.getoperationConfig();
                methodName = op.getName().getLocalPart();

                Method[] methods = ImplClass.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(methodName)) {
                        this.method = methods[i];
                        break;
                    }
                }
                //deserialize (XML-> java)
                Object[] parms = deserializeParameters(msgContext, method, xpp);
                //invoke the WebService

                WSDLInteropTestDocLitPortType benchmark = (WSDLInteropTestDocLitPortType) obj;
                Object result = null;
                OMElement returnelement = null;
                SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
                OMNamespace ns = fac.createOMNamespace("http://soapinterop.org/xsd", "doclitTypes");

                if ("echoVoid".equals(methodName)) {
                    benchmark.echoVoid();
                    returnelement = fac.createOMElement("echoVoidReturn", ns);
                } else if ("echoString".equals(methodName)) {
                    result = benchmark.echoString((String) parms[0]);
                    returnelement = fac.createOMElement("echoStringReturn", ns);
                } else if ("echoStringArray".equals(methodName)) {
                    result = benchmark.echoStringArray((String[]) parms[0]);
                    returnelement = fac.createOMElement("echoStringArrayReturn", ns);
                } else if ("echoStruct".equals(methodName)) {
                    result = benchmark.echoStruct((SOAPStruct) parms[0]);
                    returnelement = fac.createOMElement("echoStructReturn", ns);
                }
                Encoder outobj = null;
                if (result != null) {
                    if (result instanceof String || result instanceof String[]) {
                        outobj = new SimpleTypeEncoder(result);
                    } else if (result instanceof SOAPStruct) {
                        outobj = new SOAPStructEncoder((SOAPStruct) result);
                    }
                }

                SOAPEnvelope responseEnvelope = fac.getDefaultEnvelope();

                responseEnvelope.getBody().addChild(returnelement);
                if (result != null) {
                    returnelement.setBuilder(new ObjectToOMBuilder(returnelement, outobj));
                    returnelement.declareNamespace(OMConstants.ARRAY_ITEM_NSURI, "arrays");
                    returnelement.declareNamespace(
                        "http://soapinterop.org/WSDLInteropTestDocLit",
                        "s");

                }
                msgContext.setEnvelope(responseEnvelope);

                return msgContext;
            } else {
                throw new AxisFault("this Service only supports doc-lit");
            }

        } catch (SecurityException e) {
            throw AxisFault.makeFault(e);
        } catch (IllegalArgumentException e) {
            throw AxisFault.makeFault(e);
        } catch (java.rmi.RemoteException e) {
            throw AxisFault.makeFault(e);
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }
}
