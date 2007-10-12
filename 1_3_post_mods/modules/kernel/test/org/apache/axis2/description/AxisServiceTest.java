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
package org.apache.axis2.description;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class AxisServiceTest extends TestCase {

    public void testAddMessageElementQNameToOperationMappingBasic() {
        AxisService service = new AxisService();
        
        AxisOperation op1 = new InOnlyAxisOperation();
        QName opName = new QName("foo");
        
        // test registering the same operation multiple times
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));        
    }
    
    public void testAddMessageElementQNameToOperationMappingOverloading() {
        AxisService service = new AxisService();
        
        AxisOperation op1 = new InOnlyAxisOperation();
        AxisOperation op2 = new InOnlyAxisOperation();
        AxisOperation op3 = new InOnlyAxisOperation();
        QName opName = new QName("foo");
        
        // test registering different operations under the same opName
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op1);
        
        assertEquals(op1, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op2);
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));
        
        service.addMessageElementQNameToOperationMapping(opName, op3);
        
        assertEquals(null, service.getOperationByMessageElementQName(opName));       
    }
    
}
