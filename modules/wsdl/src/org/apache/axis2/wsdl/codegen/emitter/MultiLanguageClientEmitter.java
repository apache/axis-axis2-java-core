package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.Constants;
import org.apache.axis2.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis2.wsdl.codegen.writer.BeanWriter;
import org.apache.axis2.wsdl.codegen.writer.CallbackHandlerWriter;
import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.DatabindingSupportClassWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.LocalTestClassWriter;
import org.apache.axis2.wsdl.codegen.writer.MessageReceiverWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.codegen.writer.TestClassWriter;
import org.apache.axis2.wsdl.codegen.writer.TestServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.TestSkeletonImplWriter;
import org.apache.axis2.wsdl.databinding.TypeMapper;
//import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.wsdl.extensions.SOAPOperation;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Document;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
* Abstract Client emitter
* the XML will look like the following
* todo escape the following
* <pre>
<interface package="">
<method name="">
<input>
<param name="" type=""/>*
</input> ?
<output>
<param name="" type=""/>?
</output>?
</method>
</interface>
</pre>
*/


public abstract class MultiLanguageClientEmitter implements Emitter {
    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
    private static final String LOCAL_TEST_SUFFIX = "LocalTest";
    private static final String SERVICE_CLASS_SUFFIX = "Skeleton";
    private static final String TEST_PACKAGE_NAME_SUFFIX = ".test";
    private static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";
    private static final String DATABINDING_PACKAGE_NAME_SUFFIX = ".databinding";
    private static final String TEST_SERVICE_CLASS_NAME_SUFFIX = "SkeletonTest";
    private static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";


    protected InputStream xsltStream = null;
    protected CodeGenConfiguration configuration;
    protected TypeMapper mapper;

    /**
     * Sets the mapper
     *
     * @param mapper
     * @see org.apache.axis2.wsdl.databinding.TypeMapper
     */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the code generator configuration
     *
     * @param configuration
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {
        try {
            //get the binding
            WSDLDescription wom = this.configuration.getWom();
            WSDLBinding axisBinding = wom.getBinding(
                    AxisBindingBuilder.AXIS_BINDING_QNAME);
            WSDLService axisService = null;
            //write interfaces
            Map services = wom.getServices();
            if (!services.isEmpty()) {
                if (services.size() == 1) {
                    axisService = (WSDLService) services.values().toArray()[0];
                } else {
                    throw new UnsupportedOperationException(
                            "Single service WSDL files only");
                }
            }
            //
            testCompatibiltyAll(axisBinding);
            //
            writeInterface(axisBinding);
            //write interface implementations
            writeInterfaceImplementation(axisBinding, axisService);
            //write the call back handlers
            writeCallBackHandlers(axisBinding);
            //write the test classes
//            writeTestClasses(axisBinding);
            //write the databinding supporters
            writeDatabindingSupporters(axisBinding);
            //write a dummy implementation call for the tests to run.
            //writeTestSkeletonImpl(axisBinding);
            //write a testservice.xml that will load the dummy skeleton impl for testing
            //writeTestServiceXML(axisBinding);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CodeGenerationException(e);
        }
    }


    protected void writeTestSkeletonImpl(WSDLBinding binding) throws Exception {
        if (configuration.isWriteTestCase()) {
            Document classModel = createDocumentForTestSkeletonImpl(binding);
            TestSkeletonImplWriter callbackWriter =
                    new TestSkeletonImplWriter(
                            this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(classModel, callbackWriter);
        }
    }

    /**
     *
     */
    protected void writeCallBackHandlers(WSDLBinding binding) throws Exception {

        if (configuration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler(
                    binding);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(
                            this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(interfaceModel, callbackWriter);
        }

    }

    /**
     * Write the local test classes. these test classes are different from the
     * usual test classes because it is meant to work with the generated test
     * skeleton class.
     *
     * @param binding
     * @throws Exception
     */
    protected void writeLocalTestClasses(WSDLBinding binding) throws Exception {

        if (configuration.isWriteTestCase()) {
            Document classModel = createDOMDocuementForLocalTestCase(
                    binding);
            LocalTestClassWriter callbackWriter =
                    new LocalTestClassWriter(
                            this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(classModel, callbackWriter);
        }
    }

    /**
     *
     */
    protected void writeTestClasses(WSDLBinding binding) throws Exception {

        if (configuration.isWriteTestCase()) {
            Document classModel = createDOMDocuementForTestCase(binding);
            TestClassWriter callbackWriter =
                    new TestClassWriter(this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(classModel, callbackWriter);
        }

    }

    /**
     * Writes the interfaces
     *
     * @param axisBinding
     * @throws Exception
     */
    protected void writeInterface(WSDLBinding axisBinding) throws Exception {
        Document interfaceModel = createDOMDocuementForInterface(
                axisBinding);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage());
        writeClass(interfaceModel, interfaceWriter);
    }

    /**
     * Writes the skeleton
     *
     * @param axisBinding
     * @throws Exception
     */
    protected void writeSkeleton(WSDLBinding axisBinding) throws Exception {

        //Note -  One can generate the skeleton using the interface XML
        Document skeletonModel = createDOMDocuementForSkeleton(axisBinding);
        ClassWriter skeletonWriter = new SkeletonWriter(
                this.configuration.getOutputLocation(),
                this.configuration.getOutputLanguage());
        writeClass(skeletonModel, skeletonWriter);


    }

    /**
     * Writes the skeleton
     *
     * @param axisBinding
     * @throws Exception
     */
    protected void writeDatabindingSupporters(WSDLBinding axisBinding) throws Exception {
        Collection col = axisBinding.getBoundInterface().getOperations()
                .values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            //Note -  there will be a supporter generated per method and will contain the methods to serilize and
            //deserilize the relevant objects
            Document databindingSupporterModel = createDOMDocumentforSerialization(
                    (WSDLOperation) iterator.next());
            ClassWriter databindingSupportWriter = new DatabindingSupportClassWriter(
                    this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage(),
                    this.configuration.getDatabindingType());
            writeClass(databindingSupporterModel, databindingSupportWriter);
        }

    }

    protected void writeTestServiceXML(WSDLBinding axisBinding) throws Exception {
        if (this.configuration.isWriteTestCase()) {
            //Note -  One can generate the service xml using the interface XML
            Document skeletonModel = createDOMDocuementForServiceXML(
                    axisBinding, true);
            TestServiceXMLWriter testServiceXmlWriter = new TestServiceXMLWriter(
                    this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage());
            writeClass(skeletonModel, testServiceXmlWriter);
        }
    }

    /**
     * Writes the skeleton
     *
     * @param axisBinding
     * @throws Exception
     */
    protected void writeServiceXml(WSDLBinding axisBinding) throws Exception {
        if (this.configuration.isGenerateDeployementDescriptor()) {
            Document skeletonModel = createDOMDocuementForServiceXML(
                    axisBinding, false);
            ClassWriter serviceXmlWriter = new ServiceXMLWriter(
                    this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage());
            writeClass(skeletonModel, serviceXmlWriter);
        }
    }


    /**
     * Writes the implementations
     *
     * @param axisBinding
     * @throws Exception
     */
    protected void writeInterfaceImplementation(WSDLBinding axisBinding,
                                                WSDLService service) throws Exception {
        Document interfaceImplModel = createDOMDocuementForInterfaceImplementation(
                axisBinding, service);
        InterfaceImplementationWriter writer =
                new InterfaceImplementationWriter(
                        this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage());
        writeClass(interfaceImplModel, writer);
    }

    protected void writeMessageReceiver(WSDLBinding axisBinding) throws Exception {
        if (configuration.isWriteMessageReceiver()) {
            Document classModel = createDocumentForMessageReceiver(
                    axisBinding);
            MessageReceiverWriter writer =
                    new MessageReceiverWriter(
                            this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(classModel, writer);
        }
    }

    /**
     * todo Not used yet
     *
     * @param wsdlType
     * @throws Exception
     */
    protected void writeBeans(WSDLTypes wsdlType) throws Exception {
        Collection collection = wsdlType.getExtensibilityElements();
        if (collection != null) {
            for (Iterator iterator = collection.iterator();
                 iterator.hasNext();) {
                Document interfaceModel = createDOMDocuementForBean();
                BeanWriter beanWriter =
                        new BeanWriter(this.configuration.getOutputLocation(),
                                this.configuration.getOutputLanguage());
                writeClass(interfaceModel, beanWriter);
            }
        }

    }

    /**
     * A resusable method for the implementation of interface and implementation writing
     *
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    protected void writeClass(Document model, ClassWriter writer) throws IOException,
            Exception {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        // Use a Transformer for output
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();

        DOMSource source = new DOMSource(model);
        StreamResult result = new StreamResult(memoryStream);
        transformer.transform(source, result);

        writer.loadTemplate();
        writer.createOutFile(
                model.getDocumentElement().getAttribute("package"),
                model.getDocumentElement().getAttribute("name"));
        writer.writeOutFile(
                new ByteArrayInputStream(memoryStream.toByteArray()));
    }

    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitSkeleton()
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {
            //get the binding
            WSDLBinding axisBinding = this.configuration.getWom().getBinding(
                    AxisBindingBuilder.AXIS_BINDING_QNAME);
            //
            testCompatibiltyAll(axisBinding);
            //write interfaces
            writeSkeleton(axisBinding);
            //write interface implementations
            writeServiceXml(axisBinding);
            //write the local test classes
//            writeLocalTestClasses(axisBinding);
            //write a dummy implementation call for the tests to run.
            writeTestSkeletonImpl(axisBinding);
            //write a testservice.xml that will load the dummy skeleton impl for testing
            writeTestServiceXML(axisBinding);
            //write a MessageReceiver for this particular service.
            writeMessageReceiver(axisBinding);
            //////////////////////////////////////
            // Call the emit stub method to generate the client side too
            emitStub();

        } catch (Exception e) {
            e.printStackTrace();
            throw new CodeGenerationException(e);
        }
    }

    protected Document createDocumentForTestSkeletonImpl(
            WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();

        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName() + TEST_PACKAGE_NAME_SUFFIX,
                rootElement);
        addAttribute(doc,
                "servicename",
                boundInterface.getName().getLocalPart() + SERVICE_CLASS_SUFFIX,
                rootElement);
        addAttribute(doc,
                "implpackage",
                configuration.getPackageName(),
                rootElement);
        addAttribute(doc,
                "name",
                boundInterface.getName().getLocalPart() +
                TEST_SERVICE_CLASS_NAME_SUFFIX,
                rootElement);
        addAttribute(doc,
                "namespace",
                boundInterface.getName().getNamespaceURI(),
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;
    }

    private Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            return doc;
        } catch (ParserConfigurationException e) {
            return null;
        }
    }

    /**
     * Generating the callbacks
     *
     * @param binding
     * @return
     */
    protected Document createDOMDocumentForCallbackHandler(
            WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("callback");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        addAttribute(doc,
                "name",
                boundInterface.getName().getLocalPart() +
                CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        addAttribute(doc,
                "namespace",
                boundInterface.getName().getNamespaceURI(),
                rootElement);

        //TODO JAXRPC mapping support should be considered
        this.loadOperations(boundInterface, doc, rootElement);
        //this.loadOperations(boundInterface, doc, rootElement, "on", "Complete");

        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Finds the input element for the xml document
     *
     * @param doc
     * @param operation
     * @return
     */
    protected Element getInputElement(Document doc,
                                      WSDLOperation operation) {
        Element inputElt = doc.createElement("input");
        Element param = getInputParamElement(doc, operation);
        inputElt.appendChild(param);
        return inputElt;
    }

    private Element getInputParamElement(Document doc,
                                         WSDLOperation operation) {
        //todo this should go in a loop
        Element param = doc.createElement("param");
        MessageReference inputMessage = operation.getInputMessage();
        addAttribute(doc,
                "name",
                this.mapper.getParameterName(inputMessage.getElement()),
                param);
        String typeMapping = this.mapper.getTypeMapping(
                inputMessage.getElement());
        String typeMappingStr = typeMapping == null ? "" : typeMapping;
        addAttribute(doc, "type", typeMappingStr, param);
        return param;
    }

    /**
     * Finds the output element for the output element
     *
     * @param doc
     * @param operation
     * @return
     */
    protected Element getOutputElement(Document doc,
                                       WSDLOperation operation) {
        Element outputElt = doc.createElement("output");
        Element param = getOutputParamElement(doc, operation);
        outputElt.appendChild(param);
        return outputElt;
    }

    private Element getOutputParamElement(Document doc,
                                          WSDLOperation operation) {
        Element param = doc.createElement("param");
        addAttribute(doc,
                "name",
                this.mapper.getParameterName(
                        operation.getOutputMessage().getElement()),
                param);

        String typeMapping = this.mapper.getTypeMapping(
                operation.getOutputMessage().getElement());
        String typeMappingStr = typeMapping == null ? "" : typeMapping;
        addAttribute(doc, "type", typeMappingStr, param);
        return param;
    }

    /**
     * Todo Finish this
     *
     * @return
     */
    protected Document createDOMDocuementForBean() {
        return null;
    }

    protected Document createDOMDocuementForServiceXML(WSDLBinding binding,
                                                       boolean forTesting) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        String localPart = boundInterface.getName().getLocalPart();
        if (forTesting) {
            addAttribute(doc,
                    "package",
                    configuration.getPackageName() + TEST_PACKAGE_NAME_SUFFIX,
                    rootElement);
            addAttribute(doc,
                    "name",
                    localPart + TEST_SERVICE_CLASS_NAME_SUFFIX,
                    rootElement);
            addAttribute(doc,
                    "servicename",
                    localPart + TEST_SERVICE_CLASS_NAME_SUFFIX,
                    rootElement);
        } else {
            addAttribute(doc,
                    "package",
                    configuration.getPackageName(),
                    rootElement);
            addAttribute(doc,
                    "name",
                    localPart + SERVICE_CLASS_SUFFIX,
                    rootElement);
            addAttribute(doc, "servicename", localPart, rootElement);
        }

        addAttribute(doc,
                "messagereceiver",
                localPart + MESSAGE_RECEIVER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);

        return doc;
    }


    protected Document createDocumentForMessageReceiver(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc,
                "name",
                localPart + MESSAGE_RECEIVER_SUFFIX,
                rootElement);
        addAttribute(doc,
                "skeletonname",
                localPart + SERVICE_CLASS_SUFFIX,
                rootElement);
        addAttribute(doc,
                "basereceiver",
                "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver",
                rootElement);
        addAttribute(doc,
                "dbsupportpackage",
                configuration.getPackageName() +
                DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement, binding);
        doc.appendChild(rootElement);


        return doc;
    }

    /**
     * Creates the DOM tree for the interface creation
     *
     * @param binding
     * @return
     */
    protected Document createDOMDocuementForInterface(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        addAttribute(doc,
                "name",
                boundInterface.getName().getLocalPart(),
                rootElement);
        addAttribute(doc,
                "callbackname",
                boundInterface.getName().getLocalPart() +
                CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;

    }

    protected Document createDOMDocuementForSkeleton(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();;
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        addAttribute(doc,
                "name",
                boundInterface.getName().getLocalPart() + SERVICE_CLASS_SUFFIX,
                rootElement);
        addAttribute(doc,
                "callbackname",
                boundInterface.getName().getLocalPart() +
                CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;

    }

    private void fillSyncAttributes(Document doc, Element rootElement) {
        addAttribute(doc,
                "isAsync",
                this.configuration.isAsyncOn() ? "1" : "0",
                rootElement);
        addAttribute(doc,
                "isSync",
                this.configuration.isSyncOn() ? "1" : "0",
                rootElement);
    }

    private void loadOperations(WSDLInterface boundInterface,
                                Document doc,
                                Element rootElement) {
        loadOperations(boundInterface, doc, rootElement, null);
    }

    private void loadOperations(WSDLInterface boundInterface,
                                Document doc,
                                Element rootElement,
                                WSDLBinding binding) {
        Collection col = boundInterface.getOperations().values();
        Element methodElement = null;
        WSDLOperation operation = null;

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            operation = (WSDLOperation) iterator.next();
            methodElement = doc.createElement("method");
            String localPart = operation.getName().getLocalPart();
            addAttribute(doc, "name", localPart, methodElement);
            addAttribute(doc,
                    "namespace",
                    operation.getName().getNamespaceURI(),
                    methodElement);
            addAttribute(doc, "style", operation.getStyle(), methodElement);
            addAttribute(doc,
                    "dbsupportname",
                    localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                    methodElement);
            if (null != binding) {
                WSDLBindingOperation bindingOperation = binding.getBindingOperation(
                        operation.getName());
                addSOAPAction(doc, methodElement, bindingOperation);
                testCompatibilityInput(bindingOperation);
                testCompatibilityOutput(bindingOperation);
            }
            addAttribute(doc,
                    "mep",
                    operation.getMessageExchangePattern(),
                    methodElement);
            methodElement.appendChild(getInputElement(doc, operation));
            methodElement.appendChild(getOutputElement(doc, operation));
            rootElement.appendChild(methodElement);
        }
    }


    private void addSOAPAction(Document doc,
                               Element rootElement,
                               WSDLBindingOperation binding) {
        Iterator extIterator = binding.getExtensibilityElements().iterator();
        boolean actionAdded = false;
        while (extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (element.getType().equals(ExtensionConstants.SOAP_OPERATION)) {
                addAttribute(doc,
                        "soapaction",
                        ((SOAPOperation) element).getSoapAction(),
                        rootElement);
                actionAdded = true;
            }
        }

        if (!actionAdded) {
            addAttribute(doc, "soapaction", "", rootElement);
        }
    }

    protected Document createDOMDocuementForTestCase(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc, "name", localPart + TEST_SUFFIX, rootElement);
        addAttribute(doc,
                "namespace",
                boundInterface.getName().getNamespaceURI(),
                rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc,
                "callbackname",
                localPart + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        addAttribute(doc, "stubname", localPart + STUB_SUFFIX, rootElement);
        addAttribute(doc,
                "dbsupportpackage",
                configuration.getPackageName() +
                DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;

    }

    protected Document createDOMDocuementForLocalTestCase(
            WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        String serviceXMLPath = configuration.getPackageName().replace('.',
                '/') +
                TEST_PACKAGE_NAME_SUFFIX.replace('.', '/') +
                "/testservice.xml";
        addAttribute(doc,
                "package",
                configuration.getPackageName() + TEST_PACKAGE_NAME_SUFFIX,
                rootElement);
        addAttribute(doc, "servicexmlpath", serviceXMLPath, rootElement);
        addAttribute(doc,
                "implpackage",
                configuration.getPackageName(),
                rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc, "name", localPart + LOCAL_TEST_SUFFIX, rootElement);
        addAttribute(doc,
                "namespace",
                boundInterface.getName().getNamespaceURI(),
                rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc,
                "callbackname",
                localPart + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        addAttribute(doc, "stubname", localPart + STUB_SUFFIX, rootElement);
        addAttribute(doc,
                "address",
                "http://localhost:" + Constants.TEST_PORT + "/services/" +
                boundInterface.getName().getLocalPart() +
                TEST_SERVICE_CLASS_NAME_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;

    }

    protected Document createDOMDocumentforSerialization(
            WSDLOperation operation) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName() +
                DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);
        String localPart = operation.getName().getLocalPart();
        addAttribute(doc,
                "name",
                localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                rootElement);
        addAttribute(doc, "methodname", localPart, rootElement);
        addAttribute(doc,
                "namespace",
                operation.getName().getNamespaceURI(),
                rootElement);
        rootElement.appendChild(getInputParamElement(doc, operation));
        rootElement.appendChild(getOutputParamElement(doc, operation));
        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Creates the DOM tree for implementations
     *
     * @param binding
     * @param service
     * @return
     */
    protected Document createDOMDocuementForInterfaceImplementation(
            WSDLBinding binding, WSDLService service) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        WSDLEndpoint endpoint = null;
        HashMap endpoints = service.getEndpoints();
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc, "name", localPart + STUB_SUFFIX, rootElement);
        addAttribute(doc, "servicename", localPart, rootElement);
        addAttribute(doc,
                "namespace",
                boundInterface.getName().getNamespaceURI(),
                rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc,
                "callbackname",
                localPart + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        addAttribute(doc,
                "dbsupportpackage",
                configuration.getPackageName() +
                DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);
        addEndpoints(doc, rootElement, endpoints);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement, binding);
        doc.appendChild(rootElement);


        return doc;

    }

    protected void addEndpoints(Document doc,
                                Element rootElement,
                                HashMap endpointMap) {
        Object[] endpoints = endpointMap.values().toArray();
        WSDLEndpoint endpoint;
        Element endpointElement;
        Text text;
        for (int i = 0; i < endpoints.length; i++) {
            endpoint = (WSDLEndpoint) endpoints[i];
            endpointElement = doc.createElement("endpoint");
            org.apache.wsdl.extensions.SOAPAddress address = null;
            Iterator iterator = endpoint.getExtensibilityElements().iterator();
            while (iterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator.next();
                if (element.getType().equals(ExtensionConstants.SOAP_ADDRESS)) {
                    address = (org.apache.wsdl.extensions.SOAPAddress) element;
                }
            }
            text = doc.createTextNode(address.getLocationURI());     //todo How to get the end point address
            endpointElement.appendChild(text);
            rootElement.appendChild(endpointElement);
        }

    }

    protected void addAttribute(Document document,
                                String AttribName,
                                String attribValue,
                                Element element) {
        Attr attribute = document.createAttribute(AttribName);
        attribute.setValue(attribValue);
        element.setAttributeNode(attribute);
    }

    protected String removeUnsuitableCharacters(String word) {
        return word.replaceAll("\\W", "_");
    }

    private void testCompatibiltyAll(WSDLBinding binding) {
        HashMap map = binding.getBindingOperations();
        WSDLBindingOperation bindingOp;
        Collection col = map.values();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            bindingOp = (WSDLBindingOperation) iterator.next();
            testCompatibilityInput(bindingOp);
            testCompatibilityOutput(bindingOp);
        }


    }

    private void testCompatibilityInput(WSDLBindingOperation binding) {

        Iterator extIterator = binding.getInput().getExtensibilityElements()
                .iterator();
        while (extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (element.getType().equals(ExtensionConstants.SOAP_BODY)) {
                if (WSDLConstants.WSDL_USE_ENCODED.equals(
                        ((SOAPBody) element).getUse())) {
                    throw new RuntimeException(
                            "The use 'encoded' is not supported!");
                }
            }
        }
    }

    private void testCompatibilityOutput(WSDLBindingOperation binding) {

        Iterator extIterator = binding.getOutput().getExtensibilityElements()
                .iterator();
        while (extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (element.getType().equals(ExtensionConstants.SOAP_BODY)) {
                if (WSDLConstants.WSDL_USE_ENCODED.equals(
                        ((SOAPBody) element).getUse())) {
                    throw new RuntimeException(
                            "The use 'encoded' is not supported!");
                }
            }
        }
    }
}

