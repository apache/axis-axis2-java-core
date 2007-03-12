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

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.i18n.CodegenMessages;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.jaxme.generator.impl.GeneratorImpl;
import org.apache.ws.jaxme.generator.sg.GroupSG;
import org.apache.ws.jaxme.generator.sg.ObjectSG;
import org.apache.ws.jaxme.generator.sg.SchemaSG;
import org.apache.ws.jaxme.generator.sg.TypeSG;
import org.apache.ws.jaxme.generator.sg.impl.JAXBSchemaReader;
import org.apache.ws.jaxme.js.JavaQName;
import org.apache.ws.jaxme.xs.xml.XsQName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;

public class JaxMeExtension extends AbstractDBProcessingExtension {
    public static final String SCHEMA_FOLDER = "schemas";

    public static String MAPPINGS = "mappings";
    public static String MAPPING = "mapping";
    public static String MESSAGE = "message";
    public static String JAVA_NAME = "javaclass";

    public static final String MAPPING_FOLDER = "Mapping";
    public static final String MAPPER_FILE_NAME = "mapper";
    public static final String SCHEMA_PATH = "/org/apache/axis2/wsdl/codegen/schema/";

    boolean debug = false;

    public void engage(CodeGenConfiguration configuration) {

        //test the databinding type. If not just fall through
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }

        try {
            //get the types from the types section
            List typesList = new ArrayList();
            List axisServices = configuration.getAxisServices();
            AxisService axisService = null;
            for (Iterator iter = axisServices.iterator();iter.hasNext();){
                axisService = (AxisService) iter.next();
                typesList.addAll(axisService.getSchema());
            }

            //check for the imported types. Any imported types are supposed to be here also
            if (typesList == null || typesList.isEmpty()) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                configuration.setTypeMapper(new DefaultTypeMapper());
                return;
            }


            Vector xmlObjectsVector = new Vector();
             //create the type mapper
        //First try to take the one that is already there
        TypeMapper mapper = configuration.getTypeMapper();
        if (mapper==null){
            mapper =new JavaTypeMapper();
        }

            for (int i = 0; i < typesList.size(); i++) {
                XmlSchema schema = (XmlSchema) typesList.get(i);
                xmlObjectsVector.add(new InputSource(new StringReader(getSchemaAsString(schema))));
            }

//            TODO: FIXME            
//            Element[] additionalSchemas = loadAdditionalSchemas();
//            // Need to add the third party schemas
//            for (int i = 0; i < additionalSchemas.length; i++) {
//                String s = DOM2Writer.nodeToString(additionalSchemas[i]);
//                xmlObjectsVector.add(new InputSource(new StringReader(s)));
//            }

            File outputDir = new File(configuration.getOutputLocation(),configuration.getSourceLocation());

            JAXBSchemaReader reader = new JAXBSchemaReader();
            reader.setSupportingExtensions(true);

            GeneratorImpl generator = new GeneratorImpl();
            generator.setTargetDirectory(outputDir);
            generator.setForcingOverwrite(false);
            generator.setSchemaReader(reader);

            for (int i = 0; i < xmlObjectsVector.size(); i++) {
                SchemaSG sg = generator.generate((InputSource) xmlObjectsVector.elementAt(i));
                ObjectSG[] elements = sg.getElements();
                for (int j = 0; j < elements.length; j++) {
                    XsQName qName = elements[j].getName();
                    JavaQName name = elements[j].getClassContext().getXMLInterfaceName();
                    mapper.addTypeMappingName(new QName(qName.getNamespaceURI(), qName.getLocalName()),
                            name.getPackageName() + '.' + name.getClassName());
                }
                TypeSG[] types = sg.getTypes();
                for (int j = 0; j < types.length; j++) {
                    XsQName qName = types[j].getName();
                    JavaQName name = types[j].getRuntimeType();
                    mapper.addTypeMappingName(new QName(qName.getNamespaceURI(), qName.getLocalName()),
                            name.getPackageName() + '.' + name.getClassName());
                }
                GroupSG[] groups = sg.getGroups();
                for (int j = 0; j < groups.length; j++) {
                    XsQName qName = groups[j].getName();
                    JavaQName name = groups[j].getClassContext().getXMLInterfaceName();
                    mapper.addTypeMappingName(new QName(qName.getNamespaceURI(), qName.getLocalName()),
                            name.getPackageName() + '.' + name.getClassName());
                }
            }

            //set the type mapper to the config
            configuration.setTypeMapper(mapper);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Loading the external schemas.
     *
     * @return element array consisting of the the DOM element objects that represent schemas
     */
    private Element[] loadAdditionalSchemas() {
        //load additional schemas
        String[] schemaNames = ConfigPropertyFileLoader.getThirdPartySchemaNames();
        Element[] schemaElements;

        try {
            ArrayList additionalSchemaElements = new ArrayList();
            DocumentBuilder documentBuilder = getNamespaceAwareDocumentBuilder();
            for (int i = 0; i < schemaNames.length; i++) {
                //the location for the third party schema;s is hardcoded
                if (!"".equals(schemaNames[i].trim())) {
                    InputStream schemaStream = this.getClass().getResourceAsStream(SCHEMA_PATH + schemaNames[i]);
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
            throw new RuntimeException(CodegenMessages.getMessage("extension.additionalSchemaFailure"), e);
        }

        return schemaElements;
    }

    private DocumentBuilder getNamespaceAwareDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory.newDocumentBuilder();
    }


    private String getSchemaAsString(XmlSchema schema) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.write(baos);
        return baos.toString();
    }
}

