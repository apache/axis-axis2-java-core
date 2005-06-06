package org.apache.axis.wsdl.codegen.emitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.codegen.CodeGenerationException;
import org.apache.axis.wsdl.codegen.Constants;
import org.apache.axis.wsdl.codegen.extension.AxisBindingBuilder;
import org.apache.axis.wsdl.codegen.writer.BeanWriter;
import org.apache.axis.wsdl.codegen.writer.CallbackHandlerWriter;
import org.apache.axis.wsdl.codegen.writer.ClassWriter;
import org.apache.axis.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis.wsdl.codegen.writer.TestClassWriter;
import org.apache.axis.wsdl.codegen.writer.TestServiceXMLWriter;
import org.apache.axis.wsdl.codegen.writer.TestSkeltonImplWriter;
import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.crimson.tree.XmlDocument;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Text;



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


public abstract class MultiLanguageClientEmitter implements Emitter{
    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
    private static final String SERVICE_CLASS_SUFFIX ="Skeleton";
    private static final String TEST_PACKAGE_NAME_SUFFIX =".test";
    private static final String TEST_SERVICE_CLASS_NAME_SUFFIX ="SkeletonTest";
    

    protected InputStream xsltStream = null;
    protected CodeGenConfiguration configuration;
    protected TypeMapper mapper;

    /**
     * Sets the mapper
     * @see org.apache.axis.wsdl.databinding.TypeMapper
     * @param mapper
     */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Sets the code generator configuration
     * @param configuration
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     *
     * @see org.apache.axis.wsdl.codegen.emitter.Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {
        try {
            //get the binding
            WSDLDescription wom = this.configuration.getWom();
            WSDLBinding axisBinding = wom.getBinding(AxisBindingBuilder.AXIS_BINDING_QNAME);
            WSDLService axisService = null;
            //write interfaces
            HashMap services = wom.getServices();
            if (!services.isEmpty()) {
                if (services.size()==1){
                    axisService = (WSDLService)services.values().toArray()[0];
                }else{
                    throw new UnsupportedOperationException("Single service WSDL files only");
                }
            }

            writeInterface(axisBinding);
            //write interface implementations
            writeInterfaceImplementation(axisBinding,axisService);
            //write the call back handlers
            writeCallBackHandlers(axisBinding);
            //write the test classes
            writeTestClasses(axisBinding);
            //write a dummy implementation call for the tests to run.
            writeTestSkeletonImpl(axisBinding);
            //write a testservice.xml that will load the dummy skeleton impl for testing
            writeTestServiceXML(axisBinding);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CodeGenerationException(e);
        }
    }
    
    
    protected void writeTestSkeletonImpl(WSDLBinding binding)throws Exception{
    	if (configuration.isWriteTestCase()){
            XmlDocument classModel = createDocumentForTestSkeltonImpl(binding);
            TestSkeltonImplWriter callbackWriter =
                    new TestSkeltonImplWriter(this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage()
                    );
            writeClass(classModel,callbackWriter);
        }
    }
    
    /**
     *
     */
    protected void writeCallBackHandlers(WSDLBinding binding) throws Exception{

        if (configuration.isAsyncOn()){
            XmlDocument interfaceModel = createDOMDocumentForCallbackHandler(binding);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage()
                    );
            writeClass(interfaceModel,callbackWriter);
        }

    }

    /**
     *
     */
    protected void writeTestClasses(WSDLBinding binding) throws Exception{

        if (configuration.isWriteTestCase()){
            XmlDocument classModel = createDOMDocuementForTestCase(binding);
            TestClassWriter callbackWriter =
                    new TestClassWriter(this.configuration.getOutputLocation(),
                            this.configuration.getOutputLanguage()
                    );
            writeClass(classModel,callbackWriter);
        }

    }
    /**
     * Writes the interfaces
     * @param axisBinding
     * @throws Exception
     */
    protected void writeInterface(WSDLBinding axisBinding) throws Exception {
        XmlDocument interfaceModel = createDOMDocuementForInterface(axisBinding);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
        writeClass(interfaceModel,interfaceWriter);
    }

    /**
     * Writes the skeleton
     * @param axisBinding
     * @throws Exception
     */
    protected void writeSkeleton(WSDLBinding axisBinding) throws Exception {

        //Note -  One can generate the skeleton using the interface XML
        XmlDocument skeletonModel = createDOMDocuementForInterface(axisBinding);
        ClassWriter skeletonWriter = new SkeletonWriter(this.configuration.getOutputLocation(),
                this.configuration.getOutputLanguage()
        );
        writeClass(skeletonModel,skeletonWriter);


    }
    
    
	
    protected void writeTestServiceXML(WSDLBinding axisBinding) throws Exception {
        if (this.configuration.isWriteTestCase()){
            //Note -  One can generate the service xml using the interface XML
            XmlDocument skeletonModel = createDOMDocuementForServiceXML(axisBinding, true);
            TestServiceXMLWriter testServiceXmlWriter = new TestServiceXMLWriter(this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage()
            );
            writeClass(skeletonModel,testServiceXmlWriter);
        }
    }

    /**
     * Writes the skeleton
     * @param axisBinding
     * @throws Exception
     */
    protected void writeServiceXml(WSDLBinding axisBinding) throws Exception {
        if (this.configuration.isGenerateDeployementDescriptor()){
            //Note -  One can generate the service xml using the interface XML
            XmlDocument skeletonModel = createDOMDocuementForServiceXML(axisBinding, false);
            ClassWriter serviceXmlWriter = new ServiceXMLWriter(this.configuration.getOutputLocation(),
                    this.configuration.getOutputLanguage()
            );
            writeClass(skeletonModel,serviceXmlWriter);
        }
    }


    /**
     * Writes the implementations
     * @param axisBinding
     * @throws Exception
     */
    protected void writeInterfaceImplementation(WSDLBinding axisBinding,WSDLService service) throws Exception {
        XmlDocument interfaceImplModel = createDOMDocuementForInterfaceImplementation(axisBinding, service);
        InterfaceImplementationWriter writer =
                new InterfaceImplementationWriter(this.configuration.getOutputLocation(),
                        this.configuration.getOutputLanguage()
                );
        writeClass(interfaceImplModel,writer);
    }

    /**
     * todo Not used yet
     * @param wsdlType
     * @throws Exception
     */
    protected void writeBeans(WSDLTypes wsdlType) throws Exception {
        Collection collection= wsdlType.getExtensibilityElements();
        if (collection != null){
            for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
                XmlDocument interfaceModel = createDOMDocuementForBean();
                BeanWriter beanWriter =
                        new BeanWriter(this.configuration.getOutputLocation(),
                                this.configuration.getOutputLanguage()
                        );
                writeClass(interfaceModel,beanWriter);
            }
        }

    }
    /**
     * A resusable method for the implementation of interface and implementation writing
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    private void writeClass(XmlDocument model,ClassWriter writer) throws IOException,Exception {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        model.write(memoryStream);
        writer.loadTemplate();
        writer.createOutFile(model.getDocumentElement().getAttribute("package"),
                model.getDocumentElement().getAttribute("name"));
        writer.writeOutFile(new ByteArrayInputStream(memoryStream.toByteArray()));
    }
    
    

    /**
     * @see org.apache.axis.wsdl.codegen.emitter.Emitter#emitSkeleton()
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {
            //get the binding
            WSDLBinding axisBinding = this.configuration.getWom().getBinding(AxisBindingBuilder.AXIS_BINDING_QNAME);
            //write interfaces
            writeSkeleton(axisBinding);
            //write interface implementations
            writeServiceXml(axisBinding);
            //write the test classes
            writeTestClasses(axisBinding);
            //write a dummy implementation call for the tests to run.
            writeTestSkeletonImpl(axisBinding);
            //write a testservice.xml that will load the dummy skeleton impl for testing
            writeTestServiceXML(axisBinding);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CodeGenerationException(e);
        }
    }

    
    protected XmlDocument createDocumentForTestSkeltonImpl(WSDLBinding binding){
    	WSDLInterface boundInterface = binding.getBoundInterface();

        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,"package",configuration.getPackageName()+TEST_PACKAGE_NAME_SUFFIX, rootElement);
        addAttribute(doc,"servicename",boundInterface.getName().getLocalPart()+SERVICE_CLASS_SUFFIX,rootElement);
        addAttribute(doc, "implpackage", configuration.getPackageName(), rootElement);
        addAttribute(doc,"name",boundInterface.getName().getLocalPart()+TEST_SERVICE_CLASS_NAME_SUFFIX,rootElement);
        addAttribute(doc, "namespace", boundInterface.getName().getNamespaceURI(), rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);

        return doc;
    }
    
    /**
     * Generating the callbacks
     * @param binding
     * @return
     */
    protected XmlDocument createDOMDocumentForCallbackHandler(WSDLBinding binding){
        WSDLInterface boundInterface = binding.getBoundInterface();
        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("callback");
        addAttribute(doc,"package",configuration.getPackageName(),rootElement);
        addAttribute(doc,"name",boundInterface.getName().getLocalPart()+CALL_BACK_HANDLER_SUFFIX,rootElement);
        addAttribute(doc,"namespace",boundInterface.getName().getNamespaceURI(),rootElement);

        //TODO JAXRPC mapping support should be considered
        this.loadOperations(boundInterface, doc, rootElement);
        //this.loadOperations(boundInterface, doc, rootElement, "on", "Complete");

        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Finds the input element for the xml document
     * @param doc
     * @param operation
     * @return
     */
    protected Element getInputElement(XmlDocument doc,WSDLOperation operation){
        Element inputElt = doc.createElement("input");
        //todo this should be multiple
        Element param = doc.createElement("param");
        addAttribute(doc,"name",this.mapper.getParameterName(operation.getInputMessage().getElement()),param);

        Class typeMapping = this.mapper.getTypeMapping(operation.getInputMessage().getElement());
        String typeMappingStr  =typeMapping==null?"":typeMapping.getName();
        addAttribute(doc,"type",typeMappingStr,param);

        inputElt.appendChild(param);

        return inputElt;
    }

    /**
     * Finds the output element for the output element
     * @param doc
     * @param operation
     * @return
     */
    protected Element getOutputElement(XmlDocument doc,WSDLOperation operation){
        Element outputElt = doc.createElement("output");
        Element param = doc.createElement("param");
        addAttribute(doc,"name",this.mapper.getParameterName(operation.getOutputMessage().getElement()),param);

        Class typeMapping = this.mapper.getTypeMapping(operation.getOutputMessage().getElement());
        String typeMappingStr=typeMapping==null?"":typeMapping.getName();
        addAttribute(doc,"type",typeMappingStr,param);
        outputElt.appendChild(param);

        return outputElt;
    }

    /**
     * Todo Finish this
     * @return
     */
    protected XmlDocument createDOMDocuementForBean(){
        return null;
    }

    protected XmlDocument createDOMDocuementForServiceXML(WSDLBinding binding, boolean forTesting) {
    	WSDLInterface boundInterface = binding.getBoundInterface();

        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("interface");
        if(forTesting){
        	addAttribute(doc,"package",configuration.getPackageName()+TEST_PACKAGE_NAME_SUFFIX, rootElement);
	        addAttribute(doc,"name",boundInterface.getName().getLocalPart()+TEST_SERVICE_CLASS_NAME_SUFFIX,rootElement);
        }else{        
	        addAttribute(doc,"package",configuration.getPackageName(), rootElement);
	        addAttribute(doc,"name",boundInterface.getName().getLocalPart()+SERVICE_CLASS_SUFFIX,rootElement);
        }
        addAttribute(doc,"servicename",boundInterface.getName().getLocalPart()+TEST_SERVICE_CLASS_NAME_SUFFIX,rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);

        return doc;
    }
    /**
     * Creates the DOM tree for the interface creation
     * @param binding
     * @return
     */
    protected XmlDocument createDOMDocuementForInterface(WSDLBinding binding){
        WSDLInterface boundInterface = binding.getBoundInterface();

        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,"package",configuration.getPackageName(), rootElement);
        if(this.configuration.isServerSide()){
        	addAttribute(doc,"name",boundInterface.getName().getLocalPart()+SERVICE_CLASS_SUFFIX,rootElement);
        }else{
        	addAttribute(doc,"name",boundInterface.getName().getLocalPart(),rootElement);
        }
        addAttribute(doc,"callbackname",boundInterface.getName().getLocalPart() + CALL_BACK_HANDLER_SUFFIX,rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);
        return doc;

    }

    private void fillSyncAttributes(XmlDocument doc, Element rootElement) {
        addAttribute(doc,"isAsync",this.configuration.isAsyncOn()?"1":"0",rootElement);
        addAttribute(doc,"isSync",this.configuration.isSyncOn()?"1":"0",rootElement);
    }

    private void loadOperations(WSDLInterface boundInterface, XmlDocument doc, Element rootElement){
        loadOperations(boundInterface, doc, rootElement, null, null);
    }

    private void loadOperations(WSDLInterface boundInterface, XmlDocument doc, Element rootElement, String operationPrefix, String operationPostfix) {
        Collection col = boundInterface.getOperations().values();
        Element methodElement = null;
        WSDLOperation operation = null;

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            operation = (WSDLOperation) iterator.next();
            methodElement = doc.createElement("method");
            addAttribute(doc,"name",removeUnsuitableCharacters(operation.getName().getLocalPart()),methodElement);
            addAttribute(doc,"namespace",operation.getName().getNamespaceURI(),methodElement);
            methodElement.appendChild(getInputElement(doc,operation));
            methodElement.appendChild(getOutputElement(doc,operation));
            rootElement.appendChild(methodElement);
        }
    }

    protected XmlDocument createDOMDocuementForTestCase(WSDLBinding binding) {
        WSDLInterface boundInterface = binding.getBoundInterface();

        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("class");
        String serviceXMLPath = configuration.getPackageName().replace('.','/')+TEST_PACKAGE_NAME_SUFFIX.replace('.','/')+"/testservice.xml";
        addAttribute(doc,"package",configuration.getPackageName()+TEST_PACKAGE_NAME_SUFFIX,rootElement);
        addAttribute(doc, "servicexmlpath", serviceXMLPath, rootElement);
        addAttribute(doc, "implpackage", configuration.getPackageName(), rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc,"name",localPart+TEST_SUFFIX,rootElement);
        addAttribute(doc,"namespace",boundInterface.getName().getNamespaceURI(),rootElement);
        addAttribute(doc,"interfaceName",localPart,rootElement);
        addAttribute(doc,"callbackname",localPart + CALL_BACK_HANDLER_SUFFIX,rootElement);
        addAttribute(doc,"stubname",localPart + STUB_SUFFIX,rootElement);
        addAttribute(doc, "address", "http://localhost:"+Constants.TEST_PORT+"/services/"+boundInterface.getName().getLocalPart()+TEST_SERVICE_CLASS_NAME_SUFFIX, rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);        
        return doc;

    }


    /**
     * Creates the DOM tree for implementations
     * @param binding
     * @param service
     * @return
     */
    protected XmlDocument createDOMDocuementForInterfaceImplementation(WSDLBinding binding, WSDLService service) {
        WSDLInterface boundInterface = binding.getBoundInterface();
        WSDLEndpoint endpoint = null;
        HashMap endpoints = service.getEndpoints();
        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("class");
        addAttribute(doc,"package",configuration.getPackageName(),rootElement);
        String localPart = boundInterface.getName().getLocalPart();
        addAttribute(doc,"name",localPart+STUB_SUFFIX,rootElement);
        addAttribute(doc,"servicename",localPart,rootElement);
        addAttribute(doc,"namespace",boundInterface.getName().getNamespaceURI(),rootElement);
        addAttribute(doc,"interfaceName",localPart,rootElement);
        addAttribute(doc,"callbackname",localPart + CALL_BACK_HANDLER_SUFFIX,rootElement);
//        addAttribute(doc,"testcase",localPart + TEST_SUFFIX,rootElement);
        addEndpoints(doc,rootElement,endpoints);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);
        doc.appendChild(rootElement);

        return doc;

    }

    protected void addEndpoints(XmlDocument doc,Element rootElement,HashMap endpointMap){
        Object[] endpoints = endpointMap.values().toArray();
        WSDLEndpoint endpoint;
        Element endpointElement;
        Text text;
        for (int i = 0; i < endpoints.length; i++) {
            endpoint = (WSDLEndpoint)endpoints[i];
            endpointElement = doc.createElement("endpoint");
            org.apache.wsdl.extensions.SOAPAddress address = null;
            Iterator iterator = endpoint.getExtensibilityElements().iterator();
            while(iterator.hasNext())  {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement)iterator.next();
                if (element.getType().equals(ExtensionConstants.SOAP_ADDRESS)){
                   address = (org.apache.wsdl.extensions.SOAPAddress)element;
                }
            }
            text = doc.createTextNode(address.getLocationURI());     //todo How to get the end point address
            endpointElement.appendChild(text);
            rootElement.appendChild(endpointElement);
        }

    }

    protected void addAttribute(XmlDocument document,String AttribName, String attribValue, Element element){
        Attr attribute = document.createAttribute(AttribName);
        attribute.setValue(attribValue);
        element.setAttributeNode(attribute);
    }

    protected String removeUnsuitableCharacters(String word){
        return word.replaceAll("\\W","_");
    }

}

