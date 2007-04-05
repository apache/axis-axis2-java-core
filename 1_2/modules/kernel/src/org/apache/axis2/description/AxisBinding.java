/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAP11Constants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

public class AxisBinding extends AxisDescription {

    private QName name;

    private String type;

    private Map options;

    private Map faults;

    public AxisBindingMessage getFault(String name) {
        return (AxisBindingMessage) faults.get(name);
    }

    public void addFault(AxisBindingMessage fault) {
        this.faults.put(fault.getName(), fault);
    }

    public AxisBinding() {
        options = new HashMap();
        faults = new HashMap();
    }


    public void setProperty(String name, Object value) {
        options.put(name, value);
    }

    /**
     * @param name name of the property to search for
     * @return the value of the property, or null if the property is not found
     */
    public Object getProperty(String name) {
        Object obj = options.get(name);
        if (obj != null) {
            return obj;
        }

        obj = WSDL20DefaultValueHolder.getDefaultValue(name);

        return obj;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getKey() {
        return null;
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(String moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged() is not supported");

    }

    /**
     * Generates the binding element
     * @param tns - The targetnamespace
     * @param wsoap - The SOAP namespace (WSDL 2.0)
     * @param whttp - The HTTP namespace (WSDL 2.0)
     * @param interfaceName - The name of the interface
     * @param nameSpaceMap - The namespacemap of the service
     * @return The generated binding element
     */
    public OMElement toWSDL20(OMNamespace tns, OMNamespace wsoap, OMNamespace whttp,
                              String interfaceName,  Map nameSpaceMap) {
        String property;
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement bindingElement;
        bindingElement = omFactory.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, null);
        bindingElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                                                this.name.getLocalPart()));
        bindingElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null,
                                                                tns.getPrefix() + ":" + interfaceName));

        if (WSDL2Constants.URI_WSDL2_SOAP.equals(type) || Constants.URI_SOAP11_HTTP.equals(type) ||
                Constants.URI_SOAP12_HTTP.equals(type)) {
            // SOAP Binding specific properties
            property = (String) options.get(WSDL2Constants.ATTR_WSOAP_VERSION);
            if (property != null) {
                if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(property)) {
                    bindingElement.addAttribute(omFactory.createOMAttribute(
                            WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                            WSDL2Constants.SOAP_VERSION_1_1));
                } else {
                    bindingElement.addAttribute(omFactory.createOMAttribute(
                            WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                            WSDL2Constants.SOAP_VERSION_1_2));
                }
            }
            property = (String) options.get(WSDL2Constants.ATTR_WSOAP_PROTOCOL);
            if (property != null) {
                bindingElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_PROTOCOL, wsoap, property));
            }
            property = (String) options.get(WSDL2Constants.ATTR_WSOAP_MEP);
            if (property != null) {
                bindingElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_MEP_DEFAULT, wsoap, property));
            }
            ArrayList soapModules = (ArrayList) options.get(WSDL2Constants.ATTR_WSOAP_MODULE);
            if (soapModules != null && soapModules.size() > 0) {
                WSDLSerializationUtil.addSOAPModuleElements(omFactory, soapModules, wsoap, bindingElement);
            }
        } else if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {
            // HTTP Binding specific properties
            property = (String) options.get(WSDL2Constants.ATTR_WHTTP_METHOD);
            if (property != null) {
                bindingElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_METHOD_DEFAULT, whttp, property));
            }
        }

        // Common Properties
        property = getType();
        if (property != null) {
            bindingElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null, property));
        }
        property = (String) options.get(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING);
        if (property != null) {
            bindingElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_CONTENT_ENCODING_DEFAULT, whttp, property));
        }
        property = (String) options.get(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        if (property != null) {
            bindingElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_QUERY_PARAMETER_SEPERATOR_DEFAULT, whttp, property));
        }

        // Populate Binding faults
        if (faults != null) {
            Iterator iterator = faults.values().iterator();
            while (iterator.hasNext()) {
                AxisBindingMessage axisBindingFault = (AxisBindingMessage) iterator.next();
                bindingElement.addChild(axisBindingFault.toWSDL20(tns, wsoap, whttp, nameSpaceMap));
            }
        }

        // Populate Binding Operations
        Iterator iterator = this.getChildren();
        while (iterator.hasNext()) {
            AxisBindingOperation axisBindingOperation = (AxisBindingOperation) iterator.next();
            bindingElement.addChild(axisBindingOperation.toWSDL20(tns, wsoap, whttp, type, nameSpaceMap));
        }
        return bindingElement;
    }
}
