package org.apache.axis2.description;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.commons.schema.XmlSchema;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
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

public class AxisService2WSDL2 implements WSDL2Constants {

    private AxisService axisService;

    private String[] url;
    private OMNamespace wsoap;
    private OMNamespace tns;


    public AxisService2WSDL2(AxisService service, String[] serviceURL) {
        this.axisService = service;
        url = serviceURL;
    }

    public OMElement generateOM() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        fac.createOMNamespace(WSDL_NAMESPACE,
                              DEFAULT_WSDL_NAMESPACE_PREFIX);
        OMElement description = fac.createOMElement(DESCRIPTION, null);
        Map nameSpaceMap = axisService.getNameSpacesMap();
        Iterator keys = nameSpaceMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if ("".equals(key)) {
                description.declareDefaultNamespace((String) nameSpaceMap.get(key));
            } else {
                description.declareNamespace((String) nameSpaceMap.get(key), key);
            }
        }
        wsoap = description.declareNamespace(URI_WSDL2_SOAP, SOAP_PREFIX);
        description.declareNamespace(URI_WSDL2_SOAP_ENV, SOAP_ENV_PREFIX);
        String prefix = getPrefix(axisService.getTargetNamespace());
        if (prefix == null || "".equals(prefix)) {
            prefix = DEFAULT_TARGET_NAMESPACE_PREFIX;
        }
        axisService.getNameSpacesMap().put(prefix,
                                           axisService.getTargetNamespace());
        tns = description.declareNamespace(axisService.getTargetNamespace(), prefix);

        description.addAttribute("targetNamespace", axisService.getTargetNamespace(),
                                 null);
        //adding service document
        if (axisService.getServiceDescription() != null) {
            addDocumentation(description, fac, axisService.getServiceDescription());
        }
        OMElement wsdlTypes = fac.createOMElement("types", null);
        description.addChild(wsdlTypes);
        // populate the schema mappings
        axisService.populateSchemaMappings();
        ArrayList schemas = axisService.getSchema();
        for (int i = 0; i < schemas.size(); i++) {
            StringWriter writer = new StringWriter();
            XmlSchema schema = axisService.getSchema(i);

            if (!Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
                schema.write(writer);
                String schemaString = writer.toString();

                if (!"".equals(schemaString)) {
                    wsdlTypes.addChild(
                            XMLUtils.toOM(new ByteArrayInputStream(schemaString.getBytes())));
                }
            }
        }
        //generating interface
        generateInterface(description, fac);

        //generating soap binding
        generateSOAPBinding(description, fac);

        //generating service element
        generateServiceElement(description, fac);

        return description;
    }

    /**
     * To add documenttaion tag to any given element
     *
     * @param element
     * @param factory
     * @param docmentString
     */
    private void addDocumentation(OMElement element,
                                  OMFactory factory,
                                  String docmentString) {
        OMElement documentation = factory.createOMElement(DOCUMENTATION, wsoap);
        documentation.setText(docmentString);
        element.addChild(documentation);

    }

    private void generateInterface(OMElement description, OMFactory fac) {
        OMElement interfaceElement = fac.createOMElement(INTERFACE_LOCAL_NAME, null);
        interfaceElement.addAttribute("name", axisService.getName() + INTERFACE_PREFIX, null);
        generateInterfaceFaultElement(interfaceElement, fac);
        generateInterfaceOperations(interfaceElement, fac);
        description.addChild(interfaceElement);
    }

    private void generateInterfaceOperations(OMElement interfaceElement, OMFactory fac) {
        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (!axisOperation.isControlOperation()) {

                String operationName = axisOperation.getName().getLocalPart();
                OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME,
                                                          null);
                interfaceElement.addChild(operation);
                operation.addAttribute(ATTRIBUTE_NAME, operationName, null);
                String MEP = axisOperation.getMessageExchangePattern();
                operation.addAttribute(ATTRIBUTE_NAME_PATTERN, getUpdatedMEP(MEP), null);
                //TODO need to add : style="http://www.w3.org/2006/01/wsdl/style/iri"
                //TODO need to add : swsdlx:safe = "true"
                if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(MEP)) {
                    AxisMessage inaxisMessage = axisOperation
                            .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                    if (inaxisMessage != null) {
                        OMElement input = fac.createOMElement(IN_PUT_LOCAL_NAME,
                                                              null);
                        input.addAttribute(MESSAGE_LABEL, WSDLConstants.MESSAGE_LABEL_IN_VALUE,
                                           null);
                        operation.addChild(input);
                        addMessageElementAtt(input, inaxisMessage);
                    }
                }

                if (WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_OUT_OPTIONAL_IN.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OPTIONAL_OUT.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_OUT_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_ROBUST_IN_ONLY.equals(MEP)
                        || WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT.equals(MEP)) {
                    AxisMessage outAxisMessage = axisOperation
                            .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    if (outAxisMessage != null) {
                        OMElement output = fac.createOMElement(OUT_PUT_LOCAL_NAME,
                                                               null);
                        operation.addChild(output);
                        output.addAttribute(MESSAGE_LABEL, WSDLConstants.MESSAGE_LABEL_OUT_VALUE,
                                            null);
                        addMessageElementAtt(output, outAxisMessage);
                    }
                }
                addInterfaceOperationFault(operation, fac, axisOperation);
            }
        }
    }

    private void addMessageElementAtt(OMElement messageElement, AxisMessage message) {
        QName elementQName = message.getElementQName();
        String attValue = elementQName.getPrefix() + ":" + elementQName.getLocalPart();
        messageElement.addAttribute(ATTRIBUTE_ELEMENT, attValue, null);
    }

    private void addInterfaceOperationFault(OMElement operationElement, OMFactory fac,
                                            AxisOperation operation) {

        ArrayList faultMessages = operation.getFaultMessages();
        for (Iterator iterator = faultMessages.iterator(); iterator.hasNext();) {
            AxisMessage faultMessage = (AxisMessage) iterator.next();
            if (faultMessage != null) {
                QName elementQName = faultMessage.getElementQName();

                String direction = faultMessage.getDirection();
                OMElement faultElement = null;
                if (MESSAGE_LABEL_OUT.equalsIgnoreCase(direction)) {
                    faultElement = fac.createOMElement(OUT_FAULT, null, operationElement);
                } else if (MESSAGE_LABEL_IN.equalsIgnoreCase(direction)) {
                    faultElement = fac.createOMElement(IN_FAULT, null, operationElement);
                } else {
                    return;
                }

                faultElement.addAttribute(MESSAGE_LABEL, direction, null);
                faultElement.addAttribute(ATTRIBUTE_REF, elementQName.getPrefix() + ":" +
                        elementQName.getLocalPart(), null);

            }
        }


    }

    private void generateInterfaceFaultElement(OMElement interfaceElement, OMFactory fac) {
//          axisService.get
    }

    private void generateSOAPBinding(OMElement description, OMFactory fac) {
        OMElement bindingElement = fac.createOMElement(BINDING_LOCAL_NAME, null);
        description.addChild(bindingElement);
        bindingElement.addAttribute("name", axisService.getName() + SOAP_BINDING_PREFIX, null);
        bindingElement
                .addAttribute(INTERFACE_LOCAL_NAME, tns.getPrefix() + ":" + axisService.getName() +
                        INTERFACE_PREFIX, null);
        bindingElement.addAttribute("type", URI_WSDL2_SOAP, null);
        bindingElement.addAttribute("protocol", HTTP_PROTOCAL, wsoap);

        addBindingFaultElement(bindingElement, fac);
        generateBindingOperations(bindingElement, fac);

    }

    private void addBindingFaultElement(OMElement bindingElement, OMFactory fac) {
        //TODO : need to implement this
    }

    private void generateBindingOperations(OMElement bindingElement, OMFactory fac) {
        Iterator operations = axisService.getOperations();
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();
            if (axisOperation.isControlOperation()) {
                continue;
            }
            OMElement operation = fac.createOMElement(OPERATION_LOCAL_NAME, null);
            bindingElement.addChild(operation);
            String MEP = axisOperation.getMessageExchangePattern();
            operation.addAttribute("ref", tns.getPrefix() + ":" +
                    axisOperation.getName().getLocalPart(), null);
            //TODO : I am not sure whether I am doing the right thing here , need to read the spec
            operation.addAttribute("mep", getMep(axisOperation.getMessageExchangePattern()), wsoap);
        }
    }

    private void generateServiceElement(OMElement description, OMFactory fac) throws Exception {
        OMElement serviceElement = fac.createOMElement(SERVICE_LOCAL_NAME, null);
        serviceElement.addAttribute(ATTRIBUTE_NAME, axisService.getName() + "Service", null);
        serviceElement.addAttribute(INTERFACE_LOCAL_NAME, tns.getPrefix()
                + ":" + axisService.getName() + INTERFACE_PREFIX, null);

        description.addChild(serviceElement);
        generateEndpoints(serviceElement, fac);
    }

    private void generateEndpoints(OMElement serviceElement, OMFactory fac) throws Exception {
        int count;
        for (int i = 0; i < url.length; i++) {
            String s = url[i];
            OMElement endpoint = fac.createOMElement("endpoint", null);
            URL url = new URL(s);
            endpoint.addAttribute(ATTRIBUTE_NAME, axisService.getName()
                    + "Endpoint" + "_" + url.getProtocol(), null);
            //TODO : We have to have mechnishm to support multiple bindings
            endpoint.addAttribute(BINDING_LOCAL_NAME, tns.getPrefix() + ":" +
                    axisService.getName() + SOAP_BINDING_PREFIX, null);
            endpoint.addAttribute("address", s, null);
            serviceElement.addChild(endpoint);
        }
    }

    /**
     * To get the soap mep for given wsdl2.0 mep
     */
    private String getMep(String mep) {
        //TODO : need to fix this , this wrong
        return "http://www.w3.org/2003/05/soap/mep/soap-response";
    }


    /**
     * This method is to convert http://www.w3.org/2004/08/wsdl/in-out to
     * http://www.w3.org/2006/01/wsdl/in-out
     *
     * @param oldmepuri
     */
    public String getUpdatedMEP(String oldmepuri) {
        //TODO : Need to improve this , what I am doing is wrong
        return oldmepuri.replaceAll("2004/08", "2006/01");
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
