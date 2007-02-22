package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingMessage;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.WSDL20DefaultValueHolder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.util.CommandLineOptionConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.HTTPHeaderMessage;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.SOAPModuleMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.AntBuildWriter;
import org.apache.axis2.wsdl.codegen.writer.CallbackHandlerWriter;
import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.ExceptionWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceImplementationWriter;
import org.apache.axis2.wsdl.codegen.writer.InterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.MessageReceiverWriter;
import org.apache.axis2.wsdl.codegen.writer.SchemaWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonInterfaceWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.codegen.writer.TestClassWriter;
import org.apache.axis2.wsdl.codegen.writer.WSDL11Writer;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.util.MessagePartInformationHolder;
import org.apache.axis2.wsdl.util.TypeTesterUtil;
import org.apache.axis2.wsdl.util.XSLTIncludeResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.woden.internal.util.dom.DOM2Writer;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


public class AxisServiceBasedMultiLanguageEmitter implements Emitter {

    protected static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    protected static final String STUB_SUFFIX = "Stub";
    protected static final String TEST_SUFFIX = "Test";
    protected static final String SKELETON_CLASS_SUFFIX = "Skeleton";
    protected static final String SKELETON_CLASS_SUFFIX_BACK = "Impl";
    protected static final String SKELETON_INTERFACE_SUFFIX = "SkeletonInterface";
    // keep a seperate variable for  SKELETON_INTERFACE_SUFFIX_BACK although it is
    // "" to accomadate any future changes easily.
    protected static final String SKELETON_INTERFACE_SUFFIX_BACK = "";
    protected static final String STUB_INTERFACE_SUFFIX_BACK = "Stub";
    protected static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";
    protected static final String FAULT_SUFFIX = "Exception";
    protected static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";

    protected static Map mepToClassMap;
    protected static Map mepToSuffixMap;

    protected AxisBinding axisBinding;

    protected int uniqueFaultNameCounter = 0;
    /**
     * Field constructorMap
     */
    protected static HashMap constructorMap = new HashMap(50);

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
        mepToClassMap = new HashMap();
        mepToClassMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY,
                "org.apache.axis2.receivers.AbstractInMessageReceiver");
        mepToClassMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY,
                "org.apache.axis2.receivers.AbstractRobustInMessageReceiver");
        mepToClassMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT,
                "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");

        //populate the MEP -> suffix map
        mepToSuffixMap = new HashMap();
        mepToSuffixMap.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY,
                           MESSAGE_RECEIVER_SUFFIX + "InOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY,
                           MESSAGE_RECEIVER_SUFFIX + "InOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_ROBUST_IN_ONLY,
                           MESSAGE_RECEIVER_SUFFIX + "RobustInOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY,
                           MESSAGE_RECEIVER_SUFFIX + "RobustInOnly");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_OUT,
                           MESSAGE_RECEIVER_SUFFIX + "InOut");
        mepToSuffixMap.put(WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT,
                           MESSAGE_RECEIVER_SUFFIX + "InOut");
        //register the other types as necessary
    }

    //~--- fields -------------------------------------------------------------
    protected static final Log log = LogFactory.getLog(AxisServiceBasedMultiLanguageEmitter.class);
    protected URIResolver resolver;

    protected Map infoHolder;

    protected CodeGenConfiguration codeGenConfiguration;

    protected TypeMapper mapper;

    protected AxisService axisService;

    //a map to keep the fault classNames
    protected Map fullyQualifiedFaultClassNameMap = new HashMap();
    protected Map faultClassNameMap = new HashMap();

    protected Map instantiatableMessageClassNames = new HashMap();

    protected static final String TEST_SRC_DIR_NAME = "test";


    /**
     * default constructor - builds
     */
    public AxisServiceBasedMultiLanguageEmitter() {
        infoHolder = new HashMap();
    }

    /**
     * Sets the relevant codegen configuration
     *
     * @param configuration
     * @see Emitter#setCodeGenConfiguration(org.apache.axis2.wsdl.codegen.CodeGenConfiguration)
     */
    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.codeGenConfiguration = configuration;
        this.axisService = codeGenConfiguration.getAxisService();
        this.axisBinding = axisService.getEndpoint(axisService.getEndpointName()).getBinding();
        resolver = new XSLTIncludeResolver(codeGenConfiguration);
    }

    /**
     * Sets the type mapper
     *
     * @param mapper
     * @see Emitter#setMapper(org.apache.axis2.wsdl.databinding.TypeMapper)
     */
    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    private Object getBindingPropertyFromOperation(String name, QName qName) {

        // Get the correct AxisBindingOperation coresponding to the AxisOperation
        AxisBindingOperation axisBindingOperation =
                (AxisBindingOperation) axisBinding.getChild(qName);
        Object property = null;

        if (axisBindingOperation != null) {
            property = axisBindingOperation.getProperty(name);
        }

        if (property == null) {
            property = axisBinding.getProperty(name);
        }

        if (property == null) {
            property = WSDL20DefaultValueHolder.getDefaultValue(name);
        }

        return property;
    }

    private Object getBindingPropertyFromMessage(String name, QName qName, String key) {

        Object property = null;
        // Get the correct AxisBindingOperation coresponding to the AxisOperation
        AxisBindingOperation axisBindingOperation =
                (AxisBindingOperation) axisBinding.getChild(qName);

        AxisBindingMessage axisBindingMessage = null;
        if (axisBindingOperation != null) {
            axisBindingMessage = (AxisBindingMessage) axisBindingOperation.getChild(key);
            if (axisBindingMessage != null) {
                property = axisBindingMessage.getProperty(name);
            }

            if (property == null) {
                property = axisBindingOperation.getProperty(name);
            }
        }


        if (property == null) {
            property = axisBinding.getProperty(name);
        }

        if (property == null) {
            property = WSDL20DefaultValueHolder.getDefaultValue(name);
        }

        return property;
    }

    private Object getBindingPropertyFromMessageFault(String name, QName qName, String key) {

        Object property = null;
        // Get the correct AxisBindingOperation coresponding to the AxisOperation
        AxisBindingOperation axisBindingOperation =
                (AxisBindingOperation) axisBinding.getChild(qName);

        AxisBindingMessage axisBindingMessageFault = null;
        AxisBindingMessage axisBindingFault = null;
        if (axisBindingOperation != null) {
            axisBindingMessageFault = (AxisBindingMessage) axisBindingOperation.getFault(key);

            if (axisBindingMessageFault != null) {
                property = axisBindingMessageFault.getProperty(name);
            }

            if (property == null) {
                axisBindingFault = axisBinding.getFault(key);
                property = axisBindingFault.getProperty(name);
            }
        }

        if (property == null) {
            property = WSDL20DefaultValueHolder.getDefaultValue(name);
        }

        return property;
    }


    /**
     * Update mapper for the stub
     */
    protected void updateMapperForStub() {
        updateMapperClassnames(getFullyQualifiedStubName());
    }

    /**
     * Returns the fully qualified Stub name
     * reused in many methods
     *
     * @return classname
     */
    protected String getFullyQualifiedStubName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + STUB_SUFFIX;
    }

    /**
     * rests the fault name maps
     */
    protected void resetFaultNames() {
        fullyQualifiedFaultClassNameMap.clear();
        faultClassNameMap.clear();
    }

    /**
     * Populate a map of fault class names
     */
    protected void generateAndPopulateFaultNames() {
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
     * @throws CodeGenerationException
     * @see Emitter#emitStub()
     */
    public void emitStub() throws CodeGenerationException {
        try {
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

            // write the Exceptions
            writeExceptions();

            // write interface implementations
            writeInterfaceImplementation();

            // write the test classes
            writeTestClasses();

            // write an ant build file
            // Note that ant build is generated only once
            // and that has to happen here only if the
            // client side code is required
            if (!codeGenConfiguration.isGenerateAll()) {
                //our logic for the build xml is that it will
                //only be written when not flattened
                if (!codeGenConfiguration.isFlattenFiles()) {
                    writeAntBuild();
                }
            }
        } catch (CodeGenerationException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
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
        String[] dotSeparatedValues = packageName.split("\\.");

        addAttribute(doc, "package", dotSeparatedValues[0], rootElement);
        addAttribute(doc, "name", serviceName, rootElement);
        addAttribute(doc, "servicename", serviceName, rootElement);
        addAttribute(doc, "src", codeGenConfiguration.getSourceLocation(), rootElement);
        addAttribute(doc, "resource", codeGenConfiguration.getResourceLocation(), rootElement);
        if (codeGenConfiguration.isServerSide()) {
            addAttribute(doc,
                    "isserverside",
                    "yes",
                    rootElement);
        }

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
                    new TestClassWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            TEST_SRC_DIR_NAME),
                            codeGenConfiguration.getOutputLanguage());

            writeClass(classModel, callbackWriter);
        }
    }

    /**
     * Creates the XML Model for the test case
     *
     * @return DOM document
     */
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
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "stubname",
                    makeJavaClassName(axisService.getBindingName()) + STUB_SUFFIX, rootElement);
        } else {
            addAttribute(doc, "stubname", coreClassName + STUB_SUFFIX, rootElement);
        }

        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
                rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc, false));
        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////

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
                new InterfaceImplementationWriter(
                        codeGenConfiguration.isFlattenFiles() ?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                        codeGenConfiguration.getSourceLocation()),
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

        addAttribute(doc, "servicename", localPart, rootElement);
        //The target nemespace is added as the namespace for this service
        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "interfaceName",
                    makeJavaClassName(axisService.getEndpointName()) + STUB_INTERFACE_SUFFIX_BACK,
                    rootElement);
            addAttribute(doc, "name", makeJavaClassName(axisService.getBindingName()) + STUB_SUFFIX,
                    rootElement);
        } else {
            addAttribute(doc, "interfaceName", localPart, rootElement);
            addAttribute(doc, "name", stubName, rootElement);
        }

        addAttribute(doc, "callbackname", localPart + CALL_BACK_HANDLER_SUFFIX, rootElement);
        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
                rootElement);

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
        rootElement.appendChild(createDOMElementforDatabinders(doc, false));

        Object moduleCodegenPolicyExtensionElement;

        //if some extension has added the stub methods property, add them to the
        //main document
        if ((moduleCodegenPolicyExtensionElement =
                codeGenConfiguration.getProperty("module-codegen-policy-extensions")) != null) {
            rootElement.appendChild(
                    doc.importNode((Element) moduleCodegenPolicyExtensionElement, true));
        }

        //add another element to have the unique list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));


        doc.appendChild(rootElement);

        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;
    }

    /**
     * A util method that returns a unique list of faults
     *
     * @param doc
     * @return DOM element
     */
    protected Element getUniqueListofFaults(Document doc) {
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
            if (mapper.getDefaultMappingName().equals(typeMapping)) {
                addAttribute(doc, "default", "yes", faultElement);
            }
            addAttribute(doc, "value", getParamInitializer(typeMapping),
                    faultElement);


            rootElement.appendChild(faultElement);
        }
        return rootElement;
    }

    /**
     * add the qNames of the operation fault message names to faultMessages Mep
     *
     * @param operationFaultMessages
     * @param faultMessagesToMep
     */

    private void addFaultMessages(List operationFaultMessages, Set faultMessagesToMep) {

        AxisMessage faultMessage;
        for (Iterator iter = operationFaultMessages.iterator(); iter.hasNext();) {
            faultMessage = (AxisMessage) iter.next();
            faultMessagesToMep.add(faultMessage.getElementQName());
        }

    }

    /**
     * A util method that returns a unique list of faults for a given mep
     *
     * @param doc
     * @return DOM element
     */
    protected Element getUniqueListofFaultsofMep(Document doc, String mep) {

        //  list to keep fault message qnames for this mep
        Set faultListForMep = new HashSet();

        Iterator iter = this.axisService.getOperations();
        AxisOperation axisOperation;

        for (; iter.hasNext();) {
            axisOperation = (AxisOperation) iter.next();
            if (mep == null) {
                // add the fault messages
                addFaultMessages(axisOperation.getFaultMessages(), faultListForMep);
            } else {
                if (mep.equals(axisOperation.getMessageExchangePattern())) {
                    // add the fault messages
                    addFaultMessages(axisOperation.getFaultMessages(), faultListForMep);
                }
            }
        }

        Element rootElement = doc.createElement("fault-list");
        Element faultElement;
        QName key;
        Iterator iterator = faultListForMep.iterator();
        while (iterator.hasNext()) {
            faultElement = doc.createElement("fault");
            key = (QName) iterator.next();

            //as for the name of a fault, we generate an exception
            addAttribute(doc, "name",
                    (String) fullyQualifiedFaultClassNameMap.get(key),
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

            String exceptionName = key.getLocalPart();
            addAttribute(doc, "localname",
                    exceptionName == null ? "" : exceptionName,
                    faultElement);

            // add an extra attribute to say whether the type mapping is
            // the default
            if (mapper.getDefaultMappingName().equals(typeMapping)) {
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
            String policyString = PolicyUtil.policyComponentToString(servicePolicy);
            policyString = PolicyUtil.getSafeString(policyString);
            addAttribute(doc, "policy", policyString, rootElement);
        }

        Element endpointElement = doc.createElement("endpoint");

        String endpoint = axisService.getEndpointURL();
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
        addAttribute(doc, "soap-version",
                (String) axisBinding.getProperty(WSDL2Constants.ATTR_WSOAP_VERSION), rootElement);
    }

    /**
     * Writes the exceptions.
     */
    protected void writeExceptions() throws Exception {
        Element faultElement;
        QName key;
        Iterator iterator = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (iterator.hasNext()) {
            Document doc = getEmptyDocument();

            faultElement = doc.createElement("fault");

            addAttribute(doc, "package", codeGenConfiguration.getPackageName(), faultElement);

            key = (QName) iterator.next();

            //as for the name of a fault, we generate an exception
            addAttribute(doc, "name",
                    (String) faultClassNameMap.get(key),
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
            if (mapper.getDefaultMappingName().equals(typeMapping)) {
                addAttribute(doc, "default", "yes", faultElement);
            }
            addAttribute(doc, "value", getParamInitializer(typeMapping),
                    faultElement);
            ExceptionWriter exceptionWriter =
                    new ExceptionWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            codeGenConfiguration.getSourceLocation()),
                            codeGenConfiguration.getOutputLanguage());

            doc.appendChild(faultElement);
            writeClass(doc, exceptionWriter);
        }
    }

    /**
     * Generates the model for the callbacks.
     */
    protected Document createDOMDocumentForException() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("callback");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name",
                makeJavaClassName(axisService.getName()) + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // TODO JAXRPC mapping support should be considered here ??
        this.loadOperations(doc, rootElement, null);

        doc.appendChild(rootElement);
        return doc;
    }


    /**
     * Writes the callback handlers.
     */
    protected void writeCallBackHandlers() throws Exception {
        if (codeGenConfiguration.isAsyncOn()) {
            Document interfaceModel = createDOMDocumentForCallbackHandler();
            debugLogDocument("Document for callback handler:", interfaceModel);
            CallbackHandlerWriter callbackWriter =
                    new CallbackHandlerWriter(
                            codeGenConfiguration.isFlattenFiles() ?
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            null) :
                                    getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                            codeGenConfiguration.getSourceLocation()),
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
        addAttribute(doc, "name",
                makeJavaClassName(axisService.getName()) + CALL_BACK_HANDLER_SUFFIX, rootElement);

        // TODO JAXRPC mapping support should be considered here ??
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
                new InterfaceWriter(
                        codeGenConfiguration.isFlattenFiles() ?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                        codeGenConfiguration.getSourceLocation()),
                        this.codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceModel, interfaceWriter);
    }

    /**
     * Creates the DOM tree for the interface creation. Uses the interface.
     */
    protected Document createDOMDocumentForInterface(boolean writeDatabinders) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");
        String localPart = null;
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            localPart =
                    makeJavaClassName(axisService.getEndpointName() + STUB_INTERFACE_SUFFIX_BACK);
        } else {
            localPart = makeJavaClassName(axisService.getName());
        }

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", localPart, rootElement);
        addAttribute(doc, "callbackname",
                makeJavaClassName(axisService.getName()) + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
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
     * Update mapper for message receiver
     */
    protected void updateMapperForMessageReceiver() {
        updateMapperClassnames(getFullyQualifiedMessageReceiverName());
    }

    /**
     * @return fully qualified MR name
     */
    protected String getFullyQualifiedMessageReceiverName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        return packageName + "." + localPart + MESSAGE_RECEIVER_SUFFIX;
    }

    /**
     * @return fully qualified skeleton name
     */
    protected String getFullyQualifiedSkeletonName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        String skeltonName;
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            skeltonName = packageName + "." + makeJavaClassName(axisService.getBindingName()) +
                    SKELETON_CLASS_SUFFIX_BACK;
        } else {
            skeltonName = packageName + "." + localPart + SKELETON_CLASS_SUFFIX;
        }
        return skeltonName;
    }

    /**
     * @return fully qualified skeleton interface name
     */
    protected String getFullyQualifiedSkeletonInterfaceName() {
        String packageName = codeGenConfiguration.getPackageName();
        String localPart = makeJavaClassName(axisService.getName());
        String skeltonInterfaceName;
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            skeltonInterfaceName = packageName + "." +
                    makeJavaClassName(axisService.getEndpointName()) +
                    SKELETON_INTERFACE_SUFFIX_BACK;
        } else {
            skeltonInterfaceName = packageName + "." + localPart + SKELETON_INTERFACE_SUFFIX;
        }
        return skeltonInterfaceName;
    }

    /**
     * Emits the skeleton
     *
     * @throws CodeGenerationException
     */
    public void emitSkeleton() throws CodeGenerationException {

        try {
            // see the comment at updateMapperClassnames for details and reasons for
            // calling this method
            if (mapper.isObjectMappingPresent()) {
                updateMapperForMessageReceiver();
            } else {
                copyToFaultMap();
            }

            //handle faults
            generateAndPopulateFaultNames();

            //
            if (codeGenConfiguration.isServerSideInterface()) {
                //write skeletonInterface
                writeSkeletonInterface();
            }

            // write skeleton only if the used has
            // asked for the deployment descriptor in the interface mode
            // else write it anyway :)
            if (codeGenConfiguration.isServerSideInterface()) {
                if (codeGenConfiguration.isGenerateDeployementDescriptor()) {
                    writeSkeleton();
                }
            } else {
                writeSkeleton();
            }

            // write a MessageReceiver for this particular service.
            writeMessageReceiver();

            // write the Exceptions
            writeExceptions();

            // write service xml
            // if asked
            if (codeGenConfiguration.isGenerateDeployementDescriptor()) {
                writeServiceXml();
            }

            //write the ant build
            //we skip this for the flattened case
            if (!codeGenConfiguration.isFlattenFiles()) {
                writeAntBuild();
            }

            //for the server side codegen
            //we need to serialize the WSDL's
            writeWSDLFiles();

        } catch (CodeGenerationException cgExp) {
            throw cgExp;
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    /**
     * Write out the WSDL files (and the schemas)
     * writing the WSDL (and schemas) is somewhat special so we cannot follow
     * the usual pattern of using the class writer
     */
    protected void writeWSDLFiles() {

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
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getResourceLocation()));


        Map schemaMappings = axisService.getSchemaMappingTable();
        Iterator keys = schemaMappings.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            schemaWriter.writeSchema(
                    (XmlSchema) schemaMappings.get(key),
                    (String) key
            );
        }

        //switch between the correct writer
        if (CommandLineOptionConstants.WSDL2JavaConstants.WSDL_VERSION_2.
                equals(codeGenConfiguration.getWSDLVersion())) {

//            WSDL20Writer wsdl20Writer = new WSDL20Writer(
//                    codeGenConfiguration.isFlattenFiles() ?
//                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
//                            getOutputDirectory(codeGenConfiguration.getOutputLocation(),
//                                    codeGenConfiguration.getResourceLocation())
//            );
//            wsdl20Writer.writeWSDL(axisService);

            // TODO : We can not write WSDL 2.0 documents. Even WSDL20Writer implementation is wrong.
            // wait till Woden implements it.

        } else {

            WSDL11Writer wsdl11Writer = new WSDL11Writer(
                    codeGenConfiguration.isFlattenFiles() ?
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                            getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                    codeGenConfiguration.getResourceLocation()));
            wsdl11Writer.writeWSDL(axisService);

        }


    }

    /**
     * Utility method to copy the faults to the correct map
     */
    protected void copyToFaultMap() {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            instantiatableMessageClassNames.put(key,
                    classNameMap.get(key));
        }
    }

    /**
     * Change the fault classnames to go with the package and stub
     */
    protected void updateFaultPackageForStub() {
        Iterator faultClassNameKeys = fullyQualifiedFaultClassNameMap.keySet().iterator();
        while (faultClassNameKeys.hasNext()) {
            Object key = faultClassNameKeys.next();
            String className = (String) fullyQualifiedFaultClassNameMap.get(key);
            //append the skelton name
            String fullyQualifiedStubName = getFullyQualifiedStubName();
            fullyQualifiedFaultClassNameMap.put(key, codeGenConfiguration.getPackageName() + "."
                    + className);
        }
    }

    /**
     * Writes the message receiver
     *
     * @throws Exception
     */
    protected void writeMessageReceiver() throws Exception {

        if (codeGenConfiguration.isWriteMessageReceiver()) {
            //loop through the meps and generate code for each mep
            Iterator it = mepToClassMap.keySet().iterator();
            while (it.hasNext()) {
                String mep = (String) it.next();
                Document classModel = createDocumentForMessageReceiver(
                        mep,
                        codeGenConfiguration.isServerSideInterface());
                debugLogDocument("Document for message receiver (mep=" + mep +
                        "):", classModel);
                //write the class only if any methods are found
                if (Boolean.TRUE.equals(infoHolder.get(mep))) {
                    MessageReceiverWriter writer =
                            new MessageReceiverWriter(
                                    codeGenConfiguration.isFlattenFiles() ?
                                            getOutputDirectory(
                                                    codeGenConfiguration.getOutputLocation(),
                                                    null) :
                                            getOutputDirectory(
                                                    codeGenConfiguration.getOutputLocation(),
                                                    codeGenConfiguration.getSourceLocation()),
                                    codeGenConfiguration.getOutputLanguage());

                    writeClass(classModel, writer);
                }
            }
        }
    }

    /**
     * Creates the XML model for the message receiver
     *
     * @param mep
     * @param isServerSideInterface
     * @return DOM Document
     */
    protected Document createDocumentForMessageReceiver(String mep, boolean isServerSideInterface) {

        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);

        String localPart = makeJavaClassName(axisService.getName());

        addAttribute(doc, "name", localPart + mepToSuffixMap.get(mep), rootElement);
        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
                rootElement);

        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "skeletonname",
                    makeJavaClassName(axisService.getBindingName()) + SKELETON_CLASS_SUFFIX_BACK,
                    rootElement);
            if (isServerSideInterface) {
                addAttribute(doc, "skeletonInterfaceName", makeJavaClassName(
                        axisService.getEndpointName()) + SKELETON_INTERFACE_SUFFIX_BACK,
                        rootElement);
            } else {
                addAttribute(doc, "skeletonInterfaceName", makeJavaClassName(
                        axisService.getBindingName()) + SKELETON_CLASS_SUFFIX_BACK,
                        rootElement);
            }
        } else {
            addAttribute(doc, "skeletonname", localPart + SKELETON_CLASS_SUFFIX, rootElement);
            if (isServerSideInterface) {
                addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_INTERFACE_SUFFIX,
                        rootElement);
            } else {
                addAttribute(doc, "skeletonInterfaceName", localPart + SKELETON_CLASS_SUFFIX,
                        rootElement);
            }
        }

        addAttribute(doc, "basereceiver", (String) mepToClassMap.get(mep), rootElement);

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
        //create the databinder element with serverside as true
        rootElement.appendChild(createDOMElementforDatabinders(doc, true));

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaultsofMep(doc, mep));

        doc.appendChild(rootElement);

        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////

        return doc;
    }

    /**
     * create a dom element for databinders. This is called by other
     *
     * @param doc
     */
    protected Element createDOMElementforDatabinders(Document doc, boolean isServerside) {

        // First Iterate through the operations and find the relevant fromOM and toOM methods to be generated
        ArrayList parameters = new ArrayList();


        for (Iterator operationsIterator = axisService.getOperations(); operationsIterator.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();

            // Add the parameters to a map with their type as the key
            // this step is needed to remove repetitions

            // process the input parameters
            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDLUtil.isInputPresentForMEP(MEP)) {
                Element[] inputParamElement = getInputParamElement(doc, axisOperation);
                for (int i = 0; i < inputParamElement.length; i++) {
                    //add an attribute to the parameter saying that this is an
                    //input
                    addAttribute(doc, "direction", "in", inputParamElement[i]);
                    //add the short type name
                    parameters.add(
                            inputParamElement[i]);

                }
            }
            // process output parameters
            if (WSDLUtil.isOutputPresentForMEP(MEP)) {
                Element outputParamElement = getOutputParamElement(doc, axisOperation);
                if (outputParamElement != null) {
                    //set the direction as out
                    addAttribute(doc, "direction", "out", outputParamElement);
                    parameters.add(outputParamElement);
                }
            }

            //process faults
            Element[] faultParamElements = getFaultParamElements(doc, axisOperation);
            for (int i = 0; i < faultParamElements.length; i++) {
                //set the direction as out - all faults are out messages ?
                addAttribute(doc, "direction", "out", faultParamElements[i]);
                parameters.add(faultParamElements[i]);
            }

            // process the header parameters
            Element newChild;
            List headerParameterQNameList = new ArrayList();
            addHeaderOperations(headerParameterQNameList, axisOperation, true);
            List parameterElementList = getParameterElementList(doc, headerParameterQNameList,
                    WSDLConstants.SOAP_HEADER);

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameters.add(newChild);
            }

            headerParameterQNameList.clear();
            parameterElementList.clear();
            addHeaderOperations(headerParameterQNameList, axisOperation, false);
            parameterElementList = getParameterElementList(doc, headerParameterQNameList,
                    WSDLConstants.SOAP_HEADER);

            for (int i = 0; i < parameterElementList.size(); i++) {
                newChild = (Element) parameterElementList.get(i);
                parameters.add(newChild);
            }
        }

        Element rootElement = doc.createElement("databinders");
        //add the db type attribute  - the name of the databinding type
        //this will be used to select the correct template
        addAttribute(doc, "dbtype", codeGenConfiguration.getDatabindingType(), rootElement);
        //add the wrapped flag state - this is used by JiBX, but may be useful
        //for other frameworks in the future
        String wrapflag = Boolean.toString(codeGenConfiguration.isParametersWrapped());
        addAttribute(doc, "wrapped", wrapflag, rootElement);

        //at this point we may need to capture the extra parameters passes to the
        //particular databinding framework
        //these parameters showup in the property map with String keys, and we
        //can just copy these items as attributes of the <extra> element.
        Element extraElement = addElement(doc, "extra", null, rootElement);
        Map propertiesMap = codeGenConfiguration.getProperties();
        for (Iterator it = propertiesMap.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            if (key instanceof String) {
                Object value = propertiesMap.get(key);
                //if the value is null set it to empty string
                if (value == null) value = "";
                //add key="value" attribute to element iff value a string
                if (value instanceof String) {
                    addAttribute(doc, (String) key, (String) value, extraElement);
                }
            }
        }

        //add the server side attribute. this helps the databinding template
        //to determine the methods to generate
        if (isServerside) {
            addAttribute(doc, "isserverside", "yes", rootElement);
        }
        // add the names of the elements that have base 64 content
        // if the base64 name list is missing then this whole step is skipped
        rootElement.appendChild(getBase64Elements(doc));

        //add the method names
        rootElement.appendChild(getOpNames(doc));

        for (Iterator iterator = parameters.iterator(); iterator.hasNext();) {
            rootElement.appendChild((Element) iterator.next());
        }

        // finish with any extra information from service and operations
        Parameter details = axisService.getParameter(Constants.DATABINDING_SERVICE_DETAILS);
        if (details != null) {
            Object value = details.getValue();
            if (value instanceof Element) {
                rootElement.appendChild(doc.importNode((Element) value, true));
            } else if (value instanceof List) {
                for (Iterator iter = ((List) value).iterator(); iter.hasNext();) {
                    rootElement.appendChild(doc.importNode((Element) iter.next(), true));
                }
            }
        }

        for (Iterator operationsIterator = axisService.getOperations(); operationsIterator.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
            details = axisOperation.getParameter(Constants.DATABINDING_OPERATION_DETAILS);
            if (details != null) {
                rootElement.appendChild(doc.importNode((Element) details.getValue(), true));
            }
        }

        ///////////////////////////////////////////////
//        System.out.println("databinding root element " + DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////

        return rootElement;
    }

    /**
     * Adds the short type name to the given parameter element
     * if the type has no '.' characters in its name
     * the type itself is taken as the shorttype
     *
     * @param paramElement
     */
    protected void addShortType(Element paramElement, String type) {
        if (type != null && type.indexOf('.') != -1) {
            addAttribute(paramElement.getOwnerDocument(),
                    "shorttype",
                    type.substring(type.lastIndexOf('.') + 1),
                    paramElement);
        } else {
            addAttribute(paramElement.getOwnerDocument(),
                    "shorttype",
                    type == null ? "" : type,
                    paramElement);
        }

    }

    /**
     * Gets an element representing the operation names
     *
     * @param doc
     * @return Returns Element.
     */
    protected Element getOpNames(Document doc) {
        Element root = doc.createElement("opnames");
        Element elt;


        for (Iterator operationsIterator = axisService.getOperations(); operationsIterator.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operationsIterator.next();
            elt = doc.createElement("name");
            elt.appendChild(doc.createTextNode(axisOperation.getName().getLocalPart()));

            //what needs to be put here as the opertation namespace is actually the
            //traget namespace of the service
            addAttribute(doc, "opnsuri", axisService.getTargetNamespace(), elt);
            root.appendChild(elt);
        }

        return root;
    }

    /**
     * Gets the base64 types. If not available this will be empty!!!
     *
     * @param doc
     * @return Returns Element.
     */
    protected Element getBase64Elements(Document doc) {
        Element root = doc.createElement("base64Elements");
        Element elt;
        QName qname;

        // this is a list of QNames
        List list = (List) codeGenConfiguration.getProperties().get(Constants.BASE_64_PROPERTY_KEY);

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
    protected void processModelObjects(Map objectMappings, Element root, Document doc) {
        Iterator objectIterator = objectMappings.values().iterator();

        while (objectIterator.hasNext()) {
            Object o = objectIterator.next();

            if (o instanceof Document) {
                //we cannot have an empty document
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
     * in this case we modify the package name to have the class a inner class of the stub,
     * interface or the message receiver depending on the style
     */
    protected void updateMapperClassnames(String fullyQulifiedIncludingClassNamePrefix) {
        Map classNameMap = mapper.getAllMappedNames();
        Iterator keys = classNameMap.keySet().iterator();

        while (keys.hasNext()) {
            Object key = keys.next();
            String className = (String) classNameMap.get(key);

            //this is a generated class name - update the name
            if (!TypeTesterUtil.hasPackage(className) && !TypeTesterUtil.isPrimitive(className)) {
                classNameMap.put(key, fullyQulifiedIncludingClassNamePrefix + "." + className);
                instantiatableMessageClassNames.put(key,
                        fullyQulifiedIncludingClassNamePrefix + "$" + className);
            } else {
                //this is a fully qualified class name - just leave it as it is
                classNameMap.put(key, className);
                instantiatableMessageClassNames.put(key,
                        className);
            }
        }
    }

    /**
     * Write the service XML
     *
     * @throws Exception
     */
    protected void writeServiceXml() throws Exception {

        // Write the service xml in a folder with the
        Document serviceXMLModel = createDOMDocumentForServiceXML();
        debugLogDocument("Document for service XML:", serviceXMLModel);
        ClassWriter serviceXmlWriter =
                new ServiceXMLWriter(
                        codeGenConfiguration.isFlattenFiles() ?
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                                getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                        codeGenConfiguration.getResourceLocation()),
                        this.codeGenConfiguration.getOutputLanguage());

        writeClass(serviceXMLModel, serviceXmlWriter);

    }

    protected Document createDOMDocumentForServiceXML() {
        Document doc = getEmptyDocument();
        String className = null;
        String serviceName = axisService.getName();
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            className = makeJavaClassName(axisService.getBindingName());
        } else {
            className = makeJavaClassName(serviceName);
        }

        doc.appendChild(getServiceElement(serviceName, className, doc));
        return doc;

    }

    /**
     * A resusable method to return the service element for creating the
     * service xml
     *
     * @param serviceName
     * @param className
     * @param doc
     * @return DOM Element
     */
    protected Element getServiceElement(String serviceName, String className, Document doc) {
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", codeGenConfiguration.getPackageName(), rootElement);
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "name", className + SKELETON_CLASS_SUFFIX_BACK, rootElement);
        } else {
            addAttribute(doc, "name", className + SKELETON_CLASS_SUFFIX, rootElement);
        }

        if (!codeGenConfiguration.isWriteTestCase()) {
            addAttribute(doc, "testOmit", "true", rootElement);
        }
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = mepToClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.get(key))) {
                Element elt = addElement(doc, "messagereceiver",
                        makeJavaClassName(serviceName) + mepToSuffixMap.get(key), rootElement);
                addAttribute(doc, "mepURI", key.toString(), elt);
            }

        }

        loadOperations(doc, rootElement, null);

        return rootElement;
    }

    protected void writeSkeleton() throws Exception {
        Document skeletonModel =
                createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());
        debugLogDocument("Document for skeleton:", skeletonModel);
        ClassWriter skeletonWriter = new SkeletonWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    /**
     * Write the skeletonInterface
     *
     * @throws Exception
     */
    protected void writeSkeletonInterface() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeletonInterface();
        debugLogDocument("Document for skeleton Interface:", skeletonModel);
        ClassWriter skeletonInterfaceWriter = new SkeletonInterfaceWriter(
                codeGenConfiguration.isFlattenFiles() ?
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(), null) :
                        getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                                codeGenConfiguration.getSourceLocation())
                , this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonInterfaceWriter);
    }

    /**
     * Creates the XMLModel for the skeleton
     *
     * @param isSkeletonInterface
     * @return DOM Document
     */
    protected Document createDOMDocumentForSkeleton(boolean isSkeletonInterface) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "name",
                    makeJavaClassName(axisService.getBindingName()) + SKELETON_CLASS_SUFFIX_BACK,
                    rootElement);
        } else {
            addAttribute(doc, "name", serviceName + SKELETON_CLASS_SUFFIX, rootElement);
        }
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);
        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
                rootElement);
        if (isSkeletonInterface) {
            if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
                addAttribute(doc, "skeletonInterfaceName", makeJavaClassName(
                        axisService.getEndpointName()) + SKELETON_INTERFACE_SUFFIX_BACK,
                        rootElement);
            } else {
                addAttribute(doc, "skeletonInterfaceName", serviceName + SKELETON_INTERFACE_SUFFIX,
                        rootElement);
            }

        }
        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);

        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;

    }

    /**
     * Creates the XML model for the skeleton interface
     *
     * @return DOM Document
     */
    protected Document createDOMDocumentForSkeletonInterface() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        if (this.codeGenConfiguration.isBackwordCompatibilityMode()) {
            addAttribute(doc, "name", makeJavaClassName(axisService.getEndpointName()) +
                    SKELETON_INTERFACE_SUFFIX_BACK, rootElement);
        } else {
            addAttribute(doc, "name", serviceName + SKELETON_INTERFACE_SUFFIX, rootElement);
        }

        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);

        //add backwordcompatibility attribute
        addAttribute(doc, "isbackcompatible",
                String.valueOf(codeGenConfiguration.isBackwordCompatibilityMode()),
                rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        //////////////////////////////////////////////////////////
//        System.out.println(DOM2Writer.nodeToString(rootElement));
        ////////////////////////////////////////////////////////////
        return doc;

    }

    /**
     * Loads the operations
     *
     * @param doc
     * @param rootElement
     * @param mep
     * @return boolean
     */
    protected boolean loadOperations(Document doc, Element rootElement, String mep) {
        Element methodElement;
        String serviceName = makeJavaClassName(axisService.getName());

        Iterator operationNames = axisService.getOperationsNameList().iterator();
        boolean opsFound = false;
        QName operationName;
        while (operationNames.hasNext()) {
            operationName = (QName) operationNames.next();
            AxisOperation axisOperation = axisService.getOperation(operationName);

            // populate info holder with mep information. This will used in determining which
            // message receiver to use, etc.,


            String messageExchangePattern = axisOperation.getMessageExchangePattern();
            if (infoHolder.get(messageExchangePattern) == null) {
                infoHolder.put(messageExchangePattern, Boolean.TRUE);
            }

            if (mep == null) {
                opsFound = true;
                methodElement = generateMethodElement(doc, serviceName, axisOperation);
                rootElement.appendChild(methodElement);

            } else {
                //mep is present - we move ahead only if the given mep matches the mep of this operation

                if (mep.equals(axisOperation.getMessageExchangePattern())) {
                    //at this point we know it's true
                    opsFound = true;
                    methodElement = generateMethodElement(doc, serviceName, axisOperation);
                    rootElement.appendChild(methodElement);
                    //////////////////////
                }
            }

        }

        return opsFound;
    }

    /**
     * Common code to generate a <method> element from an operation.
     *
     * @param doc
     * @param endpointName
     * @param axisOperation
     * @return generated element
     * @throws DOMException
     */
    private Element generateMethodElement(Document doc, String endpointName,
                                          AxisOperation axisOperation) throws DOMException {
        Element methodElement;
        List soapHeaderInputParameterList = new ArrayList();
        List soapHeaderOutputParameterList = new ArrayList();
        methodElement = doc.createElement("method");
        String localPart = axisOperation.getName().getLocalPart();

        addAttribute(doc, "name", JavaUtils.xmlNameToJava(localPart), methodElement);
        addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
        addAttribute(doc, "style", (String) getBindingPropertyFromOperation(
                WSDLConstants.WSDL_1_1_STYLE, axisOperation.getName()), methodElement);
        addAttribute(doc, "http_location", (String) getBindingPropertyFromOperation(
                WSDL2Constants.ATTR_WHTTP_LOCATION, axisOperation.getName()), methodElement);
        addAttribute(doc, "dbsupportname",
                endpointName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                methodElement);
        String messageExchangePattern = axisOperation.getMessageExchangePattern();
        addAttribute(doc, "mep", Utils.getAxisSpecifMEPConstant(messageExchangePattern) + "",
                methodElement);
        addAttribute(doc, "mepURI", messageExchangePattern, methodElement);

        // check for this operation to be handled directly by databinding code generation
        Parameter dbmethname = axisOperation.getParameter(Constants.DATABINDING_GENERATED_RECEIVER);
        if (dbmethname != null) {
            addAttribute(doc, "usedbmethod", (String) dbmethname.getValue(), methodElement);
        }
        Parameter dbgenimpl =
                axisOperation.getParameter(Constants.DATABINDING_GENERATED_IMPLEMENTATION);
        if (dbgenimpl != null && Boolean.TRUE.equals(dbgenimpl.getValue())) {
            addAttribute(doc, "usdbimpl", "true", methodElement);
        } else {
            addAttribute(doc, "usdbimpl", "false", methodElement);
        }

        addSOAPAction(doc, methodElement, axisOperation.getName());
        addOutputAndFaultActions(doc, methodElement, axisOperation);
        addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
//        addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

        /*
         * Setting the policy of the operation
         */
        Policy policy = axisOperation.getPolicyInclude().getPolicy();
        if (policy != null) {
            try {
                addAttribute(doc, "policy",
                        PolicyUtil.getSafeString(PolicyUtil.policyComponentToString(policy)),
                        methodElement);
            } catch (Exception ex) {
                throw new RuntimeException("can't serialize the policy ..");
            }
        }


        if (WSDLUtil.isInputPresentForMEP(messageExchangePattern)) {
            methodElement.appendChild(getInputElement(doc,
                    axisOperation, soapHeaderInputParameterList));
        }
        if (WSDLUtil.isOutputPresentForMEP(messageExchangePattern)) {
            methodElement.appendChild(getOutputElement(doc,
                    axisOperation, soapHeaderOutputParameterList));
        }
        methodElement.appendChild(getFaultElement(doc,
                axisOperation));

        setTransferCoding(axisOperation, methodElement, doc);

        String property = (String) getBindingPropertyFromOperation(
                WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                axisOperation.getName());
        if (property != null) {
            methodElement.appendChild(generateOptionParamComponent(doc,
                   "org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR",
                   "\"" + property + "\""));
        }

        String mep = (String) getBindingPropertyFromOperation(WSDL2Constants.ATTR_WSOAP_MEP,
                                                              axisOperation.getName());

        String bindingType = axisBinding.getType();

        if (WSDL2Constants.URI_WSOAP_MEP.equalsIgnoreCase(mep)) {
            methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.Constants.Configuration.ENABLE_REST",
                       "true"));
            methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.Constants.Configuration.HTTP_METHOD",
                       "\"" +
                               org.apache.axis2.Constants.Configuration
                                       .HTTP_METHOD_GET +
                               "\""));
            methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.Constants.Configuration.CONTENT_TYPE",
                       "\"" +
                               org.apache.axis2.transport.http.HTTPConstants.MEDIA_TYPE_X_WWW_FORM +
                               "\""));
        } else if (bindingType != null && bindingType.equals(WSDL2Constants.URI_WSDL2_HTTP)) {

            methodElement.appendChild(generateOptionParamComponent(doc,
                               "org.apache.axis2.Constants.Configuration.ENABLE_REST",
                               "true"));

            property = (String) getBindingPropertyFromOperation(WSDL2Constants.ATTR_WHTTP_METHOD,
                                                                axisOperation.getName());
            if (property != null) {
                methodElement.appendChild(generateOptionParamComponent(doc,
                           "org.apache.axis2.Constants.Configuration.HTTP_METHOD",
                           "\"" + property + "\""));
            }

            // If there is no WHTTP_METHOD defined then we better compute the default value which is get if the operation
            // is wsdlx:safe or post otherwise
            else if (!WSDL2Constants.URI_WSOAP_MEP.equalsIgnoreCase(mep)) {
                Parameter safe = axisOperation.getParameter(WSDL2Constants.ATTR_WSDLX_SAFE);
                if (safe != null) {
                    if (((Boolean) safe.getValue()).booleanValue()) {
                        methodElement.appendChild(generateOptionParamComponent(doc,
                                       "org.apache.axis2.Constants.Configuration.HTTP_METHOD",
                                       "\"" +
                                               org.apache.axis2.Constants.Configuration
                                                       .HTTP_METHOD_GET +
                                               "\""));
                    } else {
                        methodElement.appendChild(generateOptionParamComponent(doc,
                                   "org.apache.axis2.Constants.Configuration.HTTP_METHOD",
                                   "\"" +
                                           org.apache.axis2.Constants.Configuration
                                                   .HTTP_METHOD_POST +
                                           "\""));
                    }
                }
            }

            Boolean value = (Boolean) getBindingPropertyFromOperation(
                    WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED,
                    axisOperation.getName());
            if (value != null) {
                methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED",
                       "\"" + value.booleanValue() +
                               "\""));
            }

            property = (String) getBindingPropertyFromOperation(WSDL2Constants.ATTR_WHTTP_CODE,
                                                                axisOperation.getName());
            if (property != null) {
                methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_CODE",
                       "\"" + property + "\""));
            }

            property = (String) getBindingPropertyFromOperation(
                    WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                    axisOperation.getName());
            if (property != null) {
                methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.Constants.Configuration.CONTENT_TYPE",
                       "\"" + property + "\""));
            }

        }
        return methodElement;
    }

    /**
     * Set the transfer coding property of the input message
     *
     * @param axisOperation
     * @param methodElement
     * @param doc
     */
    private void setTransferCoding(AxisOperation axisOperation, Element methodElement,
                                   Document doc) {
        // Add a optionParam element which holds the value of transferCoding
        String transferCoding =
                (String) getBindingPropertyFromMessage(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                                       axisOperation.getName(),
                                                       WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
        if (!"".equals(transferCoding)) {
            if ("gzip".equals(transferCoding) || "compress".equals(transferCoding)) {
                methodElement.appendChild(generateOptionParamComponent(doc,
                       "org.apache.axis2.transport.http.HTTPConstants.MC_GZIP_REQUEST",
                       "true"));
            }
        }
    }

    /**
     * Set thttp header properties needed for the stub
     *
     * @param axisOperation
     * @param methodElement
     * @param doc
     */
    private void setHttpHeaderOptions(AxisOperation axisOperation, Element methodElement,
                                      Document doc) {
        // Add a optionParam elements here

    }

    // ==================================================================
    //                   Util Methods
    // ==================================================================

    protected Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
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
    protected void addAttribute(Document document, String AttribName, String attribValue,
                                Element element) {
        XSLTUtils.addAttribute(document, AttribName, attribValue, element);
    }

    /**
     * @param doc
     * @param rootElement
     */
    protected void fillSyncAttributes(Document doc, Element rootElement) {
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
    protected void debugLogDocument(String description, Document doc) {
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
        if (dir2 != null && !"".equals(dir2)) {
            outputDir = new File(outputDir, dir2);
        }

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
     * @param qName
     */
    protected void addSOAPAction(Document doc, Element rootElement, QName qName) {
        addAttribute(doc, "soapaction",
                (String) getBindingPropertyFromOperation(WSDL2Constants.ATTR_WSOAP_ACTION, qName),
                rootElement);
    }

    /**
     * Adds the output and fault actions
     *
     * @param doc
     * @param methodElement
     * @param operation
     */
    private void addOutputAndFaultActions(Document doc, Element methodElement,
                                          AxisOperation operation) {
        String outputAction = operation.getOutputAction();
        if (outputAction != null) {
            Element outputActionElt =
                    doc.createElement(org.apache.axis2.Constants.OUTPUT_ACTION_MAPPING);
            outputActionElt.setAttribute(AddressingConstants.WSA_ACTION, outputAction);
            methodElement.appendChild(outputActionElt);
        }

        String[] faultActionNames = operation.getFaultActionNames();
        if (faultActionNames != null) {
            for (int i = 0; i < faultActionNames.length; i++) {
                Element faultActionElt =
                        doc.createElement(org.apache.axis2.Constants.FAULT_ACTION_MAPPING);
                faultActionElt.setAttribute(org.apache.axis2.Constants.FAULT_ACTION_NAME,
                        faultActionNames[i]);
                faultActionElt.setAttribute(AddressingConstants.WSA_ACTION,
                        operation.getFaultAction(faultActionNames[i]));
                methodElement.appendChild(faultActionElt);
            }
        }
    }

    /**
     * populate the header parameters
     *
     * @param soapHeaderParameterQNameList
     * @param axisOperation
     * @param input
     */
    protected void addHeaderOperations(List soapHeaderParameterQNameList,
                                       AxisOperation axisOperation,
                                       boolean input) {
        ArrayList headerparamList = new ArrayList();
        String MEP = axisOperation.getMessageExchangePattern();
        if (input) {
            if (WSDLUtil.isInputPresentForMEP(MEP)) {

                headerparamList = (ArrayList) getBindingPropertyFromMessage(
                        WSDL2Constants.ATTR_WSOAP_HEADER, axisOperation.getName(),
                        WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);

            }
        } else {
            if (WSDLUtil.isOutputPresentForMEP(MEP)) {
                headerparamList = (ArrayList) getBindingPropertyFromMessage(
                        WSDL2Constants.ATTR_WSOAP_HEADER, axisOperation.getName(),
                        WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
            }
        }
        if (headerparamList != null) {
            for (Iterator iterator = headerparamList.iterator(); iterator.hasNext();) {
                SOAPHeaderMessage header = (SOAPHeaderMessage) iterator.next();
                soapHeaderParameterQNameList.add(header);
            }
        }

    }

    /**
     * populate the header parameters to faults
     *
     * @param soapHeaderParameterQNameList
     * @param axisOperation
     */
    protected void addHeaderOperationsToFault(List soapHeaderParameterQNameList, AxisOperation axisOperation) {
        ArrayList headerparamList = new ArrayList();
        ArrayList faultMessages = axisOperation.getFaultMessages();
        Iterator iter = faultMessages.iterator();
        while (iter.hasNext()) {
            AxisMessage axisFaultMessage = (AxisMessage) iter.next();
            headerparamList.addAll((ArrayList) getBindingPropertyFromMessageFault(
                    WSDL2Constants.ATTR_WSOAP_HEADER, axisOperation.getName(),
                    axisFaultMessage.getName()));
        }

        if (headerparamList != null) {
            for (Iterator iterator = headerparamList.iterator(); iterator.hasNext();) {
                SOAPHeaderMessage header = (SOAPHeaderMessage) iterator.next();
                soapHeaderParameterQNameList.add(header.getElement());
            }
        }
    }


    /**
     * Get the input element
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     * @return DOM element
     */
    protected Element getInputElement(Document doc, AxisOperation operation,
                                      List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        String mep = operation.getMessageExchangePattern();

        if (WSDLUtil.isInputPresentForMEP(mep)) {

            /*
             * Setting the input message policy as an attribute of the input element.
             */
            AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inputMessage != null) {
                PolicyInclude policyInclude = inputMessage.getPolicyInclude();
                Policy policy = policyInclude.getPolicy();

                if (policy != null) {
                    policy = (Policy) policy.normalize(policyInclude.getPolicyRegistry(), false);
                    try {
                        String policyString = PolicyUtil.policyComponentToString(policy);
                        policyString = PolicyUtil.getSafeString(policyString);
                        addAttribute(doc, "policy", policyString, inputElt);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            Element[] param = getInputParamElement(doc, operation);
            for (int i = 0; i < param.length; i++) {
                inputElt.appendChild(param[i]);
            }

            List parameterElementList = getParameterElementList(doc, headerParameterQNameList,
                                                                WSDLConstants.SOAP_HEADER);
            parameterElementList.addAll(getParameterElementListForHttpHeader(doc,
                                             (ArrayList) getBindingPropertyFromMessage(
                                                     WSDL2Constants.ATTR_WHTTP_HEADER,
                                                     operation.getName(),
                                                     WSDLConstants.WSDL_MESSAGE_DIRECTION_IN),
                                             WSDLConstants.HTTP_HEADER));
            parameterElementList.addAll(getParameterElementListForSOAPModules(doc,
                                      (ArrayList) getBindingPropertyFromMessage(
                                              WSDL2Constants.ATTR_WSOAP_MODULE,
                                              operation.getName(),
                                              WSDLConstants.WSDL_MESSAGE_DIRECTION_IN)));

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
    protected Element getOutputElement(Document doc, AxisOperation operation,
                                       List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        String mep = operation.getMessageExchangePattern();


        if (WSDLUtil.isOutputPresentForMEP(mep)) {

            AxisMessage outMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            if (outMessage != null) {
                PolicyInclude policyInclude = outMessage.getPolicyInclude();
                Policy policy = policyInclude.getPolicy();

                if (policy != null) {
                    policy = (Policy) policy.normalize(policyInclude.getPolicyRegistry(), false);

                    try {
                        String policyString = PolicyUtil.policyComponentToString(policy);
                        policyString = PolicyUtil.getSafeString(policyString);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            Element param = getOutputParamElement(doc, operation);

            if (param != null) {
                outputElt.appendChild(param);
            }

            List outputElementList = getParameterElementList(doc, headerParameterQNameList,
                                                             WSDLConstants.SOAP_HEADER);
            outputElementList.addAll(getParameterElementListForHttpHeader(doc,
                                                                          (ArrayList) getBindingPropertyFromMessage(
                                                                                  WSDL2Constants.ATTR_WHTTP_HEADER,
                                                                                  operation.getName(),
                                                                                  WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT),
                                                                          WSDLConstants.HTTP_HEADER));

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
    protected Element[] getFaultParamElements(Document doc, AxisOperation operation) {
        ArrayList params = new ArrayList();
        ArrayList faultMessages = operation.getFaultMessages();

        if (faultMessages != null && !faultMessages.isEmpty()) {
            Element paramElement;
            AxisMessage msg;
            for (int i = 0; i < faultMessages.size(); i++) {
                paramElement = doc.createElement("param");
                msg = (AxisMessage) faultMessages.get(i);

                if (msg.getElementQName() == null) {
                    throw new RuntimeException("Element QName is null for " + msg.getName() + "!");
                }

                //as for the name of a fault, we generate an exception
                addAttribute(doc, "name",
                        (String) fullyQualifiedFaultClassNameMap.get(msg.getElementQName()),
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

                //add the short name
                addShortType(paramElement, typeMapping);

                String attribValue = (String) instantiatableMessageClassNames.
                        get(msg.getElementQName());
                addAttribute(doc, "instantiatableType",
                        attribValue == null ? "" : attribValue,
                        paramElement);

                // add an extra attribute to say whether the type mapping is
                // the default
                if (mapper.getDefaultMappingName().equals(typeMapping)) {
                    addAttribute(doc, "default", "yes", paramElement);
                }
                addAttribute(doc, "value", getParamInitializer(typeMapping),
                        paramElement);

                Iterator iter = msg.getExtensibilityAttributes().iterator();
                while (iter.hasNext()) {
                    // process extensibility attributes
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
    protected Element[] getInputParamElement(Document doc, AxisOperation operation) {

        AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        List paramElementList = new ArrayList();
        if (inputMessage != null) {

            // This is the  wrapped component - add the type mapping
            Element mainParameter = generateParamComponent(doc,
                    this.mapper.getParameterName(
                            inputMessage.getElementQName()),
                    this.mapper.getTypeMappingName(
                            inputMessage.getElementQName()),
                    operation.getName(),
                    inputMessage.getElementQName()
            );

            paramElementList.add(mainParameter);

            //if the unwrapping or backWordCompatibility flag is on then we have to
            //put the element complex type if it exits
            if (this.codeGenConfiguration.isBackwordCompatibilityMode() ||
                    !this.codeGenConfiguration.isParametersWrapped()) {
                if (inputMessage.getParameter(Constants.COMPLEX_TYPE) != null) {
                    Parameter parameter = inputMessage.getParameter(Constants.COMPLEX_TYPE);
                    addAttribute(doc, "complextype", (String) parameter.getValue(), mainParameter);
                }
            }

            // this message has been unwrapped - find the correct references of the
            // the message by looking at the unwrapped details object and attach the
            // needed parameters inside main parameter element
            if (inputMessage.getParameter(Constants.UNWRAPPED_KEY) != null) {

                //we have this unwrapped earlier. get the info holder
                //and then look at the parameters
                Parameter detailsParameter =
                        inputMessage.getParameter(Constants.UNWRAPPED_DETAILS);
                MessagePartInformationHolder infoHolder =
                        (MessagePartInformationHolder) detailsParameter.getValue();
                List partsList = infoHolder.getPartsList();

                //populate the parts list - this list is needed to generate multiple
                //parameters in the signatures
                for (int i = 0; i < partsList.size(); i++) {
                    QName qName = (QName) partsList.get(i);
                    mainParameter.appendChild(generateParamComponent(doc,
                            this.mapper.getParameterName(qName),
                            this.mapper.getTypeMappingName(qName),
                            operation.getName(),
                            qName,
                            qName.getLocalPart(),
                            (this.mapper.getTypeMappingStatus(qName) != null),
                            Constants.ARRAY_TYPE.equals(this.mapper.getTypeMappingStatus(qName)))
                    );
                }


            }


        }

        return (Element[]) paramElementList.toArray(
                new Element[paramElementList.size()]);
    }

    /**
     * A convenient method for the generating the parameter
     * element
     *
     * @param doc
     * @param paramName
     * @param paramType
     * @return DOM Element
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName operationName,
                                             QName paramQName) {
        return generateParamComponent(doc,paramName,paramType,operationName, paramQName, null,false,false);
    }

    /**
     * A convenient method for the generating the parameter
     * element
     *
     * @param doc
     * @param paramName
     * @param paramType
     * @return DOM Element
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName paramQName) {
        return generateParamComponent(doc,paramName,paramType,null, paramQName, null,false,false);
    }

    /**
     * A convenient method for the generating optionParam components
     *
     * @param doc
     * @param name
     * @param value
     * @return Element
     */
    protected Element generateOptionParamComponent(Document doc, String name, String value) {

        Element optionParamElement = doc.createElement("optionParam");
        addAttribute(doc, "name", name, optionParamElement);
        addAttribute(doc, "value", value, optionParamElement);
        return optionParamElement;
    }

    /**
     * A convenient method for the generating the parameter
     * element
     *
     * @param doc
     * @param paramName
     * @param paramType
     * @param opName
     * @param paramName
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName opName,
                                             QName paramQName,
                                             String partName,
                                             boolean isPrimitive,
                                             boolean isArray) {
        Element paramElement = doc.createElement("param");
        addAttribute(doc, "name",
                paramName, paramElement);

        addAttribute(doc, "type",
                (paramType == null) ? "" : paramType,
                paramElement);

        //adds the short type
        addShortType(paramElement, paramType);

        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(paramType)) {
            addAttribute(doc, "default", "yes", paramElement);
        }
        addAttribute(doc, "value", getParamInitializer(paramType), paramElement);
        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);

        //if the opName and partName are present , add them
        if (opName != null) {
            addAttribute(doc, "opname", opName.getLocalPart(), paramElement);
        }

        if (paramQName!=null){
            Element qNameElement = doc.createElement("qname");
            addAttribute(doc,"nsuri", paramQName.getNamespaceURI(), qNameElement);
            addAttribute(doc,"localname", paramQName.getLocalPart(), qNameElement);
            paramElement.appendChild(qNameElement);
        }
        if (partName!= null){
            addAttribute(doc,"partname",
                    JavaUtils.capitalizeFirstChar(partName),
                    paramElement);
        }

        if (isPrimitive) {
            addAttribute(doc, "primitive", "yes", paramElement);
        }

        if (isArray) {
            addAttribute(doc, "array", "yes", paramElement);
        }

        return paramElement;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    protected Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element paramElement = doc.createElement("param");
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

        addAttribute(doc, "name", parameterName, paramElement);
        addAttribute(doc, "type", typeMappingStr, paramElement);

        //adds the short type
        addShortType(paramElement, typeMappingStr);

        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(typeMappingStr)) {
            addAttribute(doc, "default", "yes", paramElement);
        }

        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);
        addAttribute(doc, "opname", operation.getName().getLocalPart(), paramElement);

        //if the unwrapping or backWordCompatibility flag is on then we have to
        //put the element complex type if it exits
        if (this.codeGenConfiguration.isBackwordCompatibilityMode() ||
                !this.codeGenConfiguration.isParametersWrapped()) {
            if (outputMessage.getParameter(Constants.COMPLEX_TYPE) != null) {
                Parameter parameter = outputMessage.getParameter(Constants.COMPLEX_TYPE);
                addAttribute(doc, "complextype", (String) parameter.getValue(), paramElement);
            }
        }

        // this message has been unwrapped - find the correct references of the
        // the message by looking at the unwrapped details object and attach the
        // needed parameters inside main parameter element
        if (outputMessage.getParameter(Constants.UNWRAPPED_KEY) != null) {

            //we have this unwrapped earlier. get the info holder
            //and then look at the parameters
            Parameter detailsParameter =
                    outputMessage.getParameter(Constants.UNWRAPPED_DETAILS);
            MessagePartInformationHolder infoHolder =
                    (MessagePartInformationHolder) detailsParameter.getValue();
            List partsList = infoHolder.getPartsList();

            //populate the parts list - this list is needed to generate multiple
            //parameters in the signatures
            // in out put params we only intersted if there is only one parameter
            // otherwise we can not unwrap it.
            if (partsList.size() == 1){
                QName qName = (QName) partsList.get(0);
                paramElement.appendChild(generateParamComponent(doc,
                    this.mapper.getParameterName(qName),
                    this.mapper.getTypeMappingName(qName),
                    operation.getName(),
                    qName,
                    qName.getLocalPart(),
                    (this.mapper.getTypeMappingStatus(qName) != null),
                    Constants.ARRAY_TYPE.equals(this.mapper.getTypeMappingStatus(qName)))
                );
            }

        }

        QName paramQName = outputMessage.getElementQName();
        if (paramQName!=null){
            Element qNameElement = doc.createElement("qname");
            addAttribute(doc,"nsuri", paramQName.getNamespaceURI(), qNameElement);
            addAttribute(doc,"localname", paramQName.getLocalPart(), qNameElement);
            paramElement.appendChild(qNameElement);
        }

        return paramElement;
    }

    /**
     * @param paramType
     */
    protected String getParamInitializer(String paramType) {

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
    protected List getParameterElementList(Document doc, List parameters, String location) {
        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                SOAPHeaderMessage header = (SOAPHeaderMessage) parameters.get(i);
                QName name = header.getElement();

                addAttribute(doc, "name", this.mapper.getParameterName(name), param);

                String typeMapping = this.mapper.getTypeMappingName(name);
                String typeMappingStr = (typeMapping == null)
                        ? ""
                        : typeMapping;

                addAttribute(doc, "type", typeMappingStr, param);
                addAttribute(doc, "location", location, param);
                if (header.isMustUnderstand()){
                   addAttribute(doc, "mustUnderstand","true", param); 
                }
                parameterElementList.add(param);
            }
        }

        return parameterElementList;
    }


    protected List getParameterElementListForHttpHeader(Document doc, List parameters,
                                                        String location) {


        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                HTTPHeaderMessage httpHeaderMessage = (HTTPHeaderMessage) parameters.get(i);
                QName qName = httpHeaderMessage.getqName();
                String name = httpHeaderMessage.getName();

                // use name as the name attribute of the parameter
                addAttribute(doc, "name", JavaUtils.xmlNameToJavaIdentifier(name), param);
                // header name is to set the header value
                addAttribute(doc, "headername", name, param);

                String typeMapping = this.mapper.getTypeMappingName(qName);
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

    protected List getParameterElementListForSOAPModules(Document doc, List parameters) {

        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                SOAPModuleMessage soapModuleMessage = (SOAPModuleMessage) parameters.get(i);

                // header name is to set the header value
                addAttribute(doc, "uri", soapModuleMessage.getUri(), param);

                addAttribute(doc, "location", "wsoap_module", param);
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
    protected Element addElement(Document document, String eltName, String eltValue,
                                 Element element) {
        Element elt = XSLTUtils.addChildElement(document, eltName, element);
        if (eltValue != null) {
            elt.appendChild(document.createTextNode(eltValue));
        }
        return elt;
    }


}
