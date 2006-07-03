package org.apache.axis2.description;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyConstants;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.PolicyRegistry;
import org.apache.ws.policy.util.PolicyWriter;
import org.apache.ws.policy.util.StAXPolicyWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.*;

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

    private String[] urls;

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

    private boolean generateHttp = false;

    private HashMap policiesInDefinitions;

    public AxisService2OM(AxisService service, String[] serviceURL,
                          String style, String use, String servicePath) {
        this.axisService = service;
        urls = serviceURL;
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
    }

    public OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE,
                DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);
        setDefinitionElement(ele);

        policiesInDefinitions = new HashMap();

        Map nameSpaceMap = axisService.getNameSpacesMap();
        Iterator keys = nameSpaceMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(key)) {
                ele.declareDefaultNamespace((String) nameSpaceMap.get(key));
            } else {
                ele.declareNamespace((String) nameSpaceMap.get(key), key);
            }
        }
        soap = ele.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
        soap12 = ele.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);
        http = ele.declareNamespace(HTTP_NAMESPACE, HTTP_PREFIX);
        mime = ele.declareNamespace(MIME_NAMESPACE, MIME_PREFIX);
        String prefix = getPrefix(axisService.getTargetNamespace());
        if (prefix == null || "".equals(prefix)) {
            prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
        }
        axisService.getNameSpacesMap().put(prefix,
                axisService.getTargetNamespace());
        tns = ele.declareNamespace(axisService.getTargetNamespace(), prefix);

        // adding documentation element
        //<documentation>&lt;b&gt;NEW!&lt;/b&gt; This method accepts an ISBN string and returns &lt;b&gt;Amazon.co.uk&lt;/b&gt; Sales Rank for that book.</documentation>
        String servicedescription = axisService.getServiceDescription();
        if (servicedescription != null && !"".equals(servicedescription)) {
            OMElement documenentattion = fac.createOMElement("documentation", wsdl);
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
                    XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new StringReader(schemaString));

                    StAXOMBuilder staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
                    wsdlTypes.addChild(staxOMBuilder.getDocumentElement());
                }
            }
        }

        generateMessages(fac, ele);
        generatePortType(fac, ele);
        generateSOAP11Binding(fac, ele);
        generateSOAP12Binding(fac, ele);
        // generateHttp
        if (axisService.getParent() != null) {
            AxisDescription axisdesc = axisService.getParent().getParent();
            Parameter parameter = axisdesc.getParameter("enableHTTP");
            if (parameter != null) {
                Object value = parameter.getValue();
                if ("true".equals(value.toString())) {
                    generateHttp = true;
                    generatePostBinding(fac, ele);
                }
            }
        }
        generateService(fac, ele);
        addPoliciesToDefinitionElement(policiesInDefinitions.values().iterator(), definition, fac);

        return ele;
    }

    private void generateMessages(OMFactory fac, OMElement defintions) {
        HashSet faultMessageNames = new HashSet();

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    writeMessage(inaxisMessage, fac, defintions);
                    generateHeaderMessages(inaxisMessage, fac, defintions);
                }
            }

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
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
            messagePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
            messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME, getPrefix(header
                    .getElement().getNamespaceURI())
                    + ":" + header.getElement().getLocalPart(), null);
        }
    }

    private void writeMessage(AxisMessage axismessage, OMFactory fac,
                              OMElement defintions) {
        QName schemaElementName = axismessage.getElementQName();
        OMElement messageElement = fac
                .createOMElement(MESSAGE_LOCAL_NAME, wsdl);
        messageElement
                .addAttribute(ATTRIBUTE_NAME, axismessage.getName(), null);
        defintions.addChild(messageElement);
        if (schemaElementName != null) {
            OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME,
                    wsdl);
            messageElement.addChild(messagePart);
            messagePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
            messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                    getPrefix(schemaElementName.getNamespaceURI()) + ":"
                            + schemaElementName.getLocalPart(), null);
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

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
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
            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                            wsdl);
                    input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + inaxisMessage.getName(), null);
                    addPolicyAsExtElement(PolicyInclude.INPUT_POLICY,
                            inaxisMessage.getPolicyInclude(), input, fac);
                    if(axisOperation.getWsamappingList()!=null && axisOperation.getWsamappingList().size()>0){
                        String action = axisOperation.getWsamappingList().get(0).toString();
                        addWSAWActionAttribute(fac,input,action);
                    }
                    operation.addChild(input);
                }
            }

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                            wsdl);
                    output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + outAxisMessage.getName(), null);
                    addPolicyAsExtElement(PolicyInclude.OUTPUT_POLICY,
                            outAxisMessage.getPolicyInclude(), output, fac);
                    addWSAWActionAttribute(fac,output,axisOperation.getOutputAction());
                    operation.addChild(output);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                            wsdl);
                    fault.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                            + ":" + faultyMessge.getName(), null);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(),
                            null);
                    addWSAWActionAttribute(fac,fault,axisOperation.getFaultAction(faultyMessge.getName()));
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
        generateSOAP11Port(fac, service);
        generateSOAP12Port(fac, service);

        addPolicyAsExtElement(PolicyInclude.SERVICE_POLICY, axisService
                .getPolicyInclude(), service, fac);

        if (generateHttp) {
            generateHTTPPort(fac, service);
        }

    }

    private void generateSOAP11Port(OMFactory fac, OMElement service)
            throws Exception {
        for (int i = 0; i < urls.length; i++) {
            String urlString = urls[i];
            String protocol = urlString == null ? null : new URI(urlString).getScheme();
            if (urlString == null) {
                urlString = "REPLACE_WITH_ACTUAL_URL";
            }
            OMElement port = fac.createOMElement(PORT, wsdl);
            service.addChild(port);
            port.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                    + SOAP11PORT + ((protocol == null) ? "" : "_" + protocol), null);
            port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                    + axisService.getName() + BINDING_NAME_SUFFIX, null);
            addExtensionElement(fac, port, SOAP_ADDRESS, LOCATION, urlString,
                    soap);

            addPolicyAsExtElement(PolicyInclude.PORT_POLICY, axisService
                    .getPolicyInclude(), port, fac);
        }

    }

    private void generateHTTPPort(OMFactory fac, OMElement service)
            throws Exception {
        for (int i = 0; i < urls.length; i++) {
            String urlString = urls[i];
            if (urlString != null && urlString.startsWith("http")) {
                OMElement port = fac.createOMElement(PORT, wsdl);
                service.addChild(port);
                port.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                        + HTTP_PORT + i, null);
                port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                        + axisService.getName() + HTTP_BINDING, null);
                OMElement extElement = fac.createOMElement("address", http);
                port.addChild(extElement);
                urlString = urlString.replaceAll(servicePath, "rest");
                extElement.addAttribute("location", urlString, null);
            }
        }
    }

    private void generateSOAP12Port(OMFactory fac, OMElement service)
            throws Exception {
        for (int i = 0; i < urls.length; i++) {
            String urlString = urls[i];
            String protocol = urlString == null ? null : new URI(urlString).getScheme();
            if (urlString == null) {
                urlString = "REPLACE_WITH_ACTUAL_URL";
            }
            OMElement port = fac.createOMElement(PORT, wsdl);
            service.addChild(port);
            port.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                    + SOAP12PORT + ((protocol == null) ? "" : "_" + protocol), null);
            port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":"
                    + axisService.getName() + SOAP12BINDING_NAME_SUFFIX, null);
            addExtensionElement(fac, port, SOAP_ADDRESS, LOCATION, urlString,
                    soap12);

            addPolicyAsExtElement(PolicyInclude.PORT_POLICY, axisService
                    .getPolicyInclude(), port, fac);
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
        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
                TRANSPORT_URI, STYLE, style, soap);

        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_OPTIONAL)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            addExtensionElement(fac, binding,
                    AddressingConstants.USING_ADDRESSING,
                    DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                    wsawNamespace);
        } else if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_REQUIRED)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            addExtensionElement(fac, binding,
                    AddressingConstants.USING_ADDRESSING,
                    DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                    wsawNamespace);
        }

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                    wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getSoapAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, soapAction, STYLE, style, soap);

            addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
                    axisOperation.getPolicyInclude(), operation, fac);
            addPolicyAsExtElement(PolicyInclude.AXIS_OPERATION_POLICY,
                    axisOperation.getPolicyInclude(), operation, fac);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, input, SOAP_BODY, SOAP_USE, use,
                            null, targetNamespace, soap);
                    addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
                            inaxisMessage.getPolicyInclude(), input, fac);
                    operation.addChild(input);
                    writeSoapHeaders(inaxisMessage, fac, input, soap);
                }
            }

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, output, SOAP_BODY, SOAP_USE, use,
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
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, fault, SOAP_BODY, SOAP_USE, use,
                            null, targetNamespace, soap);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(),
                            null);
                    // TODO adding policies for fault messages
                    operation.addChild(fault);
                    writeSoapHeaders(faultyMessge, fac, fault, soap);
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
        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME, TRANSPORT,
                TRANSPORT_URI, STYLE, style, soap12);

        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_OPTIONAL)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            addExtensionElement(fac, binding,
                    AddressingConstants.USING_ADDRESSING,
                    DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                    wsawNamespace);
        } else if (axisService.getWSAddressingFlag().equals(
                AddressingConstants.ADDRESSING_REQUIRED)) {
            OMNamespace wsawNamespace = fac.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            addExtensionElement(fac, binding,
                    AddressingConstants.USING_ADDRESSING,
                    DEFAULT_WSDL_NAMESPACE_PREFIX + ":required", "true",
                    wsawNamespace);
        }

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                    wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getSoapAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, soapAction, STYLE, style, soap12);

            addPolicyAsExtElement(PolicyInclude.BINDING_OPERATION_POLICY,
                    axisOperation.getPolicyInclude(), operation, fac);
            addPolicyAsExtElement(PolicyInclude.AXIS_OPERATION_POLICY,
                    axisOperation.getPolicyInclude(), operation, fac);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, input, SOAP_BODY, SOAP_USE, use,
                            null, targetNamespace, soap12);
                    addPolicyAsExtElement(PolicyInclude.BINDING_INPUT_POLICY,
                            inaxisMessage.getPolicyInclude(), input, fac);
                    operation.addChild(input);
                    writeSoapHeaders(inaxisMessage, fac, input, soap12);
                }
            }

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, output, SOAP_BODY, SOAP_USE, use,
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
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages
                            .get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME,
                            wsdl);
                    addExtensionElemnet(fac, fault, SOAP_BODY, SOAP_USE, use,
                            null, targetNamespace, soap12);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(),
                            null);
                    // add policies for fault messages
                    operation.addChild(fault);
                    writeSoapHeaders(faultyMessge, fac, fault, soap12);
                }
            }
        }
    }

    private void generatePostBinding(OMFactory fac, OMElement defintions)
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

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
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

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
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

            if (WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                    || WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT.equals(MEP)) {
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

    private void addExtensionElemnet(OMFactory fac, OMElement element,
                                     String name, String att1Name, String att1Value, String att2Name,
                                     String att2Value, OMNamespace soapNameSpace) {
        OMElement soapbinding = fac.createOMElement(name, soapNameSpace);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
        if (att2Name != null) {
            soapbinding.addAttribute(att2Name, att2Value, null);
        }
    }

    private void addExtensionElement(OMFactory fac, OMElement element,
                                     String name, String att1Name, String att1Value,
                                     OMNamespace soapNameSpace) {
        OMElement extElement = fac.createOMElement(name, soapNameSpace);
        element.addChild(extElement);
        extElement.addAttribute(att1Name, att1Value, null);
    }

    private void setDefinitionElement(OMElement defintion) {
        this.definition = defintion;
    }

    private OMElement getDefinitionElement() {
        return definition;
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
            extElement.addAttribute("message", getPrefix(targetNamespace) + ":"
                    + header.getMessage().getLocalPart(), null);
        }
    }

    private String getPrefix(String targetNameSpace) {
        Map map = axisService.getNameSpacesMap();
        Iterator keys = map.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (map.get(key).equals(targetNameSpace)) {
                return key;
            }
        }
        return null;
    }

    private void addWSAWActionAttribute(OMFactory fac, OMElement element, String action){
        if(action==null || action.length()==0){
            return;
        }
        OMNamespace namespace = element.declareNamespace(AddressingConstants.Final.WSAW_NAMESPACE,"wsaw");
        element.addAttribute("Action", action, namespace);
    }
    
    private void addPolicyAsExtElement(int type, PolicyInclude policyInclude,
                                       OMElement element, OMFactory factory) throws Exception {
        ArrayList elementList = policyInclude.getPolicyElements(type);
        StAXPolicyWriter pwrt = (StAXPolicyWriter) PolicyFactory
                .getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);

        for (Iterator iterator = elementList.iterator(); iterator.hasNext();) {
            Object policyElement = iterator.next();

            if (policyElement instanceof Policy) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pwrt.writePolicy((Policy) policyElement, baos);

                ByteArrayInputStream bais = new ByteArrayInputStream(baos
                        .toByteArray());
                element.addChild(OMXMLBuilderFactory.createStAXOMBuilder(
                        factory, StAXUtils.createXMLStreamReader(bais))
                        .getDocumentElement());

            } else if (policyElement instanceof PolicyReference) {
                OMNamespace ns = factory.createOMNamespace(
                        PolicyConstants.POLICY_NAMESPACE_URI,
                        PolicyConstants.POLICY_PREFIX);
                OMElement refElement = factory.createOMElement(
                        PolicyConstants.POLICY_REFERENCE, ns);

                String policyURIString = ((PolicyReference) policyElement)
                        .getPolicyURIString();

                OMAttribute attribute = factory.createOMAttribute("URI", null,
                        policyURIString);
                refElement.addAttribute(attribute);
                element.addChild(refElement);

                PolicyRegistry reg = policyInclude.getPolicyRegistry();

                String key = policyURIString;

                if (policyURIString.startsWith("#")) {
                    key = policyURIString.substring(policyURIString
                            .indexOf("#") + 1);
                }

                Policy p = reg.lookup(key);

                if (p == null) {
                    throw new Exception("Policy not found for uri : "
                            + policyURIString);
                }

                addPolicyToDefinitionElement(key, p);

            }

        }
    }

    private void addPolicyAsExtAttribute(int type, PolicyInclude policyInclude,
                                         OMElement element, OMFactory factory) throws Exception {

        ArrayList elementList = policyInclude.getPolicyElements(type);
        StAXPolicyWriter pwrt = (StAXPolicyWriter) PolicyFactory
                .getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);

        ArrayList policyURIs = new ArrayList();

        String policyURI;

        for (Iterator iterator = elementList.iterator(); iterator.hasNext();) {
            Object policyElement = iterator.next();

            String id;

            if (policyElement instanceof Policy) {
                Policy p = (Policy) policyElement;

                if (p.getId() != null) {
                    id = "#" + p.getId();
                } else if (p.getName() != null) {
                    id = p.getName();
                } else {
                    throw new RuntimeException(
                            "Can't add the Policy as an extensibility attribute since it doesn't have a id or a name attribute");
                }

                policyURIs.add(id);
                addPolicyToDefinitionElement(id, p);

            } else {
                String policyURIString = ((PolicyReference) policyElement)
                        .getPolicyURIString();
                PolicyRegistry registry = policyInclude.getPolicyRegistry();

                String key;
                if (policyURIString.startsWith("#")) {
                    key = policyURIString.substring(policyURIString.indexOf('#') + 1);
                } else {
                    key = policyURIString;
                }

                Policy p = registry.lookup(key);

                if (p == null) {
                    throw new RuntimeException("Cannot resolve " + policyURIString + " to a Policy");
                }
                addPolicyToDefinitionElement(key, p);
            }
        }

        if (!policyURIs.isEmpty()) {
            String value = null;

            for (Iterator iterator = policyURIs.iterator(); iterator.hasNext();) {
                String uri = (String) iterator.next();
                value = (value == null) ? uri : " " + uri;
            }

            OMNamespace ns = factory.createOMNamespace(
                    PolicyConstants.POLICY_NAMESPACE_URI,
                    PolicyConstants.POLICY_PREFIX);
            OMAttribute URIs = factory.createOMAttribute("PolicyURIs", ns,
                    value);
            element.addAttribute(URIs);

        }

    }

    private void addPoliciesToDefinitionElement(Iterator iterator,
                                                OMElement definitionElement, OMFactory factory) throws Exception {
        Policy policy;
        ByteArrayInputStream bais;
        ByteArrayOutputStream baos;
        OMElement policyElement;
        OMNode firstChild;

        for (; iterator.hasNext();) {
            policy = (Policy) iterator.next();
            baos = new ByteArrayOutputStream();
            getPolicyWriter().writePolicy(policy, baos);

            bais = new ByteArrayInputStream(baos.toByteArray());
            policyElement = OMXMLBuilderFactory.createStAXOMBuilder(
                    OMAbstractFactory.getOMFactory(),
                    XMLInputFactory.newInstance().createXMLStreamReader(bais))
                    .getDocumentElement();

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

    /**
     * Returns a StAX based PolicyWriter.
     */
    private PolicyWriter getPolicyWriter() {
        return PolicyFactory.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
    }
}
