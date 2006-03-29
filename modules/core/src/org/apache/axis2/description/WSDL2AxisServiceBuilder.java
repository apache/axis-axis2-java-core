package org.apache.axis2.description;

import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.extensions.DefaultExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.extensions.impl.ExtensionFactoryImpl;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.soap.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
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
    private static final String BOUND_INTERFACE_NAME = "BoundInterface";


    private static int nsCount = 0;
    private Map resolvedRpcWrappedElementMap = new HashMap();
    private static final String XSD_ELEMENT_FORM_DEFAULT = "elementFormDefault";
    private static final String XSD_UNQUALIFIED = "unqualified";


    private InputStream in;
    private AxisService axisService;
    private QName serviceName;
    private String portName;

    public WSDL2AxisServiceBuilder(InputStream in, QName serviceName, String portName) {
        this.in = in;
        this.serviceName = serviceName;
        this.portName = portName;
        this.axisService = new AxisService();
    }

    public AxisService populateService() throws Exception {
        Definition dif = readInTheWSDLFile(in);
        //setting target name space
        axisService.setTargetNamespace(dif.getTargetNamespace());
        //adding ns in the original WSDL
        axisService.setNameSpacesMap(dif.getNamespaces());
        //TODO : add extensiblity elements
        //scheam generation
        processImports(dif);
        Types wsdl4jTypes = dif.getTypes();
        if (null != wsdl4jTypes) {
            this.copyExtensibleElements(wsdl4jTypes.getExtensibilityElements(), dif
                    , axisService, AxisExtensiblityElementWrapper.PORT);
        }
        //////////////////(1.2) /////////////////////////////
        // create new Schema extensions element for wrapping
        Element[] schemaElements = generateWrapperSchema(dif);
        if (schemaElements != null && schemaElements.length > 0) {
            for (int i = 0; i < schemaElements.length; i++) {
                Element schemaElement = schemaElements[i];
                if (schemaElement != null) {
                    ExtensionFactoryImpl extensionFactory = new ExtensionFactoryImpl();
                    org.apache.wsdl.extensions.Schema schemaExtensibilityElement = (org.apache.wsdl.extensions.Schema) extensionFactory.getExtensionElement(
                            ExtensionConstants.SCHEMA);
                    schemaExtensibilityElement.setElement(schemaElement);
                    axisService.setSchema(getXMLSchema(schemaExtensibilityElement.getElement()));
                }
            }
        }
        processServices(dif);
        return axisService;
    }

    private void processServices(Definition dif) throws AxisFault {
        Map services = dif.getServices();
        Service service;
        Port port = null;
        if (serviceName != null) {
            service = (Service) services.get(serviceName);
            if (service == null) {
                throw new AxisFault("Service not found the WSDL " + serviceName.getLocalPart());
            }
        } else {
            if (services.size() > 0) {
                service = (Service) services.values().toArray()[0];
            } else {
                throw new AxisFault("No service element found in the WSDL");
            }
        }
        copyExtensibleElements(service.getExtensibilityElements(),
                dif, axisService,
                AxisExtensiblityElementWrapper.PORT);
        if (portName != null) {
            port = service.getPort(portName);
            if (port == null) {
                throw new AxisFault("No port found for the given name :" + portName);
            }
        } else {
            Map ports = service.getPorts();
            if (ports != null && ports.size() > 0) {
                port = (Port) ports.values().toArray()[0];
            }
        }
        if (port != null) {
            copyExtensibleElements(port.getExtensibilityElements(),
                    dif, axisService, AxisExtensiblityElementWrapper.PORT);
            Binding binding = port.getBinding();
            processBinding(binding, dif);
        }
        axisService.setName(service.getQName().getLocalPart());
    }

    private void processBinding(Binding binding, Definition dif) throws AxisFault {
        if (binding != null) {
            PortType portType = binding.getPortType();
            processPortType(portType, dif);
            //NOT yet  complte
        }
    }


    /**
     * Simply Copy information.
     *
     * @param wsdl4jPortType
     */
    // FIXME Evaluate a way of injecting features and priperties with a general
    // formatted input
    private void processPortType(PortType wsdl4jPortType,
                                 Definition dif) throws AxisFault {

        //Copy the Attribute information items
        //Copied with the Same QName so it will require no Query in Binding
        //Coping.
        Iterator wsdl4JOperationsIterator =
                wsdl4jPortType.getOperations().iterator();
        Operation wsdl4jOperation;
        while (wsdl4JOperationsIterator.hasNext()) {
            wsdl4jOperation = (Operation) wsdl4JOperationsIterator.next();
            copyExtensibleElements(wsdl4jOperation.getExtensibilityElements(),
                    dif, axisService,
                    AxisExtensiblityElementWrapper.PORT_TYPE);
            axisService.addOperation(populateOperations(wsdl4jOperation,
                    wsdl4jPortType.getQName().getNamespaceURI(),
                    dif));
        }
    }

    /////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Internal Component Copying ///////////////////
    public AxisOperation populateOperations(Operation wsdl4jOperation,
                                            String nameSpaceOfTheOperation,
                                            Definition dif) throws AxisFault {
        //Copy Name Attribute
        String MEP = getMEP(wsdl4jOperation);
        AxisOperation axisOperation = AxisOperationFactory.getOperationDescription(MEP);
        axisOperation.setName(new QName(nameSpaceOfTheOperation,
                wsdl4jOperation.getName()));

        Input wsdl4jInputMessage = wsdl4jOperation.getInput();
        QName wrappedInputName = axisOperation.getName();
        QName wrappedOutputName = new QName(
                wrappedInputName.getNamespaceURI(),
                wrappedInputName.getLocalPart() + "Response",
                wrappedInputName.getPrefix());
        if (null != wsdl4jInputMessage) {
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            Message message = wsdl4jInputMessage.getMessage();
            if (null != message) {
                inMessage.setElementQName(generateReferenceQname(
                        wrappedInputName
                        , message,
                        findWrapppable(message)));
                copyExtensibleElements(message.getExtensibilityElements(),
                        dif, inMessage, AxisExtensiblityElementWrapper.PORT_TYPE);
                //TODO : ExtensibilityAttribute have to handle here
            }
        }
        //Create an output message and add
        Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
        if (null != wsdl4jOutputMessage) {
            AxisMessage outMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            Message message = wsdl4jOutputMessage.getMessage();
            if (null != message) {
                outMessage.setElementQName(generateReferenceQname(
                        wrappedOutputName
                        , message,
                        findWrapppable(message)));
                copyExtensibleElements(message.getExtensibilityElements(),
                        dif, outMessage, AxisExtensiblityElementWrapper.PORT_TYPE);
                //TODO : ExtensibilityAttribute have to handle here
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
                        faultMessage.getQName(),
                        faultMessage, findWrapppable(faultMessage)));
                copyExtensibleElements(faultMessage.getExtensibilityElements(),
                        dif, faultyMessge, AxisExtensiblityElementWrapper.PORT_TYPE);
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
    private QName generateReferenceQname(QName outerName, Message wsdl4jMessage, boolean isWrappable) {
        QName referenceQName = null;
        if (isWrappable) {
            //The schema for this should be already made ! Find the QName from the list
            referenceQName = (QName) resolvedRpcWrappedElementMap.get(outerName.getLocalPart());

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

        if (bindingsMap != null && !bindingsMap.isEmpty()) {
            Binding[] bindings = (Binding[]) bindingsMap.values().toArray(new Binding[bindingsMap.size()]);
            Binding binding;
            for (int i = 0; i < bindings.length; i++) {
                binding = bindings[i];
                schemaElementList.add(
                        createSchemaForPorttype(binding.getPortType(), targetNamespaceUri,
                                findWrapForceable(binding)));
            }
        } else {
            PortType[] porttypesArray = (PortType[]) porttypeMap.values().toArray(new PortType[porttypeMap.size()]);
            for (int i = 0; i < porttypesArray.length; i++) {
                schemaElementList.add(
                        createSchemaForPorttype(porttypesArray[i], targetNamespaceUri, false));
            }

        }

        return (Element[]) schemaElementList.toArray(new Element[schemaElementList.size()]);
    }

    private Element createSchemaForPorttype(PortType porttype, String targetNamespaceUri, boolean forceWrapping) {

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

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //check whether there are messages that are wrappable. If there are no messages that are wrappable we'll
        //just return null and endup this process. However we need to take the force flag into account here
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        QName[] keys;
        if (forceWrapping) {
            //just take all the messages and wrap them, we've been told to force wrapping!
            keys = (QName[]) messagesMap.keySet().toArray(new QName[messagesMap.size()]);
        } else {
            //
            QName[] allKeys = (QName[]) messagesMap.keySet().toArray(new QName[messagesMap.size()]);
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

            keys = (QName[]) wrappableMessageNames.toArray(new QName[wrappableMessageNames.size()]);
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
            wsdl4jMessage = (Message) messagesMap.get(keys[i]);
            //No need to check the wrappable,

            //This message is wrappabel. However we need to see whether the message is already
            //resolved!
            if (!resolvedMessageQNames.contains(wsdl4jMessage.getQName())) {
                //This message has not been touched before!. So we can go ahead now
                Map parts = wsdl4jMessage.getParts();
                //add the complex type
                String name = wsdl4jMessage.getQName().getLocalPart();
                Element newComplexType = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);
                newComplexType.setAttribute(XSD_NAME, name);

                Element cmplxContentSequence = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_SEQUENCE_LOCAL_NAME);
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

                    child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);

                    String prefix;
                    if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())) {
                        prefix = xsdPrefix;
                    } else {
                        //this schema is a third party one. So we need to have an import statement in our generated schema
                        String uri = schemaTypeName.getNamespaceURI();
                        if (!namespaceImportsMap.containsKey(uri)) {
                            //create Element for namespace import
                            Element namespaceImport = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_IMPORT_LOCAL_NAME);
                            namespaceImport.setAttribute("namespace", uri);
                            //add this to the map
                            namespaceImportsMap.put(uri, namespaceImport);
                            //we also need to associate this uri with a prefix and include that prefix
                            //in the schema's namspace declarations. So add theis particular namespace to the
                            //prefix map as well
                            prefix = getTemporaryNamespacePrefix();
                            namespacePrefixMap.put(uri, prefix);
                        } else {
                            //this URI should be already in the namspace prefix map
                            prefix = (String) namespacePrefixMap.get(uri);
                        }


                    }
                    // If it's from a type the element we need to add a name and the type
                    //if not it's the element reference
                    if (isTyped) {
                        child.setAttribute(XSD_NAME, elementName);
                        child.setAttribute(XSD_TYPE, prefix + ":" + schemaTypeName.getLocalPart());
                    } else {
                        child.setAttribute(XSD_REF, prefix + ":" + schemaTypeName.getLocalPart());
                    }
                    cmplxContentSequence.appendChild(child);
                }
                newComplexType.appendChild(cmplxContentSequence);
                //add this newly created complextype to the list
                complexTypeElementsMap.put(wsdl4jMessage.getQName(), newComplexType);
                resolvedMessageQNames.add(wsdl4jMessage.getQName());
            }

        }

        Element elementDeclaration;

        //loop through the input op map and generate the elements
        String[] inputOperationtNames = (String[]) inputOperationsMap.keySet().toArray(
                new String[inputOperationsMap.size()]);
        for (int j = 0; j < inputOperationtNames.length; j++) {
            String inputOpName = inputOperationtNames[j];
            elementDeclaration = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME,
                    inputOpName);

            String typeValue = ((Message) inputOperationsMap.get(inputOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(XSD_TYPE,
                    AXIS2WRAPPED + ":" + typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(inputOpName, new QName(
                    targetNamespaceUri,
                    inputOpName,
                    AXIS2WRAPPED
            ));
        }

        //loop through the output op map and generate the elements
        String[] outputOperationtNames = (String[]) outputOperationsMap.keySet().toArray(
                new String[outputOperationsMap.size()]);
        for (int j = 0; j < outputOperationtNames.length; j++) {

            String baseoutputOpName = outputOperationtNames[j];
            String outputOpName = baseoutputOpName + "Response";
            elementDeclaration = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME,
                    outputOpName);
            String typeValue = ((Message) outputOperationsMap.get(baseoutputOpName)).getQName().getLocalPart();
            elementDeclaration.setAttribute(XSD_TYPE,
                    AXIS2WRAPPED + ":" + typeValue);
            elementElementsList.add(elementDeclaration);
            resolvedRpcWrappedElementMap.put(outputOpName, new QName(
                    targetNamespaceUri,
                    outputOpName,
                    AXIS2WRAPPED
            ));

        }

        //////////////////////////////////////////////////////////////////////////////////////////////
        // Now we are done with processing  the messages and generating the right schema object model
        // time to write out the schema
        //////////////////////////////////////////////////////////////////////////////////////////////


        Element schemaElement = document.createElementNS(XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":" + XML_SCHEMA_LOCAL_NAME);

        //loop through the namespace declarations first
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap.keySet().toArray(new String[namespacePrefixMap.size()]);
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
        schemaElement.setAttribute(XSD_TARGETNAMESPACE, targetNamespaceUri);
        schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT, XSD_UNQUALIFIED);

        Element[] namespaceImports = (Element[]) namespaceImportsMap.values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            schemaElement.appendChild(namespaceImports[i]);

        }


        Element[] complexTypeElements = (Element[]) complexTypeElementsMap.values().toArray(new Element[complexTypeElementsMap.size()]);
        for (int i = 0; i < complexTypeElements.length; i++) {
            schemaElement.appendChild(complexTypeElements[i]);

        }

        Element[] elementDeclarations = (Element[]) elementElementsList.toArray(new Element[elementElementsList.size()]);
        for (int i = 0; i < elementDeclarations.length; i++) {
            schemaElement.appendChild(elementDeclarations[i]);

        }


        return schemaElement;
    }

    /**
     * The intention of this procedure is to process the imports.
     * When processing the imports the imported documents will be
     * populating the items in the main document recursivley
     *
     * @param wsdl4JDefinition
     */
    private void processImports(Definition wsdl4JDefinition) {
        Map wsdlImports = wsdl4JDefinition.getImports();

        if (null != wsdlImports && !wsdlImports.isEmpty()) {
            Collection importsCollection = wsdlImports.values();
            for (Iterator iterator = importsCollection.iterator(); iterator.hasNext();) {
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
                                types.addExtensibilityElement(
                                        (ExtensibilityElement) typesList.get(j));

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

        WSDLReader reader =
                WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);
        Document doc;
        try {
            doc = XMLUtils.newDocument(in);
        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error",
                    e);
        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error",
                    e);

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
     * @param description
     * @param location                 : where is the ext element (port , portype , biding)
     * @param wsdl4jDefinition
     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Definition wsdl4jDefinition,
                                        AxisDescription description,
                                        int location) {
        Iterator iterator = wsdl4jExtensibleElements.iterator();
        ExtensionFactoryImpl extensionFactory = new ExtensionFactoryImpl();
        while (iterator.hasNext()) {

            ExtensibilityElement wsdl4jElement = (ExtensibilityElement) iterator
                    .next();

            if (wsdl4jElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) (wsdl4jElement);

                //look for the SOAP 1.2 stuff here. WSDL4j does not understand SOAP 1.2 things
                if (ExtensionConstants.SOAP_12_OPERATION.equals(unknown.getElementType())) {
                    org.apache.wsdl.extensions.SOAPOperation soapOperationExtensibiltyElement = (org.apache.wsdl.extensions.SOAPOperation) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapOperationExtensibiltyElement.setSoapAction(element.getAttribute("soapAction"));
                    soapOperationExtensibiltyElement.setStyle(element.getAttribute("style"));
                    // soapActionRequired
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapOperationExtensibiltyElement));
                } else if (ExtensionConstants.SOAP_12_BODY.equals(unknown.getElementType())) {
                    org.apache.wsdl.extensions.SOAPBody soapBodyExtensibiltyElement = (org.apache.wsdl.extensions.SOAPBody) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapBodyExtensibiltyElement.setUse(element.getAttribute("use"));
                    soapBodyExtensibiltyElement.setNamespaceURI(element.getAttribute("namespace"));
                    //encoding style
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapBodyExtensibiltyElement));
                } else if (ExtensionConstants.SOAP_12_HEADER.equals(unknown.getElementType())) {
                    org.apache.wsdl.extensions.SOAPHeader soapHeaderExtensibilityElement = (org.apache.wsdl.extensions.SOAPHeader) extensionFactory.getExtensionElement(
                            unknown.getElementType());
                    //right now there's no known header binding!. Ignore the copying of values for now
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapHeaderExtensibilityElement));
                } else if (ExtensionConstants.SOAP_12_BINDING.equals(unknown.getElementType())) {
                    org.apache.wsdl.extensions.SOAPBinding soapBindingExtensibiltyElement = (org.apache.wsdl.extensions.SOAPBinding) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapBindingExtensibiltyElement.setTransportURI(element.getAttribute("transport"));
                    soapBindingExtensibiltyElement.setStyle(element.getAttribute("style"));
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapBindingExtensibiltyElement));
                } else if (ExtensionConstants.SOAP_12_ADDRESS.equals(unknown.getElementType())) {
                    org.apache.wsdl.extensions.SOAPAddress soapAddressExtensibiltyElement = (org.apache.wsdl.extensions.SOAPAddress) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    Element element = unknown.getElement();
                    soapAddressExtensibiltyElement.setLocationURI(element.getAttribute("location"));
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapAddressExtensibiltyElement));

                } else if (ExtensionConstants.POLICY.equals(unknown.getElementType())) {
                    PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensionFactory.getExtensionElement(wsdl4jElement.getElementType());
                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory.getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    policyExtensibilityElement.setPolicyElement(policyReader.readPolicy(unknown.getElement()));
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, policyExtensibilityElement));

                } else if (ExtensionConstants.POLICY_REFERENCE.equals(unknown.getElementType())) {
                    PolicyExtensibilityElement policyExtensibilityElement = (PolicyExtensibilityElement) extensionFactory.getExtensionElement(wsdl4jElement.getElementType());
                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory.getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    policyExtensibilityElement.setPolicyElement(policyReader.readPolicyReference(unknown.getElement()));
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, policyExtensibilityElement));

                } else {

                    DefaultExtensibilityElement defaultExtensibilityElement = (DefaultExtensibilityElement) extensionFactory
                            .getExtensionElement(wsdl4jElement.getElementType());
                    defaultExtensibilityElement.setElement(unknown.getElement());
                    Boolean required = unknown.getRequired();
                    if (null != required) {
                        defaultExtensibilityElement.setRequired(required.booleanValue());
                    }
                    description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, defaultExtensibilityElement));
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
                description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapAddressExtensibilityElement));
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
                if (schema.getDocumentBaseURI() != null) {
                    schemaExtensibilityElement.setName(new QName("", schema.getDocumentBaseURI()));
                }
                axisService.setSchema(getXMLSchema(schemaExtensibilityElement.getElement()));
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
                description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapOperationextensibilityElement));
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
                description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapBodyExtensibilityElement));
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
                if (null != wsdl4jDefinition) {
                    //find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader.getMessage());
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    soapHeaderExtensibilityElement.setElement(msgPart.getElementName());
                }
                soapHeaderExtensibilityElement.setMessage(soapHeader.getMessage());

                soapHeaderExtensibilityElement.setPart(soapHeader.getPart());
                soapHeader.getMessage();
                description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapHeaderExtensibilityElement));
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
                description.setWsdlExtElements(new AxisExtensiblityElementWrapper(location, soapBindingExtensibilityElement));
            }
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
                    pushSchemaElement(((SchemaImport) v.get(i)).getReferencedSchema(), stack);
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
// We will not use the binding to set the wrappable/unwrappable state here. instead we'll look at the
// Messages for the following features
// 1. Messages with multiple parts -> We have no choice but to wrap
// 2. Messages with one part having a type attribute -> Again we have no choice but to wrap

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
     * Find the  XML schema prefix
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
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
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
        if (null != operationType) {

            if (operationType.equals(OperationType.REQUEST_RESPONSE))
                return WSDLConstants.MEP_URI_IN_OUT;

            if (operationType.equals(OperationType.ONE_WAY))
                return WSDLConstants.MEP_URI_IN_ONLY;

            if (operationType.equals(OperationType.NOTIFICATION))
                return WSDLConstants.MEP_URI_OUT_ONLY;

            if (operationType.equals(OperationType.SOLICIT_RESPONSE))
                return WSDLConstants.MEP_URI_OUT_IN;
        }
        throw new WSDLProcessingException("Cannot Determine the MEP");

    }
}
