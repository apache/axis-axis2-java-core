/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.jaxbri;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.Property;
import org.apache.axis2.util.SchemaUtil;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CodeGenerationUtility {
    private static final Log log = LogFactory.getLog(CodeGenerationUtility.class);

    /**
     * @param additionalSchemas
     * @throws RuntimeException
     */
    public static TypeMapper processSchemas(final List schemas,
                                            Element[] additionalSchemas,
                                            CodeGenConfiguration cgconfig)
            throws RuntimeException {
        try {

            //check for the imported types. Any imported types are supposed to be here also
            if (schemas == null || schemas.isEmpty()) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                return new DefaultTypeMapper();
            }

            final ArrayList xmlObjectsVector = new ArrayList();

            //create the type mapper
            JavaTypeMapper mapper = new JavaTypeMapper();

            final String baseURI = cgconfig.getBaseURI();

            for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = (XmlSchema)schemas.get(i);
                InputSource inputSource =
                        new InputSource(new StringReader(getSchemaAsString(schema)));
                inputSource.setSystemId(baseURI);
                xmlObjectsVector.add(inputSource);
            }

            File outputDir = new File(cgconfig.getOutputLocation(), "src");
            outputDir.mkdir();

            Map nsMap = cgconfig.getUri2PackageNameMap();
            EntityResolver resolver = new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    for (int i = 0; i < schemas.size(); i++) {
                        XmlSchema schema = (XmlSchema) schemas.get(i);
                        if(schema.getTargetNamespace().equals(publicId)){
                            InputSource inputSource =
                                    new InputSource(new StringReader(getSchemaAsString(schema)));
                            inputSource.setSystemId(baseURI);
                            return inputSource;
                        }
                    }
                    return null;
                }
            };

            for (int i = 0; i < xmlObjectsVector.size(); i++) {

                SchemaCompiler sc = XJC.createSchemaCompiler();
                XmlSchema schema = (XmlSchema)schemas.get(i);

                if (nsMap != null) {
                    Iterator iterator = nsMap.entrySet().iterator();
                    while(iterator.hasNext()){
                        Map.Entry entry = (Map.Entry) iterator.next();
                        String namespace = (String) entry.getKey();
                        String pkg = (String)nsMap.get(namespace);
                        registerNamespace(sc, namespace, pkg);
                    }
                } else {
                    String namespace = schema.getTargetNamespace();
                    String pkg = extractNamespace(schema);
                    registerNamespace(sc, namespace, pkg);
                }

                sc.setEntityResolver(resolver);

                sc.setErrorListener(new ErrorListener(){
                    public void error(SAXParseException saxParseException) {
                        log.error(saxParseException.getMessage());
                        log.debug(saxParseException.getMessage(), saxParseException);
                    }

                    public void fatalError(SAXParseException saxParseException) {
                        log.error(saxParseException.getMessage());
                        log.debug(saxParseException.getMessage(), saxParseException);
                    }

                    public void warning(SAXParseException saxParseException) {
                        log.warn(saxParseException.getMessage());
                        log.debug(saxParseException.getMessage(), saxParseException);
                    }

                    public void info(SAXParseException saxParseException) {
                        log.info(saxParseException.getMessage());
                        log.debug(saxParseException.getMessage(), saxParseException);
                    }
                });

                sc.parseSchema((InputSource) xmlObjectsVector.get(i));

                // Bind the XML
                S2JJAXBModel jaxbModel = sc.bind();

                if(jaxbModel == null){
                    throw new RuntimeException("Unable to generate code using jaxbri");
                }

                // Emit the code artifacts
                JCodeModel codeModel = jaxbModel.generateCode(null, null);
                FileCodeWriter writer = new FileCodeWriter(outputDir);
                codeModel.build(writer);

                Collection mappings = jaxbModel.getMappings();

                Iterator iter = mappings.iterator();

                while (iter.hasNext()) {
                    Mapping mapping = (Mapping)iter.next();
                    QName qn = mapping.getElement();
                    String typeName = mapping.getType().getTypeClass().fullName();

                    mapper.addTypeMappingName(qn, typeName);
                }

                //process the unwrapped parameters
                if (!cgconfig.isParametersWrapped()) {
                    //figure out the unwrapped operations
                    List axisServices = cgconfig.getAxisServices();
                    for (Iterator servicesIter = axisServices.iterator(); servicesIter.hasNext();) {
                        AxisService axisService = (AxisService)servicesIter.next();
                        for (Iterator operations = axisService.getOperations();
                             operations.hasNext();) {
                            AxisOperation op = (AxisOperation)operations.next();

                            if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
                                AxisMessage message = op.getMessage(
                                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                                if (message != null &&
                                        message.getParameter(Constants.UNWRAPPED_KEY) != null) {

                                    Mapping mapping = jaxbModel.get(message.getElementQName());
                                    List elementProperties = mapping.getWrapperStyleDrilldown();
                                    for(int j = 0; j < elementProperties.size(); j++){
                                        Property elementProperty = (Property) elementProperties.get(j);

                                        QName partQName =
                                                    WSDLUtil.getPartQName(op.getName().getLocalPart(),
                                                                          WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                                                          elementProperty.elementName().getLocalPart());
                                        //this type is based on a primitive type- use the
                                        //primitive type name in this case
                                        String fullJaveName =
                                                elementProperty.type().fullName();
                                        if (elementProperty.type().isArray()) {
                                            fullJaveName = fullJaveName.concat("[]");
                                        }
                                        mapper.addTypeMappingName(partQName, fullJaveName);

                                        if (elementProperty.type().isPrimitive()) {
                                            mapper.addTypeMappingStatus(partQName, Boolean.TRUE);
                                        }
                                        if (elementProperty.type().isArray()) {
                                            mapper.addTypeMappingStatus(partQName,
                                                                        Constants.ARRAY_TYPE);
                                        }
                                    }
                                }
                            }

                            if (WSDLUtil.isOutputPresentForMEP(op.getMessageExchangePattern())) {
                                AxisMessage message = op.getMessage(
                                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                                if (message != null &&
                                        message.getParameter(Constants.UNWRAPPED_KEY) != null) {

                                    Mapping mapping = jaxbModel.get(message.getElementQName());
                                    List elementProperties = mapping.getWrapperStyleDrilldown();
                                    for(int j = 0; j < elementProperties.size(); j++){
                                        Property elementProperty = (Property) elementProperties.get(j);

                                        QName partQName =
                                                    WSDLUtil.getPartQName(op.getName().getLocalPart(),
                                                                          WSDLConstants.OUTPUT_PART_QNAME_SUFFIX,
                                                                          elementProperty.elementName().getLocalPart());
                                        //this type is based on a primitive type- use the
                                        //primitive type name in this case
                                        String fullJaveName =
                                                elementProperty.type().fullName();
                                        if (elementProperty.type().isArray()) {
                                            fullJaveName = fullJaveName.concat("[]");
                                        }
                                        mapper.addTypeMappingName(partQName, fullJaveName);

                                        if (elementProperty.type().isPrimitive()) {
                                            mapper.addTypeMappingStatus(partQName, Boolean.TRUE);
                                        }
                                        if (elementProperty.type().isArray()) {
                                            mapper.addTypeMappingStatus(partQName,
                                                                        Constants.ARRAY_TYPE);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Return the type mapper
            return mapper;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void registerNamespace(SchemaCompiler sc, String namespace, String pkgName) throws Exception {
        Document doc = XMLUtils.newDocument();
        Element rootElement = doc.createElement("schema");
        rootElement.setAttribute("xmlns", "http://www.w3.org/2001/XMLSchema");
        rootElement.setAttribute("xmlns:jaxb", "http://java.sun.com/xml/ns/jaxb");
        rootElement.setAttribute("jaxb:version", "2.0");
        rootElement.setAttribute("targetNamespace", namespace);
        Element annoElement = doc.createElement("annotation");
        Element appInfo = doc.createElement("appinfo");
        Element schemaBindings = doc.createElement("jaxb:schemaBindings");
        Element pkgElement = doc.createElement("jaxb:package");
        pkgElement.setAttribute("name", pkgName);
        annoElement.appendChild(appInfo);
        appInfo.appendChild(schemaBindings);
        schemaBindings.appendChild(pkgElement);
        rootElement.appendChild(annoElement);
        File file = File.createTempFile("customized",".xsd");
        FileOutputStream stream = new FileOutputStream(file);
        try {
            Result result = new StreamResult(stream);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(rootElement), result);
            stream.flush();
            stream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        InputSource ins = new InputSource(file.toURI().toString());
        sc.parseSchema(ins);
        file.delete();
    }

    private static String extractNamespace(XmlSchema schema) {
        String pkg;
        pkg = schema.getTargetNamespace();
        if (pkg == null) {
            XmlSchema[] schemas2 = SchemaUtil.getAllSchemas(schema);
            for (int j = 0; schemas2 != null && j < schemas2.length; j++) {
                pkg = schemas2[j].getTargetNamespace();
                if (pkg != null)
                    break;
            }
        }
        if (pkg == null) {
            pkg = URLProcessor.DEFAULT_PACKAGE;
        }
        pkg = URLProcessor.makePackageName(pkg);
        return pkg;
    }

    private static String getSchemaAsString(XmlSchema schema) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.write(baos);
        return baos.toString();
    }
}