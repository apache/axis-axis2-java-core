package benchmark1;

import org.apache.axis.testUtils.*;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.om.*;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: Ajith
 * Date: Feb 10, 2005
 * Time: 3:10:05 PM
 */
public class BenchProvider extends SimpleJavaProvider {

    public Object[] deserializeParameters(
      MessageContext msgContext,
      Method method)
      throws AxisFault {
      //   org.TimeRecorder.BEFORE_DESERALIZE = System.currentTimeMillis();
      XMLStreamReader xpp =
          msgContext.getSoapOperationElement().getPullParser(true);
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
						objs[i] =
							new Integer(
								SimpleTypeEncodingUtils.deserializeInt(xpp));
					} else if (String.class.equals(parms[i])) {
						objs[i] = SimpleTypeEncodingUtils.deserializeString(xpp);
					} else if (String[].class.equals(parms[i])) {
						objs[i] = SimpleTypeEncodingUtils.deserializeStringArray(xpp);
					} else if (double.class.equals(parms[i])) {
						objs[i] =new Double(SimpleTypeEncodingUtils.deserializeDouble(xpp));
					} else if (double[].class.equals(parms[i])) {
                     	objs[i] = SimpleTypeEncodingUtils.deserializeDoubleArray(xpp);
					} else if (int[].class.equals(parms[i])) {
                     	objs[i] = SimpleTypeEncodingUtils.deserializeIntArray(xpp);
					} else if (byte[].class.equals(parms[i])) {
                     	objs[i] = SimpleTypeEncodingUtils.deserializeByteArray(xpp);
					}else {
						throw new UnsupportedOperationException("Only int,String and String[] is supported yet");
					}
				}
				return objs;

			}
		} catch (Exception e) {
			throw new AxisFault("Exception",e);
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

			Benchmark1PortType benchmark = (Benchmark1PortType)obj;
			Object result = null;
			if("echoVoid".equals(methodName))	{
				result = "";//benchmark.echoVoid();
			}else if ("echoStrings".equals(methodName))	{
				result = benchmark.echoStrings((String[])parms[0]);
			}else if ("echoInts".equals(methodName))	{
				result = benchmark.echoInts((int[])parms[0]);
			}else if ("echoDoubles".equals(methodName))	{
				result = benchmark.echoDoubles((double[])parms[0]);
			}else if ("echoBase64".equals(methodName))	{
				result = benchmark.echoBase64((byte[])parms[0]);
			}else if ("receiveStrings".equals(methodName))	{
				result = new Integer(benchmark.receiveStrings((String[])parms[0]));
			} else if ("receiveInts".equals(methodName))	{
				result = new Integer(benchmark.receiveInts((int[])parms[0]));
			} else if ("receiveDoubles".equals(methodName))	{
				result = new Integer(benchmark.receiveDoubles((double[])parms[0]));
			}else if ("receiveBase64".equals(methodName))	{
				result = new Integer(benchmark.receiveBase64((byte[])parms[0]));
			} else if ("sendStrings".equals(methodName))	{
				result = benchmark.sendStrings(((Integer)parms[0]).intValue());
			} else if ("sendInts".equals(methodName))	{
				result = benchmark.sendInts(((Integer)parms[0]).intValue());
			}else if ("sendDoubles".equals(methodName))	{
				result = benchmark.sendDoubles(((Integer)parms[0]).intValue());
			}else if ("sendBase64".equals(methodName))	{
				result = benchmark.sendBase64(((Integer)parms[0]).intValue());
			}


			Encoder outobj = null;

            Class clazz = result.getClass();
            if (clazz == String.class ||
                clazz == String[].class ||
                clazz == int.class ||
                clazz == Integer.class ||
                clazz == int[].class ||
                clazz == double.class ||
                clazz == Double.class ||
                clazz == double[].class ||
                clazz == byte[].class) {
				outobj = new SimpleTypeEncoder(result);
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
            returnelement.declareNamespace(
                "http://axis.apache.org",
                "s");

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


