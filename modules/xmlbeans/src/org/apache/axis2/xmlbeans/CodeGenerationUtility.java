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

package org.apache.axis2.xmlbeans;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.net.URL;

import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.Constants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisMessage;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

/**
 * Framework-linked code used by XMLBeans data binding support. This is accessed
 * via reflection from the XMLBeans code generation extension when XMLBeans data
 * binding is selected.
 */
public class CodeGenerationUtility {
    public static final String SCHEMA_FOLDER = "schemas";

    public static String MAPPINGS = "mappings";
    public static String MAPPING = "mapping";
    public static String MESSAGE = "message";
    public static String JAVA_NAME = "javaclass";

    public static final String MAPPING_FOLDER = "Mapping";
    public static final String MAPPER_FILE_NAME = "mapper";
    public static final String SCHEMA_PATH = "/org/apache/axis2/wsdl/codegen/schema/";

    boolean debug = false;

    /**
     * @param additionalSchemas
     * @throws RuntimeException
     */
    public static TypeMapper processSchemas(List schemas,
                                            Element[] additionalSchemas, CodeGenConfiguration cgconfig) throws RuntimeException {
        try {

            //check for the imported types. Any imported types are supposed to be here also
            if (schemas == null || schemas.isEmpty()) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                return new DefaultTypeMapper();
            }


            SchemaTypeSystem sts;
            List completeSchemaList = new ArrayList();
            List topLevelSchemaList = new ArrayList();

            //create the type mapper
            //First try to take the one that is already there
            TypeMapper mapper = cgconfig.getTypeMapper();
            if (mapper==null){
                mapper =new JavaTypeMapper();
            }

            //change the  default class name of the mapper to
            //xmlbeans specific XMLObject
            mapper.setDefaultMappingName(XmlObject.class.getName());

            Map nameSpacesMap = cgconfig.getAxisService().getNameSpacesMap();

            // process all the schemas and make a list of all of them for
            // resolving entities
            for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = (XmlSchema) schemas.get(i);
                XmlOptions options = new XmlOptions();
                options.setLoadAdditionalNamespaces(
                        nameSpacesMap); //add the namespaces
                Document[] allSchemas = schema.getAllSchemas();
                for (int j = 0; j < allSchemas.length; j++) {
                    Document allSchema = allSchemas[j];
                    completeSchemaList.add(
                            XmlObject.Factory.parse(
                                    allSchema
                                    , options));

                }
            }

            //make another list of top level schemas for passing into XMLbeans
            for (int i = 0; i < schemas.size(); i++) {
                XmlSchema schema = (XmlSchema) schemas.get(i);
                XmlOptions options = new XmlOptions();
                options.setLoadAdditionalNamespaces(
                        nameSpacesMap); //add the namespaces
                topLevelSchemaList.add(
                        XmlObject.Factory.parse(
                                getSchemaAsString(schema)
                                , options));

            }

            // add the third party schemas
            //todo perhaps checking the namespaces would be a good idea to
            //make the generated code work efficiently
            for (int i = 0; i < additionalSchemas.length; i++) {
                completeSchemaList.add(XmlObject.Factory.parse(
                        additionalSchemas[i]
                        , null));
                topLevelSchemaList.add(XmlObject.Factory.parse(
                        additionalSchemas[i]
                        , null));
            }

            //compile the type system
            Axis2EntityResolver er = new Axis2EntityResolver();
            er.setSchemas(convertToSchemaDocumentArray(completeSchemaList));
            er.setBaseUri(cgconfig.getBaseURI());


            sts = XmlBeans.compileXmlBeans(
                    //set the STS name to null. it makes the generated class
                    // include a unique (but random) STS name
                    null,
                    null,
                    convertToSchemaArray(topLevelSchemaList),
                    new Axis2BindingConfig(cgconfig.getUri2PackageNameMap()),
                    XmlBeans.getContextTypeLoader(),
                    new Axis2Filer(cgconfig),
                    new XmlOptions().setEntityResolver(er));

            // prune the generated schema type system and add the list of base64 types
            cgconfig.putProperty(Constants.BASE_64_PROPERTY_KEY,
                    findBase64Types(sts));
            cgconfig.putProperty(Constants.PLAIN_BASE_64_PROPERTY_KEY,
                    findPlainBase64Types(sts));

            //get the schematypes and add the document types to the type mapper
            SchemaType[] schemaType = sts.documentTypes();
            for (int j = 0; j < schemaType.length; j++) {
                mapper.addTypeMappingName(schemaType[j].getDocumentElementName(),
                        schemaType[j].getFullJavaName());

            }


            //process the unwrapped parameters
            if (!cgconfig.isParametersWrapped()) {
                //figure out the unwrapped operations
                AxisService axisService = cgconfig.getAxisService();
                for (Iterator operations = axisService.getOperations();
                     operations.hasNext();) {
                    AxisOperation op = (AxisOperation) operations.next();
                    if (WSDLUtil.isInputPresentForMEP(op.getMessageExchangePattern())) {
                        AxisMessage message = op.getMessage(
                                WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                        if (message!= null  && message.getParameter(Constants.UNWRAPPED_KEY) != null){
                            SchemaGlobalElement xmlbeansElement = sts.findElement(message.getElementQName());
                            SchemaType sType = xmlbeansElement.getType();

                            SchemaProperty[] elementProperties = sType.getElementProperties();
                            for (int i = 0; i < elementProperties.length; i++) {
                                SchemaProperty elementProperty = elementProperties[i];

                                QName partQName = WSDLUtil.getPartQName(op.getName().getLocalPart(),
                                        WSDLConstants.INPUT_PART_QNAME_SUFFIX,
                                        elementProperty.getName().getLocalPart());

                                //this type is based on a primitive type- use the
                                //primitive type name in this case
                                mapper.addTypeMappingName(partQName,elementProperty.getType().getFullJavaName());
                                SchemaType primitiveType = elementProperty.getType().getPrimitiveType();

                                if (primitiveType!=null){
                                    mapper.addTypeMappingStatus(partQName,Boolean.TRUE);
                                }
                            }
                        }
                    }
                }
            }

            //return mapper to be set in the config
            return mapper;


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Populate the base64 types
     * The algo is to look for simpletypes that have base64 content, and then step out of that
     * onestep and get the element. For now there's an extended check to see whether the simple type
     * is related to the Xmime:contentType!
     *
     * @param sts
     */
    private static List findBase64Types(SchemaTypeSystem sts) {
        List allSeenTypes = new ArrayList();
        List base64ElementQNamesList = new ArrayList();
        SchemaType outerType;
        //add the document types and global types
        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));

        for (int i = 0; i < allSeenTypes.size(); i++) {
            SchemaType sType = (SchemaType) allSeenTypes.get(i);

            if (sType.getContentType() == SchemaType.SIMPLE_CONTENT && sType.getPrimitiveType() != null) {
                if (org.apache.axis2.namespace.Constants.BASE_64_CONTENT_QNAME.equals(sType.getPrimitiveType().getName())) {
                    outerType = sType.getOuterType();
                    //check the outer type further to see whether it has the contenttype attribute from
                    //XMime namespace
                    SchemaProperty[] properties = sType.getProperties();
                    for (int j = 0; j < properties.length; j++) {
                        if (org.apache.axis2.namespace.Constants.XMIME_CONTENT_TYPE_QNAME.equals(properties[j].getName())) {
                            //add this only if it is a document type ??
                            if (outerType.isDocumentType()){
                                base64ElementQNamesList.add(outerType.getDocumentElementName());
                            }
                            break;
                        }
                    }
                }
            }
            //add any of the child types if there are any
            allSeenTypes.addAll(Arrays.asList(sType.getAnonymousTypes()));
        }

        return base64ElementQNamesList;
    }

    /**
     * @param sts
     * @return
     */
    private static List findPlainBase64Types(SchemaTypeSystem sts) {
        ArrayList allSeenTypes = new ArrayList();

        allSeenTypes.addAll(Arrays.asList(sts.documentTypes()));
        allSeenTypes.addAll(Arrays.asList(sts.globalTypes()));

        ArrayList base64Types = new ArrayList();

        for (Iterator iterator = allSeenTypes.iterator(); iterator.hasNext();) {
            SchemaType stype = (SchemaType) iterator.next();
            findPlainBase64Types(stype, base64Types, new ArrayList());
        }

        return base64Types;
    }

    /**
     * @param stype
     * @param base64Types
     */
    private static void findPlainBase64Types(SchemaType stype,
                                             ArrayList base64Types,
                                             ArrayList processedTypes) {

        SchemaProperty[] elementProperties = stype.getElementProperties();
        QName name;
        SchemaType schemaType;
        for (int i = 0; i < elementProperties.length; i++) {
            schemaType = elementProperties[i].getType();
            name = elementProperties[i].getName();
            if (!base64Types.contains(name) && !processedTypes.contains(schemaType.getName())) {
                processedTypes.add(stype.getName());
                if (schemaType.isPrimitiveType()) {
                    SchemaType primitiveType = schemaType.getPrimitiveType();
                    if (org.apache.axis2.namespace.Constants.BASE_64_CONTENT_QNAME.equals(primitiveType.getName())) {
                        base64Types.add(name);
                    }

                } else {
                    findPlainBase64Types(schemaType, base64Types, processedTypes);
                }
            }
        }


    }


    /**
     * Private class to generate the filer
     */
    private static class Axis2Filer implements Filer {

        private File location;
        private boolean flatten = false;
        private static final String RESOURCE_DIR_NAME = "resources";
        private static final String SOURCE_DIR_NAME = "src";
        private static final String JAVA_FILE_EXTENSION = ".java";

        private Axis2Filer(CodeGenConfiguration config) {
            location = config.getOutputLocation();
            flatten = config.isFlattenFiles();
        }

        public OutputStream createBinaryFile(String typename)
                throws IOException {
            File resourcesDirectory =
                    flatten?
                            location:
                            new File(location, RESOURCE_DIR_NAME);

            if (!resourcesDirectory.exists()) {
                resourcesDirectory.mkdirs();
            }
            File file = new File(resourcesDirectory, typename);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileOutputStream(file);
        }

        public Writer createSourceFile(String typename)
                throws IOException {
            typename =
                    typename.replace('.', File.separatorChar);

            File outputDir =
                    flatten?
                            location:
                            new File(location, SOURCE_DIR_NAME);

            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File file = new File(outputDir,
                    typename + JAVA_FILE_EXTENSION);
            file.getParentFile().mkdirs();
            file.createNewFile();
            return new FileWriter(file);
        }
    }

    /**
     * Convert schema into a String
     *
     * @param schema
     */
    private static String getSchemaAsString(XmlSchema schema) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.write(baos);
        return baos.toString();
    }

    /**
     * Custom binding configuration for the code generator. This controls
     * how the namespaces are suffixed/prefixed
     */
    private static class Axis2BindingConfig extends BindingConfig {

        private Map uri2packageMappings = null;

        public Axis2BindingConfig(Map uri2packageMappings) {
            this.uri2packageMappings = uri2packageMappings;
            if (this.uri2packageMappings == null) {
                //make an empty one to avoid nasty surprises
                this.uri2packageMappings = new HashMap();
            }
        }

        public String lookupPackageForNamespace(String uri) {
            if (uri2packageMappings.containsKey(uri)) {
                return (String) uri2packageMappings.get(uri);
            } else {
                return URLProcessor.makePackageName(uri);
            }

        }
    }

    /**
     * Get an array of schema documents
     * @param vec
     * @return
     */
    private static SchemaDocument[] convertToSchemaDocumentArray(List vec) {
        SchemaDocument[] schemaDocuments =
                (SchemaDocument[]) vec.toArray(new SchemaDocument[vec.size()]);
        //remove duplicates
        Vector uniqueSchemas = new Vector(schemaDocuments.length);
        Vector uniqueSchemaTns = new Vector(schemaDocuments.length);
        SchemaDocument.Schema s;
        for (int i = 0; i < schemaDocuments.length; i++) {
            s = schemaDocuments[i].getSchema();
            if (!uniqueSchemaTns.contains(s.getTargetNamespace())) {
                uniqueSchemas.add(schemaDocuments[i]);
                uniqueSchemaTns.add(s.getTargetNamespace());
            } else if (s.getTargetNamespace() == null) {
                //add anyway
                uniqueSchemas.add(schemaDocuments[i]);
            }
        }
        return (SchemaDocument[])
                uniqueSchemas.toArray(
                        new SchemaDocument[uniqueSchemas.size()]);
    }

    /**
     * Converts a given vector of schemaDocuments to XmlBeans processable
     * schema objects. One drawback we have here is the non-inclusion of
     * untargeted namespaces
     *
     * @param vec
     * @return
     */
    private static SchemaDocument.Schema[] convertToSchemaArray(List vec) {
        SchemaDocument[] schemaDocuments =
                (SchemaDocument[]) vec.toArray(new SchemaDocument[vec.size()]);
        //remove duplicates
        Vector uniqueSchemas = new Vector(schemaDocuments.length);
        Vector uniqueSchemaTns = new Vector(schemaDocuments.length);
        SchemaDocument.Schema s;
        for (int i = 0; i < schemaDocuments.length; i++) {
            s = schemaDocuments[i].getSchema();
            if (!uniqueSchemaTns.contains(s.getTargetNamespace())) {
                uniqueSchemas.add(s);
                uniqueSchemaTns.add(s.getTargetNamespace());
            } else if (s.getTargetNamespace() == null) {
                uniqueSchemas.add(s);
            }
        }
        return (SchemaDocument.Schema[])
                uniqueSchemas.toArray(
                        new SchemaDocument.Schema[uniqueSchemas.size()]);
    }

    /**
     * Implementation of the entity resolver
     * A custom entity resolver for XMLBeans
     */
    private static class Axis2EntityResolver implements EntityResolver {
        private SchemaDocument[] schemas;
        private String baseUri;

        public Axis2EntityResolver() {
        }

        /**
         * @param publicId - this is the target namespace
         * @param systemId - this is the location (value of schemaLocation)
         * @return
         * @see EntityResolver#resolveEntity(String, String)
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            try {
                for (int i = 0; i < schemas.length; i++){
                    SchemaDocument.Schema schema = schemas[i].getSchema();
                    if (schema.getTargetNamespace() != null &&
                            publicId != null &&
                            schema.getTargetNamespace().equals(publicId)) {
                        try {
                            return new InputSource(getSchemaAsStream(schemas[i]));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                if (systemId.indexOf(':') == -1) {
                    //if the base URI is missing then attache the file:/// to it
                    //if the systemId actually had a scheme then as per the URL
                    //constructor, the context URL scheme should be ignored
                    baseUri = (baseUri == null) ? "file:///" : baseUri;
                    URL url = new URL(new URL(baseUri),systemId);
                    return new InputSource(url.openStream() );
                }
                return XMLUtils.getEmptyInputSource();
            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        public SchemaDocument[]  getSchemas() {
            return schemas;
        }

        public void setSchemas(SchemaDocument[] schemas) {
            this.schemas = schemas;
        }

        public String getBaseUri() {
            return baseUri;
        }

        public void setBaseUri(String baseUri) {
            this.baseUri = baseUri;
        }

        /**
         * Convert schema into a InputStream
         *
         * @param doc
         */
        private ByteArrayInputStream getSchemaAsStream(SchemaDocument schema) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            schema.save(baos);
            baos.flush();
            return new ByteArrayInputStream(baos.toByteArray());

        }

    }

    /**
     * Populate the schema objects into the
     *
     * @param schemaMap
     * @param schemaList
     */
    private static void populateSchemaMap(Map schemaMap, List schemaList) {
        for (int i = 0; i < schemaList.size(); i++) {
            XmlSchema xmlSchema = (XmlSchema) schemaList.get(i);
            schemaMap.put(xmlSchema.getTargetNamespace(), xmlSchema);
        }
    }

    /**
     * Look for a given schema type given the schema type Qname
     * @param schemaMap
     * @param namespaceURI
     * @return null if the schema is not found
     */
    private static XmlSchemaType findSchemaType(Map schemaMap, QName schemaTypeName) {
        //find the schema
        XmlSchema schema = (XmlSchema) schemaMap.get(schemaTypeName.getNamespaceURI());
        if (schema!=null){
            return schema.getTypeByName(schemaTypeName);
        }
        return null;
    }
}



