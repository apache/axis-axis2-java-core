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
package org.apache.axis2.jaxws.addressing.factory;

import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;

public class EndpointReferenceFactoryImpl implements EndpointReferenceFactory {
    public EndpointReferenceFactoryImpl() {
        super();
    }
    
    private W3CEndpointReference createW3CEndpointReference(Source eprInfoset) {
        return new W3CEndpointReference(eprInfoset);
    }
    
    private SubmissionEndpointReference createSubmissionEndpointReference(Source eprInfost) {
        return new SubmissionEndpointReference(eprInfost);
    }
    
    public EndpointReference createEndpointReference(Source eprInfoset, String addressingNamespace) {
        EndpointReference endpointReference = null;
        
        if (Final.WSA_NAMESPACE.equals(addressingNamespace))
            endpointReference = createW3CEndpointReference(eprInfoset);
        else if (Submission.WSA_NAMESPACE.equals(addressingNamespace))
            endpointReference = createSubmissionEndpointReference(eprInfoset);
        else //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Unknown addressing namespace: " + addressingNamespace);
        
        return endpointReference;
    }
    
    public String getAddressingNamespace(Class clazz) {
        String addressingNamespace = null;
        
        if (W3CEndpointReference.class.isAssignableFrom(clazz))
            addressingNamespace = Final.WSA_NAMESPACE;
        else if (SubmissionEndpointReference.class.isAssignableFrom(clazz))
            addressingNamespace = Submission.WSA_NAMESPACE;
        else //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Unknown class type: " + clazz.getCanonicalName());
        
        return addressingNamespace;
    }
}
