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
package samples.userguide.sample2.client;

import java.util.Iterator;

import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.clientapi.Call;
import org.apache.axis.clientapi.Callback;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;
import org.apache.axis.om.OMText;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This file is hand written for the M1 demp purposes and will be 
 * auto generated fron WSDLx4Java tool.
 * 
 */
public class InteropTest_Stub {

	private OMFactory omFactory = OMFactory.newInstance();
	private Call call = new Call();
	protected Log log = LogFactory.getLog(this.getClass());
	
		
	
	/////////////////////////////////////////////////////////////
	///					  User setter methods                 ///
	/////////////////////////////////////////////////////////////
	
	public void setEndPointReference(EndpointReference epr){
		this.call.setTo(epr);		
	}
	
	public void setListenerTransport(String transport, boolean blocked){
		call.setListenerTransport("http",blocked);
	}
	
	
	
	///////////////////////////////////////////////////////////////
	///                  Webservice Operations					///
	///////////////////////////////////////////////////////////////
	
	public void echoString(java.lang.String inputValue, Callback callback)throws AxisFault{
		this.validate();
		call.sendReceiveAsync(this.getSOAPEnvelopForEchoString(inputValue), callback);	
			
	}
	
	public void echoInt(Integer inputValue, Callback callback) throws AxisFault{
		this.validate();
		call.sendReceiveAsync(this.getSOAPEnvelopForEchoInt(inputValue), callback);		
		
	}
	
	
	
	////////////////////////////////////////////////////////////////
	//						Util Methods						  //
	////////////////////////////////////////////////////////////////
	
	
	protected SOAPEnvelope getSOAPEnvelopForEchoString(String value){
		SOAPEnvelope envelop = OMFactory.newInstance().getDefaultEnvelope();
		OMNamespace interopNamespace = envelop.declareNamespace("http://soapinterop.org/", "interop");
		OMElement echoStringMessage = omFactory.createOMElement("echoString", interopNamespace);
		OMElement text = omFactory.createOMElement("Text", interopNamespace);
		text.addChild(omFactory.createText(value));
		echoStringMessage.addChild(text);
		envelop.getBody().addChild(echoStringMessage);
		return envelop;
	}
	
	protected SOAPEnvelope getSOAPEnvelopForEchoInt(Integer value){
		SOAPEnvelope envelope = OMFactory.newInstance().getDefaultEnvelope();
		OMNamespace namespace = envelope.declareNamespace("http://soapinterop.org/", "interop");
		OMElement echoIntMessage = omFactory.createOMElement("echoInt", namespace);
		OMElement text = omFactory.createOMElement("Text", namespace);
		text.addChild(omFactory.createText(value.toString()));
		echoIntMessage.addChild(text);
		envelope.getBody().addChild(echoIntMessage);
		return envelope;		
	}
	
	
	public String getEchoStringFromSOAPEnvelop(SOAPEnvelope envelop) throws AxisFault{
		OMElement body = envelop.getBody();
		OMElement response = null;
		Iterator childrenIter = body.getChildren();
		while(childrenIter.hasNext()){
			OMNode child = (OMNode) childrenIter.next();
			if(child instanceof OMElement && "Fault".equalsIgnoreCase(((OMElement)child).getLocalName())){
				throw new AxisFault("Fault in server side");
			}

			if(child instanceof OMElement && "echoStringResponse".equalsIgnoreCase(((OMElement)child).getLocalName())){
				response = (OMElement)child;				
			}
		}		
		Iterator textChild = response.getChildren();
		while(textChild.hasNext()){
			OMNode  child = (OMNode) textChild.next();
			if(child instanceof OMElement && "echoStringReturn".equalsIgnoreCase(((OMElement)child).getLocalName())){
				
				OMNode val =((OMElement)child).getFirstChild();
				if(val instanceof OMText)
					return new String(((OMText)val).getValue());
				
			}
		}
		
		this.log.info("Invalid data Binding");
		throw new AxisFault("Invalid data Binding");		
	}
	
	public Integer getEchoIntFromSOAPEnvelop(SOAPEnvelope envelop) throws AxisFault{
		OMElement body = envelop.getBody();		
		OMElement response = null;		
		Iterator childrenIter = body.getChildren();
		while(childrenIter.hasNext()){
			OMNode child = (OMNode) childrenIter.next();
			if(child instanceof OMElement && "Fault".equalsIgnoreCase(((OMElement)child).getLocalName())){
				throw new AxisFault("Fault in server side");
			}
			
			if(child instanceof OMElement && "echoIntResponse".equalsIgnoreCase(((OMElement)child).getLocalName())){
				response = (OMElement)child;				
			}
		}
		
		Iterator textChild = response.getChildren();
		while(textChild.hasNext()){
			OMNode  child = (OMNode) textChild.next();
			if(child instanceof OMElement && "echoIntReturn".equalsIgnoreCase(((OMElement)child).getLocalName())){
				
				OMNode val =((OMElement)child).getFirstChild();
				if(val instanceof OMText)
					return new Integer(((OMText)val).getValue());
				
			}
		}
		
		this.log.info("Invalid data Binding");
		throw new AxisFault("Invalid data Binding");	
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
