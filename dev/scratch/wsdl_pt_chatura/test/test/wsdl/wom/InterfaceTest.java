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
package test.wsdl.wom;

import java.net.URI;
import java.util.Iterator;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis.wsdl.wom.WSDLConstants;
import org.apache.axis.wsdl.wom.WSDLInterface;
import org.apache.axis.wsdl.wom.WSDLOperation;
import org.apache.axis.wsdl.wom.impl.WSDLInterfaceImpl;
import org.apache.axis.wsdl.wom.impl.WSDLOperationImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class InterfaceTest extends TestCase {
    public void testGetAllOperations(){
        WSDLOperation op ;
        WSDLInterface intfc;
        WSDLInterface[] array = new WSDLInterface[5];
        int interfaceCounter = 5;
        int operationCounter = 5;
        
        for(int j=0; j<interfaceCounter; j++){
            intfc = new WSDLInterfaceImpl();
            intfc.setName("inteface"+j);
	        for(int i=0; i<operationCounter; i++){
	            op = new WSDLOperationImpl();
	        	op.setName("op"+i+"of inteface"+j);
	        	System.out.println(op.getName());
	        	intfc.setOperation("op"+i+"of inteface"+j, op);
	        }
	        if(j>0){
	            intfc.addSuperInterface(new QName(WSDLConstants.WSDL2_0_NAMESPACE, array[j-1].getName()), array[j-1]);
	        }
	        array[j] = intfc;
        }
        System.out.println(array[0].getAllOperations().size());
        assertEquals(((WSDLOperation)array[0].getAllOperations().get("op0of inteface0")).getName(),"op0of inteface0" );
        assertEquals(((WSDLOperation)array[0].getAllOperations().get("op1of inteface0")).getName(),"op1of inteface0" );

        System.out.println(array[1].getAllOperations().size());
        System.out.println(array[1].getAllOperations().keySet());
        Iterator iter = array[1].getAllOperations().keySet().iterator();
        while(iter.hasNext()){
            System.out.println(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get(iter.next())).getName());
        }
        assertEquals(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get("op0of inteface0")).getName(),"op0of inteface0" );
        assertEquals(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get("op1of inteface0")).getName(),"op1of inteface0" );
        assertEquals(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get("op0of inteface1")).getName(),"op0of inteface1" );
        assertEquals(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get("op1of inteface1")).getName(),"op1of inteface1" );
        
        for(int j=0; j<interfaceCounter; j++){
            for(int i=0; i<operationCounter; i++){
                System.out.println("op"+j+"of inteface"+i);
                assertEquals(((WSDLOperation)array[interfaceCounter-1].getAllOperations().get("op"+j+"of inteface"+i)).getName(),"op"+j+"of inteface"+i );
            }
        }

    }
    
    /***
     * When a interface inherit two or more Interfaces the inherited operation 
     * who have the same QName should be the same Operation.
     *
     */
    public void testInheritedOperationResolution()throws Exception {
        WSDLOperation op ;
        WSDLInterface intfc;
        WSDLInterface[] array = new WSDLInterface[5];
        int interfaceCounter = 5;
        int operationCounter = 5;
        for(int i= 0; i< interfaceCounter; i++){
            intfc = new WSDLInterfaceImpl();
            for(int j=0; j< operationCounter; j++){
                op = new WSDLOperationImpl();
                op.setName("operation"+j);
                op.setTargetnemespace(new URI(WSDLConstants.WSDL2_0_NAMESPACE));
                intfc.setOperation(op.getName(), op);
            }
            intfc.setName("Interface"+i);
            array[i] = intfc;
        }
                
        WSDLInterface inheritedInterface = new WSDLInterfaceImpl();
        for(int i=0; i< array.length; i++){
            inheritedInterface.addSuperInterface(new QName(WSDLConstants.WSDL2_0_NAMESPACE, array[i].getName()), array[i]);
        }
        
        System.out.println(inheritedInterface.getAllOperations().size());
        
    }
}
