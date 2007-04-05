package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMNode;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.util.WSDLSerializationUtil;
import org.apache.axis2.AxisFault;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class AxisService2WSDL2 implements WSDL2Constants {

    private AxisService axisService;

    private String[] url;

    public AxisService2WSDL2(AxisService service, String[] serviceURL) {
        this.axisService = service;
        url = serviceURL;
    }

    /**
     * Generates a WSDL 2.0 document for this web service
     * @return The WSDL2 document element
     * @throws org.apache.axis2.AxisFault - Thrown in case an exception occurs
     */
    public OMElement toWSDL20() throws AxisFault {

        Map nameSpacesMap = axisService.getNameSpacesMap();
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement descriptionElement = omFactory.createOMElement(WSDL2Constants.DESCRIPTION, null);

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
        if (!nameSpacesMap.containsValue(WSDL2Constants.WSDL_NAMESPACE)) {
            descriptionElement.declareDefaultNamespace(WSDL2Constants.WSDL_NAMESPACE);
        }
        if (nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_SOAP)) {
            wsoap = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_SOAP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_SOAP, nameSpacesMap));
        } else {
            wsoap = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_SOAP, WSDL2Constants.SOAP_PREFIX);
        }
        if (nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_HTTP)) {
            whttp = omFactory
                    .createOMNamespace(WSDL2Constants.URI_WSDL2_HTTP,
                                       WSDLSerializationUtil.getPrefix(
                                               WSDL2Constants.URI_WSDL2_HTTP, nameSpacesMap));
        } else {
            whttp = descriptionElement
                    .declareNamespace(WSDL2Constants.URI_WSDL2_HTTP, WSDL2Constants.HTTP_PREFIX);
        }
        if (nameSpacesMap.containsValue(WSDL2Constants.URI_WSDL2_EXTENSIONS)) {
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
                omFactory.createOMElement(WSDL2Constants.DOCUMENTATION, null);
        if ((description = axisService.getServiceDescription()) != null) {
            documentationElement.setText(description);
        }
        descriptionElement.addChild(documentationElement);

        // Add types element
        OMElement typesElement = omFactory.createOMElement(WSDL2Constants.TYPES_LOCAL_NALE, null);
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
                        throw new AxisFault(e);
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
        descriptionElement.addChild(getInterfaceEmelent(tns, wsdlx, omFactory, interfaceName));

        // Check whether the axisService has any endpoints. If they exists serialize them else
        // generate default endpoint elements.
        Map endpointMap = axisService.getEndpoints();
        if (endpointMap != null && endpointMap.size() > 0) {
            String[] eprs = axisService.getEPRs();
            if (eprs == null) {
                eprs = new String[]{axisService.getName()};
            }
            OMElement serviceElement = getServiceElement(tns, omFactory, interfaceName);
            Iterator iterator = endpointMap.values().iterator();
            while (iterator.hasNext()) {
                AxisEndpoint axisEndpoint = (AxisEndpoint) iterator.next();
                serviceElement.addChild(axisEndpoint.toWSDL20(tns, whttp, eprs[0]));

                descriptionElement.addChild(axisEndpoint.getBinding().toWSDL20(tns, wsoap, whttp,
                                                                               interfaceName,
                                                                               axisService.getNameSpacesMap()));
            }

            descriptionElement.addChild(serviceElement);
        } else {

            // There are no andpoints defined hence generate default bindings and endpoints
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateSOAP11Binding(omFactory, axisService, wsoap,
                                                                tns));
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateSOAP12Binding(omFactory, axisService, wsoap,
                                                                tns));
            descriptionElement.addChild(
                    WSDLSerializationUtil.generateHTTPBinding(omFactory, axisService, whttp, tns));
            descriptionElement
                    .addChild(WSDLSerializationUtil.generateServiceElement(omFactory, tns,
                                                                           axisService));
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
    private OMElement getInterfaceEmelent(OMNamespace tns, OMNamespace wsdlx,
                                          OMFactory fac, String interfaceName) {

        OMElement interfaceElement = fac.createOMElement(WSDL2Constants.INTERFACE_LOCAL_NAME, null);
        interfaceElement.addAttribute(fac.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                                            interfaceName));
        Iterator iterator = axisService.getOperations();
        ArrayList interfaceOperations = new ArrayList();
        ArrayList interfaceFaults = new ArrayList();
        int i = 0;
        while (iterator.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) iterator.next();
            interfaceOperations.add(i, axisOperation.toWSDL20(tns, wsdlx));
            i++;
            Iterator faultsIterator = axisOperation.getFaultMessages().iterator();
            while (faultsIterator.hasNext()) {
                AxisMessage faultMessage = (AxisMessage) faultsIterator.next();
                String name = faultMessage.getName();
                if (!interfaceFaults.contains(name)) {
                    OMElement faultElement =
                            fac.createOMElement(WSDL2Constants.FAULT_LOCAL_NAME, null);
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
    private OMElement getServiceElement(OMNamespace tns, OMFactory omFactory,
                                        String interfaceName) {
        OMElement serviceElement =
                omFactory.createOMElement(WSDL2Constants.SERVICE_LOCAL_NAME, null);
        serviceElement.addAttribute(
                omFactory.createOMAttribute(WSDL2Constants.ATTRIBUTE_NAME, null,
                                            axisService.getName()));
        serviceElement.addAttribute(omFactory.createOMAttribute(WSDL2Constants.INTERFACE_LOCAL_NAME,
                                                                null, tns.getPrefix() + ":" +
                interfaceName));
        return serviceElement;
    }
}
