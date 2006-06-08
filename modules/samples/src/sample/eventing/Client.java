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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.savan.eventing.client.EventingClient;
import org.apache.savan.eventing.client.EventingClientBean;

public class Client {

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    
    private final int MIN_OPTION = 1;
    private final int MAX_OPTION = 6;
    
    private final String SUBSCRIBER_1_ID = "subscriber1";
    private final String SUBSCRIBER_2_ID = "subscriber2";
    
    private ServiceClient serviceClient = null;
    private Options options = null;
    private EventingClient eventingClient = null;
    
    private String toAddress = "http://localhost:8080/axis2/services/PublisherService";
    private String listner1Address = "http://localhost:8080/axis2/services/ListnerService1";
    private String listner2Address = "http://localhost:8080/axis2/services/ListnerService2";
    
	private final String applicationNamespaceName = "http://tempuri.org/"; 
	private final String dummyMethod = "dummyMethod";
    
	public static void main (String[] args) throws Exception {
		Client c = new Client ();
		c.run ();
	}
	
	public void run () throws Exception {
		
		System.out.println("\n");
		System.out.println("Welcome to Axis2 Eventing Sample");
		System.out.println("================================\n");
		
		boolean validOptionSelected = false;
		int selectedOption = -1;
		while (!validOptionSelected) {
			displayMenu();
			selectedOption = getIntInput();
			if (selectedOption>=MIN_OPTION && selectedOption<=MAX_OPTION)
				validOptionSelected = true;
			else 
				System.out.println("\nInvalid Option \n\n");
		}
			
		initClient ();
		performAction (selectedOption);
		
		//TODO publish
		
		System.out.println("Press enter to initialize the publisher service.");
		reader.readLine();
		
		options.setAction("uuid:DummyMethodAction");
		serviceClient.fireAndForget(getDummyMethodRequestElement ());
		
		while (true) {
			
			validOptionSelected = false;
			selectedOption = -1;
			while (!validOptionSelected) {
				displayMenu();
				selectedOption = getIntInput();
				if (selectedOption>=MIN_OPTION && selectedOption<=MAX_OPTION)
					validOptionSelected = true;
				else 
					System.out.println("\nInvalid Option \n\n");
			}
				
			performAction (selectedOption);
			
		}
		
	}
	
	private void displayMenu () {
		System.out.println("Press 1 to subscribe Listner Service 1");
		System.out.println("Press 2 to subscribe Listner Service 2");
		System.out.println("Press 3 to subscribe both listner services");
		System.out.println("Press 4 to unsubscribe Listner Service 1");
		System.out.println("Press 5 to unsubscribe Listner Service 2");
		System.out.println("Press 6 to unsubscribe both listner services");
		System.out.println("Press 7 to Exit");
	}
	
	private int getIntInput () throws IOException {
        String option = reader.readLine();
        try {
            int param = Integer.parseInt(option);
            return param;
        } catch (NumberFormatException e) {
        	//invalid option
        	return -1;
        }
	}
	
	private void initClient () throws AxisFault {
		String CLIENT_REPO_PATH = "E:\\temp\\REPO";
		String AXIS2_XML = "E:\\temp\\REPO\\axis2.xml";
		
		ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(CLIENT_REPO_PATH,AXIS2_XML);
		serviceClient = new ServiceClient (configContext,null); //TODO give a repo
		
		options = new Options ();
		serviceClient.setOptions(options);
		serviceClient.engageModule(new QName ("addressing"));
		
		eventingClient = new EventingClient (serviceClient);
		options.setTo(new EndpointReference (toAddress));
	}
	
	private void performAction (int action) throws Exception {
		
		switch (action) {
		case 1:
			doSubscribe(SUBSCRIBER_1_ID);
			break;
		case 2:
			doSubscribe(SUBSCRIBER_2_ID);
			break;
		case 3:
			doSubscribe(SUBSCRIBER_1_ID);
			doSubscribe(SUBSCRIBER_2_ID);
			break;
		case 4:
			doUnsubscribe(SUBSCRIBER_1_ID);
			break;
		case 5:
			doUnsubscribe(SUBSCRIBER_2_ID);
			break;
		case 6:
			doUnsubscribe(SUBSCRIBER_1_ID);
			doUnsubscribe(SUBSCRIBER_2_ID);
			break;
		case 7:
			System.exit(0);
			break;
		default:
			break;
		}
	}
	
	private void doSubscribe (String ID) throws Exception {
		EventingClientBean bean = new EventingClientBean ();
		
		String subscribingAddress = null;
		if (SUBSCRIBER_1_ID.equals(ID))
			subscribingAddress = listner1Address;
		else if (SUBSCRIBER_2_ID.equals(ID))
			subscribingAddress = listner2Address;
	
		bean.setDeliveryEPR(new EndpointReference (subscribingAddress));
		
		eventingClient.subscribe(bean,ID);
		Thread.sleep(1000);   //TODO remove if not sequired
	}
	
	private void doUnsubscribe (String ID) throws Exception {
		eventingClient.unsubscribe(ID);
		Thread.sleep(1000);   //TODO remove if not sequired
	}
	
	private OMElement getDummyMethodRequestElement() {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMNamespace namespace = fac.createOMNamespace(applicationNamespaceName,"ns1");
		OMElement dummyMethodElem = fac.createOMElement(dummyMethod, namespace);

		return dummyMethodElem;
	}
}
