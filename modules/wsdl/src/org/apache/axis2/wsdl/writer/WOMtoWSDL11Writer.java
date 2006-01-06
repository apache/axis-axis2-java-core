package org.apache.axis2.wsdl.writer;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PolicyReference;
import org.apache.ws.policy.util.PolicyFactory;
import org.apache.ws.policy.util.StAXPolicyWriter;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.WSDLEndpoint;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLImport;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.WSDLService;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.PolicyExtensibilityElement;
import org.apache.wsdl.extensions.SOAPAddress;
import org.apache.wsdl.extensions.SOAPBinding;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.wsdl.extensions.SOAPHeader;
import org.apache.wsdl.extensions.SOAPOperation;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;

import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

public class WOMtoWSDL11Writer implements WOMWriter {

    private String encoding = "UTF-8"; //default encoding is UTF-8
    private String defaultWSDLPrefix = "wsdl11";
    private String targetNamespacePrefix = "tns";
    private static final String WSDL1_1_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    private static final String WSDL1_1_SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final String DEFINITION_NAME = "definitions";
    private static final String IMPORT_NAME = "import";
    private static final String TYPES_NAME = "types";
    private static final String PORTTYPE_NAME = "portType";

    private static final String MESSAGE_NAME_SUFFIX = "Message";

    private Map namespaceMap = new HashMap();

    //this is our 'symbol table' for the time being. It's a simple
    //Qname <-> message object map
    private Map messageMap = new HashMap();
    private static final String BINDING_OPERATION = "operation";
    private static final String OPERATION_NAME = BINDING_OPERATION;
    private static final String BINDING_INPUT = "input";
    private static final String INPUT_NAME = BINDING_INPUT;
    private static final String BINDING_OUTPUT = "output";
    private static final String OUTPUT_NAME = BINDING_OUTPUT;
    private static final String BINDING_NAME = "binding";

    private String soapNsPrefix = null;
    private XMLStreamWriter writer;

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    //to set the defaultWSDLPrefix for the wsdl file
    public void setdefaultWSDLPrefix(String defaultWSDLPrefix) {
        this.defaultWSDLPrefix = defaultWSDLPrefix;
    }

    /**
     * @param wsdlWrapper
     * @param out
     * @see WOMWriter#writeWOM(org.apache.axis2.wsdl.WSDLVersionWrapper, java.io.OutputStream)
     */
    public void writeWOM(WSDLVersionWrapper wsdlWrapper, OutputStream out) throws WriterException {
        if (wsdlWrapper == null) {
            throw new WriterException("wsdl Wrapper cannot be null");
        }
        //the wsdl definition is present. then we can ditectly utilize the wsdl writer.
        if (wsdlWrapper.getDefinition() != null) {
            try {
                WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                writer.writeWSDL(wsdlWrapper.getDefinition(), out);
            } catch (WSDLException e) {
                throw new WriterException(e);
            }
            //wsdl description is present but not the definition. So start writing
        } else if (wsdlWrapper.getDescription() != null) {
            try {
                writeWOM(wsdlWrapper.getDescription(), out);
            } catch (XMLStreamException e) {
                throw new WriterException(e);
            }
        } else {
            throw new WriterException(" WSDL wrapper is empty!");
        }
    }

    /**
     * @param wsdlDescription
     * @param out
     * @throws WriterException
     * @see WOMWriter#writeWOM(org.apache.wsdl.WSDLDescription, java.io.OutputStream)
     */
    public void writeWOM(WSDLDescription wsdlDescription, OutputStream out) throws WriterException, XMLStreamException {
        try {
            //create a writer from the stream
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            writeStartDescripton(wsdlDescription, writer);
            //find the SOAPNs
            findSOAPNsPrefix(wsdlDescription);
            //write the imports
            writeImports(wsdlDescription);
            //write extensibility elements
            handleExtensibiltyElements(wsdlDescription.getExtensibilityElements());
            
            //write  the types            
            writeTypes(wsdlDescription);
            //write the messages
            writeMessages(wsdlDescription);
            //write the porttype
            writePortTypes(wsdlDescription);
            //write the binding
            writeBinding(wsdlDescription);
            //write the service
            writeService(wsdlDescription);
            //close definition
            writer.writeEndElement();
            writer.flush();

        } catch (UnsupportedEncodingException e) {
            throw new WriterException("wrong encoding!", e);
        } catch (IOException e) {
            throw new WriterException("Error writing to the stream!", e);
        }

    }

    /**
     * @param wsdlDescription
     * @throws IOException
     */
    private void writeService(WSDLDescription wsdlDescription) throws IOException, XMLStreamException {
        Map serviceMap = wsdlDescription.getServices();
        if (serviceMap != null && !serviceMap.isEmpty()) {
            Iterator serviceIterator = serviceMap.values().iterator();
            WSDLService service;
            while (serviceIterator.hasNext()) {
                service = (WSDLService) serviceIterator.next();
                writer.writeStartElement(defaultWSDLPrefix, "service", WSDL1_1_NAMESPACE_URI);
                writer.writeAttribute("name", service.getName().getLocalPart());
                handleExtensibiltyElements(service.getExtensibilityElements());
                //wrtie the porttypes
                Map endPointMap = service.getEndpoints();
                if (endPointMap != null && !endPointMap.isEmpty()) {
                    Iterator it = endPointMap.values().iterator();
                    while (it.hasNext()) {
                        writePort((WSDLEndpoint) it.next());
                    }
                }
                writer.writeEndElement();
            }
        }
    }

    /**
     * @param endpoint
     */
    private void writePort(WSDLEndpoint endpoint) throws IOException, XMLStreamException {
        writer.writeStartElement(defaultWSDLPrefix, "port", WSDL1_1_NAMESPACE_URI);
        writer.writeAttribute("name", endpoint.getName().getLocalPart());
        QName name = endpoint.getBinding().getName();
        writer.writeAttribute("binding", name.getPrefix() + ":" + name.getLocalPart());

        handleExtensibiltyElements(endpoint.getExtensibilityElements());

        writer.writeEndElement();
    }

    /**
     * Finds the SOAPns prefix
     *
     * @param wsdlDescription
     */
    private void findSOAPNsPrefix(WSDLDescription wsdlDescription) {
        Map map = wsdlDescription.getNamespaces();
        Iterator nsIterator = map.values().iterator();
        Iterator keyIterator = map.keySet().iterator();
        String key;
        while (nsIterator.hasNext()) {
            key = keyIterator.next().toString();
            if (WSDL1_1_SOAP_NAMESPACE_URI.equals(nsIterator.next())) {
                this.soapNsPrefix = key;
                break;
            }
        }

    }

    /**
     * @param desc
     * @throws IOException
     */
    protected void writeStartDescripton(WSDLDescription desc, XMLStreamWriter writer) throws IOException, XMLStreamException {
        //always prefix the elements with wsdl1.1 prefix
        writer.writeStartElement(defaultWSDLPrefix, DEFINITION_NAME, WSDL1_1_NAMESPACE_URI);
        //write the name
        QName wsdl1DefinitionName = desc.getWSDL1DefinitionName();
        if (wsdl1DefinitionName != null) {
            writer.writeAttribute("name", wsdl1DefinitionName.getLocalPart());
        }
        //loop through the namespaces
        String targetNameSpace = desc.getTargetNameSpace();

        namespaceMap = desc.getNamespaces();
        if (namespaceMap != null && !namespaceMap.isEmpty()) {
            Iterator nameSpaces = namespaceMap.keySet().iterator();
            String nsPrefix;
            String nsURI;
            while (nameSpaces.hasNext()) {
                nsPrefix = (String) nameSpaces.next();
                nsURI = namespaceMap.get(nsPrefix).toString();
                //make sure not to write the namespace URI of the WSDL 1.1 namespace
                if (!WSDL1_1_NAMESPACE_URI.equals(nsURI) &&
                        !targetNameSpace.equals(nsURI)) {
                    writeNamespace(nsPrefix,
                            nsURI);
                }

            }
        }
        //write the default WSDL namespace
        writeNamespace(defaultWSDLPrefix, WSDL1_1_NAMESPACE_URI);
        //write the targetnamespace with our own prefix
        writeNamespace(targetNamespacePrefix, targetNameSpace);
        //write the targetNamespace
        writer.writeAttribute("targetNamespace", desc.getTargetNameSpace());
    }

    private void writeNamespace(String namespacePrefix, String namespaceURI) throws XMLStreamException {
        // for the time being lets keep this simple as we do not expect that much of namespaces within the WSDL
        namespaceMap.put(namespacePrefix, namespaceURI);
        writer.writeNamespace(namespacePrefix, namespaceURI);

    }

    /**
     * Writes the messages. This is somewhat tricky when the message names and parts
     * have to be guessed.
     *
     * @param desc
     */
    protected void writeMessages(WSDLDescription desc) throws XMLStreamException {

        //first look for a metadata bag for this particular description
        Map mBag = desc.getMetadataBag();


        if (mBag == null || mBag.isEmpty()) {
            //No metadata! So do some guess work!
            Map interfaceMap = desc.getWsdlInterfaces();
            if (!interfaceMap.isEmpty()) {
                Iterator interfaceIterator = interfaceMap.values().iterator();
                WSDLInterface wsdlInterface;
                while (interfaceIterator.hasNext()) {
                    wsdlInterface = (WSDLInterface) interfaceIterator.next();
                    Map opMap = wsdlInterface.getOperations();
                    if (!opMap.isEmpty()) {
                        Iterator opIterator = opMap.values().iterator();
                        WSDLOperation operation;
                        while (opIterator.hasNext()) {
                            operation = (WSDLOperation) opIterator.next();
                            //populate the symbol table of Messages
                            QName inputReference = operation.getInputMessage() == null ? null : operation.getInputMessage().getElementQName();
                            if (inputReference != null) {
                                populateMessageSymbol(inputReference);
                            }

                            QName outputReference = operation.getOutputMessage() == null ? null : operation.getOutputMessage().getElementQName();
                            if (outputReference != null) {
                                populateMessageSymbol(outputReference);
                            }

                            //todo handle the faults here

                        }
                    }

                }

                // Now we are done with populating the message symbols. write them down
                Iterator messages = messageMap.values().iterator();
                while (messages.hasNext()) {
                    WSDL11Message msg = (WSDL11Message) messages.next();
                    writer.writeStartElement(defaultWSDLPrefix, "message", WSDL1_1_NAMESPACE_URI);
                    writer.writeAttribute("name", msg.getMessageName());
//                    writer.writeEndElement();

                    //write the parts
                    WSDL11MessagePart[] parts = msg.getParts();
                    WSDL11MessagePart part;
                    for (int i = 0; i < parts.length; i++) {
                        part = parts[i];
                        writer.writeStartElement(defaultWSDLPrefix, "part", WSDL1_1_NAMESPACE_URI);
                        writer.writeAttribute("name", part.getName());
                        String elementName = part.getElementName();
                        if (elementName != null) {
                            //todo ajith pls take a look at that I have done the correct thing here
                            if (part.getPrefix() != null && !part.getPrefix().trim().equals("")) {
                                elementName = part.getPrefix() + ":" + elementName;
                            }
                            writer.writeAttribute("element", elementName);
                        }
                        //put the type also here. For the time being let this be like it
                        writer.writeEndElement();

                    }

                    writer.writeEndElement();

                }


            } else {
                //use the metadata to formulate the names and stuff
                //todo fill this!!!!

            }
        }


    }

    /**
     * Our simple rule in the 'guessing game' for the message and it's parts.
     * Message name is the localpart of the QName suffixed by MESSAGE_NAME_SUFFIX
     * Partname is just 'part1' (wouldn't matter!)
     * Element reference is again the localpart of the QName but prefixed with the target
     * namespaces prefix.
     */

    private void populateMessageSymbol(QName reference) {
        if (!messageMap.containsKey(reference)) {
            //just return. The message is already there
            //create a part with name part 1 and element ref to the QName value
            //these references need to be prefixed according to the correct target namespaces
            //of the schemas
            WSDL11MessagePart part = new WSDL11MessagePart();
            part.setName("part1");
            part.setElementName(reference.getLocalPart());  //todo prefix needs to be here!!!!
            part.setPrefix(reference.getPrefix());
            WSDL11Message message = new WSDL11Message();
            message.setMessageName(reference.getLocalPart() + MESSAGE_NAME_SUFFIX);
            message.setParts(new WSDL11MessagePart[]{part});

            //fill this in the message symbol map
            messageMap.put(reference, message);
        }
    }

    /**
     * @param outWriter
     * @throws IOException
     */
    protected void writeEndDescripton(Writer outWriter) throws IOException {
        WriterUtil.writeEndElement(DEFINITION_NAME, defaultWSDLPrefix, outWriter);
    }

    /**
     * @param desc
     * @throws IOException
     */
    protected void writeImports(WSDLDescription desc) throws IOException, XMLStreamException {
        //todo 1.1 pump does not populate the imports
        //get the imports
        List imports = desc.getImports();
        int importCount = imports.size();
        WSDLImport singleImport;
        for (int i = 0; i < importCount; i++) {
            singleImport = (WSDLImport) imports.get(i);
            writer.writeStartElement(defaultWSDLPrefix, IMPORT_NAME, WSDL1_1_NAMESPACE_URI);
            writer.writeAttribute("namespace", singleImport.getNamespace());
            writer.writeAttribute("location", singleImport.getLocation());
            writer.writeEndElement();
        }


    }

    /**
     * Writes port types.
     *
     * @param desc
     * @throws IOException
     */
    protected void writeTypes(WSDLDescription desc) throws IOException, XMLStreamException {
        //get the imports
        WSDLTypes types = desc.getTypes();
        if (types != null) {
            writer.writeStartElement(defaultWSDLPrefix, TYPES_NAME, WSDL1_1_NAMESPACE_URI);
            handleExtensibiltyElements(types.getExtensibilityElements());
            writer.writeEndElement();
        }
    }

    /**
     * @param desc
     * @throws XMLStreamException
     * @throws IOException
     */
    protected void writePortTypes(WSDLDescription desc) throws XMLStreamException, IOException {
        Map interfaceMap = desc.getWsdlInterfaces();
        if (!interfaceMap.isEmpty()) {
            Iterator interfaceIterator = interfaceMap.values().iterator();
            WSDLInterface wsdlInterface;
            while (interfaceIterator.hasNext()) {
                wsdlInterface = (WSDLInterface) interfaceIterator.next();
                writer.writeStartElement(defaultWSDLPrefix, PORTTYPE_NAME, WSDL1_1_NAMESPACE_URI);
                writer.writeAttribute("name",
                        wsdlInterface.getName() == null ? "" : wsdlInterface.getName().getLocalPart());
                //write extensibility attributes
                handleExtensibilityAttributes(wsdlInterface.getExtensibilityAttributes());
                //write the operations
                writePorttypeOperations(wsdlInterface);

                writer.writeEndElement();

            }


        }
    }

    /**
     * Writes the operation.
     *
     * @param wsdlInterface
     * @throws XMLStreamException
     * @throws IOException
     */
    protected void writePorttypeOperations(WSDLInterface wsdlInterface) throws XMLStreamException, IOException {
        Map operationsMap = wsdlInterface.getOperations();
        if (!operationsMap.isEmpty()) {
            Iterator opIterator = operationsMap.values().iterator();
            WSDLOperation operation;
            while (opIterator.hasNext()) {
                operation = (WSDLOperation) opIterator.next();
                writer.writeStartElement(defaultWSDLPrefix, OPERATION_NAME, WSDL1_1_NAMESPACE_URI);
                writer.writeAttribute("name", operation.getName() == null ? "" : operation.getName().getLocalPart());
//                writer.writeEndElement();
                //write the inputs
                //write extensibility elements
                handleExtensibiltyElements(operation.getExtensibilityElements());
                WSDL11Message message;
                MessageReference inputMessage = operation.getInputMessage();
                if (inputMessage != null) {
                    message = (WSDL11Message) messageMap.get(inputMessage.getElementQName());
                    writer.writeStartElement(defaultWSDLPrefix, INPUT_NAME, WSDL1_1_NAMESPACE_URI);
                    writer.writeAttribute("message", targetNamespacePrefix + ":" + message.getMessageName());
                    //write extensibility attributes
                    handleExtensibilityAttributes(inputMessage.getExtensibilityAttributes());
                    writer.writeEndElement();
                }

                //write the outputs
                MessageReference outputMessage = operation.getOutputMessage();
                if (outputMessage != null) {
                    message = (WSDL11Message) messageMap.get(outputMessage.getElementQName());
                    writer.writeStartElement(defaultWSDLPrefix, OUTPUT_NAME, WSDL1_1_NAMESPACE_URI);
                    writer.writeAttribute("message", targetNamespacePrefix + ":" + message.getMessageName());
//                  write extensibility attributes
                    handleExtensibilityAttributes(outputMessage.getExtensibilityAttributes());
                    writer.writeEndElement();
                }

                //todo handle the faults here

                writer.writeEndElement();

            }
        }


    }

    /**
     * @param desc
     * @throws IOException
     */
    protected void writeBinding(WSDLDescription desc) throws XMLStreamException, IOException {
        Map bindingsMap = desc.getBindings();
        if (!bindingsMap.isEmpty()) {
            Iterator iterator = bindingsMap.values().iterator();
            WSDLBinding binding;
            while (iterator.hasNext()) {
                binding = (WSDLBinding) iterator.next();
                writer.writeStartElement(defaultWSDLPrefix, BINDING_NAME, WSDL1_1_NAMESPACE_URI);
                writer.writeAttribute("name", binding.getName().getLocalPart());
                writer.writeAttribute("type", targetNamespacePrefix + ":" + binding.getBoundInterface().getName().getLocalPart());
                //write the extensibility elements
                handleExtensibiltyElements(binding.getExtensibilityElements());
                //write the operations

                Map bindingOps = binding.getBindingOperations();
                if (bindingOps != null && !bindingOps.isEmpty()) {
                    Iterator bindingOpsIterator = bindingOps.values().iterator();
                    while (bindingOpsIterator.hasNext()) {
                        writebindingOperation((WSDLBindingOperation) bindingOpsIterator.next());
                    }
                }
                writer.writeEndElement();
            }
        }

    }


    protected void writebindingOperation(WSDLBindingOperation bindingOp) throws XMLStreamException, IOException {
        writer.writeStartElement(defaultWSDLPrefix, BINDING_OPERATION, WSDL1_1_NAMESPACE_URI);
        writer.writeAttribute("name", bindingOp.getName().getLocalPart());
        handleExtensibiltyElements(bindingOp.getExtensibilityElements());
        //write the input
        WSDLBindingMessageReference input = bindingOp.getInput();
        if (input != null) {
            //
            writer.writeStartElement(defaultWSDLPrefix, BINDING_INPUT, WSDL1_1_NAMESPACE_URI);
            handleExtensibiltyElements(input.getExtensibilityElements());
            writer.writeEndElement();
        }

        WSDLBindingMessageReference output = bindingOp.getOutput();
        if (output != null) {
            writer.writeStartElement(defaultWSDLPrefix, BINDING_OUTPUT, WSDL1_1_NAMESPACE_URI);
            handleExtensibiltyElements(output.getExtensibilityElements());                                  
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    protected void handleExtensibilityAttributes(List extAttributeList) throws XMLStreamException {
        int extAttributeCount = extAttributeList.size();
        for (int i = 0; i < extAttributeCount; i++) {
            writeExtensibilityAttribute((WSDLExtensibilityAttribute) extAttributeList.get(i));
        }
    }
    
    protected void handleExtensibiltyElements(List extElementList) throws XMLStreamException, IOException {
        int extensibilityElementCount = extElementList.size();
        for (int i = 0; i < extensibilityElementCount; i++) {
            writeExtensibilityElement((WSDLExtensibilityElement) extElementList.get(i));
        }

    }

    //to write schema types into output straem
    private void writeSchemas(Element element) throws XMLStreamException {
        writer.flush();
        String schemaTypes = DOM2Writer.nodeToString(element);
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(new
                ByteArrayInputStream(schemaTypes.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        StAXOMBuilder staxOMBuilder = new StAXOMBuilder(fac, xmlReader);
        OMElement schemaElement = staxOMBuilder.getDocumentElement();
        schemaElement.serialize(writer);
    }
    
    protected void writeExtensibilityAttribute(WSDLExtensibilityAttribute extAttribute) throws XMLStreamException {
        QName qname = extAttribute.getKey();
        QName value = extAttribute.getValue();
        
        writer.writeAttribute(qname.getPrefix(), qname.getNamespaceURI(), qname.getLocalPart(), value.getLocalPart());
    }

    /**
     * @param extElement
     * @throws IOException
     */
    protected void writeExtensibilityElement(WSDLExtensibilityElement extElement) throws IOException, XMLStreamException {

        if (extElement instanceof Schema) {
            Element element = ((Schema) extElement).getElement();
            writeSchemas(element);
        } else if (extElement instanceof SOAPAddress) {
            writeSOAPAddressExtensibilityElement((SOAPAddress) extElement);
        } else if (extElement instanceof SOAPBinding) {
            writeSOAPBindingExtensibilityElement((SOAPBinding) extElement);
        } else if (extElement instanceof SOAPHeader) {
            writeSOAPHeaderExtensibilityElement((SOAPHeader) extElement);
        } else if (extElement instanceof SOAPOperation) {
            writeSOAPOpextensibilityElement((SOAPOperation) extElement);
        } else if (extElement instanceof SOAPBody) {
            writeSOAPBodyExtensibilityElement((SOAPBody) extElement);
        } else if (extElement instanceof PolicyExtensibilityElement) {
            writePolicyExtensibilityElement((PolicyExtensibilityElement) extElement);
        } else {
            writer.writeComment(" Unknown extensibility element" + extElement.toString());
        }

    }

    private void writeSOAPAddressExtensibilityElement(SOAPAddress address) throws XMLStreamException {
        writer.writeStartElement(soapNsPrefix, "address", WSDL1_1_SOAP_NAMESPACE_URI);
        writer.writeAttribute("location", address.getLocationURI());
        writer.writeEndElement();
    }

    protected void writeSOAPBindingExtensibilityElement(SOAPBinding soapBinding) throws XMLStreamException {
        writer.writeStartElement(soapNsPrefix, "binding", WSDL1_1_SOAP_NAMESPACE_URI);
        writer.writeAttribute("transport", soapBinding.getTransportURI());
        writer.writeAttribute("style", soapBinding.getStyle());
        writer.writeEndElement();
    }

    protected void writeSOAPBodyExtensibilityElement(SOAPBody soapBody) throws IOException, XMLStreamException {
        writer.writeStartElement(soapNsPrefix, "body", WSDL1_1_SOAP_NAMESPACE_URI);
        writer.writeAttribute("use", soapBody.getUse());
        if (soapBody.getNamespaceURI() != null) {
            writer.writeAttribute("namespace", soapBody.getNamespaceURI());
        }
        writer.writeEndElement();
    }

    protected void writeSOAPHeaderExtensibilityElement(SOAPHeader soapHeader) throws XMLStreamException {
        writer.writeStartElement(soapNsPrefix, "header", WSDL1_1_SOAP_NAMESPACE_URI);
        writer.writeAttribute("use", soapHeader.getUse());
        writer.writeEndElement();
    }


    protected void writeSOAPOpextensibilityElement(SOAPOperation soapop) throws IOException, XMLStreamException {
        writer.writeStartElement(soapNsPrefix, BINDING_OPERATION, WSDL1_1_SOAP_NAMESPACE_URI);
        writer.writeAttribute("name", soapop.getType().getLocalPart());
        writer.writeAttribute("soapAction", soapop.getSoapAction());
        writer.writeAttribute("style", soapop.getStyle());
        writer.writeEndElement();
    }
    
    protected void writePolicyExtensibilityElement(PolicyExtensibilityElement policyExtensibilityElement) throws XMLStreamException {
        StAXPolicyWriter policyWriter = (StAXPolicyWriter) PolicyFactory.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
        Object policyElement = policyExtensibilityElement.getPolicyElement();
        
        if (policyElement instanceof Policy) {
            policyWriter.writePolicy((Policy) policyElement, writer);
                        
        } else if (policyElement instanceof PolicyReference) {
            policyWriter.writePolicyReference((PolicyReference) policyElement, writer);
        }
    }

    /**
     * Since we have no proper way to represent a WSDL 1.1 message, here's a simple bean class to
     * represent it, at least for serializing.
     */
    private class WSDL11Message {
        private String messageName;
        private WSDL11MessagePart[] parts;

        public String getMessageName() {
            return messageName;
        }

        public void setMessageName(String messageName) {
            this.messageName = messageName;
        }

        public WSDL11MessagePart[] getParts() {
            return parts;
        }

        public void setParts(WSDL11MessagePart[] parts) {
            this.parts = parts;
        }


    }

    /**
     * Same as the WSDL11 message. A simple abstraction.
     */
    private class WSDL11MessagePart {
        private String name;
        private String elementName;
        private String type;
        private String prefix;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String elementName) {
            this.elementName = elementName;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
