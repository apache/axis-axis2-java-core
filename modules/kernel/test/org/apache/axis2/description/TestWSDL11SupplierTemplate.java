/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.description;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.dataretrieval.WSDL11SupplierTemplate;

class TestWSDL11SupplierTemplate extends WSDL11SupplierTemplate {

    @Override
    protected OMElement customizeDocumentation(OMElement documentation) {
        OMFactory fac = documentation.getOMFactory();
        OMElement details = fac.createOMElement("detail", "http://axis.apache.org", "ap");
        OMElement name = fac.createOMElement("name", "http://axis.apache.org", "ap");
        name.setText("Apache Axis2");
        OMElement mail = fac.createOMElement("email", "http://axis.apache.org", "ap");
        mail.setText("user@axis.apache.org");
        details.addChild(name);
        details.addChild(mail);
        documentation.addChild(details);
        OMElement doc = documentation.getFirstChildWithName(new QName("documentation"));
        doc.detach();
        return documentation;
    }

     @Override
     protected OMElement customizePort(OMElement service) {
     OMElement address = service.getFirstElement();
     OMAttribute location = address.getAttribute(new QName("location"));
     String url = location.getAttributeValue();
     url = url.replace("192.168.1.3", "localhost");
     location.setAttributeValue(url);
     return service;
     }

}