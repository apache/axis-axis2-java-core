/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
package org.apache.axis.soap;

import javax.xml.namespace.QName;

import org.apache.axis.Constants;

/**
 * SOAP 1.1 constants
 *
 * @author Glen Daniels (gdaniels@apache.org)
 * @author Andras Avar (andras.avar@nokia.com)
 */
public class SOAP11Constants implements SOAPConstants {
    private static QName headerQName = new QName(Constants.URI_SOAP11_ENV,
                                                 Constants.ELEM_HEADER);
    private static QName bodyQName = new QName(Constants.URI_SOAP11_ENV,
                                               Constants.ELEM_BODY);
    private static QName faultQName = new QName(Constants.URI_SOAP11_ENV,
                                                Constants.ELEM_FAULT);
    private static QName roleQName = new QName(Constants.URI_SOAP11_ENV,
                                                Constants.ATTR_ACTOR);

    public String getEnvelopeURI() {
        return Constants.URI_SOAP11_ENV;
    }

    public String getEncodingURI() {
        return Constants.URI_SOAP11_ENC;
    }

    public QName getHeaderQName() {
        return headerQName;
    }

    public QName getBodyQName() {
        return bodyQName;
    }

    public QName getFaultQName() {
        return faultQName;
    }

    /**
     * Obtain the QName for the role attribute (actor/role)
     */
    public QName getRoleAttributeQName() {
        return roleQName;
    }

    /**
     * Obtain the MIME content type
     */
    public String getContentType() {
        return "text/xml";
    }

    /**
     * Obtain the "next" role/actor URI
     */
    public String getNextRoleURI() {
        return Constants.URI_SOAP11_NEXT_ACTOR;
    }

    /**
     * Obtain the ref attribute name
     */
    public String getAttrHref() {
        return Constants.ATTR_HREF;
    }

    /**
     * Obtain the item type name of an array
     */
    public String getAttrItemType() {
        return Constants.ATTR_ARRAY_TYPE;
    }

    /**
     * Obtain the Qname of VersionMismatch fault code
     */
    public QName getVerMismatchFaultCodeQName() {
        return Constants.FAULT_VERSIONMISMATCH;
    }

    /**
     * Obtain the Qname of Mustunderstand fault code
     */
    public QName getMustunderstandFaultQName() {
        return Constants.FAULT_MUSTUNDERSTAND;
    }

    /**
     * Obtain the QName of the SOAP array type
     */
    public QName getArrayType() {
        return Constants.SOAP_ARRAY;
    }
}
