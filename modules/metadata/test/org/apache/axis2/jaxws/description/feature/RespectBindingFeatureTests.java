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
package org.apache.axis2.jaxws.description.feature;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.RespectBinding;

public class RespectBindingFeatureTests extends TestCase {

    private static final String ns = "http://jaxws.axis2.apache.org/metadata/feature/respectbinding";
    
    private static final String plainServicePortName = "PlainServicePort";
    private static final String disabledServicePortName = "DisabledServicePort";

    public void testDefaultConfig() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(PlainService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, plainServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);

        boolean respect = ed.respectBinding();
        assertTrue("Strict binding support should be ENABLED.", respect);
    }
    
    public void testRespectBindingDisabled() {
        ServiceDescription sd = DescriptionFactory.createServiceDescription(DisabledService.class);
        
        EndpointDescription ed = sd.getEndpointDescription(new QName(ns, disabledServicePortName));
        assertTrue("The EndpointDescription should not be null.", ed != null);

        boolean respect = ed.respectBinding();
        assertFalse("Strict binding support should be DISABLED.", respect);
    }
    
    @WebService(targetNamespace=ns, portName=plainServicePortName)
    @RespectBinding
    class PlainService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
    
    @WebService(targetNamespace=ns, portName=disabledServicePortName)
    @RespectBinding(enabled=false)
    class DisabledService {
        public double getQuote(String symbol) {
            return 101.01;
        }
    }
}


