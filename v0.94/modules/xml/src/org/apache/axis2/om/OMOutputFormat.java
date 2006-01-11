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

package org.apache.axis2.om;

import org.apache.axis2.om.impl.MIMEOutputUtils;
import org.apache.axis2.soap.SOAP11Constants;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.util.UUIDGenerator;


/**
 * Formats options for OM Output.
 */
public class OMOutputFormat {
    private String mimeBoundary = null;
    private String rootContentId = null;
    private int nextid = 0;
    private boolean doOptimize;
    private boolean isSoap11 = true;

    /**
     * Field DEFAULT_CHAR_SET_ENCODING. Specifies the default
     * character encoding scheme to be used.
     */
    public static final String DEFAULT_CHAR_SET_ENCODING = "utf-8";

    private String charSetEncoding;
    private String xmlVersion;
    private boolean ignoreXMLDeclaration = false;


    public OMOutputFormat() {
    }

    public boolean isOptimized() {
        return doOptimize;
    }

    public String getContentType() {
        String SOAPContentType;
        if (isOptimized()) {
            if (isSoap11) {
                SOAPContentType = SOAP11Constants.SOAP_11_CONTENT_TYPE;
            } else {
                SOAPContentType = SOAP12Constants.SOAP_12_CONTENT_TYPE;
            }
            return MIMEOutputUtils.getContentTypeForMime(
                    getMimeBoundary(),
                    getRootContentId(),
                    this.getCharSetEncoding(), SOAPContentType);
        } else {
            if (!isSoap11) {
                return SOAP12Constants.SOAP_12_CONTENT_TYPE;
            } else {
                return SOAP11Constants.SOAP_11_CONTENT_TYPE;
            }
        }
    }

    public String getMimeBoundary() {
        if (mimeBoundary == null) {
            mimeBoundary =
                    "MIMEBoundary"
                            + UUIDGenerator.getUUID();
        }
        return mimeBoundary;
    }

    public String getRootContentId() {
        if (rootContentId == null) {
            rootContentId =
                    "0."
                            + UUIDGenerator.getUUID()
                            + "@apache.org";
        }
        return rootContentId;
    }

    public String getNextContentId() {
        nextid++;
        return nextid
                + "."
                + UUIDGenerator.getUUID()
                + "@apache.org";
    }

    /**
     * Returns the character set encoding scheme. If the value of the
     * charSetEncoding is not set then the default will be returned.
     *
     * @return Returns encoding string.
     */
    public String getCharSetEncoding() {
        return this.charSetEncoding;
    }

    public void setCharSetEncoding(String charSetEncoding) {
        this.charSetEncoding = charSetEncoding;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public void setSOAP11(boolean b) {
        isSoap11 = b;
    }
    
    public boolean isSOAP11() {
        return isSoap11;
    }

    public boolean isIgnoreXMLDeclaration() {
        return ignoreXMLDeclaration;
    }

    public void setIgnoreXMLDeclaration(boolean ignoreXMLDeclaration) {
        this.ignoreXMLDeclaration = ignoreXMLDeclaration;
    }

    public void setDoOptimize(boolean b) {
        doOptimize = b;
    }
}
