import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.SOAPEnvelope;

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


public class SynchronousClient {
	
	public static void main(String[] args){
		System.out.println("Initializing the Client Call....");
		Call call = new Call();
		System.out.println("Setting the Endpointreference ");
		URL url = null;
		try {
			url = new URL("http","127.0.0.1",new Integer(args[0]).intValue(),"/axis2/services/sample1");
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			System.exit(0);
		}
		
		call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
		
		try {
			call.setListenerTransport(Constants.SESSION_SCOPE, true);
			SOAPEnvelope requestEnvelop = getEchoSoapEnvelop();
			
			System.out.println("Sending request...");
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			requestEnvelop.serialize(writer,true);
			writer.flush();
			System.out.println();
			SOAPEnvelope responceEnvelop = call.sendReceive(requestEnvelop);
			System.out.println("Responce received  ...");
			responceEnvelop.serialize(writer,true);
			writer.flush();
			
		} catch (AxisFault e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (XMLStreamException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
	}
	
	
	private static SOAPEnvelope getEchoSoapEnvelop(){
		OMFactory omFactory = OMFactory.newInstance();
		SOAPEnvelope envelope = omFactory.getDefaultEnvelope();
		OMNamespace namespace = envelope.declareNamespace("http://sample1.org/sample1", "sample1");
		
		OMElement bodyContent = omFactory.createOMElement("echo", namespace);
		
		
		OMElement text = omFactory.createOMElement("Text", namespace);
		text.addChild(omFactory.createText("Axis2 Echo String"));
		bodyContent.addChild(text);
		envelope.getBody().addChild(bodyContent);
		return envelope;
	}

}
