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

package org.apache.axis2.wsdl.codegen.extension;

import com.ibm.wsdl.util.xml.DOM2Writer;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.XSLTConstants;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.wsdl.extensions.Schema;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.io.BufferedWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLBeansExtension extends AbstractCodeGenerationExtension {
    private static final String DEFAULT_STS_NAME = "axis2";
    public static final String SCHEMA_FOLDER = "schemas";

    public static String MAPPINGS = "mappings";
    public static String MAPPING = "mapping";
    public static String MESSAGE = "message";
    public static String JAVA_NAME = "javaclass";

    private File typeMappingFile;
    public static final String MAPPING_FOLDER = "Mapping";
    public static final String MAPPER_FILE_NAME = "mapper";


    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {

        //test the databinding type. If not just fall through
        if (configuration.getDatabindingType() != XSLTConstants.DataBindingTypes.XML_BEANS) {
            return;
        }

        //check the comptibilty
        checkCompatibility();

        Element[] additionalSchemas = loadAdditionalSchemas();

        try {
            //get the types from the types section
            WSDLTypes typesList = configuration.getWom().getTypes();

            //check for the imported types. Any imported types are supposed to be here also
            if (typesList == null) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                this.configuration.setTypeMapper(new DefaultTypeMapper());
                return;
            }

            List typesArray = typesList.getExtensibilityElements();
            //this a list that keeps the already processed schemas
            List processedSchemas = new ArrayList();

            WSDLExtensibilityElement extensiblityElt;
            SchemaTypeSystem sts = null;
            Vector xmlObjectsVector = new Vector();
            //create the type mapper
            JavaTypeMapper mapper = new JavaTypeMapper();

            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);
                Schema schema;

                if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {
                    schema = (Schema) extensiblityElt;
                    XmlOptions options = new XmlOptions();

                    options.setLoadAdditionalNamespaces(
                            configuration.getWom().getNamespaces()); //add the namespaces


                    Stack importedSchemaStack = schema.getImportedSchemaStack();
                    File schemaFolder = new File(configuration.getOutputLocation(), SCHEMA_FOLDER);
                    schemaFolder.mkdir();
                    //compile these schemas
                    while (!importedSchemaStack.isEmpty()) {
                        Element element = (Element)importedSchemaStack.pop();
                        String tagetNamespace = element.getAttribute("targetNamespace");
                        if (!processedSchemas.contains(tagetNamespace)){

                            // we are not using DOM toString method here, as it seems it depends on the
                            // JDK version that is being used.
                            String s = DOM2Writer.nodeToString(element);

                            //write the schema to a file
                            File tempFile = File.createTempFile("temp", ".xsd", schemaFolder);
                            FileWriter writer = new FileWriter(tempFile);
                            writer.write(s);
                            writer.flush();
                            writer.close();


                            xmlObjectsVector.add(
                                    XmlObject.Factory.parse(
                                            element
                                            , options));

                            processedSchemas.add(tagetNamespace);
                        }
                    }
                }
            }

            // add the third party schemas
            //todo pehaps checking the namespaces would be a good idea to
            //make the generated code work efficiently
            for (int i = 0; i < additionalSchemas.length; i++) {
                xmlObjectsVector.add(XmlObject.Factory.parse(
                        additionalSchemas[i]
                        , null));
            }

            //compile the type system
            XmlObject[] objeArray = convertToXMLObjectArray(xmlObjectsVector);
            BindingConfig config = new Axis2BindingConfig();

            //set the STS name to null. it makes the generated class include a unique (but random) STS name
            sts = XmlBeans.compileXmlBeans(null, null,
                    objeArray,
                    config, XmlBeans.getContextTypeLoader(),
                    new Axis2Filer(),
                    null);

            // prune the generated schema type system and add the list of base64 types
            FindBase64Types(sts);

            //get the schematypes and add the document types to the type mapper
            SchemaType[] schemaType = sts.documentTypes();
            SchemaType type;
            for (int j = 0; j < schemaType.length; j++) {
                type = schemaType[j];
                mapper.addTypeMapping(type.getDocumentElementName(),
                        type.getFullJavaName());
            }
            //set the type mapper to the config
            configuration.setTypeMapper(mapper);

            // write the mapper to a file for later retriival
            writeMappingsToFile(mapper.getAllTypeMappings());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void writeMappingsToFile(Map typeMappings) throws IOException {

        File typeMappingFolder = new File(configuration.getOutputLocation(), MAPPING_FOLDER);
        if (!typeMappingFolder.exists()) {
            typeMappingFolder.mkdir();
        }

        this.typeMappingFile = File.createTempFile(MAPPER_FILE_NAME, ".xml", typeMappingFolder);
        BufferedWriter out = new BufferedWriter(new FileWriter(typeMappingFile));
        out.write("<" + MAPPINGS + ">");

        Iterator iterator = typeMappings.keySet().iterator();
        while (iterator.hasNext()) {
            QName qName = (QName) iterator.next();
            String fullJavaName = (String) typeMappings.get(qName);
            out.write("<" + MAPPING + ">");
            out.write("<" + MESSAGE + ">" + qName.getLocalPart() + "</" + MESSAGE + ">");
            out.write("<" + JAVA_NAME + ">" + fullJavaName + "</" + JAVA_NAME + ">");
            out.write("</" + MAPPING + ">");
        }
        out.write("</" + MAPPINGS + ">");
        out.close();

    }

    /**
     * Populate the base64 types
     * The algo is to look for simpletypes that have base64 content, and then step out of that
     * onestep and get the element. For now there's an extended check to see whether the simple type
     * is related to the Xmime:contentType!
     *
     * @param sts
     */
    private void FindBase64Types(SchemaTypeSystem sts) {
        List allSeenTypes = new ArrayList();
        List base64ElementQNamesList = new ArrayList();
        SchemaType outerType;
        //add the document types and global types
        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));
        for (int i = 0; i < allSeenTypes.size(); i++) {
            SchemaType sType = (SchemaType) allSeenTypes.get(i);

            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT && sType.getPrimitiveType() != null) {
                if (XSLTConstants.BASE_64_CONTENT_QNAME.equals(sType.getPrimitiveType().getName())) {
                    outerType = sType.getOuterType();
                    //check the outer type further to see whether it has the contenttype attribute from
                    //XMime namespace
                    SchemaProperty[] properties = sType.getProperties();
                    for (int j = 0; j < properties.length; j++) {
                        if (XSLTConstants.XMIME_CONTENT_TYPE_QNAME.equals(properties[j].getName())) {
                            base64ElementQNamesList.add(outerType.getDocumentElementName());
                            break;
                        }
                    }
                }
            }
            //add any of the child types if there are any
            allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
        }

        configuration.put(XSLTConstants.BASE_64_PROPERTY_KEY, base64ElementQNamesList);
    }

    /**
     * Loading the external schemas.
     *
     * @return element array consisting of the the DOM element objects that represent schemas
     */
    private Element[] loadAdditionalSchemas() {
        //load additional schemas
        String[] schemaNames = ConfigPropertyFileLoader.getThirdPartySchemaNames();
        Element[] schemaElements = null;

        try {
            ArrayList additionalSchemaElements = new ArrayList();
            DocumentBuilder documentBuilder = getNamespaceAwareDocumentBuilder();
            for (int i = 0; i < schemaNames.length; i++) {
                //the location for the third party schema;s is hardcoded
                if (!"".equals(schemaNames[i].trim())){
                    InputStream schemaStream = this.getClass().getResourceAsStream("/org/apache/axis2/wsdl/codegen/schema/" + schemaNames[i]);
                    Document doc = documentBuilder.parse(schemaStream);
                    additionalSchemaElements.add(doc.getDocumentElement());
                }
            }

            //Create the Schema element array
            schemaElements = new Element[additionalSchemaElements.size()];
            for (int i = 0; i < additionalSchemaElements.size(); i++) {
                schemaElements[i] = (Element) additionalSchemaElements.get(i);

            }
        } catch (Exception e) {
            throw new RuntimeException("Additional schema loading failed!!", e);
        }

        return schemaElements;
    }

    private DocumentBuilder getNamespaceAwareDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory.newDocumentBuilder();
    }


    /**
     * Checking the compatibilty has to do with generating RPC/encoded stubs.
     * If the XMLBeans bindings are used encoded binding cannot be done.
     */
    private void checkCompatibility() {
        Map bindingMap = this.configuration.getWom().getBindings();
        Collection col = bindingMap.values();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            WSDLBinding b = (WSDLBinding) iterator.next();
            HashMap bindingOps = b.getBindingOperations();
            Collection bindingOpsCollection = bindingOps.values();
            for (Iterator iterator1 = bindingOpsCollection.iterator(); iterator1.hasNext();) {
                foo((WSDLBindingOperation) iterator1.next());
            }

        }
    }

    protected void foo(WSDLBindingOperation bindingOp) {
        Iterator extIterator = bindingOp.getInput().getExtensibilityElements()
                .iterator();
        while (extIterator.hasNext()) {
            WSDLExtensibilityElement element = (WSDLExtensibilityElement) extIterator.next();
            if (ExtensionConstants.SOAP_11_BODY.equals(element.getType()) ||
                    ExtensionConstants.SOAP_12_BODY.equals(element.getType())) {
                if (WSDLConstants.WSDL_USE_ENCODED.equals(
                        ((SOAPBody) element).getUse())) {
                    throw new RuntimeException(
                            "The use 'encoded' is not supported!");
                }
            }
        }
    }

    private XmlObject[] convertToXMLObjectArray(Vector vec) {
        return (XmlObject[])vec.toArray(new XmlObject[vec.size()]);
    }

    /**
     * Private class to generate the filer
     */
    private class Axis2Filer implements Filer {

        public OutputStream createBinaryFile(String typename)
                throws IOException {
            File file = new File(configuration.getOutputLocation(), typename);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileOutputStream(file);
        }

        public Writer createSourceFile(String typename)
                throws IOException {
            typename =
                    typename.replace('.', File.separatorChar);
            File file = new File(configuration.getOutputLocation(),
                    typename + ".java");
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileWriter(file);
        }
    }

    /**
     * Custom binding configuration for the code generator. This controls
     * how the namespaces are suffixed/prefixed
     */
    private class Axis2BindingConfig extends BindingConfig {
        Pattern pattern = Pattern.compile("//[\\w\\.]*");
        private static final String DATABINDING_PACKAGE_SUFFIX = "databinding";

        public String lookupPackageForNamespace(String uri) {
            //take the **.**.** part from the uri
            Matcher matcher = pattern.matcher(uri);
            String packageName = "";
            if (matcher.find()) {
                String tempPackageName = matcher.group().replaceFirst("//", "");
                //reverse the names
                String[] parts = tempPackageName.split("\\.");
                for (int i = parts.length - 1; i >= 0; i--) {
                    packageName = packageName + "." + parts[i];

                }

            } else {
                //replace all none word chars with an underscore
                packageName = uri.replaceAll("\\W", "_");
            }

            return configuration.getPackageName() == null ? "" : (configuration.getPackageName() + ".") + DATABINDING_PACKAGE_SUFFIX + packageName;
        }

    }

}

