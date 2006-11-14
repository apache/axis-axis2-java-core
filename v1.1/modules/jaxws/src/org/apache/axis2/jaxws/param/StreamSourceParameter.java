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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import org.apache.axiom.om.OMElement;


public class StreamSourceParameter extends SourceParameter {
	
    public StreamSourceParameter() {
        //do nothing
    }
    
    public StreamSourceParameter(StreamSource value){
		this.value = value;
	}

	public void fromOM(OMElement omElement) {
		if(omElement != null){
            //Convert OM to Byte Array and ByteArray to SourceStream.
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                omElement.serialize(output);
            } catch(XMLStreamException e) {
                e.printStackTrace();
            }
            InputStream input = new ByteArrayInputStream(output.toByteArray());
            value = new StreamSource(input);
		}
	}
	
    /*
    public SOAPEnvelope toEnvelope(Mode mode, String soapVersionURI) {
		SOAPFactory soapfactory =  SoapUtils.getSoapFactory(soapVersionURI);
    	SOAPEnvelope env = null;
    	try{
    		if(mode !=null && mode.equals(Mode.MESSAGE)){	
    			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(value);
        		StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(reader, soapfactory, soapVersionURI);
    			return builder.getSOAPEnvelope();
    		}
    		else{
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
    */

	/*
    public void fromEnvelope(Mode mode, SOAPEnvelope env) {
		if(env == null){
			//add validation code here
		}
		
		if(mode == null || mode.equals(Mode.PAYLOAD)){
			OMElement om = env.getBody();
			fromOM(om.getFirstElement());
			System.out.println("Mode is payload");
		}
		else if(mode.equals(Mode.MESSAGE)){
			System.out.println("Mode is message");
			fromOM(env);
		}
	}
    */
}
