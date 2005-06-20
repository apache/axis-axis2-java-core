package org.apache.axis.soap.impl.llom;

import org.apache.axis.om.OMElement;
import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.impl.llom.OMElementImpl;
import org.apache.axis.om.impl.llom.OMAttributeImpl;
import org.apache.axis.soap.SOAPFaultText;
import org.apache.axis.soap.SOAPFaultReason;
import org.apache.axis.soap.impl.llom.soap12.SOAP12Constants;

import javax.xml.namespace.QName;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * <p/>
 */
public abstract class SOAPFaultTextImpl extends SOAPElement implements SOAPFaultText {
    protected OMAttribute langAttr;

    protected SOAPFaultTextImpl(SOAPFaultReason parent) throws SOAPProcessingException {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, true);
    }

    protected SOAPFaultTextImpl(SOAPFaultReason parent, OMXMLParserWrapper builder) {
        super(parent, SOAP12Constants.SOAP_FAULT_TEXT_LOCAL_NAME, builder);
    }


    public void setLang(String lang) {
        langAttr = new OMAttributeImpl(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME, parent.getNamespace(), lang);
        this.addAttribute(langAttr);
    }

    public String getLang() {
        if (langAttr == null) {
            langAttr = this.getFirstAttribute(new QName(SOAP12Constants.SOAP_FAULT_TEXT_LANG_ATTR_LOCAL_NAME, parent.getNamespace().getName()));
        }

        return langAttr.getValue();
    }
}
