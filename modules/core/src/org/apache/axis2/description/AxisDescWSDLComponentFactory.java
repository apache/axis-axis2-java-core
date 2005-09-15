package org.apache.axis2.description;

import org.apache.axis2.wsdl.builder.WSDLComponentFactory;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionFactory;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.*;

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
        return new OperationDescription();
    }


    public WSDLEndpoint createEndpoint() {
        return new WSDLEndpointImpl();
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

    public MessageReference createMessageReference() {
        return new MessageReferenceImpl();
    }

    public WSDLBindingMessageReference createWSDLBindingMessageReference() {
        return new WSDLBindingMessageReferenceImpl();
    }

    public WSDLBindingOperation createWSDLBindingOperation() {
        return new WSDLBindingOperationImpl();
    }

    public WSDLExtensibilityAttribute createWSDLExtensibilityAttribute() {
        return new WSDLExtensibilityAttributeImpl();
    }

    /**
     * @return A new Instance of <code>ExtensionFactory</code> that
     *         is capable of creating the correct <code>ExtensibilityElement</code>
     *         given a <code>QName</code>.
     */
    public ExtensionFactory createExtensionFactory() {
        return new ExtensionFactoryImpl();
    }

    public WSDLFaultReference createFaultReference() {
        return new WSDLFaultReferenceImpl();
    }

    public WSDLBindingFault createBindingFault() {
        return new WSDLBindingFaultImpl();
    }

}
