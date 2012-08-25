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
import org.apache.axis2.description.AxisService2WSDL20;

public class WSDL20SupplierTemplate  extends AxisService2WSDL20  implements WSDLSupplier{
    
    public final void init(AxisService service) {
        super.axisService = service;
        super.serviceName = service.getName();
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
  

    public OMElement generateOM() throws Exception {

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement descriptionElement = generateDescription(omFactory);

        // Add the documentation element
        OMElement documentation = customizeDocumentation(generateDocumentation(omFactory));
        if (documentation != null) {
            descriptionElement.addChild(documentation);
        }

        OMElement types = customizeTypes(generateTypes(omFactory));
        if (types != null) {
            descriptionElement.addChild(types);
        }

        OMElement interfaces = customizeInterface(generateInterface(omFactory));
        if (interfaces != null) {
            descriptionElement.addChild(interfaces);
        }

        customizeService(generateService(omFactory, descriptionElement, isDisableREST(), isDisableSOAP12(),
                isDisableSOAP11()));

        addPoliciesToDescriptionElement(getPoliciesInDefinitions(),
                descriptionElement);

        return descriptionElement;
    }  


  
 
    protected OMElement customizeDocumentation(OMElement documentation) {
        return documentation;
    }

    protected OMElement customizeTypes(OMElement types) {
        return types;
    }


    protected OMElement customizeInterface(OMElement portType) {
        return portType;
    }

    protected final OMElement customizeService(OMElement service) {
        return service;
    }

    protected OMElement customizeEndpoint(OMElement port) {
        return port;
    }

    protected OMElement customizeBinding(OMElement binding) {
        return binding;
    }

    /**
     * This method use by AxisService2WSDL11 and users should not touch this
     * method.
     */   
    protected final OMElement modifyEndpoint(OMElement endpoint) {
        return customizeEndpoint(endpoint);
    }

    /**
     * This method use by AxisService2WSDL11 and users should not touch this
     * method.
     */    
    protected final OMElement modifyBinding(OMElement binding) {
        return customizeBinding(binding);
    }

  

}
