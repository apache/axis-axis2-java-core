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
package org.apache.axis2.jaxws.message;

import javax.xml.namespace.QName;

import org.apache.axiom.soap.SOAP12Constants;

/**
 * Agnostic representation of SOAP 1.1 and SOAP 1.2 fault code values.
 * @see XMLFault
 */
public enum XMLFaultCode {
                            // Rendered as qnames with the following local names
                            //     (the namespace is the corresponding envelope namespace)
    SENDER,                 // SOAP 1.2 Sender                SOAP 1.1  Client
    RECEIVER,               // SOAP 1.2 Receiver              SOAP 1.1  Server
    MUSTUNDERSTAND,         // SOAP 1.2 MustUnderstand        SOAP 1.1  MustUnderstand
    DATAENCODINGUNKNOWN,    // SOAP 1.2 DataEncodingUnknown   SOAP 1.1  Server
    VERSIONMISMATCH ;       // SOAP 1.2 VersionMismatch       SOAP 1.1  VersionMismatch
    
    // Utility Methods
    
    /**
     * Return QName for the given protocol
     * @param namespace of the envelope for the protocol
     * @return
     */
    public QName toQName(String namespace) {
        String localPart = null;
        if (namespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            // SOAP 1.2
            switch(this) {
            case SENDER:
                localPart = "Sender";
                break;
            case RECEIVER:
                localPart = "Receiver";
                break;
            case MUSTUNDERSTAND:
                localPart = "MustUnderstand";
                break;
            case DATAENCODINGUNKNOWN:
                localPart = "DataEncodingUnknown";
                break;
            case VERSIONMISMATCH:
                localPart = "VersionMismatch";
                break;
            }
            
        } else {
            // Assume SOAP 1.1
            switch(this) {
            case SENDER:
                localPart = "Client";
                break;
            case RECEIVER:
                localPart = "Server";
                break;
            case MUSTUNDERSTAND:
                localPart = "MustUnderstand";
                break;
            case DATAENCODINGUNKNOWN:
                localPart = "Server";
                break;
            case VERSIONMISMATCH:
                localPart = "VersionMismatch";
                break;
            }
        }
        return new QName(namespace, localPart);
    }
    
    /**
     * get the XMLPart corresponding to this specified QName
     * @param qName
     * @return corresponding XMLPart
     */
    public static XMLFaultCode fromQName(QName qName) {
        if (qName == null) {
            // Spec indicates that the default is receiver
            return RECEIVER;
        }
        String namespace = qName.getNamespaceURI();
        String localPart = qName.getLocalPart();
        XMLFaultCode xmlFaultCode= RECEIVER;
        // Due to problems in the OM, sometimes that qname is not retrieved correctly.
        // So use the localName to find the XMLFaultCode
        if (localPart.equalsIgnoreCase("Sender")) {          // SOAP 1.2
            xmlFaultCode = SENDER;
        } else if (localPart.equalsIgnoreCase("Receiver")) { // SOAP 1.2
            xmlFaultCode = RECEIVER;
        } else if (localPart.equalsIgnoreCase("Client")) {   // SOAP 1.1
            xmlFaultCode = SENDER;
        } else if (localPart.equalsIgnoreCase("Server")) {   // SOAP 1.1
            xmlFaultCode = RECEIVER;
        } else if (localPart.equalsIgnoreCase("MustUnderstand")) {  // Both
            xmlFaultCode = MUSTUNDERSTAND;
        } else if (localPart.equalsIgnoreCase("DataEncodingUnknown")) {  // SOAP 1.2
            xmlFaultCode = DATAENCODINGUNKNOWN;
        } else if (localPart.equalsIgnoreCase("VersionMismatch")) { // Both
            xmlFaultCode = VERSIONMISMATCH;
        }
        /*
         * TODO: Due to problems in the OM, sometimes that qname is not retrieved correctly.
        if (namespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
            // SOAP 1.2
            if (localPart.equals("Sender")) {
                xmlFaultCode = SENDER;
            } else if (localPart.equals("Receiver")) {
                xmlFaultCode = RECEIVER;
            } else if (localPart.equals("MustUnderstand")) {
                xmlFaultCode = MUSTUNDERSTAND;
            } else if (localPart.equals("DataEncodingUnknown")) {
                xmlFaultCode = DATAENCODINGUNKNOWN;
            } else if (localPart.equals("VersionMismatch")) {
                xmlFaultCode = VERSIONMISMATCH;
            }
        } else {
            // SOAP 1.1
            if (localPart.equals("Client")) {
                xmlFaultCode = SENDER;
            } else if (localPart.equals("Server")) {
                xmlFaultCode = RECEIVER;
            } else if (localPart.equals("MustUnderstand")) {
                xmlFaultCode = MUSTUNDERSTAND;
            } else if (localPart.equals("VersionMismatch")) {
                xmlFaultCode = VERSIONMISMATCH;
            }
        }
        */
        return xmlFaultCode;
    }
}
