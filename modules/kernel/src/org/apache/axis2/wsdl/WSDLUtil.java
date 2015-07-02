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

package org.apache.axis2.wsdl;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.WSDL2Constants;

import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

/**
 * Some utility methods for the WSDL users
 */
public class WSDLUtil {

    /**
     * returns whether the given mep uri is one of the
     * input meps
     *
     * @param mep
     */
    public static boolean isInputPresentForMEP(String mep) {
        return WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(mep) ||
                WSDL2Constants.MEP_URI_IN_ONLY.equals(mep) ||
                WSDL2Constants.MEP_URI_IN_OUT.equals(mep) ||
                WSDL2Constants.MEP_URI_OUT_IN.equals(mep) ||
                WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(mep) ||
                WSDL2Constants.MEP_URI_ROBUST_IN_ONLY.equals(mep);
    }

    /**
     * returns whether the given mep URI is one of the output meps
     *
     * @param MEP
     */
    public static boolean isOutputPresentForMEP(String MEP) {
        return WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDL2Constants.MEP_URI_IN_OUT.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_IN.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP) ||
                WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP);
    }

    /**
     * part names are not unique across messages. Hence
     * we need some way of making the part name a unique
     * one (due to the fact that the type mapper
     * is a global list of types).
     * The seemingly best way to do that is to
     * specify a namespace for the part QName reference which
     * is stored in the  list. This part qname is
     * temporary and should not be used with it's
     * namespace URI (which happened to be the operation name)
     * with _input (or a similar suffix) attached to it
     *
     * @param opName
     * @param suffix
     * @param partName
     */
    public static QName getPartQName(String opName,
                                     String suffix,
                                     String partName) {
        return new QName(opName + suffix, partName);
    }

    public static String getConstantFromHTTPLocation(String httpLocation, String httpMethod) {
        if (httpLocation.charAt(0) != '?') {
            httpLocation = "/" + httpLocation;
        }
        int index = httpLocation.indexOf("{");
        if (index > -1) {
            httpLocation = httpLocation.substring(0, index);
        }
        return httpMethod + httpLocation;
    }

    /**
     * This method will return the EndPointName for a service with give transport protocol
     * ex : StudentServiceHttpEndpoint
     *
     * @param serviceName
     * @param protocol transport protocol
     * @return
     */
    public static String getEndpointName(String serviceName, String protocol) {

        StringBuilder buffer = new StringBuilder();
        buffer.append(serviceName);
        buffer.append(protocol.substring(0, 1).toUpperCase());
        buffer.append(protocol.substring(1, protocol.length()).toLowerCase());
        buffer.append("Endpoint");
        return buffer.toString();
    }

    /**
     * Registers default extension attributes types to given <code>extensionRegistry</code> instance.
     * <p>
     * The method configures the following attributes of {@link Input}, {@link Output} and {@link Fault} WSDL elements
     * to use {@link AttributeExtensible.STRING_TYPE}:
     * <ul>
     * <li>{http://www.w3.org/2005/08/addressing}Action</li>
     * <li>{http://www.w3.org/2006/05/addressing/wsdl}Action</li>
     * <li>{http://www.w3.org/2007/05/addressing/metadata}Action</li>
     * <li>{http://schemas.xmlsoap.org/ws/2004/08/addressing}Action</li>
     * </ul>
     * </p>
     * @param extensionRegistry The extension registry to add default extension attribute types to. Must not be null.
     */
    public static void registerDefaultExtensionAttributeTypes(ExtensionRegistry extensionRegistry) {
    	if (extensionRegistry == null) {
    		throw new IllegalArgumentException("Extension registry must not be null");
    	}
    	
	    QName finalWSANS = new QName(AddressingConstants.Final.WSA_NAMESPACE, AddressingConstants.WSA_ACTION);
	    extensionRegistry.registerExtensionAttributeType(Input.class, finalWSANS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Output.class, finalWSANS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Fault.class, finalWSANS, AttributeExtensible.STRING_TYPE);
        
	    QName finalWSAWNS = new QName(AddressingConstants.Final.WSAW_NAMESPACE, AddressingConstants.WSA_ACTION);
	    extensionRegistry.registerExtensionAttributeType(Input.class, finalWSAWNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Output.class, finalWSAWNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Fault.class, finalWSAWNS, AttributeExtensible.STRING_TYPE);
	
	    QName finalWSAMNS = new QName(AddressingConstants.Final.WSAM_NAMESPACE, AddressingConstants.WSA_ACTION);
	    extensionRegistry.registerExtensionAttributeType(Input.class, finalWSAMNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Output.class, finalWSAMNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Fault.class, finalWSAMNS, AttributeExtensible.STRING_TYPE);
	
	    QName submissionWSAWNS = new QName(AddressingConstants.Submission.WSA_NAMESPACE, AddressingConstants.WSA_ACTION);
	    extensionRegistry.registerExtensionAttributeType(Input.class, submissionWSAWNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Output.class, submissionWSAWNS, AttributeExtensible.STRING_TYPE);
	    extensionRegistry.registerExtensionAttributeType(Fault.class, submissionWSAWNS, AttributeExtensible.STRING_TYPE);
    }
    
    /**
     * Creates a new WSDLReader and configures it with a {@link WSDLFactory#newPopulatedExtensionRegistry()} if it does not specify an extension registry.
     * The method will register default extension attribute types in WSDLReader's {@link WSDLReader#getExtensionRegistry() extensionRegistry},
     * see {@link #registerDefaultExtensionAttributeTypes(ExtensionRegistry)}. 
     * 
     * @return The newly created WSDLReader instance.
     * @throws WSDLException
     */
    public static WSDLReader newWSDLReaderWithPopulatedExtensionRegistry()
    		throws WSDLException {
    	WSDLFactory wsdlFactory = WSDLFactory.newInstance();
        WSDLReader reader = wsdlFactory.newWSDLReader();

        ExtensionRegistry extensionRegistry = reader.getExtensionRegistry();
        if (extensionRegistry == null) {
        	extensionRegistry = wsdlFactory.newPopulatedExtensionRegistry();
        }
        
        WSDLUtil.registerDefaultExtensionAttributeTypes(extensionRegistry);
        
        reader.setExtensionRegistry(extensionRegistry);
        
        return reader;
    }
}
