package org.apache.axis2.databinding.extensions.XMLBeans;

import com.ibm.wsdl.DefinitionImpl;
import com.ibm.wsdl.InputImpl;
import com.ibm.wsdl.MessageImpl;
import com.ibm.wsdl.OperationImpl;
import com.ibm.wsdl.OutputImpl;
import com.ibm.wsdl.PartImpl;
import com.ibm.wsdl.PortTypeImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.extensions.SchemaUtility;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.wsdl.codegen.extension.XMLBeansExtension;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
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
            OMElement typesElement = OMAbstractFactory.getOMFactory().createOMElement("types", null);
            while ((entry = zipInputStream.getNextEntry()) != null) {
                entryName = entry.getName();
                if (entryName.startsWith(XMLBeansExtension.SCHEMA_FOLDER) && entryName.endsWith(".xsd")) {
                    InputStream schemaEntry = serviceDescription.getClassLoader().getResourceAsStream(entryName);
                    typesElement.addChild(new StAXOMBuilder(schemaEntry).getDocumentElement());

                }
            }
            return typesElement;
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

    public void createMessagesAndPortTypes(ServiceDescription serviceDescription) throws AxisFault {

        HashMap mappings = readMappings(serviceDescription);
        Definition definition = new DefinitionImpl();


        try {
            ClassLoader classLoader = serviceDescription.getClassLoader();
            String serviceClassName = (String) serviceDescription.getParameter("ServiceClass").getValue();

            PortType portType = new PortTypeImpl();
            portType.setQName(serviceDescription.getName());

            Class serviceImplementation = classLoader.loadClass(serviceClassName);
            Method[] methods = serviceImplementation.getMethods();

            Iterator operationDescIter = serviceDescription.getOperations().values().iterator();

            Operation wsdlOperation;
            Input wsdlOperationInput;
            Output wsdlOperationOutput;


            while (operationDescIter.hasNext()) {
                OperationDescription operation = (OperationDescription) operationDescIter.next();
                QName methodName = operation.getName();
                Method method = getMethod(methods, methodName.getLocalPart());

                Class returnType = method.getReturnType();

                wsdlOperation = new OperationImpl();
                wsdlOperation.setName(methodName.getLocalPart());

                Message message = getMessage(mappings, returnType);
                definition.addMessage(message);
                wsdlOperationOutput = new OutputImpl();
                wsdlOperationOutput.setMessage(message);

                Class[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class aClass = parameterTypes[i];
                    message = getMessage(mappings, aClass);
                    wsdlOperationInput = new InputImpl();
                    wsdlOperationInput.setMessage(message);
                    definition.addMessage(message);
                }
                portType.addOperation(wsdlOperation);
            }

            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            wsdlWriter.writeWSDL(definition, System.out);


        } catch (ClassNotFoundException e) {
            log.error("Can not load the service " + serviceDescription + " from the given class loader");
            throw new AxisFault(e);
        } catch (WSDLException e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }


    }

    private Message getMessage(HashMap mappings, Class returnType) {
        System.out.println("returnType = " + returnType);
        String mappingName = (String) mappings.get(returnType.getName());
        System.out.println("mappingName = " + mappingName);
        Message message = new MessageImpl();
        message.setQName(new QName(mappingName));
        Part part = new PartImpl();
        part.setName("param");
        part.setElementName(new QName(mappingName));
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
                        System.out.println(javaclass + ":" + messageName);
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
