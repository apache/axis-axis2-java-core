
package userguide.sample1;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
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


public class AsynchronousClient {
	
	public static void main(String[] args) throws AxisFault {
		if(2 != args.length ){
			System.out.println("Usage <Port> ");
			
		}
		
		System.out.println("Initializing the Client Call....");
		Call call = new Call();
		System.out.println("Setting the Endpointreference ");
		URL url = null;
		try {
			url = new URL("http","127.0.0.1",new Integer(args[0]).intValue(),args[1]);
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			System.exit(0);
		}
		
		call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
		
		
		SOAPEnvelope requestEnvelop = ClientUtil.getEchoSoapEnvelop();
		try {
			call.setListenerTransport("http", true);
			
			System.out.println("Sending the Async message ....");
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			requestEnvelop.serialize(writer,true);
			writer.flush();
			System.out.println();
			
			call.sendReceiveAsync(requestEnvelop, new ClientEchoCallbackHandler() );
			
			
		} catch (AxisFault e1) {
			
			e1.printStackTrace();                            
		}
		catch (XMLStreamException e){
			e.printStackTrace();
		}
		
		System.out.println("Message sent and the client thread sleep till the responce ....");		
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e2) {
			System.exit(0);
			
		}
	}

}
