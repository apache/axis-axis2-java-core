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
package org.apache.axis2.jaxws.message.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMDocument;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;


/**
 * Miscellaneous Utilities that may be useful inside and outside the Message
 * subcomponent.
 */
public class MessageUtils {
    
    /**
     * Get an axiom SOAPFactory for the specified element
     * @param e OMElement
     * @return SOAPFactory
     */
    public static SOAPFactory getSOAPFactory(OMElement e) {
        // Getting a factory from a SOAPEnvelope is not straight-forward.
        // Please change this code if an easier mechanism is discovered.
        
        OMXMLParserWrapper builder = e.getBuilder();
        if (builder instanceof StAXBuilder) {
            StAXBuilder staxBuilder = (StAXBuilder) builder;
            OMDocument document = staxBuilder.getDocument();
            if (document != null) {
                OMFactory factory = document.getOMFactory();
                if (factory instanceof SOAPFactory) {
                    return (SOAPFactory) factory;
                }
            }
        }
        // Flow to here indicates that the envelope does not have
        // an accessible factory.  Create a new factory based on the 
        // protocol.
        
        while (e != null && !(e instanceof SOAPEnvelope)) {
            e = (OMElement) e.getParent();
        }
        if (e instanceof SOAPEnvelope) {
            if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.
                    equals(e.getNamespace().getNamespaceURI())) {
                return OMAbstractFactory.getSOAP11Factory();
            } else {
                return OMAbstractFactory.getSOAP11Factory();
            }
        }
        return null;
    }
}
