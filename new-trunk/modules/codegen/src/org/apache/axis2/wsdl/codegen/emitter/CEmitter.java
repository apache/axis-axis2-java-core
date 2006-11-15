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
import org.apache.axis2.util.Utils;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.AxisMessage;
import org.apache.neethi.Policy;
import org.apache.axiom.om.OMFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import java.io.File;

public class CEmitter extends AxisServiceBasedMultiLanguageEmitter {
    protected static final String C_STUB_PREFIX = "axis2_stub_";
    protected static final String C_SKEL_PREFIX = "axis2_skel_";
    protected static final String C_SVC_SKEL_PREFIX = "axis2_svc_skel_";
    protected static final String C_STUB_SUFFIX = "";
    protected static final String C_SKEL_SUFFIX = "";
    protected static final String C_SVC_SKEL_SUFFIX = "";

    protected static final String JAVA_DEFAULT_TYPE = "org.apache.axiom.om.OMElement";
    protected static final String C_DEFAULT_TYPE = "axiom_node_t*";

    protected static final String C_OUR_TYPE_PREFIX = "axis2_";
    protected static final String C_OUR_TYPE_SUFFIX = "_t*";
    /**
     * Emit the stub
     *
     * @throws CodeGenerationException
     */
    public void emitStub() throws CodeGenerationException {

        try {
            // write interface implementations
            writeCStub();


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
            writeCSkel();

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
     * Writes the Stub.
     *
     * @throws Exception
     */
    protected void writeCStub() throws Exception {

        // first check for the policies in this service and write them
        Document interfaceImplModel = createDOMDocumentForInterfaceImplementation();

        CStubHeaderWriter writerHStub =
                new CStubHeaderWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                        codeGenConfiguration.getSourceLocation()),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerHStub);


        CStubSourceWriter writerCStub =
                new CStubSourceWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                        codeGenConfiguration.getSourceLocation()),
                        codeGenConfiguration.getOutputLanguage());

        writeClass(interfaceImplModel, writerCStub);
    }



    /**
     * Writes the Skel.
     *
     * @throws Exception
     */
    protected void writeCSkel() throws Exception {

        Document skeletonModel = createDOMDocumentForSkeleton(codeGenConfiguration.isServerSideInterface());


        CSkelHeaderWriter skeletonWriter = new CSkelHeaderWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                codeGenConfiguration.getSourceLocation()), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);

        CSkelSourceWriter skeletonWriterStub = new CSkelSourceWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                codeGenConfiguration.getSourceLocation()), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriterStub);
    }

    /**
     * @throws Exception
     */
    protected void writeCServiceSkeleton() throws Exception {

        Document skeletonModel = createDOMDocumentForServiceSkeletonXML();
        CSvcSkeletonWriter writer = new CSvcSkeletonWriter(getOutputDirectory(codeGenConfiguration.getOutputLocation(),
                codeGenConfiguration.getSourceLocation()),
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
                    new CServiceXMLWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                            codeGenConfiguration.getResourceLocation()),
                            this.codeGenConfiguration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);
        }
    }
    /**
     * Creates the DOM tree for implementations.
     */
    protected Document createDOMDocumentForInterfaceImplementation() throws Exception {

        String serviceName = axisService.getName();
        String serviceTns = axisService.getTargetNamespace();
        String serviceCName = makeCClassName(axisService.getName());
        String stubName = C_STUB_PREFIX + serviceCName + C_STUB_SUFFIX;
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("class");

        addAttribute(doc, "name", stubName, rootElement);
        addAttribute( doc,"prefix", stubName, rootElement); //prefix to be used by the functions
        addAttribute(doc, "qname", serviceName + "|" + serviceTns, rootElement);
        addAttribute(doc, "servicename", serviceCName, rootElement);
        addAttribute(doc, "package", "", rootElement);

        addAttribute(doc, "namespace", serviceTns, rootElement);
        addAttribute(doc, "interfaceName", serviceCName, rootElement);

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
        rootElement.appendChild(createDOMElementforDatabinders(doc,false));

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

        String serviceCName = makeCClassName(axisService.getName());
        String skelName = C_SKEL_PREFIX + serviceCName + C_SKEL_SUFFIX;

        // only the name is used
        addAttribute(doc, "name", skelName , rootElement);
        addAttribute(doc, "package", "", rootElement);
        String serviceName = axisService.getName();
        String serviceTns = axisService.getTargetNamespace();
        addAttribute( doc,"prefix", skelName, rootElement); //prefix to be used by the functions
        addAttribute(doc, "qname", serviceName + "|" + serviceTns, rootElement);


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

        String localPart = makeCClassName(axisService.getName());
        String svcSkelName = C_SVC_SKEL_PREFIX + localPart + C_SVC_SKEL_SUFFIX;
        String skelName = C_SKEL_PREFIX + localPart + C_SKEL_SUFFIX;

        // only the name is used
        addAttribute(doc, "name", svcSkelName , rootElement);
        addAttribute(doc, "prefix", svcSkelName , rootElement); //prefix to be used by the functions
        String serviceName = axisService.getName();
        String serviceTns = axisService.getTargetNamespace();
        addAttribute(doc, "qname", serviceName + "|" + serviceTns, rootElement);

        addAttribute(doc, "svcname", skelName , rootElement);
        addAttribute(doc, "svcop_prefix", skelName , rootElement);
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
    protected String makeCClassName(String word) {
        //currently avoid only java key words and service names with '.' characters

        if (JavaUtils.isJavaKeyword(word)) {
            return JavaUtils.makeNonJavaKeyword(word);
        }
        return word.replace('.', '_');
    }


    /**
     * Loads the operations
     * @param doc
     * @param rootElement
     * @param mep
     * @return operations found
     */
    protected boolean loadOperations(Document doc, Element rootElement, String mep) {
        Element methodElement;
        String portTypeName = makeJavaClassName(axisService.getName());

        Iterator operations = axisService.getOperations();
        boolean opsFound = false;
        while (operations.hasNext()) {
            AxisOperation axisOperation = (AxisOperation) operations.next();

            // populate info holder with mep information. This will used in determining which
            // message receiver to use, etc.,

            String messageExchangePattern = axisOperation.getMessageExchangePattern();
            if (infoHolder.get(messageExchangePattern) == null) {
                infoHolder.put(messageExchangePattern, Boolean.TRUE);
            }

            if (mep == null) {

                opsFound = true;

                List soapHeaderInputParameterList = new ArrayList();
                List soapHeaderOutputParameterList = new ArrayList();

                methodElement = doc.createElement("method");

                String localPart = axisOperation.getName().getLocalPart();
                String opCName = makeCClassName(localPart);
                String opNS = axisOperation.getName().getNamespaceURI();

                addAttribute(doc, "name", opCName, methodElement);
                addAttribute(doc, "localpart", localPart, methodElement);
                addAttribute(doc, "qname", localPart+ "|"+ opNS, methodElement);

                addAttribute(doc, "namespace", opNS, methodElement);
                String style = axisOperation.getStyle();
                addAttribute(doc, "style", style, methodElement);
                addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                        methodElement);


                addAttribute(doc, "mep", Utils.getAxisSpecifMEPConstant(axisOperation.getMessageExchangePattern()) + "", methodElement);
                addAttribute(doc, "mepURI", axisOperation.getMessageExchangePattern(), methodElement);


                addSOAPAction(doc, methodElement, axisOperation);
                //add header ops for input
                addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                //add header ops for output
                addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                PolicyInclude policyInclude = axisOperation.getPolicyInclude();
                Policy policy = policyInclude.getPolicy();
                if (policy != null) {
                    try {
                        addAttribute(doc, "policy", PolicyUtil.policyComponentToString(policy), methodElement);
                    } catch (Exception ex) {
                        throw new RuntimeException("can't serialize the policy to a String " , ex);
                    }
                }

                methodElement.appendChild(getInputElement(doc, axisOperation, soapHeaderInputParameterList));
                methodElement.appendChild(getOutputElement(doc, axisOperation, soapHeaderOutputParameterList));
                methodElement.appendChild(getFaultElement(doc, axisOperation));

                rootElement.appendChild(methodElement);
            } else {
                //mep is present - we move ahead only if the given mep matches the mep of this operation

                if (mep.equals(axisOperation.getMessageExchangePattern())) {
                    //at this point we know it's true
                    opsFound = true;
                    List soapHeaderInputParameterList = new ArrayList();
                    List soapHeaderOutputParameterList = new ArrayList();
                    methodElement = doc.createElement("method");
                    String localPart = axisOperation.getName().getLocalPart();
                    String opCName = makeCClassName(localPart);
                    String opNS = axisOperation.getName().getNamespaceURI();

                    addAttribute(doc, "name", opCName, methodElement);
                    addAttribute(doc, "localpart", localPart, methodElement);
                    addAttribute(doc, "qname", localPart+ "|"+ opNS, methodElement);

                    addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                    addAttribute(doc, "style", axisOperation.getStyle(), methodElement);
                    addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                            methodElement);

                    addAttribute(doc, "mep", Utils.getAxisSpecifMEPConstant(axisOperation.getMessageExchangePattern()) + "", methodElement);
                    addAttribute(doc, "mepURI", axisOperation.getMessageExchangePattern(), methodElement);


                    addSOAPAction(doc, methodElement, axisOperation);
                    addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                    addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                    /*
                     * Setting the policy of the operation
                     */

                    Policy policy = axisOperation.getPolicyInclude().getPolicy();
                    if (policy != null) {
                        try {
                        addAttribute(doc, "policy",
                                PolicyUtil.policyComponentToString(policy),
                                methodElement);
                        } catch (Exception ex) {
                            throw new RuntimeException("can't serialize the policy to a String", ex);
                        }
                    }


                    methodElement.appendChild(getInputElement(doc,
                            axisOperation, soapHeaderInputParameterList));
                    methodElement.appendChild(getOutputElement(doc,
                            axisOperation, soapHeaderOutputParameterList));
                    methodElement.appendChild(getFaultElement(doc,
                            axisOperation));

                    rootElement.appendChild(methodElement);
                    //////////////////////
                }

            }

        }

        return opsFound;
    }


    /**
     * A convenient method for the generating the parameter
     * element
     *
     * @param doc
     * @param paramName
     * @param paramType
     * @param opName
     * @param paramName
     */
    protected Element generateParamComponent(Document doc,
                                             String paramName,
                                             String paramType,
                                             QName opName,
                                             String partName,
                                             boolean isPrimitive) {

        Element paramElement = doc.createElement("param");
        //return paramElement;/*
        addAttribute(doc, "name",
                paramName, paramElement);

        String typeMappingStr = (paramType == null)
                ? ""
                : paramType;


        if (JAVA_DEFAULT_TYPE.equals(typeMappingStr))
        {
            typeMappingStr = C_DEFAULT_TYPE;
        }

        addAttribute(doc, "type", typeMappingStr, paramElement);
        addAttribute(doc, "caps-type", typeMappingStr.toUpperCase(), paramElement);


        //adds the short type
        addShortType(paramElement,paramType);

        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(paramType)) {
            addAttribute(doc, "default", "yes", paramElement);
        }
        addAttribute(doc, "value", getParamInitializer(paramType), paramElement);
        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);

        //if the opName and partName are present , add them
        if (opName!=null){
            addAttribute(doc,"opname",opName.getLocalPart(),paramElement);

        }
        if (partName!= null){
            addAttribute(doc,"partname",
                    JavaUtils.capitalizeFirstChar(partName),
                    paramElement);
        }

        if (isPrimitive){
            addAttribute(doc,"primitive","yes",paramElement);
        }

        // the following methods are moved from addOurs functioin
        Map typeMap =  CTypeInfo.getTypeMap();
        Iterator it= typeMap.keySet().iterator();
        boolean isOurs = true;
        while (it.hasNext()){
            if (it.next().equals(typeMappingStr)){
                isOurs = false;
                break;
            }
        }

        if ( isOurs && typeMappingStr.length() != 0 && !typeMappingStr.equals("void") &&
                !typeMappingStr.equals(C_DEFAULT_TYPE) ){
            addAttribute(doc, "ours", "yes", paramElement);
        }
        else
        {
            isOurs = false;
        }

        if ( isOurs)
        {
            typeMappingStr = C_OUR_TYPE_PREFIX + typeMappingStr + C_OUR_TYPE_SUFFIX;
        }

        addAttribute(doc, "axis2-type", typeMappingStr, paramElement);
        addAttribute(doc, "axis2-caps-type", typeMappingStr.toUpperCase(), paramElement);

        return paramElement;  //*/
    }
    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    protected Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element paramElement = doc.createElement("param");
        AxisMessage outputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
        String typeMappingStr;
        String parameterName;

        if (outputMessage != null) {
            parameterName = this.mapper.getParameterName(outputMessage.getElementQName());
            String typeMapping = this.mapper.getTypeMappingName(outputMessage.getElementQName());
            typeMappingStr = (typeMapping == null)
                    ? ""
                    : typeMapping;
        } else {
            parameterName = "";
            typeMappingStr = "";
        }

        if (JAVA_DEFAULT_TYPE.equals(typeMappingStr))
        {
            typeMappingStr = C_DEFAULT_TYPE;
        }
        addAttribute(doc, "name", parameterName, paramElement);
        addAttribute(doc, "type", typeMappingStr, paramElement);
        addAttribute(doc, "caps-type", typeMappingStr.toUpperCase(), paramElement);


        // the following methods are moved from addOurs functioin
        Map typeMap =  CTypeInfo.getTypeMap();
        Iterator it= typeMap.keySet().iterator();
        boolean isOurs = true;
        while (it.hasNext()){
            if (it.next().equals(typeMappingStr)){
                isOurs = false;
                break;
            }
        }

        if ( isOurs && typeMappingStr.length() != 0 && !typeMappingStr.equals("void") &&
                !typeMappingStr.equals(C_DEFAULT_TYPE) ){
            addAttribute(doc, "ours", "yes", paramElement);
        }
        else
        {
            isOurs = false;
        }

        if ( isOurs)
        {
            typeMappingStr = C_OUR_TYPE_PREFIX + typeMappingStr + C_OUR_TYPE_SUFFIX;
        }
        //adds the short type
        addShortType(paramElement,typeMappingStr);


        // add an extra attribute to say whether the type mapping is the default
        if (mapper.getDefaultMappingName().equals(typeMappingStr)) {
            addAttribute(doc, "default", "yes", paramElement);
        }

        // add this as a body parameter
        addAttribute(doc, "location", "body", paramElement);
        addAttribute(doc, "opname", operation.getName().getLocalPart(), paramElement);

        return paramElement;
    }

    /**
     * Gets the output directory for source files.
     *
     * @param outputDir
     * @return Returns File.
     */
    protected File getOutputDirectory(File outputDir, String dir2) {
        if (dir2 != null && !"".equals(dir2)) {
            if (outputDir.getName().equals(".")) {
                outputDir = new File(outputDir, dir2);
            }
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;
    }


}

