package org.apache.axis2.wsdl.codegen.emitter;

import org.apache.axis2.description.AxisExtensiblityElementWrapper;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.PolicyAttachmentUtil;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.XSLTUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.axis2.wsdl.codegen.writer.SkeletonWriter;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.policy.Policy;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityAttribute;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPHeader;
import org.apache.wsdl.extensions.SOAPOperation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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

/**
 * MultiLanguageClientEmitter we have now is based on WOM. This one will directly infer the information
 * from the AxisService.
 */

public class AxisServiceBasedMultiLanguageEmitter implements Emitter {

    private static final String CALL_BACK_HANDLER_SUFFIX = "CallbackHandler";
    private static final String STUB_SUFFIX = "Stub";
    private static final String TEST_SUFFIX = "Test";
    private static final String SERVICE_CLASS_SUFFIX = "Skeleton";
    private static final String MESSAGE_RECEIVER_SUFFIX = "MessageReceiver";
    private static final String DATABINDING_SUPPORTER_NAME_SUFFIX = "DatabindingSupporter";
    private static final String DATABINDING_PACKAGE_NAME_SUFFIX = ".databinding";

    private static Map MEPtoClassMap;
    private static Map MEPtoSuffixMap;


    /**
     * Field constructorMap
     */
    private static HashMap constructorMap = new HashMap(50);

    //~--- static initializers ------------------------------------------------

    static {

        // Type maps to a valid initialization value for that type
        // Type var = new Type(arg)
        // Where "Type" is the key and "new Type(arg)" is the string stored
        // Used in emitting test cases and server skeletons.
        constructorMap.put("int", "0");
        constructorMap.put("float", "0");
        constructorMap.put("boolean", "true");
        constructorMap.put("double", "0");
        constructorMap.put("byte", "(byte)0");
        constructorMap.put("short", "(short)0");
        constructorMap.put("long", "0");
        constructorMap.put("java.lang.Boolean", "new java.lang.Boolean(false)");
        constructorMap.put("java.lang.Byte", "new java.lang.Byte((byte)0)");
        constructorMap.put("java.lang.Double", "new java.lang.Double(0)");
        constructorMap.put("java.lang.Float", "new java.lang.Float(0)");
        constructorMap.put("java.lang.Integer", "new java.lang.Integer(0)");
        constructorMap.put("java.lang.Long", "new java.lang.Long(0)");
        constructorMap.put("java.lang.Short", "new java.lang.Short((short)0)");
        constructorMap.put("java.math.BigDecimal", "new java.math.BigDecimal(0)");
        constructorMap.put("java.math.BigInteger", "new java.math.BigInteger(\"0\")");
        constructorMap.put("java.lang.Object", "new java.lang.String()");
        constructorMap.put("byte[]", "new byte[0]");
        constructorMap.put("java.util.Calendar", "java.util.Calendar.getInstance()");
        constructorMap.put("javax.xml.namespace.QName",
                "new javax.xml.namespace.QName(\"http://double-double\", \"toil-and-trouble\")");

        //populate the MEP -> class map
        MEPtoClassMap = new HashMap();
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_ONLY, "org.apache.axis2.receivers.AbstractInMessageReceiver");
        MEPtoClassMap.put(WSDLConstants.MEP_URI_IN_OUT, "org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver");

        //populate the MEP -> suffix map
        MEPtoSuffixMap = new HashMap();
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_ONLY, MESSAGE_RECEIVER_SUFFIX + "InOnly");
        MEPtoSuffixMap.put(WSDLConstants.MEP_URI_IN_OUT, MESSAGE_RECEIVER_SUFFIX + "InOut");
        //register the other types as necessary
    }

    //~--- fields -------------------------------------------------------------
    private Log log = LogFactory.getLog(getClass());
    protected URIResolver resolver;

    private Map infoHolder;

    CodeGenConfiguration codeGenConfiguration;
    protected TypeMapper mapper;
    protected PolicyAttachmentUtil attachmentUtil;

    private AxisService axisService;

    public AxisServiceBasedMultiLanguageEmitter() {
        infoHolder = new HashMap();
    }

    public void setCodeGenConfiguration(CodeGenConfiguration configuration) {
        this.codeGenConfiguration = configuration;
        this.axisService = codeGenConfiguration.getAxisService();
    }

    public void setMapper(TypeMapper mapper) {
        this.mapper = mapper;
    }

    public void emitStub() throws CodeGenerationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void emitSkeleton() throws CodeGenerationException {
        try {

            // get the interface
            int codegenStyle = this.codeGenConfiguration.getCodeGenerationStyle();

            if (codegenStyle == XSLTConstants.CodegenStyle.INTERFACE) {
                emitSkeletonInterface();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.BINDING) {
                emitSkeletonBinding();
            } else if (codegenStyle == XSLTConstants.CodegenStyle.AUTOMATIC) {
                emitSkeletonAutomatic();
            } else {
                throw new Exception(CodegenMessages.getMessage("emitter.unknownStyle", codegenStyle + ""));
            }
        } catch (Exception e) {
            throw new CodeGenerationException(e);
        }
    }

    private void emitSkeletonAutomatic() {
        // TODO
    }

    private void emitSkeletonBinding() {

        // TODO
    }

    private void emitSkeletonInterface() throws Exception {
        // write skeleton
        writeSkeleton();

        // write interface implementations
        writeServiceXml();
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface1"));
        log.info(CodegenMessages.getMessage("emitter.logEntryInterface2"));
    }

    private void writeServiceXml() throws Exception {
        if (this.codeGenConfiguration.isGenerateDeployementDescriptor()) {

            // Write the service xml in a folder with the
            Document serviceXMLModel = createDOMDocumentForServiceXML();
            debugLogDocument("Document for service XML:", serviceXMLModel);
            ClassWriter serviceXmlWriter =
                    new ServiceXMLWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(), "resources"),
                            this.codeGenConfiguration.getOutputLanguage());

            writeClass(serviceXMLModel, serviceXmlWriter);
        }
    }

    private Document createDOMDocumentForServiceXML() {
        Document doc = getEmptyDocument();
        String serviceName = axisService.getName();
        String className = makeJavaClassName(serviceName);

        doc.appendChild(getServiceElement(serviceName, className, doc));
        return doc;

    }

    private Node getServiceElement(String serviceName, String className, Document doc) {
        Element rootElement = doc.createElement("interface");

        addAttribute(doc, "package", "", rootElement);
        addAttribute(doc, "classpackage", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", className + SERVICE_CLASS_SUFFIX, rootElement);
        if (!codeGenConfiguration.isWriteTestCase()) {
            addAttribute(doc, "testOmit", "true", rootElement);
        }
        addAttribute(doc, "servicename", serviceName, rootElement);

        Iterator it = MEPtoClassMap.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();

            if (Boolean.TRUE.equals(infoHolder.get(key))) {
                Element elt = addElement(doc, "messagereceiver", className + MEPtoSuffixMap.get(key), rootElement);
                addAttribute(doc, "mep", key.toString(), elt);
            }

        }

        loadOperations(doc, rootElement, null);

        return rootElement;
    }

    private void writeSkeleton() throws Exception {
        Document skeletonModel = createDOMDocumentForSkeleton();
        debugLogDocument("Document for skeleton:", skeletonModel);
        ClassWriter skeletonWriter = new SkeletonWriter(getOutputDirectory(this.codeGenConfiguration.getOutputLocation(),
                "src"), this.codeGenConfiguration.getOutputLanguage());

        writeClass(skeletonModel, skeletonWriter);
    }

    private Document createDOMDocumentForSkeleton() {
        Document doc = getEmptyDocument();
        Element rootElement = doc.createElement("interface");

        String serviceName = makeJavaClassName(axisService.getName());
        addAttribute(doc, "package", codeGenConfiguration.getPackageName(), rootElement);
        addAttribute(doc, "name", serviceName + SERVICE_CLASS_SUFFIX, rootElement);
        addAttribute(doc, "callbackname", serviceName + CALL_BACK_HANDLER_SUFFIX,
                rootElement);

        fillSyncAttributes(doc, rootElement);
        loadOperations(doc, rootElement, null);
        doc.appendChild(rootElement);

        return doc;

    }

    private void loadOperations(Document doc, Element rootElement, String mep) {
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

                addAttribute(doc, "name", localPart, methodElement);
                addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                addAttribute(doc, "style", axisOperation.getStyle(), methodElement);
                addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                        methodElement);

                addAttribute(doc, "mep", axisOperation.getMessageExchangePattern(), methodElement);

                addSOAPAction(doc, methodElement, axisOperation);
                addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                Policy policy = axisOperation.getPolicyInclude().getPolicy();
                if (policy != null) {
                    addAttribute(doc, "policy", PolicyUtil.getPolicyAsString(policy), methodElement);
                }

                methodElement.appendChild(getInputElement(doc, axisOperation, soapHeaderInputParameterList));
                methodElement.appendChild(getOutputElement(doc, axisOperation, soapHeaderOutputParameterList));
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

                    addAttribute(doc, "name", localPart, methodElement);
                    addAttribute(doc, "namespace", axisOperation.getName().getNamespaceURI(), methodElement);
                    addAttribute(doc, "style", axisOperation.getStyle(), methodElement);
                    addAttribute(doc, "dbsupportname", portTypeName + localPart + DATABINDING_SUPPORTER_NAME_SUFFIX,
                            methodElement);

                    addAttribute(doc, "mep", axisOperation.getMessageExchangePattern(), methodElement);


                    addSOAPAction(doc, methodElement, axisOperation);
                    addHeaderOperations(soapHeaderInputParameterList, axisOperation, true);
                    addHeaderOperations(soapHeaderOutputParameterList, axisOperation, false);

                    /*
                     * Setting the policy of the operation
                     */

                    Policy policy = axisOperation.getPolicyInclude().getPolicy();
                    if (policy != null) {
                        addAttribute(doc, "policy", PolicyUtil.getPolicyAsString(policy), methodElement);
                    }


                    methodElement.appendChild(getInputElement(doc, axisOperation, soapHeaderInputParameterList));
                    methodElement.appendChild(getOutputElement(doc, axisOperation, soapHeaderOutputParameterList));
                    rootElement.appendChild(methodElement);
                    //////////////////////
                }
            }

        }
    }

    // ==================================================================
    //                   Util Methods
    // ==================================================================

    private Document getEmptyDocument() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param word
     * @return Returns character removed string.
     */
    protected String makeJavaClassName(String word) {
        if (JavaUtils.isJavaKeyword(word)) {
            return JavaUtils.makeNonJavaKeyword(word);
        } else {
            return JavaUtils.capitalizeFirstChar(JavaUtils.xmlNameToJava(word));
        }
    }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param AttribName
     * @param attribValue
     * @param element
     */
    protected void addAttribute(Document document, String AttribName, String attribValue, Element element) {
        XSLTUtils.addAttribute(document, AttribName, attribValue, element);
    }

    private void fillSyncAttributes(Document doc, Element rootElement) {
        addAttribute(doc, "isAsync", this.codeGenConfiguration.isAsyncOn()
                ? "1"
                : "0", rootElement);
        addAttribute(doc, "isSync", this.codeGenConfiguration.isSyncOn()
                ? "1"
                : "0", rootElement);
    }

    private void debugLogDocument(String description, Document doc) {
        if (log.isDebugEnabled()) {
            try {
                DOMSource source = new DOMSource(doc);
                StringWriter swrite = new StringWriter();
                swrite.write(description);
                swrite.write("\n");
                Transformer transformer =
                        TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty("omit-xml-declaration", "yes");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(source, new StreamResult(swrite));
                log.debug(swrite.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the output directory for source files.
     *
     * @param outputDir
     * @return Returns File.
     */
    protected File getOutputDirectory(File outputDir, String dir2) {
        outputDir = new File(outputDir, dir2);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        return outputDir;
    }

    /**
     * A resusable method for the implementation of interface and implementation writing.
     *
     * @param model
     * @param writer
     * @throws java.io.IOException
     * @throws Exception
     */
    protected void writeClass(Document model, ClassWriter writer) throws IOException, Exception {
        writer.loadTemplate();

        String packageName = model.getDocumentElement().getAttribute("package");
        String className = model.getDocumentElement().getAttribute("name");

        writer.createOutFile(packageName, className);

        // use the global resolver
        writer.parse(model, resolver);
    }

    private void addSOAPAction(Document doc, Element rootElement, AxisOperation axisOperation) {
        List extensibilityElements = axisOperation.getWsdlExtElements();
        boolean actionAdded = false;

        if ((extensibilityElements != null) && !extensibilityElements.isEmpty()) {
            Iterator extIterator = extensibilityElements.iterator();

            while (extIterator.hasNext()) {
                AxisExtensiblityElementWrapper axisExtensibilityElement = (AxisExtensiblityElementWrapper) extIterator.next();
                WSDLExtensibilityElement element = axisExtensibilityElement.getExtensibilityElement();

                if (element != null && ExtensionConstants.SOAP_11_OPERATION.equals(element.getType())
                        || ExtensionConstants.SOAP_12_OPERATION.equals(element.getType())) {
                    addAttribute(doc, "soapaction", ((SOAPOperation) element).getSoapAction(), rootElement);
                    actionAdded = true;
                }
            }
        }

        if (!actionAdded) {
            addAttribute(doc, "soapaction", "", rootElement);
        }
    }

    private void addHeaderOperations(List soapHeaderParameterQNameList, AxisOperation axisOperation,
                                     boolean input) {
        Iterator extIterator;

        if (input) {
            extIterator = (axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE) == null)
                    ? null
                    : axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE).getWsdlExtElements().iterator();
        } else {
            extIterator = (axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE) == null)
                    ? null
                    : axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE).getWsdlExtElements().iterator();
        }

        while ((extIterator != null) && extIterator.hasNext()) {
            AxisExtensiblityElementWrapper axisExtensibilityElement = (AxisExtensiblityElementWrapper) extIterator.next();

            WSDLExtensibilityElement element = axisExtensibilityElement.getExtensibilityElement();

            if (ExtensionConstants.SOAP_11_HEADER.equals(element.getType())) {
                SOAPHeader header = (SOAPHeader) element;

                soapHeaderParameterQNameList.add(header.getElement());
            }
        }
    }

    protected Element getInputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element inputElt = doc.createElement("input");
        Element param = getInputParamElement(doc, operation);

        if (param != null) {
            inputElt.appendChild(param);
        }

        List parameterElementList = getParameterElementList(doc, headerParameterQNameList, "header");

        for (int i = 0; i < parameterElementList.size(); i++) {
            inputElt.appendChild((Element) parameterElementList.get(i));
        }

        return inputElt;
    }

    /**
     * Finds the output element.
     *
     * @param doc
     * @param operation
     * @param headerParameterQNameList
     */
    protected Element getOutputElement(Document doc, AxisOperation operation, List headerParameterQNameList) {
        Element outputElt = doc.createElement("output");
        Element param = getOutputParamElement(doc, operation);

        if (param != null) {
            outputElt.appendChild(param);
        }

        List outputElementList = getParameterElementList(doc, headerParameterQNameList, "header");

        for (int i = 0; i < outputElementList.size(); i++) {
            outputElt.appendChild((Element) outputElementList.get(i));
        }

        return outputElt;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns the parameter element.
     */
    private Element getInputParamElement(Document doc, AxisOperation operation) {
        Element param = doc.createElement("param");
        AxisMessage inputMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

        if (inputMessage != null) {
            addAttribute(doc, "name", this.mapper.getParameterName(inputMessage.getElementQName()), param);

            // todo modify the code here to unwrap if requested
            String typeMapping = this.mapper.getTypeMappingName(inputMessage.getElementQName());

            addAttribute(doc, "type", (typeMapping == null)
                    ? ""
                    : typeMapping, param);

            // add an extra attribute to say whether the type mapping is the default
            if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMapping)) {
                addAttribute(doc, "default", "yes", param);
            }

            addAttribute(doc, "value", getParamInitializer(typeMapping), param);

            // add this as a body parameter
            addAttribute(doc, "location", "body", param);

            Iterator iter = inputMessage.getExtensibilityAttributes().iterator();

            while (iter.hasNext()) {
                WSDLExtensibilityAttribute att = (WSDLExtensibilityAttribute) iter.next();
                addAttribute(doc, att.getKey().getLocalPart(), att.getValue().toString(), param);
            }
        } else {
            param = null;
        }

        return param;
    }

    /**
     * @param doc
     * @param operation
     * @return Returns Element.
     */
    private Element getOutputParamElement(Document doc, AxisOperation operation) {
        Element param = doc.createElement("param");
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

        addAttribute(doc, "name", parameterName, param);
        addAttribute(doc, "type", typeMappingStr, param);

        // add an extra attribute to say whether the type mapping is the default
        if (TypeMapper.DEFAULT_CLASS_NAME.equals(typeMappingStr)) {
            addAttribute(doc, "default", "yes", param);
        }

        // add this as a body parameter
        addAttribute(doc, "location", "body", param);

        return param;
    }


    String getParamInitializer(String paramType) {

        // Look up paramType in the table
        String out = (String) constructorMap.get(paramType);

        if (out == null) {
            out = "null";
        }

        return out;
    }

    private List getParameterElementList(Document doc, List parameters, String location) {
        List parameterElementList = new ArrayList();

        if ((parameters != null) && !parameters.isEmpty()) {
            int count = parameters.size();

            for (int i = 0; i < count; i++) {
                Element param = doc.createElement("param");
                QName name = (QName) parameters.get(i);

                addAttribute(doc, "name", this.mapper.getParameterName(name), param);

                String typeMapping = this.mapper.getTypeMappingName(name);
                String typeMappingStr = (typeMapping == null)
                        ? ""
                        : typeMapping;

                addAttribute(doc, "type", typeMappingStr, param);
                addAttribute(doc, "location", location, param);
                parameterElementList.add(param);
            }
        }

        return parameterElementList;
    }

    /**
     * Utility method to add an attribute to a given element.
     *
     * @param document
     * @param eltName
     * @param eltValue
     * @param element
     */
    protected Element addElement(Document document, String eltName, String eltValue, Element element) {
        Element elt = XSLTUtils.addChildElement(document, eltName, element);
        elt.appendChild(document.createTextNode(eltValue));
        return elt;
    }


}
