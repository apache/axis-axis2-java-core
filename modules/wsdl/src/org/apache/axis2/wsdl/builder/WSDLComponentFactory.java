package org.apache.axis2.wsdl.builder;

import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionFactory;

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
    public WSDLFaultReference createFaultReference();

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
     * @return A new instance of <code>MessageReference</code>
     */
    public MessageReference createMessageReference();

    /**
     * @return A new instance of <code>WSDLBindingMessageReference</code>
     */
    public WSDLBindingMessageReference createWSDLBindingMessageReference();

    /**
     * @return A new instance of <code>WSDLBindingOperation</code>
     */
    public WSDLBindingOperation createWSDLBindingOperation();


    /**
     * @return A new instance of <code>WSDLExtensibilityAttribute</code>
     */
    public WSDLExtensibilityAttribute createWSDLExtensibilityAttribute();

    /**
     * @return A new instance of <code>WSDLBindingFault</code>
     */
    public WSDLBindingFault createBindingFault();

    /**
     * @return A new Instance of <code>ExtensionFactory</code> that
     *         is capable of creating the correct <code>ExtensibilityElement</code>
     *         given a <code>QName</code>.
     */
    public ExtensionFactory createExtensionFactory();

}
