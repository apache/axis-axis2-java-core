/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.net.URI;

public class AxisService2WSDL20 implements WSDL2Constants {

    private AxisService axisService;

    public AxisService2WSDL20(AxisService service) {
        this.axisService = service;
    }

    /**
     * Generates a WSDL 2.0 document for this web service
     * @return The WSDL2 document element
     * @throws Exception - Thrown in case an exception occurs
     */
    public OMElement generateOM() throws Exception {

        Map nameSpacesMap = axisService.getNameSpacesMap();
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace wsdl;

        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.WSDL_NAMESPACE)) {
            wsdl = omFactory
                    .createOMNamespace(WSDL2Constants.WSDL_NAMESPACE,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.WSDL_NAMESPACE, nameSpacesMap));
        } else {
            wsdl = omFactory
                    .createOMNamespace(WSDL2Constants.WSDL_NAMESPACE,
                                       WSDL2Constants.DEFAULT_WSDL_NAMESPACE_PREFIX);
        }

        OMElement descriptionElement = omFactory.createOMElement(WSDL2Constants.DESCRIPTION, wsdl);

        // Declare all the defined namespaces in the document
        WSDLSerializationUtil.populateNamespaces(descriptionElement, nameSpacesMap);

        descriptionElement.declareNamespace(axisService.getTargetNamespace(), axisService.getTargetNamespacePrefix());

        // Need to add the targetnamespace as an attribute according to the wsdl 2.0 spec
        OMAttribute targetNamespace = omFactory
                .createOMAttribute(WSDL2Constants.TARGET_NAMESPACE, null,
                                   axisService.getTargetNamespace());
        descriptionElement.addAttribute(targetNamespace);

        // Check whether the required namespaces are already in namespaceMap, if they are not
        // present declare them.
        OMNamespace wsoap;
        OMNamespace whttp;
        OMNamespace wsdlx;

        OMNamespace tns = omFactory
                .createOMNamespace(axisService.getTargetNamespace(),
                                   axisService.getTargetNamespacePrefix());
        if (nameSpacesMap != null && !nameSpacesMap.containsValue(WSDL2Constants.WSDL_NAMESPACE)) {
            descriptionElement.declareDefaultNamespace(WSDL2Constants.WSDL_NAMESPACE);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_SOAP)) {
            wsoap = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_SOAP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_SOAP, nameSpacesMap));
        } else {
            wsoap = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_SOAP, WSDL2Constants.SOAP_PREFIX);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_HTTP)) {
            whttp = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_HTTP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_HTTP, nameSpacesMap));
        } else {
            whttp = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_HTTP, WSDL2Constants.HTTP_PREFIX);
        }
        if (nameSpacesMap != null && nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_EXTENSIONS)) {
            wsdlx = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_EXTENSIONS,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_EXTENSIONS, nameSpacesMap));
        } else {
            wsdlx = descriptionElement.declareNamespace(WSDL2Constants.URI_WSDL2_EXTENSIONS,
                                                        WSDL2Constants.WSDL_EXTENTION_PREFIX);
        }

        // Add the documentation element
        String description;
        OMElement documentationElement =
                omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
        if ((description = axisService.getDocumentation()) != null) {
            OMText omText;
            if (description.indexOf(WSDLSerializationUtil.CDATA_START) > -1) {
                description = description.replaceFirst(WSDLSerializationUtil.CDATA_START_REGEX, "");
                description = description.replaceFirst(WSDLSerializationUtil.CDATA_END_REGEX, "");
                omText = omFactory.createOMText(description, XMLStreamConstants.CDATA);
            } else {
            omText =  omFactory.createOMText(description);
            }
            documentationElement.addChild(omText);
            descriptionElement.addChild(documentationElement);
        }

        // Add types element
        OMElement typesElement = omFactory.createOMElement(WSDL2Constants.TYPES_LOCAL_NALE, wsdl);
        axisService.populateSchemaMappings();
        ArrayList schemas = axisService.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            StringWriter writer = new StringWriter();
            XmlSchema schema = axisService.getSchema(i);

            if (!org.apache.axis2.namespace.Constants.URI_2001_SCHEMA_XSD
                    .equals(schema.getTargetNamespace())) {
                schema.write(writer);
                String schemaString = writer.toString();

                if (!"".equals(schemaString)) {
                    try {
                        typesElement.addChild(
                                XMLUtils.toOM(new ByteArrayInputStream(schemaString.getBytes())));
                    } catch (XMLStreamException e) {
                        throw AxisFault.makeFault(e);
                    }
                }
            }
        }
        descriptionElement.addChild(typesElement);

        Parameter parameter = axisService.getParameter(WSDL2Constants.INTERFACE_LOCAL_NAME);
        String interfaceName;
        if (parameter != null) {
            interfaceName = (String) parameter.getValue();
        } else {
            interfaceName = WSDL2Constants.DEFAULT_INTERFACE_NAME;
        }

        // Add the interface element
        descriptionElement.addChild(getInterfaceElement(wsdl, tns, wsdlx, omFactory, interfaceName));

        boolean disableREST = false;
        Parameter disableRESTParameter =
                axisService.getParameter(Constants.Configuration.DISABLE_REST);
        if (disableRESTParameter != null &&
                JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
            disableREST = true;
        }

        // Check whether the axisService has any endpoints. If they exists serialize them else
        // generate default endpoint elements.
        Set bindings = new HashSet();
        Map endpointMap = axisService.getEndpoints();
        if (endpointMap != null && endpointMap.size() > 0) {
            String[] eprs = axisService.getEPRs();
            if (eprs == null) {
                eprs = new String[]{axisService.getName()};
            }
            OMElement serviceElement = getServiceElement(wsdl, tns, omFactory, interfaceName);
            Iterator iterator = endpointMap.values().iterator();
            while (iterator.hasNext()) {
                // With the new binding hierachy in place we need to do some extra checking here.
                // If a service has both http and https listners up we should show two separate eprs
                // If the service was deployed with a WSDL and it had two endpoints for http and
                // https then we have two endpoints populated so we should serialize them instead
                // of updating the endpoints.
                AxisEndpoint axisEndpoint = (AxisEndpoint) iterator.next();
                AxisBinding axisBinding = axisEndpoint.getBinding();
                String type = axisBinding.getType();
                if (WSDL2Constants.URI_WSDL2_HTTP.equals(type)) {
                    if (disableREST) {
                        continue;
                    }
                }
                bindings.add(axisBinding);
                for (int i = 0; i < eprs.length; i++) {
                    String epr = eprs[i];
                    OMElement endpointElement = axisEndpoint.toWSDL20(wsdl, tns, whttp, epr);
                    boolean endpointAlreadyAdded = false;
                    Iterator endpointsAdded = serviceElement.getChildren();
                    while (endpointsAdded.hasNext()) {
                        OMElement endpoint = (OMElement) endpointsAdded.next();
                        // Checking whether a endpoint with the same binding and address exists.
                        if (endpoint.getAttribute(new QName(WSDL2Constants.BINDING_LOCAL_NAME))
                                .getAttributeValue().equals(endpointElement.getAttribute(
                                new QName(WSDL2Constants.BINDING_LOCAL_NAME)).getAttributeValue())
                                && endpoint
                                .getAttribute(new QName(WSDL2Constants.ATTRIBUTE_ADDRESS))
                                .getAttributeValue().equals(endpointElement.getAttribute(
                                new QName(WSDL2Constants.ATTRIBUTE_ADDRESS)).getAttributeValue())) {
                            endpointAlreadyAdded = true;
                        }

                    }
                    if (!endpointAlreadyAdded) {
                        serviceElement.addChild(endpointElement);
                    }
                }
            }
            Iterator iter = bindings.iterator();
            while (iter.hasNext()) {
                AxisBinding binding = (AxisBinding) iter.next();
                descriptionElement
                        .addChild(binding.toWSDL20(wsdl, tns, wsoap, whttp,
                                                   interfaceName,
                                                   axisService.getNameSpacesMap(),
                                                   axisService.getWSAddressingFlag(),
                                                   axisService.getName()));
            }

            descriptionElement.addChild(serviceElement);
        } else {

            // There are no andpoints defined hence generate default bindings and endpoints
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateSOAP11Binding(omFactory, axisService, wsdl, wsoap,
                                                                tns));
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateSOAP12Binding(omFactory, axisService, wsdl, wsoap,
                                                                tns));
            if (!disableREST) {
                descriptionElement.addChild(
                        WSDLSerializationUtil.generateHTTPBinding(omFactory, axisService, wsdl,
                                                                  whttp,
                                                                  tns));
            }
            descriptionElement
                    .addChild(WSDLSerializationUtil.generateServiceElement(omFactory, wsdl, tns,
                                                                           axisService, disableREST));
        }

        return descriptionElement;
    }

    /**
     * Generates the interface element for the service
     *
     * @param tns           - The target namespace
     * @param wsdlx         - wsdl extentions namespace
     * @param fac           - OMFactory
     * @param interfaceName - The name of the interface
     * @return - The generated interface element
     */
    private OMElement getInterfaceElement(OMNamespace wsdl, OMNamespace tns, OMNamespace wsdlx,
                                          OMFactory fac, String interfaceName) {

        OMElement interfaceElement = fac.createOMElement(WSDL2Constants.INTERFACE_LOCAL_NAME, wsdl);
        interfaceElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                                            interfaceName));
        Iterator iterator = axisService.getOperations();
        ArrayList interfaceOperations = new ArrayList();
        ArrayList interfaceFaults = new ArrayList();
        int i = 0;
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            interfaceOperations.add(i, generateInterfaceOperationElement(axisOperation, wsdl, tns, wsdlx));
            i++;
            Iterator faultsIterator = axisOperation.getFaultMessages().iterator();
            while (faultsIterator.hasNext()) {
                AxisMessage faultMessage = (AxisMessage) faultsIterator.next();
                String name = faultMessage.getName();
                if (!interfaceFaults.contains(name)) {
                    OMElement faultElement =
                            fac.createOMElement(WSDL2Constants.FAULT_LOCAL_NAME, wsdl);
                    faultElement.addAttribute(
                            fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, name));
                    faultElement.addAttribute(fac.createOMAttribute(
                            WSDL2Constants.ATTRIBUTE_ELEMENT, null, WSDLSerializationUtil
                            .getElementName(faultMessage, axisService.getNameSpacesMap())));
                    interfaceFaults.add(name);
                    interfaceElement.addChild(faultElement);
                }
            }

        }
        for (i = 0; i < interfaceOperations.size(); i++) {
            interfaceElement.addChild((OMNode) interfaceOperations.get(i));
        }
        return interfaceElement;
    }

    /**
     * Generates the service element for the service
     *
     * @param tns           - The target namespace
     * @param omFactory     - OMFactory
     * @param interfaceName - The name of the interface
     * @return - The generated service element
     */
    private OMElement getServiceElement(OMNamespace wsdl, OMNamespace tns, OMFactory omFactory,
                                        String interfaceName) {
        OMElement serviceElement =
                omFactory.createOMElement(WSDL2Constants.SERVICE_LOCAL_NAME, wsdl);
        serviceElement.addAttribute(
                omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                            axisService.getName()));
        serviceElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME,
                                                                null, tns.getPrefix() + ":" +
                interfaceName));
        return serviceElement;
    }

    /**
     * Generates the interface Operation element. As with the binding operations we dont need to
     * ask AxisMessage to serialize its message cause AxisMessage does not have specific properties
     * as bindings.
     * @param tns - The targetnamespace
     * @param wsdlx - The WSDL extentions namespace (WSDL 2.0)
     * @return The generated binding element
     */
    public OMElement generateInterfaceOperationElement(AxisOperation axisOperation, OMNamespace wsdl, OMNamespace tns, OMNamespace wsdlx) {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement axisOperationElement =
                omFactory.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
        WSDLSerializationUtil.addWSDLDocumentationElement(axisOperation, axisOperationElement, omFactory, wsdl);
        axisOperationElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME,
                                                                      null,
                                                                      axisOperation.getName().getLocalPart()));
        URI[] opStyle = (URI[]) axisOperation.getParameterValue(WSDL2Constants.OPERATION_STYLE);
        if (opStyle != null && opStyle.length > 0) {
            String style = opStyle[0].toString();
            for (int i = 1; i < opStyle.length; i++) {
                URI uri = opStyle[i];
                style = style + " " + uri;
            }
            axisOperationElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_STYLE, null, style));
        }
        axisOperationElement.addAttribute(omFactory.createOMAttribute(
                WSDL2Constants.ATTRIBUTE_NAME_PATTERN, null, axisOperation.getMessageExchangePattern()));
        Parameter param = axisOperation.getParameter(WSDL2Constants.ATTR_WSDLX_SAFE);
        if (param != null) {
            axisOperationElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_SAFE, wsdlx, (param.getValue()).toString()));
        }
        AxisService axisService = (AxisService) axisOperation.getParent();
        Map nameSpaceMap = axisService.getNameSpacesMap();

        // Add the input element
        AxisMessage inMessage = (AxisMessage) axisOperation.getChild(WSDLConstants.WSDL_MESSAGE_IN_MESSAGE);
        if (inMessage != null) {
            OMElement inMessageElement = omFactory.createOMElement(WSDL2Constants.IN_PUT_LOCAL_NAME, wsdl);
            inMessageElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ELEMENT, null, WSDLSerializationUtil.getElementName(inMessage, nameSpaceMap)));
            WSDLSerializationUtil.addWSAWActionAttribute(inMessageElement, axisOperation.getInputAction());
            WSDLSerializationUtil.addWSDLDocumentationElement(inMessage, inMessageElement, omFactory, wsdl);
            axisOperationElement.addChild(inMessageElement);
        }

        // Add the output element
        AxisMessage outMessage = (AxisMessage) axisOperation.getChild(WSDLConstants.WSDL_MESSAGE_OUT_MESSAGE);
        if (outMessage != null) {
            OMElement outMessageElement = omFactory.createOMElement(WSDL2Constants.OUT_PUT_LOCAL_NAME, wsdl);
            outMessageElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ELEMENT, null, WSDLSerializationUtil.getElementName(outMessage, nameSpaceMap)));
            WSDLSerializationUtil.addWSAWActionAttribute(outMessageElement, axisOperation.getOutputAction());
            WSDLSerializationUtil.addWSDLDocumentationElement(outMessage, outMessageElement, omFactory, wsdl);
            axisOperationElement.addChild(outMessageElement);
        }

        // Add the fault element
        ArrayList faults = axisOperation.getFaultMessages();
        if (faults != null) {
            Iterator iterator = faults.iterator();
            while (iterator.hasNext()) {
                AxisMessage faultMessage = (AxisMessage) iterator.next();
                OMElement faultElement;
                if (WSDLConstants.WSDL_MESSAGE_DIRECTION_IN.equals(faultMessage.getDirection())) {
                    faultElement = omFactory.createOMElement(WSDL2Constants.IN_FAULT_LOCAL_NAME, wsdl);
                } else {
                    faultElement = omFactory.createOMElement(WSDL2Constants.OUT_FAULT_LOCAL_NAME, wsdl);
                }
                faultElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF, null, tns.getPrefix() + ":" + faultMessage.getName()));
                WSDLSerializationUtil.addWSAWActionAttribute(faultElement, axisOperation.getFaultAction(faultMessage.getName()));
                WSDLSerializationUtil.addWSDLDocumentationElement(faultMessage, faultElement, omFactory, wsdl);
                axisOperationElement.addChild(faultElement);
            }
        }
        return axisOperationElement;
    }
}
