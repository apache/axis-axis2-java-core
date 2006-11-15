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

package org.apache.axis2.jaxws.param;

import java.io.ByteArrayInputStream;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;

public class SOAPMessageParameter implements Parameter {
	private SOAPMessage value;
	
	public SOAPMessageParameter() {
        //do nothing
    }
    
    public SOAPMessageParameter(SOAPMessage value){
		this.value = value;
	}
	public Object getValue() {
		return value;
	}

	public void setValue(Object o) {
		value = (SOAPMessage)o;
	}

    //TODO: This needs to be replaced by Rich's work with the SAAJ<->OM Converter
	public XMLStreamReader getValueAsStreamReader() {
        XMLStreamReader reader = null;
        try {
            String SoapContent = value.getSOAPPart().getEnvelope().getValue();
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(SoapContent.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reader;
    }
    
    public OMElement toOMElement() {
		try{
		    StAXOMBuilder builder =
	            new StAXOMBuilder(OMAbstractFactory.getOMFactory(), getValueAsStreamReader()); 
	        return builder.getDocumentElement();
		}catch(Exception e){
			throw new WebServiceException(e.getMessage());
		}
	}

	public void fromOM(OMElement omElement) {
		try{
			MessageFactory msgFactory = MessageFactory.newInstance();
			
			//Read OMelement and create SOAPMessage data
			String soapContent = omElement.toString();
			
			SOAPMessage message = msgFactory.createMessage(null, new ByteArrayInputStream(soapContent.getBytes()));
			value = message;
		}catch(Exception e){
			throw new WebServiceException(e.getMessage());
		}
	}

    public SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI) {
		// TODO Auto-generated method stub
		return null;
	}
	public void fromEnvelope(Mode mode, SOAPEnvelope env) {
		// TODO Auto-generated method stub
		
	}

}
