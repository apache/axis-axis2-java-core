package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.wsdl20.extensions.ExtensionElement;
import org.apache.woden.wsdl20.extensions.UnknownExtensionElement;
import org.apache.woden.wsdl20.xml.BindingElement;
import org.apache.woden.wsdl20.xml.DescriptionElement;
import org.apache.woden.wsdl20.xml.ImportElement;
import org.apache.woden.wsdl20.xml.InterfaceElement;
import org.apache.woden.wsdl20.xml.TypesElement;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.DOMPolicyReader;
import org.apache.ws.policy.util.PolicyFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
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
 */

public class WSDL20ToAxisServiceBuilder extends WSDLToAxisServiceBuilder {

    private DescriptionElement descriptionElement;
    private String wsdlURI;


    public WSDL20ToAxisServiceBuilder(String wsdlUri, QName serviceName) {
        super(null, serviceName);
        this.wsdlURI = wsdlUri;
    }

    public WSDL20ToAxisServiceBuilder(DescriptionElement descriptionElement, QName serviceName) {
        super(null, serviceName);
        this.descriptionElement = descriptionElement;
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri, AxisService service) {
        super(null, service);
        this.wsdlURI = wsdlUri;
    }


    public AxisService populateService() throws AxisFault {
        try {
            if (descriptionElement == null) {
                descriptionElement = readInTheWSDLFile(wsdlURI);
            }
            //Setting wsdl4jdefintion to axisService , so if some one want
            // to play with it he can do that by getting the parameter
            Parameter wsdldefintionParamter = new Parameter();
            wsdldefintionParamter.setName(WSDLConstants.WSDL_20_DESCRIPTION);
            wsdldefintionParamter.setValue(descriptionElement);
            axisService.addParameter(wsdldefintionParamter);

            if (descriptionElement == null) {
                return null;
            }
            //setting target name space
            axisService.setTargetNamespace(descriptionElement.getTargetNamespace().getRawPath());

            //adding ns in the original WSDL
//            processPoliciesInDefintion(wsdl4jDefinition);     TODO : Differing policy support

            //scheam generation
            processImports(descriptionElement);
            axisService.setNameSpacesMap(descriptionElement.getNamespaces());
            TypesElement typesElement = descriptionElement.getTypesElement();
            if (null != typesElement) {
                this.copyExtensibleElements(typesElement.getExtensionElements(), descriptionElement,
                        axisService, TYPES);
            }
//            Binding binding = findBinding(wsdl4jDefinition);
//            //////////////////(1.2) /////////////////////////////
//            // create new Schema extensions element for wrapping
//            Element[] schemaElements = generateWrapperSchema(wsdl4jDefinition,
//                    binding);
//            if (schemaElements != null && schemaElements.length > 0) {
//                for (int i = 0; i < schemaElements.length; i++) {
//                    Element schemaElement = schemaElements[i];
//                    if (schemaElement != null) {
//                        axisService.addSchema(getXMLSchema(schemaElement, null));
//                    }
//                }
//            }
//            processBinding(binding, wsdl4jDefinition);
            return axisService;
        } catch (WSDLException e) {
            throw new AxisFault(e);
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

//    private Binding findBinding(DescriptionElement descriptionElement) throws AxisFault {
//        ServiceElement[] serviceElements = descriptionElement.getServiceElements();
//        Service service;
//        Binding binding = null;
//        Port port = null;
//        if (serviceName != null) {
//            service = (Service) services.get(serviceName);
//            if (service == null) {
//                throw new AxisFault("Service not found the WSDL "
//                        + serviceName.getLocalPart());
//            }
//        } else {
//            if (services.size() > 0) {
//                service = (Service) services.values().toArray()[0];
//            } else {
//                throw new AxisFault("No service element found in the WSDL");
//            }
//        }
//        copyExtensibleElements(service.getExtensibilityElements(), dif,
//                axisService, SERVICE);
//        if (portName != null) {
//            port = service.getPort(portName);
//            if (port == null) {
//                throw new AxisFault("No port found for the given name :"
//                        + portName);
//            }
//        } else {
//            Map ports = service.getPorts();
//            if (ports != null && ports.size() > 0) {
//                port = (Port) ports.values().toArray()[0];
//            }
//        }
//        axisService.setName(service.getQName().getLocalPart());
//        if (port != null) {
//            copyExtensibleElements(port.getExtensibilityElements(), dif,
//                    axisService, PORT);
//            binding = port.getBinding();
//        }
//        return binding;
//    }

    private void copyExtensibleElements(ExtensionElement[] extensionElement,
                                        DescriptionElement descriptionElement, AxisDescription description,
                                        String originOfExtensibilityElements) {
        for (int i = 0; i < extensionElement.length; i++) {
            ExtensionElement element = extensionElement[i];


            if (element instanceof UnknownExtensionElement) {
                UnknownExtensionElement unknown = (UnknownExtensionElement) element;

                //look for the SOAP 1.2 stuff here. WSDL4j does not understand
                // SOAP 1.2 things
                // TODO this is wrong. Compare this with WSDL 2.0 QName
                if (WSDLConstants.SOAP_12_OPERATION.equals(unknown.getExtensionType())) {
                    Element unknownElement = unknown.getElement();
                    if (description instanceof AxisOperation) {
                        AxisOperation axisOperation = (AxisOperation) description;
                        String style = unknownElement.getAttribute("style");
                        if (style != null) {
                            axisOperation.setStyle(style);
                        }
                        axisOperation.setSoapAction(unknownElement.getAttribute("soapAction"));
                    }
                } else if (WSDLConstants.SOAP_12_HEADER.equals(unknown.getExtensionType())) {
                    //TODO : implement thid
                } else if (WSDLConstants.SOAP_12_BINDING.equals(unknown
                        .getExtensionType())) {
                    style = unknown.getElement().getAttribute("style");
                    axisService.setSoapNsUri(element.getExtensionType()
                            .getNamespaceURI());
                } else if (WSDLConstants.SOAP_12_ADDRESS.equals(unknown
                        .getExtensionType())) {
                    axisService.setEndpoint(unknown.getElement().getAttribute(
                            "location"));
                } else if (WSDLConstants.POLICY
                        .equals(unknown.getExtensionType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    Policy policy = policyReader.readPolicy(unknown
                            .getElement());

//                    addPolicy(description, originOfExtensibilityElements,
//                            policy);

                } else if (WSDLConstants.POLICY_REFERENCE.equals(unknown
                        .getExtensionType())) {

                    DOMPolicyReader policyReader = (DOMPolicyReader) PolicyFactory
                            .getPolicyReader(PolicyFactory.DOM_POLICY_READER);
                    PolicyReference policyRef = policyReader
                            .readPolicyReference(unknown.getElement());
//                    addPolicyRef(description, originOfExtensibilityElements,
//                            policyRef);

                } else {
                    //TODO : we are ignored that.
                }

//            } else if (element instanceof SOAPAddress) {
//                SOAPAddress soapAddress = (SOAPAddress) wsdl4jElement;
//                axisService.setEndpoint(soapAddress.getLocationURI());
//            } else if (wsdl4jElement instanceof Schema) {
//                Schema schema = (Schema) wsdl4jElement;
//                //just add this schema - no need to worry about the imported ones
//                axisService.addSchema(getXMLSchema(schema.getElement(),
//                        wsdl4jDefinition.getDocumentBaseURI()));
//            } else if (SOAPConstants.Q_ELEM_SOAP_OPERATION.equals(wsdl4jElement
//                    .getElementType())) {
//                SOAPOperation soapOperation = (SOAPOperation) wsdl4jElement;
//                if (description instanceof AxisOperation) {
//                    AxisOperation axisOperation = (AxisOperation) description;
//                    if (soapOperation.getStyle() != null) {
//                        axisOperation.setStyle(soapOperation.getStyle());
//                    }
//                    axisOperation.setSoapAction(soapOperation
//                            .getSoapActionURI());
//                }
//            } else if (SOAPConstants.Q_ELEM_SOAP_HEADER.equals(wsdl4jElement
//                    .getElementType())) {
//                SOAPHeader soapHeader = (SOAPHeader) wsdl4jElement;
//                SOAPHeaderMessage headerMessage = new SOAPHeaderMessage();
//                headerMessage.setNamespaceURI(soapHeader.getNamespaceURI());
//                headerMessage.setUse(soapHeader.getUse());
//                Boolean required = soapHeader.getRequired();
//                if (null != required) {
//                    headerMessage.setRequired(required.booleanValue());
//                }
//                if (null != wsdl4jDefinition) {
//                    //find the relevant schema part from the messages
//                    Message msg = wsdl4jDefinition.getMessage(soapHeader
//                            .getMessage());
//                    Part msgPart = msg.getPart(soapHeader.getPart());
//                    headerMessage.setElement(msgPart.getElementName());
//                }
//                headerMessage.setMessage(soapHeader.getMessage());
//
//                headerMessage.setPart(soapHeader.getPart());
//                if (description instanceof AxisMessage) {
//                    ((AxisMessage) description).addSopaHeader(headerMessage);
//                }
//            } else if (SOAPConstants.Q_ELEM_SOAP_BINDING.equals(wsdl4jElement
//                    .getElementType())) {
//                SOAPBinding soapBinding = (SOAPBinding) wsdl4jElement;
//                style = soapBinding.getStyle();
//                axisService.setSoapNsUri(soapBinding.getElementType()
//                        .getNamespaceURI());
//            }
            }
        }
    }


    private void processImports(DescriptionElement descriptionElement) {
        ImportElement[] wsdlImports = descriptionElement.getImportElements();

        for (int i = 0; i < wsdlImports.length; i++) {
            ImportElement importElement = wsdlImports[i];
            DescriptionElement importedDescriptionElement = importElement.getDescriptionElement();
            if (importedDescriptionElement != null) {
                processImports(importedDescriptionElement);
                //copy ns

                Map namespaces = importedDescriptionElement.getNamespaces();
                Iterator keys = namespaces.keySet().iterator();
                while (keys.hasNext()) {
                    Object key = keys.next();
                    if (! descriptionElement.getNamespaces().containsValue(namespaces.get(key))) {
                        descriptionElement.getNamespaces().put(key, namespaces.get(key));
                    }
                }

                descriptionElement.getNamespaces().putAll(namespaces);
                //copy types
                TypesElement t = importedDescriptionElement.getTypesElement();
                ExtensionElement[] typesList = t.getExtensionElements();

                TypesElement types = descriptionElement.getTypesElement();
                if (types == null) {
                    descriptionElement.setTypesElement(types);
                } else {
                    for (int j = 0; j < typesList.length; j++) {
                        ExtensionElement extensionElement = typesList[j];
                        types.addExtensionElement(extensionElement);
                    }
                }

                //add interfaces
                InterfaceElement[] interfaceElements = importedDescriptionElement.getInterfaceElements();
                for (int j = 0; j < interfaceElements.length; j++) {
                    InterfaceElement interfaceElement = interfaceElements[j];
                    descriptionElement.addInterfaceElement(interfaceElement);
                }

                //add bindings
                BindingElement[] bindingElements = importedDescriptionElement.getBindingElements();
                for (int j = 0; j < bindingElements.length; j++) {
                    BindingElement bindingElement = bindingElements[j];
                    descriptionElement.addBindingElement(bindingElement);
                }

            }
        }
    }


    private DescriptionElement readInTheWSDLFile(String wsdlURI) throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // TODO : I can not find a constant for this feature in WSDLReader
//        reader.setFeature("javax.wsdl.importDocuments", false);
        reader.setFeature(WSDLReader.FEATURE_VERBOSE, false);
        return reader.readWSDL(wsdlURI);
    }


}
