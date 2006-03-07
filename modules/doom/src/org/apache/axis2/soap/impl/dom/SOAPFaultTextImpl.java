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

package org.apache.axis2.soap.impl.dom;

import javax.xml.namespace.QName;

import org.apache.axis2.om.impl.dom.AttrImpl;
import org.apache.ws.commons.om.OMAttribute;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.om.OMXMLParserWrapper;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPFaultReason;
import org.apache.ws.commons.soap.SOAPFaultText;
import org.apache.ws.commons.soap.SOAPProcessingException;

public abstract class SOAPFaultTextImpl extends SOAPElement implements SOAPFaultText {
    
    protected OMAttribute langAttr;
    
    protected OMNamespace langNamespace = null;

    protected SOAPFaultTextImpl(SOAPFaultReason parent, SOAPFactory factory)
            throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, true, factory);
        this.langNamespace = factory.createOMNamespace(
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI,
                SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX);
    }

    protected SOAPFaultTextImpl(SOAPFaultReason parent,
                                OMXMLParserWrapper builder, SOAPFactory factory) {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, builder,
                factory);
    }


    public void setLang(String lang) {
        langAttr =
                new AttrImpl(this.ownerNode, 
                        SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                        langNamespace,
                        lang, this.factory);
        this.addAttribute(langAttr);
    }

    public String getLang() {
        if (langAttr == null) {
            langAttr =
                    this.getAttribute(
                            new QName(langNamespace.getName(),
                                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        }

        return langAttr == null ? null : langAttr.getAttributeValue();
    }
}
