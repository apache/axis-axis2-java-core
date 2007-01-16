/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.marshaller.factory;

import javax.jws.soap.SOAPBinding;

import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.DocLitBareMethodMarshallerImpl;
import org.apache.axis2.jaxws.marshaller.impl.DocLitWrappedMethodMarshallerImpl;
import org.apache.axis2.jaxws.marshaller.impl.RPCLitMethodMarshallerImpl;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitBareMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedMethodMarshaller;
import org.apache.axis2.jaxws.marshaller.impl.alt.RPCLitMethodMarshaller;
import org.apache.axis2.jaxws.description.OperationDescription;

/**
 * The MethodMarshallerFactory creates a Doc/Lit Wrapped, Doc/Lit Bare or RPC Marshaller using SOAPBinding information
 */
public class MethodMarshallerFactory {

    // The following flags are for testing only.
    // The allow us to test the old implementation against the new implementation.
    private static final boolean ALT_RPCLIT_CLIENT         = true;
    private static final boolean ALT_DOCLIT_WRAPPED_CLIENT = true;
    private static final boolean ALT_DOCLIT_BARE_CLIENT    = true;
    
    private static final boolean ALT_RPCLIT_SERVER         = true;
    private static final boolean ALT_DOCLIT_WRAPPED_SERVER = true;
    private static final boolean ALT_DOCLIT_BARE_SERVER    = true;
    
	/**
	 * Intentionally private
	 */
	private MethodMarshallerFactory() {	
    }
   
    /**
     * Create Marshaller usining the Binding information
     * @param binding
     * @param serviceDesc
     * @param endpointDesc
     * @param operationDesc
     * @param protocol
     * @return
     */
    public static MethodMarshaller createMethodMarshaller(SOAPBinding.Style style, 
            SOAPBinding.ParameterStyle paramStyle,
            boolean isClient){  // This flag is for testing only !
		if (style == SOAPBinding.Style.RPC) {
            if (ALT_RPCLIT_CLIENT && isClient || ALT_RPCLIT_SERVER && !isClient ) {
                return new RPCLitMethodMarshaller();  
            } else {
                return new RPCLitMethodMarshallerImpl();
            }
        } else if (paramStyle == SOAPBinding.ParameterStyle.WRAPPED){
            if (ALT_DOCLIT_WRAPPED_CLIENT && isClient || ALT_DOCLIT_WRAPPED_SERVER && !isClient) {
                return new DocLitWrappedMethodMarshaller();
            } else {
                return new DocLitWrappedMethodMarshallerImpl();
            }
		} else if (paramStyle == SOAPBinding.ParameterStyle.BARE){
            if (ALT_DOCLIT_BARE_CLIENT && isClient || ALT_DOCLIT_BARE_SERVER && !isClient) {
                return new DocLitBareMethodMarshaller();
            } else {
                return new DocLitBareMethodMarshallerImpl();
            }
		}
		return null;
	}

    public static MethodMarshaller getMarshaller(OperationDescription op, boolean isClient) {

        MethodMarshaller marshaller = null;
        if (isClient) {
                if (op.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT) {
                    marshaller = createDocLitMethodMarshaller(op, isClient);
                } else if (op.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
                    marshaller = createRPCLitMethodMarshaller(isClient);
                }
        } else { // SERVER
                if (op.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT) {
                    marshaller = createDocLitMethodMarshaller(op, isClient);
                } else if (op.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
                    marshaller = createRPCLitMethodMarshaller(isClient);
                }
        }
        return marshaller;
    }

    private static MethodMarshaller createDocLitMethodMarshaller(OperationDescription op, boolean isClient){
        SOAPBinding.ParameterStyle parameterStyle = null;
        if(isDocLitBare(op)){
            parameterStyle = SOAPBinding.ParameterStyle.BARE;
        }
        if(isDocLitWrapped(op)){
            parameterStyle = SOAPBinding.ParameterStyle.WRAPPED;
        }
        return createMethodMarshaller(SOAPBinding.Style.DOCUMENT, parameterStyle, isClient);
    }

    private static MethodMarshaller createRPCLitMethodMarshaller(boolean isClient){
        return createMethodMarshaller(SOAPBinding.Style.RPC, SOAPBinding.ParameterStyle.WRAPPED, isClient);
    }

    protected static boolean isDocLitBare(OperationDescription op){
        SOAPBinding.ParameterStyle methodParamStyle = op.getSoapBindingParameterStyle();
        if(methodParamStyle!=null){
            return methodParamStyle == SOAPBinding.ParameterStyle.BARE;
        }
        else{
            SOAPBinding.ParameterStyle SEIParamStyle = op.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
            return SEIParamStyle == SOAPBinding.ParameterStyle.BARE;
        }
    }

    protected static boolean isDocLitWrapped(OperationDescription op){
        SOAPBinding.ParameterStyle methodParamStyle = op.getSoapBindingParameterStyle();
        if(methodParamStyle!=null){
            return methodParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
        }
        else{
            SOAPBinding.ParameterStyle SEIParamStyle = op.getEndpointInterfaceDescription().getSoapBindingParameterStyle();
            return SEIParamStyle == SOAPBinding.ParameterStyle.WRAPPED;
        }
    }
}
