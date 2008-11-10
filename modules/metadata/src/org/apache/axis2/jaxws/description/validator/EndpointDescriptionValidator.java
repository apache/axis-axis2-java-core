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
import org.apache.axis2.jaxws.description.impl.DescriptionUtils;
import org.apache.axis2.jaxws.i18n.Messages;

import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.WebServiceFeatureAnnotation;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

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
            
            if (!validateRespectBinding()) {
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
        
        // The wsdl binding type that we now receive has been previously mapped to the expected
        // SOAP and HTTP bindings. So, there is now limited validation to perform.
        // 
        // IMPORTANT NOTE: The value returned is NOT the WSDL Binding Type value; it has been
        //    normalized to be the value corresponding to the JAXWS BindingType annotations.
        //    That means when we log this value below we need to un-normalize it so the value
        //    is one that actuall appears in the WSDL.  This isn't an issue for SOAP11 because
        //    the values are the same; but it IS an issue for SOAP12.
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
                !HTTPBinding.HTTP_BINDING.equals(bindingType) &&
                !MDQConstants.SOAP_HTTP_BINDING.equals(bindingType)) {
            
            addValidationFailure(this,
                                 "Invalid annotation binding value specified: " + bindingType);
            isBindingValid = false;
        }
        else if(bindingType.equals(MDQConstants.SOAP_HTTP_BINDING) && endpointDesc.isEndpointBased()){
        	addValidationFailure(this,
                    "A SOAP_HTTP_BINDING was found on a @Bindingtype SEI based Endpoint." +
                    " SOAP_HTTP_BINDING is supported on Provider Endpoints only.");
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
            addValidationFailure(this, "Invalid wsdl binding value specified: " 
                                 + DescriptionUtils.mapBindingTypeAnnotationToWsdl(wsdlBindingType));
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
        }
        // If wsdl binding is not HTTP binding and BindingType annotation is SOAP_HTTP_BINDING then
        // wsdl is valid and JAX-WS needs to support both soap 11 and soap 12 on Provider endpoints.
        else if(!wsdlBindingType.equals(HTTPBinding.HTTP_BINDING) 
            && bindingType.equals(MDQConstants.SOAP_HTTP_BINDING) && endpointDesc.isProviderBased()){
            isBindingValid = true;
        }
        else {
            
            // Mismatched bindings 
            String wsdlInsert = "[" + bindingHumanReadableDescription(wsdlBindingType) + "]" +
                "namespace = {" + DescriptionUtils.mapBindingTypeAnnotationToWsdl(wsdlBindingType) +"}";
            String annotationInsert = "[" + bindingHumanReadableDescription(bindingType) + "]" +
                "namespace = {" + bindingType +"}";
            
            String message = Messages.getMessage("endpointDescriptionValidation", 
                                              wsdlInsert, annotationInsert);
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
    
    /*
     * If the @RespectBinding annotation is present, then we must also have a WSDL 
     */
    private boolean validateRespectBinding() {
        // If a WSDL with a valid <wsdl:port> was present, then the WSDL is considered
        // fully specified.  Without that, the @RespectBinding annotation is invalid.
        if (endpointDesc.respectBinding()) {
            String wsdlLocation = null;
            if (!endpointDesc.isProviderBased()) {
                wsdlLocation = endpointDescJava.getAnnoWebServiceWSDLLocation();
            }
            else {
                wsdlLocation = endpointDescJava.getAnnoWebServiceProvider().wsdlLocation();
            }
            
            if (wsdlLocation == null || wsdlLocation.length() == 0) {
                addValidationFailure(this, "Annotation @RespectBinding requires that a WSDL file be specified.");    
                return Validator.INVALID;
            }
            
            // We will validate the configured bindings based on their mapping
            // to a known WebServiceFeature element.  If there is not a WebServiceFeature
            // annotation for a given binding, a validation error will be returned.
            List required = endpointDesc.getRequiredBindings();
            if (required.size() > 0) {
                Iterator i = required.iterator();
                while (i.hasNext()) {
                    QName name = (QName) i.next();
                    String featureName = getFeatureForBinding(name);
                    if (featureName != null && featureName.length() > 0) {
                        EndpointDescriptionJava edj = (EndpointDescriptionJava) endpointDesc;
                        Annotation anno = edj.getAnnoFeature(featureName);
                        WebServiceFeatureAnnotation feature = getFeatureFromAnnotation(anno);
                        
                        if (feature == null) {
                            addValidationFailure(this, "Annotation @RespectBinding was enabled, but the " +
                                        "corresponding feature " + featureName + " was not enabled.");
                            return Validator.INVALID;                            
                        }
                    }
                    else {
                       addValidationFailure(this, "Annotation @RespectBinding was enabled, but extensibility element " +
                           name + " was not recognized.");
                       return Validator.INVALID;
                    }                    
                }
                 
            }
        }        
        return Validator.VALID;
    }
    
    private WebServiceFeatureAnnotation getFeatureFromAnnotation(Annotation a) {
        return a.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
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
    
    private static String getFeatureForBinding(QName name) {
        if (name.equals(new QName("http://www.w3.org/2006/05/addressing/wsdl", "UsingAddressing"))) {
            return AddressingFeature.ID;
        }
        else {
            return null;
        }
    }
}
