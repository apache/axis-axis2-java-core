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

import org.apache.axis.AbstractTestCase;
import org.apache.axis.wsdl.wsdltowom.WOMBuilderFactory;
import org.apache.wsdl.util.Utils;
import org.w3c.dom.Document;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.FileInputStream;
import java.io.InputStream;
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
        if (null == this.womDescription) {
            InputStream in = new FileInputStream(this.getTestResourceFile("wsdl/SeismicService.wsdl"));
            this.womDescription = WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in);
        }
        if (null == wsdl4jDefinition) {
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            Document doc = Utils.newDocument(new FileInputStream(this.getTestResourceFile("wsdl/SeismicService.wsdl")));
            this.wsdl4jDefinition = reader.readWSDL(null, doc);
        }
    }

    public void testTopLevelComponentCount() throws Exception {
        this.initialize();
        assertEquals(womDescription.getServices().size(), wsdl4jDefinition.getServices().size());
        assertEquals(womDescription.getWsdlInterfaces().size(), wsdl4jDefinition.getPortTypes().size());
        ;
        assertEquals(womDescription.getServices().size(), wsdl4jDefinition.getServices().size());
        assertEquals(womDescription.getBindings().size(), wsdl4jDefinition.getBindings().size());
        assertEquals(womDescription.getTypes().getTypes().size(), wsdl4jDefinition.getTypes().getExtensibilityElements().size());
    }

    public void testInterfacesComponent() throws Exception {
        this.initialize();
        Iterator interfaceIterator = this.womDescription.getWsdlInterfaces().values().iterator();
        Iterator porttypeIterator = this.wsdl4jDefinition.getPortTypes().values().iterator();
        while (interfaceIterator.hasNext() & porttypeIterator.hasNext()) {
            WSDLInterface wsdlInterface = (WSDLInterface) interfaceIterator.next();
            PortType porttype = (PortType) porttypeIterator.next();
            assertEquals(wsdlInterface.getName(), porttype.getQName());
            assertEquals(wsdlInterface.getTargetnamespace(), porttype.getQName().getNamespaceURI());
            assertEquals(wsdlInterface.getAllOperations().size(), porttype.getOperations().size());
            Iterator womOperationIterator = wsdlInterface.getAllOperations().values().iterator();
            Iterator wsdl4jOprationIterator = porttype.getOperations().iterator();
            //Will only work if the order is retained in the iteration
            while (womOperationIterator.hasNext() & wsdl4jOprationIterator.hasNext()) {
                this.operationsWaliking((WSDLOperation) womOperationIterator.next(), (Operation) wsdl4jOprationIterator.next());
            }

        }
    }

    public void testServiceComponent() throws Exception {
        this.initialize();
        Iterator womServiceIterator = this.womDescription.getServices().values().iterator();
        Iterator wsdl4jServiceIterator = this.wsdl4jDefinition.getServices().values().iterator();

        while (womServiceIterator.hasNext() & wsdl4jServiceIterator.hasNext()) {
            WSDLService wsdlService = (WSDLService) womServiceIterator.next();
            Service wsdl4jService = (Service) wsdl4jServiceIterator.next();
            assertEquals(wsdlService.getName(), wsdl4jService.getQName());
//            System.out.println(wsdlService.getServiceInterface());
        }
    }


    private void operationsWaliking(WSDLOperation womOperation, Operation wsdl4jOperation) {
        assertEquals(womOperation.getName().getLocalPart(), wsdl4jOperation.getName());
        //System.out.println(((ExtensibilityElement)wsdl4jDefinition.getTypes().getExtensibilityElements().get(1)).getElementType());
//        System.out.println(womOperation.getInputMessage().getMessageLabel());
//        System.out.println(wsdl4jOperation.getInput().getName());
        //assertEquals(womOperation.getInputMessage().getMessageLabel(), wsdl4jOperation.getInput().getName());

    }


}
