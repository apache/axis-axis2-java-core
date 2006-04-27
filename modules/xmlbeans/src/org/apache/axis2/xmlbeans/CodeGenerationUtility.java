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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.HashMap;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;

import com.ibm.wsdl.util.xml.DOM2Writer;

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
     *
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
            JavaTypeMapper mapper = new JavaTypeMapper();
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
            Axis2EntityResolver er  = new Axis2EntityResolver();
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
                    new Axis2Filer(cgconfig.getOutputLocation()),
                    new XmlOptions().setEntityResolver(er));

            // prune the generated schema type system and add the list of base64 types
            cgconfig.putProperty(XSLTConstants.BASE_64_PROPERTY_KEY,
                    findBase64Types(sts));
            cgconfig.putProperty(XSLTConstants.PLAIN_BASE_64_PROPERTY_KEY,
                    findPlainBase64Types(sts));

            //get the schematypes and add the document types to the type mapper
            SchemaType[] schemaType = sts.documentTypes();
            SchemaType type;
            for (int j = 0; j < schemaType.length; j++) {
                type = schemaType[j];
                mapper.addTypeMappingName(type.getDocumentElementName(),
                        type.getFullJavaName());
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
                if (Constants.BASE_64_CONTENT_QNAME.equals(sType.getPrimitiveType().getName())) {
                    outerType = sType.getOuterType();
                    //check the outer type further to see whether it has the contenttype attribute from
                    //XMime namespace
                    SchemaProperty[] properties = sType.getProperties();
                    for (int j = 0; j < properties.length; j++) {
                        if (Constants.XMIME_CONTENT_TYPE_QNAME.equals(properties[j].getName())) {
                            base64ElementQNamesList.add(outerType.getDocumentElementName());
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
     *
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
            findPlainBase64Types(stype, base64Types,new ArrayList());
        }

        return base64Types;
    }

    /**
     *
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
            if (!base64Types.contains(name) && !processedTypes.contains(schemaType.getName())){
                processedTypes.add(stype.getName());
                if (schemaType.isPrimitiveType()) {
                    SchemaType primitiveType = schemaType.getPrimitiveType();
                    if (Constants.BASE_64_CONTENT_QNAME.equals(primitiveType.getName())) {
                        base64Types.add(name);
                    }

                } else {
                    findPlainBase64Types(schemaType, base64Types,processedTypes);
                }
            }
        }


    }


    /**
     * Private class to generate the filer
     */
    private static class Axis2Filer implements Filer {

        private File location;

        private Axis2Filer(File loc) {
            location = loc;
        }

        public OutputStream createBinaryFile(String typename)
                throws IOException {
            File resourcesDirectory = new File(location, "resources");
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
            File outputDir = new File(location, "src");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File file = new File(outputDir,
                    typename + ".java");
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
            if (this.uri2packageMappings==null){
                //make an empty one to avoid nasty surprises
                this.uri2packageMappings = new HashMap();
            }
        }

        public String lookupPackageForNamespace(String uri) {
            if (uri2packageMappings.containsKey(uri)){
                return (String)uri2packageMappings.get(uri);
            }else{
                return URLProcessor.makePackageName(uri);
            }

        }
    }

    /**
    *
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
            }else if (s.getTargetNamespace()==null){
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
            }else if(s.getTargetNamespace()==null){
                uniqueSchemas.add(s);
            }
        }
        return (SchemaDocument.Schema[])
                uniqueSchemas.toArray(
                        new SchemaDocument.Schema[uniqueSchemas.size()]);
    }

    /**
     * Implementation of the entity resolver
     */
    private static class Axis2EntityResolver implements EntityResolver {
        private SchemaDocument[] schemas;
        private String baseUri;

        public Axis2EntityResolver() {
        }

        /**
         * @see EntityResolver#resolveEntity(String, String)
         * @param publicId  - this is the target namespace
         * @param systemId  - this is the location (value of schemaLocation)
         * @return
         */
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,IOException{
            //System.out.println("Lookup:" + "[ " + publicId + "]" + "[" + systemId + "]");
            try {
                for (int i = 0; i < schemas.length; i++) {
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
                if(systemId.indexOf(':') == -1) {
                    File f;
                    if (baseUri!=null){
                        f=new File(new URI(baseUri+systemId));
                    }else{
                        f = new File(systemId);
                    }
                    if(f.exists()) {
                        try {
                            return new InputSource(new FileInputStream(f));
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                return XMLUtils.getEmptyInputSource();
            } catch (URISyntaxException e) {
               throw new SAXException(e);
            }catch (Exception e){
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
}



