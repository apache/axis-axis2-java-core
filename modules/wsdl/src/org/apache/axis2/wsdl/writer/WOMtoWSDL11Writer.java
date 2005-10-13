package org.apache.axis2.wsdl.writer;

import org.apache.axis2.wsdl.WSDLVersionWrapper;

import org.apache.wsdl.*;
import org.apache.wsdl.extensions.*;

import javax.wsdl.xml.WSDLWriter;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
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

public class WOMtoWSDL11Writer implements WOMWriter{

    private String encoding = "UTF-8"; //default encoding is UTF-8
    private String defaultWSDLPrefix="wsdl11";
    private String targetNamespacePrefix="tns";
    private static final String WSDL1_1_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/";
    private static final String WSDL1_1_SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final String DEFINITION_NAME = "definitions";
    private static final String IMPORT_NAME = "import";
    private static final String TYPES_NAME = "types";
    private static final String PORTTYPE_NAME = "portType";

    private static final String MESSAGE_NAME_SUFFIX = "Message";


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


    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @see WOMWriter#writeWOM(org.apache.axis2.wsdl.WSDLVersionWrapper, java.io.OutputStream)
     * @param wsdlWrapper
     * @param out
     */
    public void writeWOM(WSDLVersionWrapper wsdlWrapper, OutputStream out) throws WriterException{
        if (wsdlWrapper==null){
            throw new WriterException("wsdl Wrapper cannot be null");
        }
        //the wsdl definition is present. then we can ditectly utilize the wsdl writer.
        if (wsdlWrapper.getDefinition()!=null){
            try {
                WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
                writer.writeWSDL(wsdlWrapper.getDefinition(),out);
            } catch (WSDLException e) {
                throw new WriterException(e);
            }
            //wsdl description is present but not the definition. So start writing
        }else if (wsdlWrapper.getDescription()!=null){
            writeWOM(wsdlWrapper.getDescription(),out);
        }else{
            throw new WriterException(" WSDL wrapper is empty!");
        }
    }

    /**
     * @see WOMWriter#writeWOM(org.apache.wsdl.WSDLDescription, java.io.OutputStream)
     * @param wsdlDescription
     * @param out
     * @throws WriterException
     */
    public void writeWOM(WSDLDescription wsdlDescription, OutputStream out) throws WriterException {
        try {
            //create a writer from the stream
            Writer writer = new OutputStreamWriter(out,encoding);
            writeStartDescripton(wsdlDescription,writer);
            //find the SOAPNs
            findSOAPNsPrefix(wsdlDescription);
            //write the imports
            writeImports(wsdlDescription,writer);
            //write  the types
            writeTypes(wsdlDescription,writer);
            //write the messages
            writeMessages(wsdlDescription,writer);
            //write the porttype
            writePorttypes(wsdlDescription,writer);
            //write the binding
            writeBinding(wsdlDescription,writer);
            //write the service
            writeService(wsdlDescription,writer);
            //close definition
            writeEndDescripton(writer);
            writer.flush();

        } catch (UnsupportedEncodingException e) {
            throw new WriterException("wrong encoding!",e);
        } catch (IOException e) {
            throw new WriterException("Error writing to the stream!",e);
        }

    }

    /**
     *
     * @param wsdlDescription
     * @param writer
     * @throws IOException
     */
    private void writeService(WSDLDescription wsdlDescription, Writer writer) throws IOException{
        Map serviceMap = wsdlDescription.getServices();
        if (serviceMap!=null && !serviceMap.isEmpty()){
            Iterator serviceIterator = serviceMap.values().iterator();
            WSDLService service;
            while (serviceIterator.hasNext()) {
                service =  (WSDLService)serviceIterator.next();
                WriterUtil.writeStartElement("service",defaultWSDLPrefix,writer);
                WriterUtil.writeAttribute("name",service.getName().getLocalPart(),writer);
                WriterUtil.writeCloseStartElement(writer);
                //wrtie the porttypes
                Map endPointMap = service.getEndpoints();
                if (endPointMap!=null && !endPointMap.isEmpty()){
                    Iterator it = endPointMap.values().iterator();
                    while (it.hasNext()) {
                        writePort((WSDLEndpoint)it.next(),writer);
                    }
                }
                WriterUtil.writeEndElement("service",defaultWSDLPrefix,writer);
            }
        }
    }

    /**
     *
     * @param endpoint
     * @param outWriter
     */
    private void writePort(WSDLEndpoint endpoint,Writer outWriter) throws IOException{
        WriterUtil.writeStartElement("port",defaultWSDLPrefix,outWriter);
        WriterUtil.writeAttribute("name",endpoint.getName().getLocalPart(),outWriter);
        QName name = endpoint.getBinding().getName();
        WriterUtil.writeAttribute("binding",name.getPrefix()+":"+name.getLocalPart(),outWriter);
        WriterUtil.writeCloseStartElement(outWriter);

        handleExtensibiltyElements(endpoint.getExtensibilityElements(),outWriter);

        WriterUtil.writeEndElement("port",defaultWSDLPrefix,outWriter);
    }
    /**
     * Find the SOAPns prefix
     * @param wsdlDescription
     */
    private void findSOAPNsPrefix(WSDLDescription wsdlDescription) {
        Map map = wsdlDescription.getNamespaces();
        Iterator nsIterator = map.values().iterator();
        Iterator keyIterator = map.keySet().iterator();
        String key;
        while (nsIterator.hasNext()) {
            key =  keyIterator.next().toString();
            if (WSDL1_1_SOAP_NAMESPACE_URI.equals(nsIterator.next())){
                this.soapNsPrefix = key;
                break;
            }
        }

    }

    /**
     *
     * @param desc
     * @param outWriter
     * @throws WriterException
     * @throws IOException
     */
    protected void writeStartDescripton(WSDLDescription desc,Writer outWriter) throws  IOException {
        //always prefix the elements with wsdl1.1 prefix
        WriterUtil.writeStartElement(DEFINITION_NAME,defaultWSDLPrefix,outWriter);
        //write the name
        QName wsdl1DefinitionName = desc.getWSDL1DefinitionName();
        if (wsdl1DefinitionName!=null){
            WriterUtil.writeAttribute("name",wsdl1DefinitionName.getLocalPart(),outWriter);
        }
        //loop through the namespaces
        String targetNameSpace = desc.getTargetNameSpace();

        Map nsMap = desc.getNamespaces();
        if (nsMap!=null && !nsMap.isEmpty()){
            Iterator nameSpaces = nsMap.keySet().iterator();
            String nsPrefix;
            String nsURI;
            while (nameSpaces.hasNext()) {
                nsPrefix =  (String)nameSpaces.next();
                nsURI = nsMap.get(nsPrefix).toString();
                //make sure not to write the namespace URI of the WSDL 1.1 namespace
                if (!WSDL1_1_NAMESPACE_URI.equals(nsURI) &&
                        !targetNameSpace.equals(nsURI)){
                    WriterUtil.writeNamespace(nsPrefix,
                            nsURI,
                            outWriter);
                }

            }
        }
        //write the default WSDL namespace
        WriterUtil.writeNamespace(defaultWSDLPrefix,WSDL1_1_NAMESPACE_URI,outWriter);
        //write the targetnamespace with our own prefix
        WriterUtil.writeNamespace(targetNamespacePrefix,targetNameSpace,outWriter);
        //write the targetNamespace
        WriterUtil.writeAttribute("targetNamespace", desc.getTargetNameSpace(),outWriter);
        WriterUtil.writeCloseStartElement(outWriter);
    }

    /**
     * Write the messages. This is somewhat tricky when the message names and parts
     * have to be guessed
     * @param desc
     * @param outWriter
     */
    protected void writeMessages(WSDLDescription desc,Writer outWriter) throws IOException{

        //first look for a metadata bag for this particular description
        Map mBag = desc.getMetadataBag();


        if (mBag==null || mBag.isEmpty()){
            //No metadata! So do some guess work!
            Map interfaceMap = desc.getWsdlInterfaces();
            if (!interfaceMap.isEmpty()){
                Iterator interfaceIterator = interfaceMap.values().iterator();
                WSDLInterface wsdlInterface;
                while (interfaceIterator.hasNext()) {
                    wsdlInterface=  (WSDLInterface)interfaceIterator.next();
                    Map opMap=wsdlInterface.getOperations();
                    if (!opMap.isEmpty()){
                        Iterator opIterator = opMap.values().iterator();
                        WSDLOperation operation;
                        while (opIterator.hasNext()) {
                            operation = (WSDLOperation)opIterator.next();
                            //populate the symbol table of Messages
                            QName inputReference = operation.getInputMessage()==null?null:operation.getInputMessage().getElement();
                            if (inputReference!=null){
                                populateMessageSymbol(inputReference);
                            }

                            QName outputReference = operation.getOutputMessage()==null?null:operation.getOutputMessage().getElement();
                            if (outputReference!=null){
                                populateMessageSymbol(outputReference);
                            }

                            //todo handle the faults here

                        }
                    }

                }

                // Now we are done with populating the message symbols. write them down
                Iterator messages =  messageMap.values().iterator();
                while (messages.hasNext()) {
                    WSDL11Message msg =  (WSDL11Message)messages.next();
                    WriterUtil.writeStartElement("message",defaultWSDLPrefix,outWriter);
                    WriterUtil.writeAttribute("name",msg.getMessageName(),outWriter);
                    WriterUtil.writeCloseStartElement(outWriter);

                    //write the parts
                    WSDL11MessagePart[] parts = msg.getParts();
                    WSDL11MessagePart part;
                    for (int i = 0; i < parts.length; i++) {
                        part = parts[i];
                        WriterUtil.writeStartElement("part",defaultWSDLPrefix,outWriter);
                        WriterUtil.writeAttribute("name",part.getName(),outWriter);
                        String elementName = part.getElementName();
                        if (elementName!=null){
                            WriterUtil.writeAttribute("element",elementName,outWriter);
                        }
                        //put the type also here. For the time being let this be like it
                        WriterUtil.writeCompactEndElement(outWriter);

                    }

                    WriterUtil.writeEndElement("message",defaultWSDLPrefix,outWriter);

                }



            }else{
                //use the metadata to formulate the names and stuff
                //todo fill this!!!!

            }
        }


    }

    /**
     *  Our simple rule in the 'guessing game' for the message and it's parts.
     *  message name is the localpart of the QName suffixed by MESSAGE_NAME_SUFFIX
     *  partname is just 'part1' (wouldn't matter!)
     *  element reference is again the localpart of the QName but prefixed with the target namespaces prefix
     *
     */

    private void populateMessageSymbol(QName reference){
        if (messageMap.containsKey(reference)){
            //just return. The message is already there
            return;
        }else{
            //create a part with name part 1 and element ref to the QName value
            //these references need to be prefixed according to the correct target namespaces
            //of the schemas
            WSDL11MessagePart part = new WSDL11MessagePart();
            part.setName("part1");
            part.setElementName(reference.getLocalPart());  //todo prefix needs to be here!!!!
            WSDL11Message message = new WSDL11Message();
            message.setMessageName(reference.getLocalPart()+MESSAGE_NAME_SUFFIX);
            message.setParts(new WSDL11MessagePart[]{part});

            //fill this in the message symbol map
            messageMap.put(reference,message);
        }
    }
    /**
     *
     * @param outWriter
     * @throws WriterException
     * @throws IOException
     */
    protected void writeEndDescripton(Writer outWriter) throws IOException {
        WriterUtil.writeEndElement(DEFINITION_NAME,defaultWSDLPrefix,outWriter);
    }

    /**
     *
     * @param desc
     * @param outWriter
     * @throws IOException
     */
    protected void writeImports(WSDLDescription desc,Writer outWriter) throws IOException{
        //todo 1.1 pump does not populate the imports
        //get the imports
        List imports = desc.getImports();
        int importCount = imports.size();
        WSDLImport singleImport;
        for (int i = 0; i < importCount; i++) {
            singleImport= (WSDLImport)imports.get(i);
            WriterUtil.writeStartElement(IMPORT_NAME,defaultWSDLPrefix,outWriter);
            WriterUtil.writeAttribute("namespace",singleImport.getNamespace(),outWriter);
            WriterUtil.writeAttribute("location",singleImport.getLocation(),outWriter);
            WriterUtil.writeCompactEndElement(outWriter);
        }


    }

    /**
     * Write porttypes
     * @param desc
     * @param outWriter
     * @throws IOException
     */
    protected void writeTypes(WSDLDescription desc,Writer outWriter) throws IOException{
        //get the imports
        WSDLTypes types = desc.getTypes();
        if (types!=null){
            WriterUtil.writeStartElement(TYPES_NAME,defaultWSDLPrefix,outWriter);
            WriterUtil.writeCloseStartElement(outWriter);
            handleExtensibiltyElements(types.getExtensibilityElements(),outWriter);
            WriterUtil.writeEndElement(TYPES_NAME,defaultWSDLPrefix,outWriter);
        }
    }

    /**
     *
     * @param desc
     * @param outWriter
     */
    protected void writePorttypes(WSDLDescription desc,Writer outWriter) throws IOException{
        Map interfaceMap = desc.getWsdlInterfaces();
        if (!interfaceMap.isEmpty()){
            Iterator interfaceIterator = interfaceMap.values().iterator();
            WSDLInterface wsdlInterface;
            while (interfaceIterator.hasNext()) {
                wsdlInterface=  (WSDLInterface)interfaceIterator.next();
                WriterUtil.writeStartElement(PORTTYPE_NAME, defaultWSDLPrefix,outWriter);
                WriterUtil.writeAttribute("name",
                        wsdlInterface.getName()==null?"":wsdlInterface.getName().getLocalPart(),
                        outWriter);
                WriterUtil.writeCloseStartElement(outWriter);

                //write the operations
                writePorttypeOperations(wsdlInterface,outWriter);

                WriterUtil.writeEndElement(PORTTYPE_NAME,defaultWSDLPrefix,outWriter);

            }


        }
    }

    /**
     * Write the operation
     * @param wsdlInterface
     * @param outWriter
     */
    protected void writePorttypeOperations(WSDLInterface wsdlInterface,Writer outWriter) throws IOException{
        Map operationsMap = wsdlInterface.getOperations();
        if (!operationsMap.isEmpty()){
            Iterator opIterator = operationsMap.values().iterator();
            WSDLOperation operation;
            while (opIterator.hasNext()) {
                operation = (WSDLOperation)opIterator.next();
                WriterUtil.writeStartElement(OPERATION_NAME,defaultWSDLPrefix,outWriter);
                WriterUtil.writeAttribute("name",operation.getName()==null?"":operation.getName().getLocalPart(),outWriter);
                WriterUtil.writeCloseStartElement(outWriter);

                //write the inputs
                WSDL11Message message;
                MessageReference inputMessage = operation.getInputMessage();
                if (inputMessage!=null){
                    message  =(WSDL11Message) messageMap.get(inputMessage.getElement());
                    WriterUtil.writeStartElement(INPUT_NAME,defaultWSDLPrefix,outWriter);
                    WriterUtil.writeAttribute("message",targetNamespacePrefix+":"+message.getMessageName(),outWriter);
                    WriterUtil.writeCompactEndElement(outWriter);
                }

                //write the outputs
                MessageReference outputMessage = operation.getOutputMessage();
                if (outputMessage!=null) {
                    message  =(WSDL11Message) messageMap.get(outputMessage.getElement());
                    WriterUtil.writeStartElement(OUTPUT_NAME,defaultWSDLPrefix,outWriter);
                    WriterUtil.writeAttribute("message",targetNamespacePrefix+":"+message.getMessageName(),outWriter);
                    WriterUtil.writeCompactEndElement(outWriter);
                }

                //todo handle the faults here

                WriterUtil.writeEndElement(OPERATION_NAME,defaultWSDLPrefix,outWriter);

            }
        }


    }

    /**
     *
     * @param desc
     * @param outWriter
     * @throws IOException
     */
    protected void writeBinding(WSDLDescription desc,Writer outWriter) throws IOException{
        Map bindingsMap = desc.getBindings();
        if (!bindingsMap.isEmpty()) {
            Iterator iterator = bindingsMap.values().iterator();
            WSDLBinding binding;
            while (iterator.hasNext()) {
                binding =  (WSDLBinding)iterator.next();
                WriterUtil.writeStartElement(BINDING_NAME, defaultWSDLPrefix,outWriter);
                WriterUtil.writeAttribute("name",binding.getName().getLocalPart(),outWriter);
                WriterUtil.writeAttribute("type",targetNamespacePrefix +":"+ binding.getBoundInterface().getName().getLocalPart(),outWriter);
                WriterUtil.writeCloseStartElement(outWriter);
                //write the extensibility elements
                handleExtensibiltyElements(binding.getExtensibilityElements(),outWriter);
                //write the operations

                Map bindingOps = binding.getBindingOperations();
                if (bindingOps!=null && !bindingOps.isEmpty()){
                    Iterator bindingOpsIterator = bindingOps.values().iterator();
                    while (bindingOpsIterator.hasNext()) {
                        writebindingOperation((WSDLBindingOperation)bindingOpsIterator.next(),outWriter);
                    }
                }



                WriterUtil.writeEndElement(BINDING_NAME, defaultWSDLPrefix,outWriter);


            }
        }

    }


    protected void writebindingOperation(WSDLBindingOperation bindingOp,Writer outWriter) throws IOException{
        WriterUtil.writeStartElement(BINDING_OPERATION,defaultWSDLPrefix,outWriter);
        WriterUtil.writeAttribute("name",bindingOp.getName().getLocalPart(),outWriter);
        WriterUtil.writeCloseStartElement(outWriter);
        handleExtensibiltyElements(bindingOp.getExtensibilityElements(),outWriter);
        //write the input
        WSDLBindingMessageReference input = bindingOp.getInput();
        if (input!=null){
            WriterUtil.writeStartElement(BINDING_INPUT,defaultWSDLPrefix,outWriter);
            WriterUtil.writeCloseStartElement(outWriter);
            handleExtensibiltyElements(input.getExtensibilityElements(),outWriter);
            WriterUtil.writeEndElement(BINDING_INPUT,defaultWSDLPrefix,outWriter);
        }

        WSDLBindingMessageReference output = bindingOp.getInput();
        if (output!=null){
            WriterUtil.writeStartElement(BINDING_OUTPUT,defaultWSDLPrefix,outWriter);
            WriterUtil.writeCloseStartElement(outWriter);
            handleExtensibiltyElements(output.getExtensibilityElements(),outWriter);
            WriterUtil.writeEndElement(BINDING_OUTPUT,defaultWSDLPrefix,outWriter);
        }
        WriterUtil.writeEndElement(BINDING_OPERATION,defaultWSDLPrefix,outWriter);
    }

    protected void handleExtensibiltyElements(List extElementList,Writer outWriter) throws IOException{
        int extensibilityElementCount = extElementList.size();
        for (int i = 0; i < extensibilityElementCount; i++) {
            writeExtensibiltyElement((WSDLExtensibilityElement)extElementList.get(i),outWriter);
        }

    }

    /**
     *
     * @param extElement
     * @param outWriter
     * @throws IOException
     */
    protected void writeExtensibiltyElement(WSDLExtensibilityElement extElement,Writer outWriter) throws IOException{
        if (extElement instanceof Schema){
            outWriter.write(((Schema)extElement).getElement().toString());
        }else if(extElement instanceof SOAPAddress ){
            writeSOAPAddressExtensibilityElement((SOAPAddress)extElement,outWriter);
        }else if(extElement instanceof SOAPBinding ){
            writeSOAPBindingExtensibilityElement((SOAPBinding)extElement,outWriter);
        }else if(extElement instanceof SOAPHeader ){
            writeSOAPHeaderExtensibilityElement((SOAPHeader)extElement,outWriter);
        } else if(extElement instanceof SOAPOperation ){
            writeSOAPOpextensibilityElement((SOAPOperation)extElement,outWriter);
        }else if(extElement instanceof SOAPBody ){
            writeSOAPBodyExtensibilityElement((SOAPBody)extElement,outWriter);
        }else{
            WriterUtil.writeComment(" Unknown extensibility element" + extElement.toString(),outWriter);
        }

    }

    private void writeSOAPAddressExtensibilityElement(SOAPAddress address,Writer outWriter) throws IOException{
         WriterUtil.writeStartElement("address",this.soapNsPrefix,outWriter);
        WriterUtil.writeAttribute("location",address.getLocationURI(),outWriter);
      //  WriterUtil.writeAttribute("style",soapBinding.getStyle(),outWriter);
        WriterUtil.writeCompactEndElement(outWriter);
    }

    protected void writeSOAPBindingExtensibilityElement(SOAPBinding soapBinding,Writer outWriter)throws IOException{
        WriterUtil.writeStartElement("binding",this.soapNsPrefix,outWriter);
        WriterUtil.writeAttribute("transport",soapBinding.getTransportURI(),outWriter);
        WriterUtil.writeAttribute("style",soapBinding.getStyle(),outWriter);
        WriterUtil.writeCompactEndElement(outWriter);
    }

    protected void writeSOAPBodyExtensibilityElement(SOAPBody soapBody,Writer outWriter)throws IOException{
        WriterUtil.writeStartElement("body",this.soapNsPrefix,outWriter);
        WriterUtil.writeAttribute("use",soapBody.getUse(),outWriter);
        WriterUtil.writeCompactEndElement(outWriter);
    }

    protected void writeSOAPHeaderExtensibilityElement(SOAPHeader soapHeader,Writer outWriter)throws IOException{
        WriterUtil.writeStartElement("header",this.soapNsPrefix,outWriter);
        WriterUtil.writeAttribute("use",soapHeader.getUse(),outWriter);
        WriterUtil.writeCompactEndElement(outWriter);
    }


    protected void writeSOAPOpextensibilityElement(SOAPOperation soapop,Writer outWriter)throws IOException{
        WriterUtil.writeStartElement(BINDING_OPERATION,this.soapNsPrefix,outWriter);
        WriterUtil.writeAttribute("name",soapop.getType().getLocalPart(),outWriter);
        WriterUtil.writeAttribute("soapaction",soapop.getSoapAction(),outWriter);
        WriterUtil.writeCompactEndElement(outWriter);
    }

    /**
     * Since we have no proper way to represent a WSDL 1.1 message, here's a simple bean class to
     * represent it, at least for serializing.
     *
     */
    private class WSDL11Message{
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
    private class WSDL11MessagePart{
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
