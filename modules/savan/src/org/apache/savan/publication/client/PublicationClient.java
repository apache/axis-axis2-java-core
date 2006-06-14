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

package org.apache.savan.publication.client;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.savan.SavanException;

public class PublicationClient {

	public static void sendPublication (SOAPEnvelope publication,ConfigurationContext configurationContext) throws SavanException {
		
		try {
			ServiceClient sc = new ServiceClient (configurationContext,null);
			
			Options options = new Options ();
			options.setTo(new EndpointReference ("http://temp.publication.URI"));
			options.setAction("UUID:TempPublicationAction");
			sc.setOptions(options);
			
			//if already engaged, axis2 will neglect this engagement.
			sc.engageModule( new QName("savan"));
			
			MessageContext mc = new MessageContext ();
			mc.setEnvelope(publication);
			OperationClient client = sc.createClient(ServiceClient.ANON_OUT_ONLY_OP);
			client.addMessageContext(mc);
			client.execute(true);
		} catch (AxisFault e) {
			String message = "Could not send the publication";
			throw new SavanException (message,e);
		}
		
	}
}
