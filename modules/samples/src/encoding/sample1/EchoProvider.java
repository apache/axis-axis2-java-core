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
 
package encoding.sample1;
import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.*;
import org.apache.axis.testUtils.ArrayTypeEncoder;
import org.apache.axis.testUtils.Encoder;
import org.apache.axis.testUtils.ObjectToOMBuilder;
import org.apache.axis.testUtils.SimpleJavaProvider;
import org.apache.axis.testUtils.SimpleTypeEncoder;
import org.apache.axis.testUtils.SimpleTypeEncodingUtils;

public class EchoProvider extends SimpleJavaProvider {

    public Object[] deserializeParameters(
      MessageContext msgContext,
      Method method)
      throws AxisFault {
      XMLStreamReader xpp =
          msgContext.getEnvelope().getBody().getFirstElement().getXMLStreamReader();
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
        //          now we are at the parameter element event 
            
			if (XMLStreamConstants.END_ELEMENT == event) {
				return null;
			} else {
				for (int i = 0; i < parms.length; i++) {
					if (int.class.equals(parms[i])) {
						objs[i] =
							new Integer(
								SimpleTypeEncodingUtils.deserializeInt(xpp));
					} else if (String.class.equals(parms[i])) {
						objs[i] =
							SimpleTypeEncodingUtils.deserializeString(xpp);
					} else if (String[].class.equals(parms[i])) {
						objs[i] =
							SimpleTypeEncodingUtils.deserializeStringArray(xpp);
					} else if (EchoStruct.class.equals(parms[i])) {
						Encoder en = new EchoStructEncoder(null);
						objs[i] = en.deSerialize(xpp);
					} else if (EchoStruct[].class.equals(parms[i])) {
                        Encoder encoder = new ArrayTypeEncoder(new EchoStructEncoder(null));
						objs[i] = encoder.deSerialize(xpp);
					} else {
						throw new UnsupportedOperationException("Only int,String and String[] is supported yet");
					}
				}
				return objs;

			}
		} catch (Exception e) {
			throw new AxisFault("Exception",e);
		}
	}

    public MessageContext invokeBusinessLogic(MessageContext msgContext) throws AxisFault{
		try {
			//get the implementation class for the Web Service 
			Object obj = getTheImplementationObject(msgContext);
			
			//find the WebService method  
			Class ImplClass = obj.getClass();
            AxisOperation op = msgContext.getoperationConfig();
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

			EchoImpl echo = (EchoImpl)obj;
			Object result = null;
			if("echoEchoStruct".equals(methodName))	{
				result = echo.echoEchoStruct((EchoStruct)parms[0]);
			}else if ("echoString".equals(methodName))	{
				result = echo.echoString((String)parms[0]);
			}else if ("echoStringArray".equals(methodName))	{
				result = echo.echoStringArray((String[])parms[0]);
			}else if ("echoEchoStructArray".equals(methodName))	{
				Object[] parmsIn = (Object[])parms[0];
				EchoStruct[] structs = new EchoStruct[parmsIn.length];
				for (int i = 0; i < structs.length; i++) {
					structs[i] = (EchoStruct) parmsIn[i];

				}
				result = echo.echoEchoStructArray(structs);
			}			
			Encoder outobj = null;

			if (result instanceof String || result instanceof String[]) {
				outobj = new SimpleTypeEncoder(result);
			} else if (result instanceof EchoStruct) {
				outobj = new EchoStructEncoder((EchoStruct) result);
			} else if (result instanceof EchoStruct[]) {
				outobj =
					new ArrayTypeEncoder(
						(EchoStruct[]) result,
						new EchoStructEncoder(null));
			}

			SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
			SOAPEnvelope responseEnvelope = fac.getDefaultEnvelope();

			OMNamespace ns = fac.createOMNamespace("http://soapenc/", "res");
			OMElement responseMethodName =
				fac.createOMElement(methodName + "Response", ns);
			responseEnvelope.getBody().addChild(responseMethodName);
            ObjectToOMBuilder builder = new ObjectToOMBuilder(outobj);
            OMElement returnelement =
				fac.createOMElement(methodName + "Return", ns,responseMethodName, builder);
            builder.setStartElement(returnelement);
			responseMethodName.addChild(returnelement);
			
			returnelement.declareNamespace(
				OMConstants.ARRAY_ITEM_NSURI,
				"arrays");
            returnelement.declareNamespace(
                "http://axis.apache.org",
                "s");

            MessageContext resMessageContext = new MessageContext(msgContext);
            resMessageContext.setEnvelope(responseEnvelope);

			return resMessageContext;
		} catch (SecurityException e) {
			throw AxisFault.makeFault(e);
		} catch (IllegalArgumentException e) {
			throw AxisFault.makeFault(e);
		}
	}

}
