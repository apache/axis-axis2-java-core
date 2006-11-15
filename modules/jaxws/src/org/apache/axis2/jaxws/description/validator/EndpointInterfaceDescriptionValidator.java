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

import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionWSDL;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.validator.Validator.ValidationLevel;

/**
 * 
 */
public class EndpointInterfaceDescriptionValidator extends Validator {
    EndpointInterfaceDescription epInterfaceDesc;
    EndpointInterfaceDescriptionJava epInterfaceDescJava;
    EndpointInterfaceDescriptionWSDL epInterfaceDescWSDL;

    public EndpointInterfaceDescriptionValidator(EndpointInterfaceDescription toValidate) {
        epInterfaceDesc = toValidate;
        epInterfaceDescJava = (EndpointInterfaceDescriptionJava) epInterfaceDesc;
        epInterfaceDescWSDL = (EndpointInterfaceDescriptionWSDL) epInterfaceDesc;
        
    }
    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.description.validator.Validator#validate()
     */
    @Override
    public boolean validate() {
        if (getValidationLevel() == ValidationLevel.OFF) {
            return VALID;
        }
        if (!validateSEIvsWSDLPortType()) {
            return INVALID;
        }
        if (!validateSEIvsImplementation()) {
            return INVALID;
        }
        return VALID;
    }
    
    private boolean validateSEIvsWSDLPortType() {
        PortType portType = epInterfaceDescWSDL.getWSDLPortType();
        if (portType != null) {
            // TODO: Need more validation here, including: operation name, parameters, faults
            List wsdlOperationList = portType.getOperations(); 
            OperationDescription[] opDescArray = epInterfaceDesc.getOperations();
            if (wsdlOperationList.size() != opDescArray.length) {
                addValidationFailure(this, "Operations did not match WSDL");
                return INVALID;
            }
        }
        return VALID;
    }
    private boolean validateSEIvsImplementation() {
        // REVIEW: This level of validation is currently being done by the DBC Composite validation
        return VALID;
    }
}
