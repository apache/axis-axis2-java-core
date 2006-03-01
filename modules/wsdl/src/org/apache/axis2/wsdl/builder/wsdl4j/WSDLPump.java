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

package org.apache.axis2.wsdl.builder.wsdl4j;

import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.wsdl.builder.WSDLComponentFactory;
import org.apache.axis2.namespace.Constants;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.Component;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingFault;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLFaultReference;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.DefaultExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.ExtensionFactory;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

public class WSDLPump {

    private static final String XMLSCHEMA_NAMESPACE_URI = Constants.URI_2001_SCHEMA_XSD;
    private static final String XMLSCHEMA_NAMESPACE_PREFIX = "xs";
    private static final String XML_SCHEMA_LOCAL_NAME = "schema";
    private static final String XML_SCHEMA_SEQUENCE_LOCAL_NAME = "sequence";
    private static final String XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME = "complexType";
    private static final String XML_SCHEMA_ELEMENT_LOCAL_NAME = "element";
    private static final String XML_SCHEMA_IMPORT_LOCAL_NAME = "import";

    private static final String XSD_NAME = "name";
    private static final String XSD_TARGETNAMESPACE = "targetNamespace";
    private static final String XMLNS_AXIS2WRAPPED = "xmlns:axis2wrapped";
    private static final String AXIS2WRAPPED = "axis2wrapped";
    private static final String XSD_TYPE = "type";
    private static final String XSD_REF = "ref";
    private static final String BOUND_INTERFACE_NAME = "BoundInterface";


    private static int nsCount=0;

    private WSDLDescription womDefinition;

    private Definition wsdl4jParsedDefinition;

    private WSDLComponentFactory wsdlComponentFactory;

    private Map declaredNameSpaces=null;

    private Map resolvedRpcWrappedElementMap = new HashMap();
    private static final String XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    private static final String XSD_UNQUALIFIED = "unqualified";

    public WSDLPump(WSDLDescription womDefinition,
                    Definition wsdl4jParsedDefinition) {
        this(womDefinition, wsdl4jParsedDefinition, womDefinition);
    }

    public WSDLPump(WSDLDescription womDefinition,
                    Definition wsdl4jParsedDefinition,
                    WSDLComponentFactory wsdlComponentFactory) {
        this.womDefinition = womDefinition;
        this.wsdl4jParsedDefinition = wsdl4jParsedDefinition;
        this.wsdlComponentFactory = wsdlComponentFactory;
    }

    public void pump() {
        if (null != this.wsdl4jParsedDefinition && null != this.womDefinition) {
            this.populateDefinition(this.womDefinition,
                    this.wsdl4jParsedDefinition);
        } else {
            throw new WSDLProcessingException("Properties not set properly");
        }

    }

    private void populateDefinition(WSDLDescription wsdlDefinition,
                                    Definition wsdl4JDefinition) {
        //Go through the WSDL4J Definition and pump it to the WOM
        wsdlDefinition.setWSDL1DefinitionName(wsdl4JDefinition.getQName());
        wsdlDefinition
                .setTargetNameSpace(wsdl4JDefinition.getTargetNamespace());
        wsdlDefinition.setNamespaces(wsdl4JDefinition.getNamespaces());
        this.copyExtensibleElements(
                wsdl4JDefinition.getExtensibilityElements(), wsdlDefinition, null);

        //get the namespace map
        this.declaredNameSpaces = wsdl4JDefinition.getNamespaces();

        /////////////////////////////////////////////////////////////////// //
        // Order of the following items shouldn't be changed unless you     //
        // really know what you are doing. Reason being the components that //
        // are copied(pumped) towards the end depend on the components that //
        // has already being pumped. Following Lists some of the            //
        // dependencies.                                                    //
        //  1) The Binding refers to the Interface                          //
        //  2) The Endpoint refers to the Bindings                          //
        // ....                                                             //
        //																   	//
        //////////////////////////////////////////////////////////////////////

        //////////////////(0) process the imports ///////////////////////////
        // There can be types that are imported. Check the imports and
        // These schemas are needed for code generation
        processImports(wsdl4JDefinition);

        //////////////////(1.1)First Copy the Types/////////////////////////////
        //Types may get changed inside the Operation pumping.

        Types wsdl4jTypes = wsdl4JDefinition.getTypes();
        WSDLTypes wsdlTypes = this.wsdlComponentFactory.createTypes();
        this.womDefinition.setTypes(wsdlTypes);

        if (null != wsdl4jTypes) {
            this.copyExtensibleElements(wsdl4jTypes.getExtensibilityElements(),
                    wsdlTypes, null);
        }

        //////////////////(1.2) /////////////////////////////
        // create new Schema extensions element for wrapping
        Element[] schemaElements = generateWrapperSchema(wsdl4JDefinition);
        if (schemaElements!=null && schemaElements.length>0){
            for (int i = 0; i < schemaElements.length; i++) {
                Element schemaElement = schemaElements[i];
                if (schemaElement!=null){
                    ExtensionFactory extensionFactory = wsdlComponentFactory.createExtensionFactory();
                    org.apache.wsdl.extensions.Schema schemaExtensibilityElement = (org.apache.wsdl.extensions.Schema) extensionFactory.getExtensionElement(
                            ExtensionConstants.SCHEMA);
                    wsdlTypes.addExtensibilityElement(schemaExtensibilityElement);
                    schemaExtensibilityElement.setElement(schemaElement);
                }
            }
        }

        ////////////////////// (1.3) ////////////////////////////
        // get all the schema elements (as DOM elements) and read them
        // into  commons XMLSchema objects. This comes in handy when we
        // need to populate the message references

        // this is temporarily commented until the xmlschema baseuri handling is fixed

//        List typeExtensibilityElements = wsdlTypes.getExtensibilityElements();
//        XmlSchemaCollection commonsSchemaReader = new XmlSchemaCollection();
//        Map namespacesMap = wsdlDefinition.getNamespaces();
//        String[] prefixes = (String[])namespacesMap.keySet().toArray(new String[namespacesMap.size()]);
//        for (int i = 0; i < prefixes.length; i++) {
//            commonsSchemaReader.mapNamespace(prefixes[i],(String)namespacesMap.get(prefixes[i]));
//        }
//
//        XmlSchema schema;
//        for (int i = 0; i < typeExtensibilityElements.size(); i++) {
//            Object extElement =  typeExtensibilityElements.get(i);
//            if (extElement instanceof org.apache.wsdl.extensions.Schema){
//                org.apache.wsdl.extensions.Schema schemaExtesnsibilityElement = ((org.apache.wsdl.extensions.Schema) extElement);
//                schema = commonsSchemaReader.read(schemaExtesnsibilityElement.getElement());
//                //attach this schema in the extensibility element
//                schemaExtesnsibilityElement.setSchema(schema);
//            }
//        }



        ///////////////////(2)Copy the Interfaces///////////////////////////
        //copy the Interfaces: Get the PortTypes from WSDL4J parse OM and
        // copy it to the  WOM's WSDLInterface Components

        Iterator portTypeIterator = wsdl4JDefinition.getPortTypes().values()
                .iterator();
        WSDLInterface wsdlInterface;
        PortType portType;
        while (portTypeIterator.hasNext()) {
            wsdlInterface = this.wsdlComponentFactory.createInterface();
            portType = (PortType) portTypeIterator.next();
            this.populateInterfaces(wsdlInterface, portType,womDefinition);
            this.copyExtensibilityAttribute(portType.getExtensionAttributes(),
                    wsdlInterface);
            wsdlDefinition.addInterface(wsdlInterface);
        }

        //////////////////(3)Copy the Bindings///////////////////////
        //pump the Bindings: Get the Bindings map from WSDL4J and create a new
        // map of WSDLBinding elements. At this point we need to do some extra work since there
        //can be header parameters

        Iterator bindingIterator = wsdl4JDefinition.getBindings().values()
                .iterator();
        WSDLBinding wsdlBinding;
        Binding wsdl4jBinding;
        while (bindingIterator.hasNext()) {
            wsdlBinding = this.wsdlComponentFactory.createBinding();
            wsdl4jBinding = (Binding) bindingIterator.next();
            this.populateBindings(wsdlBinding, wsdl4jBinding, wsdl4JDefinition);
            this.copyExtensibleElements(
                    wsdl4jBinding.getExtensibilityElements(),
                    wsdlBinding, null);
            wsdlDefinition.addBinding(wsdlBinding);

        }

        ///////////////////(4)Copy the Services///////////////////////////////

        Iterator serviceIterator = wsdl4JDefinition.getServices().values()
                .iterator();
        WSDLService wsdlService;
        Service wsdl4jService;
        while (serviceIterator.hasNext()) {
            wsdlService = this.wsdlComponentFactory.createService();
            wsdl4jService = (Service) serviceIterator.next();
            this.populateServices(wsdlService, wsdl4jService);
            this.copyExtensibleElements(
                    wsdl4jService.getExtensibilityElements(),
                    wsdlService, null);
            wsdlDefinition.addService(wsdlService);
        }

    }

    //////////////////////////////////////////////////////////////////////////////
    //////////////////////// Top level Components Copying ////////////////////////

    /**
     * Simply Copy information.
     *
     * @param wsdlInterface
     * @param wsdl4jPortType
     */
    // FIXME Evaluate a way of injecting features and priperties with a general
    // formatted input
    private void populateInterfaces(WSDLInterface wsdlInterface,
                                    PortType wsdl4jPortType,
                                    WSDLDescription desc) {

        //Copy the Attribute information items
        //Copied with the Same QName so it will require no Query in Binding
        //Coping.
        wsdlInterface.setName(wsdl4jPortType.getQName());
        Iterator wsdl4JOperationsIterator =
                wsdl4jPortType.getOperations().iterator();
        WSDLOperation wsdloperation;
        Operation wsdl4jOperation;

        while (wsdl4JOperationsIterator.hasNext()) {
            wsdloperation = this.wsdlComponentFactory.createOperation();
            wsdl4jOperation = (Operation) wsdl4JOperationsIterator.next();

            this.populateOperations(wsdloperation,
                    wsdl4jOperation,
                    wsdl4jPortType.getQName().getNamespaceURI(),
                    desc.getTypes());

            this.copyExtensibleElements(
                    wsdl4jOperation.getExtensibilityElements(), wsdloperation, null);

            wsdlInterface.setOperation(wsdloperation);
        }
    }

    /**
     * The intention of this procedure is to process the imports.
     * When processing the imports the imported documents will be
     * populating the items in the main document recursivley
     * @param wsdl4JDefinition
     */
    private void processImports(Definition wsdl4JDefinition){
        Map wsdlImports = wsdl4JDefinition.getImports();

        if (null != wsdlImports && !wsdlImports.isEmpty()){
            Collection importsCollection = wsdlImports.values();
            for (Iterator iterator = importsCollection.iterator(); iterator.hasNext();) {
                Vector values = (Vector)iterator.next();
                for (int i = 0; i < values.size(); i++) {
                    Import wsdlImport = (Import)values.elementAt(i);

                    if (wsdlImport.getDefinition()!=null){
                        Definition importedDef = wsdlImport.getDefinition();
                        if (importedDef!=null){
                            processImports(importedDef);

                            //copy types
                            Types t = importedDef.getTypes();
                            List typesList = t.getExtensibilityElements();
                            for (int j = 0; j < typesList.size(); j++) {
                                Types types = wsdl4JDefinition.getTypes();
                                if(types == null){
                                    types = wsdl4JDefinition.createTypes();
                                    wsdl4JDefinition.setTypes(types);
                                }
                                types.addExtensibilityElement(
                                        (ExtensibilityElement)typesList.get(j));

                            }

                            //add messages
                            Map messagesMap = importedDef.getMessages();
                            wsdl4JDefinition.getMessages().putAll(messagesMap);

                            //add portypes
                            Map porttypeMap = importedDef.getPortTypes();
                            wsdl4JDefinition.getPortTypes().putAll(porttypeMap);

                        }

                    }
                }
            }
        }
    }



    /**
     *
     * Finds whether a given message is wrappable
     * @return
     */
    private boolean findWrapppable(Message message) {

// ********************************************************************************************
// Note
// We will not use the binding to set the wrappable/unwrappable state here. instead we'll look at the
// Messages for the following features
// 1. Messages with multiple parts -> We have no choice but to wrap
// 2. Messages with one part having a type attribute -> Again we have no choice but to wrap

// ********************************************************************************************
        Map partsMap = message.getParts();
        Iterator parts=partsMap.values().iterator();
        boolean wrappable= partsMap.size()>1;
        Part part;
        while (!wrappable && parts.hasNext()) {
            part = (Part) parts.next();
            wrappable = (part.getTypeName() != null) || wrappable;
        }


        return wrappable;
    }

    /**
     * Pre Condition: The Interface Components must be copied by now.
     */
    private void populateBindings(WSDLBinding wsdlBinding,
                                  Binding wsdl4JBinding, Definition wsdl4jDefinition) {

        //Copy attributes
        wsdlBinding.setName(wsdl4JBinding.getQName());
        QName interfaceName = wsdl4JBinding.getPortType().getQName();
        WSDLInterface wsdlInterface =
                this.womDefinition.getInterface(interfaceName);

        //FIXME Do We need this eventually???
        if (null == wsdlInterface)
            throw new WSDLProcessingException("Interface/PortType not found for the Binding :"
                    + wsdlBinding.getName());
        wsdlBinding.setBoundInterface(wsdlInterface);
        Iterator bindingoperationsIterator =
                wsdl4JBinding.getBindingOperations().iterator();
        WSDLBindingOperation wsdlBindingOperation;
        BindingOperation wsdl4jBindingOperation;
        while (bindingoperationsIterator.hasNext()) {
            wsdlBindingOperation =
                    this.wsdlComponentFactory.createWSDLBindingOperation();
            wsdl4jBindingOperation =
                    (BindingOperation) bindingoperationsIterator.next();
            this.populateBindingOperation(wsdlBindingOperation,
                    wsdl4jBindingOperation,
                    wsdl4JBinding.getQName().getNamespaceURI(), wsdl4jDefinition);
            wsdlBindingOperation.setOperation(
                    wsdlInterface.getOperation(
                            wsdl4jBindingOperation.getOperation().getName()));
            copyExtensibleElements(
                    wsdl4jBindingOperation.getExtensibilityElements(),
                    wsdlBindingOperation, wsdl4jDefinition);
            wsdlBinding.addBindingOperation(wsdlBindingOperation);
        }

    }

    public void populateServices(WSDLService wsdlService,
                                 Service wsdl4jService) {
        wsdlService.setName(wsdl4jService.getQName());
        Iterator wsdl4jportsIterator =
                wsdl4jService.getPorts().values().iterator();
        wsdlService.setServiceInterface(this.getBoundInterface(wsdlService));
        WSDLEndpoint wsdlEndpoint;
        Port wsdl4jPort;
        while (wsdl4jportsIterator.hasNext()) {
            wsdlEndpoint = this.wsdlComponentFactory.createEndpoint();
            wsdl4jPort = (Port) wsdl4jportsIterator.next();
            this.populatePorts(wsdlEndpoint,
                    wsdl4jPort,
                    wsdl4jService.getQName().getNamespaceURI());
            this.copyExtensibleElements(wsdl4jPort.getExtensibilityElements(),
                    wsdlEndpoint, null);
            wsdlService.setEndpoint(wsdlEndpoint);
        }

    }

    private void pushSchemaElement(Schema originalSchema,Stack stack){
        if (originalSchema==null){
            return;
        }
        stack.push(originalSchema.getElement());
        Map map = originalSchema.getImports();
        Collection values;
        if (map!=null && map.size()>0){
            values = map.values();
            for (Iterator iterator = values.iterator(); iterator.hasNext();) {
                //recursively add the schema's
                Vector v = (Vector)iterator.next();
                for (int i = 0; i < v.size(); i++) {
                    pushSchemaElement(((SchemaImport)v.get(i)).getReferencedSchema(),stack);
                }

            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Internal Component Copying ///////////////////
    public void populateOperations(WSDLOperation wsdlOperation,
                                   Operation wsdl4jOperation,
                                   String nameSpaceOfTheOperation,
                                   WSDLTypes wsdlTypes) {
        //Copy Name Attribute
        wsdlOperation.setName(new QName(nameSpaceOfTheOperation,
                wsdl4jOperation.getName()));

        // This code make no attempt to make use of the special xs:Token
        // defined in the WSDL 2.0. eg like #any, #none
        // Create the Input Message and add
        Input wsdl4jInputMessage = wsdl4jOperation.getInput();
        QName wrappedInputName = wsdlOperation.getName();
        QName wrappedOutputName = new QName(
                wrappedInputName.getNamespaceURI(),
                wrappedInputName.getLocalPart()+ "Response",
                wrappedInputName.getPrefix());

        if (null != wsdl4jInputMessage) {
            MessageReference wsdlInputMessage = this.wsdlComponentFactory
                    .createMessageReference();
            wsdlInputMessage.setDirection(
                    WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
            wsdlInputMessage.setMessageLabel(
                    WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            Message message = wsdl4jInputMessage.getMessage();
            if (null != message) {
                wsdlInputMessage.setElementQName(
                        this.generateReferenceQname(
                                wrappedInputName
                                ,message,
                                findWrapppable(message)));
                this.copyExtensibleElements(
                        (message).getExtensibilityElements(),
                        wsdlInputMessage, null);
            }

            this.copyExtensibilityAttribute(
                    wsdl4jInputMessage.getExtensionAttributes(),
                    wsdlInputMessage);
            wsdlOperation.setInputMessage(wsdlInputMessage);

            // attach the right schema element
            // Note  - commented till the XmlSchema baseuri code is fixed
            //findSchemaElement(wsdlInputMessage,wsdlTypes);
        }

        //Create an output message and add
        Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
        if (null != wsdl4jOutputMessage) {
            MessageReference wsdlOutputMessage =
                    this.wsdlComponentFactory.createMessageReference();
            wsdlOutputMessage.setDirection(
                    WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
            wsdlOutputMessage.setMessageLabel(
                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE);


            Message outputMessage = wsdl4jOutputMessage.getMessage();
            if (null != outputMessage) {
                wsdlOutputMessage.setElementQName(
                        this.generateReferenceQname(wrappedOutputName,outputMessage,findWrapppable(outputMessage)));
                this.copyExtensibleElements(
                        (outputMessage).getExtensibilityElements(),
                        wsdlOutputMessage, null);
            }
            this.copyExtensibilityAttribute(
                    wsdl4jOutputMessage.getExtensionAttributes(),
                    wsdlOutputMessage);
            wsdlOperation.setOutputMessage(wsdlOutputMessage);

            // attach the right schema element
            //Note - Commented till fixing the commons schema
            //findSchemaElement(wsdlOutputMessage,wsdlTypes);

        }

        Map faults = wsdl4jOperation.getFaults();
        Iterator faultKeyIterator = faults.keySet().iterator();
        WSDLFaultReference faultReference;

        while (faultKeyIterator.hasNext()) {

            Fault fault = (Fault) faults.get(faultKeyIterator.next());
            faultReference = wsdlComponentFactory.createFaultReference();
            faultReference.setDirection(
                    WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);
            Message faultMessage = fault.getMessage();
            if (null != faultMessage) {
                faultReference.setRef(
                        this.generateReferenceQname(
                                faultMessage.getQName(),
                                faultMessage,findWrapppable(faultMessage)));
            }
            wsdlOperation.addOutFault(faultReference);
            this.copyExtensibilityAttribute(fault.getExtensionAttributes(),
                    faultReference);
            //TODO Fault Message lable

        }

        //Set the MEP
        wsdlOperation.setMessageExchangePattern(WSDL11MEPFinder
                .getMEP(wsdl4jOperation));

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Utility method to populate a schema
     * @param messageRef
     * @param types
     */
    private void findSchemaElement(MessageReference messageRef,WSDLTypes types){
        QName elementQName = messageRef.getElementQName();
        List typeExtensibilityElements = types.getExtensibilityElements();
        XmlSchema schema;
        for (int i = 0; i < typeExtensibilityElements.size(); i++) {
            Object extElement =  typeExtensibilityElements.get(i);
            if (extElement instanceof org.apache.wsdl.extensions.Schema){
                org.apache.wsdl.extensions.Schema schemaExtesnsibilityElement = ((org.apache.wsdl.extensions.Schema) extElement);
                schema = schemaExtesnsibilityElement.getSchema();
                if (schema.getElementByName(elementQName)!=null){
                    messageRef.setElementSchema(schema.getElementByName(elementQName));
                    break;
                }
            }
        }
    }

    /**
     *
     * @param wsdl4jDefinition
     * @return
     */
    private Element[] generateWrapperSchema(Definition wsdl4jDefinition) {


        List schemaElementList = new ArrayList();
        String targetNamespaceUri = wsdl4jDefinition.getTargetNamespace();

        /////////////////////////////////////////////////////////////////////////////////////////////
        // if there are any bindings present then we have to process them. we have to generate a schema
        // per binding (that is the safest option). if not we just resolve to the good old port type
        // list, in which case we'll generate a schema per porttype
        ////////////////////////////////////////////////////////////////////////////////////////////

        Map bindingsMap = wsdl4jDefinition.getBindings();
        Map porttypeMap = wsdl4jDefinition.getPortTypes();

        if (bindingsMap!=null && !bindingsMap.isEmpty()){
            Binding[] bindings = (Binding[])bindingsMap.values().toArray(new Binding[bindingsMap.size()]);
            Binding binding;
            for (int i = 0; i < bindings.length; i++) {
                binding = bindings[i];
                schemaElementList.add(
                        createSchemaForPorttype(binding.getPortType(),targetNamespaceUri,
                                findWrapForceable(binding)));
            }
        }else{
            PortType[] porttypesArray = (PortType[])porttypeMap.values().toArray(new PortType[porttypeMap.size()]);
            for (int i = 0; i < porttypesArray.length; i++) {
                schemaElementList.add(
                        createSchemaForPorttype(porttypesArray[i],targetNamespaceUri,false));
            }

        }

        return (Element[])schemaElementList.toArray(new Element[schemaElementList.size()]);
    }


    private boolean findWrapForceable(Binding binding){
        List extElements = binding.getExtensibilityElements();
        for (int i = 0; i < extElements.size(); i++) {
            if (extElements.get(i) instanceof SOAPBinding){
                SOAPBinding soapBinding = (SOAPBinding)extElements.get(i);
                if ("rpc".equals(soapBinding.getStyle())){
                    //oops - we've found a SOAPBinding that has a rpc style
                    //we better force the wrapping then
                    return true;
                }
            }
        }

        return false;
    }


    /**
     * Creates a schema given the porttype
     * @return
     */
    private Element createSchemaForPorttype(PortType porttype,String targetNamespaceUri,boolean forceWrapping){

        //loop through the messages. We'll populate this map with the relevant messages
        //from the operations
        Map messagesMap = new HashMap();
        Map inputOperationsMap = new HashMap();
        Map outputOperationsMap = new HashMap();
        //this contains the required namespace imports. the key in this
        //map would be the namaspace URI
        Map namespaceImportsMap = new HashMap();
        //generated complextypes. Keep in the list for writing later
        //the key for the complexType map is the message QName
        Map complexTypeElementsMap = new HashMap();
        //generated Elements. Kep in the list for later writing
        List elementElementsList = new ArrayList();
        //list namespace prefix map. This map will include uri -> prefix
        Map namespacePrefixMap = new HashMap();

        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // First thing is to populate the message map with the messages to process.
        ////////////////////////////////////////////////////////////////////////////////////////////////////


        //we really need to do this for a single porttype!
        List operations = porttype.getOperations();
        Operation op;
        for (int k = 0; k < operations.size(); k++) {
            op = (Operation)operations.get(k);
            Input input = op.getInput();
            Message message ;
            if (input!=null){
                message = input.getMessage();
                messagesMap.put(message.getQName(),message);
                inputOperationsMap.put(op.getName(),message);

            }

            Output output = op.getOutput();
            if (output!=null){
                message = output.getMessage();
                messagesMap.put(message.getQName(),message);
                outputOperationsMap.put(op.getName(),message);
            }
            //todo also handle the faults here
        }



        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //check whether there are messages that are wrappable. If there are no messages that are wrappable we'll
        //just return null and endup this process. However we need to take the force flag into account here
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        QName[] keys;
        if(forceWrapping){
            //just take all the messages and wrap them, we've been told to force wrapping!
            keys = (QName[])messagesMap.keySet().toArray(new QName[messagesMap.size()]);
        } else{
            //
            QName[] allKeys = (QName[])messagesMap.keySet().toArray(new QName[messagesMap.size()]);
            List wrappableMessageNames = new ArrayList();
            boolean noMessagesTobeProcessed = true;
            for (int i = 0; i < allKeys.length; i++) {
                if (findWrapppable((Message)messagesMap.get(allKeys[i]))){
                    noMessagesTobeProcessed = false;
                    //add that message to the list
                    wrappableMessageNames.add(allKeys[i]);
                }
            }
            if (noMessagesTobeProcessed){
                return null;
            }

            keys = (QName[])wrappableMessageNames.toArray(new QName[wrappableMessageNames.size()]);
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Now we have the message list to process - Process the whole list of messages at once
        // since we need to generate one single schema
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        List resolvedMessageQNames = new ArrayList();
        //find the xsd prefix
        String xsdPrefix = findSchemaPrefix();
        Message wsdl4jMessage;
        //DOM document that will be the ultimate creator
        Document document = getDOMDocumentBuilder().newDocument();
        for (int i = 0; i < keys.length; i++) {
            wsdl4jMessage = (Message)messagesMap.get(keys[i]);
            //No need to check the wrappable,

            //This message is wrappabel. However we need to see whether the message is already
            //resolved!
            if (!resolvedMessageQNames.contains(wsdl4jMessage.getQName())){
                //This message has not been touched before!. So we can go ahead now
                Map parts = wsdl4jMessage.getParts();
                //add the complex type
                String name = wsdl4jMessage.getQName().getLocalPart();
                Element newComplexType = document.createElementNS(WSDLPump.XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);
                newComplexType.setAttribute(WSDLPump.XSD_NAME, name);

                Element cmplxContentSequence = document.createElementNS(WSDLPump.XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_SEQUENCE_LOCAL_NAME);
                Element child;
                Iterator iterator = parts.keySet().iterator();
                while (iterator.hasNext()) {
                    Part part = (Part) parts.get(iterator.next());
                    //the part name
                    String elementName = part.getName();
                    boolean isTyped = true;
                    //the type name
                    QName schemaTypeName;
                    if (part.getTypeName()!=null){
                        schemaTypeName = part.getTypeName();
                    }else if (part.getElementName()!=null){
                        schemaTypeName = part.getElementName();
                        isTyped = false;
                    }else{
                        throw new RuntimeException(" Unqualified Message part!");
                    }

                    child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_ELEMENT_LOCAL_NAME);

                    String prefix;
                    if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())){
                        prefix = xsdPrefix;
                    }else{
                        //this schema is a third party one. So we need to have an import statement in our generated schema
                        String uri = schemaTypeName.getNamespaceURI();
                        if (!namespaceImportsMap.containsKey(uri)){
                            //create Element for namespace import
                            Element namespaceImport = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_IMPORT_LOCAL_NAME);
                            namespaceImport.setAttribute("namespace",uri);
                            //add this to the map
                            namespaceImportsMap.put(uri,namespaceImport);
                            //we also need to associate this uri with a prefix and include that prefix
                            //in the schema's namspace declarations. So add theis particular namespace to the
                            //prefix map as well
                            prefix = getTemporaryNamespacePrefix();
                            namespacePrefixMap.put(uri,prefix);
                        }else{
                            //this URI should be already in the namspace prefix map
                            prefix = (String)namespacePrefixMap.get(uri);
                        }


                    }
                    // If it's from a type the element we need to add a name and the type
                    //if not it's the element reference
                    if (isTyped){
                        child.setAttribute(WSDLPump.XSD_NAME, elementName);
                        child.setAttribute(WSDLPump.XSD_TYPE, prefix +":"+schemaTypeName.getLocalPart());
                    }else{
                        child.setAttribute(WSDLPump.XSD_REF, prefix +":"+schemaTypeName.getLocalPart());
                    }
                    cmplxContentSequence.appendChild(child);
                }
                newComplexType.appendChild(cmplxContentSequence);
                //add this newly created complextype to the list
                complexTypeElementsMap.put(wsdl4jMessage.getQName(),newComplexType);
                resolvedMessageQNames.add(wsdl4jMessage.getQName());
            }

        }

        Element elementDeclaration;


        //loop through the input op map and generate the elements
        String[] inputOperationtNames = (String[])inputOperationsMap.keySet().toArray(
                new String[inputOperationsMap.size()]);
        for (int j = 0; j < inputOperationtNames.length; j++) {
            String inputOpName = inputOperationtNames[j];
            elementDeclaration = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(WSDLPump.XSD_NAME,
                    inputOpName);

            String typeValue = ((Message) inputOperationsMap.get(inputOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(WSDLPump.XSD_TYPE,
                    AXIS2WRAPPED + ":" +typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(inputOpName,new QName(
                    targetNamespaceUri,
                    inputOpName,
                    AXIS2WRAPPED
            ));
        }

        //loop through the output op map and generate the elements
        String[] outputOperationtNames = (String[])outputOperationsMap.keySet().toArray(
                new String[outputOperationsMap.size()]);
        for (int j = 0; j < outputOperationtNames.length; j++) {

            String baseoutputOpName = outputOperationtNames[j];
            String outputOpName = baseoutputOpName+"Response";
            elementDeclaration = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+ XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(WSDLPump.XSD_NAME,
                    outputOpName);
            String typeValue = ((Message) outputOperationsMap.get(baseoutputOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(WSDLPump.XSD_TYPE,
                    AXIS2WRAPPED + ":" +typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(outputOpName,new QName(
                    targetNamespaceUri,
                    outputOpName,
                    AXIS2WRAPPED
            ));

        }







        //////////////////////////////////////////////////////////////////////////////////////////////
        // Now we are done with processing  the messages and generating the right schema object model
        // time to write out the schema
        //////////////////////////////////////////////////////////////////////////////////////////////


        Element schemaElement = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"+XML_SCHEMA_LOCAL_NAME);


        //loop through the namespace declarations first
        String[] nameSpaceDeclarationArray = (String[])namespacePrefixMap.keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                    "xmlns:" + namespacePrefixMap.get(s).toString(),
                    s);

        }

        //add the targetNamespace

        schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                XMLNS_AXIS2WRAPPED,
                targetNamespaceUri);
        schemaElement.setAttribute(XSD_TARGETNAMESPACE,targetNamespaceUri);
        schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT,XSD_UNQUALIFIED);

        Element[] namespaceImports = (Element[])namespaceImportsMap.values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            schemaElement.appendChild(namespaceImports[i]);

        }


        Element[] complexTypeElements = (Element[])complexTypeElementsMap.values().toArray(new Element[complexTypeElementsMap.size()]);
        for (int i = 0; i < complexTypeElements.length; i++) {
            schemaElement.appendChild(complexTypeElements[i]);

        }

        Element[] elementDeclarations = (Element[])elementElementsList.toArray(new Element[elementElementsList.size()]);
        for (int i = 0; i < elementDeclarations.length; i++) {
            schemaElement.appendChild(elementDeclarations[i]);

        }




        return schemaElement;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Generates a referenceQName
     * @param wsdl4jMessage
     * @return
     */
    private QName generateReferenceQname(QName outerName,Message wsdl4jMessage,boolean isWrappable) {
        QName referenceQName = null;
        if (isWrappable) {
            //The schema for this should be already made ! Find the QName from the list
            referenceQName=(QName)resolvedRpcWrappedElementMap.get(outerName.getLocalPart());

        } else {
            //Only one part so copy the QName of the referenced type.
            Iterator outputIterator =
                    wsdl4jMessage.getParts().values().iterator();
            if (outputIterator.hasNext()) {
                Part outPart = ((Part) outputIterator.next());
                QName typeName;
                if (null != (typeName = outPart.getTypeName())) {
                    referenceQName = typeName;
                } else {
                    referenceQName = outPart.getElementName();
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////
        //System.out.println("final referenceQName = " + referenceQName);
        ////////////////////////////////////////////////////////////////////////////////
        return referenceQName;
    }

    /**
     * Utility method that returns a DOM Builder
     * @return
     */
    private DocumentBuilder getDOMDocumentBuilder() {
        DocumentBuilder documentBuilder;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return documentBuilder;
    }

    /**
     *   Find the  XML schema prefix
     */
    private String  findSchemaPrefix() {
        String xsdPrefix=null;
        if (declaredNameSpaces.containsValue(XMLSCHEMA_NAMESPACE_URI)){
            //loop and find the prefix
            Iterator it = declaredNameSpaces.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key =  (String)it.next();
                if (XMLSCHEMA_NAMESPACE_URI.equals(declaredNameSpaces.get(key))){
                    xsdPrefix = key;
                    break;
                }
            }

        }else{
            xsdPrefix = XMLSCHEMA_NAMESPACE_PREFIX; //default prefix
        }

        return  xsdPrefix;
    }

    /**
     *
     * @param wsdlBindingOperation
     * @param wsdl4jBindingOperation
     * @param nameSpaceOfTheBindingOperation
     * @param wsdl4jDefinition
     */
    private void populateBindingOperation(
            WSDLBindingOperation wsdlBindingOperation,
            BindingOperation wsdl4jBindingOperation,
            String nameSpaceOfTheBindingOperation, Definition wsdl4jDefinition) {

        wsdlBindingOperation.setName(
                new QName(nameSpaceOfTheBindingOperation,
                        wsdl4jBindingOperation.getName()));

        BindingInput wsdl4jInputBinding =
                wsdl4jBindingOperation.getBindingInput();

        if (null != wsdl4jInputBinding) {
            WSDLBindingMessageReference wsdlInputBinding =
                    this.wsdlComponentFactory.createWSDLBindingMessageReference();
            wsdlInputBinding.setDirection(
                    WSDLConstants.WSDL_MESSAGE_DIRECTION_IN);
            this.copyExtensibleElements(
                    wsdl4jInputBinding.getExtensibilityElements(),
                    wsdlInputBinding, wsdl4jDefinition);
            wsdlBindingOperation.setInput(wsdlInputBinding);
        }

        BindingOutput wsdl4jOutputBinding = wsdl4jBindingOperation
                .getBindingOutput();
        if (null != wsdl4jOutputBinding) {
            WSDLBindingMessageReference wsdlOutputBinding = this.wsdlComponentFactory
                    .createWSDLBindingMessageReference();
            wsdlOutputBinding.setDirection(
                    WSDLConstants.WSDL_MESSAGE_DIRECTION_OUT);

            this.copyExtensibleElements(
                    wsdl4jOutputBinding.getExtensibilityElements(),
                    wsdlOutputBinding, null);
            wsdlBindingOperation.setOutput(wsdlOutputBinding);
        }


        Map bindingFaults = wsdl4jBindingOperation.getBindingFaults();
        Iterator keyIterator = bindingFaults.keySet().iterator();
        while (keyIterator.hasNext()) {
            BindingFault bindingFault = (BindingFault) bindingFaults.get(
                    keyIterator.next());
            WSDLBindingFault womBindingFault = this.wsdlComponentFactory.createBindingFault();
            this.copyExtensibleElements(
                    bindingFault.getExtensibilityElements(), womBindingFault, null);
            wsdlBindingOperation.addOutFault(womBindingFault);
        }

    }

    public void populatePorts(WSDLEndpoint wsdlEndpoint, Port wsdl4jPort,
                              String targetNamspace) {
        wsdlEndpoint.setName(new QName(targetNamspace, wsdl4jPort.getName()));

        wsdlEndpoint.setBinding(
                this.womDefinition.getBinding(
                        wsdl4jPort
                                .getBinding()
                                .getQName()));

    }

    /**
     * This method will fill up the gap of WSDL 1.1 and WSDL 2.0 w.r.t. the
     * bound interface for the Service Component Defined in the WSDL 2.0. Logic
     * being if there exist only one PortType in the WSDL 1.1 file then that
     * will be set as the bound interface of the Service. If more than one
     * Porttype exist in the WSDl 1.1 file this will create a dummy Interface
     * with the available PortTypes and will return that interface so that it
     * will inherit all those interfaces.
     * <p/>
     * Eventuall this will have to be fixed using user input since
     *
     * @param service
     * @return wsdl interface
     */
    private WSDLInterface getBoundInterface(WSDLService service) {

        // Throw an exception if there are no interfaces defined as at yet.
        if (0 == this.womDefinition.getWsdlInterfaces().size())
            throw new WSDLProcessingException("There are no "
                    +
                    "Interfaces/PortTypes identified in the current partially built"
                    + "WOM");

//If there is only one Interface available hten return that because
// normally
// that interface must be the one to the service should get bound.
        if (1 == this.womDefinition.getWsdlInterfaces().size())
            return (WSDLInterface) this.womDefinition.getWsdlInterfaces()
                    .values().iterator().next();

//If there are more than one interface available... For the time being
// create a
// new interface and set all those existing interfaces as
// superinterfaces of it
// and return.
        WSDLInterface newBoundInterface = this.womDefinition.createInterface();
        newBoundInterface.setName(
                new QName(service.getNamespace(),
                        service
                                .getName()
                                .getLocalPart()
                                + BOUND_INTERFACE_NAME));
        Iterator interfaceIterator = this.womDefinition.getWsdlInterfaces()
                .values().iterator();
        while (interfaceIterator.hasNext()) {
            newBoundInterface
                    .addSuperInterface(
                            (WSDLInterface) interfaceIterator.next());
        }
        return newBoundInterface;
    }

    /**
     * Get the Extensible elements form wsdl4jExtensibleElements
     * <code>Vector</code> if any and copy them to <code>Component</code>
     *


     @param wsdl4jExtensibleElements
      * @param component
     * @param wsdl4jDefinition

     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Component component, Definition wsdl4jDefinition) {
        Iterator iterator = wsdl4jExtensibleElements.iterator();
        ExtensionFactory extensionFactory = this.wsdlComponentFactory
                .createExtensionFactory();
        while (iterator.hasNext()) {

            ExtensibilityElement wsdl4jElement = (ExtensibilityElement) iterator
                    .next();

            if (wsdl4jElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) (wsdl4jElement);

//look for the SOAP 1.2 stuff here. WSDL4j does not understand SOAP 1.2 things
                if (ExtensionConstants.SOAP_12_OPERATION.equals(unknown.getElementType())){
                    org.apache.wsdl.extensions.SOAPOperation soapOperationExtensibiltyElement = (org.apache.wsdl.extensions.SOAPOperation) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapOperationExtensibiltyElement.setSoapAction(element.getAttribute("soapAction"));
                    soapOperationExtensibiltyElement.setStyle(element.getAttribute("style"));
// soapActionRequired
                    component.addExtensibilityElement(soapOperationExtensibiltyElement);
                }else if (ExtensionConstants.SOAP_12_BODY.equals(unknown.getElementType())){
                    org.apache.wsdl.extensions.SOAPBody soapBodyExtensibiltyElement = (org.apache.wsdl.extensions.SOAPBody) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapBodyExtensibiltyElement.setUse(element.getAttribute("use"));
                    soapBodyExtensibiltyElement.setNamespaceURI(element.getAttribute("namespace"));
//encoding style
                    component.addExtensibilityElement(soapBodyExtensibiltyElement);
                }else if (ExtensionConstants.SOAP_12_HEADER.equals(unknown.getElementType())){
                    org.apache.wsdl.extensions.SOAPHeader soapHeaderExtensibilityElement = (org.apache.wsdl.extensions.SOAPHeader) extensionFactory.getExtensionElement(
                            unknown.getElementType());
                       //right now there's no known header binding!. Ignore the copying of values for now
                    component.addExtensibilityElement(soapHeaderExtensibilityElement);
                }else if (ExtensionConstants.SOAP_12_BINDING.equals(unknown.getElementType())){
                    org.apache.wsdl.extensions.SOAPBinding soapBindingExtensibiltyElement = (org.apache.wsdl.extensions.SOAPBinding) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapBindingExtensibiltyElement.setTransportURI(element.getAttribute("transport"));
                    soapBindingExtensibiltyElement.setStyle(element.getAttribute("style"));

                    component.addExtensibilityElement(soapBindingExtensibiltyElement);
                } else if (ExtensionConstants.SOAP_12_ADDRESS.equals(unknown.getElementType())){
                    org.apache.wsdl.extensions.SOAPAddress soapAddressExtensibiltyElement = (org.apache.wsdl.extensions.SOAPAddress) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapAddressExtensibiltyElement.setLocationURI(element.getAttribute("location"));
                    component.addExtensibilityElement(soapAddressExtensibiltyElement);

                } else if (ExtensionConstants.POLICY.equals(unknown.getElementType())) {
                    PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensionFactory.getExtensionElement(wsdl4jElement.getElementType());
                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory.getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    policyExtensibilityElement.setPolicyElement(policyReader.readPolicy(unknown.getElement()));
                    component.addExtensibilityElement(policyExtensibilityElement);

                } else if (ExtensionConstants.POLICY_REFERENCE.equals(unknown.getElementType())) {
                    PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensionFactory.getExtensionElement(wsdl4jElement.getElementType());
                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory.getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    policyExtensibilityElement.setPolicyElement(policyReader.readPolicyReference(unknown.getElement()));
                    component.addExtensibilityElement(policyExtensibilityElement);

                }else{

                    DefaultExtensibilityElement defaultExtensibilityElement = (DefaultExtensibilityElement) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    defaultExtensibilityElement.setElement(unknown.getElement());
                    Boolean required = unknown.getRequired();
                    if (null != required) {
                        defaultExtensibilityElement.setRequired(required.booleanValue());
                    }
                    component.addExtensibilityElement(defaultExtensibilityElement);
                }


            } else if (wsdl4jElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPAddress soapAddressExtensibilityElement = (org.apache.wsdl.extensions.SOAPAddress) extensionFactory
                        .getExtensionElement(soapAddress.getElementType());
                soapAddressExtensibilityElement.setLocationURI(soapAddress
                        .getLocationURI());
                Boolean required = soapAddress.getRequired();
                if (null != required) {
                    soapAddressExtensibilityElement.setRequired(required.booleanValue());
                }
                component.addExtensibilityElement(soapAddressExtensibilityElement);
            } else if (wsdl4jElement instanceof Schema) {
                Schema schema = (Schema) wsdl4jElement;
//schema.getDocumentBaseURI()
//populate the imported schema stack
                Stack schemaStack = new Stack();
//recursivly load the schema elements. The best thing is to push these into
//a stack and then pop from the other side
                pushSchemaElement(schema, schemaStack);
                org.apache.wsdl.extensions.Schema schemaExtensibilityElement = (org.apache.wsdl.extensions.Schema) extensionFactory.getExtensionElement(
                        schema.getElementType());
                schemaExtensibilityElement.setElement(schema.getElement());
                schemaExtensibilityElement.setImportedSchemaStack(schemaStack);
                Boolean required = schema.getRequired();
                if (null != required) {
                    schemaExtensibilityElement.setRequired(required.booleanValue());
                }
                //set the name of this Schema element
                //todo this needs to be fixed
                if(schema.getDocumentBaseURI() != null) {
                    schemaExtensibilityElement.setName(new QName("",schema.getDocumentBaseURI()));
                }
                component.addExtensibilityElement(schemaExtensibilityElement);
            } else if (SOAPConstants.Q_ELEM_SOAP_OPERATION.equals(
                    wsdl4jElement.getElementType())) {
                SOAPOperation soapOperation = (SOAPOperation) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPOperation soapOperationextensibilityElement = (org.apache.wsdl.extensions.SOAPOperation) extensionFactory.getExtensionElement(
                        soapOperation.getElementType());
                soapOperationextensibilityElement.setSoapAction(
                        soapOperation.getSoapActionURI());
                soapOperationextensibilityElement.setStyle(soapOperation.getStyle());
                Boolean required = soapOperation.getRequired();
                if (null != required) {
                    soapOperationextensibilityElement.setRequired(required.booleanValue());
                }
                component.addExtensibilityElement(soapOperationextensibilityElement);
            } else if (SOAPConstants.Q_ELEM_SOAP_BODY.equals(
                    wsdl4jElement.getElementType())) {
                SOAPBody soapBody = (SOAPBody) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPBody soapBodyExtensibilityElement = (org.apache.wsdl.extensions.SOAPBody) extensionFactory.getExtensionElement(
                        soapBody.getElementType());
                soapBodyExtensibilityElement.setNamespaceURI(
                        soapBody.getNamespaceURI());
                soapBodyExtensibilityElement.setUse(soapBody.getUse());
                Boolean required = soapBody.getRequired();
                if (null != required) {
                    soapBodyExtensibilityElement.setRequired(required.booleanValue());
                }

                component.addExtensibilityElement(soapBodyExtensibilityElement);
//add the header
            } else if (SOAPConstants.Q_ELEM_SOAP_HEADER.equals(
                    wsdl4jElement.getElementType())) {
                SOAPHeader soapHeader = (SOAPHeader) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPHeader soapHeaderExtensibilityElement = (org.apache.wsdl.extensions.SOAPHeader) extensionFactory.getExtensionElement(
                        soapHeader.getElementType());
                soapHeaderExtensibilityElement.setNamespaceURI(
                        soapHeader.getNamespaceURI());
                soapHeaderExtensibilityElement.setUse(soapHeader.getUse());
                Boolean required = soapHeader.getRequired();
                if (null != required) {
                    soapHeaderExtensibilityElement.setRequired(required.booleanValue());
                }
                if (null!=wsdl4jDefinition){
                    //find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader.getMessage());
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    soapHeaderExtensibilityElement.setElement(msgPart.getElementName());
                }
                soapHeaderExtensibilityElement.setMessage(soapHeader.getMessage());

                soapHeaderExtensibilityElement.setPart(soapHeader.getPart());
                soapHeader.getMessage();
                component.addExtensibilityElement(soapHeaderExtensibilityElement);
            } else if (SOAPConstants.Q_ELEM_SOAP_BINDING.equals(
                    wsdl4jElement.getElementType())) {
                SOAPBinding soapBinding = (SOAPBinding) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPBinding soapBindingExtensibilityElement = (org.apache.wsdl.extensions.SOAPBinding) extensionFactory.getExtensionElement(
                        soapBinding.getElementType());
                soapBindingExtensibilityElement.setTransportURI(
                        soapBinding.getTransportURI());
                soapBindingExtensibilityElement.setStyle(soapBinding.getStyle());
                Boolean required = soapBinding.getRequired();
                if (null != required) {
                    soapBindingExtensibilityElement.setRequired(required.booleanValue());
                }
                component.addExtensibilityElement(soapBindingExtensibilityElement);
            }
        }
    }

    /**
     * Get the Extensible Attributes from wsdl4jExtensibilityAttribute
     * <code>Map</code> if any and copy them to the <code>Component</code>
     *
     * @param wsdl4jExtensibilityAttributes
     * @param component
     */
    private void copyExtensibilityAttribute(Map wsdl4jExtensibilityAttributes,
                                            Component component) {
        Iterator iterator = wsdl4jExtensibilityAttributes.keySet().iterator();
        while (iterator.hasNext()) {
            QName attributeName = (QName) iterator.next();
            QName value = (QName) wsdl4jExtensibilityAttributes
                    .get(attributeName);
            WSDLExtensibilityAttribute attribute = this.wsdlComponentFactory
                    .createWSDLExtensibilityAttribute();
            attribute.setKey(attributeName);
            attribute.setValue(value);
            component.addExtensibleAttributes(attribute);
        }
    }

    /**


     /**
     *
     * @return
     */
    private String getTemporaryNamespacePrefix(){
        return "ns"+nsCount++ ;
    }
}
