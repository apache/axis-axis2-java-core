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
package org.apache.axis.wsdl.wsdltowom;

import java.util.Iterator;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDefinitions;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.impl.MessageReferenceImpl;
import org.apache.wsdl.impl.WSDLBindingImpl;
import org.apache.wsdl.impl.WSDLEndpointImpl;
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLOperationImpl;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.apache.wsdl.impl.WSDLServiceImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLPump {

    private WSDLDefinitions womDefinition;

    private Definition wsdl4jParsedDefinition;

    public WSDLPump(
        WSDLDefinitions womDefinition,
        Definition wsdl4jParsedDefinition) {
        this.womDefinition = womDefinition;
        this.wsdl4jParsedDefinition = wsdl4jParsedDefinition;
    }

    public void pump() {
        this.populateDefinition(
            this.womDefinition,
            this.wsdl4jParsedDefinition);

    }

    private void populateDefinition(
        WSDLDefinitions wsdlDefinition,
        Definition wsdl4JDefinition) {
        //Go through the WSDL4J Definition and pump it to the WOM
        wsdlDefinition.setWSDL1DefinitionName(wsdl4JDefinition.getQName());
        wsdlDefinition.setTargetNameSpace(
            wsdl4JDefinition.getTargetNamespace());

		/////////////////////////////////////////////////////////////////////////////
		//Order of the following itmes shouldn't be changed unless you really know //
		//what you are doing. Reason being the components that are copied(pumped)  //
		//towards the end depend on the components that has already being pumped.  //
		//Following Lists some of the dependencies										   //
		//1) The Binding refers to the Interface								   //
		//1) Thw Endpoint refers tot he Bindings
		// ....																	   //
		//																		   //	
		/////////////////////////////////////////////////////////////////////////////
		
		
		
		//////////////////////////(1)First pump the Types////////////////////////////
		
		//Types may get changed inside the Operation pumping.
				
		//////////////////////////(2)Pump the Interfaces/////////////////////////////
		
        
        
        //pump the Interfaces: Get the PortTypes from WSDL4J parse OM and pump it to the 
        //WOM's WSDLInterface Components 

        Iterator portTypeIterator = wsdl4JDefinition.getPortTypes().values().iterator();
        WSDLInterface wsdlInterface;
        while (portTypeIterator.hasNext()) {
            wsdlInterface = new WSDLInterfaceImpl();
            this.populateInterfaces(
                wsdlInterface,
                (PortType) portTypeIterator.next());
            wsdlDefinition.addInterface(wsdlInterface);

        }

		//////////////////////////(3)Pump the Bindings///////////////////////////////
		
		
        //pump the Bindings: Get the Bindings map from WSDL4J and create a new map of 
        //WSDLBinding elements

        
        Iterator bindingIterator = wsdl4JDefinition.getBindings().values().iterator();
        WSDLBinding wsdlBinding;
        while (bindingIterator.hasNext()) {
            wsdlBinding = new WSDLBindingImpl();
            this.populateBindings(wsdlBinding, (Binding) bindingIterator.next());
            wsdlDefinition.addBinding(wsdlBinding);

        }
        
		
		//////////////////////////(4)Pump the Service///////////////////////////////
		
		Iterator serviceIterator = wsdl4JDefinition.getServices().values().iterator();
		WSDLService wsdlService;
		while(serviceIterator.hasNext()){
			wsdlService = new WSDLServiceImpl();
			this.populateServices(wsdlService, (Service)serviceIterator.next());
		}		

        throw new UnsupportedOperationException("Fill the impl");
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////   Top level Components Copying ////////////////////

	

    /**
     * Simply Copy information.
     * @param wsdlInterface
     * @param wsdl4jPortType
     */
    //FIXME Evaluate a way of injecting features and priperties with a general formatted input
    private void populateInterfaces(
        WSDLInterface wsdlInterface,
        PortType wsdl4jPortType) {

        //Copy the Attrebute information items
		//Copied with the Same QName so it will reqire no Query in Binding pumping.
        wsdlInterface.setName(wsdl4jPortType.getQName()); 
        

        Iterator wsdl4JOperationsIterator =
            wsdl4jPortType.getOperations().iterator();
        while (wsdl4JOperationsIterator.hasNext()) {
            WSDLOperation wsdloperation = new WSDLOperationImpl();
            this.populateOperations(
                wsdloperation,
                (Operation) wsdl4JOperationsIterator.next(),
                wsdl4jPortType.getQName().getNamespaceURI());

        }

        
    }
    
    
	/**
	 * Pre Condition: The Interface Components must be copied by now.
	 */
	private void populateBindings(WSDLBinding wsdlBinding, Binding wsdl4JBinding) {
		//Copy attrebutes		
		wsdlBinding.setName(wsdl4JBinding.getQName());
		QName interfaceName = wsdl4JBinding.getPortType().getQName();
		WSDLInterface wsdlInterface = this.womDefinition.getInterface(interfaceName);
		//FIXME Do We need this eventually???
		if(null == wsdlInterface) throw new WSDLProcessingException("Interface/PortType not found for the Binding :" +wsdlBinding.getName());
		
		wsdlBinding.setBoundInterface(wsdlInterface);
		
		Iterator bindingoperationsIterator = wsdl4JBinding.getBindingOperations().iterator();
		
		while(bindingoperationsIterator.hasNext()){
			throw new UnsupportedOperationException("Fill the impl");
		}
		
	}
	
	
	
	public void  populateServices(WSDLService wsdlService, Service wsdl4jService){
		wsdlService.setName(wsdl4jService.getQName());
		Iterator wsdl4jportsIterator = wsdl4jService.getPorts().values().iterator();
		WSDLEndpoint wsdlEndpoint;
		while(wsdl4jportsIterator.hasNext()){
			wsdlEndpoint = new WSDLEndpointImpl();
			this.populatePorts(wsdlEndpoint, (Port)wsdl4jportsIterator.next(), wsdl4jService.getQName().getNamespaceURI());
		}
		
	}
		
	

    /////////////////////////////////////////////////////////////////////////////
    ////////////////////////////   Internal Component Copying ///////////////////
    //TODO Faults ??
    public void populateOperations(WSDLOperation wsdlOperation, Operation wsdl4jOperation, String nameSpaceOfTheOperation) {
        //Copy Name Attrebute
        wsdlOperation.setName(
            new QName(nameSpaceOfTheOperation, wsdl4jOperation.getName()));
        //OperationType wsdl4jOperation.getStyle()

        // Create the Input Message and add
        Input wsdl4jInputMessage = wsdl4jOperation.getInput();
        MessageReference wsdlInputMessage = new MessageReferenceImpl();
        wsdlInputMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);

        //Get all the in parts and create a new Element out of it and add it to the Types.
        //TODO

        //       	wsdlInputMessage.setMessageLabel()

        //Create an output message and add
        Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
        MessageReference wsdlOutputMessage = new MessageReferenceImpl();
        wsdlOutputMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        //Get all the in parts and create a new Element out of it and add it to the Types.
        //TODO

        //wsdlInputMessage.setMessageLabel()

        //Set the MEP
        wsdlOperation.setMessageExchangePattern(this.getRelaventMEPForTheMessageStyle(wsdl4jOperation.getStyle()));
        
    }
    
    
    public void populatePorts(WSDLEndpoint wsdlEndpoint, Port wsdl4jPort, String targetNamspace){
    	wsdlEndpoint.setName(new QName(targetNamspace, wsdl4jPort.getName()));
    	
		wsdlEndpoint.setBinding(this.womDefinition.getBinding(wsdl4jPort.getBinding().getQName()));
		///Extesibility ekements.
    }
    
    
    
    ///////////////////////////Util Methods ////////////////////////////////////
    
    /**
     * Will return the URI for the MEP.
     */
    private String getRelaventMEPForTheMessageStyle(OperationType operationType){
    	
		//TODO
		throw new UnsupportedOperationException("Fill the impl");
    	
    	
    }

}
