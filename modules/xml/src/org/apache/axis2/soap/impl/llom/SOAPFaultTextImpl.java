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

package org.apache.axis2.soap.impl.llom;

import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMXMLParserWrapper;
import org.apache.axis2.om.impl.llom.OMAttributeImpl;
import org.apache.axis2.soap.SOAP12Constants;
import org.apache.axis2.soap.SOAPFaultReason;
import org.apache.axis2.soap.SOAPFaultText;
import org.apache.axis2.soap.SOAPProcessingException;

import javax.xml.namespace.QName;

public abstract class SOAPFaultTextImpl extends SOAPElement implements SOAPFaultText {
    protected OMAttribute langAttr;
    protected OMNamespace langNamespace = OMAbstractFactory.getOMFactory()
            .createOMNamespace(
                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_URI,
                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX);

    protected SOAPFaultTextImpl(SOAPFaultReason parent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, true);
    }

    protected SOAPFaultTextImpl(SOAPFaultReason parent,
                                OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, builder);
    }


    public void setLang(String lang) {
        //langAttr = new OMAttributeImpl(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME, parent.getNamespace(), lang);
        langAttr =
                new OMAttributeImpl(
                        SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                        langNamespace,
                        lang);
        this.addAttribute(langAttr);
    }

    public String getLang() {
        if (langAttr == null) {
            //langAttr = this.getFirstAttribute(new QName(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME, parent.getNamespace().getName()));
            langAttr =
                    this.getAttribute(
                            new QName(langNamespace.getName(),
                                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME,
                                    SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_NS_PREFIX));
        }

        return langAttr == null ? null : langAttr.getAttributeValue();
    }
}
