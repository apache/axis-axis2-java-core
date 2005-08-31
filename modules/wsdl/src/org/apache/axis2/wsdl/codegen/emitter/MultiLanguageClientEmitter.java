package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.XSLTConstants;
import org.apache.axis2.wsdl.codegen.writer.*;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPOperation;
import org.apache.wsdl.extensions.SOAPHeader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
* todo Add references to relevant shcemas !
*/


public abstract class MultiLanguageClientEmitter implements Emitter {

    private Log log = LogFactory.getLog(getClass());

    /*
    *  Important! These constants are used in some places in the templates. Care should
    *  be taken when changing them
    */
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
    private static final String SERVICE_XML_OUTPUT_FOLDER_NAME = "service_descriptors.";


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
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitSkeleton()
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {
            //get the interface
            WSDLDescription wom = this.configuration.getWom();
            int codegenStyle = this.configuration.getCodeGenerationStyle();
            if (codegenStyle== XSLTConstants.CodegenStyle.INTERFACE){
                emitSkeletonInterface(wom);
            }else if (codegenStyle== XSLTConstants.CodegenStyle.BINDING){
                emitSkeletonBinding(wom);
            }else if (codegenStyle==XSLTConstants.CodegenStyle.AUTOMATIC){
                emitSkeletonAutomatic(wom);
            }else{
                throw new Exception("Unknown code generation style!!! " + codegenStyle);
            }

            // Call the emit stub method to generate the client side too
            // Do we need to enforce this here ?????
            // Perhaps we can introduce a flag to determine this!
            emitStub();

        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }
    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {
        try {
            //get the interface
            WSDLDescription wom = this.configuration.getWom();
            int codegenStyle = this.configuration.getCodeGenerationStyle();
            if (codegenStyle== XSLTConstants.CodegenStyle.INTERFACE){
                emitStubInterface(wom);
            }else if (codegenStyle== XSLTConstants.CodegenStyle.BINDING){
                emitStubBinding(wom);
            }else if (codegenStyle==XSLTConstants.CodegenStyle.AUTOMATIC){
                emitStubAutomatic(wom);
            }else{
                throw new Exception("Unknown code generation style!!! " + codegenStyle);
            }

        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Emit the skeleton with inteface only
     * @param wom
     */
    private void emitSkeletonInterface(WSDLDescription wom) throws Exception{

        Map interfaces = wom.getWsdlInterfaces();

        //loop through the wsdlInterfaces to generate the interface code
        //theoretically the interface should be the base for the interfaces
        Collection interfaceCollection =interfaces.values();
        for (Iterator iterator = interfaceCollection.iterator(); iterator.hasNext();) {
            WSDLInterface axisInteface = (WSDLInterface) iterator.next();
            //write skeleton
            writeSkeleton(axisInteface, null);
            //write interface implementations
            writeServiceXml(axisInteface, null);
        }


        log.info("Interface mode is selected.The following items will not be generated");
        log.info("1. Message Receiver");
    }

    /**
     * Emit the skelton with binding
     * @param wom
     */
    private void emitSkeletonBinding(WSDLDescription wom) throws Exception{
        Map bindings = wom.getBindings();
        if (bindings==null || bindings.isEmpty()){
            //asking for a code generation with a binding when a binding is
            //not present should be the cause of an Exception !
            throw new Exception("Cannot find a binding!!");

        }else{
            WSDLBinding axisBinding;
            Collection bindingCollection = bindings.values();
            for (Iterator iterator = bindingCollection.iterator(); iterator.hasNext();) {
                axisBinding  =  (WSDLBinding)iterator.next();

                //write skeleton
                writeSkeleton(axisBinding.getBoundInterface(), axisBinding);
                //write service xml
                writeServiceXml(axisBinding.getBoundInterface(), axisBinding);
                //write a MessageReceiver for this particular service.
                writeMessageReceiver(axisBinding);
            }
        }
    }

    /**
     * Skeleton emission - Automatic mode
     * @param wom
     */
    private void emitSkeletonAutomatic(WSDLDescription wom) throws Exception{
        Map bindings = wom.getBindings();
        if (bindings==null || bindings.isEmpty()){
            //No binding is not present.use the interface mode
            emitSkeletonInterface(wom);
        }else{
            //use the binding mode
            emitSkeletonBinding(wom);
        }

    }
    /**
     * Emits the stub code with the interfaces only
     * @param wom
     * @throws Exception
     */
    private void emitStubInterface(WSDLDescription wom) throws Exception{
        Map interfaces = wom.getWsdlInterfaces();
        //loop through the wsdlInterfaces to generate the interface code
        Collection interfaceCollection =interfaces.values();
        for (Iterator iterator = interfaceCollection.iterator(); iterator.hasNext();) {
            //Write the interfaces
            WSDLInterface axisInterface = (WSDLInterface) iterator.next();
            writeInterface(axisInterface, null);
            //write the call back handlers
            writeCallBackHandlers(axisInterface, null);
        }

        //log the message stating that the binding dependent parts are not generated
        log.info("Interface code generation was selected! The following items are not generated");
        log.info("1. Stub");
        log.info("2. CallbackHandler");
        log.info("3. Test Classes");
        log.info("4. Databinding Supporters");
    }

    /**
     * Emit the stubcode with bindings
     * @param wom
     * @throws Exception
     */
    private void emitStubBinding(WSDLDescription wom) throws Exception{
        Map bindings = wom.getBindings();
        if (bindings==null || bindings.isEmpty()){
            //asking for a code generation with a binding when a binding is
            //not present should be the cause of an Exception !
            throw new Exception("Cannot find a binding!!");
        }else{
            WSDLBinding axisBinding ;
            WSDLService axisService = null;
            Collection bindingCollection = bindings.values();
            for (Iterator iterator = bindingCollection.iterator(); iterator.hasNext();) {

                axisBinding  =  (WSDLBinding)iterator.next();
                //Check the service
                axisService = checkService(wom, axisService);
                //write the inteface
                //feed the binding information also
                writeInterface(axisBinding.getBoundInterface(), axisBinding);
                //write the call back handlers
                writeCallBackHandlers(axisBinding.getBoundInterface(), axisBinding);
                //write interface implementations
                writeInterfaceImplementation(axisBinding, axisService);
                //write the test classes
                writeTestClasses(axisBinding);
                //write the databinding supporters
                writeDatabindingSupporters(axisBinding);
                //write a dummy implementation call for the tests to run.
                //writeTestSkeletonImpl(axisBinding);
                //write a testservice.xml that will load the dummy skeleton impl for testing
                //writeTestServiceXML(axisBinding);
            }
        }
    }

    /**
     * emit the stubcode with the automatic mode. Look for the binding and if present
     * emit the skeleton with the binding. Else go for the interface
     * @param wom
     */
    private void emitStubAutomatic(WSDLDescription wom) throws Exception{
        Map bindings = wom.getBindings();
        if (bindings==null || bindings.isEmpty()){
            //No binding is not present.use the interface mode
            emitStubInterface(wom);
        }else{
            //use the binding mode
            emitStubBinding(wom);
        }
    }

    /**
     * Check the service for compatibility. Am incompatible service consists of
     * multiple services ATM
     * @param wom
     * @param axisService
     * @return  the WSDLService object
     */
    private WSDLService checkService(WSDLDescription wom, WSDLService axisService) {
        Map services = wom.getServices();
        if (!services.isEmpty()) {
            if (services.size() == 1) {
                axisService = (WSDLService) services.values().toArray()[0];
            } else {
                throw new UnsupportedOperationException(
                        "Single service WSDL files only");
            }
        }
        return axisService;
    }




    /**
     * Write the callback handlers
     */
    protected void writeCallBackHandlers(WSDLInterface wsdlInterface, WSDLBinding axisBinding) throws Exception {

        if (configuration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler(
                    wsdlInterface, axisBinding);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(
                            this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(interfaceModel, callbackWriter);
        }

    }



    /**
     *
     */
    protected void writeTestClasses(WSDLBinding binding) throws Exception {

        if (configuration.isWriteTestCase()) {
            Document classModel = createDOMDocumentForTestCase(binding);
            TestClassWriter callbackWriter =
                    new TestClassWriter(this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage());
            writeClass(classModel, callbackWriter);
        }

    }

    /**
     * Writes the interfaces
     * @param axisInterface
     * @param axisBinding
     * @throws Exception
     */
    protected void writeInterface(WSDLInterface axisInterface, WSDLBinding axisBinding) throws Exception {
        Document interfaceModel = createDOMDocumentForInterface(axisInterface, axisBinding);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage());
        writeClass(interfaceModel, interfaceWriter);
    }


    /**
     * Writes the skeleton
     *
     * @param axisInteface
     * @param axisBinding
     * @throws Exception
     */
    protected void writeSkeleton(WSDLInterface axisInteface, WSDLBinding axisBinding) throws Exception {

        //Note -  One can generate the skeleton using the interface XML
        Document skeletonModel = createDOMDocumentForSkeleton(axisInteface, axisBinding);
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

        String portTypeName = axisBinding.getBoundInterface().getName().getLocalPart();
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            //Note -  there will be a supporter generated per method and will contain the methods to serilize and
            //deserailize the relevant objects

            WSDLOperation operation = (WSDLOperation) iterator.next();
            WSDLBindingOperation bindingop = axisBinding.getBindingOperation(operation.getName());
            Document databindingSupporterModel = createDOMDocumentforSerialization(
                    operation,portTypeName, bindingop);
            ClassWriter databindingSupportWriter = new DatabindingSupportClassWriter(
                    this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage(),
                    this.configuration.getDatabindingType());
            writeClass(databindingSupporterModel, databindingSupportWriter);
        }

    }


    /**
     * Writes the skeleton
     *
     * @param axisInterface
     * @param axisBinding
     * @throws Exception
     */
    protected void writeServiceXml(WSDLInterface axisInterface, WSDLBinding axisBinding) throws Exception {
        if (this.configuration.isGenerateDeployementDescriptor()) {
            //Write the service xml in a folder with the
            Document skeletonModel = createDOMDocumentForServiceXML(
                    axisInterface, false, axisBinding);
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
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation(
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




    private Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generating the model for the callbacks
     * @param boundInterface
     * @param axisBinding
     */
    protected Document createDOMDocumentForCallbackHandler(
            WSDLInterface boundInterface, WSDLBinding axisBinding) {
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
        this.loadOperations(boundInterface, doc, rootElement,axisBinding);
        //this.loadOperations(boundInterface, doc, rootElement, "on", "Complete");

        doc.appendChild(rootElement);
        return doc;
    }



    /**
     * Finds the input element for the xml document
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getInputElement(Document doc,
                                      WSDLOperation operation, List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        Element param = getInputParamElement(doc, operation);
        if (param!=null){
            inputElt.appendChild(param);
        }
        List parameterElementList = getParameterElementList(doc,headerParameterQNameList, "header");
        for (int i = 0; i < parameterElementList.size(); i++) {
            inputElt.appendChild((Element)parameterElementList.get(i));
        }
        return inputElt;
    }





    private List getParameterElementList(Document doc, List parameters, String location){
        List parameterElementList = new ArrayList();
        if (parameters!=null && !parameters.isEmpty()) {
            int count = parameters.size();
            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                QName name = (QName) parameters.get(i);
                addAttribute(doc,
                        "name",
                        this.mapper.getParameterName(name),
                        param);
                String typeMapping = this.mapper.getTypeMapping(name);
                String typeMappingStr = typeMapping == null ? "" : typeMapping;
                addAttribute(doc, "type", typeMappingStr, param);
                addAttribute(doc,"location",location,param);
                parameterElementList.add(param);
            }

        }
        return parameterElementList;
    }

    /**
     *
     * @param doc
     * @param operation
     * @return the parameter element
     */
    private Element getInputParamElement(Document doc,
                                         WSDLOperation operation) {
        //todo this should go in a loop
        Element param = doc.createElement("param");
        MessageReference inputMessage = operation.getInputMessage();
        if (inputMessage!=null){
            addAttribute(doc,
                    "name",
                    this.mapper.getParameterName(inputMessage.getElement()),
                    param);
            String typeMapping = this.mapper.getTypeMapping(
                    inputMessage.getElement());
            String typeMappingStr = typeMapping == null ? "" : typeMapping;
            addAttribute(doc, "type", typeMappingStr, param);
            //add this as a body parameter
            addAttribute(doc,"location","body",param);
        }else{
            param = null;
        }



        return param;
    }

    /**
     * Finds the output element for the output element
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getOutputElement(Document doc,
                                       WSDLOperation operation, List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        Element param = getOutputParamElement(doc, operation);
        if (param!=null){
            outputElt.appendChild(param);
        }
        List outputElementList = getParameterElementList(doc,headerParameterQNameList, "header");
        for (int i = 0; i < outputElementList.size(); i++) {
            outputElt.appendChild((Element)outputElementList.get(i));
        }
        return outputElt;
    }


    private Element getOutputParamElement(Document doc,
                                          WSDLOperation operation) {
        Element param = doc.createElement("param");
        MessageReference outputMessage = operation.getOutputMessage();
        String typeMappingStr;
        String parameterName;

        if (outputMessage!=null){
            parameterName =  this.mapper.getParameterName(
                    outputMessage.getElement()) ;
            String typeMapping = this.mapper.getTypeMapping(
                    operation.getOutputMessage().getElement());
            typeMappingStr = typeMapping == null ? "" : typeMapping;

        }else{
            parameterName = "" ;
            typeMappingStr = "";
        }
        addAttribute(doc,"name",parameterName,param);
        addAttribute(doc,"type", typeMappingStr, param);
        //add this as a body parameter
        addAttribute(doc,"location","body",param);

        return param;
    }




    protected Document createDOMDocumentForServiceXML(WSDLInterface boundInterface,
                                                      boolean forTesting, WSDLBinding axisBinding) {

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
            //put the package to be SERVICE_XML_OUTPUT_FOLDER_NAME.interface name
            //this forces the service XML to be written in a folder of it's porttype
            //name
            addAttribute(doc,
                    "package",
                    SERVICE_XML_OUTPUT_FOLDER_NAME+localPart,
                    rootElement);
            addAttribute(doc,
                    "classpackage",
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
        loadOperations(boundInterface, doc, rootElement,axisBinding);
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
     * Creates the DOM tree for the interface creation. Uses the interface
     * @param wsdlInterface
     * @param axisBinding
     */
    protected Document createDOMDocumentForInterface(WSDLInterface wsdlInterface, WSDLBinding axisBinding) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        addAttribute(doc,
                "name",
                wsdlInterface.getName().getLocalPart(),
                rootElement);
        addAttribute(doc,
                "callbackname",
                wsdlInterface.getName().getLocalPart() +
                        CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(wsdlInterface, doc, rootElement,axisBinding);
        doc.appendChild(rootElement);
        return doc;

    }


    /**
     * Create the model for the skeleton
     * @param boundInterface
     * @param axisBinding
     * @return documentModel for the skeleton
     */
    protected Document createDOMDocumentForSkeleton(WSDLInterface boundInterface, WSDLBinding axisBinding) {

        Document doc = getEmptyDocument();
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
        loadOperations(boundInterface, doc, rootElement,axisBinding);
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

    /**
     * Loads operations based on the interface
     * @param boundInterface
     * @param doc
     * @param rootElement
     */
    private void loadOperations(WSDLInterface boundInterface,
                                Document doc,
                                Element rootElement) {
        loadOperations(boundInterface, doc, rootElement, null);
    }


    /**
     *
     * @param boundInterface
     * @param doc
     * @param rootElement
     * @param binding
     */
    private void loadOperations(WSDLInterface boundInterface,
                                Document doc,
                                Element rootElement,
                                WSDLBinding binding) {

        Collection col = boundInterface.getOperations().values();
        String portTypeName = boundInterface.getName().getLocalPart();

        List soapHeaderInputParameterList = new ArrayList();
        List soapHeaderOutputParameterList = new ArrayList();
        Element methodElement ;
        WSDLOperation operation ;

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
                    portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                    methodElement);

            addAttribute(doc,
                    "mep",
                    operation.getMessageExchangePattern(),
                    methodElement);

            if (null != binding) {
                WSDLBindingOperation bindingOperation =
                        binding.getBindingOperation(operation.getName());
                addSOAPAction(doc, methodElement, bindingOperation);
                addHeaderOperations(soapHeaderInputParameterList,bindingOperation,true);
                addHeaderOperations(soapHeaderOutputParameterList,bindingOperation,false);
            }

            methodElement.appendChild(getInputElement(doc, operation, soapHeaderInputParameterList));
            methodElement.appendChild(getOutputElement(doc, operation, soapHeaderOutputParameterList));

            rootElement.appendChild(methodElement);
          
        }
    }

    private void addHeaderOperations(List soapHeaderParameterQNameList, WSDLBindingOperation bindingOperation,boolean input) {
        Iterator extIterator;
        if (input){
            extIterator = bindingOperation.getInput().getExtensibilityElements().iterator();
        }else{
            extIterator = bindingOperation.getOutput().getExtensibilityElements().iterator();
        }

        while (extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (element.getType().equals(ExtensionConstants.SOAP_HEADER)) {
                SOAPHeader header = (SOAPHeader)element;
                soapHeaderParameterQNameList.add(header.getElement());
            }
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

    protected Document createDOMDocumentForTestCase(WSDLBinding binding) {
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



    protected Document createDOMDocumentforSerialization(
            WSDLOperation operation, String portTypeName, WSDLBindingOperation bindingOperation) {

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
                portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                rootElement);
        addAttribute(doc, "methodname", localPart, rootElement);
        addAttribute(doc,
                "namespace",
                operation.getName().getNamespaceURI(),
                rootElement);
        Element inputParamElement = getInputParamElement(doc, operation);
        if (inputParamElement!=null){
            rootElement.appendChild(inputParamElement);
        }
        Element outputParamElement = getOutputParamElement(doc, operation);
        if (outputParamElement!=null){
            rootElement.appendChild(outputParamElement);
        }

        if (bindingOperation!=null) {
            List headerParameterQNameList= new ArrayList();
            addHeaderOperations(headerParameterQNameList,bindingOperation,true);
            List parameterElementList = getParameterElementList(doc,headerParameterQNameList, "header");
            for (int i = 0; i < parameterElementList.size(); i++) {
                rootElement.appendChild((Element)parameterElementList.get(i));
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList,bindingOperation,false);
            parameterElementList = getParameterElementList(doc,headerParameterQNameList, "header");
            for (int i = 0; i < parameterElementList.size(); i++) {
                rootElement.appendChild((Element)parameterElementList.get(i));
            }
        }

        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Creates the DOM tree for implementations
     *
     * @param binding
     * @param service
     */
    protected Document createDOMDocumentForInterfaceImplementation(
            WSDLBinding binding, WSDLService service) {
        WSDLInterface boundInterface = binding.getBoundInterface();

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


}

