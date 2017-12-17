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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;

import junit.framework.TestCase;

public class WSDL11ToAxisServiceBuilderTest extends TestCase {
    /**
     * Tests processing of an operation that declares multiple faults referring to the same message.
     * In this case, {@link WSDL11ToAxisServiceBuilder} must correctly populate the
     * {@link AxisMessage} object for both faults. In particular,
     * {@link AxisMessage#getElementQName()} must return consistent information. This is a
     * regression test for AXIS2-4533.
     * 
     * @throws Exception
     */
    public void testMultipleFaultsWithSameMessage() throws Exception {
        InputStream in = new FileInputStream("test-resources/wsdl/faults.wsdl");
        try {
            AxisService service = new WSDL11ToAxisServiceBuilder(in).populateService();
            AxisOperation operation = service.getOperation(new QName("urn:test", "test"));
            assertNotNull(operation);
            List<AxisMessage> faultMessages = operation.getFaultMessages();
            assertEquals(2, faultMessages.size());
            AxisMessage error1 = faultMessages.get(0);
            AxisMessage error2 = faultMessages.get(1);
            assertEquals("errorMessage", error1.getName());
            assertEquals("errorMessage", error2.getName());
            assertEquals(new QName("urn:test", "error"), error1.getElementQName());
            assertEquals(new QName("urn:test", "error"), error2.getElementQName());
        } finally {
            in.close();
        }
    }
    
    public void testNonDuplicatedElementsHttpBinding() throws Exception {
        final String wsdlPath = "test-resources/wsdl/nonduplicatedElements.wsdl";
        InputStream in = new FileInputStream(wsdlPath);
        final String targetNamespace = "http://www.example.org";
        final QName serviceName = new QName(targetNamespace, "FooService");
        final String portName = "FooHttpGetPort";
          
        AxisService service = new WSDL11ToAxisServiceBuilder(in, serviceName, portName).populateService();
        List schemaDocuments = service.getSchema();
        List duplicatedGlobalElements = findDuplicatedGlobalElements(schemaDocuments);
        // NO duplicated element should exists
        assertTrue("Duplicated global element declarations found in '" +  wsdlPath, 
            duplicatedGlobalElements.isEmpty());
    }

    protected List findDuplicatedGlobalElements(List schemaDocuments) {
        List duplicatedGlobalElementDeclarations = new ArrayList();
        Set globalElementDeclarations = new HashSet();
        // Iterate over all schema documents
        for (int i = 0; i < schemaDocuments.size(); i++) {
            XmlSchema schemaDocument = (XmlSchema)schemaDocuments.get(i);
            XmlSchemaObjectCollection items = schemaDocument.getItems();
            for (Iterator itemsIt = items.getIterator(); itemsIt.hasNext();) {
                XmlSchemaObject xmlSchemaObject = (XmlSchemaObject)itemsIt.next();
                // Check only XML schema elements
                if (xmlSchemaObject instanceof XmlSchemaElement) {
                    QName elementName = ((XmlSchemaElement)xmlSchemaObject).getQName();
                    /* Was another element with the same name found in this or
                      other XML schema document? */
                    if (globalElementDeclarations.contains(elementName)) {
                        duplicatedGlobalElementDeclarations.add(elementName);
                    } else {
                        globalElementDeclarations.add(elementName);
                    }
                }
            }
        }
        return duplicatedGlobalElementDeclarations;
    }

}
