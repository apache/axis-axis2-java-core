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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.jaxws.util.SoapUtils;


/*
 * The StringParameter is a simple implementation of the Parameter
 * interface for binding XML strings.
 */
public class StringParameter implements Parameter {
    
    private String value;
    
    public StringParameter() {
        //do nothing
    }
    
    public StringParameter(String s) {
        this.value = s;
    }
    
    /**
     * @see Parameter#getValue()
     */
    public Object getValue() { 
        return value;
    }
    
    /**
     * @see Parameter#setValue(Object)
     */
    public void setValue(Object v) {
        this.value = (String) v;
    }
    
    /**
     * @see Parameter#getValueAsStreamReader()
     */
    public XMLStreamReader getValueAsStreamReader() {
        if (value != null) {
            try {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                        new ByteArrayInputStream(value.getBytes()));
                return reader;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            throw new WebServiceException("Cannot create XMLStreamReader from null value");
        }
        
        return null;
    }
    
    /**
     * @see Parameter#toOMElement()
     */
    public OMElement toOMElement() {
        try {
            StAXOMBuilder builder =
                new StAXOMBuilder(OMAbstractFactory.getOMFactory(), getValueAsStreamReader()); 
            
            OMElement documentElement = builder.getDocumentElement();
            return documentElement;
        } catch (Exception e) {
        	throw new WebServiceException(e.getMessage());
        }
        
    }
    
    /**
     * @see Parameter#fromOM(OMElement)
     */
    public void fromOM(OMElement documentElement){
    	this.value = documentElement.toString();
    }

    public SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI) {
		// TODO Auto-generated method stub
    	SOAPFactory soapfactory =  SoapUtils.getSoapFactory(soapVersionURI);
    	SOAPEnvelope env = null;
    	try{
    		if(mode !=null && mode.equals(Mode.MESSAGE)){	
    			/* I am assuming that if the mode is message then param has a soap xml with
    			 * Envelope as root node. If not then I will throw a webservice exception.
    			 */
    			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(value.getBytes()));
        		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, soapfactory, soapVersionURI);
    			return builder.getSOAPEnvelope();
    		}
    		else{
    			/* I am assuming that param is a plain xml string.
    			 * This code is return just to cater to dispatch sync call where we are using
    			 * serviceClient.
    			 */
    			env = soapfactory.getDefaultEnvelope();
    			env.getHeader().detach();
    			SOAPBody body = env.getBody();
    			body.addChild(toOMElement());
    		}
    	}catch(Exception e){
    		throw new WebServiceException(e.getMessage());
    	}
       return env;
	}

	public void fromEnvelope(Mode mode, SOAPEnvelope env) {
		// TODO Auto-generated method stub
		if(env == null){
			//add validation code here
		}
		
		if(mode == null || mode.equals(Mode.PAYLOAD)){
			OMElement om = env.getBody();
			fromOM(om.getFirstElement());
		}
		else if(mode.equals(Mode.MESSAGE)){
			fromOM(env);
		}
		
	}
}
