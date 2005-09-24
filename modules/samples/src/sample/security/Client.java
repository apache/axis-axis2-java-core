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
	public static void main(String[] args) {
		try {
			
			// Get the repository location from the args
			String repo = args[0];
			String port = args[1];
			
			OMElement payload = getEchoElement();
			Call call = new Call(repo);
			
			call.setTo(new EndpointReference("http://127.0.0.1:" + port + "/axis2/services/SecureService"));
			call.setTransportInfo(Constants.TRANSPORT_HTTP,
					Constants.TRANSPORT_HTTP, false);
			call.set(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);

			//Blocking invocation
			OMElement result = call.invokeBlocking("echo", payload);

			StringWriter writer = new StringWriter();
			result.serializeWithCache(XMLOutputFactory.newInstance()
					.createXMLStreamWriter(writer));
			writer.flush();

			System.out.println("Response: " + writer.toString());

			System.out.println("SecureService Invocation successful :-)");
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
