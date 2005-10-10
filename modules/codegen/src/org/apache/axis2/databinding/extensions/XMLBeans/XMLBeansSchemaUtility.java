package org.apache.axis2.databinding.extensions.XMLBeans;

import com.ibm.wsdl.DefinitionImpl;
import com.ibm.wsdl.InputImpl;
import com.ibm.wsdl.MessageImpl;
import com.ibm.wsdl.OperationImpl;
import com.ibm.wsdl.OutputImpl;
import com.ibm.wsdl.PartImpl;
import com.ibm.wsdl.PortTypeImpl;
import com.ibm.wsdl.TypesImpl;
import com.ibm.wsdl.extensions.schema.SchemaImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.extensions.SchemaUtility;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
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
    private Definition definition;

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

    public void fillInformationFromServiceDescription(ServiceDescription serviceDescription, Definition definition) throws AxisFault {
        this.definition = definition;

        // first fill the schema information
        getSchema(serviceDescription);

        // now fill port type and message elements
        createMessagesAndPortTypes(serviceDescription);
    }

    public Definition fillInformationFromServiceDescription(ServiceDescription serviceDescription) throws AxisFault {
        this.definition = new DefinitionImpl();
        this.fillInformationFromServiceDescription(serviceDescription, this.definition);
        return definition;
    }

    private void getSchema(ServiceDescription serviceDescription) throws AxisFault {
        if (!isRelevant(serviceDescription)) {
            return;
        }


        try {
            File file = new File(serviceDescription.getFileName());
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));

            ZipEntry entry;
            String entryName = "";

            Schema schema;
            Types types = new TypesImpl();
            definition.setTypes(types);

            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryName = entry.getName();
                if (entryName.startsWith(XMLBeansExtension.SCHEMA_FOLDER) && entryName.endsWith(".xsd")) {
                    InputStream schemaEntry = serviceDescription.getClassLoader().getResourceAsStream(entryName);
                    schema = new SchemaImpl();
                    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(schemaEntry);
                    schema.setElement(document.getDocumentElement());
                    types.addExtensibilityElement(schema);
                }
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        } catch (ParserConfigurationException e) {
            throw new AxisFault(e);
        } catch (SAXException e) {
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

    private void createMessagesAndPortTypes(ServiceDescription serviceDescription) throws AxisFault {

        HashMap mappings = readMappings(serviceDescription);


        try {
            ClassLoader classLoader = serviceDescription.getClassLoader();
            String serviceClassName = (String) serviceDescription.getParameter("ServiceClass").getValue();

            // create PortType with class name
            PortType portType = new PortTypeImpl();
            portType.setQName(serviceDescription.getName());
            definition.addPortType(portType);
            portType.setUndefined(false);

            Class serviceImplementation = classLoader.loadClass(serviceClassName);
            Method[] methods = serviceImplementation.getMethods();

            // add messages to the definition and operations to the port type
            Operation wsdlOperation;
            Input wsdlOperationInput;
            Output wsdlOperationOutput;

            Iterator operationDescIter = serviceDescription.getOperations().values().iterator();
            while (operationDescIter.hasNext()) {
                OperationDescription operation = (OperationDescription) operationDescIter.next();
                QName methodName = operation.getName();
                Method method = getMethod(methods, methodName.getLocalPart());

                // create operation
                wsdlOperation = new OperationImpl();
                wsdlOperation.setName(methodName.getLocalPart());
                wsdlOperation.setUndefined(false);

                // create Output message and add that to the definition
                Class returnType = method.getReturnType();
                Message message = getMessage(mappings, returnType);
                definition.addMessage(message);

                // add the same message as the output of the operation
                wsdlOperationOutput = new OutputImpl();
                wsdlOperationOutput.setMessage(message);
                wsdlOperation.setOutput(wsdlOperationOutput);

                Class[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class aClass = parameterTypes[i];
                    message = getMessage(mappings, aClass);
                    wsdlOperationInput = new InputImpl();
                    wsdlOperationInput.setMessage(message);
                    definition.addMessage(message);
                    wsdlOperation.setInput(wsdlOperationInput);
                }
                portType.addOperation(wsdlOperation);
            }


        } catch (ClassNotFoundException e) {
            log.error("Can not load the service " + serviceDescription + " from the given class loader");
            throw new AxisFault(e);
        }

    }

    private Message getMessage(HashMap mappings, Class returnType) {
        String mappingName = (String) mappings.get(returnType.getName());

        // First create message
        Message message = new MessageImpl();
        message.setQName(new QName(mappingName));

        // create the part of the message
        Part part = new PartImpl();
        part.setName("param");
        part.setElementName(new QName(mappingName));
        message.addPart(part);
        message.setUndefined(false);
        return message;
    }

    private HashMap readMappings(ServiceDescription serviceDescription) throws AxisFault {
        HashMap mappings = new HashMap();
        try {
            File file = new File(serviceDescription.getFileName());
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));

            ZipEntry entry;
            String entryName = "";
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryName = entry.getName();
                if (entryName.startsWith(XMLBeansExtension.MAPPING_FOLDER) && entryName.endsWith(".xml")) {
                    InputStream schemaEntry = serviceDescription.getClassLoader().getResourceAsStream(entryName);
                    OMElement mappingsElement = new StAXOMBuilder(schemaEntry).getDocumentElement();
                    Iterator mappingElementsIter = mappingsElement.getChildElements();
                    while (mappingElementsIter.hasNext()) {
                        OMElement mappingElement = (OMElement) mappingElementsIter.next();
                        String messageName = mappingElement.getFirstChildWithName(new QName(XMLBeansExtension.MESSAGE)).getText();
                        String javaclass = mappingElement.getFirstChildWithName(new QName(XMLBeansExtension.JAVA_NAME)).getText();
                        mappings.put(javaclass, messageName);
                    }
                }
            }
            return mappings;
        } catch (IOException e) {
            throw new AxisFault(e);
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    private Method getMethod(Method[] methods, String methodName) {
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equalsIgnoreCase(methodName)) {
                return method;
            }

        }
        return null;
    }
}
