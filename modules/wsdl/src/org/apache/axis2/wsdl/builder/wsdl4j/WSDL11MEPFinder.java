/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.wsdl.builder.wsdl4j;

import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.impl.WSDLProcessingException;

import javax.wsdl.Operation;
import javax.wsdl.OperationType;

/**
 * @author chathura@opensource.lk
 */
public class WSDL11MEPFinder {

    public static String getMEP(Operation operation) {
//		boolean inMessageExist = false, outMessageExist = false;
//		if(null != operation.getInput()){
//			inMessageExist = true;
//		}
//		if(null != operation.getOutput()){
//			outMessageExist = true;
//		}
//		
//		if(inMessageExist && outMessageExist){
//			return WSDLConstants.MEP_URI_IN_OUT;
//		}
//		
//		if(inMessageExist && !outMessageExist){
//			return WSDLConstants.MEP_URI_IN_ONLY;
//		}
//		
//		if(!inMessageExist && outMessageExist){
//			return WSDLConstants.MEP_URI_OUT_ONLY;
//		}
//
        OperationType operationType = operation.getStyle();
        if (null != operationType) {

            if (operationType.equals(OperationType.REQUEST_RESPONSE))
                return WSDLConstants.MEP_URI_IN_OUT;

            if (operationType.equals(OperationType.ONE_WAY))
                return WSDLConstants.MEP_URI_IN_ONLY;

            if (operationType.equals(OperationType.NOTIFICATION))
                return WSDLConstants.MEP_URI_OUT_ONLY;

            if (operationType.equals(OperationType.SOLICIT_RESPONSE))
                return WSDLConstants.MEP_URI_OUT_IN;
        }
        throw new WSDLProcessingException("Cannot Determine the MEP");

    }
}
