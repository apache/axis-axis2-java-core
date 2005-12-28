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
 */

package org.apache.axis2.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.XMLUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.impl.WSDLProcessingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * AxisServiceBuilder builds an AxisService using a WSDL document which is feed
 * as a javax.wsdl.Definition or as an InputStream. If there are multiple
 * javax.wsdl.Service elements in the WSDL, the first is picked.
 */
public class AxisServiceBuilder {

    private static final String XMLSCHEMA_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema";

    private static final String XMLSCHEMA_NAMESPACE_PREFIX = "xs";

    private static final String XML_SCHEMA_SEQUENCE_LOCAL_NAME = "sequence";

    private static final String XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME = "complexType";

    private static final String XML_SCHEMA_ELEMENT_LOCAL_NAME = "element";

    private static final String XML_SCHEMA_IMPORT_LOCAL_NAME = "import";

    private static final String XSD_NAME = "name";

    private static final String XSD_TYPE = "type";

    private static final String XSD_REF = "ref";

    private int nsCount = 1;

    public AxisService getAxisService(InputStream wsdlInputStream)
            throws WSDLException {
        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        reader.setFeature("javax.wsdl.importDocuments", true);

        Document doc;

        try {
            doc = XMLUtils.newDocument(wsdlInputStream);

        } catch (ParserConfigurationException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser Configuration Error", e);

        } catch (SAXException e) {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Parser SAX Error", e);

        } catch (IOException e) {
            throw new WSDLException(WSDLException.INVALID_WSDL, "IO Error", e);
        }

        Definition wsdlDefinition = reader.readWSDL(null, doc);
        return getAxisService(wsdlDefinition);
    }

    public AxisService getAxisService(Definition definition)
            throws WSDLProcessingException {

        AxisService axisService = new AxisService();
        Map services = definition.getServices();

        if (services.isEmpty()) {
            throw new WSDLProcessingException("no Service element is found");
        }

        Iterator serviceIterator = services.values().iterator();
        Service service = (Service) serviceIterator.next();

        // setting the name
        axisService.setName(service.getQName().getLocalPart());

        // setting the schema
        Types types = definition.getTypes();
        Iterator extElements = types.getExtensibilityElements().iterator();

        ExtensibilityElement extElement;

        while (extElements.hasNext()) {
            extElement = (ExtensibilityElement) extElements.next();

            if (extElement instanceof Schema) {
                Element schemaElement = ((Schema) extElement).getElement();
                axisService.setSchema(getXMLSchema(schemaElement));
            }
        }

        // getting the port of the service with SOAP binding
        Port port = getSOAPBindingPort(service);

        if (port == null) {
            throw new WSDLProcessingException("no SOAPBinding Port found");
        }

        Binding binding = port.getBinding();
        Iterator bindingOperations = binding.getBindingOperations().iterator();

        while (bindingOperations.hasNext()) {
            BindingOperation bindingOperation = (BindingOperation) bindingOperations
                    .next();
            Operation operation = bindingOperation.getOperation();

            try {
                AxisOperation axisOperation = AxisOperationFactory
                        .getAxisOperation(getMessageExchangePattern(bindingOperation));

                // setting parent
                axisOperation.setParent(axisService);
                // setting operation name
                axisOperation.setName(new QName(operation.getName()));
                
                // Input
                Input input = operation.getInput();
                AxisMessage inAxisMsg = axisOperation
                        .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                Message inMsg = input.getMessage();

                if (wrapperble(inMsg)) {
                    Element wrapper = getWrappedElement(definition, inMsg);
                    inAxisMsg.setElementQName(new QName(wrapper
                            .getNamespaceURI(), wrapper.getLocalName(), wrapper
                            .getPrefix()));

                    // TODO
                    // axisService.addSchema(getXmlSchema(wrapper));

                } else {
                    Iterator parts = inMsg.getParts().values().iterator();
                    Part part = (Part) parts.next();
                    QName qname = part.getElementName();
                    inAxisMsg.setElementQName(qname);
                    
                }

                // Output
                Output output = operation.getOutput();

                if (output != null) {

                    AxisMessage outAxisMsg = axisOperation
                            .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    Message outMsg = output.getMessage();

                    if (wrapperble(outMsg)) {
                        Element wrapper = getWrappedElement(definition, outMsg);
                        outAxisMsg.setElementQName(new QName(wrapper
                                .getNamespaceURI(), wrapper.getLocalName(),
                                wrapper.getPrefix()));
                        // TODO
                        // axisService.addSchema(getXmlSchema(wrapper));

                    } else {
                        Iterator parts = outMsg.getParts().values().iterator();
                        Part part = (Part) parts.next();
                        QName qname = part.getElementName();
                        outAxisMsg.setElementQName(qname);
                    }
                }

                axisService.addOperation(axisOperation);

            } catch (AxisFault axisFault) {

                throw new WSDLProcessingException(axisFault.getMessage());
            }
        }
        getXmlSchemaPrefix(definition);

        return axisService;
    }

    private Port getSOAPBindingPort(Service service) {
        Iterator ports = service.getPorts().values().iterator();
        Port port;
        Binding binding;
        Iterator extElements;
        
        while (ports.hasNext()) {
            port = (Port) ports.next();
            binding = port.getBinding();
            extElements = binding.getExtensibilityElements().iterator();

            while (extElements.hasNext()) {

                if (extElements.next() instanceof SOAPBinding) {
                    return port;
                }
            }
        }
        return null;
    }

    private int getMessageExchangePattern(BindingOperation bindingOperation) {

        if (bindingOperation.getBindingOutput() == null) {
            return WSDLConstants.MEP_CONSTANT_IN_ONLY;

        } else {
            return WSDLConstants.MEP_CONSTANT_IN_OUT;
        }
    }

    private XmlSchema getXMLSchema(Element element) {
        return (new XmlSchemaCollection()).read(element);
    }

    private String getXmlSchemaPrefix(Definition definition) {
        Map namespaces = definition.getNamespaces();

        if (namespaces.containsValue(XMLSCHEMA_NAMESPACE_URI)) {

            Iterator prefixes = namespaces.keySet().iterator();
            String prefix;

            while (prefixes.hasNext()) {
                prefix = (String) prefixes.next();

                if (XMLSCHEMA_NAMESPACE_URI.equals(namespaces.get(prefix))) {
                    return prefix;
                }
            }
        }
        return XMLSCHEMA_NAMESPACE_PREFIX; //default prefix
    }

    private boolean wrapperble(Message message) {
        Map parts = message.getParts();

        if (parts.size() > 1) {
            return true;
        }

        Iterator iterator = parts.values().iterator();
        Part part;

        while (iterator.hasNext()) {
            part = (Part) iterator.next();
            if (part.getTypeName() != null) {
                return true;
            }
        }
        return false;
    }

    private Document getDOMDocument() {
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware(true);
            return fac.newDocumentBuilder().newDocument();

        } catch (ParserConfigurationException ex) {
            throw new WSDLProcessingException(ex.getMessage());
        }
    }

    private Element getWrappedElement(Definition definition, Message message) {
        Document document = getDOMDocument();
        String xsdPrefix = getXmlSchemaPrefix(definition);
        Map namespaceImports = new HashMap();
        Map namespacePrefixes = new HashMap();
        Map parts = message.getParts();

        String name = message.getQName().getLocalPart();
        Element nComplexType = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                        + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);
        nComplexType.setAttribute(XSD_NAME, name);

        Element contextSequence = document.createElementNS(
                XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                        + XML_SCHEMA_SEQUENCE_LOCAL_NAME);

        Element childElement;
        Iterator iterator = parts.values().iterator();

        while (iterator.hasNext()) {
            Part part = (Part) iterator.next();
            String elementName = part.getName();
            boolean isTyped = true;

            QName schemaTypeName;

            if (part.getTypeName() != null) {
                schemaTypeName = part.getTypeName();

            } else if (part.getElementName() != null) {
                schemaTypeName = part.getElementName();

            } else {
                throw new WSDLProcessingException("unqualified message part");
            }

            childElement = document.createElementNS(XMLSCHEMA_NAMESPACE_URI,
                    xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
            String prefix;

            if (!XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName
                    .getNamespaceURI())) {
                String namespace = schemaTypeName.getNamespaceURI();

                if (namespaceImports.containsKey(namespace)) {
                    Element namespaceImport = document.createElementNS(
                            XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
                                    + XML_SCHEMA_IMPORT_LOCAL_NAME);
                    namespaceImport.setAttribute("namespace", namespace);
                    namespaceImports.put(namespace, namespaceImport);
                    prefix = getTempPrefix();
                    namespacePrefixes.put(namespace, prefix);

                } else {
                    prefix = (String) namespacePrefixes.get(namespace);
                }

            } else {
                prefix = xsdPrefix;
            }

            if (isTyped) {
                childElement.setAttribute(XSD_NAME, elementName);
                childElement.setAttribute(XSD_TYPE, prefix + ":"
                        + schemaTypeName.getLocalPart());

            } else {
                childElement.setAttribute(XSD_REF, prefix + ":"
                        + schemaTypeName.getLocalPart());
            }

            contextSequence.appendChild(childElement);
        }

        nComplexType.appendChild(contextSequence);
        return nComplexType;

    }

    private String getTempPrefix() {
        return "ns" + nsCount++;
    }
}
