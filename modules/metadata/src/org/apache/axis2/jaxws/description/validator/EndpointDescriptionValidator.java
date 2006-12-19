/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.validator;

import javax.wsdl.Port;
import javax.wsdl.Service;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;

/**
 * 
 */
public class EndpointDescriptionValidator extends Validator {
    EndpointDescription endpointDesc;
    EndpointDescriptionJava endpointDescJava;
    EndpointDescriptionWSDL endpointDescWSDL;
    
    public EndpointDescriptionValidator(EndpointDescription toValidate) {
        endpointDesc = toValidate;
        endpointDescJava = (EndpointDescriptionJava) endpointDesc;
        endpointDescWSDL = (EndpointDescriptionWSDL) endpointDesc;
    }
    
    public boolean validate() {

        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }

        if (!validateWSDLPort()) {
            return INVALID;
        }
        
        if (!validateWSDLBindingType()) {
            return INVALID;
        }
        
        if (!validateEndpointInterface()) {
            return INVALID;
        }
        return VALID;
    }

    private boolean validateWSDLBindingType() {
        // TODO: The getWSDLBindingType is not getting the correct value; it needs to return the
        //       <soap:binding transport> value
        if (true) return VALID;
        String bindingType = endpointDesc.getBindingType();
        String wsdlBindingType = endpointDescWSDL.getWSDLBindingType();
        if (wsdlBindingType != null && !wsdlBindingType.equals(bindingType)) {
            addValidationFailure(this, "Binding type is invald");
            return INVALID;
        }
        return VALID;
    }

    private boolean validateWSDLPort() {
        // VALIDATION: If the service is specified in the WSDL, then the port must also be specified.
        //             If the service is NOT in the WSDL, then this is "partial wsdl" and there is nothing to validate
        //             against the WSDL
        Service wsdlService = endpointDescWSDL.getWSDLService();
        if (wsdlService != null) {
            Port wsdlPort = endpointDescWSDL.getWSDLPort();
            if (wsdlPort == null) {
                addValidationFailure(this, "Serivce exists in WSDL, but Port does not.  Not a valid Partial WSDL");
                return INVALID;
            }
        }
        return VALID;
    }
    
    private boolean validateEndpointInterface() {
        EndpointInterfaceDescription eid = endpointDesc.getEndpointInterfaceDescription();
        if (eid != null) {
            EndpointInterfaceDescriptionValidator eidValidator = new EndpointInterfaceDescriptionValidator(eid);
            boolean isEndpointInterfaceValid = eidValidator.validate();
            if (!isEndpointInterfaceValid) {
                addValidationFailure(eidValidator, "Invalid Endpoint Interface");
                return INVALID;
            }
        }
        return VALID;
    }
}
