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
package org.apache.axis.saaj;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Class SOAPConnectionImpl
 * 
 * @author Ashutosh Shahi (ashutosh.shahi@gmail.com)
 *
 */
public class SOAPConnectionImpl extends SOAPConnection {
	
	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPConnection#call(javax.xml.soap.SOAPMessage, java.lang.Object)
	 */
	public SOAPMessage call(SOAPMessage request, Object endpoint)
			throws SOAPException {
		try{
			org.apache.axis.soap.SOAPEnvelope envelope = ((SOAPEnvelopeImpl)request.getSOAPPart().getEnvelope()).getOMEnvelope();
			
			Call call = new Call();
			URL url = new URL(endpoint.toString());
			call.setTransportInfo(Constants.TRANSPORT_HTTP,Constants.TRANSPORT_HTTP, true);
			call.setTo(new EndpointReference(AddressingConstants.WSA_TO, url.toString()));
			org.apache.axis.soap.SOAPEnvelope responseEnv = (org.apache.axis.soap.SOAPEnvelope)call.invokeBlocking("echo", envelope);
			SOAPEnvelopeImpl response = new SOAPEnvelopeImpl(responseEnv);
			return new SOAPMessageImpl(response);
			
			}catch (MalformedURLException mue) {
				throw new SOAPException(mue);
			}catch (AxisFault af){
				throw new SOAPException(af);
			}
	}

	
	/* (non-Javadoc)
	 * @see javax.xml.soap.SOAPConnection#close()
	 */
	public void close() throws SOAPException {
		// TODO Auto-generated method stub

	}

}
