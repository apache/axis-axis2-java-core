package org.apache.axis.impl.llom;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.om.*;

import javax.xml.namespace.QName;
import java.util.Locale;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * User: Eran Chinthaka - Lanka Software Foundation
 * Date: Dec 8, 2004
 * Time: 2:02:25 PM
 */
public class SOAPFaultImpl extends OMElementImpl implements SOAPFault, OMConstants {

    private Exception e;

    public SOAPFaultImpl(OMElement parent, Exception e) {
        super(parent);
        this.e = e;
        this.addChild(OMFactory.newInstance().createText(this, e.getMessage()));
        localName = SOAPFAULT_LOCAL_NAME;
        setNamespace(new OMNamespaceImpl(SOAPFAULT_NAMESPACE_URI, SOAPFAULT_NAMESPACE_PREFIX));
    }

//    public SOAPFaultImpl(OMElement parent) {
//        super(parent);
//
//    }

    public SOAPFaultImpl(OMNamespace ns, OMElement parent, OMXMLParserWrapper builder) {
        super(SOAPFAULT_LOCAL_NAME, ns, parent, builder);
    }

    public void setFaultCode(QName faultCode) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public QName getFaultCode() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public void setFaultActor(String faultActor) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public String getFaultActor() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public void setFaultString(String faultString) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public String getFaultString() {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public void setFaultString(String faultString, Locale locale) throws OMException {
        throw new UnsupportedOperationException(); //TODO implement this
    }

    public Exception getException() throws OMException {
        if (e == null) {
            return new AxisFault();
        } else {
            return e;
        }
    }
}
