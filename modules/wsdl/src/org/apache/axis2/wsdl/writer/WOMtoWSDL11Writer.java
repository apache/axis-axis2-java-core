package org.apache.axis2.wsdl.writer;

import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.wsdl.*;
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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
//            Writer writer = new OutputStreamWriter(out,encoding);
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);

            writeStartDescripton(wsdlDescription, writer);
            //find the SOAPNs
            findSOAPNsPrefix(wsdlDescription);
            //write the imports
            writeImports(wsdlDescription);
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
     * Find the SOAPns prefix
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
     * Write the messages. This is somewhat tricky when the message names and parts
     * have to be guessed
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
     * message name is the localpart of the QName suffixed by MESSAGE_NAME_SUFFIX
     * partname is just 'part1' (wouldn't matter!)
     * element reference is again the localpart of the QName but prefixed with the target namespaces prefix
     */

    private void populateMessageSymbol(QName reference) {
        if (messageMap.containsKey(reference)) {
            //just return. The message is already there
            return;
        } else {
            //create a part with name part 1 and element ref to the QName value
            //these references need to be prefixed according to the correct target namespaces
            //of the schemas
            WSDL11MessagePart part = new WSDL11MessagePart();
            part.setName("part1");
            part.setElementName(reference.getLocalPart());  //todo prefix needs to be here!!!!
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
     * Write porttypes
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
     */
    protected void writePortTypes(WSDLDescription desc) throws XMLStreamException {
        Map interfaceMap = desc.getWsdlInterfaces();
        if (!interfaceMap.isEmpty()) {
            Iterator interfaceIterator = interfaceMap.values().iterator();
            WSDLInterface wsdlInterface;
            while (interfaceIterator.hasNext()) {
                wsdlInterface = (WSDLInterface) interfaceIterator.next();
                writer.writeStartElement(defaultWSDLPrefix, PORTTYPE_NAME, WSDL1_1_NAMESPACE_URI);
                writer.writeAttribute("name",
                        wsdlInterface.getName() == null ? "" : wsdlInterface.getName().getLocalPart());

                //write the operations
                writePorttypeOperations(wsdlInterface);

                writer.writeEndElement();

            }


        }
    }

    /**
     * Write the operation
     *
     * @param wsdlInterface
     */
    protected void writePorttypeOperations(WSDLInterface wsdlInterface) throws XMLStreamException {
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
                WSDL11Message message;
                MessageReference inputMessage = operation.getInputMessage();
                if (inputMessage != null) {
                    message = (WSDL11Message) messageMap.get(inputMessage.getElementQName());
                    writer.writeStartElement(defaultWSDLPrefix,INPUT_NAME, WSDL1_1_NAMESPACE_URI);
                    writer.writeAttribute("message", targetNamespacePrefix + ":" + message.getMessageName());
                    writer.writeEndElement();
                }

                //write the outputs
                MessageReference outputMessage = operation.getOutputMessage();
                if (outputMessage != null) {
                    message = (WSDL11Message) messageMap.get(outputMessage.getElementQName());
                    writer.writeStartElement(defaultWSDLPrefix, OUTPUT_NAME, WSDL1_1_NAMESPACE_URI);
                    writer.writeAttribute("message", targetNamespacePrefix + ":" + message.getMessageName());
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
            writer.writeStartElement(defaultWSDLPrefix, BINDING_OUTPUT, WSDL1_1_NAMESPACE_URI);
            handleExtensibiltyElements(input.getExtensibilityElements());
            writer.writeEndElement();
        }

        WSDLBindingMessageReference output = bindingOp.getInput();
        if (output != null) {
            writer.writeStartElement(defaultWSDLPrefix, BINDING_OUTPUT, WSDL1_1_NAMESPACE_URI);
            handleExtensibiltyElements(output.getExtensibilityElements());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    protected void handleExtensibiltyElements(List extElementList) throws XMLStreamException, IOException {
        int extensibilityElementCount = extElementList.size();
        for (int i = 0; i < extensibilityElementCount; i++) {
            writeExtensibiltyElement((WSDLExtensibilityElement) extElementList.get(i));
        }

    }

    /**
     * @param extElement
     * @throws IOException
     */
    protected void writeExtensibiltyElement(WSDLExtensibilityElement extElement) throws IOException, XMLStreamException {
        if (extElement instanceof Schema) {
            Element element = ((Schema) extElement).getElement();
            if (element.getNodeValue() != null) {
                writer.writeStartElement(element.toString());
                writer.writeEndElement();
            }
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
        } else {
            writer.writeComment(" Unknown extensibility element" + extElement.toString());
        }

    }

    private void writeSOAPAddressExtensibilityElement(SOAPAddress address) throws IOException, XMLStreamException {
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
        writer.writeAttribute("soapaction", soapop.getSoapAction());
        writer.writeEndElement();
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
     * Samething as the WSDL11 message. A simple abstraction
     */
    private class WSDL11MessagePart {
        private String name;
        private String elementName;
        private String type;

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
