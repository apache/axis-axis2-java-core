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

package org.apache.axis2.dataretrieval;

import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisService2WSDL11;

public abstract class WSDL11SupplierTemplate extends AxisService2WSDL11 implements WSDLSupplier {

    public final void init(AxisService service) {
        super.axisService = service;
        this.serviceName = service.getName();
        try {
            super.init();
        } catch (AxisFault e) {
            e.printStackTrace();
        }

    }

    public Object getWSDL(AxisService service) throws AxisFault {
        try {
            return generateOM();
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }

    }

    @Override
    public final OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement definition = generateDefinition(fac);

        OMElement documentation = customizeDocumentation(generateDocumentation(fac));
        if (documentation != null) {
            definition.addChild(documentation);
        }

        OMElement types = customizeTypes(generateTypes(fac));
        if (types != null) {
            definition.addChild(types);
        }

        List<OMElement> messages = customizeMessages(generateMessages(fac));
        for (OMElement message : messages) {
            if (message != null) {
                definition.addChild(message);
            }
        }

        OMElement portType = customizePortType(generatePortType(fac));
        definition.addChild(portType);

        if (!isDisableSOAP11()) {
            definition.addChild(portType);
        }

        customizeService(generateService(fac, definition, isDisableREST(), isDisableSOAP12(),
                isDisableSOAP11()));
        addPoliciesToDefinitionElement(getPoliciesInDefinitions().values().iterator(), definition);
        return definition;
    }

    protected OMElement customizeDocumentation(OMElement documentation) {
        return documentation;
    }

    protected OMElement customizeTypes(OMElement types) {
        return types;
    }

    protected List<OMElement> customizeMessages(List<OMElement> messages) {
        return messages;
    }

    protected OMElement customizePortType(OMElement portType) {
        return portType;
    }

    protected final OMElement customizeService(OMElement service) {
        return service;
    }

    protected OMElement customizePort(OMElement port) {
        return port;
    }

    protected OMElement customizeBinding(OMElement binding) {
        return binding;
    }

    /**
     * This method use by AxisService2WSDL11 and users should not touch this
     * method.
     */
    @Override
    protected final OMElement modifyPort(OMElement port) {
        return customizePort(port);
    }

    /**
     * This method use by AxisService2WSDL11 and users should not touch this
     * method.
     */
    @Override
    protected final OMElement modifyBinding(OMElement binding) {
        return customizeBinding(binding);
    }

}
