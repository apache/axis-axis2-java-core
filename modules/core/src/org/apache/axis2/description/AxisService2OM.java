package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Iterator;
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

public class AxisService2OM implements org.apache.ws.java2wsdl.Constants {

    private XmlSchema schema;

    private AxisService axisService;

    private String [] url;

    private String targetNamespace;
    private OMNamespace soap;
    private OMNamespace tns;
    private OMNamespace wsdl;

    private String style;
    private String use;

    public AxisService2OM(XmlSchema schema, AxisService service,
                          String [] serviceURL, String style, String use) {
        this.schema = schema;
        this.axisService = service;
        url = serviceURL;

        this.schema = schema;
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
        ele.declareNamespace(AXIS2_XSD, "ns1");
        ele.declareNamespace(SCHEMA_NAME_SPACE, DEFAULT_SCHEMA_NAMESPACE_PREFIX);
        soap = ele.declareNamespace(DEFAULT_SOAP_NAMESPACE, DEFAULT_SOAP_NAMESPACE_PREFIX);
        tns = ele.declareNamespace(DEFAULT_TARGET_NAMESPACE, TARGETNAMESPACE_PREFIX);
        ele.addAttribute("targetNamespace", DEFAULT_TARGET_NAMESPACE, null);
        OMElement wsdlTypes = fac.createOMElement("types", wsdl);
        StringWriter writer = new StringWriter();
        schema.write(writer);
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(new
                ByteArrayInputStream(writer.toString().getBytes()));

        StAXOMBuilder staxOMBuilder = new
                StAXOMBuilder(fac, xmlReader);
        wsdlTypes.addChild(staxOMBuilder.getDocumentElement());
        ele.addChild(wsdlTypes);
        generateMessages(fac, ele);
        generatePortType(fac, ele);
        generateBinding(fac, ele);
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
                    QName scheamElementName = inaxisMessage.getElementQName();
                    OMElement requestMessge = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
                    requestMessge.addAttribute(ATTRIBUTE_NAME, scheamElementName.getLocalPart()
                            + MESSAGE_SUFFIX, null);
                    defintions.addChild(requestMessge);
                    OMElement requestPart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
                    requestMessge.addChild(requestPart);
                    requestPart.addAttribute(ATTRIBUTE_NAME, "part1", null);
                    requestPart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                            scheamElementName.getPrefix() + ":" + scheamElementName.getLocalPart()
                                    + REQUEST, null);
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
                    QName scheamElementName = outAxisMessage.getElementQName();
                    OMElement responseMessge = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
                    responseMessge.addAttribute(ATTRIBUTE_NAME,
                            scheamElementName.getLocalPart() + MESSAGE_SUFFIX, null);
                    defintions.addChild(responseMessge);
                    OMElement responsePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
                    responseMessge.addChild(responsePart);
                    responsePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
                    responsePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                            scheamElementName.getPrefix() + ":" + scheamElementName.getLocalPart() + RESPONSE, null);
                }
            }
        }
    }

    /**
     * Generate the porttypes
     */
    private void generatePortType(OMFactory fac,
                                  OMElement defintions) {
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
                    QName scheamElementName = inaxisMessage.getElementQName();
                    OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
                    input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                            + scheamElementName.getLocalPart() + MESSAGE_SUFFIX, null);
                    operation.addChild(input);
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
                    QName scheamElementName = outAxisMessage.getElementQName();
                    OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                    output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                            + scheamElementName.getLocalPart() + MESSAGE_SUFFIX, null);
                    operation.addChild(output);
                }
            }
        }
    }

    /**
     * Generate the service
     */
    public void generateService(OMFactory fac,
                                OMElement defintions) {
        OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
        defintions.addChild(service);
        service.addAttribute(ATTRIBUTE_NAME, axisService.getName(), null);
        for (int i = 0; i < url.length; i++) {
            String urlString = url[i];
            OMElement port = fac.createOMElement(PORT, wsdl);
            service.addChild(port);
            port.addAttribute(ATTRIBUTE_NAME, axisService.getName() + PORT + i, null);
            port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":" +
                    axisService.getName() + BINDING_NAME_SUFFIX, null);
            addExtensionElemnet(fac, port, SOAP_ADDRESS, LOCATION,
                    urlString);
        }


    }

    /**
     * Generate the bindings
     */
    private void generateBinding(OMFactory fac,
                                 OMElement defintions) {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, axisService.getName() + BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":" + axisService.getName() + PORT_TYPE_SUFFIX, null);

        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME,
                TRANSPORT, TRANSPORT_URI,
                STYLE, style);
        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            String opeartionName = axisOperation.getName().getLocalPart();
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(operation);
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, opeartionName,
                    STYLE, style);
            operation.addAttribute(ATTRIBUTE_NAME, opeartionName, null);

            OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
            addExtensionElemnet(fac, input, SOAP_BODY, SOAP_USE, use, "namespace",
                    targetNamespace);
            operation.addChild(input);

            OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
            addExtensionElemnet(fac, output, SOAP_BODY, SOAP_USE, use, "namespace",
                    targetNamespace);
            operation.addChild(output);
        }
    }

    private void addExtensionElemnet(OMFactory fac,
                                     OMElement element,
                                     String name,
                                     String att1Name,
                                     String att1Value,
                                     String att2Name,
                                     String att2Value) {
        OMElement soapbinding = fac.createOMElement(name, soap);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
        soapbinding.addAttribute(att2Name, att2Value, null);
    }

    private void addExtensionElemnet(OMFactory fac,
                                     OMElement element,
                                     String name,
                                     String att1Name,
                                     String att1Value) {
        OMElement soapbinding = fac.createOMElement(name, soap);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
    }


}