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
package org.apache.axis2.jaxws.addressing;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.addressing.metadata.InterfaceName;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.w3c.dom.Element;

public final class SubmissionEndpointReferenceBuilder {
	private static final Element[] ZERO_LENGTH_ARRAY = new Element[0];

	private String address;
    private QName serviceName;
    private QName endpointName;
    private String wsdlDocumentLocation;
    private List<Element> referenceParameters;
    private QName portType;
    
    public SubmissionEndpointReferenceBuilder() {
    }
    
    public SubmissionEndpointReferenceBuilder address(String address) {
        this.address = address;
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder serviceName(QName serviceName) {
        this.serviceName = serviceName;
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder endpointName(QName endpointName) {
        //TODO NLS enable
        if (this.serviceName == null) {
            throw new IllegalStateException("The endpoint qname cannot be set before the service qname.");
        }
        
        this.endpointName = endpointName;
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder wsdlDocumentLocation(String wsdlDocumentLocation) {
        this.wsdlDocumentLocation = wsdlDocumentLocation;
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder referenceProperty(Element referenceProperty) {
        //TODO NLS enable
        if (referenceProperty == null) {
            throw new IllegalArgumentException("A reference property cannot be null.");
        }
        
        if (this.referenceParameters == null) {
            this.referenceParameters = new ArrayList<Element>();
        }
        
        this.referenceParameters.add(referenceProperty);
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder referenceParameter(Element referenceParameter) {
        //TODO NLS enable
        if (referenceParameter == null) {
            throw new IllegalArgumentException("A reference parameter cannot be null.");
        }
        
        if (this.referenceParameters == null) {
            this.referenceParameters = new ArrayList<Element>();
        }
        
        this.referenceParameters.add(referenceParameter);
        return this;
    }
    
    public SubmissionEndpointReferenceBuilder portType(QName portType) {
        this.portType = portType;
        return this;
    }
    
    public SubmissionEndpointReference build() {
    	SubmissionEndpointReference submissionEPR = null;
    	
        String addressingNamespace =
        	EndpointReferenceUtils.getAddressingNamespace(SubmissionEndpointReference.class);    	
        org.apache.axis2.addressing.EndpointReference axis2EPR =
        	EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, endpointName, wsdlDocumentLocation, addressingNamespace);
    	
        try {
        	EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters.toArray(ZERO_LENGTH_ARRAY));
        	EndpointReferenceUtils.addInterface(axis2EPR, portType, InterfaceName.subQName);
        	
            submissionEPR =
                (SubmissionEndpointReference) EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("A problem occured during the creation of an endpoint reference. See the nested exception for details.", e);
        }
        
        return submissionEPR;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
