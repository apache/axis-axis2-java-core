package org.apache.ws.java2wsdl;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.codehaus.jam.JMethod;

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
 * @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class Java2OMBuilder implements Java2WSDLConstants {

    private TypeTable typeTable = null;

    private static int prefixCount = 1;

    private static final String NAMESPACE_PREFIX = "ns";

    private JMethod method [];

    private Collection schemaCollection;

    private String serviceName;

    private String targetNamespace;

    private String targetNamespacePrefix;

    private OMNamespace ns1;

    private OMNamespace soap;

    private OMNamespace soap12;

    private OMNamespace tns;

    private OMNamespace wsdl;

    private OMNamespace mime;

    private OMNamespace http;

    private String style;

    private String use;

    private String locationURL;

    public Java2OMBuilder(JMethod[] method, Collection schemaCollection,
                          TypeTable typeTab, String serviceName, String targetNamespace,
                          String targetNamespacePrefix, String style, String use,
                          String locationURL) {
        this.method = method;
        this.schemaCollection = schemaCollection;
        this.typeTable = typeTab;
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

        if (locationURL == null) {
            this.locationURL = DEFAULT_LOCATION_URL;
        } else {
            this.locationURL = locationURL;
        }
        this.serviceName = serviceName;

        if (targetNamespace != null && !targetNamespace.trim().equals("")) {
            this.targetNamespace = targetNamespace;
        } else {
            this.targetNamespace = DEFAULT_TARGET_NAMESPACE;
        }

        if (targetNamespacePrefix != null
                && !targetNamespacePrefix.trim().equals("")) {
            this.targetNamespacePrefix = targetNamespacePrefix;
        } else {
            this.targetNamespacePrefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
        }
    }

    public OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE,
                DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);

        ele.addAttribute("targetNamespace", targetNamespace, null);
        generateNamespaces(fac, ele);
        generateTypes(fac, ele);
        generateMessages(fac, ele);
        generatePortType(fac, ele);
        generateBinding(fac, ele);
        generateService(fac, ele);
        return ele;
    }

    private void generateNamespaces(OMFactory fac, OMElement defintions) {
        soap = defintions.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
        tns = defintions.declareNamespace(targetNamespace,
                targetNamespacePrefix);
        soap12 = defintions.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);
        http = defintions.declareNamespace(HTTP_NAMESPACE, HTTP_PREFIX);
        mime = defintions.declareNamespace(MIME_NAMESPACE, MIME_PREFIX);
    }

    private void generateTypes(OMFactory fac, OMElement defintions)
            throws Exception {
        OMElement wsdlTypes = fac.createOMElement("types", wsdl);
        StringWriter writer = new StringWriter();

        // wrap the Schema elements with this start and end tags to create a
        // document root
        // under which the schemas can fall into
        writer.write("<xmlSchemas>");
        writeSchemas(writer);
        writer.write("</xmlSchemas>");
        XMLStreamReader xmlReader = XMLInputFactory.newInstance()
                .createXMLStreamReader(
                        new ByteArrayInputStream(writer.toString().getBytes()));

        StAXOMBuilder staxOMBuilders = new StAXOMBuilder(fac, xmlReader);
        Iterator iterator = staxOMBuilders.getDocumentElement()
                .getChildElements();
        while (iterator.hasNext()) {
            wsdlTypes.addChild((OMNode) iterator.next());
        }
        defintions.addChild(wsdlTypes);
    }

    private void writeSchemas(StringWriter writer) {
        Iterator iterator = schemaCollection.iterator();
        Iterator typeIterator = null;
        Iterator elementIterator = null;
        XmlSchema xmlSchema = null;

        while (iterator.hasNext()) {
            xmlSchema = (XmlSchema) iterator.next();
            typeIterator = xmlSchema.getSchemaTypes().getValues();
            while (typeIterator.hasNext()) {
                xmlSchema.getItems().add((XmlSchemaObject) typeIterator.next());
            }

            xmlSchema.write(writer);
        }
    }

    private void generateMessages(OMFactory fac, OMElement definitions) {
        Hashtable namespaceMap = new Hashtable();
        String namespacePrefix = null;
        String namespaceURI = null;
        QName messagePartType = null;
        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];

            // only if a type for the message part has already been defined
            if ((messagePartType = typeTable.getComplexSchemaType(jmethod
                    .getSimpleName())) != null) {
                namespaceURI = messagePartType.getNamespaceURI();
                // avoid duplicate namespaces
                if ((namespacePrefix = (String) namespaceMap.get(namespaceURI)) == null) {
                    namespacePrefix = generatePrefix();
                    namespaceMap.put(namespaceURI, namespacePrefix);
                }

            //Request Message
                OMElement requestMessge = fac.createOMElement(
                        MESSAGE_LOCAL_NAME, wsdl);
                requestMessge.addAttribute(ATTRIBUTE_NAME, jmethod
                        .getSimpleName()
                        + MESSAGE_SUFFIX, null);
                definitions.addChild(requestMessge);
                OMElement requestPart = fac.createOMElement(
                        PART_ATTRIBUTE_NAME, wsdl);
            requestMessge.addChild(requestPart);
            requestPart.addAttribute(ATTRIBUTE_NAME, "part1", null);

            requestPart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                        namespacePrefix + COLON_SEPARATOR
                                + jmethod.getSimpleName(), null);
            }

            // only if a type for the message part has already been defined
            if ((messagePartType = typeTable.getComplexSchemaType(jmethod
                    .getSimpleName()
                    + RESPONSE)) != null) {
                namespaceURI = messagePartType.getNamespaceURI();
                if ((namespacePrefix = (String) namespaceMap.get(namespaceURI)) == null) {
                    namespacePrefix = generatePrefix();
                    namespaceMap.put(namespaceURI, namespacePrefix);
                }
            //Response Message
                OMElement responseMessge = fac.createOMElement(
                        MESSAGE_LOCAL_NAME, wsdl);
                responseMessge.addAttribute(ATTRIBUTE_NAME, jmethod
                        .getSimpleName()
                        + RESPONSE_MESSAGE, null);
                definitions.addChild(responseMessge);
                OMElement responsePart = fac.createOMElement(
                        PART_ATTRIBUTE_NAME, wsdl);
            responseMessge.addChild(responsePart);
            responsePart.addAttribute(ATTRIBUTE_NAME, "part1", null);

            responsePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                        namespacePrefix + COLON_SEPARATOR
                                + jmethod.getSimpleName() + RESPONSE, null);
            }
        }

        // now add these unique namespaces to the the definitions element
        Enumeration enumeration = namespaceMap.keys();
        while (enumeration.hasMoreElements()) {
            namespaceURI = (String) enumeration.nextElement();
            definitions.declareNamespace(namespaceURI, (String) namespaceMap
                    .get(namespaceURI));
        }
    }

    /**
     * Generate the porttypes
     */
    private void generatePortType(OMFactory fac, OMElement defintions) {
        JMethod jmethod = null;
        OMElement operation = null;
        OMElement message = null;
        OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);
        defintions.addChild(portType);
        portType.addAttribute(ATTRIBUTE_NAME, serviceName + PORT_TYPE_SUFFIX,
                null);
        //adding message refs
        for (int i = 0; i < method.length; i++) {
            jmethod = method[i];
            operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            portType.addChild(operation);
            operation.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName(),
                    null);

            message = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
            message.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                    + COLON_SEPARATOR + jmethod.getSimpleName()
                    + MESSAGE_SUFFIX, null);
            operation.addChild(message);

            if (!jmethod.getReturnType().isVoidType()) {
                message = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                message.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix()
                        + COLON_SEPARATOR + jmethod.getSimpleName()
                        + RESPONSE_MESSAGE, null);
                operation.addChild(message);
            }
        }

    }

    /**
     * Generate the service
     */
    public void generateService(OMFactory fac, OMElement defintions) {
        OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
        defintions.addChild(service);
        service.addAttribute(ATTRIBUTE_NAME, serviceName, null);
        OMElement port = fac.createOMElement(PORT, wsdl);
        service.addChild(port);
        port.addAttribute(ATTRIBUTE_NAME, serviceName + SOAP11PORT, null);
        port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + COLON_SEPARATOR
                + serviceName + BINDING_NAME_SUFFIX, null);
        addExtensionElement(fac, port, soap, SOAP_ADDRESS, LOCATION, locationURL
                + serviceName);

        port = fac.createOMElement(PORT, wsdl);
        service.addChild(port);
        port.addAttribute(ATTRIBUTE_NAME, serviceName + SOAP12PORT, null);
        port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + COLON_SEPARATOR
                + serviceName + SOAP12BINDING_NAME_SUFFIX, null);
        addExtensionElement(fac, port, soap12, SOAP_ADDRESS, LOCATION, locationURL
                + serviceName);
    }

    /**
     * Generate the bindings
     */
    private void generateBinding(OMFactory fac, OMElement defintions) {
        generateSoap11Binding(fac, defintions);
        generateSoap12Binding(fac, defintions);
    }

    private void generateSoap11Binding(OMFactory fac, OMElement defintions) {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, serviceName + BINDING_NAME_SUFFIX,
                null);
        binding.addAttribute("type", tns.getPrefix() + COLON_SEPARATOR
                + serviceName + PORT_TYPE_SUFFIX, null);

        addExtensionElement(fac, binding, soap, BINDING_LOCAL_NAME, TRANSPORT,
                TRANSPORT_URI, STYLE, style);

        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                    wsdl);
            binding.addChild(operation);

            addExtensionElement(fac, operation, soap, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, URN_PREFIX + COLON_SEPARATOR
                    + jmethod.getSimpleName(), STYLE, style);
            operation.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName(),
                    null);

            OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
            addExtensionElement(fac, input, soap, SOAP_BODY, SOAP_USE, use,
                    "namespace", targetNamespace);
            operation.addChild(input);

            if (!jmethod.getReturnType().isVoidType()) {
                OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                addExtensionElement(fac, output, soap, SOAP_BODY, SOAP_USE, use,
                        "namespace", targetNamespace);
                operation.addChild(output);
            }
        }
    }

    private void generateSoap12Binding(OMFactory fac, OMElement defintions) {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, serviceName + SOAP12BINDING_NAME_SUFFIX,
                null);
        binding.addAttribute("type", tns.getPrefix() + COLON_SEPARATOR
                + serviceName + PORT_TYPE_SUFFIX, null);

        addExtensionElement(fac, binding, soap12, BINDING_LOCAL_NAME, TRANSPORT,
                TRANSPORT_URI, STYLE, style);

        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                    wsdl);
            binding.addChild(operation);
            operation.declareNamespace(URI_WSDL12_SOAP, SOAP12_PREFIX);

            addExtensionElement(fac, operation, soap12, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, URN_PREFIX + COLON_SEPARATOR
                    + jmethod.getSimpleName(), STYLE, style);
            operation.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName(),
                    null);

            OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
            addExtensionElement(fac, input, soap12, SOAP_BODY, SOAP_USE, use,
                    "namespace", targetNamespace);
            operation.addChild(input);

            if (!jmethod.getReturnType().isVoidType()) {
            OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
                addExtensionElement(fac, output, soap12, SOAP_BODY, SOAP_USE, use,
                        "namespace", targetNamespace);
            operation.addChild(output);
        }
    }
    }

    private void addExtensionElement(OMFactory fac, OMElement element, String name, OMNamespace namespace,
                                     Hashtable attrs) {
        OMElement soapbinding = fac.createOMElement(name, namespace);
        element.addChild(soapbinding);
        Enumeration enumeration = attrs.keys();
        String attrName = null;
        while (enumeration.hasMoreElements()) {
            attrName = (String) enumeration.nextElement();
            soapbinding.addAttribute(attrName, (String) attrs.get(attrName), null);
        }
    }

    private void addExtensionElement(OMFactory fac, OMElement element, OMNamespace namespace,
                                     String name, String att1Name, String att1Value, String att2Name,
                                     String att2Value) {
        OMElement soapbinding = fac.createOMElement(name, namespace);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
        soapbinding.addAttribute(att2Name, att2Value, null);
    }

    private void addExtensionElement(OMFactory fac, OMElement element, OMNamespace namespace,
                                     String name, String att1Name, String att1Value) {
        OMElement soapbinding = fac.createOMElement(name, namespace);
        element.addChild(soapbinding);
        soapbinding.addAttribute(att1Name, att1Value, null);
    }

    private String generatePrefix() {
        return NAMESPACE_PREFIX + prefixCount++;
    }

}
