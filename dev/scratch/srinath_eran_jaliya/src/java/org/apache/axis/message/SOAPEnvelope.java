/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.message;

import java.util.Iterator;

import javax.xml.soap.SOAPConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;
import org.apache.axis.om.OMNode;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class SOAPEnvelope {
    private SOAPConstants soapcostants;
    private OMElement envelope;
    private SOAPHeaders headers;
    private OMElement body;
    
    public SOAPEnvelope(OMElement envelope) throws AxisFault{
        this.envelope = envelope;
        String localName = envelope.getLocalName();
        if (!localName.equals(Constants.ELEM_ENVELOPE))
            throw new AxisFault(Constants.ELEM_ENVELOPE + " Tag not found ... not a SOAP message");
        
        OMNamespace omns = envelope.getNamespace();
        if(omns != null){
            String namespace = omns.getValue();
            if(namespace != null){
                    if (namespace.equals(Constants.URI_SOAP11_ENV)) { // SOAP 1.1
                        //TODO deal with the versions 
               } else if (namespace.equals(Constants.URI_SOAP12_ENV)) { // SOAP 1.2
                   //TODO deal with the versions
               } else {
                   throw new AxisFault("Unknown SOAP version");
               }
            }
        }else{
            throw new AxisFault("the Envelope got to be Name space qualified");
        }
        Iterator childeren = this.envelope.getChildren();
        
        OMElement omele = null;
        
        while(childeren.hasNext()){
            OMNode node = (OMNode)childeren.next();
            if(node.getType() == OMNode.ELEMENT_NODE){
                omele = (OMElement)node;
                if(Constants.ELEM_HEADER.equals(omele.getLocalName())){
                    if(body != null){
                        throw new AxisFault("Body can never come before the Haders ");
                    }
                    if(headers != null){
                        throw new AxisFault("Only one Header block allowed");
                    }
                    headers =  new SOAPHeaders(omele);
                }else if(Constants.ELEM_BODY.equals(omele.getLocalName())){
                    if(headers != null){
                        throw new AxisFault("Only one Body block allowed");
                    }
                    body = omele;
                }else{
                    throw new AxisFault("Only Body and Header allowed");
                }
            } 
        }
    }
    
    public SOAPConstants getSoapcostants() {
        return soapcostants;
    }
    /**
     * @param soapcostants The soapcostants to set.
     */
    public void setSoapcostants(SOAPConstants soapcostants) {
        this.soapcostants = soapcostants;
    }
    /**
     * @return Returns the body.
     */
    public OMElement getBody() {
        return body;
    }
    /**
     * @param body The body to set.
     */
    public void setBody(OMElement body) {
        this.body = body;
    }
    /**
     * @return Returns the headers.
     */
    public SOAPHeaders getHeaders() {
        return headers;
    }
    /**
     * @param headers The headers to set.
     */
    public void setHeaders(SOAPHeaders headers) {
        this.headers = headers;
    }
}
