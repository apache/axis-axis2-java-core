package org.apache.axis2.wsdl.codegen.extension;

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

import org.apache.axis.xsd.xml.schema.XmlSchema;
import org.apache.axis.xsd.xml.schema.XmlSchemaCollection;
import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;

import java.util.*;

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

            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);
                Vector xmlObjectsVector = new Vector();
                XmlSchemaCollection schemaColl = new XmlSchemaCollection();
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
                        xmlObjectsVector.add(thisSchema);
                    }
                }

                //create the type mapper
                JavaTypeMapper mapper = new JavaTypeMapper();
                int length = 0;
                for (int j = 0; j < length; j++) {
//                    mapper.addTypeMapping();
                }
                //set the type mapper to the config
                configuration.setTypeMapper(mapper);

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
