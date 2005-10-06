package org.apache.axis2.databinding.extensions.XMLBeans;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.extensions.SchemaUtility;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMNode;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 * @author : Eran Chinthaka (chinthaka@apache.org)
 */

public class XMLBeansSchemaUtility implements SchemaUtility {
    protected Log log = LogFactory.getLog(getClass());

    public boolean isRelevant(ServiceDescription serviceDescription) throws AxisFault {
        try {
            ClassLoader classLoader = serviceDescription.getClassLoader();
            String serviceClassName = (String) serviceDescription.getParameter("ServiceClass").getValue();

            Class serviceImplementation = classLoader.loadClass(serviceClassName);

            // get each and every method
            Method[] methods = serviceImplementation.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                // get the parameters for the method
                Class[] methodParameterTypes = method.getParameterTypes();
                for (int j = 0; j < methodParameterTypes.length; j++) {
                    Class parameter = methodParameterTypes[j];

                    if (isExtendsFromGivenBaseClass(parameter, XmlObject.class)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            log.error("Can not load the service " + serviceDescription + " from the given class loader");
            throw new AxisFault(e);
        }
    }

    public OMElement getSchema(ServiceDescription serviceDescription) throws AxisFault {
        if (!isRelevant(serviceDescription)) {
            return null;
        }


        try {
            File file = new File(serviceDescription.getFileName());
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));

            ZipEntry entry;
            String entryName = "";
            OMElement schemaElement = null;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryName = entry.getName();
                if (entryName.startsWith(XMLBeansExtension.SCHEMA_FOLDER) && entryName.endsWith(".xsd")) {
                    InputStream schementry = serviceDescription.getClassLoader().getResourceAsStream(entryName);
                    StAXOMBuilder builder = new StAXOMBuilder(schementry);
                    if (schemaElement == null) {
                        schemaElement = builder.getDocumentElement();
                    } else {
                        Iterator children = builder.getDocumentElement().getChildren();
                        while (children.hasNext()) {
                            schemaElement.addChild((OMNode) children.next());
                        }
                        Iterator allDeclaredNamespaces = builder.getDocumentElement().getAllDeclaredNamespaces();
                        while (allDeclaredNamespaces.hasNext()) {
                            OMNamespace omNamespace = (OMNamespace) allDeclaredNamespaces.next();
                            schemaElement.declareNamespace(omNamespace);
                        }
                    }

                }
            }
            return schemaElement;
        } catch (IOException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }

    }

    private boolean isExtendsFromGivenBaseClass(Class classNeededToBeChecked, Class baseClass) {
        if (classNeededToBeChecked == baseClass) {
            return true;
        } else {
            Class[] interfaces = classNeededToBeChecked.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] == baseClass) {
                    return true;
                }

            }
            return false;


        }


    }
}
