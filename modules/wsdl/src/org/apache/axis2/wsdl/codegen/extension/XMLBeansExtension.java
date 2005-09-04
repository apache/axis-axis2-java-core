package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.XSLTConstants;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.util.ConfigPropertyFileLoader;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.xmlbeans.*;
import org.w3c.dom.Element;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

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
*
*
*/

public class XMLBeansExtension extends AbstractCodeGenerationExtension {
    private static final String DEFAULT_STS_NAME = "foo";


    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {

        //test the databinding type. If not just fall through
        if (configuration.getDatabindingType()!= XSLTConstants.DataBindingTypes.XML_BEANS){
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
            WSDLExtensibilityElement extensiblityElt;
            SchemaTypeSystem sts;
            Vector xmlObjectsVector= new Vector();
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
                    //compile these schemas
                    while (!importedSchemaStack.isEmpty()){
                        javax.wsdl.extensions.schema.Schema tempSchema = (javax.wsdl.extensions.schema.Schema) importedSchemaStack.pop();
                        Element element = tempSchema.getElement();
                        xmlObjectsVector.add(
                                XmlObject.Factory.parse(
                                        element
                                        ,options));


                    }

                }

            }

            // add the third party schemas
            for (int i = 0; i < additionalSchemas.length; i++) {
                xmlObjectsVector.add(XmlObject.Factory.parse(
                        additionalSchemas[i]
                        ,null));
            }

            //compile the type system
            sts = XmlBeans.compileXmlBeans(DEFAULT_STS_NAME, null,
                    convertToXMLObjectArray(xmlObjectsVector),
                    new BindingConfig(), XmlBeans.getContextTypeLoader(),
                    new Axis2Filer(),
                    null);

            SchemaType[] schemaType = sts.documentTypes();
            SchemaType type;
            for (int j = 0; j < schemaType.length; j++) {
                type = schemaType[j];
                mapper.addTypeMapping(type.getDocumentElementName(),
                        type.getFullJavaName());
            }
            //set the type mapper to the config
            configuration.setTypeMapper(mapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Loading the external schemas.
     * @return element array consisting of the the DOM element objects that represent schemas
     *
     */
    private Element[] loadAdditionalSchemas() {
        //load additional schemas
        String[] schemaNames = ConfigPropertyFileLoader.getThirdPartySchemaNames();
        Element[] schemaElements = null;

        try {
            ArrayList additionalSchemaElements = new ArrayList();
            DocumentBuilder documentBuilder = getNamespaceAwareDocumentBuilder();
            for (int i = 0; i < schemaNames.length; i++) {
                InputStream schemaStream = this.getClass().getResourceAsStream("/org/apache/axis2/wsdl/codegen/schema/"+ schemaNames[i]);
                Document doc = documentBuilder.parse(schemaStream);
                additionalSchemaElements.add(doc.getDocumentElement());
            }

            //Create the Schema element array
            schemaElements = new Element[additionalSchemaElements.size()];
            for (int i = 0; i < additionalSchemaElements.size(); i++) {
                schemaElements[i] = (Element) additionalSchemaElements.get(i);

            }
        } catch (Exception e) {
            throw new RuntimeException("Additional schema loading failed!!",e);
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
    private void checkCompatibility(){
        Map bindingMap = this.configuration.getWom().getBindings();
        Collection col = bindingMap.values();

        for (Iterator iterator = col.iterator(); iterator.hasNext();) {
            WSDLBinding b = (WSDLBinding)iterator.next();
            HashMap bindingOps = b.getBindingOperations();
            Collection bindingOpsCollection = bindingOps.values();
            for (Iterator iterator1 = bindingOpsCollection.iterator(); iterator1.hasNext();) {
                foo((WSDLBindingOperation)iterator1.next());
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
    private XmlObject[] convertToXMLObjectArray(Vector vec){
        XmlObject[] xmlObjects = new XmlObject[vec.size()];
        for (int i = 0; i < vec.size(); i++) {
            xmlObjects[i] = (XmlObject)vec.get(i);
        }
        return xmlObjects;
    }
    /**
     * Private class to generate the filer
     */
    private class Axis2Filer implements Filer{

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
}

