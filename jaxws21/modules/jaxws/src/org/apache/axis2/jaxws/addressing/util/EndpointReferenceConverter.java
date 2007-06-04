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
package org.apache.axis2.jaxws.addressing.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedExceptionAction;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.EndpointReference;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Element;

public final class EndpointReferenceConverter {
    
    private static OMFactory omFactory = OMAbstractFactory.getOMFactory();
    
    private EndpointReferenceConverter() {
    }

    /**
     * Convert from a {@link org.apache.axis2.addressing.EndpointReference} to a
     * subclass of {@link EndpointReference}.
     * 
     * @param <T>
     * @param axis2EPR
     * @param clazz
     * @return
     * @throws AxisFault
     */
    public static <T extends EndpointReference> T convertFromAxis2(org.apache.axis2.addressing.EndpointReference axis2EPR, final Class<T> clazz)
    throws AxisFault, NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException, Exception {
        String addressingNamespace =
            SubmissionEndpointReference.class.isAssignableFrom(clazz) ? Submission.WSA_NAMESPACE : Final.WSA_NAMESPACE;
        QName qname = new QName(addressingNamespace, "EndpointReference", "wsa");
        OMElement omElement =
            EndpointReferenceHelper.toOM(omFactory, axis2EPR, qname, addressingNamespace);
        Element eprElement = XMLUtils.toDOM(omElement);
        Source eprInfoset = new DOMSource(eprElement);
        
        Constructor constructor =
            (Constructor) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws NoSuchMethodException {
                            return clazz.getConstructor(Source.class);
                        }
                    });

        return clazz.cast(constructor.newInstance(eprInfoset));
    }
    
    /**
     * Convert from a {@link EndpointReference} to a an instance of
     * {@link org.apache.axis2.addressing.EndpointReference}.
     * 
     * @param jaxwsEPR
     * @return
     * @throws AxisFault
     * @throws XMLStreamException
     */
    public static org.apache.axis2.addressing.EndpointReference convertToAxis2(EndpointReference jaxwsEPR)
    throws AxisFault, XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxwsEPR.writeTo(new StreamResult(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        OMElement eprElement = (OMElement) XMLUtils.toOM(bais);
        
        return EndpointReferenceHelper.fromOM(eprElement);
    }
}
