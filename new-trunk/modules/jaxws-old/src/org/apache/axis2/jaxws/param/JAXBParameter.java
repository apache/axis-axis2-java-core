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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;

/**
 * Parameter implementation for JAX-B objects. *
 */
public class JAXBParameter implements Parameter {
	
    private Object value;
    private JAXBContext context;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
	
    public JAXBParameter() {}
    
    public JAXBParameter(Object value){
		this.value = value;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object o) {
		value = o;
	}
    
    public JAXBContext getJAXBContext() {
        return context;
    }
    
    public void setJAXBContext(JAXBContext ctx) {
        context = ctx;
    }
    
    public XMLStreamReader getValueAsStreamReader() {
        createMarshaller();
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            marshaller.marshal(value, baos);
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bais);
            return reader;
        } catch (Exception e) {
            //TODO: Add proper error handling
            e.printStackTrace();
        }
        
        return null;
    }

	public OMElement toOMElement() {
        try {
            StAXOMBuilder builder = new StAXOMBuilder(
                    OMAbstractFactory.getOMFactory(),
                    getValueAsStreamReader());
            OMElement documentElement = builder.getDocumentElement();
            return documentElement;
        } catch (Exception e) {
            //TODO: Add proper error handling
            e.printStackTrace();
        }
		       
        return null;
	}

	public void fromOM(OMElement omElement) {
		createUnmarshaller();
        
        String omString = omElement.toString();
        System.out.println(">> [OMSTRING] " + omString);
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(omString.getBytes());
            value = unmarshaller.unmarshal(bais);
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
	}

	public SOAPEnvelope toEnvelope(Mode mode,String soapVersionURI) {
	    return ParameterUtils.toEnvelope(mode, soapVersionURI, this);
	}

	public void fromEnvelope(Mode mode, SOAPEnvelope env) {
		ParameterUtils.fromEnvelope(mode, env, this);
	}
    
    /*
     * Setup the Marshaller for serialization
     */
    private void createMarshaller() {
        if (context != null) {
            try {
                marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                
            } catch (JAXBException e) {
                throw new WebServiceException("Error creating Marshaller from JAXBContext");
            }
        }
        else {
            throw new WebServiceException("Cannot create Marshaller from null JAXBContext");
        }
    }
    
    /*
     * Setup the Unmarshaller for deserialization  
     */
    private void createUnmarshaller() {
        if (unmarshaller != null)
            return;
        
        if (context != null) {
            try {
                unmarshaller = context.createUnmarshaller();
            } catch (JAXBException e) {
                throw new WebServiceException("Error creating Unmarshaller from JAXBContext");
            }
        }
        else {
            throw new WebServiceException("Cannot create Unmarshaller from null JAXBContext");
        }
    }
}
