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
package org.apache.axis2.util;

import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.SOAPModuleMessage;
import org.apache.axis2.wsdl.HTTPHeaderMessage;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

/**
 * Helps the AxisService to WSDL process
 */
public class WSDLSerializationUtil {

    public static final String CDATA_START = "<![CDATA[";
    public static final String CDATA_START_REGEX = "<!\\[CDATA\\[";
    public static final String CDATA_END = "]]>";
    public static final String CDATA_END_REGEX = "\\]\\]>";

    /**
     * Given a namespace it returns the prefix for that namespace
     * @param namespace - The namespace that the prefix is needed for
     * @param nameSpaceMap - The namespaceMap
     * @return - The prefix of the namespace
     */
    public static String getPrefix(String namespace, Map nameSpaceMap) {
        Set keySet;
        if (nameSpaceMap != null && (keySet = nameSpaceMap.keySet()) != null) {
            Iterator keys = keySet.iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (nameSpaceMap.get(key).equals(namespace)) {
                    return key;
                }
            }
        }
        return null;
    }

    /**
     * Gets the correct element name for a given message
     * @param axisMessage - The axisMessage
     * @param nameSpaceMap - The namespaceMap
     * @return - The element name
     */
    public static String getElementName(AxisMessage axisMessage, Map nameSpaceMap) {
        QName elementQName = axisMessage.getElementQName();
        if (elementQName == null) {
            return WSDL2Constants.NMTOKEN_NONE;
        } else if (Constants.XSD_ANY.equals(elementQName)) {
            return WSDL2Constants.NMTOKEN_ANY;
        } else {
            String prefix =
                    WSDLSerializationUtil.getPrefix(elementQName.getNamespaceURI(), nameSpaceMap);
            return prefix + ":" + elementQName.getLocalPart();
        }
    }

    /**
     * Adds a soap header element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of soapHeaderMessages
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param element - The element that the header should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void addSOAPHeaderElements(OMFactory omFactory, ArrayList list, OMNamespace wsoap,
                                             OMElement element, Map nameSpaceMap) {
        for (int i = 0; i < list.size(); i++) {
            SOAPHeaderMessage soapHeaderMessage = (SOAPHeaderMessage) list.get(i);
            OMElement soapHeaderElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_HEADER, wsoap);
            QName qName = soapHeaderMessage.getElement();
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_ELEMENT, null,
                    getPrefix(qName.getNamespaceURI(), nameSpaceMap) + ":" + qName.getLocalPart()));
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_MUST_UNDERSTAND, null,
                    new Boolean(soapHeaderMessage.isMustUnderstand()).toString()));
            soapHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REQUIRED, null,
                    new Boolean(soapHeaderMessage.isRequired()).toString()));
            element.addChild(soapHeaderElement);
        }
    }

    /**
     * Adds a soap module element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of soapModuleMessages
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param element - The element that the header should be added to
     */
    public static void addSOAPModuleElements(OMFactory omFactory, ArrayList list, OMNamespace wsoap,
                                             OMElement element) {
        for (int i = 0; i < list.size(); i++) {
            SOAPModuleMessage soapModuleMessage = (SOAPModuleMessage) list.get(i);
            OMElement soapModuleElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_MODULE, wsoap);
            soapModuleElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REF, null, soapModuleMessage.getUri()));
            element.addChild(soapModuleElement);
        }
    }

    /**
     * Adds a HTTP header element to a given OMElement
     * @param omFactory - An OMFactory
     * @param list - The arraylist of HTTPHeaderMessages
     * @param whttp - The WSDL 2.0 HTTP namespace
     * @param element - The element that the header should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void addHTTPHeaderElements(OMFactory omFactory, ArrayList list, OMNamespace whttp,
                                             OMElement element, Map nameSpaceMap) {
        for (int i = 0; i < list.size(); i++) {
            HTTPHeaderMessage httpHeaderMessage = (HTTPHeaderMessage) list.get(i);
            OMElement httpHeaderElement =
                    omFactory.createOMElement(WSDL2Constants.ATTRIBUTE_HEADER, whttp);
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_NAME, null, httpHeaderMessage.getName()));
            QName qName = httpHeaderMessage.getqName();
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_TYPE, null,
                    getPrefix(qName.getNamespaceURI(), nameSpaceMap) + ":" + qName.getLocalPart()));
            httpHeaderElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_REQUIRED, null,
                    new Boolean(httpHeaderMessage.isRequired()).toString()));
            element.addChild(httpHeaderElement);
        }
    }

    /**
     * Generates a default SOAP 11 Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param tns - The target namespace
     * @return - The generated SOAP11Binding element
     */
    public static OMElement generateSOAP11Binding(OMFactory fac, AxisService axisService,
                                                  OMNamespace wsdl, OMNamespace wsoap, OMNamespace tns) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, axisService.getName() +
                        Java2WSDLConstants.BINDING_NAME_SUFFIX));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_SOAP));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                                                   WSDL2Constants.SOAP_VERSION_1_1));
        generateDefaultSOAPBindingOperations(axisService, fac, binding, wsdl, tns, wsoap);
        return binding;
    }

    /**
     * Generates a default SOAP 12 Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param wsoap - The WSDL 2.0 SOAP namespace
     * @param tns - The target namespace
     * @return - The generated SOAP12Binding element
     */
    public static OMElement generateSOAP12Binding(OMFactory fac, AxisService axisService,
                                                  OMNamespace wsdl, OMNamespace wsoap, OMNamespace tns) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, axisService.getName() +
                        Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_SOAP));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_VERSION, wsoap,
                                                   WSDL2Constants.SOAP_VERSION_1_2));
        generateDefaultSOAPBindingOperations(axisService, fac, binding, wsdl, tns, wsoap);
        return binding;
    }

    /**
     * Generates a default HTTP Binding for a given AxisService
     * @param fac - The OMFactory
     * @param axisService - The AxisService
     * @param whttp - The WSDL 2.0 HTTP namespace
     * @param tns - The target namespace
     * @return - The generated HTTPBinding element
     */
    public static OMElement generateHTTPBinding(OMFactory fac, AxisService axisService,
                                                OMNamespace wsdl, OMNamespace whttp, OMNamespace tns) {
        OMElement binding = fac.createOMElement(WSDL2Constants.BINDING_LOCAL_NAME, wsdl);
        String serviceName = axisService.getName();
        binding.addAttribute(
                fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null, serviceName +
                        Java2WSDLConstants.HTTP_BINDING));
        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME, null, tns
                .getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));

        binding.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_TYPE, null,
                                                   WSDL2Constants.URI_WSDL2_HTTP));
        Iterator iterator = axisService.getChildren();
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            OMElement opElement = fac.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(opElement);
            String name = axisOperation.getName().getLocalPart();
            opElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF, null,
                                                         tns.getPrefix() + ":" + name));
            opElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_LOCATION, whttp,
                                                         serviceName + "/" + name));
        }
        return binding;
    }

private static void generateDefaultSOAPBindingOperations(AxisService axisService, OMFactory omFactory, OMElement binding, OMNamespace wsdl, OMNamespace tns, OMNamespace wsoap) {        Iterator iterator = axisService.getChildren();
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            OMElement opElement = omFactory.createOMElement(WSDL2Constants.OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(opElement);
            String name = axisOperation.getName().getLocalPart();
            opElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_REF, null,
                                                         tns.getPrefix() + ":" + name));
            opElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ACTION, wsoap,
                                                         axisOperation.getInputAction()));
        }
    }

    /**
     * Generates a default service element
     * @param omFactory - The OMFactory
     * @param tns - The targetnamespace
     * @param axisService - The AxisService
     * @return - The generated service element
     * @throws AxisFault - Thrown in case an exception occurs
     */
    public static OMElement generateServiceElement(OMFactory omFactory, OMNamespace wsdl, OMNamespace tns,
                                                   AxisService axisService, boolean disableREST)
            throws AxisFault {
        String[] eprs = axisService.getEPRs();
        if (eprs == null) {
            eprs = new String[]{axisService.getName()};
        }
        OMElement serviceElement = null;
        serviceElement = omFactory.createOMElement(WSDL2Constants.SERVICE_LOCAL_NAME, wsdl);
                    serviceElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME,
                                                                            null, axisService.getName()));
                    serviceElement.addAttribute(omFactory.createOMAttribute(
                            WSDL2Constants.INTERFACE_LOCAL_NAME, null,
                            tns.getPrefix() + ":" + WSDL2Constants.DEFAULT_INTERFACE_NAME));
        for (int i = 0; i < eprs.length; i++) {
            String name = "";
            String epr = eprs[i];
            if (epr.startsWith("https://")) {
                name = WSDL2Constants.DEFAULT_HTTPS_PREFIX;
            }
            OMElement soap11EndpointElement =
                    omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
            soap11EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_NAME, null,
                    name + WSDL2Constants.DEFAULT_SOAP11_ENDPOINT_NAME));
            soap11EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.BINDING_LOCAL_NAME, null,
                    tns.getPrefix() + ":" + axisService.getName() +
                            Java2WSDLConstants.BINDING_NAME_SUFFIX));
            soap11EndpointElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
            serviceElement.addChild(soap11EndpointElement);
            OMElement soap12EndpointElement =
                    omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
            soap12EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.ATTRIBUTE_NAME, null,
                    name + WSDL2Constants.DEFAULT_SOAP12_ENDPOINT_NAME));
            soap12EndpointElement.addAttribute(omFactory.createOMAttribute(
                    WSDL2Constants.BINDING_LOCAL_NAME, null,
                    tns.getPrefix() + ":" + axisService.getName() +
                            Java2WSDLConstants.SOAP12BINDING_NAME_SUFFIX));
            soap12EndpointElement.addAttribute(
                    omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
            serviceElement.addChild(soap12EndpointElement);
            OMElement httpEndpointElement = null;
            if (!disableREST) {
                httpEndpointElement =
                        omFactory.createOMElement(WSDL2Constants.ENDPOINT_LOCAL_NAME, wsdl);
                httpEndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.ATTRIBUTE_NAME, null,
                        name + WSDL2Constants.DEFAULT_HTTP_ENDPOINT_NAME));
                httpEndpointElement.addAttribute(omFactory.createOMAttribute(
                        WSDL2Constants.BINDING_LOCAL_NAME, null,
                        tns.getPrefix() + ":" + axisService.getName() + Java2WSDLConstants
                                .HTTP_BINDING));
                httpEndpointElement.addAttribute(
                        omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_ADDRESS, null, epr));
                serviceElement.addChild(httpEndpointElement);
            }
            if (epr.startsWith("https://")) {
                OMElement soap11Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                soap11Documentation.setText("This endpoint exposes a SOAP 11 binding over a HTTPS");
                soap11EndpointElement.addChild(soap11Documentation);
                OMElement soap12Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                soap12Documentation.setText("This endpoint exposes a SOAP 12 binding over a HTTPS");
                soap12EndpointElement.addChild(soap12Documentation);
                if (!disableREST) {
                    OMElement httpDocumentation =
                            omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    httpDocumentation.setText("This endpoint exposes a HTTP binding over a HTTPS");
                    httpEndpointElement.addChild(httpDocumentation);
                }
            } else if (epr.startsWith("http://")) {
                OMElement soap11Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                soap11Documentation.setText("This endpoint exposes a SOAP 11 binding over a HTTP");
                soap11EndpointElement.addChild(soap11Documentation);
                OMElement soap12Documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                soap12Documentation.setText("This endpoint exposes a SOAP 12 binding over a HTTP");
                soap12EndpointElement.addChild(soap12Documentation);
                if (!disableREST) {
                    OMElement httpDocumentation =
                            omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
                    httpDocumentation.setText("This endpoint exposes a HTTP binding over a HTTP");
                    httpEndpointElement.addChild(httpDocumentation);
                }
            }
        }
        return serviceElement;
    }

    /**
     * Adds the namespaces to the given OMElement
     *
     * @param descriptionElement - The OMElement that the namespaces should be added to
     * @param nameSpaceMap - The namespaceMap
     */
    public static void populateNamespaces(OMElement descriptionElement, Map nameSpaceMap) {
        if (nameSpaceMap != null) {
        Iterator keys = nameSpaceMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(key)) {
                descriptionElement.declareDefaultNamespace((String) nameSpaceMap.get(key));
            } else {
                descriptionElement.declareNamespace((String) nameSpaceMap.get(key), key);
            }
            }
        }
    }

    public static void addWSAWActionAttribute(OMElement element, String action) {
        if (action == null || action.length() == 0) {
            return;
        }
        OMNamespace namespace = element.declareNamespace(
                AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
        element.addAttribute("Action", action, namespace);
    }

    public static void addExtensionElement(OMFactory fac, OMElement element,
                                     String name, String att1Name, String att1Value,
                                     OMNamespace soapNameSpace) {
        OMElement extElement = fac.createOMElement(name, soapNameSpace);
        element.addChild(extElement);
        extElement.addAttribute(att1Name, att1Value, null);
    }

    public static void addWSAddressingToBinding(String addressingFlag, OMFactory omFactory, OMElement bindingElement) {
        // Add WS-Addressing UsingAddressing element if appropriate
        // SHOULD be on the binding element per the specification
        if (addressingFlag.equals(
                AddressingConstants.ADDRESSING_OPTIONAL)) {
            OMNamespace wsawNamespace = omFactory.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(omFactory, bindingElement,
                                AddressingConstants.USING_ADDRESSING,
                                "required", "true",
                                wsawNamespace);
        } else if (addressingFlag.equals(
                AddressingConstants.ADDRESSING_REQUIRED)) {
            OMNamespace wsawNamespace = omFactory.createOMNamespace(
                    AddressingConstants.Final.WSAW_NAMESPACE, "wsaw");
            WSDLSerializationUtil.addExtensionElement(omFactory, bindingElement,
                                AddressingConstants.USING_ADDRESSING,
                                "required", "true",
                                wsawNamespace);
        }
    }

    public static void addWSDLDocumentationElement(AxisDescription axisDescription, OMElement omElement, OMFactory omFactory, OMNamespace wsdl) {
        String documentationString = axisDescription.getDocumentation();
        OMElement documentation;
        if (documentationString != null && !"".equals(documentationString)) {
            documentation = omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, wsdl);
            OMText omText;
            if (documentationString.indexOf(CDATA_START) > -1) {
                documentationString = documentationString.replaceFirst(CDATA_START_REGEX, "");
                documentationString = documentationString.replaceFirst(CDATA_END_REGEX, "");
                omText = omFactory.createOMText(documentationString, XMLStreamConstants.CDATA);
            } else {
            omText =  omFactory.createOMText(documentationString);
            }
            documentation.addChild(omText);
            omElement.addChild(documentation);
        }
    }
}
