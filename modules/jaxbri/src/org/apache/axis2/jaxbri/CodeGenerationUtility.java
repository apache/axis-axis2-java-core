/*
 * Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.jaxbri;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import org.apache.axis2.util.SchemaUtil;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CodeGenerationUtility {
    private static final Log log = LogFactory.getLog(CodeGenerationUtility.class);

    /**
     * @param additionalSchemas
     * @throws RuntimeException
     */
    public static TypeMapper processSchemas(List schemas,
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

            Vector xmlObjectsVector = new Vector();

            //create the type mapper
            JavaTypeMapper mapper = new JavaTypeMapper();

            String baseURI = cgconfig.getBaseURI();

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

            for (int i = 0; i < xmlObjectsVector.size(); i++) {

                SchemaCompiler sc = XJC.createSchemaCompiler();
                XmlSchema schema = (XmlSchema)schemas.get(i);

                String pkg = null;
                if (nsMap != null) {
                    pkg = (String)nsMap.get(schema.getTargetNamespace());
                }
                if (pkg == null) {
                    pkg = extractNamespace(schema);
                }
                sc.setDefaultPackageName(pkg);

                sc.setErrorListener(new ErrorListener(){
                    public void error(SAXParseException saxParseException) {
                        log.error(saxParseException.getMessage(), saxParseException);
                    }

                    public void fatalError(SAXParseException saxParseException) {
                        log.error(saxParseException.getMessage(), saxParseException);
                    }

                    public void warning(SAXParseException saxParseException) {
                        log.warn(saxParseException.getMessage(), saxParseException);
                    }

                    public void info(SAXParseException saxParseException) {
                        log.info(saxParseException.getMessage(), saxParseException);
                    }
                });
                sc.parseSchema((InputSource)xmlObjectsVector.elementAt(i));

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
            }

            // Return the type mapper
            return mapper;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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