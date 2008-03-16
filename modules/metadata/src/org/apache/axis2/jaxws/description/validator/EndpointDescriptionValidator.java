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

package org.apache.axis2.jaxws.description.validator;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.builder.MDQConstants;

import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

/**
 * 
 */
public class EndpointDescriptionValidator extends Validator {
    EndpointDescription endpointDesc;
    EndpointDescriptionJava endpointDescJava;
    EndpointDescriptionWSDL endpointDescWSDL;

    public EndpointDescriptionValidator(EndpointDescription toValidate) {
        endpointDesc = toValidate;
        endpointDescJava = (EndpointDescriptionJava)endpointDesc;
        endpointDescWSDL = (EndpointDescriptionWSDL)endpointDesc;
    }

    public boolean validate() {

        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }

        //The following phase II validation can only happen on the server side
        if (endpointDesc.getServiceDescription().isServerSide()) {
            if (!validateWSDLPort()) {
                return INVALID;
            }
            
            if (!validateWSDLBindingType()) {
                return INVALID;
            }
        }

        if (!validateEndpointInterface()) {
            return INVALID;
        }
        return VALID;
    }

    private boolean validateWSDLBindingType() {
        boolean isBindingValid = false;
        
        //Get the binding type from the annotation
        String bindingType = endpointDesc.getBindingType();
        
        //The wsdl binding type that we now receive has been previously mapped to the expected
        //SOAP and HTTP bindings. So, there is now limited validation to perform
        String wsdlBindingType = endpointDescWSDL.getWSDLBindingType();
        if (bindingType == null) {
            // I don't think this can happen; the Description layer should provide a default
            addValidationFailure(this,
                                 "Annotation binding type is null and did not have a default");
            isBindingValid = false;
        }
        // Validate that the annotation value specified is valid.
        else if (!SOAPBinding.SOAP11HTTP_BINDING.equals(bindingType) &&
                !SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(bindingType) &&
                !SOAPBinding.SOAP12HTTP_BINDING.equals(bindingType) &&
                !SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(bindingType) &&
                !MDQConstants.SOAP11JMS_BINDING.equals(bindingType) &&
                !MDQConstants.SOAP11JMS_MTOM_BINDING.equals(bindingType) &&
                !MDQConstants.SOAP12JMS_BINDING.equals(bindingType) &&
                !MDQConstants.SOAP12JMS_MTOM_BINDING.equals(bindingType) &&
                !HTTPBinding.HTTP_BINDING.equals(bindingType)) {
            
            addValidationFailure(this,
                                 "Invalid annotation binding value specified: " + bindingType);
            isBindingValid = false;
        }
        // If there's no WSDL, then there will be no WSDL Binding Type to validate against
        else if (wsdlBindingType == null) {
            isBindingValid = true;
        }
        // Validate that the WSDL value is valid
        else if (!SOAPBinding.SOAP11HTTP_BINDING.equals(wsdlBindingType)
                && !SOAPBinding.SOAP12HTTP_BINDING.equals(wsdlBindingType)
                && !javax.xml.ws.http.HTTPBinding.HTTP_BINDING.equals(wsdlBindingType)) {
            addValidationFailure(this, "Invalid wsdl binding value specified: " + wsdlBindingType);
            isBindingValid = false;
        }
        // Validate that the WSDL and annotations values indicate the same type of binding
        else if (wsdlBindingType.equals(SOAPBinding.SOAP11HTTP_BINDING)
                && (bindingType.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                bindingType.equals(SOAPBinding.SOAP11HTTP_MTOM_BINDING))) {
            isBindingValid = true;
        } else if (wsdlBindingType.equals(SOAPBinding.SOAP12HTTP_BINDING)
                && (bindingType.equals(SOAPBinding.SOAP12HTTP_BINDING) ||
                bindingType.equals(SOAPBinding.SOAP12HTTP_MTOM_BINDING))) {
            isBindingValid = true;
        } else if (wsdlBindingType.equals(HTTPBinding.HTTP_BINDING)
                 && bindingType.equals(HTTPBinding.HTTP_BINDING)) {
            isBindingValid = true;
        }
        // The HTTP binding is not valid on a Java Bean SEI-based endpoint; only on a Provider based one.
        else if (wsdlBindingType.equals(HTTPBinding.HTTP_BINDING) &&
                endpointDesc.isEndpointBased()) {
            addValidationFailure(this,
                                 "An HTTPBinding was found on an @WebService SEI based endpoint. " +
                                 "This is not supported.  " +
                                 "An HTTPBinding must use an @WebServiceProvider endpoint.");
            isBindingValid = false;
        } else {
            
            // Mismatched bindings 
            String wsdlInsert = "[" + bindingHumanReadableDescription(wsdlBindingType) + "]" +
                "namespace = {" + wsdlBindingType +"}";
            String annotationInsert = "[" + bindingHumanReadableDescription(bindingType) + "]" +
                "namespace = {" + bindingType +"}";
            
            // TODO NLS
            String message = "There is a mismatch between the wsdl and annotation information.  " +
                "Please make sure both use the same binding namespace.  " +
                "The wsdl =" + wsdlInsert + ".  " +
                "The annotation = " + annotationInsert + ".";
            addValidationFailure(this, message);
            
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
                addValidationFailure(this,
                                     "Serivce exists in WSDL, but Port does not.  Not a valid Partial WSDL.  Service: "
                                             + endpointDesc.getServiceQName() + "; Port: " +
                                             endpointDesc.getPortQName());
                return INVALID;
            }
        }
        return VALID;
    }

    private boolean validateEndpointInterface() {
        EndpointInterfaceDescription eid = endpointDesc.getEndpointInterfaceDescription();
        if (eid != null) {
            EndpointInterfaceDescriptionValidator eidValidator =
                    new EndpointInterfaceDescriptionValidator(eid);
            boolean isEndpointInterfaceValid = eidValidator.validate();
            if (!isEndpointInterfaceValid) {
                addValidationFailure(eidValidator, "Invalid Endpoint Interface");
                return INVALID;
            }
        }
        return VALID;
    }
    
    private static String bindingHumanReadableDescription(String ns) {
        if (SOAPBinding.SOAP11HTTP_BINDING.equals(ns)) {
            return "SOAP 1.1 HTTP Binding";
        } else if (SOAPBinding.SOAP11HTTP_MTOM_BINDING.equals(ns)) {
            return "SOAP 1.1 MTOM HTTP Binding";
        } else if (SOAPBinding.SOAP12HTTP_BINDING.equals(ns)) {
            return "SOAP 1.2 HTTP Binding";
        } else if (SOAPBinding.SOAP12HTTP_MTOM_BINDING.equals(ns)) {
            return "SOAP 1.2 MTOM HTTP Binding";
        } else if (MDQConstants.SOAP11JMS_BINDING.equals(ns)) {
            return "SOAP 1.1 JMS Binding";
        } else if (MDQConstants.SOAP11JMS_MTOM_BINDING.equals(ns)) {
            return "SOAP 1.1 MTOM JMS Binding";
        } else if (MDQConstants.SOAP12JMS_BINDING.equals(ns)) {
            return "SOAP 1.2 JMS Binding";
        } else if (MDQConstants.SOAP12JMS_MTOM_BINDING.equals(ns)) {
            return "SOAP 1.2 MTOM JMS Binding";
        } else if (HTTPBinding.HTTP_BINDING.equals(ns)) {
            return "XML HTTP Binding";
        } else {
            return "Unknown Binding";
        }
    }
}
