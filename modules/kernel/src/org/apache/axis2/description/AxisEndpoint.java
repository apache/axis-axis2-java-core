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
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMAbstractFactory;

import java.util.HashMap;
import java.util.Map;

public class AxisEndpoint extends AxisDescription {

    // The name of the endpoint
    private String name;

    // The binding reffered to by the endpoint
    private AxisBinding binding;

    // The address of the endpoint
    private String endpointURL;

    // The alias used for the endpoint
    private String alias;

    private Map options;


    public String getEndpointURL() {
        return endpointURL;
    }

    public void setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    public AxisEndpoint() {
        options = new HashMap();
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

        return null;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AxisBinding getBinding() {
        return binding;
    }

    public void setBinding(AxisBinding binding) {
        this.binding = binding;
    }

    public Object getKey() {
        //ToDO
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(String moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged() is not supported");

    }

    public OMElement toWSDL20(OMNamespace wsdl, OMNamespace tns, OMNamespace whttp, String epr) {
        String property;
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement endpointElement = omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
        endpointElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, this.getName()));
        endpointElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.BINDING_LOCAL_NAME, null, tns.getPrefix() + ":" + getBinding().getName().getLocalPart()));
        endpointElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
        property = (String) this.options.get(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_TYPE);
        if (property != null) {
           endpointElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_AUTHENTICATION_TYPE, whttp, property));
        }
        property = (String)options.get(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_REALM);
        if (property != null) {
           endpointElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_AUTHENTICATION_REALM, whttp, property));
        }
        return endpointElement;
    }
}
