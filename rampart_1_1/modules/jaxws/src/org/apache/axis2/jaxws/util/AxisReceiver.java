/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.util;


import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.param.Parameter;


public class AxisReceiver implements MessageReceiver {
	private AsyncResult result = null;
	private SOAPEnvelope envelope = null;
	private Parameter param = null;
	
	boolean complete = false;
	public AxisReceiver(Parameter param){
		this.param = param;
	}

	public void receive(MessageContext messageCtx) throws AxisFault {  
        result = new AsyncResult(messageCtx);
        envelope = result.getResponseEnvelope();
        this.complete = true;
        System.out.println("Receiver Called");      
        
    }
	
	public boolean isdone(){
		return complete;
	}
	
	public AsyncResult getResponseResult(){
		return result;
	}
	
	public SOAPEnvelope getResponseEnvelope(){
		return envelope;
	}
	
	public Object getObject(){
		param.fromOM(getElement());
		return param.getValue();
	}
	
	public Object getObject(Parameter param){
		param.fromOM(getElement());
		return param.getValue();
	}
	
	private OMElement getElement(){
    	XMLStreamReader parser = envelope.getXMLStreamReader();
    	StAXOMBuilder builder =
            new StAXOMBuilder(OMAbstractFactory.getOMFactory(), parser); 
        return builder.getDocumentElement();
    }

}
