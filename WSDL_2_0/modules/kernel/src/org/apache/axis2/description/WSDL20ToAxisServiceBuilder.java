package org.apache.axis2.description;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.woden.*;
import org.apache.woden.internal.DOMWSDLFactory;
import org.apache.woden.internal.wsdl20.extensions.soap.SOAPBindingExtensionsImpl;
import org.apache.woden.internal.wsdl20.extensions.soap.SOAPHeaderBlockImpl;
import org.apache.woden.internal.wsdl20.extensions.http.HTTPBindingExtensionsImpl;
import org.apache.woden.schema.Schema;
import org.apache.woden.wsdl20.*;
import org.apache.woden.wsdl20.enumeration.Direction;
import org.apache.woden.wsdl20.enumeration.MessageLabel;
import org.apache.woden.wsdl20.extensions.ExtensionElement;
import org.apache.woden.wsdl20.extensions.UnknownExtensionElement;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingFaultExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingOperationExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingMessageReferenceExtensions;
import org.apache.woden.wsdl20.extensions.soap.*;
import org.apache.woden.wsdl20.xml.*;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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

    protected Description description;

    private String wsdlURI;

    // FIXME @author Chathura THis shoud be a URI. Find whats used by
    // woden.
    private static String RPC = "rpc";

    protected String interfaceName;

    private String savedTargetNamespace;

    private Map namespacemap;

    private NamespaceMap stringBasedNamespaceMap;

    private boolean setupComplete = false;
    private Service wsdlService;

//    As bindings are processed add it to this array so that we dont process the same binding twice
    private Map processedBindings;


    public WSDL20ToAxisServiceBuilder(InputStream in, QName serviceName,
                                      String interfaceName) {
        this.in = in;
        this.serviceName = serviceName;
        this.interfaceName = interfaceName;
        this.axisService = new AxisService();
        setPolicyRegistryFromService(axisService);
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri,
                                      String name, String interfaceName) throws Exception {
        WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
        DescriptionElement descriptionElement = wsdlReader.readWSDL(wsdlUri);
        savedTargetNamespace = descriptionElement.getTargetNamespace()
                .toString();
        namespacemap = descriptionElement.getNamespaces();
        this.description = descriptionElement.toComponent();
        this.serviceName = null;
        if (name != null) {
            serviceName = new QName(descriptionElement.getTargetNamespace().toString(), name);
        }
        this.interfaceName = interfaceName;
        this.axisService = new AxisService();
        setPolicyRegistryFromService(axisService);
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri, QName serviceName) {
        super(null, serviceName);
        this.wsdlURI = wsdlUri;
    }

    public WSDL20ToAxisServiceBuilder(String wsdlUri, AxisService service) {
        super(null, service);
        this.wsdlURI = wsdlUri;
    }

    public AxisService populateService() throws AxisFault {

        try {
            setup();
            // Setting wsdl4jdefintion to axisService , so if some one want
            // to play with it he can do that by getting the parameter
            Parameter wsdlDescriptionParamter = new Parameter();
            wsdlDescriptionParamter.setName(WSDLConstants.WSDL_20_DESCRIPTION);
            wsdlDescriptionParamter.setValue(description);
            axisService.addParameter(wsdlDescriptionParamter);

            if (description == null) {
                return null;
            }
            // setting target name space
            axisService.setTargetNamespace(savedTargetNamespace);

            // if there are documentation elements in the root. Lets add them as the wsdlService description
            // but since there can be multiple documentation elements, lets only add the first one
//            DocumentationElement[] documentationElements = description.toElement().getDocumentationElements();
//            if (documentationElements != null && documentationElements.length > 0) {
//                axisService.setServiceDescription(documentationElements[0].getContent().toString());
//            }

            // adding ns in the original WSDL
            // processPoliciesInDefintion(wsdl4jDefinition); TODO : Defering policy handling for now - Chinthaka
            // policy support

            // schema generation

            // Create the namespacemap

            axisService.setNameSpacesMap(stringBasedNamespaceMap);
            // TypeDefinition[] typeDefinitions =
            // description.getTypeDefinitions();
            // for(int i=0; i<typeDefinitions.length; i++){
            // if("org.apache.ws.commons.schema".equals(typeDefinitions[i].getContentModel())){
            // axisService.addSchema((XmlSchema)typeDefinitions[i].getContent());
            // }else
            // if("org.w3c.dom".equals(typeDefinitions[i].getContentModel())){
            // axisService.addSchema(getXMLSchema((Element)typeDefinitions[i].getContent(),
            // null));
            // }
            //
            // }

            TypesElement typesElement = description.toElement()
                    .getTypesElement();
            if (typesElement != null) {
                Schema[] schemas = typesElement.getSchemas();
                for (int i = 0; i < schemas.length; i++) {
                    XmlSchema schemaDefinition = schemas[i].getSchemaDefinition();

                    // WSDL 2.0 spec requires that even the built-in schema should be returned
                    // once asked for schema definitions. But for data binding purposes we can ignore that
                    if (schemaDefinition != null && !Constants.URI_2001_SCHEMA_XSD
                            .equals(schemaDefinition.getTargetNamespace())) {
                        axisService.addSchema(schemaDefinition);
                    }
                }
            }

            processService();

//            Binding binding = findBinding();

//            processBinding(binding);

            return axisService;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private void processEndpoints() throws AxisFault {
        Endpoint [] endpoints = wsdlService.getEndpoints();

        if (endpoints.length == 0) {
            throw new AxisFault("No endpoints found in the WSDL");
        }

        processedBindings = new HashMap();

        Endpoint endpoint = null;

        if (this.interfaceName != null) {


            for (int i = 0; i < endpoints.length; ++i) {
                if (this.interfaceName.equals(endpoints[i].getName().toString())) {
                    endpoint = endpoints[i];
                    break;  // found it.  Stop looking
                }
            }
            if (endpoint == null) {
                throw new AxisFault("No endpoint found for the given name :"
                        + this.interfaceName);
            }

            axisService
                    .addEndpoint(endpoint.getName().toString(), processEndpoint(endpoint));
        } else {


            for (int i = 0; i < endpoints.length; i++) {
                axisService
                        .addEndpoint(endpoints[i].getName().toString(), processEndpoint(endpoints[i]));
            }
        }

        if (endpoints.length != 0) {

            endpoint = endpoints[0];

            axisService.setEndpointName(endpoint.getName().toString());
            axisService.setBindingName(endpoint.getBinding().getName().getLocalPart());
            axisService.setEndpointURL(endpoint.getAddress().toString());
        }


    }

    private void processService() throws AxisFault {

        Service[] services = description.getServices();
        if (services.length == 0) {
            throw new AxisFault("No wsdlService found in the WSDL");
        }

        if (serviceName != null) {
            for (int i = 0; i < services.length; i++) {
                if (serviceName.equals(services[i].getName())) {
                    wsdlService = services[i];
                    break;  // found it. Stop looking.
                }
            }
            if (wsdlService == null) {
                throw new AxisFault("Service with the specified name not found in the WSDL : "
                        + serviceName.getLocalPart());
            }
        }

        wsdlService = services[0];

        axisService.setName(wsdlService.getName().getLocalPart().toString());

        processInterface(wsdlService.getInterface());

        processEndpoints();

    }

    private AxisEndpoint processEndpoint(Endpoint endpoint) throws AxisFault {

        AxisEndpoint axisEndpoint = new AxisEndpoint();
        axisEndpoint.setName(endpoint.getName().toString());
        if (processedBindings.containsKey(endpoint.getBinding().getName())) {
            axisEndpoint.setBinding(
                    (AxisBinding) processedBindings.get(endpoint.getBinding().getName()));
        } else {
            axisEndpoint.setBinding(processBinding(endpoint.getBinding()));
        }

        SOAPEndpointExtensions soapEndpointExtensions = null;
        try {
            soapEndpointExtensions = (SOAPEndpointExtensions) endpoint.getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Extention not found");
        }

//        axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_TYPE,soapEndpointExtensions.getHttpAuthenticationScheme());
//        axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_REALM,soapEndpointExtensions.getHttpAuthenticationRealm());

        return axisEndpoint;

    }

    /**
     * contains all code which gathers non-wsdlService specific information from the
     * wsdl.
     * <p/>
     * After all the setup completes successfully, the setupComplete field is
     * set so that any subsequent calls to setup() will result in a no-op. Note
     * that subclass WSDL20ToAllAxisServicesBuilder will call populateService
     * for each endpoint in the WSDL. Separating the non-wsdlService specific
     * information here allows WSDL20ToAllAxisServicesBuilder to only do this
     * work 1 time per WSDL, instead of for each endpoint on each wsdlService.
     *
     * @throws AxisFault
     */
    protected void setup() throws AxisFault {
        if (setupComplete) { // already setup, just do nothing and return
            return;
        }
        try {
            if (description == null) {

                DescriptionElement descriptionElement = null;
                if (wsdlURI != null && !"".equals(wsdlURI)) {
                    descriptionElement = readInTheWSDLFile(wsdlURI);
                } else if (in != null) {

                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                            .newInstance();
                    documentBuilderFactory.setNamespaceAware(true);
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document document = documentBuilder.parse(in);

                    WSDLReader reader = DOMWSDLFactory.newInstance().newWSDLReader();
                    WSDLSource wsdlSource = reader.createWSDLSource();
                    wsdlSource.setSource(document.getDocumentElement());
                    wsdlSource.setBaseURI(new URI(getBaseUri()));
                    descriptionElement = reader.readWSDL(wsdlSource);
                } else {
                    throw new AxisFault("No resources found to read the wsdl");
                }

                savedTargetNamespace = descriptionElement.getTargetNamespace().toString();
                namespacemap = descriptionElement.getNamespaces();
                this.description = descriptionElement.toComponent();

            }
            // Create the namespacemap

            stringBasedNamespaceMap = new NamespaceMap();
            Iterator iterator = namespacemap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                stringBasedNamespaceMap.put(key, (namespacemap.get(key)).toString());
            }

            setupComplete = true;
        } catch (AxisFault e) {
            throw e; // just rethrow AxisFaults
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private AxisBinding processBinding(Binding binding)
            throws AxisFault {


        Interface serviceInterface;

        AxisBinding axisBinding = new AxisBinding();

        axisBinding.setType(binding.getType().toString());

        axisBinding.setName(binding.getName());

        String bindingType = binding.getType().toString();

        if (bindingType.equals(WSDL2Constants.URI_WSDL2_SOAP)) {
            processSOAPBindingExtention(binding, axisBinding);
        } else if (bindingType.equals(WSDL2Constants.URI_WSDL2_HTTP)) {
            processHTTPBindingExtention(binding, axisBinding);
        }

        // We should process the interface based on the service not on a binding

//            if (wsdlService != null) {
//                            serviceInterface = wsdlService.getInterface();
//                        } else {
//                            // we don't need this as wsdlService can not be null. But keeping for early stages
//                            serviceInterface = binding.getInterface();
//                        }
//
//
//
//            processInterface(serviceInterface);


        processedBindings.put(binding.getName(), axisBinding);
        return axisBinding;
    }

    private void processSOAPBindingExtention(Binding binding, AxisBinding axisBinding)
            throws AxisFault {

        // Capture all the binding specific properties

        SOAPBindingExtensionsImpl soapBindingExtensions = null;
        try {
            soapBindingExtensions = (SOAPBindingExtensionsImpl) binding
                    .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("Soap Binding Extention not found");
        }

        String soapVersion;

        if ((soapVersion = soapBindingExtensions.getSoapVersion()) != null)

            if (soapVersion.equals(WSDL2Constants.SOAP_VERSION_1_1)) {

                // Might have to remove this as its a binding specific property
                axisService.setSoapNsUri(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                        WSDL2Constants.SOAP_VERSION_1_1);
            } else {
                // Might have to remove this as its a binding specific property
                axisService.setSoapNsUri(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
                axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                        WSDL2Constants.SOAP_VERSION_1_2);
            }

        URI soapUnderlyingProtocol = soapBindingExtensions.getSoapUnderlyingProtocol();
        if (soapUnderlyingProtocol != null) {
            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_PROTOCOL,
                    soapUnderlyingProtocol.toString());
        }
        URI soapMepDefault = soapBindingExtensions.getSoapMepDefault();
        if (soapMepDefault != null) {
            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_MEP,
                    soapMepDefault.toString());
        }
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING,
                soapBindingExtensions.getHttpTransferCodingDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                soapBindingExtensions.getSoapModules());

        // Capture all the fault specific properties

        BindingFault[] bindingFaults = binding.getBindingFaults();
        for (int i = 0; i < bindingFaults.length; i++) {
            BindingFault bindingFault = bindingFaults[i];
            InterfaceFault interfaceFault = bindingFault.getInterfaceFault();

            AxisBindingMessage axisBindingFault = new AxisBindingMessage();
            axisBindingFault.setName(interfaceFault.getName().getLocalPart());
            axisBindingFault.setParent(axisBinding);

            SOAPBindingFaultExtensions soapBindingFaultExtensions = null;

            try {
                soapBindingFaultExtensions = (SOAPBindingFaultExtensions) bindingFault
                        .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_SOAP));
            } catch (URISyntaxException e) {
                throw new AxisFault("Soap Binding Extention not found");
            }

            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                    soapBindingFaultExtensions.getHttpHeaders());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING,
                    soapBindingFaultExtensions.getHttpTransferCoding());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_CODE,
                    soapBindingFaultExtensions.getSoapFaultCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_SUBCODES,
                    soapBindingFaultExtensions.getSoapFaultSubcodes());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                    createSoapHeaders(soapBindingFaultExtensions.getSoapHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                    soapBindingFaultExtensions.getSoapModules());

            axisBinding.addFault(axisBindingFault);

        }

        // Capture all the binding operation specific properties

        BindingOperation[] bindingOperations = binding.getBindingOperations();
        for (int i = 0; i < bindingOperations.length; i++) {
            BindingOperation bindingOperation = bindingOperations[i];

            AxisBindingOperation axisBindingOperation = new AxisBindingOperation();
            AxisOperation axisOperation =
                    axisService.getOperation(bindingOperation.getInterfaceOperation().getName());

            axisBindingOperation.setAxisOperation(axisOperation);
            axisBindingOperation.setParent(axisBinding);
            axisBindingOperation.setName(axisOperation.getName());

            SOAPBindingOperationExtensions soapBindingOperationExtensions = null;
            try {
                soapBindingOperationExtensions = ((SOAPBindingOperationExtensions)
                        bindingOperation.getComponentExtensionsForNamespace(
                                new URI(WSDL2Constants.URI_WSDL2_SOAP)));
            } catch (URISyntaxException e) {
                throw new AxisFault("Soap Binding Extention not found");
            }

            URI soapAction = soapBindingOperationExtensions.getSoapAction();
            if (soapAction != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_ACTION,
                        soapAction.toString());
            }
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                    soapBindingOperationExtensions.getSoapModules());
            URI soapMep = soapBindingOperationExtensions.getSoapMep();
            if (soapMep != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_MEP,
                        soapMep.toString());
            }
            URI httpLocation = soapBindingOperationExtensions.getHttpLocation();
            if (httpLocation != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION,
                        httpLocation.toString());
            }
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING,
                    soapBindingOperationExtensions.getHttpTransferCodingDefault());


            BindingMessageReference[] bindingMessageReferences =
                    bindingOperation.getBindingMessageReferences();
            for (int j = 0; j < bindingMessageReferences.length; j++) {
                BindingMessageReference bindingMessageReference = bindingMessageReferences[j];

                AxisBindingMessage axisBindingMessage = new AxisBindingMessage();
                axisBindingMessage.setParent(axisBindingOperation);

                AxisMessage axisMessage = axisOperation.getMessage(bindingMessageReference
                        .getInterfaceMessageReference().getMessageLabel().toString());

                axisBindingMessage.setAxisMessage(axisMessage);
                axisBindingMessage.setName(axisMessage.getName());
                axisBindingMessage.setDirection(axisMessage.getDirection());


                SOAPBindingMessageReferenceExtensions soapBindingMessageReferenceExtensions = null;
                try {
                    soapBindingMessageReferenceExtensions =
                            (SOAPBindingMessageReferenceExtensions) bindingMessageReference
                                    .getComponentExtensionsForNamespace(
                                            new URI(WSDL2Constants.URI_WSDL2_SOAP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("Soap Binding Extention not found");
                }

                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                        soapBindingMessageReferenceExtensions.getHttpHeaders());
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING,
                        soapBindingMessageReferenceExtensions.getHttpTransferCoding());
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                        createSoapHeaders(soapBindingMessageReferenceExtensions.getSoapHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                        soapBindingMessageReferenceExtensions.getSoapModules());

//                    SOAPHeaderBlock[] soapHeaders = soapHeaderExt.getSoapHeaders();
//
//                        for (int k = 0; k < soapHeaders.length; k++) {
//                            SOAPHeaderBlock soapHeader = soapHeaders[j];
//
//                            ElementDeclaration elementDeclaration = soapHeader.getElementDeclaration();
//
//                            if (elementDeclaration != null) {
//                                QName name = elementDeclaration.getName();
//                                SOAPHeaderMessage soapHeaderMessage = new SOAPHeaderMessage(name);
//                                soapHeaderMessage.setRequired(soapHeader.isRequired().booleanValue());
//                                message.addSoapHeader(soapHeaderMessage);
//                            }
//                        }

                axisBindingOperation.addChild(axisMessage.getDirection(), axisBindingMessage);


            }

            BindingFaultReference [] bindingFaultReferences =
                    bindingOperation.getBindingFaultReferences();
            for (int j = 0; j < bindingFaultReferences.length; j++) {
                BindingFaultReference bindingFaultReference = bindingFaultReferences[j];

                AxisBindingMessage axisBindingMessageFault = new AxisBindingMessage();
                axisBindingMessageFault.setParent(axisBindingOperation);
//                    AxisMessage axisFaultMessage = axisOperation.getMessage()
                axisBindingMessageFault.setName(bindingFaultReference.getInterfaceFaultReference()
                        .getInterfaceFault().getName().getLocalPart());

                SOAPBindingFaultReferenceExtensions soapBindingFaultReferenceExtensions = null;
                try {
                    soapBindingFaultReferenceExtensions =
                            (SOAPBindingFaultReferenceExtensions) bindingFaultReference
                                    .getComponentExtensionsForNamespace(
                                            new URI(WSDL2Constants.URI_WSDL2_SOAP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("Soap Binding Extention not found");
                }

                axisBindingMessageFault.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                        soapBindingFaultReferenceExtensions.getSoapModules());

                axisBindingOperation.addFault(axisBindingMessageFault);

            }

            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);


        }
    }

    private void processHTTPBindingExtention(Binding binding, AxisBinding axisBinding)
            throws AxisFault {

        // Capture all the binding specific properties

        HTTPBindingExtensionsImpl httpBindingExtensions = null;
        try {
            httpBindingExtensions = (HTTPBindingExtensionsImpl) binding
                    .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_HTTP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Extention not found");
        }

        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpBindingExtensions.getHttpMethodDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, httpBindingExtensions.getHttpQueryParameterSeparatorDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING, httpBindingExtensions.getHttpTransferCodingDefault());

        // Capture all the fault specific properties

        BindingFault[] bindingFaults = binding.getBindingFaults();
        for (int i = 0; i < bindingFaults.length; i++) {
            BindingFault bindingFault = bindingFaults[i];
            InterfaceFault interfaceFault = bindingFault.getInterfaceFault();

            AxisBindingMessage axisBindingFault = new AxisBindingMessage();
            axisBindingFault.setName(interfaceFault.getName().getLocalPart());
            axisBindingFault.setParent(axisBinding);

            HTTPBindingFaultExtensions httpBindingFaultExtensions = null;

            try {
                httpBindingFaultExtensions = (HTTPBindingFaultExtensions) bindingFault
                        .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_HTTP));
            } catch (URISyntaxException e) {
                throw new AxisFault("HTTP Binding Extention not found");
            }

            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CODE,
                    httpBindingFaultExtensions.getHttpErrorStatusCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                    httpBindingFaultExtensions.getHttpHeaders());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING,
                    httpBindingFaultExtensions.getHttpTransferCoding());

            axisBinding.addFault(axisBindingFault);

        }

        // Capture all the binding operation specific properties

        BindingOperation[] bindingOperations = binding.getBindingOperations();
        for (int i = 0; i < bindingOperations.length; i++) {
            BindingOperation bindingOperation = bindingOperations[i];

            AxisBindingOperation axisBindingOperation = new AxisBindingOperation();
            AxisOperation axisOperation =
                    axisService.getOperation(bindingOperation.getInterfaceOperation().getName());

            axisBindingOperation.setAxisOperation(axisOperation);
            axisBindingOperation.setParent(axisBinding);
            axisBindingOperation.setName(axisOperation.getName());

            HTTPBindingOperationExtensions httpBindingOperationExtensions = null;
            try {
                httpBindingOperationExtensions = ((HTTPBindingOperationExtensions)
                        bindingOperation.getComponentExtensionsForNamespace(
                                new URI(WSDL2Constants.URI_WSDL2_HTTP)));
            } catch (URISyntaxException e) {
                throw new AxisFault("HTTP Binding Extention not found");
            }

            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_FAULT_SERIALIZATION, httpBindingOperationExtensions.getHttpFaultSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION, httpBindingOperationExtensions.getHttpInputSerialization());
            URI httpLocation = httpBindingOperationExtensions.getHttpLocation();
            if (httpLocation != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
            }
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpBindingOperationExtensions.getHttpMethod());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION, httpBindingOperationExtensions.getHttpOutputSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, httpBindingOperationExtensions.getHttpQueryParameterSeparator());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING, httpBindingOperationExtensions.getHttpTransferCodingDefault());

            BindingMessageReference[] bindingMessageReferences =
                    bindingOperation.getBindingMessageReferences();
            for (int j = 0; j < bindingMessageReferences.length; j++) {
                BindingMessageReference bindingMessageReference = bindingMessageReferences[j];

                AxisBindingMessage axisBindingMessage = new AxisBindingMessage();
                axisBindingMessage.setParent(axisBindingOperation);

                AxisMessage axisMessage = axisOperation.getMessage(bindingMessageReference
                        .getInterfaceMessageReference().getMessageLabel().toString());

                axisBindingMessage.setAxisMessage(axisMessage);
                axisBindingMessage.setName(axisMessage.getName());
                axisBindingMessage.setDirection(axisMessage.getDirection());


                HTTPBindingMessageReferenceExtensions httpBindingMessageReferenceExtensions = null;
                try {
                    httpBindingMessageReferenceExtensions =
                            (HTTPBindingMessageReferenceExtensions) bindingMessageReference
                                    .getComponentExtensionsForNamespace(
                                            new URI(WSDL2Constants.URI_WSDL2_HTTP));
                } catch (URISyntaxException e) {
                    throw new AxisFault("HTTP Binding Extention not found");
                }

                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER, httpBindingMessageReferenceExtensions.getHttpHeaders());
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_TRANSFER_CODING, httpBindingMessageReferenceExtensions.getHttpTransferCoding());

//                    SOAPHeaderBlock[] soapHeaders = soapHeaderExt.getSoapHeaders();
//
//                        for (int k = 0; k < soapHeaders.length; k++) {
//                            SOAPHeaderBlock soapHeader = soapHeaders[j];
//
//                            ElementDeclaration elementDeclaration = soapHeader.getElementDeclaration();
//
//                            if (elementDeclaration != null) {
//                                QName name = elementDeclaration.getName();
//                                SOAPHeaderMessage soapHeaderMessage = new SOAPHeaderMessage(name);
//                                soapHeaderMessage.setRequired(soapHeader.isRequired().booleanValue());
//                                message.addSoapHeader(soapHeaderMessage);
//                            }
//                        }

                axisBindingOperation.addChild(axisMessage.getDirection(),axisBindingMessage);


            }

            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);


        }
    }

    private void processInterface(Interface serviceInterface)
            throws AxisFault {

        // TODO @author Chathura copy the policy elements
        // copyExtensionAttributes(wsdl4jPortType.getExtensionAttributes(),
        // axisService, PORT_TYPE);

        InterfaceOperation[] interfaceOperations = serviceInterface
                .getInterfaceOperations();
        for (int i = 0; i < interfaceOperations.length; i++) {
            axisService.addOperation(populateOperations(interfaceOperations[i]));
        }

        Interface[] extendedInterfaces = serviceInterface.getExtendedInterfaces();
        for (int i = 0; i < extendedInterfaces.length; i++) {
            Interface extendedInterface = extendedInterfaces[i];
            processInterface(extendedInterface);
        }

    }

    private AxisOperation populateOperations(InterfaceOperation operation) throws AxisFault {
        QName opName = operation.getName();
        // Copy Name Attribute
        AxisOperation axisOperation = axisService.getOperation(opName);

        if (axisOperation == null) {
            String MEP = operation.getMessageExchangePattern().toString();
            axisOperation = AxisOperationFactory.getOperationDescription(MEP);
            axisOperation.setName(opName);

        }

        // assuming the style of the operations of WSDL 2.0 is always document, for the time being :)
        axisOperation.setMessageExchangePattern(operation.getMessageExchangePattern().toString());
        axisOperation.setStyle("document");

        // The following can be used to capture the wsdlx:safe attribute

//        InterfaceOperationExtensionsImpl interfaceOperationExtensions;
//        try {
//            interfaceOperationExtensions = (InterfaceOperationExtensionsImpl)operation.getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_EXTENSIONS));
//        } catch (URISyntaxException e) {
//            throw new AxisFault("WSDL2 extensions not defined for this operation");
//        }
//
//        interfaceOperationExtensions.isSafety();


        InterfaceMessageReference[] interfaceMessageReferences = operation
                .getInterfaceMessageReferences();
        for (int i = 0; i < interfaceMessageReferences.length; i++) {
            InterfaceMessageReference messageReference = interfaceMessageReferences[i];
            if (messageReference.getMessageLabel().equals(
                    MessageLabel.IN)) {
                // Its an input message

                if (isServerSide) {
                    createAxisMessage(axisOperation, messageReference,
                            WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                } else {
                    createAxisMessage(axisOperation, messageReference,
                            WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                }
            } else if (messageReference.getMessageLabel().equals(
                    MessageLabel.OUT)) {
                if (isServerSide) {
                    createAxisMessage(axisOperation, messageReference,
                            WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                } else {
                    createAxisMessage(axisOperation, messageReference,
                            WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                }
            }

        }

        // add operation level faults

        InterfaceFaultReference[] faults = operation.getInterfaceFaultReferences();
        for (int i = 0; i < faults.length; i++) {
            AxisMessage faultMessage = new AxisMessage();

            InterfaceFaultReference interfaceFaultReference = faults[i];
            faultMessage.setDirection(interfaceFaultReference.getDirection().toString());

            InterfaceFault interfaceFault = interfaceFaultReference.getInterfaceFault();

            faultMessage.setElementQName(interfaceFault.getElementDeclaration().getName());
            faultMessage.setName(interfaceFault.getName().getLocalPart());

            axisOperation.setFaultMessages(faultMessage);
        }


        return axisOperation;
    }

    private void createAxisMessage(AxisOperation axisOperation,
                                   InterfaceMessageReference messageReference, String messageLabel)
            throws AxisFault {
        AxisMessage message = axisOperation
                .getMessage(messageLabel);

        String messageContentModelName = messageReference.getMessageContentModel();
        QName elementQName = null;

        if (WSDLConstants.WSDL20_2006Constants.NMTOKEN_ELEMENT.equals(messageContentModelName)) {
            elementQName = messageReference.getElementDeclaration().getName();
        } else if (WSDLConstants.WSDL20_2006Constants.NMTOKEN_ANY.equals(messageContentModelName)) {
            // TODO : Need to improve this
//           elementQName = SchemaConstants.XSD_ANY;
        } else
        if (WSDLConstants.WSDL20_2006Constants.NMTOKEN_NONE.equals(messageContentModelName)) {
            // TODO : Need to improve this
//           elementQName = new QName("None");
        } else {
            throw new AxisFault("Sorry we do not support " + messageContentModelName +
                    ". We do only support #any, #none and #element as message content models.");
        }

        message.setElementQName(elementQName);
        message.setName(elementQName != null ? elementQName.getLocalPart() : null);
        axisOperation.addMessage(message, messageLabel);

        // populate this map so that this can be used in SOAPBody based dispatching
        if (elementQName != null) {
            axisService
                    .addmessageNameToOperationMapping(elementQName.getLocalPart(), axisOperation);
        }
    }

    private Element createSchemaForInterface(InterfaceElement interfaceElement,
                                             String targetNamespaceUri, boolean forceWrapping) {

        // loop through the messages. We'll populate things map with the
        // relevant
        // messages
        // from the operations

        // this will have name (QName) as the key and
        // InterfaceMessageReferenceElement as the value
        Map messagesMap = new HashMap();

        // this will have operation name (a QName) as the key and
        // InterfaceMessageReferenceElement as the value
        Map inputOperationsMap = new HashMap();

        // this will have operation name (a QName) as the key and
        // InterfaceMessageReferenceElement as the value
        Map outputOperationsMap = new HashMap();

        Map faultyOperationsMap = new HashMap();
        // this contains the required namespace imports. the key in this
        // map would be the namaspace URI
        Map namespaceImportsMap = new HashMap();
        // generated complextypes. Keep in the list for writing later
        // the key for the complexType map is the message QName
        Map complexTypeElementsMap = new HashMap();
        // generated Elements. Kep in the list for later writing
        List elementElementsList = new ArrayList();
        // list namespace prefix map. This map will include uri -> prefix
        Map namespacePrefixMap = new HashMap();

        // //////////////////////////////////////////////////////////////////////////////////////////////////
        // First thing is to populate the message map with the messages to
        // process.
        // //////////////////////////////////////////////////////////////////////////////////////////////////

        // we really need to do this for a single porttype!
        InterfaceOperationElement[] operationElements = interfaceElement
                .getInterfaceOperationElements();
        InterfaceOperationElement opElement;
        for (int k = 0; k < operationElements.length; k++) {
            opElement = operationElements[k];
            InterfaceMessageReferenceElement[] interfaceMessageReferenceElements = opElement
                    .getInterfaceMessageReferenceElements();

            for (int i = 0; i < interfaceMessageReferenceElements.length; i++) {
                InterfaceMessageReferenceElement interfaceMessageReferenceElement =
                        interfaceMessageReferenceElements[i];
                String direction = interfaceMessageReferenceElement
                        .getDirection().toString();
                messagesMap.put(interfaceMessageReferenceElement
                        .getElementName(), interfaceMessageReferenceElement);
                if (Direction.IN.toString().equalsIgnoreCase(direction)) {
                    inputOperationsMap.put(opElement.getName(),
                            interfaceMessageReferenceElement);
                } else if (Direction.OUT.toString().equalsIgnoreCase(direction)) {
                    outputOperationsMap.put(opElement.getName(),
                            interfaceMessageReferenceElement);
                }
            }

            InterfaceFaultReferenceElement[] interfaceFaultReferenceElements = opElement
                    .getInterfaceFaultReferenceElements();

            for (int i = 0; i < interfaceFaultReferenceElements.length; i++) {
                InterfaceFaultReferenceElement interfaceFaultReferenceElement =
                        interfaceFaultReferenceElements[i];
                String direction = interfaceFaultReferenceElement
                        .getDirection().toString();
                messagesMap.put(interfaceFaultReferenceElement.getRef(),
                        interfaceFaultReferenceElement);
                faultyOperationsMap.put(interfaceFaultReferenceElement
                        .getInterfaceFaultElement(),
                        interfaceFaultReferenceElement);
            }

        }

        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // check whether there are messages that are wrappable. If there are no
        // messages that are wrappable we'll
        // just return null and endup this process. However we need to take the
        // force flag into account here
        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        QName[] keys;
        if (forceWrapping) {
            // just take all the messages and wrap them, we've been told to
            // force wrapping!
            keys = (QName[]) messagesMap.keySet().toArray(
                    new QName[messagesMap.size()]);
        } else {
            //
            QName[] allKeys = (QName[]) messagesMap.keySet().toArray(
                    new QName[messagesMap.size()]);
            List wrappableMessageNames = new ArrayList();
            boolean noMessagesTobeProcessed = true;

            // TODO Fix this
            // for (int i = 0; i < allKeys.length; i++) {
            // if (findWrapppable((Message) messagesMap.get(allKeys[i]))) {
            // noMessagesTobeProcessed = false;
            // //add that message to the list
            // wrappableMessageNames.add(allKeys[i]);
            // }
            // }
            if (noMessagesTobeProcessed) {
                return null;
            }

            keys = (QName[]) wrappableMessageNames
                    .toArray(new QName[wrappableMessageNames.size()]);
        }

        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Now we have the message list to process - Process the whole list of
        // messages at once
        // since we need to generate one single schema
        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // List resolvedMessageQNames = new ArrayList();
        // //find the xsd prefix
        // String xsdPrefix = findSchemaPrefix();
        // Message wsdl4jMessage;
        // //DOM document that will be the ultimate creator
        // Document document = getDOMDocumentBuilder().newDocument();
        // for (int i = 0; i < keys.length; i++) {
        // wsdl4jMessage = (Message) messagesMap.get(keys[i]);
        // //No need to check the wrappable,
        //
        // //This message is wrappabel. However we need to see whether the
        // // message is already
        // //resolved!
        // if (!resolvedMessageQNames.contains(wsdl4jMessage.getQName())) {
        // //This message has not been touched before!. So we can go ahead
        // // now
        // Map parts = wsdl4jMessage.getParts();
        // //add the complex type
        // String name = wsdl4jMessage.getQName().getLocalPart();
        // Element newComplexType = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_COMPLEX_TYPE_LOCAL_NAME);
        // newComplexType.setAttribute(XSD_NAME, name);
        //
        // Element cmplxContentSequence = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_SEQUENCE_LOCAL_NAME);
        // Element child;
        // Iterator iterator = parts.keySet().iterator();
        // while (iterator.hasNext()) {
        // Part part = (Part) parts.get(iterator.next());
        // //the part name
        // String elementName = part.getName();
        // boolean isTyped = true;
        // //the type name
        // QName schemaTypeName;
        // if (part.getTypeName() != null) {
        // schemaTypeName = part.getTypeName();
        // } else if (part.getElementName() != null) {
        // schemaTypeName = part.getElementName();
        // isTyped = false;
        // } else {
        // throw new RuntimeException(" Unqualified Message part!");
        // }
        //
        // child = document.createElementNS(XMLSCHEMA_NAMESPACE_URI,
        // xsdPrefix + ":" + XML_SCHEMA_ELEMENT_LOCAL_NAME);
        //
        // String prefix;
        // if (XMLSCHEMA_NAMESPACE_URI.equals(schemaTypeName
        // .getNamespaceURI())) {
        // prefix = xsdPrefix;
        // } else {
        // //this schema is a third party one. So we need to have
        // // an import statement in our generated schema
        // String uri = schemaTypeName.getNamespaceURI();
        // if (!namespaceImportsMap.containsKey(uri)) {
        // //create Element for namespace import
        // Element namespaceImport = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_IMPORT_LOCAL_NAME);
        // namespaceImport.setAttribute("namespace", uri);
        // //add this to the map
        // namespaceImportsMap.put(uri, namespaceImport);
        // //we also need to associate this uri with a prefix
        // // and include that prefix
        // //in the schema's namspace declarations. So add
        // // theis particular namespace to the
        // //prefix map as well
        // prefix = getTemporaryNamespacePrefix();
        // namespacePrefixMap.put(uri, prefix);
        // } else {
        // //this URI should be already in the namspace prefix
        // // map
        // prefix = (String) namespacePrefixMap.get(uri);
        // }
        //
        // }
        // // If it's from a type the element we need to add a name and
        // // the type
        // //if not it's the element reference
        // if (isTyped) {
        // child.setAttribute(XSD_NAME, elementName);
        // child.setAttribute(XSD_TYPE, prefix + ":"
        // + schemaTypeName.getLocalPart());
        // } else {
        // child.setAttribute(XSD_REF, prefix + ":"
        // + schemaTypeName.getLocalPart());
        // }
        // cmplxContentSequence.appendChild(child);
        // }
        // newComplexType.appendChild(cmplxContentSequence);
        // //add this newly created complextype to the list
        // complexTypeElementsMap.put(wsdl4jMessage.getQName(),
        // newComplexType);
        // resolvedMessageQNames.add(wsdl4jMessage.getQName());
        // }
        //
        // }
        //
        // Element elementDeclaration;
        //
        // //loop through the input op map and generate the elements
        // String[] inputOperationtNames = (String[])
        // inputOperationsMap.keySet()
        // .toArray(new String[inputOperationsMap.size()]);
        // for (int j = 0; j < inputOperationtNames.length; j++) {
        // String inputOpName = inputOperationtNames[j];
        // elementDeclaration = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_ELEMENT_LOCAL_NAME);
        // elementDeclaration.setAttribute(XSD_NAME, inputOpName);
        //
        // String typeValue = ((Message) inputOperationsMap.get(inputOpName))
        // .getQName().getLocalPart();
        // elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
        // + typeValue);
        // elementElementsList.add(elementDeclaration);
        // resolvedRpcWrappedElementMap.put(inputOpName, new QName(
        // targetNamespaceUri, inputOpName, AXIS2WRAPPED));
        // }
        //
        // //loop through the output op map and generate the elements
        // String[] outputOperationtNames = (String[]) outputOperationsMap
        // .keySet().toArray(new String[outputOperationsMap.size()]);
        // for (int j = 0; j < outputOperationtNames.length; j++) {
        //
        // String baseoutputOpName = outputOperationtNames[j];
        // String outputOpName = baseoutputOpName + "Response";
        // elementDeclaration = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_ELEMENT_LOCAL_NAME);
        // elementDeclaration.setAttribute(XSD_NAME, outputOpName);
        // String typeValue = ((Message) outputOperationsMap
        // .get(baseoutputOpName)).getQName().getLocalPart();
        // elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
        // + typeValue);
        // elementElementsList.add(elementDeclaration);
        // resolvedRpcWrappedElementMap.put(outputOpName, new QName(
        // targetNamespaceUri, outputOpName, AXIS2WRAPPED));
        //
        // }
        //
        // //loop through the faultoutput op map and generate the elements
        // String[] faultyOperationtNames = (String[]) faultyOperationsMap
        // .keySet().toArray(new String[faultyOperationsMap.size()]);
        // for (int j = 0; j < faultyOperationtNames.length; j++) {
        //
        // String baseFaultOpName = faultyOperationtNames[j];
        // elementDeclaration = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_ELEMENT_LOCAL_NAME);
        // elementDeclaration.setAttribute(XSD_NAME, baseFaultOpName);
        // String typeValue = ((Message) faultyOperationsMap
        // .get(baseFaultOpName)).getQName().getLocalPart();
        // elementDeclaration.setAttribute(XSD_TYPE, AXIS2WRAPPED + ":"
        // + typeValue);
        // elementElementsList.add(elementDeclaration);
        // resolvedRpcWrappedElementMap.put(baseFaultOpName, new QName(
        // targetNamespaceUri, baseFaultOpName, AXIS2WRAPPED));
        //
        // }
        //
        // //////////////////////////////////////////////////////////////////////////////////////////////
        // // Now we are done with processing the messages and generating the
        // right
        // // schema object model
        // // time to write out the schema
        // //////////////////////////////////////////////////////////////////////////////////////////////
        //
        // Element schemaElement = document.createElementNS(
        // XMLSCHEMA_NAMESPACE_URI, xsdPrefix + ":"
        // + XML_SCHEMA_LOCAL_NAME);
        //
        // //loop through the namespace declarations first
        // String[] nameSpaceDeclarationArray = (String[]) namespacePrefixMap
        // .keySet().toArray(new String[namespacePrefixMap.size()]);
        // for (int i = 0; i < nameSpaceDeclarationArray.length; i++) {
        // String s = nameSpaceDeclarationArray[i];
        // schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
        // "xmlns:" + namespacePrefixMap.get(s).toString(), s);
        //
        // }
        //
        // //add the targetNamespace
        //
        // schemaElement.setAttributeNS("http://www.w3.org/2000/xmlns/",
        // XMLNS_AXIS2WRAPPED, targetNamespaceUri);
        // schemaElement.setAttribute(XSD_TARGETNAMESPACE, targetNamespaceUri);
        // schemaElement.setAttribute(XSD_ELEMENT_FORM_DEFAULT,
        // XSD_UNQUALIFIED);
        //
        // Element[] namespaceImports = (Element[]) namespaceImportsMap.values()
        // .toArray(new Element[namespaceImportsMap.size()]);
        // for (int i = 0; i < namespaceImports.length; i++) {
        // schemaElement.appendChild(namespaceImports[i]);
        //
        // }
        //
        // Element[] complexTypeElements = (Element[]) complexTypeElementsMap
        // .values().toArray(new Element[complexTypeElementsMap.size()]);
        // for (int i = 0; i < complexTypeElements.length; i++) {
        // schemaElement.appendChild(complexTypeElements[i]);
        //
        // }
        //
        // Element[] elementDeclarations = (Element[]) elementElementsList
        // .toArray(new Element[elementElementsList.size()]);
        // for (int i = 0; i < elementDeclarations.length; i++) {
        // schemaElement.appendChild(elementDeclarations[i]);
        //
        // }

        // return schemaElement;

        return null;
    }

    private boolean findWrapForceable(BindingElement binding) {
        boolean retVal = false;
        if (RPC.equalsIgnoreCase(binding.getInterfaceElement()
                .getStyleDefault().toString())) {
            return true;
        }
        if (!retVal) {
            InterfaceOperationElement[] operations = binding
                    .getInterfaceElement().getInterfaceOperationElements();
            for (int i = 0; i < operations.length; i++) {
                URI[] styles = operations[i].getStyle();
                for (int j = 0; j < styles.length; j++) {
                    if (RPC.equalsIgnoreCase(styles[j].toString())) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    private DescriptionElement readInTheWSDLFile(String wsdlURI)
            throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();

        // TODO : I can not find a constant for this feature in WSDLReader
        // reader.setFeature("javax.wsdl.importDocuments", false);

//        reader.setFeature(WSDLReader.FEATURE_VERBOSE, false);
        return reader.readWSDL(wsdlURI);
    }

    private ArrayList createSoapHeaders(SOAPHeaderBlock soapHeaderBlocks[]) {

        ArrayList soapHeaderMessages = new ArrayList();

        for (int i = 0; i < soapHeaderBlocks.length; i++) {
            SOAPHeaderBlock soapHeaderBlock = soapHeaderBlocks[i];
            ElementDeclaration elementDeclaration = soapHeaderBlock.getElementDeclaration();

            if (elementDeclaration != null) {
                QName name = elementDeclaration.getName();
                SOAPHeaderMessage soapHeaderMessage = new SOAPHeaderMessage(name);
                soapHeaderMessage.setRequired(soapHeaderBlock.isRequired().booleanValue());
                soapHeaderMessages.add(soapHeaderMessage);
            }
        }

        return soapHeaderMessages;
    }



}
