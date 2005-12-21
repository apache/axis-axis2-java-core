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
 */

package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.XSLTConstants;
import org.apache.axis2.wsdl.codegen.writer.AntBuildWriter;
import org.apache.axis2.wsdl.codegen.writer.CallbackHandlerWriter;
import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.MessageReceiverWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.codegen.writer.TestClassWriter;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPHeader;
import org.apache.wsdl.extensions.SOAPOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class  MultiLanguageClientEmitter implements Emitter {

    private Log log = LogFactory.getLog(getClass());

    /*
    *  Important! These constants are used in some places in the templates. Care should
    *  be taken when changing them
    */
    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
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
            if (configuration.isGenerateAll()){
                 emitStub();
            }


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
                //write the ant build if not asked for all
                if (!configuration.isGenerateAll()){
                  writeAntBuild(axisBinding.getBoundInterface(),axisBinding);
                }

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
            //No binding is present.use the interface mode
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
            //note that this case we do not care about the wrapping flag
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
                //note that we do not create this interface if the user switched on the wrap classes mode
                if (!configuration.isWrapClasses()){
                    writeInterface(axisBinding.getBoundInterface(), axisBinding);
                }
                //write the call back handlers
                writeCallBackHandlers(axisBinding.getBoundInterface(), axisBinding);
                //write interface implementations
                writeInterfaceImplementation(axisBinding, axisService);
                //write the test classes
                writeTestClasses(axisBinding);

                //write a dummy implementation call for the tests to run.
                //writeTestSkeletonImpl(axisBinding);
                //write a testservice.xml that will load the dummy skeleton impl for testing
                //writeTestServiceXML(axisBinding);
                //write an ant build file
                writeAntBuild(axisBinding.getBoundInterface(),axisBinding);
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
     * Writes the Ant build
     *
     * @param axisInterface
     * @param axisBinding
     * @throws Exception
     */
    protected void writeAntBuild(WSDLInterface axisInterface, WSDLBinding axisBinding) throws Exception {
        //Write the service xml in a folder with the
        Document skeletonModel = createDOMDocumentForAntBuild(
                axisInterface, axisBinding);


        AntBuildWriter antBuildWriter = new AntBuildWriter(
                this.configuration.getOutputLocation(),
                this.configuration.getOutputLanguage());
        antBuildWriter.setDatabindingFramework(this.configuration.getDatabindingType());
        writeClass(skeletonModel, antBuildWriter);
    }

    /**
     * Writes the Service XML
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
        //first check for the policies in this service and write them
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
        writer.loadTemplate();
        writer.createOutFile(
                model.getDocumentElement().getAttribute("package"),
                model.getDocumentElement().getAttribute("name"));
        writer.parse(
                model);
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
                reformatName(boundInterface.getName().getLocalPart(),false) +
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
                String typeMapping = this.mapper.getTypeMappingName(name);
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
        Element param = doc.createElement("param");
        MessageReference inputMessage = operation.getInputMessage();
        if (inputMessage!=null){
            addAttribute(doc,
                    "name",
                    this.mapper.getParameterName(inputMessage.getElementQName()),
                    param);

            //todo modify the code here to unwrap if requested
            String typeMapping = this.mapper.getTypeMappingName(
                    inputMessage.getElementQName());
            addAttribute(doc, "type", typeMapping == null ? "" : typeMapping, param);

            //add an extra attribute to say whether the type mapping is the default
            if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMapping)){
                addAttribute(doc,"default","yes",param);
            }

            //add this as a body parameter
            addAttribute(doc,"location","body",param);
            Iterator iter = inputMessage.getExtensibilityAttributes().iterator();
            while(iter.hasNext()){
                WSDLExtensibilityAttribute att = (WSDLExtensibilityAttribute) iter.next();
                addAttribute(doc, att.getKey().getLocalPart(), att.getValue().toString(), param);
            }
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
                    outputMessage.getElementQName()) ;
            String typeMapping = this.mapper.getTypeMappingName(
                    operation.getOutputMessage().getElementQName());
            typeMappingStr = typeMapping == null ? "" : typeMapping;

        }else{
            parameterName = "" ;
            typeMappingStr = "";
        }
        addAttribute(doc,"name",parameterName,param);
        addAttribute(doc,"type", typeMappingStr, param);
        //add an extra attribute to say whether the type mapping is the default
        if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMappingStr)){
            addAttribute(doc,"default","yes",param);
        }
        //add this as a body parameter
        addAttribute(doc,"location","body",param);

        return param;
    }




    protected Document createDOMDocumentForServiceXML(WSDLInterface boundInterface,
                                                      boolean forTesting, WSDLBinding axisBinding) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        String localPart = reformatName(boundInterface.getName().getLocalPart(),false);
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
        String localPart = reformatName(boundInterface.getName().getLocalPart(),false);
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

        /////////////////////////
        rootElement.appendChild(createDOMElementforDatabinders(doc,binding));
        /////////////////////////
        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Creates the DOM tree for the interface creation. Uses the interface
     * @param wsdlInterface
     * @param axisBinding
     */
    protected Document createDOMDocumentForAntBuild(WSDLInterface wsdlInterface, WSDLBinding axisBinding) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("ant");
        String localPart = reformatName(wsdlInterface.getName().getLocalPart(),false);
        String packageName = configuration.getPackageName();
        String[] dotSeparatedValues = packageName.split("\\.");
        addAttribute(doc,
                "package",
                dotSeparatedValues[0],
                rootElement);

        addAttribute(doc,
                "name",
                localPart,
                rootElement);
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
        String localPart = reformatName(wsdlInterface.getName().getLocalPart(),false);
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);

        addAttribute(doc,
                "name",
                localPart,
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
        String localpart = reformatName(boundInterface.getName().getLocalPart(),false);
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);

        addAttribute(doc,
                "name",
                localpart + SERVICE_CLASS_SUFFIX,
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
        String portTypeName = reformatName(boundInterface.getName().getLocalPart(),false);

        Element methodElement ;
        WSDLOperation operation ;

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {

            List soapHeaderInputParameterList = new ArrayList();
            List soapHeaderOutputParameterList = new ArrayList();

            operation = (WSDLOperation) iterator.next();
            methodElement = doc.createElement("method");
            String localPart = reformatName(operation.getName().getLocalPart(),false);
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
                // todo This can be a prob !!!!!
                if (bindingOperation!=null){
                    addSOAPAction(doc, methodElement, bindingOperation);
                    addHeaderOperations(soapHeaderInputParameterList,bindingOperation,true);
                    addHeaderOperations(soapHeaderOutputParameterList,bindingOperation,false);
                }
            }

            methodElement.appendChild(getInputElement(doc, operation, soapHeaderInputParameterList));
            methodElement.appendChild(getOutputElement(doc, operation, soapHeaderOutputParameterList));

            rootElement.appendChild(methodElement);

        }
    }

    private void addHeaderOperations(List soapHeaderParameterQNameList, WSDLBindingOperation bindingOperation,boolean input) {
        Iterator extIterator;
        if (input){
            extIterator = bindingOperation.getInput()==null?null:bindingOperation.getInput().getExtensibilityElements().iterator();
        }else{
            extIterator = bindingOperation.getOutput()==null?null: bindingOperation.getOutput().getExtensibilityElements().iterator();
        }

        while (extIterator!=null && extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (ExtensionConstants.SOAP_11_HEADER.equals(element.getType())) {
                SOAPHeader header = (SOAPHeader)element;
                soapHeaderParameterQNameList.add(header.getElement());
            }
        }
    }


    private void addSOAPAction(Document doc,
                               Element rootElement,
                               WSDLBindingOperation binding) {
        List extensibilityElements = binding.getExtensibilityElements();
        boolean actionAdded = false;
        if (extensibilityElements!=null && !extensibilityElements.isEmpty()){
            Iterator extIterator = extensibilityElements.iterator();
            while (extIterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
                if (ExtensionConstants.SOAP_11_OPERATION.equals(element.getType())
                        || ExtensionConstants.SOAP_12_OPERATION.equals(element.getType()) ) {
                    addAttribute(doc,
                            "soapaction",
                            ((SOAPOperation) element).getSoapAction(),
                            rootElement);
                    actionAdded = true;
                }
            }
        }
        if (!actionAdded) {
            addAttribute(doc, "soapaction", "", rootElement);
        }
    }

    protected Document createDOMDocumentForTestCase(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();
        String localPart = reformatName(boundInterface.getName().getLocalPart(),false);
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);

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



    protected Element createDOMElementforDatabinders(
            Document doc, WSDLBinding binding) {
        //First Iterate through the operations and find the relevant fromOM and toOM methods to be generated
        Map bindingOperationsMap =  binding.getBindingOperations();

        Map parameterMap = new HashMap();
        Iterator operationsIterator =  bindingOperationsMap.values().iterator();
        while (operationsIterator.hasNext()) {
            WSDLBindingOperation bindingOperation = (WSDLBindingOperation) operationsIterator.next();
            //Add the parameters to a map with their type as the key
            //this step is needed to remove repetitions

            //process the input and output parameters
            Element inputParamElement = getInputParamElement(doc, bindingOperation.getOperation());
            if (inputParamElement!=null){
                parameterMap.put(inputParamElement.getAttribute("type"),inputParamElement);
            }
            Element outputParamElement = getOutputParamElement(doc, bindingOperation.getOperation());
            if (outputParamElement!=null){
                parameterMap.put(outputParamElement.getAttribute("type"),outputParamElement);
            }

            //todo process the exceptions

            //process the header parameters
            Element newChild;
            List headerParameterQNameList= new ArrayList();
            addHeaderOperations(headerParameterQNameList,bindingOperation,true);
            List parameterElementList = getParameterElementList(doc,headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"),newChild);
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList,bindingOperation,false);
            parameterElementList = getParameterElementList(doc,headerParameterQNameList, "header");
            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"),newChild);
            }

        }

        Element rootElement = doc.createElement("databinders");
        addAttribute(doc,"dbtype",configuration.getDatabindingType(),rootElement);

        //add the names of the elements that have base 64 content
        //if the base64 name list is missing then this whole step is skipped
        rootElement.appendChild(getBase64Elements(doc));

        //Now run through the parameters and add them to the root element
        Collection parameters = parameterMap.values();
        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            rootElement.appendChild((Element)iterator.next());
        }


        return rootElement;
    }

    /**
     * Get the base64 types. If not available this will be empty!!!
     * @param doc
     * @return element
     */
    private Element getBase64Elements(Document doc){
        Element root = doc.createElement("base64Elements");
        Element elt;
        QName qname;
        //this is a list of QNames
        List list = (List)configuration.getProperties().get(XSLTConstants.BASE_64_PROPERTY_KEY);
        if (list!=null && !list.isEmpty()){
            int count = list.size();
            for (int i = 0; i < count; i++) {
                qname = (QName)list.get(i);
                elt = doc.createElement("name") ;
                addAttribute(doc,"ns-url",qname.getNamespaceURI(),elt);
                addAttribute(doc,"localName",qname.getLocalPart(),elt);
                root.appendChild(elt);
            }
        }

        return root;
    }
    /**
     * Creates the DOM tree for implementations
     *
     * @param binding
     * @param service
     */
    protected Document createDOMDocumentForInterfaceImplementation(
            WSDLBinding binding, WSDLService service) throws Exception {
        WSDLInterface boundInterface = binding.getBoundInterface();
        HashMap endpoints = new HashMap(1);
        if (service!=null){
            endpoints = service.getEndpoints();
        }

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,
                "package",
                configuration.getPackageName(),
                rootElement);
        String localPart = reformatName(boundInterface.getName().getLocalPart(),false);
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
        //add the wrap classes flag
        if (configuration.isWrapClasses()){
            addAttribute(doc,
                    "wrapped",
                    "yes",
                    rootElement);
        }

        //todo fix this
        addAttribute(doc,
                "dbsupportpackage",
                configuration.getPackageName() +
                        DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);

        //add SOAP version
        addSoapVersion(binding,doc,rootElement);
        //add the end point
        addEndpoints(doc, rootElement, endpoints);
        //set the sync/async attributes
        fillSyncAttributes(doc, rootElement);
        //load the operations
        loadOperations(boundInterface, doc, rootElement, binding);

        //add the databind supporters. Now the databind supporters are completly contained inside
        //the stubs implementation and not visible outside
        rootElement.appendChild(
                createDOMElementforDatabinders(doc,binding));


        doc.appendChild(rootElement);

        return doc;


    }

    protected void addSoapVersion(WSDLBinding binding,Document doc,Element rootElement){
        //loop through the extensibility elements to get to the bindings element
        List extensibilityElementsList = binding.getExtensibilityElements();
        int count = extensibilityElementsList.size();
        for (int i = 0; i < count; i++) {
            WSDLExtensibilityElement extElement =  (WSDLExtensibilityElement)extensibilityElementsList.get(i);
            if (ExtensionConstants.SOAP_11_BINDING.equals(extElement.getType())){
                addAttribute(doc,"soap-version", "1.1",rootElement);
                break;
            }else if (ExtensionConstants.SOAP_12_BINDING.equals(extElement.getType())){
                addAttribute(doc,"soap-version", "1.2",rootElement);
                break;
            }
        }
    }
    /**
     * Add the endpoint to the document
     * @param doc
     * @param rootElement
     * @param endpointMap
     */

    protected void addEndpoints(Document doc,
                                Element rootElement,
                                HashMap endpointMap) throws Exception{
        // Map endpointPolicyMap = configuration.getPolicyMap();
        Object[] endpoints = endpointMap.values().toArray();
        WSDLEndpoint endpoint;
        Element endpointElement;
        Text text;
        for (int i = 0; i < endpoints.length; i++) {
            endpoint = (WSDLEndpoint) endpoints[i];

            //attach the policy for this endpoint here
//            String policyFileResourceName = null;
//            Policy policy =  (Policy)endpointPolicyMap.get(endpoint.getName());
//            if (policy!=null){
//                //process the policy for this end point
//                 policyFileResourceName = processPolicy(policy,endpoint.getName().getLocalPart());
//
//            }


            endpointElement = doc.createElement("endpoint");
            org.apache.wsdl.extensions.SOAPAddress address = null;
            Iterator iterator = endpoint.getExtensibilityElements().iterator();
            while (iterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator.next();
                if (ExtensionConstants.SOAP_11_ADDRESS.equals(element.getType()) ||
                        ExtensionConstants.SOAP_12_ADDRESS.equals(element.getType())){
                    address = (org.apache.wsdl.extensions.SOAPAddress) element;
                }
            }
            text = doc.createTextNode(address!=null?address.getLocationURI():"");
//            if (policyFileResourceName!=null){
//                addAttribute(doc,"policyRef",policyFileResourceName,endpointElement);
//            }
            endpointElement.appendChild(text);
            rootElement.appendChild(endpointElement);
        }

    }

    /**
     * Commented for now for the
     * @param policy
     * @return  the file name and the package of this policy
     */
//    private String processPolicy(Policy policy,String name) throws Exception{
//        PolicyFileWriter writer = new PolicyFileWriter(configuration.getOutputLocation());
//        String fileName = name + "_policy";
//        writer.createOutFile(configuration.getPackageName(),fileName);
//        FileOutputStream stream = writer.getStream();
//        if (stream!=null) WSPolicyParser.getInstance().printModel(policy,stream);
//
//        String packageName = configuration.getPackageName();
//        String fullFileName = "\\" + packageName.replace('.','\\') +"\\"+fileName+ ".xml";
//        return fullFileName;
//
//    }

    /**
     * Utility method to add an attribute to a given element
     * @param document
     * @param AttribName
     * @param attribValue
     * @param element
     */
    protected void addAttribute(Document document,
                                String AttribName,
                                String attribValue,
                                Element element) {
        XSLTUtils.addAttribute(document,AttribName,attribValue,element);
    }


    /**
     *
     * @param word
     * @return character removed string
     */
    protected String reformatName(String word) {
        return reformatName(word,true);
    }
    /**
     *
     * @param word
     * @return character removed string
     */
    protected String reformatName(String word, boolean decapitalizaFirst) {
        if (JavaUtils.isJavaKeyword(word)){
            return JavaUtils.makeNonJavaKeyword(word);
        }else{
            return JavaUtils.xmlNameToJava(word,decapitalizaFirst);
        }
    }


}

