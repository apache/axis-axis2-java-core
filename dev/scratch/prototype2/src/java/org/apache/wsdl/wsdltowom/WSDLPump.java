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
package org.apache.wsdl.wsdltowom;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.wsdl.wom.WSDLBinding;
import org.apache.wsdl.wom.WSDLConstants;
import org.apache.wsdl.wom.WSDLDefinitions;
import org.apache.wsdl.wom.WSDLInterface;
import org.apache.wsdl.wom.WSDLOperation;
import org.apache.wsdl.wom.impl.WSDLBindingImpl;
import org.apache.wsdl.wom.impl.WSDLInterfaceImpl;
import org.apache.wsdl.wom.impl.WSDLOperationImpl;

public class WSDLPump {

    private WSDLDefinitions womDefinition;
    
    private Definition wsdl4jParsedDefinition;
    
    
    public WSDLPump(WSDLDefinitions womDefinition, Definition wsdl4jParsedDefinition){
        this.womDefinition = womDefinition;
        this.wsdl4jParsedDefinition = wsdl4jParsedDefinition;
    }
    
    public void pump(){
        this.populateDefinition(this.womDefinition, this.wsdl4jParsedDefinition);
        
    }
    
    
    
    
    private void populateDefinition(WSDLDefinitions wsdlDefinition, Definition wsdl4JDefinition){
        //Go through the WSDL4J Definition and pump it to the WOM
        wsdlDefinition.setWSDL1DefinitionName(wsdl4JDefinition.getQName());
        wsdlDefinition.setTargetNameSpace(wsdl4JDefinition.getTargetNamespace());
        
        
        //pump the Interfaces: Get the PortTypes from WSDL4J parse OM and pump it to the 
        //WOM's WSDLInterface Components 
        
        Map wsdl4jPortTypeMap = wsdl4JDefinition.getPortTypes();
        Iterator portTypeIterator = wsdl4jPortTypeMap.values().iterator();
        WSDLInterface wsdlInterface;
        while(portTypeIterator.hasNext()){
            wsdlInterface = new WSDLInterfaceImpl();
            this.populateInterfaces(wsdlInterface, (PortType)portTypeIterator.next());
            wsdlDefinition.addInterface(wsdlInterface);
            
        }
        
        
        //pump the Bindings: Get the Bindings map from WSDL4J and create a new map of 
        //WSDLBinding elements
        
        Map wsdl4jBindingsMap = wsdl4JDefinition.getBindings();
        Iterator bindingIterator = wsdl4jBindingsMap.values().iterator();
        WSDLBinding wsdlBinding;
        while(bindingIterator.hasNext()){
            wsdlBinding = new WSDLBindingImpl();
            this.populateBindings(wsdlBinding, (Binding)bindingIterator.next());
            wsdlDefinition.addBinding(wsdlBinding);
            
        }
        //Copy the services, 
        
        throw new UnsupportedOperationException("Fill the impl");
    }
    
    
    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////   Top level Components Copying ////////////////////
    
    
    private void populateBindings(WSDLBinding wsdlBinding, Binding wsdl4JBinding){
        
        throw new UnsupportedOperationException("Fill the impl");
    }
    
    /**
     * Simply Copy information.
     * @param wsdlInterface
     * @param wsdl4jPortType
     */
    //FIXME Evaluate a way of injecting features and priperties with a general formatted input
    private void populateInterfaces(WSDLInterface wsdlInterface, PortType wsdl4jPortType){
        
        //Copy the Attrebute information items
        wsdlInterface.setName(wsdl4jPortType.getQName());
        
        Iterator wsdl4JOperationsIterator = wsdl4jPortType.getOperations().iterator();
        while(wsdl4JOperationsIterator.hasNext()){
            WSDLOperation wsdloperation = new WSDLOperationImpl();
            this.populateOperations(wsdloperation, (Operation)wsdl4JOperationsIterator.next());
            
        }
        
        throw new UnsupportedOperationException("Fill the impl");
    }
    
    
    
    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////   Internal Component Copying ///////////////////
    
    public void populateOperations(WSDLOperation wsdlOperation, Operation wsdl4jOperation){
        //Copy Attrebutes
        wsdlOperation.setName(new QName(WSDLConstants.WSDL1_1_NAMESPACE,wsdl4jOperation.getName()));
        
//        wsdl4jOperation.getFault("d").
    }
    
}
