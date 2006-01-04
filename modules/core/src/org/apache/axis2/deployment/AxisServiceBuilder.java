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

package org.apache.axis2.deployment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
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
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.util.XMLUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.OMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.ibm.wsdl.util.xml.DOM2Writer;

/**
 * AxisServiceBuilder builds an AxisService using a WSDL document which is feed
 * as a javax.wsdl.Definition or as an InputStream. If there are multiple
 * javax.wsdl.Service elements in the WSDL, the first is picked.
 */
public class AxisServiceBuilder {

    private static final String XMLSCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

    private static final String XMLSCHEMA_NAMESPACE_PREFIX = "xs";

    private static final String XML_SCHEMA_LOCAL_NAME = "schema";

    private static final String XML_SCHEMA_SEQUENCE_LOCAL_NAME = "sequence";

    private static final String XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME = "complexType";

    private static final String XML_SCHEMA_ELEMENT_LOCAL_NAME = "element";

    private static final String XML_SCHEMA_IMPORT_LOCAL_NAME = "import";

    private static final String XSD_NAME = "name";

    private static final String XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";

    private static final String XSD_UNQUALIFIED = "unqualified";

    private static final String XSD_TARGETNAMESPACE = "targetNamespace";

    private static final String XSD_TYPE = "type";

    private static final String XSD_REF = "ref";

    private static final String AXIS2WRAPPED = "axis2wrapped";

    private static final String XMLNS_AXIS2WRAPPED = "xmlns:axis2wrapped";

    private int nsCount = 1;

    public AxisService getAxisService(InputStream wsdlInputStream)
            throws WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);

        Document doc;

        try {
            doc = XMLUtils.newDocument(wsdlInputStream);

        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error", e);

        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error", e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error", e);
        }

        Definition wsdlDefinition = reader.readWSDL(null, doc);
        return getAxisService(wsdlDefinition);
    }

    public AxisService getAxisService(Definition wsdl4jDefinitions)
            throws WSDLProcessingException {

        AxisService axisService = new AxisService();
        Map services = wsdl4jDefinitions.getServices();

        if (services.isEmpty()) {
            throw new WSDLProcessingException("no Service element is found");
        }

        Iterator serviceIterator = services.values().iterator();
        Service wsdl4jService = (Service) serviceIterator.next();

        // setting the name
        axisService.setName(wsdl4jService.getQName().getLocalPart());

        /////////////////// adding Policies ////////////////////////////

        PolicyInclude policyInclude = new PolicyInclude();

        List wsdlPolicies = getPoliciesAsExtElements(wsdl4jDefinitions
                .getExtensibilityElements());
        Iterator wsdlPolicyIterator = wsdlPolicies.iterator();

        while (wsdlPolicyIterator.hasNext()) {
            Policy wsdlPolicy = (Policy) wsdlPolicyIterator.next();

            if (wsdlPolicy.getPolicyURI() != null) {
                policyInclude.registerPolicy(wsdlPolicy);
            }
        }

        axisService.setPolicyInclude(policyInclude);

        //////////////////////////////////////////////////////////////////

        // setting the schema
        Types types = wsdl4jDefinitions.getTypes();

        if (types != null) {
            Iterator extElements = types.getExtensibilityElements().iterator();
            ExtensibilityElement extElement;

            while (extElements.hasNext()) {
                extElement = (ExtensibilityElement) extElements.next();

                if (extElement instanceof Schema) {
                    Element schemaElement = ((Schema) extElement).getElement();
                    axisService.setSchema(getXMLSchema(schemaElement));
                }
            }
        }

        HashMap resolvedRPCWrapperElements = new HashMap();
        XmlSchema xmlSchemaForWrappedElements = generateWrapperSchema(
                wsdl4jDefinitions, resolvedRPCWrapperElements);

        if (xmlSchemaForWrappedElements != null) {
            axisService.setSchema(xmlSchemaForWrappedElements);
        }

        // getting the port of the service with SOAP binding
        Iterator ports = wsdl4jService.getPorts().values().iterator();

        if (!ports.hasNext()) {
            throw new WSDLProcessingException(
                    "atleast one Port should be specified");
        }

        Port wsdl4jPort = (Port) ports.next();
        Binding wsdl4jBinding = wsdl4jPort.getBinding();
        PortType wsdl4jPortType = wsdl4jBinding.getPortType();

        /////////////// Adding policies //////////////////////////////////

        List axisServicePolicies;

        // wsdl:Service
        axisServicePolicies = getPoliciesAsExtElements(wsdl4jService
                .getExtensibilityElements());
        addPolicyElements(PolicyInclude.SERVICE_POLICY, axisServicePolicies,
                policyInclude);

        // wsdl:Port
        axisServicePolicies = getPoliciesAsExtElements(wsdl4jPort
                .getExtensibilityElements());
        addPolicyElements(PolicyInclude.PORT_POLICY, axisServicePolicies,
                policyInclude);

        // wsdl:PortType
        axisServicePolicies = getPoliciesAsExtAttributes(wsdl4jPortType
                .getExtensionAttributes());
        addPolicyElements(PolicyInclude.PORT_TYPE_POLICY, axisServicePolicies,
                policyInclude);

        // TODO wsdl:Binding
        axisServicePolicies = getPoliciesAsExtElements(wsdl4jBinding
                .getExtensibilityElements());
        addPolicyElements(PolicyInclude.BINDING_POLICY, axisServicePolicies,
                policyInclude);

        //////////////////////////////////////////////////////////////////

        Iterator wsdl4jOperations = wsdl4jPortType.getOperations().iterator();

        while (wsdl4jOperations.hasNext()) {
            Operation wsdl4jOperation = (Operation) wsdl4jOperations.next();
            BindingOperation wsdl4jBindingOperation = wsdl4jBinding
                    .getBindingOperation(wsdl4jOperation.getName(), null, null);

            AxisOperation axisOperation;

            try {
                axisOperation = AxisOperationFactory
                        .getAxisOperation(getMessageExchangePattern(wsdl4jOperation));

                // setting parent
                axisOperation.setParent(axisService);
                // setting operation name
                axisOperation.setName(new QName(wsdl4jOperation.getName()));

                ////////////////adding Policy //////////////////////////////

                PolicyInclude operationPolicyInclude = new PolicyInclude(
                        axisService.getPolicyInclude());

                List operationPolicies;

                // wsdl:PortType -> wsdl:Operation
                operationPolicies = getPoliciesAsExtElements(wsdl4jOperation
                        .getExtensibilityElements());
                addPolicyElements(PolicyInclude.OPERATION_POLICY,
                        operationPolicies, operationPolicyInclude);

                // wsdl:Binding -> wsdl:Operation
                operationPolicies = getPoliciesAsExtElements(wsdl4jBindingOperation
                        .getExtensibilityElements());
                addPolicyElements(PolicyInclude.BINDING_OPERATOIN_POLICY,
                        operationPolicies, operationPolicyInclude);

                axisOperation.setPolicyInclude(operationPolicyInclude);

                ///////////////////////////////////////////////////////////////

                // Input
                Input wsdl4jInput = wsdl4jOperation.getInput();
                BindingInput wsdl4jBindingInput = wsdl4jBindingOperation
                        .getBindingInput();
                Message wsdl4jInputMessage = wsdl4jInput.getMessage();

                AxisMessage axisInputMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                //////////////////// adding Policies /////////////////////////

                PolicyInclude inputPolicyInclue = new PolicyInclude(
                        axisOperation.getPolicyInclude());

                List inputMessagePolicies;

                // wsdl:PortType -> wsdl:Operation -> wsdl:Input
                inputMessagePolicies = getPoliciesAsExtAttributes(wsdl4jInput
                        .getExtensionAttributes());
                addPolicyElements(PolicyInclude.INPUT_POLICY,
                        inputMessagePolicies, inputPolicyInclue);

                // wsdl:Binding -> wsdl:Operation -> wsdl:Input
                inputMessagePolicies = getPoliciesAsExtElements(wsdl4jBindingInput
                        .getExtensibilityElements());
                addPolicyElements(PolicyInclude.BINDING_INPUT_POLICY,
                        inputMessagePolicies, inputPolicyInclue);

                // wsdl:Message
                inputMessagePolicies = getPoliciesAsExtElements(wsdl4jInputMessage
                        .getExtensibilityElements());
                addPolicyElements(PolicyInclude.MESSAGE_POLICY,
                        inputMessagePolicies, inputPolicyInclue);

                axisInputMessage.setPolicyInclude(policyInclude);

                ///////////////////////////////////////////////////////////////

                // setting the element qname
                axisInputMessage.setElementQName(generateReferenceQname(
                        new QName(wsdl4jPortType.getQName().getNamespaceURI(),
                                wsdl4jOperation.getName()), wsdl4jInputMessage,
                        findWrapppable(wsdl4jInputMessage),
                        resolvedRPCWrapperElements));

                Output wsdl4jOutput = wsdl4jOperation.getOutput();
                BindingOutput wsdl4jBindingOutput = wsdl4jBindingOperation
                        .getBindingOutput();

                if (wsdl4jOutput != null) {

                    AxisMessage axisOutputMessage = axisOperation
                            .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

                    Message wsdl4jOutputMessage = wsdl4jOutput.getMessage();

                    /////////////// adding Policies ///////////////////////////

                    PolicyInclude outputPolicyInclude = new PolicyInclude(
                            axisService.getPolicyInclude());
                    List outputPolicies;

                    //wsdl:Output
                    outputPolicies = getPoliciesAsExtAttributes(wsdl4jOutput
                            .getExtensionAttributes());
                    addPolicyElements(PolicyInclude.OUTPUT_POLICY,
                            outputPolicies, outputPolicyInclude);

                    // BindingOutput
                    outputPolicies = getPoliciesAsExtElements(wsdl4jBindingOutput
                            .getExtensibilityElements());
                    addPolicyElements(PolicyInclude.BINDING_OUTPUT_POLICY,
                            outputPolicies, outputPolicyInclude);

                    //wsdl:Message
                    outputPolicies = getPoliciesAsExtElements(wsdl4jOutputMessage
                            .getExtensibilityElements());
                    addPolicyElements(PolicyInclude.MESSAGE_POLICY,
                            outputPolicies, outputPolicyInclude);

                    axisOutputMessage.setPolicyInclude(outputPolicyInclude);

                    ///////////////////////////////////////////////////////////

                    // setting the element qname
                    axisOutputMessage.setElementQName(generateReferenceQname(
                            new QName(wsdl4jOperation.getName()),
                            wsdl4jOutputMessage,
                            findWrapppable(wsdl4jOutputMessage),
                            resolvedRPCWrapperElements));
                }

            } catch (AxisFault axisFault) {
                throw new WSDLProcessingException(axisFault.getMessage());
            }
            axisService.addOperation(axisOperation);
        }
        return axisService;
    }

    private int getMessageExchangePattern(Operation wsdl4jOperation) {

        if (wsdl4jOperation.getOutput() == null) {
            return WSDLConstants.MEP_CONSTANT_IN_ONLY;

        } else {
            return WSDLConstants.MEP_CONSTANT_IN_OUT;
        }
    }

    private XmlSchema getXMLSchema(Element element) {
        return (new XmlSchemaCollection()).read(element);
    }

    private Document getDOMDocument() {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(true);
            return fac.newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException ex) {
            throw new WSDLProcessingException(ex.getMessage());
        }
    }

    private String getTempPrefix() {
        return "ns" + nsCount++;
    }

    private XmlSchema generateWrapperSchema(Definition wsdl4jDefinition,
            Map resolvedRpcWrappedElementMap) {

        //TODO check me
        Map declaredNameSpaces = wsdl4jDefinition.getNamespaces();

        //loop through the messages. We'll populate this map with the relevant
        // messages
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
        ///////////////////////
        String targetNamespaceUri = wsdl4jDefinition.getTargetNamespace();
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        // First thing is to populate the message map with the messages to
        // process.
        ////////////////////////////////////////////////////////////////////////////////////////////////////
        Map porttypeMap = wsdl4jDefinition.getPortTypes();
        PortType[] porttypesArray = (PortType[]) porttypeMap.values().toArray(
                new PortType[porttypeMap.size()]);
        for (int j = 0; j < porttypesArray.length; j++) {
            //we really need to do this for a single porttype!
            List operations = porttypesArray[j].getOperations();
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
                //todo also handle the faults here
            }

        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //check whether there are messages that are wrappable. If there are no
        // messages that are wrappable we'll
        //just return null and endup this process
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        QName[] keys = (QName[]) messagesMap.keySet().toArray(
                new QName[messagesMap.size()]);
        boolean noMessagesTobeProcessed = true;
        for (int i = 0; i < keys.length; i++) {
            if (findWrapppable((Message) messagesMap.get(keys[i]))) {
                noMessagesTobeProcessed = false;
                break;
            }
        }

        if (noMessagesTobeProcessed) {
            return null;
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Now we have the message list to process - Process the whole list of
        // messages at once
        //since
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        List resolvedMessageQNames = new ArrayList();
        //find the xsd prefix
        String xsdPrefix = findSchemaPrefix(declaredNameSpaces);
        Message wsdl4jMessage;
        //DOM document that will be the ultimate creator
        Document document = getDOMDocument();
        for (int i = 0; i < keys.length; i++) {
            wsdl4jMessage = (Message) messagesMap.get(keys[i]);
            //No need to chack the wrappable

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
                            prefix = getTempPrefix();
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

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Now we are done with processing the messages and generating the right
        // schema
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

        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();
        Iterator prefixes = declaredNameSpaces.keySet().iterator();

        while (prefixes.hasNext()) {
            String prefix = (String) prefixes.next();
            String u = (String) declaredNameSpaces.get(prefix);
            schemaCollection.mapNamespace(prefix, u);
        }
        return schemaCollection.read(schemaElement);
    }

    /**
     * 
     * 
     * @return
     */
    private boolean findWrapppable(Message message) {

        //********************************************************************************************
        //Note
        //We will not use the binding to set the wrappable/unwrappable state.
        // instead we'll look at the
        //Messages for the following features
        //1. Messages with multiple parts -> We have no choice but to wrap
        //2. Messages with at least one part having a type attribute -> Again
        // we have no choice but to
        //wrap

        //********************************************************************************************
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
    private String findSchemaPrefix(Map declaredNameSpaces) {
        String xsdPrefix = null;
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

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Generates a referenceQName
     * 
     * @param wsdl4jMessage
     * @return
     */
    private QName generateReferenceQname(QName outerName,
            Message wsdl4jMessage, boolean isWrappable,
            Map resolvedRpcWrappedElementMap) {
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

    private List getPoliciesAsExtElements(List extElementsList) {
        ArrayList policies = new ArrayList();

        Iterator extElements = extElementsList.iterator();
        OMPolicyReader reader = (OMPolicyReader) PolicyFactory
                .getPolicyReader(PolicyFactory.OM_POLICY_READER);
        Object extElement;

        while (extElements.hasNext()) {
            extElement = extElements.next();

            if (extElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement e = (UnknownExtensibilityElement) extElement;
                Element element = e.getElement();
                if (PolicyConstants.WS_POLICY_NAMESPACE_URI.equals(element
                        .getNamespaceURI())
                        && PolicyConstants.WS_POLICY.equals(element
                                .getLocalName())) {
                    policies.add(reader.readPolicy(getInputStream(element)));

                } else if (PolicyConstants.WS_POLICY_NAMESPACE_URI
                        .equals(element.getNamespaceURI())
                        && PolicyConstants.WS_POLICY_REFERENCE.equals(element
                                .getLocalName())) {

                    try {
                        policies.add(reader.readPolicyReference(

                        OMXMLBuilderFactory.createStAXOMBuilder(
                                OMAbstractFactory.getOMFactory(),
                                XMLInputFactory.newInstance()
                                        .createXMLStreamReader(
                                                getInputStream(element)))
                                .getDocumentElement()));

                    } catch (Exception ex) {
                        throw new WSDLProcessingException(ex.getMessage());
                    }
                }
            }
        }

        return policies;
    }

    private List getPoliciesAsExtAttributes(Map extAttributes) {
        ArrayList policies = new ArrayList();

        Object policyURIsString = extAttributes.get(new QName(
                PolicyConstants.WS_POLICY_NAMESPACE_URI, "PolicyURIs"));

        if (policyURIsString != null) {

            String[] policyURIs = ((QName) policyURIsString).getLocalPart()
                    .trim().split(" ");

            for (int i = 0; i < policyURIs.length; i++) {
                policies.add(new PolicyReference(policyURIs[i]));
            }
        }

        return policies;
    }

    private InputStream getInputStream(Element e) {
        StringWriter sw = new StringWriter();
        DOM2Writer.serializeAsXML(e, sw);
        return new ByteArrayInputStream(sw.toString().getBytes());
    }

    private void addPolicyElements(int type, List policyElements,
            PolicyInclude policyInclude) {
        Iterator policyElementIterator = policyElements.iterator();
        Object policyElement;

        while (policyElementIterator.hasNext()) {
            policyElement = policyElementIterator.next();

            if (policyElement instanceof Policy) {
                policyInclude.addPolicyElement(type, (Policy) policyElement);

            } else if (policyElement instanceof PolicyReference) {
                policyInclude.addPolicyRefElement(type,
                        (PolicyReference) policyElement);
            }
        }
    }

}
