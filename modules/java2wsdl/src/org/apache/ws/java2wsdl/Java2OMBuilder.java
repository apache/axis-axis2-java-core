package org.apache.ws.java2wsdl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.codehaus.jam.JMethod;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
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

    private JMethod method [];
    private XmlSchema schema;
    private String serviceName;
    private String targetNamespace;
    private OMNamespace ns1;
    private OMNamespace soap;
    private OMNamespace tns;
    private OMNamespace wsdl;

    private String style;
    private String use;
    private String locationURL;

    public Java2OMBuilder(JMethod[] method,
                          XmlSchema schema,
                          String serviceName,
                          String targetNamespace,
                          String style,
                          String use,
                          String locationURL) {
        this.method = method;
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
    }

    public OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        wsdl = fac.createOMNamespace(WSDL_NAMESPACE,
                DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement ele = fac.createOMElement("definitions", wsdl);
        ns1 = ele.declareNamespace(AXIS2_XSD, "ns1");
        ele.declareNamespace(URI_2001_SCHEMA_XSD, DEFAULT_SCHEMA_NAMESPACE_PREFIX);
        soap = ele.declareNamespace(URI_WSDL11_SOAP, SOAP11_PREFIX);
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
        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            //Request Message
            OMElement requestMessge = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
            requestMessge.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName()
                    + REQUEST_MESSAGE, null);
            defintions.addChild(requestMessge);
            OMElement requestPart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
            requestMessge.addChild(requestPart);
            requestPart.addAttribute(ATTRIBUTE_NAME, "part1", null);
            requestPart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                    ns1.getPrefix() + ":" + jmethod.getSimpleName()
                           , null);
            //Response Message
            OMElement responseMessge = fac.createOMElement(MESSAGE_LOCAL_NAME, wsdl);
            responseMessge.addAttribute(ATTRIBUTE_NAME,
                    jmethod.getSimpleName() + RESPONSE_MESSAGE, null);
            defintions.addChild(responseMessge);
            OMElement responsePart = fac.createOMElement(PART_ATTRIBUTE_NAME, wsdl);
            responseMessge.addChild(responsePart);
            responsePart.addAttribute(ATTRIBUTE_NAME, "part1", null);
            responsePart.addAttribute(ELEMENT_ATTRIBUTE_NAME,
                    ns1.getPrefix() + ":" + jmethod.getSimpleName() + RESPONSE, null);
        }
    }

    /**
     * Generate the porttypes
     */
    private void generatePortType(OMFactory fac,
                                  OMElement defintions) {
        OMElement portType = fac.createOMElement(PORT_TYPE_LOCAL_NAME, wsdl);
        defintions.addChild(portType);
        portType.addAttribute(ATTRIBUTE_NAME, serviceName + PORT_TYPE_SUFFIX, null);
        //adding message refs
        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            portType.addChild(operation);
            operation.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName(), null);

            OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME, wsdl);
            input.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                    + jmethod.getSimpleName() + REQUEST_MESSAGE, null);
            operation.addChild(input);

            OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME, wsdl);
            output.addAttribute(MESSAGE_LOCAL_NAME, tns.getPrefix() + ":"
                    + jmethod.getSimpleName() + RESPONSE_MESSAGE, null);
            operation.addChild(output);
        }

    }

    /**
     * Generate the service
     */
    public void generateService(OMFactory fac,
                                OMElement defintions) {
        OMElement service = fac.createOMElement(SERVICE_LOCAL_NAME, wsdl);
        defintions.addChild(service);
        service.addAttribute(ATTRIBUTE_NAME, serviceName, null);
        OMElement port = fac.createOMElement(PORT, wsdl);
        service.addChild(port);
        port.addAttribute(ATTRIBUTE_NAME, serviceName + PORT, null);
        port.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":" +
                serviceName + BINDING_NAME_SUFFIX, null);
        addExtensionElemnet(fac, port, SOAP_ADDRESS, LOCATION,
                locationURL + serviceName);
    }


    /**
     * Generate the bindings
     */
    private void generateBinding(OMFactory fac,
                                 OMElement defintions) {
        OMElement binding = fac.createOMElement(BINDING_LOCAL_NAME, wsdl);
        defintions.addChild(binding);
        binding.addAttribute(ATTRIBUTE_NAME, serviceName + BINDING_NAME_SUFFIX, null);
        binding.addAttribute("type", tns.getPrefix() + ":" + serviceName + PORT_TYPE_SUFFIX, null);

        addExtensionElemnet(fac, binding, BINDING_LOCAL_NAME,
                TRANSPORT, TRANSPORT_URI,
                STYLE, style);

        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, wsdl);
            binding.addChild(operation);
            addExtensionElemnet(fac, operation, OPERATION_LOCAL_NAME,
                    SOAP_ACTION, jmethod.getSimpleName(),
                    STYLE, style);
            operation.addAttribute(ATTRIBUTE_NAME, jmethod.getSimpleName(), null);

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
