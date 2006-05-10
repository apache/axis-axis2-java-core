package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.*;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.policy.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
 */

/**
 * MultiLanguageClientEmitter we have now is based on WOM. This one will directly infer the information
 * from the AxisService.
 */

public class AxisServiceBasedMultiLanguageEmitter implements Emitter {

    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
    private static final String SKELETON_CLASS_SUFFIX = "Skeleton";
    private static final String SKELETON_INTERFACE_SUFFIX = "SkeletonInterface";
    private static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";
    private static final String FAULT_SUFFIX = "Exception";
    private static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";
//    private static final String DATABINDING_PACKAGE_NAME_SUFFIX = ".databinding";

    private static Map MEPtoClassMap;
    private static Map MEPtoSuffixMap;

    private int uniqueFaultNameCounter = 0;
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
                "new javax.xml.namespace.QName(\"http://foo\", \"bar\")");

        //populate the MEP -> class map
        MEPtoClassMap = new HashMap();
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_ONLY, "org.apache.axis2.receivers.AbstractInMessageReceiver");
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_OUT, "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");

        //populate the MEP -> suffix map
        MEPtoSuffixMap = new HashMap();
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_ONLY, MESSAGE_RECEIVER_SUFFIX + "InOnly");
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_OUT, MESSAGE_RECEIVER_SUFFIX + "InOut");
        //register the other types as necessary
    }

    //~--- fields -------------------------------------------------------------
	private static final Log log = LogFactory.getLog(AxisServiceBasedMultiLanguageEmitter.class);
    protected URIResolver resolver;

    private Map infoHolder;

    CodeGenConfiguration codeGenConfiguration;

    protected TypeMapper mapper;

    private AxisService axisService;

    //a map to keep the fault classNames
    private Map fullyQualifiedFaultClassNameMap = new HashMap();
    private Map InstantiatableFaultClassNameMap = new HashMap();
    private Map faultClassNameMap = new HashMap();

    private Map instantiatableMessageClassNames = new HashMap();
    ;


    public AxisServiceBasedMultiLanguageEmitter() {
        infoHolder = new HashMap();
    }

    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.codeGenConfiguration = configuration;
        this.axisService = codeGenConfiguration.getAxisService();
        resolver = new XSLTIncludeResolver(this.codeGenConfiguration.getProperties());
    }

    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }


    /**
     * @see org.apache.axis2.wsdl.codegen.emitter.Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {

        try {
            emitStubFromService();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Update mapper for the stub
     */
    private void updateMapperForStub() {
        updateMapperClassnames(getFullyQualifiedStubName());
    }

    private String getFullyQualifiedStubName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + STUB_SUFFIX;
    }

    /**
     *
     */
    private void resetFaultNames() {
        fullyQualifiedFaultClassNameMap.clear();
        faultClassNameMap.clear();
    }

    /**
     * Populate a map of fault class names
     */
    private void generateAndPopulateFaultNames() {
        //loop through and find the faults
        Iterator operations = axisService.getOperations();
        AxisOperation operation;
        AxisMessage faultMessage;
        while (operations.hasNext()) {
            operation = (AxisOperation) operations.next();
            ArrayList faultMessages = operation.getFaultMessages();
            for (int i = 0; i < faultMessages.size(); i++) {
                faultMessage = (AxisMessage) faultMessages.get(i);
                //make a unique name and put that in the hashmap
                if (!fullyQualifiedFaultClassNameMap.
                        containsKey(faultMessage.getElementQName())) {
                    //make a name
                    String className = makeJavaClassName(faultMessage.getName()
                            + FAULT_SUFFIX);
                    while (fullyQualifiedFaultClassNameMap.containsValue(className)) {
                        className = makeJavaClassName(className + (uniqueFaultNameCounter++));
                    }

                    fullyQualifiedFaultClassNameMap.put(
                            faultMessage.getElementQName(),
                            className);
                    //this needs to be kept seperate and updated later
                    InstantiatableFaultClassNameMap.put(
                            faultMessage.getElementQName(),
                            className);
                    //we've to keep track of the fault base names seperately
                    faultClassNameMap.put(faultMessage.getElementQName(),
                            className);

                }
            }

        }
    }

    /**
     * Emits the stubcode with bindings.
     *
     * @throws Exception
     */
    private void emitStubFromService() throws Exception {

        // see the comment at updateMapperClassnames for details and reasons for
        // calling this method
        if (mapper.isObjectMappingPresent()) {
            updateMapperForStub();
        } else {
            copyToFaultMap();
        }

        //generate and populate the fault names before hand. We need that for
        //the smooth opration of the thing
        //first reset the fault names and recreate it
        resetFaultNames();
        generateAndPopulateFaultNames();
        updateFaultPackageForStub();

        // write the inteface
        // feed the binding information also
        // note that we do not create this interface if the user switched on the wrap classes mode
        if (!codeGenConfiguration.isPackClasses()) {
            writeInterface(false);
        }

        // write the call back handlers
        writeCallBackHandlers();

        // write interface implementations
        writeInterfaceImplementation();

        // write the test classes
        writeTestClasses();

        // write an ant build file
        //Note that ant build is generated only once
        //and that has to happen here only if the
        //client side code is required
        if (!codeGenConfiguration.isGenerateAll()) {
            writeAntBuild();
        }

    }

    /**
     * Writes the Ant build.
     *
     * @throws Exception
     */
    protected void writeAntBuild() throws Exception {

        // Write the service xml in a folder with the
        Document skeletonModel = createDOMDocumentForAntBuild();
        debugLogDocument("Document for ant build:", skeletonModel);
        AntBuildWriter antBuildWriter = new AntBuildWriter(codeGenConfiguration.getOutputLocation(),
                codeGenConfiguration.getOutputLanguage());

        antBuildWriter.setDatabindingFramework(codeGenConfiguration.getDatabindingType());
        writeClass(skeletonModel, antBuildWriter);
    }

    /**
     * Creates the DOM tree for the Ant build. Uses the interface.
     */
    protected Document createDOMDocumentForAntBuild() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("ant");
        String serviceName = makeJavaClassName(axisService.getName());
        String packageName = codeGenConfiguration.getPackageName();
        String[]      dotSeparatedValues = packageName.split("\\.");

        addAttribute(doc, "package", dotSeparatedValues[0], rootElement);
        addAttribute(doc, "name", serviceName, rootElement);
        addAttribute(doc, "servicename", serviceName, rootElement);
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Write the test classes
     */
    protected void writeTestClasses() throws Exception {
        if (codeGenConfiguration.isWriteTestCase()) {
            Document classModel = createDOMDocumentForTestCase();
            debugLogDocument("Document for test case:", classModel);
            TestClassWriter callbackWriter =
                    new TestClassWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "test"),
                            codeGenConfiguration.getOutputLanguage());

            writeClass(classModel, callbackWriter);
        }
    }

    protected Document createDOMDocumentForTestCase() {
        String coreClassName = makeJavaClassName(axisService.getName());
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", coreClassName + TEST_SUFFIX, rootElement);
        //todo is this right ???
        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        addAttribute(doc, "interfaceName", coreClassName, rootElement);
        addAttribute(doc, "callbackname", coreClassName + CALL_BACK_HANDLER_SUFFIX, rootElement);
        addAttribute(doc, "stubname", coreClassName + STUB_SUFFIX, rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc));
        doc.appendChild(rootElement);

        return doc;
    }

    /**
     * Writes the implementations.
     *
     * @throws Exception
     */
    protected void writeInterfaceImplementation() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();
        debugLogDocument("Document for interface implementation:", interfaceImplModel);
        InterfaceImplementationWriter writer =
                new InterfaceImplementationWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writer);
    }

    /**
     * Creates the DOM tree for implementations.
     */
    protected Document createDOMDocumentForInterfaceImplementation() throws Exception {

        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        String stubName = localPart + STUB_SUFFIX;
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "package", packageName, rootElement);
        addAttribute(doc, "name", stubName, rootElement);
        addAttribute(doc, "servicename", localPart, rootElement);
        //todo is this right ??
        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // add the wrap classes flag
        if (codeGenConfiguration.isPackClasses()) {
            addAttribute(doc, "wrapped", "yes", rootElement);
        }

        // add SOAP version
        addSoapVersion(doc, rootElement);

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
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc));

        Object stubMethods;

        //if some extension has added the stub methods property, add them to the
        //main document
        if ((stubMethods = codeGenConfiguration.getProperty("stubMethods")) != null) {
            rootElement.appendChild(doc.importNode((Element) stubMethods, true));
        }

        //add another element to have the unique list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        /////////////////////////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        /////////////////////////////////////////////////////


        doc.appendChild(rootElement);
        return doc;
    }

    private Element getUniqueListofFaults(Document doc) {
        Element rootElement = doc.createElement("fault-list");
        Element faultElement;
        QName key;
        Iterator iterator = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (iterator.hasNext()) {
            faultElement = doc.createElement("fault");
            key = (QName) iterator.next();

            //as for the name of a fault, we generate an exception
            addAttribute(doc, "name",
                    (String) fullyQualifiedFaultClassNameMap.get(key),
                    faultElement);
            addAttribute(doc, "intantiatiableName",
                    (String) InstantiatableFaultClassNameMap.get(key),
                    faultElement);
            addAttribute(doc, "shortName",
                    (String) faultClassNameMap.get(key),
                    faultElement);

            //the type represents the type that will be wrapped by this
            //name
            String typeMapping =
                    this.mapper.getTypeMappingName(key);
            addAttribute(doc, "type", (typeMapping == null)
                    ? ""
                    : typeMapping, faultElement);
            String attribValue = (String) instantiatableMessageClassNames.
                    get(key);

            addAttribute(doc, "instantiatableType",
                    attribValue == null ? "" : attribValue,
                    faultElement);

            // add an extra attribute to say whether the type mapping is
            // the default
            if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMapping)) {
                addAttribute(doc, "default", "yes", faultElement);
            }
            addAttribute(doc, "value", getParamInitializer(typeMapping),
                    faultElement);


            rootElement.appendChild(faultElement);
        }
        return rootElement;
    }

    /**
     * Adds the endpoint to the document.
     *
     * @param doc
     * @param rootElement
     */
    protected void addEndpoint(Document doc, Element rootElement) throws Exception {

        PolicyInclude policyInclude = axisService.getPolicyInclude();
        Policy servicePolicy = policyInclude.getPolicy();

        if (servicePolicy != null) {
            String policyString = PolicyUtil.getPolicyAsString(servicePolicy);
            addAttribute(doc, "policy", policyString, rootElement);
        }

        Element endpointElement = doc.createElement("endpoint");

        String endpoint = axisService.getEndpoint();
        Text text = doc.createTextNode((endpoint != null)
                ? endpoint
                : "");

        endpointElement.appendChild(text);
        rootElement.appendChild(endpointElement);
    }

    /**
     * Looks for the SOAPVersion and adds it.
     *
     * @param doc
     * @param rootElement
     */
    protected void addSoapVersion(Document doc, Element rootElement) {

        // loop through the extensibility elements to get to the bindings element

        String soapNsUri = axisService.getSoapNsUri();
        if (Constants.URI_WSDL11_SOAP.equals(soapNsUri)) {
            addAttribute(doc, "soap-version", "1.1", rootElement);
        } else if (Constants.URI_WSDL12_SOAP.equals(soapNsUri)) {
            addAttribute(doc, "soap-version", "1.2", rootElement);
        }

    }


    /**
     * Writes the callback handlers.
     */
    protected void writeCallBackHandlers() throws Exception {
        if (codeGenConfiguration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler();
            debugLogDocument("Document for callback handler:", interfaceModel);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                            codeGenConfiguration.getOutputLanguage());

            writeClass(interfaceModel, callbackWriter);
        }
    }

    /**
     * Generates the model for the callbacks.
     */
    protected Document createDOMDocumentForCallbackHandler() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("callback");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", makeJavaClassName(axisService.getName()) + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // TODO JAXRPC mapping support should be considered
        this.loadOperations(doc, rootElement, null);

        doc.appendChild(rootElement);
        return doc;
    }

    /**
     * Writes the interfaces.
     *
     * @throws Exception
     */
    protected void writeInterface(boolean writeDatabinders) throws Exception {
        Document interfaceModel = createDOMDocumentForInterface(writeDatabinders);
        debugLogDocument("Document for interface:", interfaceModel);
        InterfaceWriter interfaceWriter =
                new InterfaceWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                        this.codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceModel, interfaceWriter);
    }

    /**
     * Creates the DOM tree for the interface creation. Uses the interface.
     */
    protected Document createDOMDocumentForInterface(boolean writeDatabinders) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        String localPart = makeJavaClassName(axisService.getName());

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", localPart, rootElement);
        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

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
     * Emit the skeltons
     *
     * @throws CodeGenerationException
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {
            emitSkeletonService();
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Update mapper for message receiver
     */
    private void updateMapperForMessageReceiver() {
        updateMapperClassnames(getFullyQualifiedMessageReceiverName());
    }

    /**
     * @return fully qualified MR name
     */
    private String getFullyQualifiedMessageReceiverName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + MESSAGE_RECEIVER_SUFFIX;
    }

    /**
     * @return fully qualified MR name
     */
    private String getFullyQualifiedSkeletonName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + SKELETON_CLASS_SUFFIX;
    }

    /**
     * @throws Exception
     */
    private void emitSkeletonService() throws Exception {
        // see the comment at updateMapperClassnames for details and reasons for
        // calling this method
        if (mapper.isObjectMappingPresent()) {
            updateMapperForMessageReceiver();
        } else {
            copyToFaultMap();
        }

        //handle faults
        generateAndPopulateFaultNames();
        updateFaultPackageForSkeleton();

        if (codeGenConfiguration.isServerSideInterface()){
            //write skeletonInterface
            writeSkeletonInterface();
        }
        // write skeleton
        writeSkeleton();

        // write a MessageReceiver for this particular service.
        writeMessageReceiver();

        // write interface implementations
        writeServiceXml();

        //write the ant build
        writeAntBuild();

        //for the server side codegen
        //we need to serialize the WSDL's
        writeWSDLFiles();
    }

    /**
     * Write out the WSDL files (and the schemas)
     * writing the WSDL (and schemas) is somewhat special so we cannot follow
     * the usual pattern of using the class writer
     */
    private void writeWSDLFiles() {
        //first modify the schema names (and locations) so that
        //they have unique (flattened) names and the schema locations
        //are adjusted to suit it
        axisService.setCustomSchemaNamePrefix("");//prefix with nothing
        axisService.setCustomSchemaNameSuffix(".xsd");//suffix with .xsd - the file name extension
        //force the mappings to be reconstructed
        axisService.setSchemaLocationsAdjusted(false);
        axisService.populateSchemaMappings();

        //now get the schema list and write it out
        SchemaWriter schemaWriter = new SchemaWriter(
                codeGenConfiguration.getOutputLocation());
        Hashtable schemaMappings = axisService.getSchemaMappingTable();
        Iterator keys = schemaMappings.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            schemaWriter.writeSchema(
                    (XmlSchema) schemaMappings.get(key),
                    (String) key
            );
        }


        WSDLWriter wsdlWriter = new WSDLWriter(
                codeGenConfiguration.getOutputLocation());
        wsdlWriter.writeWSDL(axisService);


    }

    private void copyToFaultMap() {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            instantiatableMessageClassNames.put(key,
                    classNameMap.get(key));
        }
    }

    /**
     *
     */
    private void updateFaultPackageForStub() {
        Iterator faultClassNameKeys = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (faultClassNameKeys.hasNext()) {
            Object key = faultClassNameKeys.next();
            String className = (String) fullyQualifiedFaultClassNameMap.get(key);
            //append the skelton name
            String fullyQualifiedStubName = getFullyQualifiedStubName();
            fullyQualifiedFaultClassNameMap.put(key, fullyQualifiedStubName + "."
                    + className);
            InstantiatableFaultClassNameMap.put(key, fullyQualifiedStubName + "$"
                    + className);
        }
    }

    /**
     *
     */
    private void updateFaultPackageForSkeleton() {
        Iterator faultClassNameKeys = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (faultClassNameKeys.hasNext()) {
            Object key = faultClassNameKeys.next();
            String className = (String) fullyQualifiedFaultClassNameMap.get(key);
            //append the skelton name
            String fullyQualifiedSkeletonName = getFullyQualifiedSkeletonName();
            fullyQualifiedFaultClassNameMap.put(key, fullyQualifiedSkeletonName + "."
                    + className);
            InstantiatableFaultClassNameMap.put(key, fullyQualifiedSkeletonName + "$"
                    + className);
        }
    }

    /**
     * @throws Exception
     */
    protected void writeMessageReceiver() throws Exception {

        if (codeGenConfiguration.isWriteMessageReceiver()) {
            //loop through the meps and generate code for each mep
            Iterator it = MEPtoClassMap.keySet().iterator();
            while (it.hasNext()) {
                String mep = (String) it.next();
                Document classModel = createDocumentForMessageReceiver(
                        mep,
                        codeGenConfiguration.isServerSideInterface());
                debugLogDocument("Document for message receiver:", classModel);
                //write the class only if any methods are found
                if (Boolean.TRUE.equals(infoHolder.get(mep))) {
                    MessageReceiverWriter writer =
                            new MessageReceiverWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                                    codeGenConfiguration.getOutputLanguage());

                    writeClass(classModel, writer);
                }
            }
        }
    }

    protected Document createDocumentForMessageReceiver(String mep,boolean isServerSideInterface) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);

        String localPart = makeJavaClassName(axisService.getName());

        addAttribute(doc, "name", localPart + MEPtoSuffixMap.get(mep), rootElement);
        addAttribute(doc, "skeletonname", localPart + SKELETON_CLASS_SUFFIX, rootElement);
        if (isServerSideInterface){
            addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_INTERFACE_SUFFIX,
                    rootElement);
        }else{
             addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_CLASS_SUFFIX,
                    rootElement);
        }
        addAttribute(doc, "basereceiver", (String) MEPtoClassMap.get(mep), rootElement);
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

        boolean isOpsFound = loadOperations(doc, rootElement, mep);
        //put the result in the property map
        infoHolder.put(mep, isOpsFound ? Boolean.TRUE : Boolean.FALSE);
        rootElement.appendChild(createDOMElementforDatabinders(doc));

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);

        //////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////
        return doc;
    }

    /**
     * create a dom doc for databinders
     *
     * @param doc
     */
    protected Element createDOMElementforDatabinders(Document doc) {

        // First Iterate through the operations and find the relevant fromOM and toOM methods to be generated
        Map parameterMap = new HashMap();
        Iterator operationsIterator = axisService.getOperations();

        while (operationsIterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
            // Add the parameters to a map with their type as the key
            // this step is needed to remove repetitions

            // process the input parameters
            String MEP = axisOperation.getMessageExchangePattern();
            if (isInputPresentForMEP(MEP)) {
                Element inputParamElement = getInputParamElement(doc, axisOperation);
                if (inputParamElement != null) {
                    parameterMap.put(inputParamElement.getAttribute("type"), inputParamElement);
                }
            }
            // process output parameters
            if (isOutputPresentForMEP(MEP)) {
                Element outputParamElement = getOutputParamElement(doc, axisOperation);
                if (outputParamElement != null) {
                    parameterMap.put(outputParamElement.getAttribute("type"), outputParamElement);
                }
            }
            //process faults
            Element[] faultParamElements = getFaultParamElements(doc, axisOperation);
            for (int i = 0; i < faultParamElements.length; i++) {
                parameterMap.put(
                        faultParamElements[i].getAttribute("type"),
                        faultParamElements[i]);
            }

            // process the header parameters
            Element newChild;
            List headerParameterQNameList = new ArrayList();

            addHeaderOperations(headerParameterQNameList, axisOperation, true);

            List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"), newChild);
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList, axisOperation, false);
            parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameterMap.put(newChild.getAttribute("type"), newChild);
            }
        }

        Element rootElement = doc.createElement("databinders");

        addAttribute(doc, "dbtype", codeGenConfiguration.getDatabindingType(), rootElement);

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

    private boolean isInputPresentForMEP(String MEP) {
        return WSDLConstants.MEP_URI_IN_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_IN_OUT.equals(MEP);
    }

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
        List list = (List) codeGenConfiguration.getProperties().get(XSLTConstants.BASE_64_PROPERTY_KEY);

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

                // oops we have no idea how to do this, if the model provided is not a DOM document
                // we are done. we might as well skip  it here
            }
        }
    }

    /**
     * we need to modify the mapper's class name list. The issue here is that in this case we do not
     * expect the fully qulified class names to be present in the class names list due to the simple
     * reason that they've not been written yet! Hence the mappers class name list needs to be updated
     * to suit the expected package to be written
     * in this case we modify the package name to have make the class a inner class of the stub,
     * interface or the message receiver depending on the style
     */
    private void updateMapperClassnames(String fullyQulifiedIncludingClassNamePrefix) {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();

        while (keys.hasNext()) {
            Object key = keys.next();
            String className = (String) classNameMap.get(key);
            classNameMap.put(key, fullyQulifiedIncludingClassNamePrefix + "." + className);
            instantiatableMessageClassNames.put(key,
                    fullyQulifiedIncludingClassNamePrefix + "$" + className);
        }
    }

    /**
     * Write the service XML
     *
     * @throws Exception
     */
    private void writeServiceXml() throws Exception {
        if (this.codeGenConfiguration.isGenerateDeployementDescriptor()) {

            // Write the service xml in a folder with the
            Document serviceXMLModel = createDOMDocumentForServiceXML();
            debugLogDocument("Document for service XML:", serviceXMLModel);
            ClassWriter serviceXmlWriter =
                    new ServiceXMLWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(), "resources"),
                            this.codeGenConfiguration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);
        }
    }

    private Document createDOMDocumentForServiceXML() {
        Document doc = getEmptyDocument();
        String serviceName = axisService.getName();
        String className = makeJavaClassName(serviceName);

        doc.appendChild(getServiceElement(serviceName, className, doc));
        return doc;

    }

    private Node getServiceElement(String serviceName, String className, Document doc) {
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", className + SKELETON_CLASS_SUFFIX, rootElement);
        if (!codeGenConfiguration.isWriteTestCase()) {
            addAttribute(doc, "testOmit", "true", rootElement);
        }
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = MEPtoClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.get(key))) {
                Element elt = addElement(doc, "messagereceiver", className + MEPtoSuffixMap.get(key), rootElement);
                addAttribute(doc, "mep", key.toString(), elt);
            }

        }

        loadOperations(doc, rootElement, null);

        return rootElement;
    }

    private void writeSkeleton() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());
        debugLogDocument("Document for skeleton:", skeletonModel);
        ClassWriter skeletonWriter = new SkeletonWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                "src"), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    /**
     * Write the skeletonInterface
     *
     * @throws Exception
     */
    private void writeSkeletonInterface() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeletonInterface();
        debugLogDocument("Document for skeleton Interface:", skeletonModel);
        ClassWriter skeletonInterfaceWriter = new SkeletonInterfaceWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                "src"), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonInterfaceWriter);
    }


    private Document createDOMDocumentForSkeleton(boolean isSkeletonInterface) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", serviceName + SKELETON_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        if (isSkeletonInterface){
            addAttribute(doc, "skeletonInterfaceName", serviceName + SKELETON_INTERFACE_SUFFIX,
                    rootElement);
        }
        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    private Document createDOMDocumentForSkeletonInterface() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", serviceName + SKELETON_INTERFACE_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    private boolean loadOperations(Document doc, Element rootElement, String mep) {
        Element methodElement;
        String portTypeName = makeJavaClassName(axisService.getName());

        Iterator operations = axisService.getOperations();
        boolean opsFound = false;
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();

            // populate info holder with mep information. This will used in determining which
            // message receiver to use, etc.,

            String messageExchangePattern = axisOperation.getMessageExchangePattern();
            if (infoHolder.get(messageExchangePattern) == null) {
                infoHolder.put(messageExchangePattern, Boolean.TRUE);
            }

            if (mep == null) {

                opsFound = true;

                List soapHeaderInputParameterList = new ArrayList();
                List soapHeaderOutputParameterList = new ArrayList();

                methodElement = doc.createElement("method");

                String localPart = axisOperation.getName().getLocalPart();

                addAttribute(doc, "name", localPart, methodElement);
                addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                String style = axisOperation.getStyle();
                addAttribute(doc, "style", style, methodElement);
                addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                        methodElement);

                addAttribute(doc, "mep", axisOperation.getMessageExchangePattern(), methodElement);

                addSOAPAction(doc, methodElement, axisOperation);
                //add header ops for input
                addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                //add header ops for output
                addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                PolicyInclude policyInclude = axisOperation.getPolicyInclude();
                Policy policy = policyInclude.getPolicy();
                if (policy != null) {
                    addAttribute(doc, "policy", PolicyUtil.getPolicyAsString(policy), methodElement);
                }

                methodElement.appendChild(getInputElement(doc, axisOperation, soapHeaderInputParameterList));
                methodElement.appendChild(getOutputElement(doc, axisOperation, soapHeaderOutputParameterList));
                methodElement.appendChild(getFaultElement(doc, axisOperation));

                rootElement.appendChild(methodElement);
            } else {
                //mep is present - we move ahead only if the given mep matches the mep of this operation

                if (mep.equals(axisOperation.getMessageExchangePattern())) {
                    //at this point we know it's true
                    opsFound = true;
                    List soapHeaderInputParameterList = new ArrayList();
                    List soapHeaderOutputParameterList = new ArrayList();
                    methodElement = doc.createElement("method");
                    String localPart = axisOperation.getName().getLocalPart();

                    addAttribute(doc, "name", localPart, methodElement);
                    addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                    addAttribute(doc, "style", axisOperation.getStyle(), methodElement);
                    addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                            methodElement);

                    addAttribute(doc, "mep", axisOperation.getMessageExchangePattern(), methodElement);


                    addSOAPAction(doc, methodElement, axisOperation);
                    addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                    addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                    /*
                     * Setting the policy of the operation
                     */

                    Policy policy = axisOperation.getPolicyInclude().getPolicy();
                    if (policy != null) {
                        addAttribute(doc, "policy",
                                PolicyUtil.getPolicyAsString(policy),
                                methodElement);
                    }


                    methodElement.appendChild(getInputElement(doc,
                            axisOperation, soapHeaderInputParameterList));
                    methodElement.appendChild(getOutputElement(doc,
                            axisOperation, soapHeaderOutputParameterList));
                    methodElement.appendChild(getFaultElement(doc,
                            axisOperation));

                    rootElement.appendChild(methodElement);
                    //////////////////////
                }
            }

        }

        return opsFound;
    }

    // ==================================================================
    //                   Util Methods
    // ==================================================================

    private Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
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
     * @param doc
     * @param rootElement
     */
    private void fillSyncAttributes(Document doc, Element rootElement) {
        addAttribute(doc, "isAsync", this.codeGenConfiguration.isAsyncOn()
                ? "1"
                : "0", rootElement);
        addAttribute(doc, "isSync", this.codeGenConfiguration.isSyncOn()
                ? "1"
                : "0", rootElement);
    }

    /**
     * debugging method - write the output to the debugger
     *
     * @param description
     * @param doc
     */
    private void debugLogDocument(String description, Document doc) {
        if (log.isDebugEnabled()) {
            try {
                DOMSource source = new DOMSource(doc);
                StringWriter swrite = new StringWriter();
                swrite.write(description);
                swrite.write("\n");
                Transformer transformer =
                        TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("omit-xml-declaration", "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, new StreamResult(swrite));
                log.debug(swrite.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
     * A resusable method for the implementation of interface and implementation writing.
     *
     * @param model
     * @param writer
     * @throws java.io.IOException
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
     * Adds the soap action
     *
     * @param doc
     * @param rootElement
     * @param axisOperation
     */
    private void addSOAPAction(Document doc, Element rootElement, AxisOperation axisOperation) {
        addAttribute(doc, "soapaction", axisOperation.getSoapAction(), rootElement);
    }

    /**
     * populate the header parameters
     *
     * @param soapHeaderParameterQNameList
     * @param axisOperation
     * @param input
     */
    private void addHeaderOperations(List soapHeaderParameterQNameList, AxisOperation axisOperation,
                                     boolean input) {
        ArrayList headerparamList = new ArrayList();
        String MEP = axisOperation.getMessageExchangePattern();
        if (input) {
            if (isInputPresentForMEP(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    headerparamList = inaxisMessage.getSoapHeaders();

                }
            }
        } else {
            if (isOutputPresentForMEP(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    headerparamList = outAxisMessage.getSoapHeaders();
                }
            }
        }

        for (Iterator iterator = headerparamList.iterator(); iterator.hasNext();) {
            SOAPHeaderMessage header = (SOAPHeaderMessage) iterator.next();
            soapHeaderParameterQNameList.add(header.getElement());
        }
    }

    private boolean isOutputPresentForMEP(String MEP) {
        return WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                WSDLConstants.MEP_URI_IN_OUT.equals(MEP);
    }

    protected Element getInputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        String MEP = operation.getMessageExchangePattern();
        if (isInputPresentForMEP(MEP)) {
            Element param = getInputParamElement(doc, operation);

            if (param != null) {
                inputElt.appendChild(param);
            }

            List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

            for (int i = 0; i < parameterElementList.size(); i++) {
                inputElt.appendChild((Element) parameterElementList.get(i));
            }
        }
        return inputElt;
    }

    /**
     * Get the fault element - No header faults are supported
     *
     * @param doc
     * @param operation
     */
    protected Element getFaultElement(Document doc, AxisOperation operation) {
        Element faultElt = doc.createElement("fault");
        Element[] param = getFaultParamElements(doc, operation);

        for (int i = 0; i < param.length; i++) {
            faultElt.appendChild(param[i]);
        }

        return faultElt;
    }

    /**
     * Finds the output element.
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getOutputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        String MEP = operation.getMessageExchangePattern();
        if (isOutputPresentForMEP(MEP)) {
            Element param = getOutputParamElement(doc, operation);

            if (param != null) {
                outputElt.appendChild(param);
            }

            List outputElementList = getParameterElementList(doc, headerParameterQNameList, "header");
            for (int i = 0; i < outputElementList.size(); i++) {
                outputElt.appendChild((Element) outputElementList.get(i));
            }
        }
        return outputElt;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    private Element[] getFaultParamElements(Document doc, AxisOperation operation) {
        ArrayList params = new ArrayList();
        ArrayList faultMessages = operation.getFaultMessages();

        if (faultMessages != null && !faultMessages.isEmpty()) {
            Element paramElement;
            AxisMessage msg;
            for (int i = 0; i < faultMessages.size(); i++) {
                paramElement = doc.createElement("param");
                msg = (AxisMessage) faultMessages.get(i);

                //as for the name of a fault, we generate an exception
                addAttribute(doc, "name",
                        (String) fullyQualifiedFaultClassNameMap.get(msg.getElementQName()),
                        paramElement);
                addAttribute(doc, "intantiatiableName",
                        (String) InstantiatableFaultClassNameMap.get(msg.getElementQName()),
                        paramElement);
                addAttribute(doc, "shortName",
                        (String) faultClassNameMap.get(msg.getElementQName()),
                        paramElement);

                // attach the namespace and the localName
                addAttribute(doc, "namespace",
                        msg.getElementQName().getNamespaceURI(),
                        paramElement);
                addAttribute(doc, "localname",
                        msg.getElementQName().getLocalPart(),
                        paramElement);
                //the type represents the type that will be wrapped by this
                //name
                String typeMapping =
                        this.mapper.getTypeMappingName(msg.getElementQName());
                addAttribute(doc, "type", (typeMapping == null)
                        ? ""
                        : typeMapping, paramElement);
                String attribValue = (String) instantiatableMessageClassNames.
                        get(msg.getElementQName());

                addAttribute(doc, "instantiatableType",
                        attribValue == null ? "" : attribValue,
                        paramElement);

                // add an extra attribute to say whether the type mapping is
                // the default
                if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMapping)) {
                    addAttribute(doc, "default", "yes", paramElement);
                }
                addAttribute(doc, "value", getParamInitializer(typeMapping),
                        paramElement);

                Iterator iter = msg.getExtensibilityAttributes().iterator();
                while (iter.hasNext()) {
                    //TODO : implement this
//
                }
                params.add(paramElement);
            }

            return (Element[]) params.toArray(new Element[params.size()]);
        } else {
            return new Element[]{};//return empty array
        }


    }


    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    private Element getInputParamElement(Document doc, AxisOperation operation) {
        Element param = doc.createElement("param");
        AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

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
                //TODO : pls implement this
//                WSDLExtensibilityAttribute att = (WSDLExtensibilityAttribute) iter.next();
//                addAttribute(doc, att.getKey().getLocalPart(), att.getValue().toString(), param);
            }
        } else {
            param = null;
        }

        return param;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    private Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element param = doc.createElement("param");
        AxisMessage outputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        String typeMappingStr;
        String parameterName;

        if (outputMessage != null) {
            parameterName = this.mapper.getParameterName(outputMessage.getElementQName());
            String typeMapping = this.mapper.getTypeMappingName(outputMessage.getElementQName());
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

    /**
     * @param paramType
     */
    private String getParamInitializer(String paramType) {

        // Look up paramType in the table
        String out = (String) constructorMap.get(paramType);

        if (out == null) {
            out = "null";
        }

        return out;
    }

    /**
     * @param doc
     * @param parameters
     * @param location
     */
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
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param eltName
     * @param eltValue
     * @param element
     */
    protected Element addElement(Document document, String eltName, String eltValue, Element element) {
        Element elt = XSLTUtils.addChildElement(document, eltName, element);
        elt.appendChild(document.createTextNode(eltValue));
        return elt;
    }

//    ///////////////////////////////////////////////////////////////////////////
//    /////////////  Utility methods to travel the schemas and
//    /**
//     * run 1 -calcualte unique names
//     * @param schemas
//     */
//    private void calcualteSchemaNames(List schemas, Hashtable nameTable) {
//        //first traversal - fill the hashtable
//        for (int i = 0; i < schemas.size(); i++) {
//            XmlSchema schema = (XmlSchema) schemas.get(i);
//            XmlSchemaObjectCollection includes = schema.getIncludes();
//            for (int j = 0; j < includes.getCount(); j++) {
//                Object item = includes.getItem(i);
//                XmlSchema s = null;
//                if (item instanceof XmlSchemaExternal) {
//                    //recursively call the calculating
//                    XmlSchemaExternal externalSchema = (XmlSchemaExternal) item;
//                    s = externalSchema.getSchema();
//                    calcualteSchemaNames(Arrays.asList(
//                            new XmlSchema[]{s}),
//                            nameTable);
//                    nameTable.put(s,
//                            ("xsd" + count++));
//
//                }
//            }
//        }
//    }
//
//    /**
//     * Run 2  - adjust the names
//     * @param schemas
//     */
//    private void adjustSchemaNames(List schemas, Hashtable nameTable) {
//        //first traversal - fill the hashtable
//        for (int i = 0; i < schemas.size(); i++) {
//            XmlSchema schema = (XmlSchema) schemas.get(i);
//            XmlSchemaObjectCollection includes = schema.getIncludes();
//            for (int j = 0; j < includes.getCount(); j++) {
//                Object item = includes.getItem(i);
//                if (item instanceof XmlSchemaExternal) {
//                    //recursively call the name adjusting
//                    XmlSchemaExternal xmlSchemaExternal = (XmlSchemaExternal) item;
//                    adjustSchemaNames(Arrays.asList(
//                            new XmlSchema[]{xmlSchemaExternal.getSchema()}), nameTable);
//                    xmlSchemaExternal.setSchemaLocation(
//                            //this should return the correct end point!
//                            getName() +
//                                    "?xsd=" +
//                                    nameTable.get(xmlSchemaExternal.getSchema()));
//                }
//            }
//        }
//    }
//
//    /**
//     * Swap the key,value pairs
//     *
//     * @param originalTable
//     * @return
//     */
//    private Hashtable swapMappingTable(Hashtable originalTable) {
//        Hashtable swappedTable = new Hashtable(originalTable.size());
//        Iterator keys = originalTable.keySet().iterator();
//        Object key;
//        while (keys.hasNext()) {
//            key = keys.next();
//            swappedTable.put(originalTable.get(key), key);
//        }
//
//        return swappedTable;
//    }


}
