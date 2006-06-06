package org.apache.axis2.description;

import com.ibm.wsdl.extensions.soap.SOAPConstants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

public class WSDL11ToAxisServiceBuilder extends WSDLToAxisServiceBuilder {

   protected static final Log log =
           LogFactory.getLog(WSDL11ToAxisServiceBuilder.class);

    private String portName;

    private static final String BINDING = "Binding";

    private static final String SERVICE = "Service";

    private static final String PORT = "Port";

    private static final String PORT_TYPE = "PortType";

    private static final String PORT_TYPE_OPERATION = "PortType.Operation";

    private static final String PORT_TYPE_OPERATION_INPUT = "PortType.Operation.Input";

    private static final String PORT_TYPE_OPERATION_OUTPUT = "PortType.Operation.Output";

    private static final String PORT_TYPE_OPERATION_FAULT = "PortType.Operation.Fault";

    private static final String BINDING_OPERATION = "Binding.Operation";

    private static final String BINDING_OPERATION_INPUT = "Binding.Operation.Input";

    private static final String BINDING_OPERATION_OUTPUT = "Binding.Operation.Output";

    private Definition wsdl4jDefinition = null;

    private WSDLLocator customWSLD4JResolver;

    public WSDL11ToAxisServiceBuilder(InputStream in, QName serviceName,
                                      String portName) {
        super(in, serviceName);
        this.portName = portName;
    }

    public WSDL11ToAxisServiceBuilder(Definition def, QName serviceName,
                                      String portName) {
        super(null, serviceName);
        this.wsdl4jDefinition = def;
        this.portName = portName;
    }

    public WSDL11ToAxisServiceBuilder(InputStream in, AxisService service) {
        super(in, service);
    }

    public WSDL11ToAxisServiceBuilder(InputStream in) {
        this(in, null, null);
    }

    /**
     * sets a custem WSDL4J locator
     * 
     * @param customWSLD4JResolver
     */
    public void setCustomWSLD4JResolver(WSDLLocator customWSLD4JResolver) {
        this.customWSLD4JResolver = customWSLD4JResolver;
    }

    public AxisService populateService() throws AxisFault {
        try {
            if (wsdl4jDefinition == null) {
                wsdl4jDefinition = readInTheWSDLFile(in);
            }
            //Setting wsdl4jdefintion to axisService , so if some one want
            // to play with it he can do that by getting the parameter
            Parameter wsdldefintionParamter = new Parameter();
            wsdldefintionParamter.setName(WSDLConstants.WSDL_4_J_DEFINITION);
            wsdldefintionParamter.setValue(wsdl4jDefinition);
            axisService.addParameter(wsdldefintionParamter);

            if (wsdl4jDefinition == null) {
                return null;
            }
            //setting target name space
            axisService.setTargetNamespace(wsdl4jDefinition
                    .getTargetNamespace());
            //adding ns in the original WSDL
            processPoliciesInDefintion(wsdl4jDefinition);
            //scheam generation
            processImports(wsdl4jDefinition);
            axisService.setNameSpacesMap(wsdl4jDefinition.getNamespaces());
            Types wsdl4jTypes = wsdl4jDefinition.getTypes();
            if (null != wsdl4jTypes) {
                this.copyExtensibleElements(wsdl4jTypes
                        .getExtensibilityElements(), wsdl4jDefinition,
                        axisService, TYPES);
            }
            Binding binding = findBinding(wsdl4jDefinition);
            //////////////////(1.2) /////////////////////////////
            // create new Schema extensions element for wrapping
            Element[] schemaElements = generateWrapperSchema(wsdl4jDefinition,
                    binding);
            if (schemaElements != null && schemaElements.length > 0) {
                for (int i = 0; i < schemaElements.length; i++) {
                    Element schemaElement = schemaElements[i];
                    if (schemaElement != null) {
                        axisService
                                .addSchema(getXMLSchema(schemaElement, null));
                    }
                }
            }
            processBinding(binding, wsdl4jDefinition);
            return axisService;
        } catch (WSDLException e) {
            throw new AxisFault(e);
        } catch (Exception e) {
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
                //pick the first service - we don't really have a choice here
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
                //pick the port with the SOAP address as the default port
                port = findSOAPPort(ports);
                if (port==null){
                    //a SOAP port was not found - log a warning
                    // and use the first port in the list
                    log.info("A SOAP port was not found - " +
                            "picking a random port!");
                    port = (Port) ports.values().toArray()[0];
                }
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

    /**
     * Finds a SOAP port given the port map
     * @param ports
     * @return
     */
    private Port findSOAPPort(Map ports) {
        Port port;
        for (Iterator portsIterator = ports.values().iterator();
             portsIterator.hasNext();) {
            port = (Port)portsIterator.next();
            List extensibilityElements =  port.getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++) {
                Object extElement =  extensibilityElements.get(i);
                if (extElement instanceof SOAPAddress){
                    //SOAP 1.1 address found - return that port and we are done
                    return port;
                }

                if (extElement instanceof UnknownExtensibilityElement){
                    //todo check for a SOAP 1.2 address
                    // extensibility element here
                }

            }

        }
        //None found - just return null.
        return null;
    }

    private void processBinding(Binding binding, Definition dif)
            throws Exception {
        if (binding != null) {
            copyExtensibleElements(binding.getExtensibilityElements(), dif,
                    axisService, BINDING);

            PortType portType = binding.getPortType();
            processPortType(portType, dif);

            List list = binding.getBindingOperations();

            for (int i = 0; i < list.size(); i++) {
                BindingOperation wsdl4jBindingOperation = (BindingOperation) list
                        .get(i);
                AxisOperation operation = axisService.getOperation(new QName(
                        wsdl4jBindingOperation.getName()));
                copyExtensibleElements(wsdl4jBindingOperation
                        .getExtensibilityElements(), dif, operation,
                        BINDING_OPERATION);

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
                                .getExtensibilityElements(), dif, inMessage,
                                BINDING_OPERATION_INPUT);

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
            throws Exception {

        copyExtensionAttributes(wsdl4jPortType.getExtensionAttributes(),
                axisService, PORT_TYPE);

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
                                             Definition dif) throws Exception {
        QName opName = new QName(wsdl4jOperation.getName());
        //Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);
        if (axisOperation == null) {
            String MEP = getMEP(wsdl4jOperation);
            axisOperation = AxisOperationFactory.getOperationDescription(MEP);
            axisOperation.setName(opName);

            //All policy includes must share same registry
            PolicyInclude pi = axisOperation.getPolicyInclude();
            if (pi == null) {
                pi = new PolicyInclude();
                axisOperation.setPolicyInclude(pi);
            }
            pi.setPolicyRegistry(registry);
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
        if (isServerSide) {
            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {
                    inMessage
                            .setElementQName(generateReferenceQname(
                                    wrappedInputName, message,
                                    findWrapppable(message)));
                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                            dif, inMessage, PORT_TYPE_OPERATION_INPUT);

                }
            }
            //Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {
                    outMessage
                            .setElementQName(generateReferenceQname(
                                    wrappedOutputName, message,
                                    findWrapppable(message)));
                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                            dif, outMessage, PORT_TYPE_OPERATION_OUTPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
            }
        } else {
            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {
                    inMessage
                            .setElementQName(generateReferenceQname(
                                    wrappedInputName, message,
                                    findWrapppable(message)));
                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                            dif, inMessage, PORT_TYPE_OPERATION_OUTPUT);

                }
            }
            //Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {
                    outMessage
                            .setElementQName(generateReferenceQname(
                                    wrappedOutputName, message,
                                    findWrapppable(message)));
                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                            dif, outMessage, PORT_TYPE_OPERATION_INPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
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

        //loop through the messages. We'll populate thins map with the relevant
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
                            //copy ns

                            Map namespaces = importedDef.getNamespaces();
                            Iterator keys = namespaces.keySet().iterator();
                            while (keys.hasNext()) {
                                Object key = keys.next();
                                if (!wsdl4jDefinition.getNamespaces()
                                        .containsValue(namespaces.get(key))) {
                                    wsdl4jDefinition.getNamespaces().put(key,
                                            namespaces.get(key));
                                }
                            }

                            wsdl4jDefinition.getNamespaces().putAll(namespaces);
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

    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        //switch off the verbose mode for all usecases
        reader.setFeature("javax.wsdl.verbose", false);

        if (customWSLD4JResolver != null) {
            return reader.readWSDL(customWSLD4JResolver);
        } else {
            reader.setFeature("javax.wsdl.importDocuments", false);
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
                throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error",
                        e);
            }
            return reader.readWSDL(getBaseUri(), doc);
        }
    }

    /**
     * Get the Extensible elements form wsdl4jExtensibleElements
     * <code>Vector</code> if any and copy them to <code>Component</code>
     * 
     * @param wsdl4jExtensibleElements
     * @param description
     *            where is the ext element (port , portype , biding)
     * @param wsdl4jDefinition
     * @param originOfExtensibilityElements -
     *            this will indicate the place this extensibility element came
     *            from.
     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Definition wsdl4jDefinition, AxisDescription description,
                                        String originOfExtensibilityElements) {
        Iterator iterator = wsdl4jExtensibleElements.iterator();
        while (iterator.hasNext()) {
            ExtensibilityElement wsdl4jElement = (ExtensibilityElement) iterator
                    .next();

            if (wsdl4jElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) (wsdl4jElement);

                //look for the SOAP 1.2 stuff here. WSDL4j does not understand
                // SOAP 1.2 things
                if (WSDLConstants.SOAP_12_OPERATION.equals(unknown
                        .getElementType())) {
                    Element element = unknown.getElement();
                    if (description instanceof AxisOperation) {
                        AxisOperation axisOperation = (AxisOperation) description;
                        String style = element.getAttribute("style");
                        if (style != null) {
                            axisOperation.setStyle(style);
                        }
                        axisOperation.setSoapAction(element
                                .getAttribute("soapAction"));
                    }
                } else if (WSDLConstants.SOAP_12_HEADER.equals(unknown
                        .getElementType())) {
                    //TODO : implement thid
                } else if (WSDLConstants.SOAP_12_BINDING.equals(unknown
                        .getElementType())) {
                    style = unknown.getElement().getAttribute("style");
                    axisService.setSoapNsUri(wsdl4jElement.getElementType()
                            .getNamespaceURI());
                } else if (WSDLConstants.SOAP_12_ADDRESS.equals(unknown
                        .getElementType())) {
                    axisService.setEndpoint(unknown.getElement().getAttribute(
                            "location"));
                } else if (WSDLConstants.POLICY
                        .equals(unknown.getElementType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    Policy policy = policyReader.readPolicy(unknown
                            .getElement());

                    addPolicy(description, originOfExtensibilityElements,
                            policy);

                } else if (WSDLConstants.POLICY_REFERENCE.equals(unknown
                        .getElementType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    PolicyReference policyRef = policyReader
                            .readPolicyReference(unknown.getElement());
                    addPolicyRef(description, originOfExtensibilityElements,
                            policyRef);
                } else if (AddressingConstants.Final.WSAW_USING_ADDRESSING
                        .equals(unknown.getElementType())
                        || AddressingConstants.Submission.WSAW_USING_ADDRESSING
                        .equals(unknown.getElementType())) {
                    // Read the wsaw:UsingAddressing flag from the WSDL. It is
                    // only valid on the Port or Binding
                    // so only recognise it as en extensibility elemtn of one of
                    // those.
                    if (originOfExtensibilityElements.equals(PORT)
                            || originOfExtensibilityElements.equals(BINDING)) {
                        if (Boolean.TRUE.equals(unknown.getRequired())) {
                            axisService
                                    .setWSAddressingFlag(AddressingConstants.ADDRESSING_REQUIRED);
                        } else {
                            axisService
                                    .setWSAddressingFlag(AddressingConstants.ADDRESSING_OPTIONAL);
                        }
                    }

                } else {
                    //TODO : we are ignored that.
                }

            } else if (wsdl4jElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) wsdl4jElement;
                axisService.setEndpoint(soapAddress.getLocationURI());
            } else if (wsdl4jElement instanceof Schema) {
                Schema schema = (Schema) wsdl4jElement;
                //just add this schema - no need to worry about the imported
                // ones
                axisService.addSchema(getXMLSchema(schema.getElement(),
                        wsdl4jDefinition.getDocumentBaseURI()));
            } else if (SOAPConstants.Q_ELEM_SOAP_OPERATION.equals(wsdl4jElement
                    .getElementType())) {
                SOAPOperation soapOperation = (SOAPOperation) wsdl4jElement;
                if (description instanceof AxisOperation) {
                    AxisOperation axisOperation = (AxisOperation) description;
                    if (soapOperation.getStyle() != null) {
                        axisOperation.setStyle(soapOperation.getStyle());
                    }
                    axisOperation.setSoapAction(soapOperation
                            .getSoapActionURI());
                }
            } else if (SOAPConstants.Q_ELEM_SOAP_HEADER.equals(wsdl4jElement
                    .getElementType())) {
                SOAPHeader soapHeader = (SOAPHeader) wsdl4jElement;
                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();
                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
                headerMessage.setUse(soapHeader.getUse());
                Boolean required = soapHeader.getRequired();
                if (null != required) {
                    headerMessage.setRequired(required.booleanValue());
                }
                if (null != wsdl4jDefinition) {
                    //find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    headerMessage.setElement(msgPart.getElementName());
                }
                headerMessage.setMessage(soapHeader.getMessage());

                headerMessage.setPart(soapHeader.getPart());
                if (description instanceof AxisMessage) {
                    ((AxisMessage) description).addSoapHeader(headerMessage);
                }
            } else if (SOAPConstants.Q_ELEM_SOAP_BINDING.equals(wsdl4jElement
                    .getElementType())) {
                SOAPBinding soapBinding = (SOAPBinding) wsdl4jElement;
                style = soapBinding.getStyle();
                axisService.setSoapNsUri(soapBinding.getElementType()
                        .getNamespaceURI());
            }
        }
    }

    private void addPolicy(AxisDescription description,
                           String originOfExtensibilityElements, Policy policy) {

        if (description instanceof AxisService) {
            // wsdl:service
            if (SERVICE.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.SERVICE_POLICY, policy);

                // wsdl:service -> wsdl:port
            } else if (PORT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.PORT_POLICY, policy);

                // wsdl:binding
            } else if (BINDING.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.BINDING_POLICY, policy);

            }

            //TODO wsdl:portType ?

        } else if (description instanceof AxisOperation) {

            // wsdl:portType -> wsdl:operation
            if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.OPERATION_POLICY, policy);

                // wsdl:binding -> wsdl:operation
            } else {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.BINDING_OPERATION_POLICY, policy);
            }

        } else {

            // wsdl:portType -> wsdl:operation -> wsdl:input
            if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.INPUT_POLICY, policy);

                // wsdl:binding -> wsdl:operation -> wsdl:input
            } else if (BINDING_OPERATION_INPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.BINDING_INPUT_POLICY, policy);

                // wsdl:portType -> wsdl:operation -> wsdl:put
            } else if (PORT_TYPE_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.OUTPUT_POLICY, policy);

                // wsdl:binding -> wsdl:operation -> wsdl:output
            } else if (BINDING_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyElement(
                        PolicyInclude.BINDING_OUTPUT_POLICY, policy);
            }
        }
    }

    private void addPolicyRef(AxisDescription description,
                              String originOfExtensibilityElements,
                              PolicyReference policyRefElement) {

        if (description instanceof AxisService) {
            // wsdl:service
            if (SERVICE.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.SERVICE_POLICY, policyRefElement);

                // wsdl:service -> wsdl:port
            } else if (PORT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.PORT_POLICY, policyRefElement);

                // wsdl:binding
            } else if (BINDING.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.BINDING_POLICY, policyRefElement);
            }

            //TODO wsdl:portType ?

        } else if (description instanceof AxisOperation) {

            // wsdl:portType -> wsdl:operation
            if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.OPERATION_POLICY, policyRefElement);

                // wsdl:binding -> wsdl:operation
            } else {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.BINDING_POLICY, policyRefElement);
            }

        } else {

            // wsdl:portType -> wsdl:operation -> wsdl:input
            if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.INPUT_POLICY, policyRefElement);

                // wsdl:binding -> wsdl:operation -> wsdl:input
            } else if (BINDING_OPERATION_INPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.BINDING_INPUT_POLICY, policyRefElement);

                // wsdl:portType -> wsdl:operation -> wsdl:put
            } else if (PORT_TYPE_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.OUTPUT_POLICY, policyRefElement);

                // wsdl:binding -> wsdl:operation -> wsdl:output
            } else if (BINDING_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                description.getPolicyInclude().addPolicyRefElement(
                        PolicyInclude.BINDING_OUTPUT_POLICY, policyRefElement);
            }

            //TODO Faults ..
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

    private String getMEP(Operation operation) throws Exception {
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
                throw new Exception("Cannot Determine the MEP");
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
                throw new Exception("Cannot Determine the MEP");
            }
        }
        throw new Exception("Cannot Determine the MEP");
    }

    private void copyExtensionAttributes(Map extAttributes,
                                          AxisDescription description,
                                          String origin) {

        QName key;
        QName value;

        for (Iterator iterator = extAttributes.keySet().iterator(); iterator
                .hasNext();) {
            key = (QName) iterator.next();
            if (PolicyConstants.POLICY_NAMESPACE_URI.equals(key
                    .getNamespaceURI())
                    && "PolicyURIs".equals(key.getLocalPart())) {
                value = (QName) extAttributes.get(key);
                String policyURIs = value.getLocalPart();

                if (policyURIs.length() != 0) {
                    String[] uris = policyURIs.split(" ");

                    PolicyReference ref;
                    for (int i = 0; i < uris.length; i++) {
                        ref = new PolicyReference(uris[i]);

                        if (PORT_TYPE.equals(origin)) {
                            PolicyInclude include = description
                                    .getPolicyInclude();
                            include.addPolicyRefElement(
                                    PolicyInclude.PORT_TYPE_POLICY, ref);
                        }
                    }
                }
            }
        }

    }

    private void processPoliciesInDefintion(Definition definition) {
        List extElements = definition.getExtensibilityElements();

        ExtensibilityElement extElement;
        for (Iterator iterator = extElements.iterator(); iterator.hasNext();) {
            extElement = (ExtensibilityElement) iterator.next();

            if (extElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) extElement;
                if (WSDLConstants.POLICY.equals(unknown.getElementType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    Policy policy = policyReader.readPolicy(unknown
                            .getElement());

                    if (policy.getId() != null) {
                        registry.register(policy.getId(), policy);
                    }

                    if (policy.getName() != null) {
                        registry.register(policy.getName(), policy);
                    }
                }
            }
        }
    }

}