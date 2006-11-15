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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.WebServiceException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.w3c.dom.Document;

public class DOMSourceParameter extends SourceParameter {

    public DOMSourceParameter() {
        //do nothing
    }
    
    public DOMSourceParameter(DOMSource value){
		this.value = value;
	}
	
	/**
     * This is a temporary implementation to get around a bug
     * in the Woodstox parser.
	 */
    public XMLStreamReader getValueAsStreamReader() {
        if (value != null) {
            try {
                //TODO: This is not the correct way to do this and we
                //should be able to use the method implemetation from the super class.
                //There is a bug in the Woodstox parser that causes the super's impl to
                //hang.  We need this to be able to support DOMSource until we have a
                //fix for that bug.
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Result result = new StreamResult(out);
                Transformer transformer =  TransformerFactory.newInstance().newTransformer();
                transformer.transform(value, result);
                
                ByteArrayInputStream bytes = new ByteArrayInputStream(out.toByteArray());
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(bytes);
                return reader;
            } catch (Exception e) {
                //TODO: Add proper error handling
                e.printStackTrace();
            }            
        }
        else {
            throw new WebServiceException("Cannot create XMLStreamReader from null value");
        }
        
        return null;
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
		if(omElement != null){
            try{
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                omElement.serialize(output);
                InputStream input = new ByteArrayInputStream(output.toByteArray());
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
                
                Document domTree = domBuilder.parse(input);
                value = new DOMSource(domTree);
            }catch(Exception e){
                throw new WebServiceException(e.getMessage());
            }
		}
	}
}
