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
package org.apache.axis.samples.userguide.example1;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.samples.utils.OMUtil;

/**
 * This file is hand written for the M1 demp purposes and will be 
 * auto generated fron WSDLx4Java tool.
 * 
 */
public class InteropTest_Stub {

	private OMFactory omFactory = OMFactory.newInstance();
	private Call call = new Call();
	
		
	
	
	//////////////////////User setter methods//////////////////////
	
	public void setEnePointReference(EndpointReference epr){
		this.call.setTo(epr);		
	}
	
	//\\\\\\\\\\\\\\\\\\End user setter methods\\\\\\\\\\\\\\\\\\//
	
	
	/////////////////////Webservice Operations/////////////////////
	
	public java.lang.String echoString(java.lang.String inputValue)throws AxisFault{
		this.validate();
		SOAPEnvelope responceEnvelop = call.sendReceive(this.getSOAPEnvelopForEchoString(inputValue));	
		return this.getEchoStringFromSOAPEnvelop(responceEnvelop);
		
		
		
	}
	//\\\\\\\\\\\\\\\\End Webservice Operations\\\\\\\\\\\\\\\\\\//
	
	
	
	
	
	
	////////////////////////////////////////////////////////////////
	//						Util Methods						  //
	////////////////////////////////////////////////////////////////
	
	
	protected SOAPEnvelope getSOAPEnvelopForEchoString(String value){
		SOAPEnvelope envelop = OMUtil.getEmptySoapEnvelop();
		OMNamespace interopNamespace = envelop.declareNamespace("http://soapinterop.org/", "interop");
		OMElement echoStringMessage = omFactory.createOMElement("echoStringRequest", interopNamespace);
		OMElement text = omFactory.createOMElement("Text", null);
		text.addChild(omFactory.createText(value));
		echoStringMessage.addChild(text);
		envelop.addChild(echoStringMessage);
		return envelop;
	}
	
	protected String getEchoStringFromSOAPEnvelop(SOAPEnvelope envelop){
		OMElement body = envelop.getBody();
		OMElement response = null;
		Iterator childrenIter = body.getChildren();
		while(childrenIter.hasNext()){
			OMNode child = (OMNode) childrenIter.next();
			if(child instanceof OMElement && "echoStringResponse".equalsIgnoreCase(((OMElement)child).getLocalName())){
				response = (OMElement)child;				
			}
		}
		
		Iterator textChild = response.getChildrenWithName(new QName("", "Text"));
		String value= null;
		if(textChild.hasNext()){
			 value = ((String)((OMElement)textChild.next()).getValue());
		}
		return value;
		
	}
	
	
	
	protected void validate() throws AxisFault{
		String errorMessage = null;
		
		if(null == this.call.getTO())
			errorMessage = "End Point reference is not set: ";
		
		
		
		if(null != errorMessage){
			errorMessage = "One or more Errors occured :: "+ errorMessage;
			throw new AxisFault(errorMessage);
		}
		
		
	}
	
}
