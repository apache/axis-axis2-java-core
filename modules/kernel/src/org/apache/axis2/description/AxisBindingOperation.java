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
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class AxisBindingOperation extends AxisDescription {

    private AxisOperation axisOperation;

    private QName name;

    private Map faults;

    private Map options;

    public AxisBindingOperation() {
        options = new HashMap();
        faults = new HashMap();
    }

    public AxisBindingMessage getFault(String name) {
        return (AxisBindingMessage) faults.get(name);
    }

    public void addFault(AxisBindingMessage fault) {
        this.faults.put(fault.getName(), fault);
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public AxisOperation getAxisOperation() {
        return axisOperation;
    }

    public void setAxisOperation(AxisOperation axisOperation) {
        this.axisOperation = axisOperation;
    }

    public void setProperty(String name, Object value) {
        options.put(name, value);
    }

    public Object getProperty(String name) {
        Object property = this.options.get(name);

        AxisBinding parent;
        if (property == null && (parent = (AxisBinding) this.getParent()) != null) {
            property = parent.getProperty(name);
        }

        if (property == null) {
            property = WSDL20DefaultValueHolder.getDefaultValue(name);
        }

        return property;
    }

    public Object getKey() {
        return null;
    }

    public void engageModule(AxisModule axisModule) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(String moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged() is not supported");

    }

    /**
     * Generates the bindingOperation element
     * @param tns - The targetnamespace
     * @param wsoap - The SOAP namespace (WSDL 2.0)
     * @param whttp - The HTTP namespace (WSDL 2.0)
     * @param type - Indicates whether the binding is SOAP or HTTP
     * @param nameSpaceMap - The namespacemap of the service
     * @return The generated binding element
     */
    public OMElement toWSDL20(OMNamespace wsdl, OMNamespace tns, OMNamespace wsoap, OMNamespace whttp,
                              String type,  Map nameSpaceMap) {
        String property;
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement bindingOpElement =
                omFactory.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
        bindingOpElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF,
                                                                  null, tns.getPrefix() + ":" +
                this.name.getLocalPart()));

        if (WSDL2Constants.URI_WSDL2_SOAP.equals(type) || Constants.URI_SOAP11_HTTP.equals(type) ||
                Constants.URI_SOAP12_HTTP.equals(type)) {
            // SOAP Binding specific properties
            property = (String) this.options.get(WSDL2Constants.ATTR_WSOAP_ACTION);
            if (property != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_ACTION, wsoap, property));
            }
            ArrayList soapModules = (ArrayList) this.options.get(WSDL2Constants.ATTR_WSOAP_MODULE);
            if (soapModules != null && soapModules.size() > 0) {
                WSDLSerializationUtil.addSOAPModuleElements(omFactory, soapModules, wsoap, bindingOpElement);
            }
            property = (String) this.options.get(WSDL2Constants.ATTR_WSOAP_MEP);
            if (property != null) {
                bindingOpElement.addAttribute(
                        omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_MEP, wsoap, property));
            }
        } else if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {

            // HTTP Binding specific properties
            property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION);
            if (property != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_INPUT_SERIALIZATION, whttp, property));
            }
            property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION);
            if (property != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_OUTPUT_SERIALIZATION, whttp, property));
            }
            property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_FAULT_SERIALIZATION);
            if (property != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_FAULT_SERIALIZATION, whttp, property));
            }
            Boolean ignoreUncited =
                    (Boolean) this.options.get(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED);
            if (ignoreUncited != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_IGNORE_UNCITED, whttp, ignoreUncited.toString()));
            }
            property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_METHOD);
            if (property != null) {
                bindingOpElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_METHOD, whttp, property));
            }
        }

        // Common properties
        property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_LOCATION);
        if (property != null) {
            bindingOpElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_LOCATION, whttp, property));
        }
        property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING);
        if (property != null) {
            bindingOpElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_CONTENT_ENCODING, whttp, property));
        }
        property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        if (property != null) {
            bindingOpElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_QUERY_PARAMETER_SEPERATOR, whttp, property));
        }

        // Add the input element
        AxisBindingMessage inMessage =
                (AxisBindingMessage) this.getChild(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        if (inMessage != null) {
            bindingOpElement.addChild(inMessage.toWSDL20(wsdl, tns, wsoap, whttp, nameSpaceMap));
        }

        // Add the output element
        AxisBindingMessage outMessage =
                (AxisBindingMessage) this.getChild(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        if (outMessage != null) {
            bindingOpElement.addChild(outMessage.toWSDL20(wsdl, tns, wsoap, whttp, nameSpaceMap));
        }

        // Add any fault elements
        if (faults != null && faults.size() > 0) {
            Collection faultValues = faults.values();
            Iterator iterator = faultValues.iterator();
            while (iterator.hasNext()) {
                AxisBindingMessage faultMessage = (AxisBindingMessage) iterator.next();
                bindingOpElement.addChild(faultMessage.toWSDL20(wsdl, tns, wsoap, whttp, nameSpaceMap));
            }
        }
        WSDLSerializationUtil.addWSDLDocumentationElement(this, bindingOpElement, omFactory, wsdl);
        return bindingOpElement;
    }

}
