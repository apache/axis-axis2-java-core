package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.ExternalPolicySerializer;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyComponent;
import org.apache.neethi.PolicyReference;
import org.apache.neethi.PolicyRegistry;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.java2wsdl.Java2WSDLConstants;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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

public class AxisService2OM implements Java2WSDLConstants {

    private AxisService axisService;

    private String[] serviceEndpointURLs;

    private String targetNamespace;

    private OMElement definition;

    private OMNamespace soap;

    private OMNamespace soap12;

    private OMNamespace http;

    private OMNamespace mime;

    private OMNamespace tns;

    private OMNamespace wsdl;

    private String style;

    private String use;

    private String servicePath;

    private HashMap policiesInDefinitions;

    private ExternalPolicySerializer serializer;

    private HashMap messagesMap;

    public AxisService2OM(AxisService service, String[] serviceEndpointURLs,
                          String style, String use, String servicePath) {
        this.axisService = service;

        // the EPR list of AxisService contains REST EPRs as well. Those REST EPRs will be used to generated HTTPBinding
        // and rest of the EPRs will be used to generate SOAP 1.1 and 1.2 bindings. Let's first initialize those set of
        // EPRs now to be used later, especially when we generate the WSDL.
        this.serviceEndpointURLs = serviceEndpointURLs;

        if (style == null) {
            this.style = DOCUMENT;
        } else {
            this.style = style;
        }
        if (use == null) {
            this.use = LITERAL;
        } else {
            this.use = use;
        }
        this.servicePath = servicePath;
        this.targetNamespace = service.getTargetNamespace();

        serializer = new ExternalPolicySerializer();
        // CHECKME check whether service.getAxisConfiguration() return null ???

        AxisConfiguration configuration = service.getAxisConfiguration();
        if (configuration != null) {
            serializer.setAssertionsToFilter(configuration
                    .getLocalPolicyAssertions());
        }

    }

    public OMElement generateOM() throws Exception {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE,
                                     DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);
        setDefinitionElement(ele);

        policiesInDefinitions = new HashMap();

        Map namespaceMap = axisService.getNameSpacesMap();
        WSDLSerializationUtil.populateNamespaces(ele, namespaceMap);
        soap = ele.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
        soap12 = ele.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);
        http = ele.declareNamespace(HTTP_NAMESPACE, HTTP_PREFIX);
        mime = ele.declareNamespace(MIME_NAMESPACE, MIME_PREFIX);
        String prefix = WSDLSerializationUtil.getPrefix(axisService.getTargetNamespace(),
                                                        namespaceMap);
        if (prefix == null || "".equals(prefix)) {
            prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
        }

        namespaceMap.put(prefix, axisService.getTargetNamespace());
        tns = ele.declareNamespace(axisService.getTargetNamespace(), prefix);

        // adding documentation element
        // <documentation>&lt;b&gt;NEW!&lt;/b&gt; This method accepts an ISBN
        // string and returns &lt;b&gt;Amazon.co.uk&lt;/b&gt; Sales Rank for
        // that book.</documentation>
        String servicedescription = axisService.getServiceDescription();
        if (servicedescription != null && !"".equals(servicedescription)) {
            OMElement documenentattion = fac.createOMElement("documentation",
                                                             wsdl);
            documenentattion.setText(servicedescription);
            ele.addChild(documenentattion);
        }

        ele.addAttribute("targetNamespace", axisService.getTargetNamespace(),
                         null);
        OMElement wsdlTypes = fac.createOMElement("types", wsdl);
        ele.addChild(wsdlTypes);

        // populate the schema mappings
        axisService.populateSchemaMappings();

        ArrayList schemas = axisService.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            StringWriter writer = new StringWriter();

            // XmlSchema schema = (XmlSchema) schemas.get(i);
            XmlSchema schema = axisService.getSchema(i);

            String targetNamespace = schema.getTargetNamespace();
            if (!Constants.NS_URI_XML.equals(targetNamespace)) {
                schema.write(writer);
                String schemaString = writer.toString();
                if (!"".equals(schemaString)) {
                    wsdlTypes.addChild(XMLUtils.toOM(new StringReader(
                            schemaString)));
                }
            }
        }
        generateMessages(fac, ele);
        generatePortType(fac, ele);
        generateSOAP11Binding(fac, ele);
        generateSOAP12Binding(fac, ele);
        generateHTTPBinding(fac, ele);

        generateService(fac, ele);
        addPoliciesToDefinitionElement(policiesInDefinitions.values()
                .iterator(), definition);

        return ele;
    }

    private void generateMessages(OMFactory fac, OMElement defintions) {
        HashSet faultMessageNames = new HashSet();
        messagesMap = new HashMap();

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    writeMessage(inaxisMessage, fac, defintions);
                    generateHeaderMessages(inaxisMessage, fac, defintions);
                }
            }

            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    writeMessage(outAxisMessage, fac, defintions);
                    generateHeaderMessages(outAxisMessage, fac, defintions);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage axisMessage = (AxisMessage) faultyMessages
                            .get(i);
                    String name = axisMessage.getName();
                    if (faultMessageNames.add(name)) {
                        writeMessage(axisMessage, fac, defintions);
                        generateHeaderMessages(axisMessage, fac, defintions);
                    }
                }
            }
        }
    }

    private void generateHeaderMessages(AxisMessage axismessage, OMFactory fac,
                                        OMElement defintions) {
        ArrayList extList = axismessage.getSoapHeaders();
        for (int i = 0; i < extList.size(); i++) {
            SOAPHeaderMessage header = (SOAPHeaderMessage) extList.get(i);
            OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME,
                                                           wsdl);
            messageElement.addAttribute(ATTRIBUTE_NAME, header.getMessage()
                    .getLocalPart(), null);
            defintions.addChild(messageElement);
            OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME,
                                                        wsdl);
            messageElement.addChild(messagePart);
            messagePart.addAttribute(ATTRIBUTE_NAME, header.part(), null);
            if (header.getElement() == null) {
                throw new RuntimeException(ELEMENT_ATTRIBUTE_NAME
                        + " is null for " + header.getMessage());
            }
            messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME, WSDLSerializationUtil.getPrefix(header
                    .getElement().getNamespaceURI(), axisService.getNameSpacesMap())
                    + ":" + header.getElement().getLocalPart(), null);
        }
    }

    private void writeMessage(AxisMessage axismessage, OMFactory fac,
                              OMElement defintions) {
        if (messagesMap.get(axismessage.getName()) == null) {
            messagesMap.put(axismessage.getName(), axismessage);
            QName schemaElementName = axismessage.getElementQName();
            OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME,
                                                           wsdl);
            messageElement.addAttribute(ATTRIBUTE_NAME, axismessage.getName(),
                                        null);
            defintions.addChild(messageElement);
            if (schemaElementName != null) {
                OMElement messagePart = fac.createOMElement(
                        PART_ATTRIBUTE_NAME, wsdl);
                messageElement.addChild(messagePart);
                messagePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
                messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                                         WSDLSerializationUtil.getPrefix(schemaElementName.getNamespaceURI(), axisService.getNameSpacesMap()) + ":"
                                                 + schemaElementName.getLocalPart(), null);
            }
        }

    }

    /**
     * Generate the porttypes
     */
    private void generatePortType(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);
        defintions.addChild(portType);

        portType.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                + PORT_TYPE_SUFFIX, null);

        addPolicyAsExtAttribute(PolicyInclude.PORT_TYPE_POLICY, axisService
                .getPolicyInclude(), portType, fac);
        for (Iterator operations = axisService.getOperations(); operations.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation() || axisOperation.getName() == null) {
                continue;
            }
            String operationName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                                                      wsdl);
            portType.addChild(operation);
            operation.addAttribute(ATTRIBUTE_NAME, operationName, null);
            addPolicyAsExtElement(PolicyInclude.OPERATION_POLICY, axisOperation
                    .getPolicyInclude(), operation, fac);

            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                                                          wsdl);
                    input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + inaxisMessage.getName(), null);
                    addPolicyAsExtElement(PolicyInclude.INPUT_POLICY,
                                          inaxisMessage.getPolicyInclude(), input, fac);
                    WSDLSerializationUtil.addWSAWActionAttribute(input, axisOperation
                            .getInputAction());
                    operation.addChild(input);
                }
            }

            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                                                           wsdl);
                    output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + outAxisMessage.getName(), null);
                    addPolicyAsExtElement(PolicyInclude.OUTPUT_POLICY,
                                          outAxisMessage.getPolicyInclude(), output, fac);
                    WSDLSerializationUtil.addWSAWActionAttribute(output, axisOperation
                            .getOutputAction());
                    operation.addChild(output);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessage = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                                          wsdl);
                    fault.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + faultyMessage.getName(), null);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(),
                                       null);
                    WSDLSerializationUtil.addWSAWActionAttribute(fault, axisOperation
                            .getFaultAction(faultyMessage.getName()));
                    // TODO add policies for fault messages
                    operation.addChild(fault);
                }
            }

        }
    }

    /**
     * Generate the service
     */
    public void generateService(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
        defintions.addChild(service);
        service.addAttribute(ATTRIBUTE_NAME, axisService.getName(), null);
        generateSOAP11Ports(fac, service);
        generateSOAP12Ports(fac, service);

        addPolicyAsExtElement(PolicyInclude.SERVICE_POLICY, axisService
                .getPolicyInclude(), service, fac);
            generateHTTPPorts(fac, service);
        }

    private void generateSOAP11Ports(OMFactory fac, OMElement service)
            throws Exception {
        for (int i = 0; i < serviceEndpointURLs.length; i++) {
            String urlString = serviceEndpointURLs[i];
            if (urlString != null) {
                String protocol = urlString == null ? null : new URI(urlString)
                        .getScheme();
                if (urlString == null) {
                    urlString = "REPLACE_WITH_ACTUAL_URL";
                }
                OMElement port = fac.createOMElement(PORT, wsdl);
                service.addChild(port);
                String name = axisService.getName() + SOAP11PORT
                        + ((protocol == null) ? "" : "_" + protocol);
                if (i > 0) {
                    name += i;
                }
                port.addAttribute(ATTRIBUTE_NAME, name, null);
                port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                        + axisService.getName() + BINDING_NAME_SUFFIX, null);
                WSDLSerializationUtil.addExtensionElement(fac, port, SOAP_ADDRESS, LOCATION, urlString,
                                    soap);

                addPolicyAsExtElement(PolicyInclude.PORT_POLICY, axisService
                        .getPolicyInclude(), port, fac);
            }
        }

    }

    private void generateHTTPPorts(OMFactory fac, OMElement service)
            throws Exception {
        String[] exposedEPRs = axisService.getEPRs();
        for (int i = 0; i < exposedEPRs.length; i++) {
            String urlString = serviceEndpointURLs[i];
            if (urlString != null && urlString.startsWith("http")) {
                OMElement port = fac.createOMElement(PORT, wsdl);
                service.addChild(port);
                String name = axisService.getName() + HTTP_PORT;
                if (i > 0) {
                    name += i;
                }
                port.addAttribute(ATTRIBUTE_NAME, name, null);
                port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                        + axisService.getName() + HTTP_BINDING, null);
                OMElement extElement = fac.createOMElement("address", http);
                port.addChild(extElement);
//                urlString = urlString.replaceAll(servicePath, "rest");
                extElement.addAttribute("location", urlString, null);
            }
        }
    }

    private void generateSOAP12Ports(OMFactory fac, OMElement service)
            throws Exception {
        for (int i = 0; i < serviceEndpointURLs.length; i++) {
            String urlString = serviceEndpointURLs[i];
            if (urlString != null) {
                String protocol = urlString == null ? null : new URI(urlString)
                        .getScheme();
                if (urlString == null) {
                    urlString = "REPLACE_WITH_ACTUAL_URL";
                }
                OMElement port = fac.createOMElement(PORT, wsdl);
                service.addChild(port);
                String name = axisService.getName() + SOAP12PORT
                        + ((protocol == null) ? "" : "_" + protocol);
                if (i > 0) {
                    name += i;
                }
                port.addAttribute(ATTRIBUTE_NAME, name, null);
                port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                        + axisService.getName() + SOAP12BINDING_NAME_SUFFIX, null);
                WSDLSerializationUtil.addExtensionElement(fac, port, SOAP_ADDRESS, LOCATION, urlString,
                                    soap12);

                addPolicyAsExtElement(PolicyInclude.PORT_POLICY, axisService
                        .getPolicyInclude(), port, fac);
            }
        }
    }

    /**
     * Generate the bindings
     */
    private void generateSOAP11Binding(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                + BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":"
                + axisService.getName() + PORT_TYPE_SUFFIX, null);

        addPolicyAsExtElement(PolicyInclude.AXIS_SERVICE_POLICY, axisService
                .getPolicyInclude(), binding, fac);
        addPolicyAsExtElement(PolicyInclude.BINDING_POLICY, axisService
                .getPolicyInclude(), binding, fac);

        // Adding ext elements
        addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
                            TRANSPORT_URI, STYLE, style, soap);

        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_OPTIONAL)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(fac, binding,
                                AddressingConstants.USING_ADDRESSING,
                                DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                                wsawNamespace);
        } else if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_REQUIRED)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(fac, binding,
                                AddressingConstants.USING_ADDRESSING,
                                DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                                wsawNamespace);
        }

        for (Iterator operations = axisService.getOperations(); operations.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation() || axisOperation.getName() == null) {
                continue;
            }
            String operationName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                                                      wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getInputAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
                                SOAP_ACTION, soapAction, STYLE, style, soap);

            addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
                                  axisOperation.getPolicyInclude(), operation, fac);
            addPolicyAsExtElement(PolicyInclude.AXIS_OPERATION_POLICY,
                                  axisOperation.getPolicyInclude(), operation, fac);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, operationName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                                                          wsdl);
                    addExtensionElement(fac, input, SOAP_BODY, SOAP_USE, use,
                                        null, targetNamespace, soap);
                    addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
                                          inaxisMessage.getPolicyInclude(), input, fac);
                    operation.addChild(input);
                    writeSoapHeaders(inaxisMessage, fac, input, soap);
                }
            }

            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                                                           wsdl);
                    addExtensionElement(fac, output, SOAP_BODY, SOAP_USE, use,
                                        null, targetNamespace, soap);
                    addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
                                          outAxisMessage.getPolicyInclude(), output, fac);
                    operation.addChild(output);
                    writeSoapHeaders(outAxisMessage, fac, output, soap);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessage = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                                          wsdl);
                    addExtensionElement(fac, fault, FAULT_LOCAL_NAME, SOAP_USE, use,
                                        ATTRIBUTE_NAME, faultyMessage.getName(), soap12);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(),
                                       null);
                    // TODO adding policies for fault messages
                    operation.addChild(fault);
                    writeSoapHeaders(faultyMessage, fac, fault, soap);
                }
            }
        }

    }

    /**
     * Generate the bindings
     */
    private void generateSOAP12Binding(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                + SOAP12BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":"
                + axisService.getName() + PORT_TYPE_SUFFIX, null);

        addPolicyAsExtElement(PolicyInclude.AXIS_SERVICE_POLICY, axisService
                .getPolicyInclude(), binding, fac);
        addPolicyAsExtElement(PolicyInclude.BINDING_POLICY, axisService
                .getPolicyInclude(), binding, fac);

        // Adding ext elements
        addExtensionElement(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
                            TRANSPORT_URI, STYLE, style, soap12);

        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_OPTIONAL)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(fac, binding,
                                AddressingConstants.USING_ADDRESSING,
                                DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                                wsawNamespace);
        } else if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_REQUIRED)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(fac, binding,
                                AddressingConstants.USING_ADDRESSING,
                                DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                                wsawNamespace);
        }

        for (Iterator operations = axisService.getOperations(); operations.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation() || axisOperation.getName() == null) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                                                      wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getInputAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElement(fac, operation, OPERATION_LOCAL_NAME,
                                SOAP_ACTION, soapAction, STYLE, style, soap12);

            addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
                                  axisOperation.getPolicyInclude(), operation, fac);
            addPolicyAsExtElement(PolicyInclude.AXIS_OPERATION_POLICY,
                                  axisOperation.getPolicyInclude(), operation, fac);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                                                          wsdl);
                    addExtensionElement(fac, input, SOAP_BODY, SOAP_USE, use,
                                        null, targetNamespace, soap12);
                    addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
                                          inaxisMessage.getPolicyInclude(), input, fac);
                    operation.addChild(input);
                    writeSoapHeaders(inaxisMessage, fac, input, soap12);
                }
            }

            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                                                           wsdl);
                    addExtensionElement(fac, output, SOAP_BODY, SOAP_USE, use,
                                        null, targetNamespace, soap12);
                    addPolicyAsExtElement(PolicyInclude.BINDING_OUTPUT_POLICY,
                                          outAxisMessage.getPolicyInclude(), output, fac);
                    operation.addChild(output);
                    writeSoapHeaders(outAxisMessage, fac, output, soap12);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessage = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                                                          wsdl);
                    addExtensionElement(fac, fault, FAULT_LOCAL_NAME, SOAP_USE, use,
                                        ATTRIBUTE_NAME, faultyMessage.getName(), soap12);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessage.getName(),
                                       null);
                    // add policies for fault messages
                    operation.addChild(fault);
                    writeSoapHeaders(faultyMessage, fac, fault, soap12);
                }
            }
        }
    }

    private void generateHTTPBinding(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                + HTTP_BINDING, null);
        binding.addAttribute("type", tns.getPrefix() + ":"
                + axisService.getName() + PORT_TYPE_SUFFIX, null);

        // Adding ext elements
        OMElement httpBinding = fac.createOMElement("binding", http);
        binding.addChild(httpBinding);
        httpBinding.addAttribute("verb", "POST", null);


        for (Iterator operations = axisService.getOperations(); operations.hasNext();) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation() || axisOperation.getName() == null) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                                                      wsdl);
            binding.addChild(operation);

            OMElement httpOperation = fac.createOMElement("operation", http);
            operation.addChild(httpOperation);
            httpOperation.addAttribute("location", axisOperation.getName()
                    .getLocalPart(), null);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_IN_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                                                          wsdl);
                    OMElement inputelement = fac.createOMElement("content",
                                                                 mime);
                    input.addChild(inputelement);
                    inputelement.addAttribute("type", "text/xml", null);
                    operation.addChild(input);
                }
            }

            if (WSDL2Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDL2Constants.MEP_URI_OUT_OPTIONAL_IN
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OPTIONAL_OUT
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_ROBUST_OUT_ONLY
                    .equals(MEP)
                    || WSDL2Constants.MEP_URI_IN_OUT
                    .equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                                                           wsdl);
                    OMElement outElement = fac.createOMElement("content", mime);
                    outElement.addChild(outElement);
                    outElement.addAttribute("type", "text/xml", null);
                    output.addChild(outElement);
                    operation.addChild(output);
                }
            }
        }
    }

    private void writeSoapHeaders(AxisMessage inaxisMessage, OMFactory fac,
                                  OMElement input, OMNamespace soapNameSpace) throws Exception {
        ArrayList extElementList;
        extElementList = inaxisMessage.getSoapHeaders();
        if (extElementList != null) {
            Iterator elements = extElementList.iterator();
            while (elements.hasNext()) {
                SOAPHeaderMessage soapheader = (SOAPHeaderMessage) elements
                        .next();
                addSOAPHeader(fac, input, soapheader, soapNameSpace);
            }
        }
    }

    private void addExtensionElement(OMFactory fac, OMElement element,
                                     String name, String att1Name, String att1Value,
                                     String att2Name,
                                     String att2Value, OMNamespace soapNameSpace) {
        OMElement soapbinding = fac.createOMElement(name, soapNameSpace);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
        if (att2Name != null) {
            soapbinding.addAttribute(att2Name, att2Value, null);
        }
    }

    private void setDefinitionElement(OMElement defintion) {
        this.definition = defintion;
    }

    private void addSOAPHeader(OMFactory fac, OMElement element,
                               SOAPHeaderMessage header, OMNamespace soapNameSpace) {
        OMElement extElement = fac.createOMElement("header", soapNameSpace);
        element.addChild(extElement);
        String use = header.getUse();
        if (use != null) {
            extElement.addAttribute("use", use, null);
        }
        if (header.part() != null) {
            extElement.addAttribute("part", header.part(), null);
        }
        if (header.getMessage() != null) {
            extElement.addAttribute("message", WSDLSerializationUtil.getPrefix(targetNamespace, axisService.getNameSpacesMap()) + ":"
                    + header.getMessage().getLocalPart(), null);
        }
    }

    private void addPolicyAsExtElement(int type, PolicyInclude policyInclude,
                                       OMElement element, OMFactory factory) throws Exception {
        ArrayList elementList = policyInclude.getPolicyElements(type);

        for (Iterator iterator = elementList.iterator(); iterator.hasNext();) {
            Object policyElement = iterator.next();

            if (policyElement instanceof Policy) {
                element.addChild(PolicyUtil.getPolicyComponentAsOMElement(
                        (PolicyComponent) policyElement, serializer));

            } else if (policyElement instanceof PolicyReference) {
                element
                        .addChild(PolicyUtil
                                .getPolicyComponentAsOMElement((PolicyComponent) policyElement));

                PolicyRegistry reg = policyInclude.getPolicyRegistry();
                String key = ((PolicyReference) policyElement).getURI();

                if (key.startsWith("#")) {
                    key = key.substring(key.indexOf("#") + 1);
                }

                Policy p = reg.lookup(key);

                if (p == null) {
                    throw new Exception("Policy not found for uri : " + key);
                }

                addPolicyToDefinitionElement(key, p);
            }
        }
    }

    private void addPolicyAsExtAttribute(int type, PolicyInclude policyInclude,
                                         OMElement element, OMFactory factory) throws Exception {

        ArrayList elementList = policyInclude.getPolicyElements(type);
        ArrayList policyURIs = new ArrayList();

        for (Iterator iterator = elementList.iterator(); iterator.hasNext();) {
            Object policyElement = iterator.next();
            String key;

            if (policyElement instanceof Policy) {
                Policy p = (Policy) policyElement;

                if (p.getId() != null) {
                    key = "#" + p.getId();
                } else if (p.getName() != null) {
                    key = p.getName();
                } else {
                    throw new RuntimeException(
                            "Can't add the Policy as an extensibility attribute since it doesn't have a id or a name attribute");
                }

                policyURIs.add(key);
                addPolicyToDefinitionElement(key, p);

            } else {
                String uri = ((PolicyReference) policyElement).getURI();
                PolicyRegistry registry = policyInclude.getPolicyRegistry();

                if (uri.startsWith("#")) {
                    key = uri.substring(uri.indexOf('#') + 1);
                } else {
                    key = uri;
                }

                Policy p = registry.lookup(key);

                if (p == null) {
                    throw new RuntimeException("Cannot resolve " + uri
                            + " to a Policy");
                }
                addPolicyToDefinitionElement(key, p);
            }
        }

        if (!policyURIs.isEmpty()) {
            String value = null;

            /*
             * We need to create a String that is like 'uri1 uri2 .." to set as
             * the value of the wsp:PolicyURIs attribute.
             */
            for (Iterator iterator = policyURIs.iterator(); iterator.hasNext();) {
                String uri = (String) iterator.next();
                value = (value == null) ? uri : " " + uri;
            }

            OMNamespace ns = factory.createOMNamespace(
                    org.apache.neethi.Constants.URI_POLICY_NS,
                    org.apache.neethi.Constants.ATTR_WSP);
            OMAttribute URIs = factory.createOMAttribute("PolicyURIs", ns,
                                                         value);
            element.addAttribute(URIs);
        }
    }

    private void addPoliciesToDefinitionElement(Iterator iterator,
                                                OMElement definitionElement) throws Exception {
        Policy policy;
        OMElement policyElement;
        OMNode firstChild;

        for (; iterator.hasNext();) {
            policy = (Policy) iterator.next();
            policyElement = PolicyUtil.getPolicyComponentAsOMElement(policy,
                                                                     serializer);

            firstChild = definition.getFirstOMChild();

            if (firstChild != null) {
                firstChild.insertSiblingBefore(policyElement);
            } else {
                definitionElement.addChild(policyElement);
            }
        }
    }

    private void addPolicyToDefinitionElement(String key, Policy policy) {
        policiesInDefinitions.put(key, policy);
    }
}
