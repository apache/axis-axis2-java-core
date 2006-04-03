package org.apache.axis2.description;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.extensions.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
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

public class AxisService2OM implements Java2WSDLConstants {

    private AxisService axisService;

    private String [] url;

    private String targetNamespace;
    private OMNamespace soap;
    private OMNamespace soap12;
    private OMNamespace tns;
    private OMNamespace wsdl;

    private String style;
    private String use;

    public AxisService2OM(AxisService service,
                          String [] serviceURL, String style, String use) {
        this.axisService = service;
        url = serviceURL;
        if (style == null) {
            this.style = DOCUMNT;
        } else {
            this.style = style;
        }
        if (use == null) {
            this.use = LITERAL;
        } else {
            this.use = use;
        }
        this.targetNamespace = service.getTargetNamespace();
    }

    public OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE,
                DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);
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
        String prefix = getPrefix(axisService.getTargetNamespace());
        if (prefix == null || "".equals(prefix)) {
            prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
            axisService.getNameSpacesMap().put(prefix, axisService.getTargetNamespace());
        }
        tns = ele.declareNamespace(axisService.getTargetNamespace(), prefix);


        ele.addAttribute("targetNamespace", axisService.getTargetNamespace(), null);
        OMElement wsdlTypes = fac.createOMElement("types", wsdl);
        ele.addChild(wsdlTypes);
        StringWriter writer = new StringWriter();
        axisService.printSchema(writer);
        if (!"".equals(writer.toString())) {
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(new
                    ByteArrayInputStream(writer.toString().getBytes()));

            StAXOMBuilder staxOMBuilder = new
                    StAXOMBuilder(fac, xmlReader);
            wsdlTypes.addChild(staxOMBuilder.getDocumentElement());
        }
        generateMessages(fac, ele);
        generatePortType(fac, ele);
        generateSOAP11Binding(fac, ele);
        generateSOAP12Binding(fac, ele);
        generateService(fac, ele);
        return ele;
    }

    private void generateMessages(OMFactory fac,
                                  OMElement defintions) {
        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();

            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDLConstants.MEP_URI_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    writeMessage(inaxisMessage, fac, defintions);
                    generateHeaderMessages(inaxisMessage, fac, defintions);
                }
            }

            if (WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
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
                    AxisMessage axisMessage = (AxisMessage) faultyMessages.get(i);
                    writeMessage(axisMessage, fac, defintions);
                    generateHeaderMessages(axisMessage, fac, defintions);
                }
            }
        }
    }

    private void generateHeaderMessages(AxisMessage axismessage, OMFactory fac, OMElement defintions) {
        ArrayList extList = axismessage.getWsdlExtElements();
        for (int i = 0; i < extList.size(); i++) {
            AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                    (AxisExtensiblityElementWrapper) extList.get(i);
            WSDLExtensibilityElement wsldExteElement = axisExtensiblityElementWrapper.getExtensibilityElement();
            if (wsldExteElement instanceof SOAPHeader) {
                SOAPHeader header = (SOAPHeader) wsldExteElement;
                OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
                messageElement.addAttribute(ATTRIBUTE_NAME, header.getMessage().getLocalPart()
                        , null);
                defintions.addChild(messageElement);
                OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
                messageElement.addChild(messagePart);
                messagePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
                messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                        getPrefix(header.getElement().getNamespaceURI()) + ":" + header.getElement().getLocalPart()
                        , null);
            }
        }
    }

    private void writeMessage(AxisMessage axismessage, OMFactory fac, OMElement defintions) {
        QName scheamElementName = axismessage.getElementQName();
        OMElement messageElement = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
        messageElement.addAttribute(ATTRIBUTE_NAME, axismessage.getName()
                + MESSAGE_SUFFIX, null);
        defintions.addChild(messageElement);
        if (scheamElementName != null) {
            OMElement messagePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
            messageElement.addChild(messagePart);
            messagePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
            messagePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                    getPrefix(scheamElementName.getNamespaceURI()) + ":" + scheamElementName.getLocalPart()
                    , null);
        }

    }

    /**
     * Generate the porttypes
     */
    private void generatePortType(OMFactory fac,
                                  OMElement defintions) throws Exception {
        OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);
        defintions.addChild(portType);
        portType.addAttribute(ATTRIBUTE_NAME, axisService.getName() + PORT_TYPE_SUFFIX, null);

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String operationName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            portType.addChild(operation);
            operation.addAttribute(ATTRIBUTE_NAME, operationName, null);

            String MEP = axisOperation.getMessageExchangePattern();
            if (WSDLConstants.MEP_URI_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
                    input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                            + inaxisMessage.getName() + MESSAGE_SUFFIX, null);
                    operation.addChild(input);
                    writePortTypePartsExtensibleElements(inaxisMessage, fac, input);
                }
            }

            if (WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                    output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                            + outAxisMessage.getName() + MESSAGE_SUFFIX, null);
                    operation.addChild(output);
                    writePortTypePartsExtensibleElements(outAxisMessage, fac, output);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages.get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);
                    fault.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                            + faultyMessge.getName() + MESSAGE_SUFFIX, null);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(), null);
                    operation.addChild(fault);
                    writePortTypePartsExtensibleElements(faultyMessge, fac, fault);
                }
            }
        }
    }

    private void writePortTypePartsExtensibleElements(AxisMessage faultyMessge,
                                                      OMFactory fac,
                                                      OMElement output) throws Exception {
        ArrayList extElementList = faultyMessge.getWsdlExtElements();
        if (extElementList != null) {
            Iterator elements = extElementList.iterator();
            while (elements.hasNext()) {
                AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                        (AxisExtensiblityElementWrapper) elements.next();
                if (axisExtensiblityElementWrapper.getType() ==
                        AxisExtensiblityElementWrapper.PORT_TYPE) {
                    WSDLExtensibilityElement wsdlextElement =
                            axisExtensiblityElementWrapper.getExtensibilityElement();
                    writeExtensibilityElement(wsdlextElement, fac, output, soap);
                }
            }
        }
    }

    /**
     * Generate the service
     */
    public void generateService(OMFactory fac,
                                OMElement defintions) throws Exception {
        OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
        defintions.addChild(service);
        service.addAttribute(ATTRIBUTE_NAME, axisService.getName(), null);
        generateSOAP11Port(fac, service);
        generateSOAP12Port(fac, service);
    }

    private void generateSOAP11Port(OMFactory fac, OMElement service) throws Exception {
        for (int i = 0; i < url.length; i++) {
            String urlString = url[i];
            OMElement port = fac.createOMElement(PORT, wsdl);
            service.addChild(port);
            port.addAttribute(ATTRIBUTE_NAME, axisService.getName() + SOAP11PORT + i, null);
            port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":" +
                    axisService.getName() + BINDING_NAME_SUFFIX, null);
            addExtensionElemnet(fac, port, SOAP_ADDRESS, LOCATION,
                    urlString, soap);


            ArrayList extElementList = axisService.getWsdlExtElements();
            if (extElementList != null) {
                Iterator elements = extElementList.iterator();
                while (elements.hasNext()) {
                    AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                            (AxisExtensiblityElementWrapper) elements.next();
                    if (axisExtensiblityElementWrapper.getType() ==
                            AxisExtensiblityElementWrapper.PORT) {
                        WSDLExtensibilityElement wsdlextElement =
                                axisExtensiblityElementWrapper.getExtensibilityElement();
                        if (!(wsdlextElement instanceof SOAPAddress)) {
                            writeExtensibilityElement(wsdlextElement, fac, port, soap);
                        }
                    }
                }
            }
        }
    }

    private void generateSOAP12Port(OMFactory fac, OMElement service) throws Exception {
        for (int i = 0; i < url.length; i++) {
            String urlString = url[i];
            OMElement port = fac.createOMElement(PORT, wsdl);
            service.addChild(port);
            port.addAttribute(ATTRIBUTE_NAME, axisService.getName() + SOAP12PORT + i, null);
            port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":" +
                    axisService.getName() + SOAP12BINDING_NAME_SUFFIX, null);
            addExtensionElemnet(fac, port, SOAP_ADDRESS, LOCATION,
                    urlString, soap12);


            ArrayList extElementList = axisService.getWsdlExtElements();
            if (extElementList != null) {
                Iterator elements = extElementList.iterator();
                while (elements.hasNext()) {
                    AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                            (AxisExtensiblityElementWrapper) elements.next();
                    if (axisExtensiblityElementWrapper.getType() ==
                            AxisExtensiblityElementWrapper.PORT) {
                        WSDLExtensibilityElement wsdlextElement =
                                axisExtensiblityElementWrapper.getExtensibilityElement();
                        if (!(wsdlextElement instanceof SOAPAddress)) {
                            writeExtensibilityElement(wsdlextElement, fac, port, soap12);
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate the bindings
     */
    private void generateSOAP11Binding(OMFactory fac,
                                       OMElement defintions) throws Exception {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName() + BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":" + axisService.getName() + PORT_TYPE_SUFFIX, null);

        //Adding ext elements
        writeBindingExtensibleElements(fac, binding, soap);
        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME,
                TRANSPORT, TRANSPORT_URI,
                STYLE, style, soap);

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getSoapAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, soapAction,
                    STYLE, style, soap);
            //writing ext elements
            writeOperationExtensibleElements(axisOperation, fac, operation, soap);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDLConstants.MEP_URI_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, input, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap);
                    operation.addChild(input);
                    writeBidingPartExtensibleElements(inaxisMessage, fac, input, soap);
                }
            }

            if (WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, output, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap);
                    operation.addChild(output);
                    writeBidingPartExtensibleElements(outAxisMessage, fac, output, soap);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages.get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, fault, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(), null);
                    operation.addChild(fault);
                    writeBidingPartExtensibleElements(faultyMessge, fac, fault, soap);
                }
            }
        }
    }

    /**
     * Generate the bindings
     */
    private void generateSOAP12Binding(OMFactory fac,
                                       OMElement defintions) throws Exception {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName() + SOAP12BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":" + axisService.getName() + PORT_TYPE_SUFFIX, null);

        //Adding ext elements
        writeBindingExtensibleElements(fac, binding, soap12);
        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME,
                TRANSPORT, TRANSPORT_URI,
                STYLE, style, soap12);

        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(operation);
            String soapAction = axisOperation.getSoapAction();
            if (soapAction == null) {
                soapAction = "";
            }
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, soapAction,
                    STYLE, style, soap12);
            //writing ext elements
            writeOperationExtensibleElements(axisOperation, fac, operation, soap);

            String MEP = axisOperation.getMessageExchangePattern();

            if (WSDLConstants.MEP_URI_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage inaxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inaxisMessage != null) {
                    operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, input, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap12);
                    operation.addChild(input);
                    writeBidingPartExtensibleElements(inaxisMessage, fac, input, soap12);
                }
            }

            if (WSDLConstants.MEP_URI_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_ROBUST_IN_ONLY.equals(MEP) ||
                    WSDLConstants.MEP_URI_IN_OUT.equals(MEP)) {
                AxisMessage outAxisMessage = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (outAxisMessage != null) {
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, output, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap12);
                    operation.addChild(output);
                    writeBidingPartExtensibleElements(outAxisMessage, fac, output, soap12);
                }
            }

            // generate fault Messages
            ArrayList faultyMessages = axisOperation.getFaultMessages();
            if (faultyMessages != null) {
                for (int i = 0; i < faultyMessages.size(); i++) {
                    AxisMessage faultyMessge = (AxisMessage) faultyMessages.get(i);
                    OMElement fault = fac.createOMElement(FAULT_LOCAL_NAME, wsdl);
                    addExtensionElemnet(fac, fault, SOAP_BODY, SOAP_USE, use, "namespace",
                            targetNamespace, soap12);
                    fault.addAttribute(ATTRIBUTE_NAME, faultyMessge.getName(), null);
                    operation.addChild(fault);
                    writeBidingPartExtensibleElements(faultyMessge, fac, fault, soap12);
                }
            }
        }
    }

    private void writeBidingPartExtensibleElements(AxisMessage inaxisMessage,
                                                   OMFactory fac,
                                                   OMElement input,
                                                   OMNamespace soapNameSpace) throws Exception {
        ArrayList extElementList;
        extElementList = inaxisMessage.getWsdlExtElements();
        if (extElementList != null) {
            Iterator elements = extElementList.iterator();
            while (elements.hasNext()) {
                AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                        (AxisExtensiblityElementWrapper) elements.next();
                if (axisExtensiblityElementWrapper.getType() ==
                        AxisExtensiblityElementWrapper.PORT_BINDING) {
                    WSDLExtensibilityElement wsdlextElement =
                            axisExtensiblityElementWrapper.getExtensibilityElement();
                    if (!(wsdlextElement instanceof SOAPBody)) {
                        writeExtensibilityElement(wsdlextElement, fac, input, soapNameSpace);
                    }
                    if (wsdlextElement instanceof SOAPHeader) {
                        writeExtensibilityElement(wsdlextElement, fac, input, soapNameSpace);
                    }

                }
            }
        }
    }

    private void writeOperationExtensibleElements(AxisOperation axisOperation,
                                                  OMFactory fac,
                                                  OMElement operation,
                                                  OMNamespace soapNameSpace) throws Exception {
        ArrayList extElementList;
        extElementList = axisOperation.getWsdlExtElements();
        if (extElementList != null) {
            Iterator elements = extElementList.iterator();
            while (elements.hasNext()) {
                AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                        (AxisExtensiblityElementWrapper) elements.next();
                if (axisExtensiblityElementWrapper.getType() == AxisExtensiblityElementWrapper.PORT_BINDING) {
                    WSDLExtensibilityElement wsdlextElement = axisExtensiblityElementWrapper.getExtensibilityElement();
                    if (!(wsdlextElement instanceof SOAPOperation)) {
                        writeExtensibilityElement(wsdlextElement, fac, operation, soapNameSpace);
                    }
                }
            }
        }
    }

    private void writeBindingExtensibleElements(OMFactory fac,
                                                OMElement binding,
                                                OMNamespace soapNameSpace) throws Exception {
        ArrayList extElementList = axisService.getWsdlExtElements();
        if (extElementList != null) {
            Iterator elements = extElementList.iterator();
            while (elements.hasNext()) {
                AxisExtensiblityElementWrapper axisExtensiblityElementWrapper =
                        (AxisExtensiblityElementWrapper) elements.next();
                if (axisExtensiblityElementWrapper.getType() == AxisExtensiblityElementWrapper.PORT_BINDING) {
                    WSDLExtensibilityElement wsdlextElement = axisExtensiblityElementWrapper.getExtensibilityElement();
                    if (!(wsdlextElement instanceof SOAPBinding)) {
                        writeExtensibilityElement(wsdlextElement, fac, binding, soapNameSpace);
                    }
                }
            }
        }
    }

    private void addExtensionElemnet(OMFactory fac,
                                     OMElement element,
                                     String name,
                                     String att1Name,
                                     String att1Value,
                                     String att2Name,
                                     String att2Value,
                                     OMNamespace soapNameSpace) {
        OMElement soapbinding = fac.createOMElement(name, soapNameSpace);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
        soapbinding.addAttribute(att2Name, att2Value, null);
    }

    private void addExtensionElemnet(OMFactory fac,
                                     OMElement element,
                                     String name,
                                     String att1Name,
                                     String att1Value,
                                     OMNamespace soapNameSpace) {
        OMElement extElement = fac.createOMElement(name, soapNameSpace);
        element.addChild(extElement);
        extElement.addAttribute(att1Name, att1Value, null);
    }


    protected void writeExtensibilityElement(WSDLExtensibilityElement extElement,
                                             OMFactory fac,
                                             OMElement element,
                                             OMNamespace soapNameSpace) throws Exception {
        if (extElement instanceof SOAPAddress) {
            addExtensionElemnet(fac, element, SOAP_ADDRESS, LOCATION,
                    ((SOAPAddress) extElement).getLocationURI(), soapNameSpace);
        } else if (extElement instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding) extElement;
            addExtensionElemnet(fac, element, BINDING_LOCAL_NAME, TRANSPORT,
                    soapBinding.getTransportURI(), STYLE, soapBinding.getStyle(),
                    soapNameSpace);
        } else if (extElement instanceof SOAPHeader) {
            SOAPHeader soapHeader = (SOAPHeader) extElement;
            addSOAPHeader(fac, element, soapHeader, soapNameSpace);
        } else if (extElement instanceof SOAPOperation) {
            SOAPOperation soapop = (SOAPOperation) extElement;
            addExtensionElemnet(fac, element, OPERATION_LOCAL_NAME, SOAP_ACTION,
                    soapop.getSoapAction(), STYLE, soapop.getStyle(),
                    soapNameSpace);
        } else if (extElement instanceof SOAPBody) {
            SOAPBody soapBody = (SOAPBody) extElement;
            if (soapBody.getNamespaceURI() != null) {
                addExtensionElemnet(fac, element, SOAP_BODY, SOAP_USE,
                        soapBody.getUse(), "namespace", soapBody.getNamespaceURI(),
                        soapNameSpace);
            } else {
                addExtensionElemnet(fac, element, SOAP_BODY, SOAP_USE, soapBody.getUse(),
                        soapNameSpace);
            }
        } else if (extElement instanceof PolicyExtensibilityElement) {
            throw new UnsupportedOperationException();
        } else {
            if (extElement instanceof DefaultExtensibilityElement) {
                DefaultExtensibilityElement wsdlExetElement = (DefaultExtensibilityElement) extElement;
                String wsdl = DOM2Writer.nodeToString(wsdlExetElement.getElement());
                if (wsdl != null) {
                    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                    XMLStreamReader xmlReader = xmlInputFactory.createXMLStreamReader(new
                            ByteArrayInputStream(wsdl.getBytes()));
                    StAXOMBuilder staxOMBuilder = new
                            StAXOMBuilder(fac, xmlReader);
                    element.addChild(staxOMBuilder.getDocumentElement());
                }
            } else {
                throw new Exception(" Unknown extensibility element" + extElement.toString());
            }
        }
    }

    private void addSOAPHeader(OMFactory fac, OMElement element,
                               SOAPHeader header,
                               OMNamespace soapNameSpace) {
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
            extElement.addAttribute("message",
                    getPrefix(targetNamespace) + ":" +
                            header.getMessage().getLocalPart(), null);
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

}