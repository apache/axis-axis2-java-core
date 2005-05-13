package org.apache.axis.description;

import org.apache.axis.wsdl.builder.WSDLComponentFactory;
import org.apache.wsdl.*;
import org.apache.wsdl.impl.*;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisDescWSDLComponentFactory implements WSDLComponentFactory {

	
	public WSDLDescription createDescription() {
		return new WSDLDescriptionImpl();
	}

	
	public WSDLService createService() {
		return new ServiceDescription();
	}

	
	public WSDLInterface createInterface() {
		return new WSDLInterfaceImpl();
	}

	
	public WSDLTypes createTypes() {
		return new WSDLTypesImpl();
	}

	
	public WSDLBinding createBinding() {
		return new WSDLBindingImpl();
	}

	
	public WSDLOperation createOperation() {
		return new WSDLOperationImpl();
	}

	
	public WSDLEndpoint createEndpoint() {
		return new WSDLEndpointImpl();
	}

	
	public WSDLFault createFault() {
		return new WSDLFaultImpl();
	}

	
	public WSDLFeature createFeature() {
		return new WSDLFeatureImpl();
	}

	
	public WSDLImport createImport() {
		return new WSDLImportImpl();
	}

	
	public WSDLInclude createInclude() {
		return new WSDLIncludeImpl();
	}

	
	public WSDLProperty createProperty() {
		return new WSDLPropertyImpl();
	}
	
	public MessageReference createMessageReference(){
		return new MessageReferenceImpl();
	}
	
	public WSDLBindingMessageReference createWSDLBindingMessageReference(){
		return new WSDLBindingMessageReferenceImpl();
	}
	
	public WSDLBindingOperation createWSDLBindingOperation(){
		return new WSDLBindingOperationImpl();
	}
	
	public WSDLExtensibilityElement createWSDLExtensibilityElement(){
		return new WSDLExtensibilityElementImpl();
	}

}
