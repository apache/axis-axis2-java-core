package org.apache.axis.description;

import org.apache.axis.wsdl.builder.WSDLComponentFactory;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLFault;
import org.apache.wsdl.WSDLFeature;
import org.apache.wsdl.WSDLImport;
import org.apache.wsdl.WSDLInclude;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLProperty;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.impl.MessageReferenceImpl;
import org.apache.wsdl.impl.WSDLBindingImpl;
import org.apache.wsdl.impl.WSDLBindingMessageReferenceImpl;
import org.apache.wsdl.impl.WSDLBindingOperationImpl;
import org.apache.wsdl.impl.WSDLDescriptionImpl;
import org.apache.wsdl.impl.WSDLEndpointImpl;
import org.apache.wsdl.impl.WSDLExtensibilityElementImpl;
import org.apache.wsdl.impl.WSDLFaultImpl;
import org.apache.wsdl.impl.WSDLFeatureImpl;
import org.apache.wsdl.impl.WSDLImportImpl;
import org.apache.wsdl.impl.WSDLIncludeImpl;
import org.apache.wsdl.impl.WSDLInterfaceImpl;
import org.apache.wsdl.impl.WSDLOperationImpl;
import org.apache.wsdl.impl.WSDLPropertyImpl;
import org.apache.wsdl.impl.WSDLTypesImpl;

/**
 * @author chathura@opensource.lk
 *
 */
public class AxisDescWSDLComponentFactory implements WSDLComponentFactory {

	
	public WSDLDescription createDescription() {
		return new WSDLDescriptionImpl();
	}

	
	public WSDLService createService() {
		return new AxisService();
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
