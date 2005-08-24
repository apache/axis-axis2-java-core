package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.codegen.XSLTConstants;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.wsdl.*;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.wsdl.extensions.SOAPBody;
import org.apache.xmlbeans.*;

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

        //test whether the TCCL has the Xbeans classes
        //ClassLoader cl = Thread.currentThread().getContextClassLoader();


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
            //Vector schemabaseURIList=new Vector();
            Vector xmlObjectsVector= new Vector();
            //String schemaBaseURI ;

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
                        xmlObjectsVector.add(
                                XmlObject.Factory.parse(
                                        tempSchema.getElement()
                                        ,options));

                    }


                }

            }
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
            if (element.getType().equals(ExtensionConstants.SOAP_BODY)) {
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

