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
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsdl.ExtensionElement;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.impl.ExtensionElementImpl;
import org.apache.wsdl.impl.MessageReferenceImpl;
import org.apache.wsdl.impl.WSDLBindingImpl;
import org.apache.wsdl.impl.WSDLBindingMessageReferenceImpl;
import org.apache.wsdl.impl.WSDLBindingOperationImpl;
import org.apache.wsdl.impl.WSDLEndpointImpl;
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLOperationImpl;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.apache.wsdl.impl.WSDLServiceImpl;
import org.apache.wsdl.impl.WSDLTypesImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDLPump {

	private static final String BOUND_INTERFACE_NAME = "BoundInterface";
    private WSDLDescription womDefinition;

    private Definition wsdl4jParsedDefinition;

    public WSDLPump(
        WSDLDescription womDefinition,
        Definition wsdl4jParsedDefinition) {
        this.womDefinition = womDefinition;
        this.wsdl4jParsedDefinition = wsdl4jParsedDefinition;
    }

    public void pump() {
        if(null != this.wsdl4jParsedDefinition && null != this.womDefinition ){
        	this.populateDefinition(this.womDefinition, this.wsdl4jParsedDefinition);
        }else{
            throw new WSDLProcessingException("Properties not set properly");
        }

    }

    private void populateDefinition(
        WSDLDescription wsdlDefinition,
        Definition wsdl4JDefinition) {
        //Go through the WSDL4J Definition and pump it to the WOM
        wsdlDefinition.setWSDL1DefinitionName(wsdl4JDefinition.getQName());
        wsdlDefinition.setTargetNameSpace(
            wsdl4JDefinition.getTargetNamespace());

		/////////////////////////////////////////////////////////////////////////////
		//Order of the following itmes shouldn't be changed unless you really know //
		//what you are doing. Reason being the components that are copied(pumped)  //
		//towards the end depend on the components that has already being pumped.  //
		//Following Lists some of the dependencies								   //
		//1) The Binding refers to the Interface								   //
		//1) Thw Endpoint refers tot he Bindings								   //
		// ....																	   //
		//																		   //	
		/////////////////////////////////////////////////////////////////////////////
		
		
		
		//////////////////////////(1)First pump the Types////////////////////////////		
		//Types may get changed inside the Operation pumping.
				
        Types wsdl4jTypes = wsdl4JDefinition.getTypes();
        WSDLTypes wsdlTypes = new WSDLTypesImpl();
        Iterator wsdl4jelmentsIterator = wsdl4jTypes.getExtensibilityElements().iterator();
        ExtensibilityElement wsdl4jElement;
        ExtensionElement womElement;
        while(wsdl4jelmentsIterator.hasNext()){
            wsdl4jElement = (ExtensibilityElement)wsdl4jelmentsIterator.next();
            womElement = new ExtensionElementImpl();
            if(null != wsdl4jElement.getRequired())
                womElement.setRequired(wsdl4jElement.getRequired().booleanValue());
            //TODO
            
            wsdlTypes.addElement(wsdl4jElement.getElementType(), womElement);            
        }
        this.womDefinition.setTypes(wsdlTypes);
        
        
        
        
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
			wsdlDefinition.addService(wsdlService);
		}		
        
    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////////   Top level Components Copying ////////////////////



    /**
     * Simply Copy information.
     * @param wsdlInterface
     * @param wsdl4jPortType
     */
    //FIXME Evaluate a way of injecting features and priperties with a general formatted input
    private void populateInterfaces(WSDLInterface wsdlInterface, PortType wsdl4jPortType) {

        //Copy the Attrebute information items
		//Copied with the Same QName so it will reqire no Query in Binding pumping.
        wsdlInterface.setName(wsdl4jPortType.getQName()); 
        

        Iterator wsdl4JOperationsIterator = wsdl4jPortType.getOperations().iterator();
        WSDLOperation wsdloperation ;
        while (wsdl4JOperationsIterator.hasNext()) {
            wsdloperation = new WSDLOperationImpl();
            this.populateOperations( wsdloperation, (Operation) wsdl4JOperationsIterator.next(), wsdl4jPortType.getQName().getNamespaceURI());
            wsdlInterface.setOperation(wsdloperation);

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
		
		WSDLBindingOperation wsdlBindingOperation;
		while(bindingoperationsIterator.hasNext()){
		    wsdlBindingOperation = new WSDLBindingOperationImpl();			
			this.populateBindingOperation(wsdlBindingOperation, (BindingOperation)bindingoperationsIterator.next(), wsdl4JBinding.getQName().getNamespaceURI());
			wsdlBinding.addBindingOperation(wsdlBindingOperation);
		}
		
		
	}
	
	
	
	public void  populateServices(WSDLService wsdlService, Service wsdl4jService){
		wsdlService.setName(wsdl4jService.getQName());
		Iterator wsdl4jportsIterator = wsdl4jService.getPorts().values().iterator();
		wsdlService.setServiceInterface(this.getBoundInterface(wsdlService));
		WSDLEndpoint wsdlEndpoint;
		while(wsdl4jportsIterator.hasNext()){
			wsdlEndpoint = new WSDLEndpointImpl();
			this.populatePorts(wsdlEndpoint, (Port)wsdl4jportsIterator.next(), wsdl4jService.getQName().getNamespaceURI());
			wsdlService.setEndpoint(wsdlEndpoint);
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
        
        wsdlInputMessage.setMessageLabel(wsdl4jInputMessage.getName());
        //wsdlInputMessage.setElement(wsdl4jInputMessage.getMessage())

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
    
    
    private void populateBindingOperation(WSDLBindingOperation wsdlBindingOperation, BindingOperation wsdl4jBindingOperation, String nameSpaceOfTheBindingOperation){
        wsdlBindingOperation.setName(new QName(nameSpaceOfTheBindingOperation, wsdl4jBindingOperation.getName()));
        
        BindingInput wsdl4jInputBinding = wsdl4jBindingOperation.getBindingInput();
        WSDLBindingMessageReference wsdlInputBinding =  new WSDLBindingMessageReferenceImpl();
        
        wsdlInputBinding.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        //TODO
        wsdlBindingOperation.setInput(wsdlInputBinding);
        
        
        
        BindingOutput wsdl4jOutputBinding = wsdl4jBindingOperation.getBindingOutput();
        WSDLBindingMessageReference wsdlOutputBinding =  new WSDLBindingMessageReferenceImpl();
        
        wsdlInputBinding.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
        //TODO
        wsdlBindingOperation.setInput(wsdlOutputBinding);
        
        
        
    }
    
    
    
    public void populatePorts(WSDLEndpoint wsdlEndpoint, Port wsdl4jPort, String targetNamspace){
    	wsdlEndpoint.setName(new QName(targetNamspace, wsdl4jPort.getName()));
    	
		wsdlEndpoint.setBinding(this.womDefinition.getBinding(wsdl4jPort.getBinding().getQName()));
		///Extesibility elements.
    }
    
    
    
    ///////////////////////////Util Methods ////////////////////////////////////
   
    /**
     * Will return the URI for the MEP. if null will retun the IN_OUT as default
     * pattern.
     */
    private String getRelaventMEPForTheMessageStyle(OperationType operationType){
        
        if(null != operationType){
	            
	        if(operationType.equals(OperationType.REQUEST_RESPONSE)) 
	            return WSDLConstants.MEP_URI_IN_OUT;
	        
	    	if(operationType.equals(OperationType.ONE_WAY))
	    	    return WSDLConstants.MEP_URI_IN_ONLY;
	    	
	    	if(operationType.equals(OperationType.NOTIFICATION))
	    	    return WSDLConstants.MEP_URI_OUT_ONLY;
	    	
	    	if(operationType.equals(OperationType.SOLICIT_RESPONSE))
	    	    return WSDLConstants.MEP_URI_OUT_IN;
        }
        //TODO
        return WSDLConstants.MEP_URI_OUT_IN;
    }
    
    /**
     * This method will fill up the gap of WSDL 1.1 and WSDL 2.0 w.r.t. the 
     * bound interface for the Service Component Defined in the WSDL 2.0.
     * Logic being if there exist only one PortType in the WSDL 1.1 file
     * then that will be set as the bound interface of the Service. If more than one 
     * Porttype exist in the WSDl 1.1 file this will create a dummy Interface
     * with the available PortTypes and will return that interface so that it 
     * will inherit all those interfaces.
     * 
     * Eventuall this will have to be fixed using user input since  
     * @param service
     * @return
     */
    private WSDLInterface getBoundInterface(WSDLService service){
    	
    	// Throw an exception if there are no interfaces defined as at yet.
    	if(0 ==this.womDefinition.getWsdlInterfaces().size())
    		throw new WSDLProcessingException( "There are no " +
    				"Interfaces/PortTypes identified in the current partially built" +
    				"WOM");
    	
    	//If there is only one Interface available hten return that because normally
    	// that interface must be the one to the service should get bound.
    	if(1 == this.womDefinition.getWsdlInterfaces().size())
    		return (WSDLInterface)this.womDefinition.getWsdlInterfaces().values().iterator().next();
    	
    	//If there are more than one interface available... For the time being create a 
    	// new interface and set all those existing interfaces as superinterfaces of it
    	// and return.
    	WSDLInterface newBoundInterface = this.womDefinition.createInterface();
    	newBoundInterface.setName(new QName(service.getNamespace(), service.getName().getLocalPart()+ BOUND_INTERFACE_NAME));
    	Iterator interfaceIterator = this.womDefinition.getWsdlInterfaces().values().iterator();
    	while(interfaceIterator.hasNext()){
    		newBoundInterface.addSuperInterface((WSDLInterface)interfaceIterator.next());
    	}
    	return newBoundInterface;    	
    }
    
}
