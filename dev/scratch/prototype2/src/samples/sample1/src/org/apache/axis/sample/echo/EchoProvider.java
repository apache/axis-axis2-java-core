/*
 * Created on Jan 29, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.axis.sample.echo;

import java.lang.reflect.Method;

import javax.xml.stream.XMLStreamReader;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.encoding.Encoder;
import org.apache.axis.encoding.OutObjectArrayImpl;
import org.apache.axis.encoding.OutObjectImpl;
import org.apache.axis.encoding.SimpleTypeEncodingUtils;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.llom.builder.ObjectToOMBuilder;
import org.apache.axis.impl.providers.SimpleJavaProvider;
import org.apache.axis.om.OMConstants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OutObject;
import org.apache.axis.om.SOAPEnvelope;

public class EchoProvider extends SimpleJavaProvider {

	public Object[] deserializeParameters(XMLStreamReader xpp, Method method)
		throws AxisFault {
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
			} else if (EchoStruct.class.equals(parms[i])) {
				Encoder en = new EchoStructEncoder(null);
				objs[i] = en.deSerialize(xpp);
			} else if (EchoStruct[].class.equals(parms[i])) {
				objs[i] =
					SimpleTypeEncodingUtils.deserializeArray(
						xpp,
						new EchoStructEncoder(null));
			} else {
				throw new UnsupportedOperationException("Only int,String and String[] is supported yet");
			}
		}
		return objs;
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
			OutObject outobj = null;

			if (result instanceof String || result instanceof String[]) {
				outobj = new OutObjectImpl(result);
			} else if (result instanceof EchoStruct) {
				outobj = new EchoStructEncoder((EchoStruct) result);
			} else if (result instanceof EchoStruct[]) {
				outobj =
					new OutObjectArrayImpl(
						(EchoStruct[]) result,
						new EchoStructEncoder(null));
			}

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
				"arrays");
			msgContext.setEnvelope(responseEnvelope);

			return msgContext;
		} catch (SecurityException e) {
			throw AxisFault.makeFault(e);
		} catch (IllegalArgumentException e) {
			throw AxisFault.makeFault(e);
		}
	}

}
