/*
 * Copyright  1999-2004 The Apache Software Foundation.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.savan.eventing.subscribers;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.eventing.Delivery;
import org.apache.savan.subscribers.LeafSubscriber;

public class EventingLeafSubscriber extends LeafSubscriber implements EventingSubscriber {

	private EndpointReference endToEPr;
	
	private Delivery delivery;
	
	public Delivery getDelivery() {
		return delivery;
	}

	public EndpointReference getEndToEPr() {
		return endToEPr;
	}

	public void setDelivery(Delivery delivery) {
		this.delivery = delivery;
	}

	public void setEndToEPr(EndpointReference errorReportingEPR) {
		this.endToEPr = errorReportingEPR;
	}
	
	public void doProtocolSpecificPublication(SavanMessageContext publication) throws SavanException {
		
		EndpointReference deliveryEPR  = delivery.getDeliveryEPR();
		
		try {
			ServiceClient sc = new ServiceClient (publication.getConfigurationContext(),null);
			
			Options options = publication.getMessageContext().getOptions();
			if (options==null) {
				options = new Options ();
			}
			
			sc.engageModule(new QName ("addressing"));
			
			options.setProperty("xmppasync", "true");
			sc.setOptions(options);
			
			options.setTo(deliveryEPR);
			MessageContext mc = new MessageContext ();
			mc.setEnvelope(publication.getEnvelope());
			OperationClient client = sc.createClient(ServiceClient.ANON_OUT_ONLY_OP);
			client.addMessageContext(mc);
			client.execute(true);
		} catch (AxisFault e) {
			throw new SavanException (e);
		}
		
		
	}

}
