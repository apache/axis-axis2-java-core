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

import javax.xml.soap.SOAPConstants;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMNamespace;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class SOAPEnvelope {
    private SOAPConstants soapcostants;
    private OMElement envelope;
    
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
            throw new AxisFault("the Envelope got to be Name spache qualified");
        }
    }
    
    /**
     * @return Returns the omelement.
     */
    public OMElement getOmelement() {
        return envelope;
    }
    /**
     * @param omelement The omelement to set.
     */
    public void setOmelement(OMElement omelement) {
        this.envelope = omelement;
    }
    /**
     * @return Returns the soapcostants.
     */
    public SOAPConstants getSoapcostants() {
        return soapcostants;
    }
    /**
     * @param soapcostants The soapcostants to set.
     */
    public void setSoapcostants(SOAPConstants soapcostants) {
        this.soapcostants = soapcostants;
    }
}
