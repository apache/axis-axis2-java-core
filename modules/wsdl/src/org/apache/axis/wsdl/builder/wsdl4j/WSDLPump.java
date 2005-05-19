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
package org.apache.axis.wsdl.builder.wsdl4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.axis.wsdl.builder.WSDLComponentFactory;
import org.apache.wsdl.Component;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.impl.WSDLProcessingException;

/**
 * @author chathura@opensource.lk
 */
public class WSDLPump {

	private static final String BOUND_INTERFACE_NAME = "BoundInterface";

	private WSDLDescription womDefinition;

	private Definition wsdl4jParsedDefinition;

	private WSDLComponentFactory wsdlComponenetFactory;

	public WSDLPump(WSDLDescription womDefinition,
			Definition wsdl4jParsedDefinition) {
		this(womDefinition, wsdl4jParsedDefinition, womDefinition);
	}

	public WSDLPump(WSDLDescription womDefinition,
			Definition wsdl4jParsedDefinition,
			WSDLComponentFactory wsdlComponentFactory) {
		this.womDefinition = womDefinition;
		this.wsdl4jParsedDefinition = wsdl4jParsedDefinition;
		this.wsdlComponenetFactory = wsdlComponentFactory;
	}

	public void pump() {
		if (null != this.wsdl4jParsedDefinition && null != this.womDefinition) {
			this.populateDefinition(this.womDefinition,
					this.wsdl4jParsedDefinition);
		} else {
			throw new WSDLProcessingException("Properties not set properly");
		}

	}

	private void populateDefinition(WSDLDescription wsdlDefinition,
			Definition wsdl4JDefinition) {
		//Go through the WSDL4J Definition and pump it to the WOM
		wsdlDefinition.setWSDL1DefinitionName(wsdl4JDefinition.getQName());
		wsdlDefinition
				.setTargetNameSpace(wsdl4JDefinition.getTargetNamespace());

		//////////////////////////////////////////////////////////////////////////////
		//Order of the following itmes shouldn't be changed unless you really
		// //
		// know what you are doing. Reason being the components that are //
		// copied(pumped) towards the end depend on the components that has //
		// already being pumped. Following Lists some of the dependencies //
		//1) The Binding refers to the Interface //
		//2) Thw Endpoint refers tot he Bindings //
		// .... //
		//																		   	//
		//////////////////////////////////////////////////////////////////////////////

		/////////////////////////(1)First pump the
		// Types//////////////////////////////
		//Types may get changed inside the Operation pumping.

		Types wsdl4jTypes = wsdl4JDefinition.getTypes();
		if (null != wsdl4jTypes) {
			WSDLTypes wsdlTypes = this.wsdlComponenetFactory.createTypes();
			Iterator wsdl4jelmentsIterator = wsdl4jTypes
					.getExtensibilityElements().iterator();
			ExtensibilityElement wsdl4jElement;
			WSDLExtensibilityElement womElement;
			UnknownExtensibilityElement temp = new UnknownExtensibilityElement();
			while (wsdl4jelmentsIterator.hasNext()) {
				wsdl4jElement = (ExtensibilityElement) wsdl4jelmentsIterator
						.next();
				womElement = this.wsdlComponenetFactory
						.createWSDLExtensibilityElement();
				if (null != wsdl4jElement.getRequired())
					womElement.setRequired(wsdl4jElement.getRequired()
							.booleanValue());
				//FIXME Find a permanent solution.
				if (wsdl4jElement.getClass().equals(temp.getClass())) {
					womElement
							.setElement(((UnknownExtensibilityElement) wsdl4jElement)
									.getElement());
				}
				wsdlTypes
						.addElement(wsdl4jElement.getElementType(), womElement);
			}
			this.womDefinition.setTypes(wsdlTypes);
		}

		////////////////////////(2)Pump the
		// Interfaces///////////////////////////
		//pump the Interfaces: Get the PortTypes from WSDL4J parse OM and pump
		// it to the
		//WOM's WSDLInterface Components

		Iterator portTypeIterator = wsdl4JDefinition.getPortTypes().values()
				.iterator();
		WSDLInterface wsdlInterface;
		while (portTypeIterator.hasNext()) {
			wsdlInterface = this.wsdlComponenetFactory.createInterface();
			this.populateInterfaces(wsdlInterface, (PortType) portTypeIterator
					.next());
			wsdlDefinition.addInterface(wsdlInterface);

		}

		//////////////////////////(3)Pump the
		// Bindings///////////////////////////////
		//pump the Bindings: Get the Bindings map from WSDL4J and create a new
		// map of
		//WSDLBinding elements

		Iterator bindingIterator = wsdl4JDefinition.getBindings().values()
				.iterator();
		WSDLBinding wsdlBinding;
		while (bindingIterator.hasNext()) {
			wsdlBinding = this.wsdlComponenetFactory.createBinding();
			this
					.populateBindings(wsdlBinding, (Binding) bindingIterator
							.next());
			wsdlDefinition.addBinding(wsdlBinding);

		}

		//////////////////////////(4)Pump the
		// Services///////////////////////////////

		Iterator serviceIterator = wsdl4JDefinition.getServices().values()
				.iterator();
		WSDLService wsdlService;
		while (serviceIterator.hasNext()) {
			wsdlService = this.wsdlComponenetFactory.createService();
			this
					.populateServices(wsdlService, (Service) serviceIterator
							.next());
			wsdlDefinition.addService(wsdlService);
		}

	}

	//////////////////////////////////////////////////////////////////////////////
	////////////////////////// Top level Components Copying
	// ////////////////////

	/**
	 * Simply Copy information.
	 * 
	 * @param wsdlInterface
	 * @param wsdl4jPortType
	 */
	//FIXME Evaluate a way of injecting features and priperties with a general
	// formatted input
	private void populateInterfaces(WSDLInterface wsdlInterface,
			PortType wsdl4jPortType) {

		//Copy the Attrebute information items
		//Copied with the Same QName so it will reqire no Query in Binding
		// pumping.
		wsdlInterface.setName(wsdl4jPortType.getQName());

		Iterator wsdl4JOperationsIterator = wsdl4jPortType.getOperations()
				.iterator();
		WSDLOperation wsdloperation;
		while (wsdl4JOperationsIterator.hasNext()) {
			wsdloperation = this.wsdlComponenetFactory.createOperation();
			this.populateOperations(wsdloperation,
					(Operation) wsdl4JOperationsIterator.next(), wsdl4jPortType
							.getQName().getNamespaceURI());
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
		WSDLInterface wsdlInterface = this.womDefinition
				.getInterface(interfaceName);
		//FIXME Do We need this eventually???
		if (null == wsdlInterface)
			throw new WSDLProcessingException(
					"Interface/PortType not found for the Binding :"
							+ wsdlBinding.getName());

		wsdlBinding.setBoundInterface(wsdlInterface);

		Iterator bindingoperationsIterator = wsdl4JBinding
				.getBindingOperations().iterator();

		WSDLBindingOperation wsdlBindingOperation;
		while (bindingoperationsIterator.hasNext()) {
			wsdlBindingOperation = this.wsdlComponenetFactory
					.createWSDLBindingOperation();
			this.populateBindingOperation(wsdlBindingOperation,
					(BindingOperation) bindingoperationsIterator.next(),
					wsdl4JBinding.getQName().getNamespaceURI());
			wsdlBinding.addBindingOperation(wsdlBindingOperation);
		}

	}

	public void populateServices(WSDLService wsdlService, Service wsdl4jService) {
		wsdlService.setName(wsdl4jService.getQName());
		Iterator wsdl4jportsIterator = wsdl4jService.getPorts().values()
				.iterator();
		wsdlService.setServiceInterface(this.getBoundInterface(wsdlService));
		WSDLEndpoint wsdlEndpoint;
		while (wsdl4jportsIterator.hasNext()) {
			wsdlEndpoint = this.wsdlComponenetFactory.createEndpoint();
			this.populatePorts(wsdlEndpoint, (Port) wsdl4jportsIterator.next(),
					wsdl4jService.getQName().getNamespaceURI());
			wsdlService.setEndpoint(wsdlEndpoint);
		}

	}

	/////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Internal Component Copying ///////////////////
	//TODO Faults ??
	public void populateOperations(WSDLOperation wsdlOperation,
			Operation wsdl4jOperation, String nameSpaceOfTheOperation) {
		//Copy Name Attrebute
		wsdlOperation.setName(new QName(nameSpaceOfTheOperation,
				wsdl4jOperation.getName()));

		//This code make no attempt to make use of the special xs:Token
		//defined in the WSDL 2.0. eg like #any, #none
	
		//OperationType wsdl4jOperation.getStyle()

		// Create the Input Message and add

		Input wsdl4jInputMessage = wsdl4jOperation.getInput();
		MessageReference wsdlInputMessage = this.wsdlComponenetFactory
				.createMessageReference();
		wsdlInputMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);

		//Get all the in parts and create a new Element out of it and add it to
		// the Types.
		//TODO


		if(wsdl4jInputMessage.getMessage().getParts().size()>1)
			throw new WSDLProcessingException("Multipart Parsing not Supported");
		Iterator inputIterator = wsdl4jInputMessage.getMessage().getParts().values().iterator();
		if(inputIterator.hasNext()){
			Part part = ((Part)inputIterator.next());
			QName element ;
			if(null != (element= part.getTypeName())){
				wsdlInputMessage.setElement(element);
			}else{
				wsdlInputMessage.setElement(part.getElementName());
			}
		}

		
		wsdlOperation.setInputMessage(wsdlInputMessage);
		
		
		//Create an output message and add
		Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
		MessageReference wsdlOutputMessage = this.wsdlComponenetFactory.createMessageReference();
		wsdlOutputMessage.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
		
		if(wsdl4jOutputMessage.getMessage().getParts().size()>1)
			throw new WSDLProcessingException("Multipart Parsing not Supported");
		Iterator outputIterator = wsdl4jOutputMessage.getMessage().getParts().values().iterator();
		if(outputIterator.hasNext()){
			Part outPart = ((Part)outputIterator.next());
			QName typeName ;
			if(null != (typeName = outPart.getTypeName())){
				wsdlOutputMessage.setElement(typeName);
			}else{
				wsdlOutputMessage.setElement(outPart.getElementName());
			}
		}
	
		
		wsdlOperation.setOutputMessage(wsdlOutputMessage);
		//TODO

		

		//Set the MEP
		wsdlOperation.setMessageExchangePattern(WSDL11MEPFinder
				.getMEP(wsdl4jOperation));

	}

	private void populateBindingOperation(
			WSDLBindingOperation wsdlBindingOperation,
			BindingOperation wsdl4jBindingOperation,
			String nameSpaceOfTheBindingOperation) {
		wsdlBindingOperation.setName(new QName(nameSpaceOfTheBindingOperation,
				wsdl4jBindingOperation.getName()));

		BindingInput wsdl4jInputBinding = wsdl4jBindingOperation
				.getBindingInput();
		WSDLBindingMessageReference wsdlInputBinding = this.wsdlComponenetFactory
				.createWSDLBindingMessageReference();

		wsdlInputBinding.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
		//TODO
		wsdlBindingOperation.setInput(wsdlInputBinding);

		BindingOutput wsdl4jOutputBinding = wsdl4jBindingOperation
				.getBindingOutput();
		WSDLBindingMessageReference wsdlOutputBinding = this.wsdlComponenetFactory
				.createWSDLBindingMessageReference();

		wsdlInputBinding.setDirection(WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
		//TODO
		wsdlBindingOperation.setInput(wsdlOutputBinding);

	}

	public void populatePorts(WSDLEndpoint wsdlEndpoint, Port wsdl4jPort,
			String targetNamspace) {
		wsdlEndpoint.setName(new QName(targetNamspace, wsdl4jPort.getName()));

		wsdlEndpoint.setBinding(this.womDefinition.getBinding(wsdl4jPort
				.getBinding().getQName()));
//		this.copyExtensibilityAttribute(wsdl4jPort.getExtensionAttributes(), wsdlEndpoint);
//		this.copyExtensibleElements(wsdl4jPort.getExtensibilityElements(), wsdlEndpoint);
	}

	

	/**
	 * This method will fill up the gap of WSDL 1.1 and WSDL 2.0 w.r.t. the
	 * bound interface for the Service Component Defined in the WSDL 2.0. Logic
	 * being if there exist only one PortType in the WSDL 1.1 file then that
	 * will be set as the bound interface of the Service. If more than one
	 * Porttype exist in the WSDl 1.1 file this will create a dummy Interface
	 * with the available PortTypes and will return that interface so that it
	 * will inherit all those interfaces.
	 * 
	 * Eventuall this will have to be fixed using user input since
	 * 
	 * @param service
	 * @return
	 */
	private WSDLInterface getBoundInterface(WSDLService service) {

		// Throw an exception if there are no interfaces defined as at yet.
		if (0 == this.womDefinition.getWsdlInterfaces().size())
			throw new WSDLProcessingException(
					"There are no "
							+ "Interfaces/PortTypes identified in the current partially built"
							+ "WOM");

		//If there is only one Interface available hten return that because
		// normally
		// that interface must be the one to the service should get bound.
		if (1 == this.womDefinition.getWsdlInterfaces().size())
			return (WSDLInterface) this.womDefinition.getWsdlInterfaces()
					.values().iterator().next();

		//If there are more than one interface available... For the time being
		// create a
		// new interface and set all those existing interfaces as
		// superinterfaces of it
		// and return.
		WSDLInterface newBoundInterface = this.womDefinition.createInterface();
		newBoundInterface.setName(new QName(service.getNamespace(), service
				.getName().getLocalPart()
				+ BOUND_INTERFACE_NAME));
		Iterator interfaceIterator = this.womDefinition.getWsdlInterfaces()
				.values().iterator();
		while (interfaceIterator.hasNext()) {
			newBoundInterface
					.addSuperInterface((WSDLInterface) interfaceIterator.next());
		}
		return newBoundInterface;
	}
	
	/**
	 * Get the Extensible elements form wsdl4jExtensibleElements <code>Vector</code>if any and
	 * copy them to <code>Component</code>
	 * @param wsdl4jExtensibleElements
	 * @param womExtensibleElements
	 */
	private void copyExtensibleElements(List wsdl4jExtensibleElements, Component component){
		Iterator iterator = wsdl4jExtensibleElements.iterator();
		while(iterator.hasNext()){
			Object obj = iterator.next();
			if(obj instanceof UnknownExtensibilityElement){
				UnknownExtensibilityElement temp = (UnknownExtensibilityElement)(obj);
				WSDLExtensibilityElement extensibilityElement = this.wsdlComponenetFactory.createWSDLExtensibilityElement();
				extensibilityElement.setElement(temp.getElement());
				Boolean required = temp.getRequired();
				if(null != required){
					extensibilityElement.setRequired(required.booleanValue());
				}
				component.addExtensibilityElement(extensibilityElement);
			}
		}
	}
	
	/**
	 * Get the Extensible Attributes from wsdl4jExtensibilityAttribute <code>Map</code> if
	 * any and copy them to the <code>Component</code>
	 * @param wsdl4jExtensibilityAttributes
	 * @param component
	 */
	private void copyExtensibilityAttribute(Map wsdl4jExtensibilityAttributes, Component component){
		Iterator iterator = wsdl4jExtensibilityAttributes.keySet().iterator();
		while(iterator.hasNext()){
			QName attributeName = (QName)iterator.next();
			QName value = (QName)wsdl4jExtensibilityAttributes.get(attributeName);
			WSDLExtensibilityAttribute attribute = this.wsdlComponenetFactory.createWSDLExtensibilityAttribute();
			attribute.setKey(attributeName);
			attribute.setValue(value);
			component.addExtensibleAttributes(attribute);
		}
	}

}