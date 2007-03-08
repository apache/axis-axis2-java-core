package org.apache.axis2.description;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingHelper;
import org.apache.axis2.addressing.wsdl.WSDL11ActionHelper;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.HttpAddress;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.SoapAddress;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.util.WSDL4JImportedWSDLHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyReference;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
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
import javax.wsdl.extensions.http.HTTPAddress;
import javax.wsdl.extensions.http.HTTPBinding;
import javax.wsdl.extensions.http.HTTPOperation;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    protected static final Log log = LogFactory
            .getLog(WSDL11ToAxisServiceBuilder.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();

    protected String portName;

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

    protected Definition wsdl4jDefinition = null;

    private WSDLLocator customWSLD4JResolver;

    public static final String RPC_STYLE = "rpc";

    public static final String DOCUMENT_STYLE = "document";

    public static final String ENCODED_USE = "encoded";

    /**
     * Keeps a list of processable operations initiate to an empty list
     */
    private List wrappableOperations = new ArrayList();
    // used to keep the binding type of the selected binding
    private String bindingType;

    public static final String WRAPPED_OUTPUTNAME_SUFFIX = "Response";

    public static final String XML_NAMESPACE_URI = "http://www.w3.org/2000/xmlns/";

    public static final String NAMESPACE_DECLARATION_PREFIX = "xmlns:";

    private static int prefixCounter = 0;

    public static final String NAMESPACE_URI = "namespace";

    public static final String TRAGET_NAMESPACE = "targetNamespace";

    public static final String BINDING_TYPE_SOAP = "soap";
    public static final String BINDING_TYPE_HTTP = "http";

    /**
     * keep track of whether setup code related to the entire wsdl is complete.
     * Note that WSDL11ToAllAxisServices will call setup multiple times, so this
     * field is used to make subsequent calls no-ops.
     */
    private boolean setupComplete = false;

    private Map schemaMap = null;

    private static final String JAVAX_WSDL_VERBOSE_MODE_KEY = "javax.wsdl.verbose";

    /**
     * constructor taking in the service name and the port name
     *
     * @param in
     * @param serviceName
     * @param portName
     */
    public WSDL11ToAxisServiceBuilder(InputStream in, QName serviceName,
                                      String portName) {
        super(in, serviceName);
        this.portName = portName;
    }

    /**
     * @param def
     * @param serviceName
     * @param portName
     */
    public WSDL11ToAxisServiceBuilder(Definition def, QName serviceName,
                                      String portName) {
        super(null, serviceName);
        this.wsdl4jDefinition = def;
        this.portName = portName;
    }

    /**
     * @param in
     * @param service
     */
    public WSDL11ToAxisServiceBuilder(InputStream in, AxisService service) {
        super(in, service);
    }

    /**
     * @param in
     */
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

    /**
     * populates a given service This is the only publicly accessible method in
     * this class
     *
     * @throws AxisFault
     */
    public AxisService populateService() throws AxisFault {
        try {

            setup();
            // Setting wsdl4jdefintion to axisService , so if some one want
            // to play with it he can do that by getting the parameter
            Parameter wsdlDefinitionParameter = new Parameter();
            wsdlDefinitionParameter.setName(WSDLConstants.WSDL_4_J_DEFINITION);
            wsdlDefinitionParameter.setValue(wsdl4jDefinition);
            axisService.addParameter(wsdlDefinitionParameter);
            axisService.setWsdlFound(true);
            axisService.setCustomWsld(true);

            if (wsdl4jDefinition == null) {
                return null;
            }

            // setting target name space
            axisService.setTargetNamespace(wsdl4jDefinition.getTargetNamespace());
            axisService.setNameSpacesMap(new NamespaceMap(wsdl4jDefinition.getNamespaces()));
            Service wsdl4jService = findService(wsdl4jDefinition);
            Binding binding = findBinding(wsdl4jDefinition, wsdl4jService);

            if (binding.getPortType() == null) {
                throw new AxisFault("There is no port type associated with the binding");
            }

            // create new Schema extensions element for wrapping
            // (if its present)
            Element[] schemaElements = generateWrapperSchema(schemaMap, binding);

            // we might have modified the schemas by now so the addition should
            // happen here
            Types wsdl4jTypes = wsdl4jDefinition.getTypes();
            if (null != wsdl4jTypes) {
                this.copyExtensibleElements(wsdl4jTypes
                        .getExtensibilityElements(), wsdl4jDefinition,
                                                     axisService, TYPES);
            }

            // add the newly created schemas
            if (schemaElements != null && schemaElements.length > 0) {
                for (int i = 0; i < schemaElements.length; i++) {
                    Element schemaElement = schemaElements[i];
                    if (schemaElement != null) {
                        axisService
                                .addSchema(getXMLSchema(schemaElement, null));
                    }
                }
            }
            // copy the documentation element content to the description
            Element documentationElement = wsdl4jDefinition
                    .getDocumentationElement();
            if ((documentationElement != null) && (documentationElement.getFirstChild() != null)) {
                Node firstChild = documentationElement.getFirstChild();
                String serviceDes;
                if (firstChild.getNodeType() == Node.TEXT_NODE) {
                    serviceDes = firstChild.getNodeValue();
                } else {
                    serviceDes = DOM2Writer.nodeToString(firstChild);
                }
                axisService.setServiceDescription(serviceDes);
            }

            axisService.setName(wsdl4jService.getQName().getLocalPart());
            populateEndpoints(binding, wsdl4jService);

            return axisService;
        } catch (WSDLException e) {
            log.error(e);
            throw new AxisFault(e);
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault(e);
        }
    }

    /**
     * @param binding
     * @param wsdl4jService must have atlease one port
     * @throws AxisFault
     */
    private void populateEndpoints(Binding binding, Service wsdl4jService) throws AxisFault {

        Map wsdl4jPorts = wsdl4jService.getPorts();
        QName bindingName = binding.getQName();

        Port port;
        AxisEndpoint axisEndpoint = null;

        // process the port type for this binding
        // although we support multiports they must be belongs to same port type and should have the
        // same soap style
        populatePortType(wsdl4jDefinition.getPortType(binding.getPortType().getQName()));

        Binding currentBinding;

        for (Iterator iterator = wsdl4jPorts.values().iterator(); iterator.hasNext();) {
            port = (Port) iterator.next();
            // we process the port only if it has the same port type as the selected binding
            currentBinding = wsdl4jDefinition.getBinding(port.getBinding().getQName());
            if (currentBinding.getPortType().getQName().equals(binding.getPortType().getQName())) {
                axisEndpoint = new AxisEndpoint();
                axisEndpoint.setName(port.getName());

                if (axisService.getEndpointName() == null &&
                        bindingName.equals(port.getBinding().getQName())) {
                    populateEndpoint(axisEndpoint, port, true);
                    axisService.setEndpointName(axisEndpoint.getName());
                    axisService.setBindingName(axisEndpoint.getBinding().getName().getLocalPart());
                    if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                        SoapAddress soapAddress = (SoapAddress) axisEndpoint
                                .getProperty(WSDL2Constants.ATTR_WSOAP_ADDRESS);
                        axisService.setEndpointURL(soapAddress.getLocation());
                    } else if (BINDING_TYPE_HTTP.equals(this.bindingType)) {
                        HttpAddress httpAddress = (HttpAddress) axisEndpoint
                                .getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
                        axisService.setEndpointURL(httpAddress.getLocation());
                    }

                } else {
                    populateEndpoint(axisEndpoint, port, false);
                }
                axisService.addEndpoint(port.getName(), axisEndpoint);
            }

        }

    }

    /**
     * setting message qname is a binding dependent process for an example message element depends on the
     * soap style (rpc or document) and parts elememet of the soap body
     * On the otherhand we keep only one set of axis operations belongs to a selected port type in axis service
     * So setting qname refetences done only with the selected binding processing
     *
     * @param axisEndpoint
     * @param wsdl4jPort
     * @param isSetMessageQNames
     * @throws AxisFault
     */
    private void populateEndpoint(AxisEndpoint axisEndpoint, Port wsdl4jPort,
                                  boolean isSetMessageQNames)
            throws AxisFault {

        copyExtensibleElements(wsdl4jPort.getExtensibilityElements(), wsdl4jDefinition,
                               axisEndpoint, BINDING);

        AxisBinding axisBinding = new AxisBinding();
        Binding wsdl4jBinding = wsdl4jDefinition.getBinding(wsdl4jPort.getBinding().getQName());

        axisBinding.setName(wsdl4jBinding.getQName());
        axisEndpoint.setBinding(axisBinding);

        populateBinding(axisBinding, wsdl4jBinding, isSetMessageQNames);


    }

    private void populatePortType(PortType wsdl4jPortType) throws AxisFault {
        List wsdl4jOperations = wsdl4jPortType.getOperations();

        if (wsdl4jOperations.size() < 1) {
            throw new AxisFault("No operation found in the portType element");
        }

        AxisOperation axisOperation;
        List operationNames = new ArrayList();

        QName opName;
        Operation wsdl4jOperation;

        for (Iterator iterator = wsdl4jOperations.iterator(); iterator.hasNext();) {
            wsdl4jOperation = (Operation) iterator.next();

            axisOperation = populateOperations(wsdl4jOperation, wsdl4jPortType, wsdl4jDefinition);
            axisOperation.setParent(axisService);
            axisService.addChild(axisOperation);
            operationNames.add(axisOperation.getName());
        }

        // this is used only in codegen to preserve operation order
        if (isCodegen) {
            axisService.setOperationsNameList(operationNames);
        }

    }

    private void populateBinding(AxisBinding axisBinding, Binding wsdl4jBinding,
                                 boolean isSetMessageQNames)
            throws AxisFault {

        copyExtensibleElements(wsdl4jBinding.getExtensibilityElements(), wsdl4jDefinition,
                               axisBinding, BINDING);

        List wsdl4jBidingOperations = wsdl4jBinding.getBindingOperations();

        if (wsdl4jBidingOperations.size() < 1) {
            throw new AxisFault("No operation found for the binding");
        }

        AxisOperation axisOperation;
        Operation wsdl4jOperation;

        AxisBindingOperation axisBindingOperation;
        BindingOperation wsdl4jBindingOperation;

        Map httpLocationMap = new TreeMap();
        String httpLocation = null;

        PortType portType = wsdl4jDefinition.getPortType(wsdl4jBinding.getPortType().getQName());

        for (Iterator iterator = wsdl4jBidingOperations.iterator(); iterator.hasNext();) {

            axisBindingOperation = new AxisBindingOperation();
            wsdl4jBindingOperation = (BindingOperation) iterator.next();
            wsdl4jOperation = findOperation(portType, wsdl4jBindingOperation);

            axisBindingOperation.setName(new QName("", wsdl4jBindingOperation.getName()));

            axisOperation = axisService.getOperation(new QName("", wsdl4jOperation.getName()));
            axisBindingOperation.setAxisOperation(axisOperation);

            // process ExtensibilityElements of the wsdl4jBinding
            copyExtensibleElements(wsdl4jBindingOperation.getExtensibilityElements(),
                                   wsdl4jDefinition, axisBindingOperation, BINDING_OPERATION);

            httpLocation =
                    (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);
            if (httpLocation != null) {
                httpLocationMap.put(RESTUtil.getConstantFromHTTPLocation(httpLocation),
                                    axisBindingOperation.getAxisOperation());
            }

            BindingInput wsdl4jBindingInput = wsdl4jBindingOperation.getBindingInput();

            if (wsdl4jBindingInput != null &&
                    WSDLUtil.isInputPresentForMEP(axisOperation.getMessageExchangePattern())) {

                AxisBindingMessage axisBindingInMessage = new AxisBindingMessage();
                copyExtensibleElements(wsdl4jBindingInput.getExtensibilityElements(),
                                       wsdl4jDefinition,
                                       axisBindingInMessage, BINDING_OPERATION_INPUT);

                AxisMessage axisInMessage =
                        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

                if (isSetMessageQNames) {
                    addQNameReference(axisInMessage, wsdl4jBindingOperation.getOperation(),
                                      wsdl4jBindingInput,
                                      wrappableOperations.contains(wsdl4jBindingOperation));
                }

                axisBindingInMessage.setAxisMessage(axisInMessage);
                axisBindingInMessage.setDirection(axisInMessage.getDirection());
                
                axisBindingOperation
                        .addChild(axisBindingInMessage.getDirection(), axisBindingInMessage);
            }

            BindingOutput wsdl4jBindingOutput = wsdl4jBindingOperation.getBindingOutput();

            if (wsdl4jBindingOutput != null &&
                    WSDLUtil.isOutputPresentForMEP(axisOperation.getMessageExchangePattern())) {
                AxisBindingMessage axisBindingOutMessage = new AxisBindingMessage();
                AxisMessage axisOutMessage =
                        axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);

                copyExtensibleElements(wsdl4jBindingOutput.getExtensibilityElements(),
                                       wsdl4jDefinition,
                                       axisBindingOutMessage, BINDING_OPERATION_OUTPUT);

                if (isSetMessageQNames) {
                    addQNameReference(axisOutMessage, wsdl4jBindingOperation.getOperation(),
                                      wsdl4jBindingOutput,
                                      wrappableOperations.contains(wsdl4jBindingOperation));
                }


                axisBindingOutMessage.setAxisMessage(axisOutMessage);
                axisBindingOutMessage.setDirection(axisOutMessage.getDirection());

                axisBindingOperation
                        .addChild(axisBindingOutMessage.getDirection(), axisBindingOutMessage);
            }

            Map bindingFaultsMap = wsdl4jBindingOperation.getBindingFaults();

            /* process the binding faults */
            for (Iterator bindingFaults = bindingFaultsMap.values().iterator();
                 bindingFaults.hasNext();) {

                BindingFault bindingFault = (BindingFault) bindingFaults.next();
                Fault wsdl4jFault = wsdl4jOperation.getFault(bindingFault.getName());
                Message wsdl4jFaultMessge = wsdl4jFault.getMessage();

                AxisMessage faultMessage = findFaultMessage(
                        wsdl4jFaultMessge.getQName().getLocalPart(),
                        axisOperation.getFaultMessages());

                AxisBindingMessage axisBindingFaultMessage = new AxisBindingMessage();
                axisBindingFaultMessage.setAxisMessage(faultMessage);
                axisBindingFaultMessage.setParent(axisBindingOperation);

                axisBindingOperation.addFault(axisBindingFaultMessage);
                if (isSetMessageQNames) {
                    addQNameReference(faultMessage, wsdl4jFault.getMessage());
                }
            }

            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);
        }
        axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationMap);
    }

    /**
     * contains all code which gathers non-service specific information from the
     * wsdl. <p/> After all the setup completes successfully, the setupComplete
     * field is set so that any subsequent calls to setup() will result in a
     * no-op. Note that subclass WSDL11ToAllAxisServicesBuilder will call
     * populateService for each port in the WSDL. Separating the non-service
     * specific information here allows WSDL11ToAllAxisServicesBuilder to only
     * do this work 1 time per WSDL, instead of for each port on each service.
     *
     * @throws WSDLException if readInTheWSDLFile fails
     */
    protected void setup() throws WSDLException {
        if (setupComplete) { // already setup, just do nothing and return
            return;
        }
        if (wsdl4jDefinition == null) {
            wsdl4jDefinition = readInTheWSDLFile(in);
        }
        if (wsdl4jDefinition == null) {
            return; // can't continue without wsdl
        }

        // process the imports
        WSDL4JImportedWSDLHelper.processImports(wsdl4jDefinition);

        // Adding the policies in the Definition to the the PolicyRegistry
        processPoliciesInDefintion(wsdl4jDefinition);

        // setup the schemaMap
        schemaMap = getSchemaMap(wsdl4jDefinition.getTypes());

        setupComplete = true; // if any part of setup fails, don't mark
        // setupComplete
    }


    /**
     * Populate a map of targetNamespace vs DOM schema element This is used to
     * grab the correct schema element when adding a new element
     *
     * @param wsdl4jTypes
     */

    private Map getSchemaMap(Types wsdl4jTypes) {
        Map schemaMap = new HashMap();

        Object extensibilityElement;
        if (wsdl4jTypes != null) {
            for (Iterator iterator = wsdl4jTypes.getExtensibilityElements().iterator();
                 iterator.hasNext();) {
                extensibilityElement = iterator.next();

                if (extensibilityElement instanceof Schema) {
                    Element schemaElement = ((Schema) extensibilityElement).getElement();
                    schemaMap.put(schemaElement.getAttribute(XSD_TARGETNAMESPACE), schemaElement);
                }
            }
        }

        return schemaMap;
    }

    /**
     * return the service to process
     * if user has specified we check whether it exists
     * else pick a random service and throws an exception if not found any thing
     *
     * @param definition
     * @return service to process
     * @throws AxisFault
     */

    private Service findService(Definition definition) throws AxisFault {
        Map services = definition.getServices();
        Service service = null;
        if (serviceName != null) {
            // i.e if a user has specified a pirticular port
            service = (Service) services.get(serviceName);
            if (service == null) {
                throw new AxisFault("Service " + serviceName
                        + " was not found in the WSDL");
            }
        } else {
            if (services.size() > 0) {
                for (Iterator iter = services.values().iterator(); iter.hasNext();) {
                    service = (Service) iter.next();
                    if (service.getPorts().size() > 0) {
                        //i.e we have found a service with ports
                        break;
                    }
                }
                if ((service == null) || (service.getPorts().size() == 0)) {
                    throw new AxisFault("there is no service with ports to pick");
                }

            } else {
                throw new AxisFault("No service was not found in the WSDL at " +
                        definition.getDocumentBaseURI()
                        + " with targetnamespace "
                        + definition.getTargetNamespace());
            }
        }
        return service;
    }

    /**
     * Look for the relevant binding!
     * if user has spcifed a port get it
     * otherwise find first soap port or pick random one if there is no soap port
     *
     * @param dif
     * @param service service can not be null
     * @throws AxisFault
     */
    private Binding findBinding(Definition dif, Service service) throws AxisFault {

        Binding binding = null;
        Port port = null;
        copyExtensibleElements(service.getExtensibilityElements(), dif, axisService, SERVICE);
        if (portName != null) {
            // i.e if user has specified a service
            port = service.getPort(portName);
            if (port == null) {
                throw new AxisFault("No port found for the given name :"
                        + portName);
            }
        } else {
            Map ports = service.getPorts();
            if (ports != null && ports.size() > 0) {
                // pick the port with the SOAP address as the default port
                port = findSOAPPort(ports);
                if (port == null) {
                    // a SOAP port was not found - log a warning
                    // and use the first port in the list
                    log.info("A SOAP port was not found - "
                            + "picking a random port!");
                    port = (Port) ports.values().toArray()[0];
                }
            }
        }

        axisService.setName(service.getQName().getLocalPart());

        if (port != null) {
            copyExtensibleElements(port.getExtensibilityElements(), dif,
                                   axisService, PORT);
            binding = dif.getBinding(port.getBinding().getQName());
            if (binding == null) {
                binding = port.getBinding();
            }
        }

        return binding;
    }

    /**
     * Finds a SOAP port given the port map
     *
     * @param ports
     */
    private Port findSOAPPort(Map ports) {
        Port port;
        for (Iterator portsIterator = ports.values().iterator(); portsIterator
                .hasNext();) {
            port = (Port) portsIterator.next();
            List extensibilityElements = port.getExtensibilityElements();
            for (int i = 0; i < extensibilityElements.size(); i++) {
                Object extElement = extensibilityElements.get(i);
                if (extElement instanceof SOAPAddress) {
                    // SOAP 1.1 address found - return that port and we are done
                    return port;
                }

                if (extElement instanceof SOAP12Address) {
                    // SOAP 1.2 address found - return that port and we are done
                    return port;
                }
            }
        }
        // None found - just return null.
        return null;
    }

    private Operation findOperation(PortType portType,
                                    BindingOperation wsdl4jBindingOperation) {
        Operation op = wsdl4jBindingOperation.getOperation();
        String input = null;
        if (op != null && op.getInput() != null) {
            input = op.getInput().getName();
            if (":none".equals(input)) {
                input = null;
            }
        }
        String output = null;
        if (op != null && op.getOutput() != null) {
            output = op.getOutput().getName();
            if (":none".equals(output)) {
                output = null;
            }
        }
        Operation op2 = portType.getOperation(op.getName(), input, output);
        return ((op2 == null) ? op : op2);
    }

    /**
     * Find the fault message relevant to a given name from the fault message
     * list
     *
     * @param name
     * @param faultMessages
     */
    private AxisMessage findFaultMessage(String name, ArrayList faultMessages) {
        AxisMessage tempMessage;
        for (int i = 0; i < faultMessages.size(); i++) {
            tempMessage = (AxisMessage) faultMessages.get(i);
            if (name.equals(tempMessage.getName())) {
                return tempMessage;
            }

        }
        return null;
    }

    /**
     * Add the QName for the binding input
     *
     * @param inMessage
     * @param wsdl4jOperation
     * @param bindingInput
     * @param isWrapped       - basically whether the operation is soap/rpc or not
     */
    private void addQNameReference(AxisMessage inMessage,
                                   Operation wsdl4jOperation, BindingInput bindingInput,
                                   boolean isWrapped) {

        List extensibilityElements = bindingInput.getExtensibilityElements();
        Message wsdl4jMessage = wsdl4jOperation.getInput().getMessage();

        addQNameReference(inMessage,
                          wsdl4jOperation,
                          isWrapped,
                          extensibilityElements,
                          wsdl4jMessage,
                          wsdl4jOperation.getName());
    }

    /**
     * Add the QName for the binding output
     *
     * @param outMessage
     * @param wsdl4jOperation
     * @param isWrapped
     */
    private void addQNameReference(AxisMessage outMessage,
                                   Operation wsdl4jOperation, BindingOutput bindingOutput,
                                   boolean isWrapped) {

        if (bindingOutput != null) {
            List extensibilityElements = bindingOutput.getExtensibilityElements();
            if (wsdl4jOperation.getOutput() == null) {
                return;
            }
            Message wsdl4jMessage = wsdl4jOperation.getOutput().getMessage();

            addQNameReference(outMessage,
                              wsdl4jOperation,
                              isWrapped,
                              extensibilityElements,
                              wsdl4jMessage,
                              wsdl4jOperation.getName() + WRAPPED_OUTPUTNAME_SUFFIX);
        }
    }

    private void addQNameReference(AxisMessage message,
                                   Operation wsdl4jOperation,
                                   boolean isWrapped,
                                   List extensibilityElements,
                                   Message wsdl4jMessage,
                                   String rpcOperationName) {
        if (isWrapped) {
            // we have already validated and process the qname references
            // so set it here
            // The schema for this should be already made ! Find the
            // QName from
            // the list and add it - the name for this is just the
            message.setElementQName((QName) resolvedRpcWrappedElementMap
                    .get(rpcOperationName));
            ((AxisService)message.getParent().getParent()).addMessageElementQNameToOperationMapping(
                    (QName)resolvedRpcWrappedElementMap.get(rpcOperationName),
                            (AxisOperation)message.getParent());
        } else {
            // now we are sure this is an document literal type element
            List bindingPartsList = getPartsListFromSoapBody(extensibilityElements);
            if (bindingPartsList == null) {
                // i.e user has not given any part list so we go to message and pick the firest part if
                // available
                if ((wsdl4jMessage.getParts() != null) && (wsdl4jMessage.getParts().size() > 0)) {
                    if (wsdl4jMessage.getParts().size() == 1) {
                        Part part = (Part) wsdl4jMessage.getParts().values().iterator().next();
                        QName elementName = part.getElementName();
                        if (elementName != null) {
                            message.setElementQName(elementName);
                            ((AxisService) message.getParent().getParent())
                                    .addMessageElementQNameToOperationMapping(elementName,
                                                                              (AxisOperation) message
                                                                                      .getParent());
                        } else {
                            throw new WSDLProcessingException(
                                    "No element type is defined for message " +
                                            wsdl4jMessage.getQName().getLocalPart());
                        }
                    } else {
                        // user has specified more than one parts with out specifing a part in
                        // soap body
                        throw new WSDLProcessingException("More than one part for message " +
                                wsdl4jMessage.getQName().getLocalPart());
                    }
                } else {
                    // this is allowed in the spec in this case element qname is null and nothing is send
                    // in the soap body
                    message.setElementQName(null);
                }
            } else {
                if (bindingPartsList.size() == 0) {
                    // we donot have to set the element qname
                    message.setElementQName(null);
                } else if (bindingPartsList.size() == 1) {
                    Part part = wsdl4jMessage.getPart((String) bindingPartsList.get(0));
                    if (part != null) {
                        QName elementName = part.getElementName();
                        if (elementName != null) {
                            message.setElementQName(elementName);
                            ((AxisService) message.getParent().getParent())
                                    .addMessageElementQNameToOperationMapping(elementName,
                                                                              (AxisOperation) message
                                                                                      .getParent());
                        } else {
                            throw new WSDLProcessingException(
                                    "No element type is defined for message" +
                                            wsdl4jMessage.getQName().getLocalPart());
                        }
                    } else {
                        throw new WSDLProcessingException("Missing part named "
                                + bindingPartsList.get(0) + " ");
                    }

                } else {
                    // i.e more than one part specified in this case we have
                    // to send an exception
                    throw new WSDLProcessingException(
                            "More than one element part is not allwed in document literal " +
                                    " type binding operation " + wsdl4jOperation.getName());
                }
            }

        }

    }

    /**
     * Add the QName for the binding output
     */
    private void addQNameReference(AxisMessage faultMessage,
                                   Message wsdl4jMessage) throws AxisFault {

        // for a fault this is trivial - All faults are related directly to a
        // message by the name and are supposed to have a single part. So it is
        // a matter of copying the right QName from the message part

        // get the part
        Map parts = wsdl4jMessage.getParts();
        if (parts == null || parts.size()==0) {
            String message = "There are no parts"
                    + " for fault message : "
                    + wsdl4jMessage.getQName();
            log.error(message);
            throw new WSDLProcessingException(message);
        }
        Part wsdl4jMessagePart = (Part) parts.values()
                .toArray()[0];
        if (wsdl4jMessagePart == null) {
            throw new WSDLProcessingException();
        }
        QName name = wsdl4jMessagePart.getElementName();
        if (name == null) {
            String message = "Part '"
                    + wsdl4jMessagePart.getName()
                    + "' of fault message '"
                    + wsdl4jMessage.getQName()
                    + "' must be defined with 'element=QName' and not 'type=QName'";
            log.error(message);
            throw new AxisFault(message);
        }

        faultMessage.setElementQName(name);
    }

    /**
     * A util method that returns the SOAP style included in the binding
     * operation
     *
     * @param bindingOp
     */
    private String getSOAPStyle(BindingOperation bindingOp) {
        List extensibilityElements = bindingOp.getExtensibilityElements();
        for (int i = 0; i < extensibilityElements.size(); i++) {
            Object extElement = extensibilityElements.get(i);
            if (extElement instanceof SOAPOperation) {
                return ((SOAPOperation) extElement).getStyle();
            } else if (extElement instanceof SOAP12Operation) {
                return ((SOAP12Operation) extElement).getStyle();
            }

        }
        return null;
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

        // set port the type name
        axisService.setEndpointName(wsdl4jPortType.getQName().getLocalPart());

        Iterator wsdl4JOperationsIterator = wsdl4jPortType.getOperations()
                .iterator();
        Operation wsdl4jOperation;
        while (wsdl4JOperationsIterator.hasNext()) {
            wsdl4jOperation = (Operation) wsdl4JOperationsIterator.next();
            axisService.addOperation(populateOperations(wsdl4jOperation,
                                                        wsdl4jPortType, dif));
        }
    }

    /**
     * Copy the component from the operation
     *
     * @param wsdl4jOperation
     * @param dif
     * @throws AxisFault
     */
    private AxisOperation populateOperations(Operation wsdl4jOperation,
                                             PortType wsdl4jPortType, Definition dif)
            throws AxisFault {
        QName opName = new QName(wsdl4jOperation.getName());
        // Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);
        if (axisOperation == null) {
            String MEP = getMEP(wsdl4jOperation);
            axisOperation = AxisOperationFactory.getOperationDescription(MEP);
            axisOperation.setName(opName);

            // setting the PolicyInclude property of the AxisOperation
            PolicyInclude policyInclude = new PolicyInclude(axisOperation);
            axisOperation.setPolicyInclude(policyInclude);
        }

        copyExtensibleElements(wsdl4jOperation.getExtensibilityElements(), dif,
                               axisOperation, PORT_TYPE_OPERATION);

        Input wsdl4jInputMessage = wsdl4jOperation.getInput();

        if (isServerSide) {
            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {
                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                                           dif, inMessage, PORT_TYPE_OPERATION_INPUT);

                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                ArrayList inputActions = axisOperation.getWsamappingList();
                String action = null;
                if (inputActions == null || inputActions.size() == 0) {
                    action = WSDL11ActionHelper
                            .getActionFromInputElement(dif, wsdl4jPortType,
                                                       wsdl4jOperation, wsdl4jInputMessage);
                }
                if (action != null) {
                    if (inputActions == null) {
                        inputActions = new ArrayList();
                        axisOperation.setWsamappingList(inputActions);
                    }
                    inputActions.add(action);
                    axisService.mapActionToOperation(action, axisOperation);
                }
            }
            // Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {

                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                                           dif, outMessage, PORT_TYPE_OPERATION_OUTPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                String action = axisOperation.getOutputAction();
                if (action == null) {
                    action = WSDL11ActionHelper.getActionFromOutputElement(dif,
                                                                           wsdl4jPortType,
                                                                           wsdl4jOperation,
                                                                           wsdl4jOutputMessage);
                }
                if (action != null) {
                    axisOperation.setOutputAction(action);
                }
            }
        } else {

            // for the client side we have to do something that is a bit
            // weird. The in message is actually taken from the output
            // and the output is taken from the in

            if (null != wsdl4jInputMessage) {
                AxisMessage inMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                Message message = wsdl4jInputMessage.getMessage();
                if (null != message) {

                    inMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                                           dif, inMessage, PORT_TYPE_OPERATION_OUTPUT);

                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                String action = axisOperation.getOutputAction();
                if (action == null) {
                    action = WSDL11ActionHelper
                            .getActionFromInputElement(dif, wsdl4jPortType,
                                                       wsdl4jOperation, wsdl4jInputMessage);
                }
                if (action != null) {
                    axisOperation.setOutputAction(action);
                }
            }
            // Create an output message and add
            Output wsdl4jOutputMessage = wsdl4jOperation.getOutput();
            if (null != wsdl4jOutputMessage) {
                AxisMessage outMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message message = wsdl4jOutputMessage.getMessage();
                if (null != message) {

                    outMessage.setName(message.getQName().getLocalPart());
                    copyExtensibleElements(message.getExtensibilityElements(),
                                           dif, outMessage, PORT_TYPE_OPERATION_INPUT);

                    // wsdl:portType -> wsdl:operation -> wsdl:output
                }
                // Check if the action is already set as we don't want to
                // override it
                // with the Default Action Pattern
                ArrayList inputActions = axisOperation.getWsamappingList();
                String action = null;
                if (inputActions == null || inputActions.size() == 0) {
                    action = WSDL11ActionHelper.getActionFromOutputElement(dif,
                                                                           wsdl4jPortType,
                                                                           wsdl4jOperation,
                                                                           wsdl4jOutputMessage);
                }
                if (action != null) {
                    if (inputActions == null) {
                        inputActions = new ArrayList();
                        axisOperation.setWsamappingList(inputActions);
                    }
                    inputActions.add(action);
                }
            }
        }

        Map faults = wsdl4jOperation.getFaults();
        Iterator faultKeyIterator = faults.keySet().iterator();

        while (faultKeyIterator.hasNext()) {
            Fault fault = (Fault) faults.get(faultKeyIterator.next());
            AxisMessage axisFaultMessage = new AxisMessage();
            Message faultMessage = fault.getMessage();
            if (null != faultMessage) {
                axisFaultMessage
                        .setName(faultMessage.getQName().getLocalPart());

                copyExtensibleElements(faultMessage.getExtensibilityElements(),
                                       dif, axisFaultMessage, PORT_TYPE_OPERATION_FAULT);

            }

            // Check if the action is already set as we don't want to override
            // it
            // with the Default Action Pattern
            String action = axisOperation.getFaultAction(fault.getName());
            if (action == null) {
                action = WSDL11ActionHelper.getActionFromFaultElement(dif,
                                                                      wsdl4jPortType,
                                                                      wsdl4jOperation, fault);
            }
            if (action != null) {
                axisOperation.addFaultAction(fault.getName(), action);
            }
            axisOperation.setFaultMessages(axisFaultMessage);
        }
        return axisOperation;
    }

    /**
     * Generates a list of wrapper schemas
     *
     * @param wsdl4jBinding
     */
    private Element[] generateWrapperSchema(Map schemaMap, Binding wsdl4jBinding) {

        List schemaElementList = new ArrayList();
        // target namespace for this should be the namespace URI for
        // the porttype
        String porttypeNamespaceURI = wsdl4jBinding.getPortType().getQName().getNamespaceURI();

        // //////////////////////////////////////////////////////////////////////
        // if there are any bindings present then we have to process them. we
        // have to generate a schema per wsdl4jBinding (that is the safest
        // option).
        // if not we just resolve to
        // the good old port type
        // list, in which case we'll generate a schema per porttype
        // //////////////////////////////////////////////////////////////////////

        // findwrappable operations return either the rpc soap operations or
        // Http binding operations

        List wrappableBindingOperationsList = findWrappableBindingOperations(wsdl4jBinding);

        // this method returns all the new schemas created when processing the rpc messages
        Map newSchemaMap = createSchemaForPorttype(porttypeNamespaceURI,
                                                   wrappableBindingOperationsList, schemaMap);

        schemaElementList.addAll(newSchemaMap.values());
        return (Element[]) schemaElementList
                .toArray(new Element[schemaElementList.size()]);
    }

    /**
     * Create a schema by looking at the port type
     *
     * @param namespaceURI - namespace of the porttype uri we use this only if a user has not specified
     *                     a namespace in soap:body
     * @return null if there is no element
     */
    private Map createSchemaForPorttype(String namespaceURI,
                                        List operationListToProcess,
                                        Map existingSchemaMap) {

        // this map is used to keep the newly added schemas
        Map newSchemaMap = new HashMap();
        // first of all look at the operations list
        // we can return immediately if we get the operations list
        // as empty
        if (operationListToProcess.isEmpty()) {
            return newSchemaMap;
        }

        // loop through the messages. We'll populate thins map with the relevant
        // messages
        // from the operations
        Map messageQnameToMessageMap = new HashMap();
        Map operationToInputMessageMap = new HashMap();
        Map operationToOutputMessageMap = new HashMap();

        // this contains the required namespace imports. the key in this
        // map would be the namaspace URI
        Map namespaceImportsMap = null;
        // list namespace prefix map. This map will include uri -> prefix
        Map namespacePrefixMap = null;

        // //////////////////////////////////////////////////////////////////////////////////////////////////
        // First thing is to populate the message map with the messages to
        // process.
        // //////////////////////////////////////////////////////////////////////////////////////////////////

        // we really need to do this for a single porttype!
        BindingOperation op;
        for (int k = 0; k < operationListToProcess.size(); k++) {
            op = (BindingOperation) operationListToProcess.get(k);
            Input input = op.getOperation().getInput();
            Message message;
            if (input != null) {
                message = input.getMessage();
                messageQnameToMessageMap.put(message.getQName(), message);
                operationToInputMessageMap.put(op, message);
            }

            Output output = op.getOperation().getOutput();
            if (output != null) {
                message = output.getMessage();
                messageQnameToMessageMap.put(message.getQName(), message);
                operationToOutputMessageMap.put(op, message);
            }

            // we do not want to process fault messages since they can only be used as document type
            // see basic profile 4.4.2
        }

        // find the xsd prefix
        String xsdPrefix = findSchemaPrefix();
        // DOM document that will be the ultimate creator
        Document document = getDOMDocumentBuilder().newDocument();

        Element elementDeclaration;

        //loop through the input op map and generate the elements
        BindingOperation operation;
        for (Iterator operationsIter = operationToInputMessageMap.keySet().iterator();
             operationsIter.hasNext();) {

            operation = (BindingOperation) operationsIter.next();
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                    + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, operation.getName());

            //when creating the inner complex type we have to find the parts list from the binding input
            BindingInput bindingInput = operation.getBindingInput();
            Message message = (Message) operationToInputMessageMap.get(operation);

            if (bindingInput != null) {

                Iterator partsIterator = null;
                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    // first see the body parts list
                    List bodyPartsList =
                            getPartsListFromSoapBody(bindingInput.getExtensibilityElements());
                    if (bodyPartsList != null) {
                        partsIterator = message.getOrderedParts(bodyPartsList).iterator();
                    } else {
                        // then see the parameter order
                        List parameterOrder = operation.getOperation().getParameterOrdering();
                        if (parameterOrder != null) {
                            partsIterator = message.getOrderedParts(parameterOrder).iterator();
                        } else {
                            // finally get the message part list
                            partsIterator = message.getParts().values().iterator();
                        }

                    }
                } else {
                    // i.e http binding
                    partsIterator = message.getParts().values().iterator();
                }


                namespaceImportsMap = new HashMap();
                namespacePrefixMap = new HashMap();

                Node newComplexType = getNewComplextType(document,
                                                         xsdPrefix,
                                                         partsIterator,
                                                         namespaceImportsMap,
                                                         namespacePrefixMap);

                elementDeclaration.appendChild(newComplexType);
                String namespaceToUse = namespaceURI;

                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    String bodyNamespace =
                            getNamespaceFromSoapBody(bindingInput.getExtensibilityElements());
                    namespaceToUse = bodyNamespace != null ? bodyNamespace : namespaceURI;
                }

                if (existingSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is already exists with the original wsdl schemas
                    addElementToAnExistingSchema((Element) existingSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else if (newSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is with a newly created scheam
                    addElementToAnExistingSchema((Element) newSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else {
                    // i.e this element namespace has not found yet so
                    // we have to create new schema for it
                    Element newSchema = createNewSchemaWithElement(elementDeclaration,
                                                                  namespacePrefixMap,
                                                                  namespaceImportsMap,
                                                                  namespaceToUse,
                                                                  document,
                                                                  xsdPrefix);
                    newSchemaMap.put(namespaceToUse, newSchema);
                }
                resolvedRpcWrappedElementMap.put(operation.getName(), new QName(
                        namespaceToUse, operation.getName(), AXIS2WRAPPED));

            } else {
                throw new WSDLProcessingException(
                        "No binding input is defiend for binding operation ==> "
                                + operation.getName());
            }

        }

        // loop through the output to map and generate the elements
        for (Iterator operationsIterator = operationToOutputMessageMap.keySet().iterator();
             operationsIterator.hasNext();) {
            operation = (BindingOperation) operationsIterator.next();
            String baseoutputOpName = operation.getName();
            // see basic profile 4.7.19
            String outputOpName = baseoutputOpName + WRAPPED_OUTPUTNAME_SUFFIX;
            elementDeclaration = document.createElementNS(
                    XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                    + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            elementDeclaration.setAttribute(XSD_NAME, outputOpName);

            BindingOutput bindingOutput = operation.getBindingOutput();
            Message message = (Message) operationToOutputMessageMap.get(operation);

            if (bindingOutput != null) {
                Iterator partsIterator = null;
                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    // first see the body parts list
                    List bodyPartsList =
                            getPartsListFromSoapBody(bindingOutput.getExtensibilityElements());
                    if (bodyPartsList != null) {
                        bodyPartsList.add("result");
                        partsIterator = message.getOrderedParts(bodyPartsList).iterator();
                    } else {
                        // then see the parameter order
                        List parameterOrder = operation.getOperation().getParameterOrdering();
                        if (parameterOrder != null) {
                            parameterOrder.add("result");
                            partsIterator = message.getOrderedParts(parameterOrder).iterator();
                        } else {
                            // finally get the message part list
                            partsIterator = message.getParts().values().iterator();
                        }

                    }
                } else {
                    // i.e if http binding
                    partsIterator = message.getParts().values().iterator();
                }

                // we have to initialize the hash maps always since we add the elements onece we
                // generate it
                namespacePrefixMap = new HashMap();
                namespaceImportsMap = new HashMap();

                Node newComplexType = getNewComplextType(document,
                                                         xsdPrefix,
                                                         partsIterator,
                                                         namespaceImportsMap,
                                                         namespacePrefixMap);
                elementDeclaration.appendChild(newComplexType);

                String namespaceToUse = namespaceURI;

                if (BINDING_TYPE_SOAP.equals(this.bindingType)) {
                    String bodyNamespace =
                            getNamespaceFromSoapBody(bindingOutput.getExtensibilityElements());
                    namespaceToUse = bodyNamespace != null ? bodyNamespace : namespaceURI;
                }

                if (existingSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is already exists with the original wsdl schemas
                    addElementToAnExistingSchema((Element) existingSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else if (newSchemaMap.containsKey(namespaceToUse)) {
                    // i.e this namespace is with a newly created scheam
                    addElementToAnExistingSchema((Element) newSchemaMap.get(namespaceToUse),
                                                 elementDeclaration,
                                                 namespacePrefixMap,
                                                 namespaceImportsMap,
                                                 namespaceToUse);
                } else {
                    // i.e this element namespace has not found yet so
                    // we have to create new schema for it
                    Element newSchema = createNewSchemaWithElement(elementDeclaration,
                                                                  namespacePrefixMap,
                                                                  namespaceImportsMap,
                                                                  namespaceToUse,
                                                                  document,
                                                                  xsdPrefix);
                    newSchemaMap.put(namespaceToUse, newSchema);
                }
                resolvedRpcWrappedElementMap.put(outputOpName, new QName(
                        namespaceToUse, outputOpName, AXIS2WRAPPED));

            } else {
                throw new WSDLProcessingException(
                        "No binding out put is defined for binding operation ==>" +
                                operation.getName());
            }
        }

        return newSchemaMap;
    }

    private void addElementToAnExistingSchema(Element schemaElement,
                                              Element newElement,
                                              Map namespacePrefixMap,
                                              Map namespaceImportsMap,
                                              String targetNamespace) {

        Document ownerDocument = schemaElement.getOwnerDocument();

        // loop through the namespace declarations first and add them
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
                .keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            checkAndAddNamespaceDeclarations(s, namespacePrefixMap,
                                             schemaElement);
        }

        // add imports - check whether it is the targetnamespace before
        // adding
        Element[] namespaceImports = (Element[]) namespaceImportsMap
                .values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            if (!targetNamespace.equals(namespaceImports[i]
                    .getAttribute(NAMESPACE_URI))) {
                schemaElement.appendChild(ownerDocument.importNode(
                        namespaceImports[i], true));
            }
        }

        schemaElement.appendChild(ownerDocument.importNode(newElement, true));

    }

    private Element createNewSchemaWithElement(Element newElement,
                                              Map namespacePrefixMap,
                                              Map namespaceImportsMap,
                                              String targetNamespace,
                                              Document document,
                                              String xsdPrefix) {

        Element schemaElement = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                + XML_SCHEMA_LOCAL_NAME);

        // loop through the namespace declarations first
        String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
                .keySet().toArray(new String[namespacePrefixMap.size()]);
        for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
            String s = nameSpaceDeclarationArray[i];
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX
                                                 + namespacePrefixMap.get(s).toString(), s);
        }

        if (schemaElement.getAttributeNS(XML_NAMESPACE_URI, xsdPrefix).length() == 0) {
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX + xsdPrefix,
                                         XMLSCHEMA_NAMESPACE_URI);
        }

        // add the targetNamespace
        schemaElement.setAttributeNS(XML_NAMESPACE_URI, XMLNS_AXIS2WRAPPED, targetNamespace);
        schemaElement.setAttribute(XSD_TARGETNAMESPACE, targetNamespace);
        schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT, XSD_UNQUALIFIED);

        // add imports
        Element[] namespaceImports = (Element[]) namespaceImportsMap
                .values().toArray(new Element[namespaceImportsMap.size()]);
        for (int i = 0; i < namespaceImports.length; i++) {
            schemaElement.appendChild(namespaceImports[i]);

        }

        schemaElement.appendChild(newElement);
        return schemaElement;
    }

    private List getPartsListFromSoapBody(List extensibilityElements) {
        List partsList = null;
        ExtensibilityElement extElement;
        for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
            extElement = (ExtensibilityElement) iter.next();
            // SOAP 1.1 body element found!
            if (extElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody) extElement;
                if ((soapBody.getUse() != null) && (soapBody.getUse().equals(ENCODED_USE))) {
                    throw new WSDLProcessingException("Encoded use is not supported");
                }
                partsList = soapBody.getParts();
            } else if (extElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body) extElement;
                if ((soapBody.getUse() != null) && (soapBody.getUse().equals(ENCODED_USE))) {
                    throw new WSDLProcessingException("Encoded use is not supported");
                }
                partsList = soapBody.getParts();
            }
        }
        return partsList;
    }

    private String getNamespaceFromSoapBody(List extensibilityElements) {

        ExtensibilityElement extElement;
        String namespace = null;
        for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
            extElement = (ExtensibilityElement) iter.next();
            // SOAP 1.1 body element found!
            if (extElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody) extElement;
                namespace = soapBody.getNamespaceURI();
            } else if (extElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body) extElement;
                namespace = soapBody.getNamespaceURI();
            }
        }
        return namespace;
    }

    /**
     * creates a new shema complex element according to the elements sequence difined
     * this parts list is always for a message refering from the
     * soap rpc type operation
     *
     * @param document
     * @param xsdPrefix
     * @param partsIterator
     * @param namespaceImportsMap
     * @param namespacePrefixMap
     * @return the new complex type element
     */

    private Element getNewComplextType(Document document,
                                       String xsdPrefix,
                                       Iterator partsIterator,
                                       Map namespaceImportsMap,
                                       Map namespacePrefixMap) {
        // add the complex type
        Element newComplexType = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);

        Element cmplxTypeSequence = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                + XML_SCHEMA_SEQUENCE_LOCAL_NAME);
        Element child;
        Part part;

        while (partsIterator.hasNext()) {
            part = (Part) partsIterator.next();
            // the part name
            String elementName = part.getName();

            // the type name
            QName schemaTypeName = part.getTypeName();

            if (schemaTypeName != null) {

                child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI,
                                                 xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
                // always child attribute should be in no namespace
                child.setAttribute("form", "unqualified");

                String prefix;
                if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName.getNamespaceURI())) {
                    prefix = xsdPrefix;
                } else {
                    // this schema is a third party one. So we need to have
                    // an import statement in our generated schema
                    String uri = schemaTypeName.getNamespaceURI();
                    if (!namespaceImportsMap.containsKey(uri)) {
                        // create Element for namespace import
                        Element namespaceImport = document.createElementNS(
                                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                + XML_SCHEMA_IMPORT_LOCAL_NAME);
                        namespaceImport.setAttribute(NAMESPACE_URI, uri);
                        // add this to the map
                        namespaceImportsMap.put(uri, namespaceImport);
                        // we also need to associate this uri with a prefix
                        // and include that prefix
                        // in the schema's namspace declarations. So add
                        // theis particular namespace to the
                        // prefix map as well
                        prefix = getTemporaryNamespacePrefix();
                        namespacePrefixMap.put(uri, prefix);
                    } else {
                        // this URI should be already in the namspace prefix
                        // map
                        prefix = (String) namespacePrefixMap.get(uri);
                    }

                }

                child.setAttribute(XSD_NAME, elementName);
                child.setAttribute(XSD_TYPE, prefix + ":" + schemaTypeName.getLocalPart());


                cmplxTypeSequence.appendChild(child);

            } else {
                // see the basic profile 4.4.1 for rpc-literal messages parts can have only types
                throw new WSDLProcessingException("RPC-literal type message part " +
                        part.getName() + "should have a type attribute ");
            }

        }
        newComplexType.appendChild(cmplxTypeSequence);
        return newComplexType;
    }

    /**
     * @param prefixMap
     */
    private void checkAndAddNamespaceDeclarations(String namespace,
                                                  Map prefixMap, Element schemaElement) {
        // get the attribute for the current namespace
        String prefix = (String) prefixMap.get(namespace);
        // A prefix must be found at this point!
        String existingURL = schemaElement.getAttributeNS(XML_NAMESPACE_URI,
                                                          NAMESPACE_DECLARATION_PREFIX + prefix);
        if (existingURL == null || existingURL.length()==0) {
            // there is no existing URL by that prefix - declare a new namespace
                schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                             NAMESPACE_DECLARATION_PREFIX + prefix, namespace);
        } else if (existingURL.equals(namespace)) {
            // this namespace declaration is already there with the same prefix
            // ignore it
        } else {
            // there is a different namespace declared in the given prefix
            // change the prefix in the prefix map to a new one and declare it

            // create a prefix
            String generatedPrefix = "ns" + prefixCounter++;
            while (prefixMap.containsKey(generatedPrefix)) {
                generatedPrefix = "ns" + prefixCounter++;
            }
            schemaElement.setAttributeNS(XML_NAMESPACE_URI,
                                         NAMESPACE_DECLARATION_PREFIX + generatedPrefix, namespace);
            // add to the map
            prefixMap.put(namespace, generatedPrefix);
        }

    }

    /**
     * Read the WSDL file given the inputstream for the WSDL source
     *
     * @param in
     * @throws WSDLException
     */
    private Definition readInTheWSDLFile(InputStream in) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // switch off the verbose mode for all usecases
        reader.setFeature(JAVAX_WSDL_VERBOSE_MODE_KEY, false);

        // if the custem resolver is present then use it
        if (customWSLD4JResolver != null) {
            return reader.readWSDL(customWSLD4JResolver);
        } else {
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
     * <p/> Note - SOAP body extensible element will be processed differently
     *
     * @param wsdl4jExtensibleElements
     * @param description                   where is the ext element (port , portype , biding)
     * @param wsdl4jDefinition
     * @param originOfExtensibilityElements -
     *                                      this will indicate the place this extensibility element came
     *                                      from.
     */
    private void copyExtensibleElements(List wsdl4jExtensibleElements,
                                        Definition wsdl4jDefinition, AxisDescription description,
                                        String originOfExtensibilityElements) throws AxisFault {

        ExtensibilityElement wsdl4jExtensibilityElement;

        for (Iterator iterator = wsdl4jExtensibleElements.iterator(); iterator.hasNext();) {

            wsdl4jExtensibilityElement = (ExtensibilityElement) iterator.next();

            if (wsdl4jExtensibilityElement instanceof UnknownExtensibilityElement) {

                UnknownExtensibilityElement unknown =
                        (UnknownExtensibilityElement) (wsdl4jExtensibilityElement);
                QName type = unknown.getElementType();

                // <wsp:Policy>
                if (WSDLConstants.WSDL11Constants.POLICY.equals(type)) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: PolicyElement found " + unknown);
                    }
                    Policy policy = (Policy) PolicyUtil.getPolicyComponent(unknown.getElement());
                    int attachmentScope =
                            getPolicyAttachmentPoint(description, originOfExtensibilityElements);
                    if (attachmentScope > -1) {
                        description.getPolicyInclude().addPolicyElement(
                                attachmentScope, policy);
                    }
                    // <wsp:PolicyReference>
                } else if (WSDLConstants.WSDL11Constants.POLICY_REFERENCE
                        .equals(type)) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: PolicyReference found " + unknown);
                    }
                    PolicyReference policyReference = (PolicyReference) PolicyUtil
                            .getPolicyComponent(unknown.getElement());
                    int attachmentScope =
                            getPolicyAttachmentPoint(description, originOfExtensibilityElements);
                    if (attachmentScope > -1) {
                        description.getPolicyInclude().addPolicyRefElement(
                                attachmentScope, policyReference);
                    }
                } else if (AddressingConstants.Final.WSAW_USING_ADDRESSING
                        .equals(type)
                        || AddressingConstants.Submission.WSAW_USING_ADDRESSING
                        .equals(unknown.getElementType())) {
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: wsaw:UsingAddressing found " + unknown);
                    }
                    // FIXME We need to set this the appropriate Axis Description AxisEndpoint or
                    // AxisBinding .
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

                } else if (AddressingConstants.Final.WSAW_ANONYMOUS
                        .equals(unknown.getElementType())) {
                    if (originOfExtensibilityElements.equals(BINDING_OPERATION)) {
                        AxisOperation axisOperation = (AxisOperation) description;
                        if (unknown.getElement().getFirstChild() != null
                                && unknown.getElement().getFirstChild()
                                .getNodeType() == Node.TEXT_NODE) {
                            String anonymousValue = unknown.getElement()
                                    .getFirstChild().getNodeValue();
                            AddressingHelper.setAnonymousParameterValue(
                                    axisOperation, anonymousValue);
                        }
                    }

                } else {
                    // Ignore this element - it is a totally unknown element
                    if (isTraceEnabled) {
                        log.trace("copyExtensibleElements:: Unknown Extensibility Element found " +
                                unknown);
                    }
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAP12Address) {
                SOAP12Address soapAddress = (SOAP12Address) wsdl4jExtensibilityElement;
                SoapAddress address = new SoapAddress();
                address.setLocation(soapAddress.getLocationURI());
                if (description instanceof AxisEndpoint) {
                    ((AxisEndpoint) description)
                            .setProperty(WSDL2Constants.ATTR_WSOAP_ADDRESS, address);
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress) wsdl4jExtensibilityElement;
                SoapAddress address = new SoapAddress();
                address.setLocation(soapAddress.getLocationURI());
                if (description instanceof AxisEndpoint) {
                    ((AxisEndpoint) description)
                            .setProperty(WSDL2Constants.ATTR_WSOAP_ADDRESS, address);
                }
            } else if (wsdl4jExtensibilityElement instanceof HTTPAddress) {
                HTTPAddress httpAddress = (HTTPAddress) wsdl4jExtensibilityElement;
                HttpAddress address = new HttpAddress();
                address.setLocation(httpAddress.getLocationURI());
                if (description instanceof AxisEndpoint) {
                    ((AxisEndpoint) description)
                            .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, address);
                }

            } else if (wsdl4jExtensibilityElement instanceof Schema) {
                Schema schema = (Schema) wsdl4jExtensibilityElement;
                // just add this schema - no need to worry about the imported
                // ones
                axisService.addSchema(getXMLSchema(schema.getElement(), schema
                        .getDocumentBaseURI()));
            } else if (wsdl4jExtensibilityElement instanceof SOAP12Operation) {
                SOAP12Operation soapOperation = (SOAP12Operation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String style = soapOperation.getStyle();
                if (style != null) {
                    axisBindingOperation.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String soapActionURI = soapOperation.getSoapActionURI();
                if (soapActionURI != null) {
                    axisBindingOperation
                            .setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, soapActionURI);
                    axisBindingOperation.getAxisOperation().setSoapAction(soapActionURI);
                    axisService.mapActionToOperation(soapActionURI,
                                                     axisBindingOperation.getAxisOperation());
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String style = soapOperation.getStyle();
                if (style != null) {
                    axisBindingOperation.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String soapAction = soapOperation.getSoapActionURI();
                if ((soapAction != null) && (!soapAction.equals(""))) {
                    axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
                    axisBindingOperation.getAxisOperation().setSoapAction(soapAction);
                    axisService.mapActionToOperation(soapAction,
                                                     axisBindingOperation.getAxisOperation());
                }
            } else if (wsdl4jExtensibilityElement instanceof HTTPOperation) {
                HTTPOperation httpOperation = (HTTPOperation) wsdl4jExtensibilityElement;
                AxisBindingOperation axisBindingOperation = (AxisBindingOperation) description;

                String httpLocation = httpOperation.getLocationURI();
                if (httpLocation != null) {
                    // change the template to make it same as WSDL 2 template
                    httpLocation = httpLocation.replaceAll("\\(", "{");
                    httpLocation = httpLocation.replaceAll("\\)", "}");
                    axisBindingOperation
                            .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);

                }
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                                                 HTTPConstants.MEDIA_TYPE_X_WWW_FORM);


            } else if (wsdl4jExtensibilityElement instanceof SOAP12Header) {

                SOAP12Header soapHeader = (SOAP12Header) wsdl4jExtensibilityElement;
                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();

                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
                headerMessage.setUse(soapHeader.getUse());

                Boolean required = soapHeader.getRequired();

                if (required != null) {
                    headerMessage.setRequired(required.booleanValue());
                }

                if (wsdl4jDefinition != null) {
                    // find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());

                    if (msg == null) {
                        // TODO i18n this
                        throw new AxisFault("message "
                                + soapHeader.getMessage()
                                + " not found in the WSDL ");
                    }
                    Part msgPart = msg.getPart(soapHeader.getPart());

                    if (msgPart == null) {
                        // TODO i18n this
                        throw new AxisFault("message part "
                                + soapHeader.getPart()
                                + " not found in the WSDL ");
                    }
                    // see basic profile 4.4.2 Bindings and Faults header, fault and headerfaults
                    // can only have elements
                    headerMessage.setElement(msgPart.getElementName());
                }

                headerMessage.setMessage(soapHeader.getMessage());
                headerMessage.setPart(soapHeader.getPart());

                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    List soapHeaders =
                            (List) bindingMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders == null) {
                        soapHeaders = new ArrayList();
                        bindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER, soapHeaders);
                    }
                    soapHeaders.add(headerMessage);
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAPHeader) {

                SOAPHeader soapHeader = (SOAPHeader) wsdl4jExtensibilityElement;
                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();
                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
                headerMessage.setUse(soapHeader.getUse());
                Boolean required = soapHeader.getRequired();
                if (null != required) {
                    headerMessage.setRequired(required.booleanValue());
                }
                if (null != wsdl4jDefinition) {
                    // find the relevant schema part from the messages
                    Message msg = wsdl4jDefinition.getMessage(soapHeader
                            .getMessage());
                    if (msg == null) {
                        // todo i18n this
                        throw new AxisFault("message "
                                + soapHeader.getMessage()
                                + " not found in the WSDL ");
                    }
                    Part msgPart = msg.getPart(soapHeader.getPart());
                    if (msgPart == null) {
                        // todo i18n this
                        throw new AxisFault("message part "
                                + soapHeader.getPart()
                                + " not found in the WSDL ");
                    }
                    headerMessage.setElement(msgPart.getElementName());
                }
                headerMessage.setMessage(soapHeader.getMessage());

                headerMessage.setPart(soapHeader.getPart());

                if (description instanceof AxisBindingMessage) {
                    AxisBindingMessage bindingMessage = (AxisBindingMessage) description;
                    List soapHeaders =
                            (List) bindingMessage.getProperty(WSDL2Constants.ATTR_WSOAP_HEADER);
                    if (soapHeaders == null) {
                        soapHeaders = new ArrayList();
                        bindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER, soapHeaders);
                    }
                    soapHeaders.add(headerMessage);
                }
            } else if (wsdl4jExtensibilityElement instanceof SOAPBinding) {

                SOAPBinding soapBinding = (SOAPBinding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;

                axisBinding.setType(soapBinding.getTransportURI());

                String soapNamespaceURI = soapBinding.getElementType().getNamespaceURI();
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION, soapNamespaceURI);
                axisService.setSoapNsUri(soapNamespaceURI);

                String style = soapBinding.getStyle();
                if (style != null) {
                    axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

            } else if (wsdl4jExtensibilityElement instanceof SOAP12Binding) {

                SOAP12Binding soapBinding = (SOAP12Binding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;

                String soapNamesapceURI = soapBinding.getElementType().getNamespaceURI();
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION, soapNamesapceURI);
                axisService.setSoapNsUri(soapNamesapceURI);

                String style = soapBinding.getStyle();
                if (style != null) {
                    axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE, style);
                }

                String transportURI = soapBinding.getTransportURI();
                axisBinding.setType(transportURI);

            } else if (wsdl4jExtensibilityElement instanceof HTTPBinding) {
                HTTPBinding httpBinding = (HTTPBinding) wsdl4jExtensibilityElement;
                AxisBinding axisBinding = (AxisBinding) description;
                // set the binding style same as the wsd2 to process smoothly
                axisBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);
                axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpBinding.getVerb());
            }
        }
    }

    private int getPolicyAttachmentPoint(AxisDescription description,
                                         String originOfExtensibilityElements) {
        int result = -1; // Attachment Point Not Identified
        if (description instanceof AxisService) {
            // wsdl:service
            if (SERVICE.equals(originOfExtensibilityElements)) {
                result = PolicyInclude.SERVICE_POLICY;
                // wsdl:service -> wsdl:port
            } else if (PORT.equals(originOfExtensibilityElements)) {
                result = PolicyInclude.PORT_POLICY;
                // wsdl:binding
            } else if (BINDING.equals(originOfExtensibilityElements)) {
                result = PolicyInclude.BINDING_POLICY;
            }
            // TODO policy for wsdl:portType ?
        } else if (description instanceof AxisOperation) {
            // wsdl:portType -> wsdl:operation
            if (PORT_TYPE_OPERATION.equals(originOfExtensibilityElements)) {
                result = PolicyInclude.OPERATION_POLICY;
                // wsdl:binding -> wsdl:operation
            } else {
                result = PolicyInclude.BINDING_POLICY;
            }
        } else {
            // wsdl:portType -> wsdl:operation -> wsdl:input

            if (PORT_TYPE_OPERATION_INPUT.equals(originOfExtensibilityElements)) {
                result = PolicyInclude.INPUT_POLICY;
                // wsdl:binding -> wsdl:operation -> wsdl:input
            } else if (BINDING_OPERATION_INPUT
                    .equals(originOfExtensibilityElements)) {
                result = PolicyInclude.BINDING_INPUT_POLICY;
                // wsdl:portType -> wsdl:operation -> wsdl:put
            } else if (PORT_TYPE_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                result = PolicyInclude.OUTPUT_POLICY;
                // wsdl:binding -> wsdl:operation -> wsdl:output
            } else if (BINDING_OPERATION_OUTPUT
                    .equals(originOfExtensibilityElements)) {
                result = PolicyInclude.BINDING_OUTPUT_POLICY;
            }
            // TODO Faults ..
        }
        if (isTraceEnabled) {
            log.trace("getPolicyAttachmentPoint:: axisDescription=" + description +
                    " extensibilityPoint=" + originOfExtensibilityElements + " result=" + result);
        }
        return result;
    }

    /**
     * Look for the wrappable operations depending on the style
     *
     * @param binding
     */
    private List findWrappableBindingOperations(Binding binding) {
        // first find the global style declaration.
        // for a SOAP binding this can be only rpc or document
        // as per the WSDL spec (section 3.4) the default style is document

        // now we have to handle the http bindings case as well
        //

        boolean isRPC = false;
        boolean isSOAPBinding = false;
        boolean isHttpBinding = false;

        List extElements = binding.getExtensibilityElements();
        for (int i = 0; i < extElements.size(); i++) {
            if (extElements.get(i) instanceof SOAPBinding) {
                // we have a global SOAP binding!
                isSOAPBinding = true;
                SOAPBinding soapBinding = (SOAPBinding) extElements.get(i);
                if (RPC_STYLE.equals(soapBinding.getStyle())) {
                    // set the global style to rpc
                    isRPC = true;
                }
                this.bindingType = BINDING_TYPE_SOAP;
                break;
            } else if (extElements.get(i) instanceof SOAP12Binding) {
                // we have a global SOAP binding!
                isSOAPBinding = true;
                SOAP12Binding soapBinding = (SOAP12Binding) extElements.get(i);
                if (RPC_STYLE.equals(soapBinding.getStyle())) {
                    // set the global style to rpc
                    isRPC = true;
                }
                this.bindingType = BINDING_TYPE_SOAP;
                break;
            } else if (extElements.get(i) instanceof HTTPBinding) {
                isHttpBinding = true;
                this.bindingType = BINDING_TYPE_HTTP;
            }
        }

        // go through every operation and get their styles.
        // each one can have a style override from the global
        // styles. Depending on the style add the relevant operations
        // to the return list
        List returnList = new ArrayList();

        if (isHttpBinding || isSOAPBinding) {
            BindingOperation bindingOp;
            for (Iterator bindingOperationsIterator =
                    binding.getBindingOperations().iterator(); bindingOperationsIterator.hasNext();)
            {
                bindingOp = (BindingOperation) bindingOperationsIterator.next();
                if (isSOAPBinding) {
                    String style = getSOAPStyle(bindingOp);
                    if (style == null) {
                        // no style specified
                        // use the global style to determine whether to put this one or
                        // not
                        if (isRPC) {
                            returnList.add(bindingOp);
                        }
                    } else if (RPC_STYLE.equals(style)) {
                        // add to the list
                        returnList.add(bindingOp);
                    }
                    // if not RPC we just leave it - default is doc
                } else {
                    // i.e an http binding then we have to add the operation any way
                    returnList.add(bindingOp);
                }
            }
        }

        // if the binding is not either soap or http binding then we return and empty list

        // set this to the global list
        wrappableOperations = returnList;
        return returnList;
    }

    /**
     * Guess the MEP based on the order of messages
     *
     * @param operation
     * @throws AxisFault
     */
    private String getMEP(Operation operation) throws AxisFault {
        OperationType operationType = operation.getStyle();
        if (isServerSide) {
            if (operationType != null) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT;
                }

                if (operationType.equals(OperationType.ONE_WAY)) {
                    if (operation.getFaults().size() > 0) {
                        return WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY;
                    }
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY;
                }

                if (operationType.equals(OperationType.NOTIFICATION)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY;
                }

                if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN;
                }
                throw new AxisFault("Cannot Determine the MEP");
            }
        } else {
            if (operationType != null) {
                if (operationType.equals(OperationType.REQUEST_RESPONSE)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_IN;
                }

                if (operationType.equals(OperationType.ONE_WAY)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY;
                }

                if (operationType.equals(OperationType.NOTIFICATION)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY;
                }

                if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
                    return WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT;
                }
                throw new AxisFault("Cannot Determine the MEP");
            }
        }
        throw new AxisFault("Cannot Determine the MEP");
    }

    /**
     * Copies the extension attributes
     *
     * @param extAttributes
     * @param description
     * @param origin
     */
    private void copyExtensionAttributes(Map extAttributes,
                                         AxisDescription description, String origin) {

        QName key;
        QName value;

        for (Iterator iterator = extAttributes.keySet().iterator(); iterator
                .hasNext();) {
            key = (QName) iterator.next();

            if (Constants.URI_POLICY_NS.equals(key.getNamespaceURI())
                    && "PolicyURIs".equals(key.getLocalPart())) {

                value = (QName) extAttributes.get(key);
                String policyURIs = value.getLocalPart();

                if (policyURIs.length() != 0) {
                    String[] uris = policyURIs.split(" ");

                    PolicyReference ref;
                    for (int i = 0; i < uris.length; i++) {
                        ref = new PolicyReference();
                        ref.setURI(uris[i]);

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

    /**
     * Process the policy definitions
     *
     * @param definition
     */
    private void processPoliciesInDefintion(Definition definition) {
        List extElements = definition.getExtensibilityElements();

        ExtensibilityElement extElement;
        for (Iterator iterator = extElements.iterator(); iterator.hasNext();) {
            extElement = (ExtensibilityElement) iterator.next();

            if (extElement instanceof UnknownExtensibilityElement) {
                UnknownExtensibilityElement unknown = (UnknownExtensibilityElement) extElement;
                if (WSDLConstants.WSDL11Constants.POLICY.equals(unknown
                        .getElementType())) {

                    Policy policy = (Policy) PolicyUtil
                            .getPolicyComponent(unknown.getElement());

                    String key;
                    if ((key = policy.getName()) != null
                            || (key = policy.getId()) != null) {
                        registry.register(key, policy);
                    }

                }
            }
        }
    }

    /**
     * Inner class declaration for the processing exceptions
     */
    public static class WSDLProcessingException extends RuntimeException {
        public WSDLProcessingException() {
        }

        public WSDLProcessingException(String message) {
            super(message);
        }

        public WSDLProcessingException(Throwable cause) {
            super(cause);
        }

        public WSDLProcessingException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}