package org.apache.axis.wsdl.codegen.extension;

import org.apache.axis.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis.wsdl.databinding.JavaTypeMapper;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.xmlbeans.*;
import org.w3c.dom.Element;

import java.io.*;
import java.util.List;

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
public class XMLBeansExtension extends AbstractCodeGenerationExtension implements CodeGenExtension {
    private static final String DEFUALT_STS_NAME = "foo";


    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {
        WSDLTypes typesList = configuration.getWom().getTypes();
        if (typesList==null){
            //there are no types to be code generated
            return;
        }
        List typesArray = typesList.getExtensibilityElements();
        WSDLExtensibilityElement extensiblityElt = null;
        XmlObject[] xmlObjects=new XmlObject[typesArray.size()];

        for (int i = 0; i < typesArray.size(); i++) {
            extensiblityElt =  (WSDLExtensibilityElement)typesArray.get(i);
            if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {
                try {
                    Element schemaElement = ((Schema)extensiblityElt).getElelment();
//                    //add the namespaces
                    XmlOptions options = new XmlOptions();
                    options.setLoadAdditionalNamespaces(configuration.getWom().getNamespaces());
                    //options.
                    xmlObjects[i] = XmlObject.Factory.parse(schemaElement,options);
                } catch (XmlException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        final File outputFolder =  configuration.getOutputLocation();

        try {
            SchemaTypeSystem sts = XmlBeans.compileXmlBeans(DEFUALT_STS_NAME, null,
                    xmlObjects,
                    new BindingConfig(), XmlBeans.getContextTypeLoader(),
                    new Filer() {
                        public OutputStream createBinaryFile(String typename)
                                throws IOException {
                            File file = new File(outputFolder,typename);
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                            return new FileOutputStream(file);
                        }

                        public Writer createSourceFile(String typename)
                                throws IOException {
                            typename = typename.replace('.',File.separatorChar);
                            File file = new File(outputFolder,typename+".java");
                            file.getParentFile().mkdirs();
                            file.createNewFile();
                            return new FileWriter(file);
                        }
                    }, null);

            //create the type mapper
            JavaTypeMapper mapper = new JavaTypeMapper();
            SchemaType[] types= sts.documentTypes();

            for (int i= 0; i < types.length; i++){
                //System.out.println("type name = " + types[i].getFullJavaImplName()+" "+types[i].getDocumentElementName());
                mapper.addTypeMapping(types[i].getDocumentElementName(), types[i].getFullJavaName());
            }
            //set the type mapper to the config
            configuration.setTypeMapper(mapper);

        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
    }



}
