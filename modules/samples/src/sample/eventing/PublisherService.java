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

package sample.eventing;

import java.util.Random;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;

public class PublisherService {
  
	ConfigurationContext configurationContext = null;
	
	public void init(ServiceContext serviceContext) throws AxisFault {
		System.out.println("Eventing Service INIT called");
		configurationContext = serviceContext.getConfigurationContext();
		
		PublisherThread thread = new PublisherThread ();
		thread.start();
	}
  
	public void dummyMethod(OMElement param) throws Exception  {
		System.out.println("Eventing Service dummy method called");
	}
	
	private class PublisherThread extends Thread {
		
		String Publication = "Publication";
		String publicationNamespaceValue = "http://tempuri/publication/";
		Random r = new Random ();
		
		public void run () {
			try {
				
				ServiceClient sc = new ServiceClient (configurationContext,null);
				Options options = new Options ();
				sc.setOptions(options);
				
				//if already engaged, axis2 will neglect this engagement.
				sc.engageModule( new QName("savan"));
				
				options.setTo(new EndpointReference ("http://dummyTargetEPR/"));
				options.setAction("DummyAction");
				
				while (true) {
					
					Thread.sleep(5000);
					
					//publishing
					System.out.println("Publishing next publication...");
					sc.fireAndForget(getNextPublicationEnvelope ());
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public OMElement getNextPublicationEnvelope () {
			SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
			OMNamespace namespace = factory.createOMNamespace(publicationNamespaceValue,"ns1");
			OMElement publicationElement = factory.createOMElement(Publication,namespace);
			
			int value = r.nextInt();
			publicationElement.setText(new Integer(value).toString());
			
			OMElement publishMethod = factory.createOMElement("publish",namespace);
			publishMethod.addChild(publicationElement);
			
			return publishMethod;
		}
	}
}
