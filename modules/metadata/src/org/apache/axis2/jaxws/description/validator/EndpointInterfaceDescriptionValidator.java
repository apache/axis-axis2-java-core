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

import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescriptionWSDL;
import org.apache.axis2.jaxws.description.OperationDescription;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 */
public class EndpointInterfaceDescriptionValidator extends Validator {
    EndpointInterfaceDescription epInterfaceDesc;
    EndpointInterfaceDescriptionJava epInterfaceDescJava;
    EndpointInterfaceDescriptionWSDL epInterfaceDescWSDL;

    public EndpointInterfaceDescriptionValidator(EndpointInterfaceDescription toValidate) {
        epInterfaceDesc = toValidate;
        epInterfaceDescJava = (EndpointInterfaceDescriptionJava)epInterfaceDesc;
        epInterfaceDescWSDL = (EndpointInterfaceDescriptionWSDL)epInterfaceDesc;

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

            OperationDescription[] dispatchableOpDescArray = 
                epInterfaceDesc.getDispatchableOperations();
    
            if (wsdlOperationList.size() != dispatchableOpDescArray.length) {
                addValidationFailure(this, "The number of operations in the WSDL " +
                        "portType does not match the number of methods in the SEI or " +
                        "Web service implementation class.  " +
                        "wsdl operations = [" + toString(wsdlOperationList) +"] " +
                        "dispatch operations = [" + toString(dispatchableOpDescArray) +"]");
                return INVALID;
            }

            // If they are the same size, let's check to see if the operation names match
            if (!checkOperationsMatchMethods(wsdlOperationList, dispatchableOpDescArray)) {
                addValidationFailure(this, "The operation names in the WSDL portType " +
                        "do not match the method names in the SEI or Web service i" +
                        "mplementation class.  " +
                        "wsdl operations = [" + toString(wsdlOperationList) +"] " +
                        "dispatch operations = [" + toString(dispatchableOpDescArray) +"]");
                return INVALID;
            }
        }
        return VALID;
    }

    private boolean checkOperationsMatchMethods(List wsdlOperationList, OperationDescription[]
            opDescArray) {
        List<String> opNameList = createWSDLOperationNameList(wsdlOperationList);
        for (int i = 0; i < opDescArray.length; i++) {
            OperationDescription opDesc = opDescArray[i];
            if (opNameList.contains(opDesc.getOperationName())) {
                opNameList.remove(opDesc.getOperationName());
            } else {
                return false;
            }
        }
        return true;
    }

    private List<String> createWSDLOperationNameList(List wsdlOperationList) {
        List<String> opNameList = new ArrayList<String>();
        Iterator wsdlOpIter = wsdlOperationList.iterator();
        while (wsdlOpIter.hasNext()) {
            Object obj = wsdlOpIter.next();
            if (obj instanceof Operation) {
                Operation operation = (Operation)obj;
                opNameList.add(operation.getName());
            }
        }
        return opNameList;
    }

    private boolean validateSEIvsImplementation() {
        // REVIEW: This level of validation is currently being done by the DBC Composite validation
        return VALID;
    }
    
    private static String toString(List wsdlOperationList) {
        String result = "";
        Iterator wsdlOpIter = wsdlOperationList.iterator();
        while (wsdlOpIter.hasNext()) {
            Object obj = wsdlOpIter.next();
            if (obj instanceof Operation) {
                Operation operation = (Operation)obj;
                result += operation.getName() + " ";
            }
        }
        return result;
    }
    
    private static String toString(OperationDescription[] wsdlOpDescs) {
        String result = "";
        for (int i= 0; i<wsdlOpDescs.length; i++) {
            result += wsdlOpDescs[i].getOperationName() + " ";
        }
        return result;
    }
}
