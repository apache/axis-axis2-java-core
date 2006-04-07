package org.apache.axis2.description;

import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
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
import java.util.Stack;
import java.util.Vector;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 */

public class WSDL2AxisServiceBuilder {

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

    private static int nsCount = 0;

    private Map resolvedRpcWrappedElementMap = new HashMap();

    private static final String XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";

    private static final String XSD_UNQUALIFIED = "unqualified";

    private InputStream in;

    private AxisService axisService;

    private QName serviceName;

    private String portName;


    private boolean isServerSide = true;
    private static final String BINDING = "Binding";
    private static final String SERVICE = "Service";
    private static final String PORT = "Port";
    private static final String TYPES = "Types";
    private static final String PORT_TYPE_OPERATION = "PortType.Operation";
    private static final String PORT_TYPE_OPERATION_INPUT = "PortType.Operation.Input";
    private static final String PORT_TYPE_OPERATION_OUTPUT = "PortType.Operation.Output";
    private static final String PORT_TYPE_OPERATION_FAULT = "PortType.Operation.Fault";
    private static final String BINDING_OPERATION = "Binding.Operation";
    private static final String BINDING_OPERATION_INPUT = "Binding.Operation.Input";
    private static final String BINDING_OPERATION_OUTPUT = "Binding.Operation.Output";

    private Definition wsdl4jDefinition = null;

    private String style = null;

    public WSDL2AxisServiceBuilder(InputStream in, QName serviceName,
                                   String portName) {
        this.in = in;
        this.serviceName = serviceName;
        this.portName = portName;
        this.axisService = new AxisService();
    }

    public WSDL2AxisServiceBuilder(Definition def, QName serviceName,
                                   String portName) {
        this.wsdl4jDefinition = def;
        this.serviceName = serviceName;
        this.portName = portName;
        this.axisService = new AxisService();
    }

    public WSDL2AxisServiceBuilder(InputStream in, AxisService service) {
        this(in);
        this.axisService = service;
    }

    public WSDL2AxisServiceBuilder(InputStream in) {
        this(in, null, null);
    }

    public boolean isServerSide() {
        return isServerSide;
    }

    public void setServerSide(boolean serverSide) {
        isServerSide = serverSide;
    }

    public AxisService populateService() throws AxisFault {
        try {
            if (wsdl4jDefinition == null) {
                wsdl4jDefinition = readInTheWSDLFile(in);
            }

            if (wsdl4jDefinition == null) {
                return null;
            }
            //setting target name space
            axisService.setTargetNamespace(wsdl4jDefinition.getTargetNamespace());
            //adding ns in the original WSDL
            axisService.setNameSpacesMap(wsdl4jDefinition.getNamespaces());
            //scheam generation
            processImports(wsdl4jDefinition);
            Types wsdl4jTypes = wsdl4jDefinition.getTypes();
            if (null != wsdl4jTypes) {
                this.copyExtensibleElements(wsdl4jTypes
                        .getExtensibilityElements(), wsdl4jDefinition, axisService, TYPES);
            }
            Binding binding = findBinding(wsdl4jDefinition);
            //////////////////(1.2) /////////////////////////////
            // create new Schema extensions element for wrapping
            Element[] schemaElements = generateWrapperSchema(wsdl4jDefinition, binding);
            if (schemaElements != null && schemaElements.length > 0) {
                for (int i = 0; i < schemaElements.length; i++) {
                    Element schemaElement = schemaElements[i];
                    if (schemaElement != null) {
                        System.out.println(schemaElement.getNamespaceURI());
                        ExtensionFactoryImpl extensionFactory = new ExtensionFactoryImpl();
                        org.apache.wsdl.extensions.Schema schemaExtensibilityElement = (org.apache.wsdl.extensions.Schema) extensionFactory
                                .getExtensionElement(ExtensionConstants.SCHEMA);
                        schemaExtensibilityElement.setElement(schemaElement);
                        axisService
                                .setSchema(getXMLSchema(schemaExtensibilityElement
                                        .getElement()));
                    }
                }
            }
            processBinding(binding, wsdl4jDefinition);
            return axisService;
        } catch (WSDLException e) {
            throw new AxisFault(e);
        }
    }


    private Binding findBinding(Definition dif) throws AxisFault {
        Map services = dif.getServices();
        Service service;
        Binding binding = null;
        Port port = null;
        if (serviceName != null) {
            service = (Service) services.get(serviceName);
            if (service == null) {
                throw new AxisFault("Service not found the WSDL "
                        + serviceName.getLocalPart());
            }
        } else {
            if (services.size() > 0) {
                service = (Service) services.values().toArray()[0];
            } else {
                throw new AxisFault("No service element found in the WSDL");
            }
        }
        copyExtensibleElements(service.getExtensibilityElements(), dif,
                axisService, SERVICE);
        if (portName != null) {
            port = service.getPort(portName);
            if (port == null) {
                throw new AxisFault("No port found for the given name :"
                        + portName);
            }
        } else {
            Map ports = service.getPorts();
            if (ports != null && ports.size() > 0) {
                port = (Port) ports.values().toArray()[0];
            }
        }
        axisService.setName(service.getQName().getLocalPart());
        if (port != null) {
            copyExtensibleElements(port.getExtensibilityElements(), dif,
                    axisService, PORT);
            binding = port.getBinding();
        }
        return binding;
    }


    private void processBinding(Binding binding, Definition dif)
            throws AxisFault {
        if (binding != null) {
            copyExtensibleElements(binding.getExtensibilityElements(), wsdl4jDefinition,
                    axisService,BINDING);
            PortType portType = binding.getPortType();
            processPortType(portType, dif);

            List list = binding.getBindingOperations();
            copyExtensibleElements(binding.getExtensibilityElements(), dif,
                    axisService, BINDING);
            for (int i = 0; i < list.size(); i++) {
                BindingOperation wsdl4jBindingOperation = (BindingOperation) list
                        .get(i);
                AxisOperation operation = axisService.getOperation(new QName(
                        wsdl4jBindingOperation.getName()));
                copyExtensibleElements(wsdl4jBindingOperation
                        .getExtensibilityElements(), dif, operation, BINDING_OPERATION);

                BindingInput bindingInput = wsdl4jBindingOperation
                        .getBindingInput();
                BindingOutput bindingOutput = wsdl4jBindingOperation
                        .getBindingOutput();
                String MEP = operation.getMessageExchangePattern();
                if (bindingInput != null) {
                    if (WSDLConstants.MEP_URI_IN_ONLY.equals(MEP)
                            || WSDLConstants.MEP_URI_IN_OPTIONAL_OUT
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_OUT_OPTIONAL_IN
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_ROBUST_OUT_ONLY
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                            || WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                        AxisMessage inMessage = operation
                                .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                        copyExtensibleElements(bindingInput
                                .getExtensibilityElements(), dif, inMessage, BINDING_OPERATION_INPUT);

                    }
                }
                if (bindingOutput != null) {
                    if (WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP)
                            || WSDLConstants.MEP_URI_OUT_OPTIONAL_IN
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_IN_OPTIONAL_OUT
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_ROBUST_OUT_ONLY
                            .equals(MEP)
                            || WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                            || WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                        AxisMessage outAxisMessage = operation
                                .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                        copyExtensibleElements(bindingOutput
                                .getExtensibilityElements(), dif,
                                outAxisMessage, BINDING_OPERATION_OUTPUT);

                    }
                }
            }

        }
    }

    /**
     * Simply Copy information.
     *
     * @param wsdl4jPortType
     */
    // FIXME Evaluate a way of injecting features and priperties with a general
    // formatted input
    private void processPortType(PortType wsdl4jPortType, Definition dif)
            throws AxisFault {

        //Copy the Attribute information items
        //Copied with the Same QName so it will require no Query in Binding
        //Coping.


        Iterator wsdl4JOperationsIterator = wsdl4jPortType.getOperations()
                .iterator();
        Operation wsdl4jOperation;
        while (wsdl4JOperationsIterator.hasNext()) {
            wsdl4jOperation = (Operation) wsdl4JOperationsIterator.next();

            axisService.addOperation(populateOperations(wsdl4jOperation, dif));
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Internal Component Copying ///////////////////
    private AxisOperation populateOperations(Operation wsdl4jOperation,
                                             Definition dif) throws AxisFault {
        QName opName = new QName(wsdl4jOperation.getName());
        //Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);
        if (axisOperation == null) {
            String MEP = getMEP(wsdl4jOperation);
            axisOperation = AxisOperationFactory
                    .getOperationDescription(MEP);
            axisOperation.setName(opName);
        }
        if (style != null) {
            axisOperation.setStyle(style);
        }
        copyExtensibleElements(wsdl4jOperation.getExtensibilityElements(), dif,
                axisOperation, PORT_TYPE_OPERATION);

        Input wsdl4jInputMessage = wsdl4jOperation.getInput();
        QName wrappedInputName = axisOperation.getName();
        QName wrappedOutputName = new QName(wrappedInputName.getNamespaceURI(),
                wrappedInputName.getLocalPart() + "Response", wrappedInputName
                .getPrefix());
        if (null != wsdl4jInputMessage) {
            AxisMessage inMessage = axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            Message message = wsdl4jInputMessage.getMessage();
            if (null != message) {
                inMessage.setElementQName(generateReferenceQname(
                        wrappedInputName, message, findWrapppable(message)));
                inMessage.setName(message.getQName().getLocalPart());
                copyExtensibleElements(message.getExtensibilityElements(), dif,
                        inMessage, PORT_TYPE_OPERATION_INPUT);

            }
        }
        //Create an output message and add
        Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
        if (null != wsdl4jOutputMessage) {
            AxisMessage outMessage = axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            Message message = wsdl4jOutputMessage.getMessage();
            if (null != message) {
                outMessage.setElementQName(generateReferenceQname(
                        wrappedOutputName, message, findWrapppable(message)));
                outMessage.setName(message.getQName().getLocalPart());
                copyExtensibleElements(message.getExtensibilityElements(), dif,
                        outMessage, PORT_TYPE_OPERATION_OUTPUT);

                // wsdl:portType -> wsdl:operation -> wsdl:output
                populatePolicyInclude(PolicyInclude.OUTPUT_POLICY, outMessage);
            }
        }

        Map faults = wsdl4jOperation.getFaults();
        Iterator faultKeyIterator = faults.keySet().iterator();

        while (faultKeyIterator.hasNext()) {
            Fault fault = (Fault) faults.get(faultKeyIterator.next());
            AxisMessage faultyMessge = new AxisMessage();
            Message faultMessage = fault.getMessage();
            if (null != faultMessage) {
                faultyMessge.setElementQName(generateReferenceQname(
                        faultMessage.getQName(), faultMessage,
                        findWrapppable(faultMessage)));
                copyExtensibleElements(faultMessage.getExtensibilityElements(),
                        dif, faultyMessge, PORT_TYPE_OPERATION_FAULT);
                faultyMessge.setName(faultMessage.getQName().getLocalPart());

            }

            axisOperation.setFaultMessages(faultyMessge);
        }
        return axisOperation;
    }

    /**
     * Generates a referenceQName
     *
     * @param wsdl4jMessage
     */
    private QName generateReferenceQname(QName outerName,
                                         Message wsdl4jMessage, boolean isWrappable) {
        QName referenceQName = null;
        if (isWrappable) {
            //The schema for this should be already made ! Find the QName from
            // the list
            referenceQName = (QName) resolvedRpcWrappedElementMap.get(outerName
                    .getLocalPart());

        } else {
            //Only one part so copy the QName of the referenced type.
            Iterator outputIterator = wsdl4jMessage.getParts().values()
                    .iterator();
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

    private Element[] generateWrapperSchema(Definition wsdl4jDefinition,
                                            Binding binding) {

        List schemaElementList = new ArrayList();
        String targetNamespaceUri = wsdl4jDefinition.getTargetNamespace();

        /////////////////////////////////////////////////////////////////////////////////////////////
        // if there are any bindings present then we have to process them. we
        // have to generate a schema
        // per binding (that is the safest option). if not we just resolve to
        // the good old port type
        // list, in which case we'll generate a schema per porttype
        ////////////////////////////////////////////////////////////////////////////////////////////

        schemaElementList.add(createSchemaForPorttype(binding.getPortType(),
                targetNamespaceUri, findWrapForceable(binding)));
        return (Element[]) schemaElementList
                .toArray(new Element[schemaElementList.size()]);
    }

    private Element createSchemaForPorttype(PortType porttype,
                                            String targetNamespaceUri, boolean forceWrapping) {

        //loop through the messages. We'll populate this map with the relevant
        // messages
        //from the operations
        Map messagesMap = new HashMap();
        Map inputOperationsMap = new HashMap();
        Map outputOperationsMap = new HashMap();
        Map faultyOperationsMap = new HashMap();
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
        // First thing is to populate the message map with the messages to
        // process.
        ////////////////////////////////////////////////////////////////////////////////////////////////////

        //we really need to do this for a single porttype!
        List operations = porttype.getOperations();
        Operation op;
        for (int k = 0; k < operations.size(); k++) {
            op = (Operation) operations.get(k);
            Input input = op.getInput();
            Message message;
            if (input != null) {
                message = input.getMessage();
                messagesMap.put(message.getQName(), message);
                inputOperationsMap.put(op.getName(), message);
            }

            Output output = op.getOutput();
            if (output != null) {
                message = output.getMessage();
                messagesMap.put(message.getQName(), message);
                outputOperationsMap.put(op.getName(), message);
            }

            Map faultMap = op.getFaults();
            if (faultMap != null && faultMap.size() > 0) {
                Iterator keys = faultMap.keySet().iterator();
                while (keys.hasNext()) {
                    Object key = keys.next();
                    Fault fault = (Fault) faultMap.get(key);
                    if (fault != null) {
                        message = fault.getMessage();
                        messagesMap.put(message.getQName(), message);
                        faultyOperationsMap.put(key, message);
                    }
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //check whether there are messages that are wrappable. If there are no
        // messages that are wrappable we'll
        //just return null and endup this process. However we need to take the
        // force flag into account here
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        QName[] keys;
        if (forceWrapping) {
            //just take all the messages and wrap them, we've been told to
            // force wrapping!
            keys = (QName[]) messagesMap.keySet().toArray(
                    new QName[messagesMap.size()]);
        } else {
            //
            QName[] allKeys = (QName[]) messagesMap.keySet().toArray(
                    new QName[messagesMap.size()]);
            List wrappableMessageNames = new ArrayList();
            boolean noMessagesTobeProcessed = true;
            for (int i = 0; i < allKeys.length; i++) {
                if (findWrapppable((Message) messagesMap.get(allKeys[i]))) {
                    noMessagesTobeProcessed = false;
                    //add that message to the list
                    wrappableMessageNames.add(allKeys[i]);
                }
            }
            if (noMessagesTobeProcessed) {
                return null;
            }

            keys = (QName[]) wrappableMessageNames
                    .toArray(new QName[wrappableMessageNames.size()]);
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Now we have the message list to process - Process the whole list of
        // messages at once
        // since we need to generate one single schema
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        List resolvedMessageQNames = new ArrayList();
        //find the xsd prefix
        String xsdPrefix = findSchemaPrefix();
        Message wsdl4jMessage;
        //DOM document that will be the ultimate creator
        Document document = getDOMDocumentBuilder().newDocument();
        for (int i = 0; i < keys.length; i++) {
            wsdl4jMessage = (Message) messagesMap.get(keys[i]);
            //No need to check the wrappable,

            //This message is wrappabel. However we need to see whether the
            // message is already
            //resolved!
            if (!resolvedMessageQNames.contains(wsdl4jMessage.getQName())) {
                //This message has not been touched before!. So we can go ahead
                // now
                Map parts = wsdl4jMessage.getParts();
                //add the complex type
                String name = wsdl4jMessage.getQName().getLocalPart();
                Element newComplexType = document.createElementNS(
                        XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                        + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);
                newComplexType.setAttribute(XSD_NAME, name);

                Element cmplxContentSequence = document.createElementNS(
                        XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                        + XML_SCHEMA_SEQUENCE_LOCAL_NAME);
                Element child;
                Iterator iterator = parts.keySet().iterator();
                while (iterator.hasNext()) {
                    Part part = (Part) parts.get(iterator.next());
                    //the part name
                    String elementName = part.getName();
                    boolean isTyped = true;
                    //the type name
                    QName schemaTypeName;
                    if (part.getTypeName() != null) {
                        schemaTypeName = part.getTypeName();
                    } else if (part.getElementName() != null) {
                        schemaTypeName = part.getElementName();
                        isTyped = false;
                    } else {
                        throw new RuntimeException(" Unqualified Message part!");
                    }

                    child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI,
                            xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);

                    String prefix;
                    if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName
                            .getNamespaceURI())) {
                        prefix = xsdPrefix;
                    } else {
                        //this schema is a third party one. So we need to have
                        // an import statement in our generated schema
                        String uri = schemaTypeName.getNamespaceURI();
                        if (!namespaceImportsMap.containsKey(uri)) {
                            //create Element for namespace import
                            Element namespaceImport = document.createElementNS(
                                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                    + XML_SCHEMA_IMPORT_LOCAL_NAME);
                            namespaceImport.setAttribute("namespace", uri);
                            //add this to the map
                            namespaceImportsMap.put(uri, namespaceImport);
                            //we also need to associate this uri with a prefix
                            // and include that prefix
                            //in the schema's namspace declarations. So add
                            // theis particular namespace to the
                            //prefix map as well
                            prefix = getTemporaryNamespacePrefix();
                            namespacePrefixMap.put(uri, prefix);
                        } else {
                            //this URI should be already in the namspace prefix
                            // map
                            prefix = (String) namespacePrefixMap.get(uri);
                        }

                    }
                    // If it's from a type the element we need to add a name and
                    // the type
                    //if not it's the element reference
                    if (isTyped) {
                        child.setAttribute(XSD_NAME, elementName);
                        child.setAttribute(XSD_TYPE, prefix + ":"
                                + schemaTypeName.getLocalPart());
                    } else {
                        child.setAttribute(XSD_REF, prefix + ":"
                                + schemaTypeName.getLocalPart());
                    }
                    cmplxContentSequence.appendChild(child);
                }
                newComplexType.appendChild(cmplxContentSequence);
                //add this newly created complextype to the list
                complexTypeElementsMap.put(wsdl4jMessage.getQName(),
                        newComplexType);
                resolvedMessageQNames.add(wsdl4jMessage.getQName());
            }

        }

        Element elementDeclaration;

        //loop through the input op map and generate the elements
        String[] inputOperationtNames = (String[]) inputOperationsMap.keySet()
                .toArray(new String[inputOperationsMap.size()]);
        for (int j = 0; j < inputOperationtNames.length; j++) {
            String inputOpName = inputOperationtNames[j];
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                    + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, inputOpName);

            String typeValue = ((Message) inputOperationsMap.get(inputOpName))
                    .getQName().getLocalPart();
            elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
                    + typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(inputOpName, new QName(
                    targetNamespaceUri, inputOpName, AXIS2WRAPPED));
        }

        //loop through the output op map and generate the elements
        String[] outputOperationtNames = (String[]) outputOperationsMap
                .keySet().toArray(new String[outputOperationsMap.size()]);
        for (int j = 0; j < outputOperationtNames.length; j++) {

            String baseoutputOpName = outputOperationtNames[j];
            String outputOpName = baseoutputOpName + "Response";
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                    + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, outputOpName);
            String typeValue = ((Message) outputOperationsMap
                    .get(baseoutputOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
                    + typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(outputOpName, new QName(
                    targetNamespaceUri, outputOpName, AXIS2WRAPPED));

        }

        //loop through the faultoutput op map and generate the elements
        String[] faultyOperationtNames = (String[]) faultyOperationsMap
                .keySet().toArray(new String[faultyOperationsMap.size()]);
        for (int j = 0; j < faultyOperationtNames.length; j++) {

            String baseFaultOpName = faultyOperationtNames[j];
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                    + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, baseFaultOpName);
            String typeValue = ((Message) faultyOperationsMap
                    .get(baseFaultOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
                    + typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(baseFaultOpName, new QName(
                    targetNamespaceUri, baseFaultOpName, AXIS2WRAPPED));

        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Now we are done with processing the messages and generating the right
        // schema object model
        // time to write out the schema
        //////////////////////////////////////////////////////////////////////////////////////////////

        Element schemaElement = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                + XML_SCHEMA_LOCAL_NAME);

        //loop through the namespace declarations first
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
                .keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                    "xmlns:" + namespacePrefixMap.get(s).toString(), s);

        }

        //add the targetNamespace

        schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
                XMLNS_AXIS2WRAPPED, targetNamespaceUri);
        schemaElement.setAttribute(XSD_TARGETNAMESPACE, targetNamespaceUri);
        schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT, XSD_UNQUALIFIED);

        Element[] namespaceImports = (Element[]) namespaceImportsMap.values()
                .toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            schemaElement.appendChild(namespaceImports[i]);

        }

        Element[] complexTypeElements = (Element[]) complexTypeElementsMap
                .values().toArray(new Element[complexTypeElementsMap.size()]);
        for (int i = 0; i < complexTypeElements.length; i++) {
            schemaElement.appendChild(complexTypeElements[i]);

        }

        Element[] elementDeclarations = (Element[]) elementElementsList
                .toArray(new Element[elementElementsList.size()]);
        for (int i = 0; i < elementDeclarations.length; i++) {
            schemaElement.appendChild(elementDeclarations[i]);

        }

        return schemaElement;
    }

    /**
     * The intention of this procedure is to process the imports. When
     * processing the imports the imported documents will be populating the
     * items in the main document recursivley
     *
     * @param wsdl4JDefinition
     */
    private void processImports(Definition wsdl4JDefinition) {
        Map wsdlImports = wsdl4JDefinition.getImports();

        if (null != wsdlImports && !wsdlImports.isEmpty()) {
            Collection importsCollection = wsdlImports.values();
            for (Iterator iterator = importsCollection.iterator(); iterator
                    .hasNext();) {
                Vector values = (Vector) iterator.next();
                for (int i = 0; i < values.size(); i++) {
                    Import wsdlImport = (Import) values.elementAt(i);

                    if (wsdlImport.getDefinition() != null) {
                        Definition importedDef = wsdlImport.getDefinition();
                        if (importedDef != null) {
                            processImports(importedDef);

                            //copy types
                            Types t = importedDef.getTypes();
                            List typesList = t.getExtensibilityElements();
                            for (int j = 0; j < typesList.size(); j++) {
                                Types types = wsdl4JDefinition.getTypes();
                                if (types == null) {
                                    types = wsdl4JDefinition.createTypes();
                                    wsdl4JDefinition.setTypes(types);
                                }
                                types
                                        .addExtensibilityElement((ExtensibilityElement) typesList
                                                .get(j));

                            }

                            //add messages
                            Map messagesMap = importedDef.getMessages();
                            wsdl4JDefinition.getMessages().putAll(messagesMap);

                            //add portypes
                            Map porttypeMap = importedDef.getPortTypes();
                            wsdl4JDefinition.getPortTypes().putAll(porttypeMap);

                            //add bindings
                            Map bindingMap = importedDef.getBindings();
                            wsdl4JDefinition.getBindings().putAll(bindingMap);

                        }

                    }
                }
            }
        }
    }

    private XmlSchema getXMLSchema(Element element) {
        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        Map nsMap = axisService.getNameSpacesMap();
        Iterator keys = nsMap.keySet().iterator();
        String key;
        while (keys.hasNext()) {
            key = (String) keys.next();
            schemaCollection.mapNamespace(key, (String) nsMap.get(key));
        }
        return schemaCollection.read(element);
    }

    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        Document doc;
        try {
            doc = XMLUtils.newDocument(in);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error", e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error", e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error", e);
        }

        return reader.readWSDL(null, doc);
    }

    /**
     * Get the Extensible elements form wsdl4jExtensibleElements
     * <code>Vector</code> if any and copy them to <code>Component</code>
     *
     * @param wsdl4jExtensibleElements
     * @param description                   where is the ext element (port , portype , biding)
     * @param wsdl4jDefinition
     * @param originOfExtensibilityElements - this will indicate the place this extensibility element
     *                                      came from.
     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Definition wsdl4jDefinition, AxisDescription description, String originOfExtensibilityElements) {
        Iterator iterator = wsdl4jExtensibleElements.iterator();
        ExtensionFactoryImpl extensionFactory = new ExtensionFactoryImpl();
        while (iterator.hasNext()) {

            ExtensibilityElement wsdl4jElement = (ExtensibilityElement) iterator
                    .next();

            if (wsdl4jElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) (wsdl4jElement);

                //look for the SOAP 1.2 stuff here. WSDL4j does not understand
                // SOAP 1.2 things
                if (ExtensionConstants.SOAP_12_OPERATION.equals(unknown
                        .getElementType())) {
                    Element element = unknown.getElement();
                    if (description instanceof AxisOperation) {
                        AxisOperation axisOperation = (AxisOperation) description;
                        String style = element.getAttribute("style");
                        if (style != null) {
                            axisOperation.setStyle(style);
                        }
                        axisOperation.setSoapAction(element.getAttribute("soapAction"));
                    }
                } else if (ExtensionConstants.SOAP_12_HEADER.equals(unknown
                        .getElementType())) {
                    //TODO : implement thid
                } else if (ExtensionConstants.SOAP_12_BINDING.equals(unknown
                        .getElementType())) {
                    style = unknown.getElement().getAttribute("style");
                    axisService.setSoapNsUri(wsdl4jElement.getElementType().getNamespaceURI());
                } else if (ExtensionConstants.SOAP_12_ADDRESS.equals(unknown
                        .getElementType())) {
                    axisService.setEndpoint(unknown.getElement().getAttribute("location"));
                } else if (ExtensionConstants.POLICY.equals(unknown
                        .getElementType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    Policy policy = policyReader.readPolicy(unknown.getElement());

                    addExtensibilityElementsToAxisDescription(description, originOfExtensibilityElements, policy);

                } else if (ExtensionConstants.POLICY_REFERENCE.equals(unknown.getElementType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    PolicyReference policyRef = policyReader.readPolicyReference(unknown.getElement());
                    addExtensibilityElementsToAxisDescription(description, originOfExtensibilityElements, policyRef);


                } else {
                    //TODO : we are ignored that.
                }

            } else if (wsdl4jElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) wsdl4jElement;
                axisService.setEndpoint(soapAddress.getLocationURI());
            } else if (wsdl4jElement instanceof Schema) {
                Schema schema = (Schema) wsdl4jElement;
                //schema.getDocumentBaseURI()
                //populate the imported schema stack
                Stack schemaStack = new Stack();
                //recursivly load the schema elements. The best thing is to
                // push these into
                //a stack and then pop from the other side
                pushSchemaElement(schema, schemaStack);
                org.apache.wsdl.extensions.Schema schemaExtensibilityElement = (org.apache.wsdl.extensions.Schema) extensionFactory
                        .getExtensionElement(schema.getElementType());
                schemaExtensibilityElement.setElement(schema.getElement());
                schemaExtensibilityElement.setImportedSchemaStack(schemaStack);
                Boolean required = schema.getRequired();
                if (null != required) {
                    schemaExtensibilityElement.setRequired(required
                            .booleanValue());
                }
                //set the name of this Schema element
                //todo this needs to be fixed
                if (schema.getDocumentBaseURI() != null) {
                    schemaExtensibilityElement.setName(new QName("", schema
                            .getDocumentBaseURI()));
                }
                axisService.setSchema(getXMLSchema(schemaExtensibilityElement
                        .getElement()));
            } else if (SOAPConstants.Q_ELEM_SOAP_OPERATION.equals(wsdl4jElement
                    .getElementType())) {
                SOAPOperation soapOperation = (SOAPOperation) wsdl4jElement;
                if (description instanceof AxisOperation) {
                    AxisOperation axisOperation = (AxisOperation) description;
                    if (soapOperation.getStyle() != null) {
                        axisOperation.setStyle(soapOperation.getStyle());
                    }
                    axisOperation.setSoapAction(soapOperation.getSoapActionURI());
                }
            } else if (SOAPConstants.Q_ELEM_SOAP_HEADER.equals(wsdl4jElement
                    .getElementType())) {
                SOAPHeader soapHeader = (SOAPHeader) wsdl4jElement;
                org.apache.wsdl.extensions.SOAPHeader soapHeaderExtensibilityElement = (org.apache.wsdl.extensions.SOAPHeader) extensionFactory
                        .getExtensionElement(soapHeader.getElementType());
                soapHeaderExtensibilityElement.setNamespaceURI(soapHeader
                        .getNamespaceURI());
                soapHeaderExtensibilityElement.setUse(soapHeader.getUse());
                Boolean required = soapHeader.getRequired();
                if (null != required) {
                    soapHeaderExtensibilityElement.setRequired(required
                            .booleanValue());
                }
                if (null != wsdl4jDefinition) {
                    //find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    soapHeaderExtensibilityElement.setElement(msgPart
                            .getElementName());
                }
                soapHeaderExtensibilityElement.setMessage(soapHeader
                        .getMessage());

                soapHeaderExtensibilityElement.setPart(soapHeader.getPart());
                if (description instanceof AxisMessage) {
                    ((AxisMessage) description).addSopaHeader(soapHeaderExtensibilityElement);
                }
            } else if (SOAPConstants.Q_ELEM_SOAP_BINDING.equals(wsdl4jElement
                    .getElementType())) {
                SOAPBinding soapBinding = (SOAPBinding) wsdl4jElement;
                style = soapBinding.getStyle();
                axisService.setSoapNsUri(soapBinding.getElementType().getNamespaceURI());
            }
        }
    }

    private void addExtensibilityElementsToAxisDescription(AxisDescription description, String originOfExtensibilityElements, Policy policy) {
        if (description instanceof AxisService) {
            if (SERVICE.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.SERVICE_POLICY, policy);

            } else if (PORT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.PORT_POLICY, policy);

            } else if (BINDING.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.BINDING_POLICY, policy);
            }

        } else if (description instanceof AxisOperation) {

            if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.OPERATION_POLICY, policy);

            } else if (BINDING_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.BINDING_POLICY, policy);
            }

        } else if (description instanceof AxisMessage) {

            if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.INPUT_POLICY, policy);

            } else if (BINDING_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.BINDING_INPUT_POLICY, policy);

            } else if (PORT_TYPE_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.OUTPUT_POLICY, policy);

            } else if (BINDING_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(PolicyInclude.BINDING_OUTPUT_POLICY, policy);
            }

            //TODO Faults ..

        }
    }

    private void addExtensibilityElementsToAxisDescription(AxisDescription description, String originOfExtensibilityElements, PolicyReference policyRefElement) {
        if (description instanceof AxisService) {
            if (SERVICE.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.SERVICE_POLICY, policyRefElement);

            } else if (PORT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.PORT_POLICY, policyRefElement);

            } else if (BINDING.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.BINDING_POLICY, policyRefElement);
            }

        } else if (description instanceof AxisOperation) {

            if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.OPERATION_POLICY, policyRefElement);

            } else if (BINDING_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.BINDING_POLICY, policyRefElement);
            }

        } else if (description instanceof AxisMessage) {

            if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.INPUT_POLICY, policyRefElement);

            } else if (BINDING_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.BINDING_INPUT_POLICY, policyRefElement);

            } else if (PORT_TYPE_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.OUTPUT_POLICY, policyRefElement);

            } else if (BINDING_OPERATION_OUTPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(PolicyInclude.BINDING_OUTPUT_POLICY, policyRefElement);
            }

            //TODO Faults ..

        }
    }

    private void pushSchemaElement(Schema originalSchema, Stack stack) {
        if (originalSchema == null) {
            return;
        }
        stack.push(originalSchema.getElement());
        Map map = originalSchema.getImports();
        Collection values;
        if (map != null && map.size() > 0) {
            values = map.values();
            for (Iterator iterator = values.iterator(); iterator.hasNext();) {
                //recursively add the schema's
                Vector v = (Vector) iterator.next();
                for (int i = 0; i < v.size(); i++) {
                    pushSchemaElement(((SchemaImport) v.get(i))
                            .getReferencedSchema(), stack);
                }

            }
        }
    }

    private boolean findWrapForceable(Binding binding) {
        List extElements = binding.getExtensibilityElements();
        for (int i = 0; i < extElements.size(); i++) {
            if (extElements.get(i) instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding) extElements.get(i);
                if ("rpc".equals(soapBinding.getStyle())) {
                    //oops - we've found a SOAPBinding that has a rpc style
                    //we better force the wrapping then
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Finds whether a given message is wrappable
     */
    private boolean findWrapppable(Message message) {

        // ********************************************************************************************
        // Note
        // We will not use the binding to set the wrappable/unwrappable state
        // here. instead we'll look at the
        // Messages for the following features
        // 1. Messages with multiple parts -> We have no choice but to wrap
        // 2. Messages with one part having a type attribute -> Again we have no
        // choice but to wrap

        // ********************************************************************************************
        Map partsMap = message.getParts();
        Iterator parts = partsMap.values().iterator();
        boolean wrappable = partsMap.size() > 1;
        Part part;
        while (!wrappable && parts.hasNext()) {
            part = (Part) parts.next();
            wrappable = (part.getTypeName() != null) || wrappable;
        }

        return wrappable;
    }

    /**
     * Find the XML schema prefix
     */
    private String findSchemaPrefix() {
        String xsdPrefix = null;
        Map declaredNameSpaces = axisService.getNameSpacesMap();
        if (declaredNameSpaces.containsValue(XMLSCHEMA_NAMESPACE_URI)) {
            //loop and find the prefix
            Iterator it = declaredNameSpaces.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = (String) it.next();
                if (XMLSCHEMA_NAMESPACE_URI.equals(declaredNameSpaces.get(key))) {
                    xsdPrefix = key;
                    break;
                }
            }
        } else {
            xsdPrefix = XMLSCHEMA_NAMESPACE_PREFIX; //default prefix
        }
        return xsdPrefix;
    }

    /**
     * Utility method that returns a DOM Builder
     */
    private DocumentBuilder getDOMDocumentBuilder() {
        DocumentBuilder documentBuilder;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        return documentBuilder;
    }

    /**
     */
    private String getTemporaryNamespacePrefix() {
        return "ns" + nsCount++;
    }

    private String getMEP(Operation operation) {
        OperationType operationType = operation.getStyle();
        if (isServerSide) {
            if (null != operationType) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE))
                    return WSDLConstants.MEP_URI_IN_OUT;

                if (operationType.equals(OperationType.ONE_WAY))
                    return WSDLConstants.MEP_URI_IN_ONLY;

                if (operationType.equals(OperationType.NOTIFICATION))
                    return WSDLConstants.MEP_URI_OUT_ONLY;

                if (operationType.equals(OperationType.SOLICIT_RESPONSE))
                    return WSDLConstants.MEP_URI_OUT_IN;
                throw new WSDLProcessingException("Cannot Determine the MEP");
            }
        } else {
            if (null != operationType) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE))
                    return WSDLConstants.MEP_URI_OUT_IN;

                if (operationType.equals(OperationType.ONE_WAY))
                    return WSDLConstants.MEP_URI_OUT_ONLY;

                if (operationType.equals(OperationType.NOTIFICATION))
                    return WSDLConstants.MEP_URI_IN_ONLY;

                if (operationType.equals(OperationType.SOLICIT_RESPONSE))
                    return WSDLConstants.MEP_URI_IN_OUT;
                throw new WSDLProcessingException("Cannot Determine the MEP");
            }
        }
        throw new WSDLProcessingException("Cannot Determine the MEP");
    }

    private void populatePolicyInclude(int location, AxisDescription description) {
        //TODO : Sanka pls fix this
//        PolicyInclude policyInclude = description.getPolicyInclude();
//        ArrayList wsdlExtElements = description.getWsdlExtElements();
//
//        if (wsdlExtElements != null) {
//            Object wsdlExtElement;
//            Object policyElement;
//
//            for (Iterator iterator = wsdlExtElements.iterator(); iterator
//                    .hasNext();) {
//                wsdlExtElement = iterator.next();
//
//                if (wsdlExtElement instanceof PolicyExtensibilityElement) {
//                    policyElement = ((PolicyExtensibilityElement) wsdlExtElement)
//                            .getPolicyElement();
//
//                    if (policyElement instanceof Policy) {
//                        policyInclude.addPolicyElement(location,
//                                (Policy) policyElement);
//
//                    } else {
//                        policyInclude.addPolicyRefElement(location,
//                                (PolicyReference) policyElement);
//                    }
//                }
//            }

//    }
    }
}
