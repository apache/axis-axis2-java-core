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
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLOperationImpl;

import javax.xml.namespace.QName;
import java.util.Iterator;

public class InterfaceTest extends AbstractTestCase {
    public InterfaceTest(String testName) {
        super(testName);
    }

    public void testGetAllOperations() {
        WSDLOperation op;
        WSDLInterface intfc;
        WSDLInterface[] array = new WSDLInterface[5];
        int interfaceCounter = 5;
        int operationCounter = 5;

        for (int j = 0; j < interfaceCounter; j++) {
            intfc = new WSDLInterfaceImpl();
            intfc.setName(new QName(WSDLConstants.WSDL2_0_NAMESPACE, "inteface" + j));
            for (int i = 0; i < operationCounter; i++) {
                op = new WSDLOperationImpl();
                op.setName(new QName(WSDLConstants.WSDL1_1_NAMESPACE, "op" + i + "of inteface" + j));
                assertNotNull(op.getName());
                intfc.setOperation(op);
            }
            if (j > 0) {
                intfc.addSuperInterface(array[j - 1]);
            }
            array[j] = intfc;
        }
        //System.out.println(array[0].getAllOperations().size());
        assertEquals(((WSDLOperation) array[0].getAllOperations().get(new QName(WSDLConstants.WSDL1_1_NAMESPACE, "op0of inteface0"))).getName().getLocalPart(), "op0of inteface0");
        assertEquals(((WSDLOperation) array[0].getAllOperations().get(new QName(WSDLConstants.WSDL1_1_NAMESPACE, "op1of inteface0"))).getName().getLocalPart(), "op1of inteface0");
        assertEquals(array[interfaceCounter - 1].getAllOperations().size(), interfaceCounter * operationCounter);
        assertEquals(interfaceCounter * operationCounter, array[interfaceCounter - 1].getAllOperations().size());

        Iterator iter = array[1].getAllOperations().keySet().iterator();
        while (iter.hasNext()) {
            assertNotNull(((WSDLOperation) array[interfaceCounter - 1].getAllOperations().get(iter.next())).getName());
        }

        for (int j = 0; j < interfaceCounter; j++) {
            for (int i = 0; i < operationCounter; i++) {

                assertEquals(((WSDLOperation) array[interfaceCounter - 1].getAllOperations().get(new QName(WSDLConstants.WSDL1_1_NAMESPACE, "op" + j + "of inteface" + i))).getName().getLocalPart(), "op" + j + "of inteface" + i);
            }
        }

    }

    /**
     * When a interface inherit two or more Interfaces the inherited operation
     * who have the same QName should be the same Operation.
     */
    public void testInheritedOperationResolution() throws Exception {
        WSDLOperation op;
        WSDLInterface intfc;
        WSDLInterface[] array = new WSDLInterface[5];
        int interfaceCounter = 5;
        int operationCounter = 5;
        for (int i = 0; i < interfaceCounter; i++) {
            intfc = new WSDLInterfaceImpl();
            for (int j = 0; j < operationCounter; j++) {
                op = new WSDLOperationImpl();
                op.setName(new QName(WSDLConstants.WSDL1_1_NAMESPACE, "operation" + j));
                intfc.setOperation(op);
            }
            intfc.setName(new QName(WSDLConstants.WSDL2_0_NAMESPACE, "Interface" + i));
            array[i] = intfc;
        }

        WSDLInterface inheritedInterface = new WSDLInterfaceImpl();
        for (int i = 0; i < array.length; i++) {
            inheritedInterface.addSuperInterface(array[i]);
        }

        assertEquals(inheritedInterface.getAllOperations().size(), 5);

    }
}

