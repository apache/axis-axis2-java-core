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
package org.apache.wsdl;


import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import java.util.Iterator;

/**
 * @author chathura@opensource.lk
 */
public class WOMBuilderTest extends AbstractTestCase {

    private WSDLDescription womDescription = null;

    private Definition wsdl4jDefinition = null;

    public WOMBuilderTest(String testName) {
        super(testName);
    }

    private void initialize() throws Exception {

        WSDLVersionWrapper wsdlVersionWrapper = null;
        if (null == this.womDescription) {
            String path = getTestResourceFile("InteropTest.wsdl").getAbsolutePath();
			wsdlVersionWrapper =
                    WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11)
                    .build(path);
            this.womDescription = wsdlVersionWrapper.getDescription();
        }
        if (null == wsdl4jDefinition) {
            this.wsdl4jDefinition = wsdlVersionWrapper.getDefinition();
        }
    }

    public void testTopLevelComponentCount() throws Exception {
        this.initialize();
        assertEquals(womDescription.getServices().size(),
                wsdl4jDefinition.getServices().size());
        assertEquals(womDescription.getWsdlInterfaces().size(),
                wsdl4jDefinition.getPortTypes().size());
        ;
        assertEquals(womDescription.getServices().size(),
                wsdl4jDefinition.getServices().size());
        assertEquals(womDescription.getBindings().size(),
                wsdl4jDefinition.getBindings().size());

    }

    public void testInterfacesComponent() throws Exception {
        this.initialize();
        Iterator interfaceIterator = this.womDescription.getWsdlInterfaces()
                .values()
                .iterator();
        Iterator porttypeIterator = this.wsdl4jDefinition.getPortTypes()
                .values()
                .iterator();
        while (interfaceIterator.hasNext() & porttypeIterator.hasNext()) {
            WSDLInterface wsdlInterface = (WSDLInterface) interfaceIterator.next();
            PortType porttype = (PortType) porttypeIterator.next();
            assertEquals(wsdlInterface.getName(), porttype.getQName());
            assertEquals(wsdlInterface.getTargetnamespace(),
                    porttype.getQName().getNamespaceURI());
            assertEquals(wsdlInterface.getAllOperations().size(),
                    porttype.getOperations().size());
            Iterator womOperationIterator = wsdlInterface.getAllOperations()
                    .values()
                    .iterator();
            Iterator wsdl4jOprationIterator = porttype.getOperations()
                    .iterator();
            //Will only work if the order is retained in the iteration
            while (wsdl4jOprationIterator.hasNext()) {
                Operation wsdl4jOperation = (Operation) wsdl4jOprationIterator.next();
                this.operationsWaliking(
                        wsdlInterface.getOperation(wsdl4jOperation.getName()),
                        wsdl4jOperation);
            }
            while (womOperationIterator.hasNext()) {
                WSDLOperation womOperation = (WSDLOperation) womOperationIterator.next();
                this.operationsWaliking(womOperation,
                        porttype.getOperation(
                                womOperation.getName().getLocalPart(),
                                null,
                                null));
            }

        }
    }

    public void testServiceComponent() throws Exception {
        this.initialize();
        Iterator womServiceIterator = this.womDescription.getServices().values()
                .iterator();
        Iterator wsdl4jServiceIterator = this.wsdl4jDefinition.getServices()
                .values()
                .iterator();

        while (womServiceIterator.hasNext() & wsdl4jServiceIterator.hasNext()) {
            WSDLService wsdlService = (WSDLService) womServiceIterator.next();
            Service wsdl4jService = (Service) wsdl4jServiceIterator.next();
            assertEquals(wsdlService.getName(), wsdl4jService.getQName());

        }
    }


    private void operationsWaliking(WSDLOperation womOperation,
                                    Operation wsdl4jOperation) {
        assertEquals(womOperation.getName().getLocalPart(),
                wsdl4jOperation.getName());
        //System.out.println(womOperation.getMessageExchangePattern());


    }


}
