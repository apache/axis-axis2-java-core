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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
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
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
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
import org.apache.wsdl.WSDLConstants;
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
import javax.xml.transform.URIResolver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//~--- classes ----------------------------------------------------------------

public abstract class MultiLanguageClientEmitter implements Emitter {

    /*
     *  Important! These constants are used in some places in the templates. Care should
     *  be taken when changing them
     */
    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
    private static final String SERVICE_CLASS_SUFFIX = "Skeleton";
    private static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";
    private static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";
    private static final String DATABINDING_PACKAGE_NAME_SUFFIX = ".databinding";

    private static Map MEPtoClassMap;
    private static Map MEPtoSuffixMap;

    /**
     * Field constructorMap
     */
    private static HashMap constructorMap = new HashMap(50);

    //~--- static initializers ------------------------------------------------

    static {

        // Type maps to a valid initialization value for that type
        // Type var = new Type(arg)
        // Where "Type" is the key and "new Type(arg)" is the string stored
        // Used in emitting test cases and server skeletons.
        constructorMap.put("int", "0");
        constructorMap.put("float", "0");
        constructorMap.put("boolean", "true");
        constructorMap.put("double", "0");
        constructorMap.put("byte", "(byte)0");
        constructorMap.put("short", "(short)0");
        constructorMap.put("long", "0");
        constructorMap.put("java.lang.Boolean", "new java.lang.Boolean(false)");
        constructorMap.put("java.lang.Byte", "new java.lang.Byte((byte)0)");
        constructorMap.put("java.lang.Double", "new java.lang.Double(0)");
        constructorMap.put("java.lang.Float", "new java.lang.Float(0)");
        constructorMap.put("java.lang.Integer", "new java.lang.Integer(0)");
        constructorMap.put("java.lang.Long", "new java.lang.Long(0)");
        constructorMap.put("java.lang.Short", "new java.lang.Short((short)0)");
        constructorMap.put("java.math.BigDecimal", "new java.math.BigDecimal(0)");
        constructorMap.put("java.math.BigInteger", "new java.math.BigInteger(\"0\")");
        constructorMap.put("java.lang.Object", "new java.lang.String()");
        constructorMap.put("byte[]", "new byte[0]");
        constructorMap.put("java.util.Calendar", "java.util.Calendar.getInstance()");
        constructorMap.put("javax.xml.namespace.QName",
                "new javax.xml.namespace.QName(\"http://double-double\", \"toil-and-trouble\")");


        //populate the MEP -> class map
        MEPtoClassMap = new HashMap();
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_ONLY,"org.apache.axis2.receivers.AbstractInMessageReceiver");
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_OUT,"org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");

        //populate the MEP -> suffix map
        MEPtoSuffixMap = new HashMap();
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_ONLY,MESSAGE_RECEIVER_SUFFIX + "InOnly");
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_OUT,MESSAGE_RECEIVER_SUFFIX + "InOut");
        //register the other types as necessary
    }

    //~--- fields -------------------------------------------------------------

    private Log log = LogFactory.getLog(getClass());

    /**
     * This information holder keeps the necessary information of
     * what to codegen. The service, port, binding (and the porttype)
     * if the service and binding tags are missing then only the
     * portype wil be there
     * This will get populated before executing any code generation
     */
    protected InformationHolder infoHolder = null;
    protected CodeGenConfiguration configuration;
    protected TypeMapper mapper;
    protected URIResolver resolver;

    //~--- methods ------------------------------------------------------------

    /**
     * Commented for now for the
     * @param policy
     * @return the file name and the package of this policy
     */

//  private String processPolicy(Policy policy,String name) throws Exception{
//      PolicyFileWriter writer = new PolicyFileWriter(configuration.getOutputLocation());
//      String fileName = name + "_policy";
//      writer.createOutFile(configuration.getPackageName(),fileName);
//      FileOutputStream stream = writer.getStream();
//      if (stream!=null) WSPolicyParser.getInstance().printModel(policy,stream);
//
//      String packageName = configuration.getPackageName();
//      String fullFileName = "\\" + packageName.replace('.','\\') +"\\"+fileName+ ".xml";
//      return fullFileName;
//
//  }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param AttribName
     * @param attribValue
     * @param element
     */
    protected void addAttribute(Document document, String AttribName, String attribValue, Element element) {
        XSLTUtils.addAttribute(document, AttribName, attribValue, element);
    }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param eltName
     * @param eltValue
     * @param element
     */
    protected Element addElement(Document document, String eltName, String eltValue, Element element) {
        Element elt = XSLTUtils.addChildElement(document,eltName,element);
        elt.appendChild(document.createTextNode(eltValue));
        return elt;
    }


    /**
     * Adds the endpoint to the document.
     *
     * @param doc
     * @param rootElement
     */
    protected void addEndpoint(Document doc, Element rootElement) throws Exception {
        WSDLEndpoint endpoint = infoHolder.getPort();

        // attach the policy for this endpoint here
        //            String policyFileResourceName = null;
        //            Policy policy =  (Policy)endpointPolicyMap.get(endpoint.getName());
        //            if (policy!=null){
        //                //process the policy for this end point
        //                 policyFileResourceName = processPolicy(policy,endpoint.getName().getLocalPart());
        //
        //            }
        Element endpointElement = doc.createElement("endpoint");
        org.apache.wsdl.extensions.SOAPAddress address = null;
        Iterator iterator = endpoint.getExtensibilityElements().iterator();

        while (iterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) iterator.next();

            if (ExtensionConstants.SOAP_11_ADDRESS.equals(element.getType())
                    || ExtensionConstants.SOAP_12_ADDRESS.equals(element.getType())) {
                address = (org.apache.wsdl.extensions.SOAPAddress) element;
            }
        }

        Text text = doc.createTextNode((address != null)
                ? address.getLocationURI()
                : "");

//      if (policyFileResourceName!=null){
//          addAttribute(doc,"policyRef",policyFileResourceName,endpointElement);
//      }
        endpointElement.appendChild(text);
        rootElement.appendChild(endpointElement);
    }

    private void addHeaderOperations(List soapHeaderParameterQNameList, WSDLBindingOperation bindingOperation,
                                     boolean input) {
        Iterator extIterator;

        if (input) {
            extIterator = (bindingOperation.getInput() == null)
                    ? null
                    : bindingOperation.getInput().getExtensibilityElements().iterator();
        } else {
            extIterator = (bindingOperation.getOutput() == null)
                    ? null
                    : bindingOperation.getOutput().getExtensibilityElements().iterator();
        }

        while ((extIterator != null) && extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();

            if (ExtensionConstants.SOAP_11_HEADER.equals(element.getType())) {
                SOAPHeader header = (SOAPHeader) element;

                soapHeaderParameterQNameList.add(header.getElement());
            }
        }
    }

    private void addSOAPAction(Document doc, Element rootElement, WSDLBindingOperation binding) {
        List extensibilityElements = binding.getExtensibilityElements();
        boolean actionAdded = false;

        if ((extensibilityElements != null) && !extensibilityElements.isEmpty()) {
            Iterator extIterator = extensibilityElements.iterator();

            while (extIterator.hasNext()) {
                WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();

                if (ExtensionConstants.SOAP_11_OPERATION.equals(element.getType())
                        || ExtensionConstants.SOAP_12_OPERATION.equals(element.getType())) {
                    addAttribute(doc, "soapaction", ((SOAPOperation) element).getSoapAction(), rootElement);
                    actionAdded = true;
                }
            }
        }

        if (!actionAdded) {
            addAttribute(doc, "soapaction", "", rootElement);
        }
    }

    /**
     * Looks for the SOAPVersion and adds it.
     *
     * @param binding
     * @param doc
     * @param rootElement
     */
    protected void addSoapVersion(WSDLBinding binding, Document doc, Element rootElement) {

        // loop through the extensibility elements to get to the bindings element
        List extensibilityElementsList = binding.getExtensibilityElements();
        int count = extensibilityElementsList.size();

        for (int i = 0; i < count; i++) {
            WSDLExtensibilityElement extElement = (WSDLExtensibilityElement) extensibilityElementsList.get(i);

            if (ExtensionConstants.SOAP_11_BINDING.equals(extElement.getType())) {
                addAttribute(doc, "soap-version", "1.1", rootElement);

                break;
            } else if (ExtensionConstants.SOAP_12_BINDING.equals(extElement.getType())) {
                addAttribute(doc, "soap-version", "1.2", rootElement);

                break;
            }
        }
    }

    /**
     * Creates the DOM tree for the Ant build. Uses the interface.
     */
    protected Document createDOMDocumentForAntBuild() {
        WSDLInterface wsdlInterface = infoHolder.getPorttype();
        WSDLService service = infoHolder.getService();
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("ant");
        String localPart = getCoreClassName(wsdlInterface);
        String packageName = configuration.getPackageName();
        String[]      dotSeparatedValues = packageName.split("\\.");

        addAttribute(doc, "package", dotSeparatedValues[0], rootElement);
        addAttribute(doc, "name", localPart, rootElement);

        String servicename = localPart;

        if (service != null) {
            servicename = service.getName().getLocalPart();
        }

        addAttribute(doc, "servicename", servicename, rootElement);
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Generates the model for the callbacks.
     */
    protected Document createDOMDocumentForCallbackHandler() {
        Document doc = getEmptyDocument();
        WSDLInterface boundInterface = infoHolder.getPorttype();
        WSDLBinding axisBinding = infoHolder.getBinding();
        Element rootElement = doc.createElement("callback");

        addAttribute(doc, "package", configuration.getPackageName(), rootElement);
        addAttribute(doc, "name", getCoreClassName(boundInterface) + CALL_BACK_HANDLER_SUFFIX, rootElement);
        addAttribute(doc, "namespace", boundInterface.getName().getNamespaceURI(), rootElement);

        // TODO JAXRPC mapping support should be considered
        this.loadOperations(boundInterface, doc, rootElement, axisBinding);

        // this.loadOperations(boundInterface, doc, rootElement, "on", "Complete");
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Creates the DOM tree for the interface creation. Uses the interface.
     */
    protected Document createDOMDocumentForInterface(boolean writeDatabinders) {
        Document doc = getEmptyDocument();
        WSDLInterface wsdlInterface = infoHolder.getPorttype();
        WSDLBinding axisBinding = infoHolder.getBinding();
        Element rootElement = doc.createElement("interface");
        String localPart = getCoreClassName(wsdlInterface);

        addAttribute(doc, "package", configuration.getPackageName(), rootElement);
        addAttribute(doc, "name", localPart, rootElement);
        addAttribute(doc, "callbackname", wsdlInterface.getName().getLocalPart() + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(wsdlInterface, doc, rootElement, axisBinding);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (writeDatabinders) {
            if (mapper.isObjectMappingPresent()) {

                // add an attribute to the root element showing that the writing has been skipped
                addAttribute(doc, "skip-write", "yes", rootElement);

                // process the mapper objects
                processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
            }
        }

        // #############################################################################################
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Creates the DOM tree for implementations.
     */
    protected Document createDOMDocumentForInterfaceImplementation() throws Exception {
        WSDLInterface boundInterface = infoHolder.getPorttype();
        WSDLBinding binding = infoHolder.getBinding();
        String packageName = configuration.getPackageName();
        String localPart = getCoreClassName(boundInterface);
        String stubName = localPart + STUB_SUFFIX;
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "name", stubName, rootElement);
        addAttribute(doc, "servicename", localPart, rootElement);
        addAttribute(doc, "namespace", boundInterface.getName().getNamespaceURI(), rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // add the wrap classes flag
        if (configuration.isPackClasses()) {
            addAttribute(doc, "wrapped", "yes", rootElement);
        }

        // add SOAP version
        addSoapVersion(binding, doc, rootElement);

        // add the end point
        addEndpoint(doc, rootElement);

        // set the sync/async attributes
        fillSyncAttributes(doc, rootElement);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (mapper.isObjectMappingPresent()) {

            // add an attribute to the root element showing that the writing has been skipped
            addAttribute(doc, "skip-write", "yes", rootElement);

            // process the mapper objects
            processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
        }

        // #############################################################################################

        // load the operations
        loadOperations(boundInterface, doc, rootElement, binding);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc, binding));
        doc.appendChild(rootElement);

        return doc;
    }

    protected Document createDOMDocumentForServiceXML() {
        WSDLInterface boundInterface = infoHolder.getPorttype();
        WSDLBinding axisBinding = infoHolder.getBinding();
        WSDLService service = infoHolder.getService();
        Document doc = getEmptyDocument();
        String coreClassName = getCoreClassName(boundInterface);

        // WSDLEndpoint endpoint = infoHolder.getPort();
        if (service != null) {
            doc.appendChild(getServiceElement(service.getName().getLocalPart(), coreClassName, doc, boundInterface,
                    axisBinding));
        } else {

            // service is missing. However we can derive a service name from the porttype
            doc.appendChild(getServiceElement(boundInterface.getName().getLocalPart(), coreClassName, doc,
                    boundInterface, axisBinding));
        }

        return doc;
    }

    /**
     * Creates the model for the skeleton.
     *
     * @return Returns documentModel for the skeleton.
     */
    protected Document createDOMDocumentForSkeleton() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        WSDLInterface boundInterface = infoHolder.getPorttype();
        WSDLBinding axisBinding = infoHolder.getBinding();

        // name the skeleton after the binding's name !
        String localpart = getCoreClassName(boundInterface);

        addAttribute(doc, "package", configuration.getPackageName(), rootElement);
        addAttribute(doc, "name", localpart + SERVICE_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", boundInterface.getName().getLocalPart() + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement, axisBinding);
        doc.appendChild(rootElement);

        return doc;
    }

    protected Document createDOMDocumentForTestCase() {
        WSDLInterface boundInterface = infoHolder.getPorttype();
        WSDLBinding binding = infoHolder.getBinding();
        String localPart = getCoreClassName(boundInterface);
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", configuration.getPackageName(), rootElement);
        addAttribute(doc, "name", localPart + TEST_SUFFIX, rootElement);
        addAttribute(doc, "namespace", boundInterface.getName().getNamespaceURI(), rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX, rootElement);
        addAttribute(doc, "stubname", localPart + STUB_SUFFIX, rootElement);
        addAttribute(doc, "dbsupportpackage", configuration.getPackageName() + DATABINDING_PACKAGE_NAME_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(boundInterface, doc, rootElement);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc, binding));
        doc.appendChild(rootElement);

        return doc;
    }

    protected Element createDOMElementforDatabinders(Document doc, WSDLBinding binding) {

        // First Iterate through the operations and find the relevant fromOM and toOM methods to be generated
        Map bindingOperationsMap = binding.getBindingOperations();
        Map parameterMap = new HashMap();
        Iterator operationsIterator = bindingOperationsMap.values().iterator();

        while (operationsIterator.hasNext()) {
            WSDLBindingOperation bindingOperation = (WSDLBindingOperation) operationsIterator.next();

            // Add the parameters to a map with their type as the key
            // this step is needed to remove repetitions

            // process the input and output parameters
            Element inputParamElement = getInputParamElement(doc, bindingOperation.getOperation());

            if (inputParamElement != null) {
                parameterMap.put(inputParamElement.getAttribute("type"), inputParamElement);
            }

            Element outputParamElement = getOutputParamElement(doc, bindingOperation.getOperation());

            if (outputParamElement != null) {
                parameterMap.put(outputParamElement.getAttribute("type"), outputParamElement);
            }

            // todo process the exceptions
            // process the header parameters
            Element newChild;
            List headerParameterQNameList = new ArrayList();

            addHeaderOperations(headerParameterQNameList, bindingOperation, true);

            List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"), newChild);
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList, bindingOperation, false);
            parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"), newChild);
            }
        }

        Element rootElement = doc.createElement("databinders");

        addAttribute(doc, "dbtype", configuration.getDatabindingType(), rootElement);

        // add the names of the elements that have base 64 content
        // if the base64 name list is missing then this whole step is skipped
        rootElement.appendChild(getBase64Elements(doc));

        // Now run through the parameters and add them to the root element
        Collection parameters = parameterMap.values();

        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            rootElement.appendChild((Element) iterator.next());
        }

        return rootElement;
    }

    protected Document createDocumentForMessageReceiver(String mep) {

        WSDLBinding binding = infoHolder.getBinding();
        WSDLInterface boundInterface = infoHolder.getPorttype();
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", configuration.getPackageName(), rootElement);

        String localPart = getCoreClassName(boundInterface);

        addAttribute(doc, "name", localPart + MEPtoSuffixMap.get(mep), rootElement);
        addAttribute(doc, "skeletonname", localPart + SERVICE_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "basereceiver", (String)MEPtoClassMap.get(mep), rootElement);
        fillSyncAttributes(doc, rootElement);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (mapper.isObjectMappingPresent()) {

            // add an attribute to the root element showing that the writing has been skipped
            addAttribute(doc, "skip-write", "yes", rootElement);

            // process the mapper objects
            processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
        }

        // #############################################################################################

        boolean isOpsFound = loadOperations(boundInterface, doc, rootElement, null,mep);

        //put the result in the property map
        infoHolder.putProperty(mep,isOpsFound?Boolean.TRUE:Boolean.FALSE);
        // ///////////////////////
        rootElement.appendChild(createDOMElementforDatabinders(doc, binding));

        // ///////////////////////
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitSkeleton()
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {

            // get the interface
            int codegenStyle = this.configuration.getCodeGenerationStyle();

            if (codegenStyle == XSLTConstants.CodegenStyle.INTERFACE) {
                emitSkeletonInterface();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.BINDING) {
                emitSkeletonBinding();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.AUTOMATIC) {
                emitSkeletonAutomatic();
            } else {
                throw new Exception(CodegenMessages.getMessage("emitter.unknownStyle", codegenStyle + ""));
            }

            // Call the emit stub method to generate the client side too
            if (configuration.isGenerateAll()) {
                emitStub();
            }
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Skeleton emission - Automatic mode
     */
    private void emitSkeletonAutomatic() throws Exception {
        if (infoHolder.getBinding() == null) {

            // No binding is present.use the interface mode
            emitSkeletonInterface();
        } else {

            // use the binding mode
            emitSkeletonBinding();
        }
    }

    /**
     * Emits the skeleton with binding.
     */
    private void emitSkeletonBinding() throws Exception {
        WSDLBinding axisBinding;

        axisBinding = infoHolder.getBinding();

        WSDLInterface wsInterface = infoHolder.getPorttype();

        if (axisBinding == null) {

            // asking for a code generation with a binding when a binding is
            // not present should be the cause of an Exception !
            throw new Exception(CodegenMessages.getMessage("emitter.cannotFindBinding"));
        }

        // see the comment at updateMapperClassnames for details and reasons for
        // calling this method
        if (mapper.isObjectMappingPresent()) {
            updateMapperForMessageReceiver(wsInterface);
        }

        // Note  -  thsi order is very important
        // write skeleton
        writeSkeleton();

         // write a MessageReceiver for this particular service.
        writeMessageReceiver();

        // write service xml
        writeServiceXml();



        // write the ant build if not asked for all
        if (!configuration.isGenerateAll()) {
            writeAntBuild();
        }
    }

    /**
     * Emits the skeleton with interface only.
     */
    private void emitSkeletonInterface() throws Exception {

        // write skeleton
        writeSkeleton();

        // write interface implementations
        writeServiceXml();
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface1"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface2"));
    }

    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {
        try {

            // get the interface
            int codegenStyle = this.configuration.getCodeGenerationStyle();

            if (codegenStyle == XSLTConstants.CodegenStyle.INTERFACE) {
                emitStubInterface();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.BINDING) {
                emitStubBinding();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.AUTOMATIC) {
                emitStubAutomatic();
            } else {
                throw new Exception(CodegenMessages.getMessage("emitter.unknownStyle", codegenStyle + ""));
            }
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * emit the stubcode with the automatic mode. Look for the binding and if present
     * emit the skeleton with the binding. Else go for the interface
     */
    private void emitStubAutomatic() throws Exception {
        if (infoHolder.getBinding() == null) {

            // No binding is not present.use the interface mode
            emitStubInterface();
        } else {

            // use the binding mode
            emitStubBinding();
        }
    }

    /**
     * Emits the stubcode with bindings.
     *
     * @throws Exception
     */
    private void emitStubBinding() throws Exception {
        WSDLInterface axisInterface = infoHolder.getPorttype();

        // see the comment at updateMapperClassnames for details and reasons for
        // calling this method
        if (mapper.isObjectMappingPresent()) {
            updateMapperForStub(axisInterface);
        }

        // write the inteface
        // feed the binding information also
        // note that we do not create this interface if the user switched on the wrap classes mode
        if (!configuration.isPackClasses()) {
            writeInterface(false);
        }

        // write the call back handlers
        writeCallBackHandlers();

        // write interface implementations
        writeInterfaceImplementation();

        // write the test classes
        writeTestClasses();

        // write a dummy implementation call for the tests to run.
        // writeTestSkeletonImpl(axisBinding);
        // write a testservice.xml that will load the dummy skeleton impl for testing
        // writeTestServiceXML(axisBinding);
        // write an ant build file
        writeAntBuild();
    }

    /**
     * Emits the stub code with interfaces only.
     *
     * @throws Exception
     */
    private void emitStubInterface() throws Exception {
        WSDLInterface axisInterface = infoHolder.getPorttype();

        if (mapper.isObjectMappingPresent()) {
            updateMapperForInterface(axisInterface);
        }

        // Write the interfaces
        // note that this case we do not care about the wrapping flag
        writeInterface(true);

        // write the call back handlers
        writeCallBackHandlers();

        // log the message stating that the binding dependent parts are not generated
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface1"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface3"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface4"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface5"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface6"));
    }

    private void fillSyncAttributes(Document doc, Element rootElement) {
        addAttribute(doc, "isAsync", this.configuration.isAsyncOn()
                ? "1"
                : "0", rootElement);
        addAttribute(doc, "isSync", this.configuration.isSyncOn()
                ? "1"
                : "0", rootElement);
    }

    /**
     * Loads operations based on the interface.
     *
     * @param boundInterface
     * @param doc
     * @param rootElement
     */
    private void loadOperations(WSDLInterface boundInterface, Document doc, Element rootElement) {
        loadOperations(boundInterface, doc, rootElement, null);
    }

    /**
     * @param boundInterface
     * @param doc
     * @param rootElement
     * @param binding
     */
    private boolean loadOperations(WSDLInterface boundInterface, Document doc, Element rootElement, WSDLBinding binding) {
        return loadOperations( boundInterface, doc,  rootElement,  null,null);
    }
    /**
     * @param boundInterface
     * @param doc
     * @param rootElement
     * @param binding
     *
     * @return operations found
     */
    private boolean loadOperations(WSDLInterface boundInterface,
                                   Document doc,
                                   Element rootElement,
                                   WSDLBinding binding,
                                   String mep) {
        Collection col = boundInterface.getOperations().values();
        String portTypeName = getCoreClassName(boundInterface);
        Element methodElement;
        WSDLOperation operation = null;
        boolean opsFound = false;
        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            operation = (WSDLOperation) iterator.next();
            if (mep==null){
                //at this point we know it's true
                opsFound = true;

                List soapHeaderInputParameterList = new ArrayList();
                List soapHeaderOutputParameterList = new ArrayList();
                methodElement = doc.createElement("method");
                String localPart = operation.getName().getLocalPart();

                addAttribute(doc, "name", localPart, methodElement);
                addAttribute(doc, "namespace", operation.getName().getNamespaceURI(), methodElement);
                addAttribute(doc, "style", operation.getStyle(), methodElement);
                addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                        methodElement);

                addAttribute(doc, "mep", operation.getMessageExchangePattern(), methodElement);

                if (null != binding) {
                    WSDLBindingOperation bindingOperation = binding.getBindingOperation(operation.getName());

                    // todo This can be a prob !!!!!
                    if (bindingOperation != null) {
                        addSOAPAction(doc, methodElement, bindingOperation);
                        addHeaderOperations(soapHeaderInputParameterList, bindingOperation, true);
                        addHeaderOperations(soapHeaderOutputParameterList, bindingOperation, false);
                    }
                }

                methodElement.appendChild(getInputElement(doc, operation, soapHeaderInputParameterList));
                methodElement.appendChild(getOutputElement(doc, operation, soapHeaderOutputParameterList));
                rootElement.appendChild(methodElement);
                //////////////////////
            }else{
                //mep is present - we move ahead only if the given mep matches the mep of this operation

                if (mep.equals(operation.getMessageExchangePattern())){
                    //at this point we know it's true
                    opsFound = true;
                    List soapHeaderInputParameterList = new ArrayList();
                    List soapHeaderOutputParameterList = new ArrayList();
                    methodElement = doc.createElement("method");
                    String localPart = operation.getName().getLocalPart();

                    addAttribute(doc, "name", localPart, methodElement);
                    addAttribute(doc, "namespace", operation.getName().getNamespaceURI(), methodElement);
                    addAttribute(doc, "style", operation.getStyle(), methodElement);
                    addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                            methodElement);

                    addAttribute(doc, "mep", operation.getMessageExchangePattern(), methodElement);

                    if (null != binding) {
                        WSDLBindingOperation bindingOperation = binding.getBindingOperation(operation.getName());

                        // todo This can be a prob !!!!!
                        if (bindingOperation != null) {
                            addSOAPAction(doc, methodElement, bindingOperation);
                            addHeaderOperations(soapHeaderInputParameterList, bindingOperation, true);
                            addHeaderOperations(soapHeaderOutputParameterList, bindingOperation, false);
                        }
                    }

                    methodElement.appendChild(getInputElement(doc, operation, soapHeaderInputParameterList));
                    methodElement.appendChild(getOutputElement(doc, operation, soapHeaderOutputParameterList));
                    rootElement.appendChild(methodElement);
                    //////////////////////
                }
            }
        }

        return opsFound;
    }

    /**
     * @param word
     * @return Returns character removed string.
     */
    protected String makeJavaClassName(String word) {
        if (JavaUtils.isJavaKeyword(word)) {
            return JavaUtils.makeNonJavaKeyword(word);
        } else {
            return JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(word));
        }
    }

    /**
     * Populate the info holder looking at the WOM
     */
    private void populateInformationHolder() {
        infoHolder = new InformationHolder();

        WSDLDescription wsdlDescription = configuration.getWom();
        int codegenStyle = this.configuration.getCodeGenerationStyle();

        // user explicity asked for the interface mode, so we can get
        // away with the populating the portype only
        if (codegenStyle == XSLTConstants.CodegenStyle.INTERFACE) {
            populatePorttype(infoHolder, wsdlDescription);

            return;
        }

        // start with the service
        Map services = wsdlDescription.getServices();

        if ((services != null) && !services.isEmpty()) {
            WSDLService selectedService;
            WSDLEndpoint selectedEndpoint;

            if (services.size() > 1) {

                // look for the users setting here
                if (configuration.getServiceName() != null) {
                    selectedService = wsdlDescription.getService(new QName(configuration.getWom().getTargetNameSpace(),
                            configuration.getServiceName()));

                    if (selectedService == null) {
                        throw new RuntimeException(CodegenMessages.getMessage("emitter.serviceNotFoundError",
                                configuration.getServiceName()));
                    }
                } else {

                    // print warning
                    System.out.println(CodegenMessages.getMessage("emitter.warningMultipleServices"));
                    // note - we are sure of no NPE's here
                    selectedService = (WSDLService) services.values().iterator().next();
                }
            } else {
                // note - we are sure of no NPE's here
                selectedService = (WSDLService) services.values().iterator().next();
            }

            infoHolder.setService(selectedService);

            // get the ports from the service
            Map endpoints = selectedService.getEndpoints();

            if ((endpoints != null) && !endpoints.isEmpty()) {
                if (endpoints.size() > 1) {

                    // look for the users setting here
                    if (configuration.getPortName() != null) {
                        selectedEndpoint =
                                selectedService.getEndpoint(new QName(configuration.getWom().getTargetNameSpace(),
                                        configuration.getPortName()));

                        if (selectedEndpoint == null) {
                            throw new RuntimeException(CodegenMessages.getMessage("emitter.endpointNotFoundError"));
                        }
                    } else {
                        System.out.println(CodegenMessages.getMessage("emitter.warningMultipleEndpoints"));
                        selectedEndpoint = (WSDLEndpoint) endpoints.values().iterator().next();
                    }
                } else {
                    selectedEndpoint = (WSDLEndpoint) endpoints.values().iterator().next();
                }

                infoHolder.setPort(selectedEndpoint);

                WSDLBinding binding = selectedEndpoint.getBinding();

                infoHolder.setBinding(binding);
                infoHolder.setPorttype(binding.getBoundInterface());
            } else {

                // having no endpoints?? this is surely an exception
                throw new RuntimeException(CodegenMessages.getMessage("emitter.noEndpointsFoundError"));
            }
        } else {

            // log warning saying that the service was not found and switching to the interface mode
            System.out.println(CodegenMessages.getMessage("emitter.switchingMessages"));
            populatePorttype(infoHolder, wsdlDescription);
        }
    }

    // populates the porttypes
    private void populatePorttype(InformationHolder info, WSDLDescription description) {
        Map wsdlInterfaces = description.getWsdlInterfaces();

        if ((wsdlInterfaces != null) && !wsdlInterfaces.isEmpty()) {
            if (wsdlInterfaces.size() > 1) {

                // print warning - there are many interfaces. pick the first
                System.out.println(CodegenMessages.getMessage("emitter.warningMultiplePorttypes"));
            }

            Iterator porttypeIterator = wsdlInterfaces.values().iterator();

            info.setPorttype((WSDLInterface) porttypeIterator.next());
        }
    }

    /**
     * @param objectMappings
     * @param root
     * @param doc
     */
    private void processModelObjects(Map objectMappings, Element root, Document doc) {
        Iterator objectIterator = objectMappings.values().iterator();

        while (objectIterator.hasNext()) {
            Object o = objectIterator.next();

            if (o instanceof Document) {
                root.appendChild(doc.importNode(((Document) o).getDocumentElement(), true));
            } else {

                // oops we have no idea how to do this
            }
        }
    }

    /**
     * we need to modify the mapper's class name list. The issue here is that in this case we do not
     * expect the fully qulified class names to be present in the class names list due to the simple
     * reason that they've not been written yet! Hence the mappers class name list needs to be updated
     * to suit the expected package to be written
     * in this case we modify the package name to have make the class a inner class of the stub
     */
    private void updateMapperClassnames(String fullyQulifiedIncludingClassNamePrefix) {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();

        while (keys.hasNext()) {
            Object key = keys.next();

            classNameMap.put(key, fullyQulifiedIncludingClassNamePrefix + classNameMap.get(key));
        }
    }

    private void updateMapperForInterface(WSDLInterface boundInterface) {
        String packageName = configuration.getPackageName();
        String interfaceName = getCoreClassName(boundInterface);

        updateMapperClassnames(packageName + "." + interfaceName + ".");
    }

    private void updateMapperForMessageReceiver(WSDLInterface boundInterface) {
        String packageName = configuration.getPackageName();
        String localPart = getCoreClassName(boundInterface);
        String messageReceiver = localPart + MESSAGE_RECEIVER_SUFFIX;

        updateMapperClassnames(packageName + "." + messageReceiver + ".");
    }

    private void updateMapperForStub(WSDLInterface boundInterface) {
        String packageName = configuration.getPackageName();
        String localPart = getCoreClassName(boundInterface);
        String stubName = localPart + STUB_SUFFIX;

        updateMapperClassnames(packageName + "." + stubName + ".");
    }

    /**
     * Writes the Ant build.
     *
     * @throws Exception
     */
    protected void writeAntBuild() throws Exception {

        // Write the service xml in a folder with the
        Document skeletonModel = createDOMDocumentForAntBuild();
        AntBuildWriter antBuildWriter = new AntBuildWriter(this.configuration.getOutputLocation(),
                this.configuration.getOutputLanguage());

        antBuildWriter.setDatabindingFramework(this.configuration.getDatabindingType());
        writeClass(skeletonModel, antBuildWriter);
    }

    /**
     * Writes the callback handlers.
     */
    protected void writeCallBackHandlers() throws Exception {
        if (configuration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler();
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(getOutputDirectory(this.configuration.getOutputLocation(), "src"),
                            this.configuration.getOutputLanguage());

            writeClass(interfaceModel, callbackWriter);
        }
    }

    /**
     * A resusable method for the implementation of interface and implementation writing.
     *
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    protected void writeClass(Document model, ClassWriter writer) throws IOException, Exception {
        writer.loadTemplate();

        String packageName = model.getDocumentElement().getAttribute("package");
        String className = model.getDocumentElement().getAttribute("name");

        writer.createOutFile(packageName, className);

        // use the global resolver
        writer.parse(model, resolver);
    }

    /**
     * Writes the interfaces.
     *
     * @throws Exception
     */
    protected void writeInterface(boolean writeDatabinders) throws Exception {
        Document interfaceModel = createDOMDocumentForInterface(writeDatabinders);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(getOutputDirectory(this.configuration.getOutputLocation(), "src"),
                        this.configuration.getOutputLanguage());

        writeClass(interfaceModel, interfaceWriter);
    }

    /**
     * Writes the implementations.
     *
     * @throws Exception
     */
    protected void writeInterfaceImplementation() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();
        InterfaceImplementationWriter writer =
                new InterfaceImplementationWriter(getOutputDirectory(this.configuration.getOutputLocation(), "src"),
                        this.configuration.getOutputLanguage());

        writeClass(interfaceImplModel, writer);
    }

    protected void writeMessageReceiver() throws Exception {
        if (configuration.isWriteMessageReceiver()) {
            //loop through the meps and generate code for each mep
            Iterator it = MEPtoClassMap.keySet().iterator();
            while (it.hasNext()) {
                String mep = (String) it.next();
                Document classModel = createDocumentForMessageReceiver(mep);
                //write the class only if any methods are found
                if (infoHolder.getProperty(mep).equals(Boolean.TRUE)) {
                    MessageReceiverWriter writer =
                            new MessageReceiverWriter(getOutputDirectory(this.configuration.getOutputLocation(), "src"),
                                    this.configuration.getOutputLanguage());

                    writeClass(classModel, writer);
                }
            }
        }
    }

    /**
     * Writes the Service XML.
     *
     * @throws Exception
     */
    protected void writeServiceXml() throws Exception {
        if (this.configuration.isGenerateDeployementDescriptor()) {

            // Write the service xml in a folder with the
            Document serviceXMLModel = createDOMDocumentForServiceXML();
            ClassWriter serviceXmlWriter =
                    new ServiceXMLWriter(getOutputDirectory(this.configuration.getOutputLocation(), "resources"),
                            this.configuration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);
        }
    }

    /**
     * Writes the skeleton.
     *
     * @throws Exception
     */
    protected void writeSkeleton() throws Exception {

        // Note -  One can generate the skeleton using the interface XML
        Document skeletonModel = createDOMDocumentForSkeleton();
        ClassWriter skeletonWriter = new SkeletonWriter(getOutputDirectory(this.configuration.getOutputLocation(),
                "src"), this.configuration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    /**
     *  Write the test classes
     */
    protected void writeTestClasses() throws Exception {
        if (configuration.isWriteTestCase()) {
            Document classModel = createDOMDocumentForTestCase();
            TestClassWriter callbackWriter =
                    new TestClassWriter(getOutputDirectory(this.configuration.getOutputLocation(), "test"),
                            this.configuration.getOutputLanguage());

            writeClass(classModel, callbackWriter);
        }
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Gets the base64 types. If not available this will be empty!!!
     *
     * @param doc
     * @return Returns Element.
     */
    private Element getBase64Elements(Document doc) {
        Element root = doc.createElement("base64Elements");
        Element elt;
        QName qname;

        // this is a list of QNames
        List list = (List) configuration.getProperties().get(XSLTConstants.BASE_64_PROPERTY_KEY);

        if ((list != null) && !list.isEmpty()) {
            int count = list.size();

            for (int i = 0; i < count; i++) {
                qname = (QName) list.get(i);
                elt = doc.createElement("name");
                addAttribute(doc, "ns-url", qname.getNamespaceURI(), elt);
                addAttribute(doc, "localName", qname.getLocalPart(), elt);
                root.appendChild(elt);
            }
        }

        return root;
    }

    private String getCoreClassName(WSDLInterface wsInterface) {
        if (wsInterface != null) {
            return makeJavaClassName(wsInterface.getName().getLocalPart());
        } else {
            throw new RuntimeException(CodegenMessages.getMessage("emitter.coreclassNameError"));
        }
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
     * Finds the input element for the xml document.
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getInputElement(Document doc, WSDLOperation operation, List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        Element param = getInputParamElement(doc, operation);

        if (param != null) {
            inputElt.appendChild(param);
        }

        List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

        for (int i = 0; i < parameterElementList.size(); i++) {
            inputElt.appendChild((Element) parameterElementList.get(i));
        }

        return inputElt;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    private Element getInputParamElement(Document doc, WSDLOperation operation) {
        Element param = doc.createElement("param");
        MessageReference inputMessage = operation.getInputMessage();

        if (inputMessage != null) {
            addAttribute(doc, "name", this.mapper.getParameterName(inputMessage.getElementQName()), param);

            // todo modify the code here to unwrap if requested
            String typeMapping = this.mapper.getTypeMappingName(inputMessage.getElementQName());

            addAttribute(doc, "type", (typeMapping == null)
                    ? ""
                    : typeMapping, param);

            // add an extra attribute to say whether the type mapping is the default
            if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMapping)) {
                addAttribute(doc, "default", "yes", param);
            }

            addAttribute(doc, "value", getParamInitializer(typeMapping), param);

            // add this as a body parameter
            addAttribute(doc, "location", "body", param);

            Iterator iter = inputMessage.getExtensibilityAttributes().iterator();

            while (iter.hasNext()) {
                WSDLExtensibilityAttribute att = (WSDLExtensibilityAttribute) iter.next();

                addAttribute(doc, att.getKey().getLocalPart(), att.getValue().toString(), param);
            }
        } else {
            param = null;
        }

        return param;
    }

    /**
     * Gets the output directory for source files.
     *
     * @param outputDir
     * @return Returns File.
     */
    protected File getOutputDirectory(File outputDir, String dir2) {
        outputDir = new File(outputDir, dir2);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;
    }

    /**
     * Finds the output element.
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getOutputElement(Document doc, WSDLOperation operation, List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        Element param = getOutputParamElement(doc, operation);

        if (param != null) {
            outputElt.appendChild(param);
        }

        List outputElementList = getParameterElementList(doc, headerParameterQNameList, "header");

        for (int i = 0; i < outputElementList.size(); i++) {
            outputElt.appendChild((Element) outputElementList.get(i));
        }

        return outputElt;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    private Element getOutputParamElement(Document doc, WSDLOperation operation) {
        Element param = doc.createElement("param");
        MessageReference outputMessage = operation.getOutputMessage();
        String typeMappingStr;
        String parameterName;

        if (outputMessage != null) {
            parameterName = this.mapper.getParameterName(outputMessage.getElementQName());

            String typeMapping = this.mapper.getTypeMappingName(operation.getOutputMessage().getElementQName());

            typeMappingStr = (typeMapping == null)
                    ? ""
                    : typeMapping;
        } else {
            parameterName = "";
            typeMappingStr = "";
        }

        addAttribute(doc, "name", parameterName, param);
        addAttribute(doc, "type", typeMappingStr, param);

        // add an extra attribute to say whether the type mapping is the default
        if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMappingStr)) {
            addAttribute(doc, "default", "yes", param);
        }

        // add this as a body parameter
        addAttribute(doc, "location", "body", param);

        return param;
    }

    String getParamInitializer(String paramType) {

        // Look up paramType in the table
        String out = (String) constructorMap.get(paramType);

        if (out == null) {
            out = "null";
        }

        return out;
    }

    private List getParameterElementList(Document doc, List parameters, String location) {
        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                QName name = (QName) parameters.get(i);

                addAttribute(doc, "name", this.mapper.getParameterName(name), param);

                String typeMapping = this.mapper.getTypeMappingName(name);
                String typeMappingStr = (typeMapping == null)
                        ? ""
                        : typeMapping;

                addAttribute(doc, "type", typeMappingStr, param);
                addAttribute(doc, "location", location, param);
                parameterElementList.add(param);
            }
        }

        return parameterElementList;
    }

    /**
     * Gets the element.
     *
     * @param doc
     * @param boundInterface
     * @param axisBinding
     * @return Returns Element.
     */
    private Element getServiceElement(String serviceName, String porttypeName, Document doc,
                                      WSDLInterface boundInterface, WSDLBinding axisBinding
    ) {
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", configuration.getPackageName(), rootElement);
        addAttribute(doc, "name", porttypeName + SERVICE_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = MEPtoClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.getProperty(key))){
                Element elt = addElement(doc, "messagereceiver", porttypeName + MEPtoSuffixMap.get(key), rootElement);
                addAttribute(doc,"mep",key.toString(),elt);
            }

        }

//        fillSyncAttributes(doc, rootElement);
//        loadOperations(boundInterface, doc, rootElement, axisBinding);

        return rootElement;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Sets the code generator configuration.
     * This needs to be called before asking to codegen
     *
     * @param configuration
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.configuration = configuration;
        resolver = new XSLTIncludeResolver(this.configuration.getProperties());

        // select necessary information from the WOM
        populateInformationHolder();
    }

    /**
     * Sets the mapper.
     *
     * @param mapper
     * @see org.apache.axis2.wsdl.databinding.TypeMapper
     */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    //~--- inner classes ------------------------------------------------------

    /**
     * A simple class for keeping the information of the
     * relevant service/port/binding/porttype combination
     */
    private class InformationHolder {
        private WSDLBinding binding;
        private WSDLEndpoint port;
        private WSDLInterface porttype;
        private WSDLService service;

        private HashMap propertyMap = new HashMap();

        public void putProperty(Object key,Object val){
            propertyMap.put(key,val);
        }

        public Object getProperty(Object key){
            return propertyMap.get(key);
        }
        //~--- get methods ----------------------------------------------------

        public WSDLBinding getBinding() {
            return binding;
        }

        public WSDLEndpoint getPort() {
            return port;
        }

        public WSDLInterface getPorttype() {
            return porttype;
        }

        public WSDLService getService() {
            return service;
        }

        //~--- set methods ----------------------------------------------------

        public void setBinding(WSDLBinding binding) {
            this.binding = binding;
        }

        public void setPort(WSDLEndpoint port) {
            this.port = port;
        }

        public void setPorttype(WSDLInterface porttype) {
            this.porttype = porttype;
        }

        public void setService(WSDLService service) {
            this.service = service;
        }
    }


}
