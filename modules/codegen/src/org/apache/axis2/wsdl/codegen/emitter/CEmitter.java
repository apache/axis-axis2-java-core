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
package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.*;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;


public class CEmitter extends AxisServiceBasedMultiLanguageEmitter {
    protected static final String C_PREFIX ="axis2_";
    protected static final String C_STUB_SUFFIX = "_stub";
    protected static final String C_SKEL_SUFFIX = "";
    protected static final String C_SVC_SKEL_SUFFIX = "_svc_skeleton";
    /**
     * Emit the stub
     *
     * @throws CodeGenerationException
     */
    public void emitStub() throws CodeGenerationException {

        try {
            // write interface implementations
            writeCStubSource();

            writeCStubHeader();


        } catch (Exception e) {
            //log the error here
            e.printStackTrace();
        }
    }

    /**
     * Emit the skeltons
     *
     * @throws CodeGenerationException
     */
    public void emitSkeleton() throws CodeGenerationException {
        try {
             // write skeleton
            writeCSkelSource();
            writeCSkelHeader();

            // write a Service Skeleton for this particular service.
            writeCServiceSkeleton();

            writeServiceXml();
        }
        catch( Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Writes the Stub header.
     *
     * @throws Exception
     */
    protected void writeCStubHeader() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();

        CStubHeaderWriter writerHStub =
                new CStubHeaderWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerHStub);
    }

    /**
     * Writes the Stub source.
     *
     * @throws Exception
     */
    protected void writeCStubSource() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();

        CStubSourceWriter writerCStub =
                new CStubSourceWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerCStub);
    }

    /**
     * Writes the Skel header.
     *
     * @throws Exception
     */
    protected void writeCSkelHeader() throws Exception {

        Document skeletonModel = createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());


        CSkelHeaderWriter skeletonWriter = new CSkelHeaderWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                "src"), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    /**
     * Writes the Skel source.
     *
     * @throws Exception
     */
    protected void writeCSkelSource() throws Exception {

        Document skeletonModel = createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());

        CSkelSourceWriter skeletonWriterStub = new CSkelSourceWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                "src"), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriterStub);
    }
    /**
     * @throws Exception
     */
    protected void writeCServiceSkeleton() throws Exception {

        Document skeletonModel = createDOMDocumentForServiceSkeletonXML();
        CSvcSkeletonWriter writer = new CSvcSkeletonWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(), "src"),
                                    codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, writer);

    }

    /**
     * Write the service XML
     *
     * @throws Exception
     */
    protected void writeServiceXml() throws Exception {
        if (this.codeGenConfiguration.isGenerateDeployementDescriptor()) {

            // Write the service xml in a folder with the
            Document serviceXMLModel = createDOMDocumentForServiceXML();
            ClassWriter serviceXmlWriter =
                    new CServiceXMLWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(), "src"),
                            this.codeGenConfiguration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);
        }
    }
    /**
     * Creates the DOM tree for implementations.
     */
    protected Document createDOMDocumentForInterfaceImplementation() throws Exception {

        String localPart = makeJavaClassName(axisService.getName());
        String stubName = C_PREFIX + localPart + C_STUB_SUFFIX;
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "name", stubName, rootElement);
        addAttribute(doc, "servicename", localPart, rootElement);
        addAttribute(doc, "package", "", rootElement);

        addAttribute(doc, "namespace", axisService.getTargetNamespace(), rootElement);
        addAttribute(doc, "interfaceName", localPart, rootElement);

        /* The following block of code is same as for the
         * AxisServiceBasedMultiLanguageEmitter createDOMDocumentForInterfaceImplementation()
         */
        // add the wrap classes flag
        if (codeGenConfiguration.isPackClasses()) {
            addAttribute(doc, "wrapped", "yes", rootElement);
        }

        // add SOAP version
        addSoapVersion(doc, rootElement);

        // add the end point
        addEndpoint(doc, rootElement);

        // set the sync/async attributes
        fillSyncAttributes(doc, rootElement);

        // ###########################################################################################
        // this block of code specifically applies to the integration of databinding code into the
        // generated classes tightly (probably as inner classes)
        // ###########################################################################################
        // check for the special models in the mapper and if they are present process them
        if (mapper.isObjectMappingPresent()) {

            // add an attribute to the root element showing that the writing has been skipped
            addAttribute(doc, "skip-write", "yes", rootElement);

            // process the mapper objects
            processModelObjects(mapper.getAllMappedObjects(), rootElement, doc);
        }

        // #############################################################################################

        // load the operations
        loadOperations(doc, rootElement, null);

        // add the databind supporters. Now the databind supporters are completly contained inside
        // the stubs implementation and not visible outside
        rootElement.appendChild(createDOMElementforDatabinders(doc));

        Object stubMethods;

        //if some extension has added the stub methods property, add them to the
        //main document
        if ((stubMethods = codeGenConfiguration.getProperty("stubMethods")) != null) {
            rootElement.appendChild(doc.importNode((Element) stubMethods, true));
        }

        //add another element to have the unique list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        /////////////////////////////////////////////////////
        //System.out.println(DOM2Writer.nodeToString(rootElement));
        /////////////////////////////////////////////////////


        doc.appendChild(rootElement);
        return doc;
    }

    protected Document createDOMDocumentForSkeleton(boolean isSkeletonInterface) {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String localPart = makeJavaClassName(axisService.getName());
        String skelName = C_PREFIX + localPart + C_SKEL_SUFFIX;

        // only the name is used
        addAttribute(doc, "name", skelName , rootElement);
        addAttribute(doc, "package", "", rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    protected Document createDOMDocumentForServiceSkeletonXML() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String localPart = makeJavaClassName(axisService.getName());
        String svcSkelName = C_PREFIX + localPart + C_SVC_SKEL_SUFFIX;
        String skelName = C_PREFIX + localPart + C_SKEL_SUFFIX;

        // only the name is used
        addAttribute(doc, "name", svcSkelName , rootElement);
        addAttribute(doc, "svcname", skelName , rootElement);
        addAttribute(doc, "package", "", rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);

        // add SOAP version
        addSoapVersion(doc, rootElement);
        
        //attach a list of faults
        rootElement.appendChild(getUniqueListofFaults(doc));

        doc.appendChild(rootElement);
        return doc;

    }

    /**
     * @param word
     * @return Returns character removed string.
     */
    protected String makeJavaClassName(String word) {
        //currently avoid only java key words
        if (JavaUtils.isJavaKeyword(word)) {
            return JavaUtils.makeNonJavaKeyword(word);
        }
        return word;
    }

    /**
     * @param doc
     * @param operation
     * @param param
     */
    protected void  addOursAttri (Document doc, AxisOperation operation, Element param ){

        Map typeMap =  CTypeInfo.getTypeMap();
        Iterator it= typeMap.keySet().iterator();

        AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        QName typeMapping = inputMessage.getElementQName();

        String paramType = this.mapper.getTypeMappingName(inputMessage.getElementQName());
        if ( doc ==null || paramType==null || param==null){
            return;
        }
        addAttribute(doc, "caps-type", paramType.toUpperCase(), param);

        boolean isOurs = true;
        while (it.hasNext()){
            if (it.next().equals(typeMapping)){
                isOurs = false;
                break;
            }
        }

        if ( isOurs && !paramType.equals("") && !paramType.equals("void") &&
                !paramType.equals("org.apache.axiom.om.OMElement") ){
            addAttribute(doc, "ours", "yes", param);
        }
    }

    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    protected Element[] getInputParamElement(Document doc, AxisOperation operation) {
        Element[] param = super.getInputParamElement( doc, operation);
        for (int i = 0; i < param.length; i++) {
           addOursAttri ( doc, operation, param[i]);
        }
        return param;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    protected Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element param = super.getOutputParamElement( doc, operation);
        addOursAttri ( doc, operation, param);
        return param;
    }
}

