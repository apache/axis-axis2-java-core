package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.apache.xmlbeans.BindingConfig;
import org.apache.xmlbeans.Filer;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
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

public class XMLBeansExtension extends AbstractCodeGenerationExtension {
    private static final String DEFAULT_STS_NAME = "foo";


    public void init(CodeGenConfiguration configuration) {
        this.configuration = configuration;
    }

    public void engage() {
        try {
            WSDLTypes typesList = configuration.getWom().getTypes();
            if (typesList == null) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                this.configuration.setTypeMapper(new DefaultTypeMapper());
                return;
            }
            List typesArray = typesList.getExtensibilityElements();
            WSDLExtensibilityElement extensiblityElt = null;
            XmlObject[] xmlObjects = new XmlObject[typesArray.size()];

            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);

                if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {

                    try {
                        Element schemaElement = ((Schema) extensiblityElt).getElelment();
                        //System.out.println("schemaElement = " + schemaElement);
                        XmlOptions options = new XmlOptions();
                        //options.setCompileDownloadUrls();//download imported URL's
                        options.setLoadAdditionalNamespaces(
                                configuration.getWom().getNamespaces()); //add the namespaces
                        //options.
                        xmlObjects[i] =
                                XmlObject.Factory.parse(schemaElement, options);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            final File outputFolder = configuration.getOutputLocation();

            try {

                //System.out.println(XmlBeans.loadXsd(xmlObjects));

                SchemaTypeSystem sts = XmlBeans.compileXmlBeans(DEFAULT_STS_NAME, null,
                        xmlObjects,
                        new BindingConfig(), XmlBeans.getContextTypeLoader(),
                        new Filer() {
                            public OutputStream createBinaryFile(String typename)
                                    throws IOException {
                                File file = new File(outputFolder, typename);
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                                return new FileOutputStream(file);
                            }

                            public Writer createSourceFile(String typename)
                                    throws IOException {
                                typename =
                                        typename.replace('.', File.separatorChar);
                                File file = new File(outputFolder,
                                        typename + ".java");
                                file.getParentFile().mkdirs();
                                file.createNewFile();
                                return new FileWriter(file);
                            }
                        }, new XmlOptions().setCompileDownloadUrls());

                //create the type mapper
                JavaTypeMapper mapper = new JavaTypeMapper();
                SchemaType[] types = sts.documentTypes();

                for (int i = 0; i < types.length; i++) {
                    mapper.addTypeMapping(types[i].getDocumentElementName(),
                            types[i].getFullJavaName());
                }
                //set the type mapper to the config
                configuration.setTypeMapper(mapper);

            } catch (XmlException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
