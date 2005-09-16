package sample.security;

import java.io.StringWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.OMNamespace;

public class Client {

	/**
	 * @param args
	 */
	private static EndpointReference targetEPR = new EndpointReference(
			"http://127.0.0.1:8080/axis2/services/SecureService/echo");

	public static void main(String[] args) {
		try {
			
			//TODO : Get the repository location from the args
			
			
			OMElement payload = getEchoElement();
			Call call = new Call();
			call.setTo(targetEPR);
			call.setTransportInfo(Constants.TRANSPORT_HTTP,
					Constants.TRANSPORT_HTTP, false);

			//Blocking invocation
			OMElement result = call.invokeBlocking("echo", payload);

			StringWriter writer = new StringWriter();
			result.serializeWithCache(XMLOutputFactory.newInstance()
					.createXMLStreamWriter(writer));
			writer.flush();

			System.out.println(writer.toString());

		} catch (AxisFault axisFault) {
			axisFault.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	private static OMElement getEchoElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(
                "http://example1.org/example1", "example1");
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("Text", omNs);
        value.addChild(fac.createText(value, "Axis2 Echo String "));
        method.addChild(value);

        return method;
	}

}
