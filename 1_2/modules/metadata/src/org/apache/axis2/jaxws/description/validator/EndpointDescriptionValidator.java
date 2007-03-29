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
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

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
        boolean isBindingValid = false;
        String bindingType = endpointDesc.getBindingType();
        String wsdlBindingType = endpointDescWSDL.getWSDLBindingType();
        if (bindingType == null) {
            // I don't think this can happen; the Description layer should provide a default
            addValidationFailure(this, "Annotation binding type is null and did not have a default");
            isBindingValid = false;
        }
        // Validate that the annotation value specified is valid.
        else if (!SOAPBinding.SOAP11HTTP_BINDING.equals(bindingType) &&
                 !SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(bindingType) &&
                 !SOAPBinding.SOAP12HTTP_BINDING.equals(bindingType) &&
                 !SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingType) &&
                 !HTTPBinding.HTTP_BINDING.equals(bindingType)) {
            addValidationFailure(this, "Invalid annotation binding value specified: " + bindingType);
            isBindingValid = false;
        }
        // If there's no WSDL, then there will be no WSDL Binding Type to validate against
        else if (wsdlBindingType == null) {
            isBindingValid = true;
        }
        // Validate that the WSDL value is valid
        else if (!EndpointDescriptionWSDL.SOAP11_WSDL_BINDING.equals(wsdlBindingType) &&
                 !EndpointDescriptionWSDL.SOAP12_WSDL_BINDING.equals(wsdlBindingType) &&
                 !EndpointDescriptionWSDL.HTTP_WSDL_BINDING.equals(wsdlBindingType)) {
            addValidationFailure(this, "Invalid wsdl binding value specified: " + wsdlBindingType);
            isBindingValid = false;
        }
        // Validate that the WSDL and annotations values indicate the same type of binding
        else if (wsdlBindingType.equals(EndpointDescriptionWSDL.SOAP11_WSDL_BINDING)
                 && (bindingType.equals(SOAPBinding.SOAP11HTTP_BINDING) || 
                     bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING))) {
            isBindingValid = true;
        }
        else if (wsdlBindingType.equals(EndpointDescriptionWSDL.SOAP12_WSDL_BINDING)
                 && (bindingType.equals(SOAPBinding.SOAP12HTTP_BINDING) ||
                     bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING))) {
            isBindingValid = true;
        }
        else if (wsdlBindingType.equals(EndpointDescriptionWSDL.HTTP_WSDL_BINDING) &&
                 bindingType.equals(HTTPBinding.HTTP_BINDING)) {
            isBindingValid = true;
        }
        // The HTTP binding is not valid on a Java Bean SEI-based endpoint; only on a Provider based one.
        else if (wsdlBindingType.equals(EndpointDescriptionWSDL.HTTP_WSDL_BINDING) &&
                 endpointDesc.isEndpointBased()) {
            addValidationFailure(this, "The HTTPBinding can not be specified for SEI-based endpoints");
            isBindingValid = false;
        }
        else {
            addValidationFailure(this, "Invalid binding; wsdl = " + wsdlBindingType + ", annotation = " + bindingType);
            isBindingValid = false;
        }
        return isBindingValid;
    }

    private boolean validateWSDLPort() {
        // VALIDATION: If the service is specified in the WSDL, then the port must also be specified.
        //             If the service is NOT in the WSDL, then this is "partial wsdl" and there is nothing to validate
        //             against the WSDL
        Service wsdlService = endpointDescWSDL.getWSDLService();
        if (wsdlService != null) {
            Port wsdlPort = endpointDescWSDL.getWSDLPort();
            if (wsdlPort == null) {
                addValidationFailure(this, "Serivce exists in WSDL, but Port does not.  Not a valid Partial WSDL.  Service: " 
                        + endpointDesc.getServiceQName() + "; Port: " + endpointDesc.getPortQName());
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
