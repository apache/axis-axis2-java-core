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

package org.apache.axis2.policy.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.PolicyComponent;

/**
 * Assertion to pick up the QName 
 * <wsoma:OptimizedMimeSerialization xmlns:wsoma="http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization"/>
 * 
 * */
public class MTOMAssertion implements Assertion{
	
	private boolean isOptional = false;
	
	public final static String NS = "http://schemas.xmlsoap.org/ws/2004/09/policy/optimizedmimeserialization";

	public final static String MTOM_SERIALIZATION_CONFIG_LN = "OptimizedMimeSerialization";
	public final static String PREFIX = "wsoma";
	  
    public QName getName()
    {
    	return new QName(NS, MTOM_SERIALIZATION_CONFIG_LN);
    }
    
    public short getType() {
        return Constants.TYPE_ASSERTION;
    }
    
    public boolean equal(PolicyComponent policyComponent) {
        throw new UnsupportedOperationException("TODO");
    }
    
    
    public boolean isOptional()
    {
    	
    	return isOptional;
    }
    
    public void setOptional(boolean isOptional) {
        this.isOptional = isOptional;
    }
    
    public void serialize(XMLStreamWriter writer) throws XMLStreamException
    {
    	  String prefix = writer.getPrefix(NS);

          if (prefix == null) {
              prefix = PREFIX;
              writer.setPrefix(PREFIX, NS);
          }

          writer.writeStartElement(PREFIX, MTOM_SERIALIZATION_CONFIG_LN, NS);
          
          if(isOptional)
        		writer.writeAttribute("Optional", "true");
          
	      writer.writeNamespace(PREFIX, NS);
	      writer.writeEndElement();
          
    }
    
    
    public PolicyComponent normalize()
    {
    	 throw new UnsupportedOperationException("TODO");
    }
        
       
   
    
}
	