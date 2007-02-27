package org.apache.axis2.description;

import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.transport.http.util.RESTUtil;
import org.apache.axis2.wsdl.HTTPHeaderMessage;
import org.apache.axis2.wsdl.SOAPHeaderMessage;
import org.apache.axis2.wsdl.SOAPModuleMessage;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.woden.WSDLException;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.WSDLSource;
import org.apache.woden.internal.DOMWSDLFactory;
import org.apache.woden.internal.wsdl20.extensions.InterfaceOperationExtensionsImpl;
import org.apache.woden.internal.wsdl20.extensions.http.HTTPBindingExtensionsImpl;
import org.apache.woden.internal.wsdl20.extensions.http.HTTPHeaderImpl;
import org.apache.woden.internal.wsdl20.extensions.soap.SOAPBindingExtensionsImpl;
import org.apache.woden.schema.Schema;
import org.apache.woden.wsdl20.Binding;
import org.apache.woden.wsdl20.BindingFault;
import org.apache.woden.wsdl20.BindingFaultReference;
import org.apache.woden.wsdl20.BindingMessageReference;
import org.apache.woden.wsdl20.BindingOperation;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.ElementDeclaration;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Interface;
import org.apache.woden.wsdl20.InterfaceFault;
import org.apache.woden.wsdl20.InterfaceFaultReference;
import org.apache.woden.wsdl20.InterfaceMessageReference;
import org.apache.woden.wsdl20.InterfaceOperation;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.enumeration.MessageLabel;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingFaultExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingMessageReferenceExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPBindingOperationExtensions;
import org.apache.woden.wsdl20.extensions.http.HTTPHeader;
import org.apache.woden.wsdl20.extensions.http.HTTPLocation;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingFaultExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingFaultReferenceExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingMessageReferenceExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPBindingOperationExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPEndpointExtensions;
import org.apache.woden.wsdl20.extensions.soap.SOAPHeaderBlock;
import org.apache.woden.wsdl20.extensions.soap.SOAPModule;
import org.apache.woden.wsdl20.xml.DescriptionElement;
import org.apache.woden.wsdl20.xml.TypesElement;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        String fullPath = wsdlUri;
        if (!wsdlUri.startsWith("http://")){
           File file = new File(wsdlUri);
           fullPath = file.getAbsolutePath();
        }
        Description description = wsdlReader.readWSDL(fullPath);
        DescriptionElement descriptionElement = description.toElement();
        savedTargetNamespace = descriptionElement.getTargetNamespace()
                .toString();
        namespacemap = descriptionElement.getNamespaces();
        this.description = description;
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
            return axisService;
        } catch (Exception e) {
            throw new AxisFault(e);
        }
    }

    private void processEndpoints() throws AxisFault {
        Endpoint[] endpoints = wsdlService.getEndpoints();

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
                        .addEndpoint(endpoints[i].getName().toString(),
                                     processEndpoint(endpoints[i]));
            }
        }

        if (endpoint == null && endpoints.length > 0) {
            endpoint = endpoints[0];
        }

        axisService.setEndpointName(endpoint.getName().toString());
        axisService.setBindingName(endpoint.getBinding().getName().getLocalPart());
        axisService.setEndpointURL(endpoint.getAddress().toString());

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
            soapEndpointExtensions = (SOAPEndpointExtensions) endpoint
                    .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Extention not found");
        }

        if (soapEndpointExtensions != null) {

            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_TYPE,
                                     soapEndpointExtensions.getHttpAuthenticationScheme());
            axisEndpoint.setProperty(WSDL2Constants.ATTR_WHTTP_AUTHENTICATION_REALM,
                                     soapEndpointExtensions.getHttpAuthenticationRealm());

        }
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

                Description description = null;
                DescriptionElement descriptionElement = null;
                if (wsdlURI != null && !"".equals(wsdlURI)) {
                    description = readInTheWSDLFile(wsdlURI);
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
                    description = reader.readWSDL(wsdlSource);
                    descriptionElement = description.toElement();
                } else {
                    throw new AxisFault("No resources found to read the wsdl");
                }

                savedTargetNamespace = descriptionElement.getTargetNamespace().toString();
                namespacemap = descriptionElement.getNamespaces();
                this.description = description;

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

        processedBindings.put(binding.getName(), axisBinding);
        return axisBinding;
    }

    private void processSOAPBindingExtention(Binding binding, AxisBinding axisBinding)
            throws AxisFault {

        // Capture all the binding specific properties

        Map httpLocationTable = new TreeMap();
        SOAPBindingExtensionsImpl soapBindingExtensions = null;
        try {
            soapBindingExtensions = (SOAPBindingExtensionsImpl) binding
                    .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_SOAP));
        } catch (URISyntaxException e) {
            throw new AxisFault("Soap Binding Extention not found");
        }

        String soapVersion;
        if ((soapVersion = soapBindingExtensions.getSoapVersion()) != null)

        {
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
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                soapBindingExtensions.getHttpContentEncodingDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                createSoapModules(soapBindingExtensions.getSoapModules()));
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                soapBindingExtensions.getHttpQueryParameterSeparatorDefault());

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
                    createHttpHeaders(soapBindingFaultExtensions.getHttpHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                    soapBindingFaultExtensions.getHttpContentEncoding());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_CODE,
                    soapBindingFaultExtensions.getSoapFaultCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_SUBCODES,
                    soapBindingFaultExtensions.getSoapFaultSubcodes());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                    createSoapHeaders(soapBindingFaultExtensions.getSoapHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                    createSoapModules(soapBindingFaultExtensions.getSoapModules()));

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
                    createSoapModules(soapBindingOperationExtensions.getSoapModules()));
            URI soapMep = soapBindingOperationExtensions.getSoapMep();
            if (soapMep != null) {
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WSOAP_MEP,
                        soapMep.toString());
            }
            HTTPLocation httpLocation = soapBindingOperationExtensions.getHttpLocation();
            // If httpLocation is not null we should extract a constant part from it and add its value and the
            // corresponding AxisOperation to a map in order to dispatch rest messages. If httpLocation is null we add
            // the operation name into this map.
            String httpLocationString = "";
            if (httpLocation != null) {
                String httpLocationTemplete = httpLocation.getLocationTemplate();
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocationTemplete);
                httpLocationString = RESTUtil.getConstantFromHTTPLocation(httpLocationTemplete);

            }

            httpLocationTable.put(httpLocationString, axisOperation);
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                    soapBindingOperationExtensions.getHttpContentEncodingDefault());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                    soapBindingOperationExtensions.getHttpQueryParameterSeparator());


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
                        createHttpHeaders(soapBindingMessageReferenceExtensions.getHttpHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                        soapBindingMessageReferenceExtensions.getHttpContentEncoding());
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_HEADER,
                        createSoapHeaders(soapBindingMessageReferenceExtensions.getSoapHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WSOAP_MODULE,
                        createSoapModules(soapBindingMessageReferenceExtensions.getSoapModules()));

                axisBindingOperation.addChild(axisMessage.getDirection(), axisBindingMessage);

            }

            BindingFaultReference [] bindingFaultReferences =
                    bindingOperation.getBindingFaultReferences();
            for (int j = 0; j < bindingFaultReferences.length; j++) {
                BindingFaultReference bindingFaultReference = bindingFaultReferences[j];

                AxisBindingMessage axisBindingMessageFault = new AxisBindingMessage();
                axisBindingMessageFault.setParent(axisBindingOperation);
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
                        createSoapModules(soapBindingFaultReferenceExtensions.getSoapModules()));

                axisBindingOperation.addFault(axisBindingMessageFault);

            }
            axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE,httpLocationTable);
            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);


        }
    }

    private void processHTTPBindingExtention(Binding binding, AxisBinding axisBinding)
            throws AxisFault {


        Map httpLocationTable = new TreeMap();
        // Capture all the binding specific properties

        HTTPBindingExtensionsImpl httpBindingExtensions = null;
        try {
            httpBindingExtensions = (HTTPBindingExtensionsImpl) binding
                    .getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_HTTP));
        } catch (URISyntaxException e) {
            throw new AxisFault("HTTP Binding Extention not found");
        }

        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD,
                                httpBindingExtensions.getHttpMethodDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                                httpBindingExtensions.getHttpQueryParameterSeparatorDefault());
        axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                                httpBindingExtensions.getHttpContentEncodingDefault());

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
                    httpBindingFaultExtensions.getHttpErrorStatusCode().getCode());
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                    createHttpHeaders(httpBindingFaultExtensions.getHttpHeaders()));
            axisBindingFault.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                    httpBindingFaultExtensions.getHttpContentEncoding());
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

            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_FAULT_SERIALIZATION,
                    httpBindingOperationExtensions.getHttpFaultSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                    httpBindingOperationExtensions.getHttpInputSerialization());
            HTTPLocation httpLocation = httpBindingOperationExtensions.getHttpLocation();

            // If httpLocation is not null we should extract a constant part from it and add its value and the
            // corresponding AxisOperation to a map in order to dispatch rest messages. If httpLocation is null we add
            // the operation name into this map.
            String httpLocationString = "";
            if (httpLocation != null) {
                String httpLocationTemplete = httpLocation.getLocationTemplate();
                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocationTemplete);
                httpLocationString = RESTUtil.getConstantFromHTTPLocation(httpLocationTemplete);

            }

            httpLocationTable.put(httpLocationString, axisOperation);
            
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_IGNORE_UNCITED, httpBindingOperationExtensions.
                    isHttpLocationIgnoreUncited());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, httpBindingOperationExtensions.
                    getHttpMethod());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION,
                    httpBindingOperationExtensions.getHttpOutputSerialization());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                    httpBindingOperationExtensions.getHttpQueryParameterSeparator());
            axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                    httpBindingOperationExtensions.getHttpContentEncodingDefault());

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

                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_HEADER,
                        createHttpHeaders(httpBindingMessageReferenceExtensions.getHttpHeaders()));
                axisBindingMessage.setProperty(WSDL2Constants.ATTR_WHTTP_CONTENT_ENCODING,
                        httpBindingMessageReferenceExtensions.getHttpContentEncoding());
                axisBindingOperation.addChild(axisMessage.getDirection(), axisBindingMessage);

            }

            BindingFaultReference[] bindingFaultReferences =
                    bindingOperation.getBindingFaultReferences();
            for (int j = 0; j < bindingFaultReferences.length; j++) {
                BindingFaultReference bindingFaultReference = bindingFaultReferences[j];

                AxisBindingMessage axisBindingMessageFault =
                        axisBinding.getFault(bindingFaultReference.getInterfaceFaultReference()
                                .getInterfaceFault().getName().getLocalPart());

                axisBindingOperation.addFault(axisBindingMessageFault);

            }

            axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationTable);
            axisBinding.addChild(axisBindingOperation.getName(), axisBindingOperation);

        }
    }

    private void processInterface(Interface serviceInterface)
            throws AxisFault {

        // TODO @author Chathura copy the policy elements
        // copyExtensionAttributes(wsdl4jPortType.getExtensionAttributes(),
        // axisService, PORT_TYPE);

        List operationNames = new ArrayList();


        InterfaceOperation[] interfaceOperations = serviceInterface
                .getInterfaceOperations();
        for (int i = 0; i < interfaceOperations.length; i++) {
            axisService.addOperation(populateOperations(interfaceOperations[i]));
            operationNames.add(interfaceOperations[i].getName());
        }

        if (isCodegen){
            axisService.setOperationsNameList(operationNames);
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

//         The following can be used to capture the wsdlx:safe attribute

        InterfaceOperationExtensionsImpl interfaceOperationExtensions;
        try {
            interfaceOperationExtensions = (InterfaceOperationExtensionsImpl)operation.getComponentExtensionsForNamespace(new URI(WSDL2Constants.URI_WSDL2_EXTENSIONS));
        } catch (URISyntaxException e) {
            throw new AxisFault("WSDL2 extensions not defined for this operation");
        }

        if (interfaceOperationExtensions != null) {
            Parameter parameter = new Parameter(WSDL2Constants.ATTR_WSDLX_SAFE, new Boolean(interfaceOperationExtensions.isSafety()));
            axisOperation.addParameter(parameter);
        }


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
           elementQName = Constants.XSD_ANY;
        } else if (WSDLConstants.WSDL20_2006Constants.NMTOKEN_NONE.equals(messageContentModelName)) {
            // nothing to do here keep the message element as null
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
                    .addMessageElementQNameToOperationMapping(elementQName, axisOperation);
        }
    }

    private Description readInTheWSDLFile(String wsdlURI)
            throws WSDLException {

        WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
        return reader.readWSDL(wsdlURI);
    }

    /**
     * Convert woden dependent SOAPHeaderBlock objects to SOAPHeaderMessage objects
     *
     * @param soapHeaderBlocks - An array of SOAPHeaderBlock objects
     * @return ArrayList - An ArrayList of SOAPHeaderMessage objects
     */
    private ArrayList createSoapHeaders(SOAPHeaderBlock soapHeaderBlocks[]) {

        if (soapHeaderBlocks.length == 0) {
        return null;
        }
        ArrayList soapHeaderMessages = new ArrayList();

        for (int i = 0; i < soapHeaderBlocks.length; i++) {
            SOAPHeaderBlock soapHeaderBlock = soapHeaderBlocks[i];
            ElementDeclaration elementDeclaration = soapHeaderBlock.getElementDeclaration();

            if (elementDeclaration != null) {
                QName name = elementDeclaration.getName();
                SOAPHeaderMessage soapHeaderMessage = new SOAPHeaderMessage();
                soapHeaderMessage.setElement(name);
                soapHeaderMessage.setRequired(soapHeaderBlock.isRequired().booleanValue());
                soapHeaderMessage.setMustUnderstand(soapHeaderBlock.mustUnderstand().booleanValue());
                soapHeaderMessages.add(soapHeaderMessage);
            }
        }
        return soapHeaderMessages;
    }

    /**
     * Convert woden dependent SOAPHeaderBlock objects to SOAPHeaderMessage objects
     *
     * @param soapModules - An array of SOAPModule objects
     * @return ArrayList - An ArrayList of SOAPHeaderMessage objects
     */
    private ArrayList createSoapModules(SOAPModule soapModules[]) {

        if (soapModules.length == 0) {
        return null;
        }
        ArrayList soapModuleMessages = new ArrayList();

        for (int i = 0; i < soapModules.length; i++) {
            SOAPModule soapModule = soapModules[i];
            SOAPModuleMessage soapModuleMessage = new SOAPModuleMessage();
            soapModuleMessage.setUri(soapModule.getRef().toString());
            soapModuleMessages.add(soapModuleMessage);
        }
        return soapModuleMessages;
    }

    /**
     * Convert woden dependent HTTPHeader objects to Header objects
     *
     * @param httpHeaders - An array of HTTPHeader objects
     * @return ArrayList - An ArrayList of Header objects
     */
    private ArrayList createHttpHeaders(HTTPHeader httpHeaders[]) {

        if (httpHeaders.length == 0) {
        return null;
        }
        ArrayList httpHeaderMessages = new ArrayList();

        for (int i = 0; i < httpHeaders.length; i++) {
            HTTPHeaderImpl httpHeader = (HTTPHeaderImpl)httpHeaders[i];
            HTTPHeaderMessage httpHeaderMessage = new HTTPHeaderMessage();
                httpHeaderMessage.setqName(httpHeader.getTypeName());
                httpHeaderMessage.setName(httpHeader.getName());
                httpHeaderMessage.setRequired(httpHeader.isRequired().booleanValue());
                httpHeaderMessages.add(httpHeaderMessage);
        }
        return httpHeaderMessages;
    }
}
