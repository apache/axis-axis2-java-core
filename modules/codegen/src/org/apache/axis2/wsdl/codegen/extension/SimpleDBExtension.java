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
import org.apache.axis2.databinding.schema.SchemaCompiler;
import org.apache.axis2.databinding.schema.CompilerOptions;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

/**
 * Work in progress to test simple DataBinding with the XmlSchema lib
 *
 */
public class SimpleDBExtension extends AbstractCodeGenerationExtension {
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
            Vector xmlSchemaTypeVector = new Vector();
            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);

                XmlSchemaCollection schemaColl = new XmlSchemaCollection();
                //add the namespace map here. it is absolutely needed
                Schema schema = null;

                if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {
                    schema = (Schema) extensiblityElt;
                    Map inScopeNS = configuration.getWom().getNamespaces();
                    for (Iterator it = inScopeNS.keySet().iterator(); it.hasNext();) {
                        String prefix = (String) it.next();
                        schemaColl.mapNamespace(prefix,
                                (String)inScopeNS.get(prefix));
                    }

                    Stack importedSchemaStack = schema.getImportedSchemaStack();
                    //compile these schemas
                    while (!importedSchemaStack.isEmpty()) {
                        Element el = ((javax.wsdl.extensions.schema.Schema)importedSchemaStack.pop()).getElement();
                        XmlSchema thisSchema = schemaColl.read(el);
                        xmlSchemaTypeVector.add(thisSchema);
                    }
                }

                //call the schema compiler
                CompilerOptions options = new CompilerOptions().setOutputLocation(configuration.getOutputLocation());
                SchemaCompiler schemaCompiler = new SchemaCompiler(options);
                schemaCompiler
                        .compile(xmlSchemaTypeVector);

                //create the type mapper
                JavaTypeMapper mapper = new JavaTypeMapper();
                //get the processed element map and transfer it to the type mapper
                Map processedMap = schemaCompiler.getProcessedElementmap();
                Iterator keys = processedMap.keySet().iterator();
                QName key;
                while (keys.hasNext()) {
                   key =(QName)keys.next();
                   mapper.addTypeMapping(key,processedMap.get(key));
                }

                //set the type mapper to the config
                configuration.setTypeMapper(mapper);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
