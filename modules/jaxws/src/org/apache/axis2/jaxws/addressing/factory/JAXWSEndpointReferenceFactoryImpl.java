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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;

public class JAXWSEndpointReferenceFactoryImpl implements JAXWSEndpointReferenceFactory {
    private static volatile JAXBContext jaxbContext;

    public JAXWSEndpointReferenceFactoryImpl() {
        super();
    }
    
    public EndpointReference createEndpointReference(Source eprInfoset) throws JAXBException {
        Unmarshaller um = getJAXBContext().createUnmarshaller();
        
        return (EndpointReference) um.unmarshal(eprInfoset);
    }
    
    public String getAddressingNamespace(Class clazz) {
        String addressingNamespace = null;
        
        if (W3CEndpointReference.class.isAssignableFrom(clazz))
            addressingNamespace = Final.WSA_NAMESPACE;
        else if (SubmissionEndpointReference.class.isAssignableFrom(clazz))
            addressingNamespace = Submission.WSA_NAMESPACE;
        else //TODO NLS enable.
            throw ExceptionFactory.makeWebServiceException("Unknown class type: " + clazz);
        
        return addressingNamespace;
    }
    
    private JAXBContext getJAXBContext() throws JAXBException {
        //This is an implementation of double-checked locking.
        //It works because jaxbContext is volatile.
        if (jaxbContext == null) {
            synchronized (JAXWSEndpointReferenceFactoryImpl.class) {
                if (jaxbContext == null)
                    jaxbContext = JAXBContext.newInstance(W3CEndpointReference.class,
                                                          SubmissionEndpointReference.class);
            }
        }
        
        return jaxbContext;
    }
}
