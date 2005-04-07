package org.apache.axis.wsdl.builder;

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

/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLComponentFactory {

	/**
	 * @return A new instance of type <code>WSDLDescription</code>
	 */
	public WSDLDescription createDescription();

	/**
	 * @return A new instance of type <code>WSDLService</code>
	 */
	public WSDLService createService();

	/**
	 * @return A new instance of type <code>WSDLInterface</code>
	 */
	public WSDLInterface createInterface();

	/**
	 * @return A new instance of type <code>WSDLTypes</code>
	 */
	public WSDLTypes createTypes();

	/**
	 * @return A new instance of type <code>WSDLBinding</code>
	 */
	public WSDLBinding createBinding();

	/**
	 * @return A new instance of type <code>WSDLOperation</code>
	 */
	public WSDLOperation createOperation();

	/**
	 * @return A new instance of type <code>WSDLEndpoint</code>
	 */
	public WSDLEndpoint createEndpoint();

	/**
	 * @return A new instance of type <code>WSDLFault</code>
	 */
	public WSDLFault createFault();

	/**
	 * @return A new instance of type <code>WSDLFeature</code>
	 */
	public WSDLFeature createFeature();

	/**
	 * @return A new instance of type <code>WSDLImport</code>
	 */
	public WSDLImport createImport();

	/**
	 * @return A new instance of type <code>WSDLInclude</code>
	 */
	public WSDLInclude createInclude();

	/**
	 * Method createProperty
	 *
	 * @return A new instance of <code>WSDLProperty</code>
	 */
	public WSDLProperty createProperty();
	
	/**
	 * 
	 * @return A new instance of <code>MessageReference</code>
	 */
	public MessageReference createMessageReference();
	
	/**
	 * 
	 * @return A new instance of <code>WSDLBindingMessageReference</code>
	 */
	public WSDLBindingMessageReference createWSDLBindingMessageReference();
	
	/**
	 * 
	 * @return A new instance of <code>WSDLBindingOperation</code>
	 */
	public WSDLBindingOperation createWSDLBindingOperation();
	
	/**
	 * 
	 * @return A new instance of <code>WSDLExtensibilityElement</code>
	 */
	public WSDLExtensibilityElement createWSDLExtensibilityElement();

}
