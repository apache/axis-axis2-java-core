package org.apache.axis2.databinding.extensions.XMLBeans;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.extensions.SchemaUtility;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

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

        ArrayList mainClassListForSchemaGeneration = new ArrayList();

        try {
            ClassLoader classLoader = serviceDescription.getClassLoader();
            String serviceClassName = (String) serviceDescription.getParameter("ServiceClass").getValue();

            Class serviceImplementation = classLoader.loadClass(serviceClassName);

            // get each and every method
            Method[] methods = serviceImplementation.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method serviceImplClassMethods = methods[i];

                // get the parameters for the serviceImplClassMethods
                Class[] methodParameterTypes = serviceImplClassMethods.getParameterTypes();
                for (int j = 0; j < methodParameterTypes.length; j++) {
                    Class parameter = methodParameterTypes[j];
                    if (isExtendsFromGivenBaseClass(parameter, XmlObject.class)) {
                        Class aClass = classLoader.loadClass(parameter.getName() + "$Factory");
                        Method newInstanceMethod = aClass.getMethod("newInstance", null);
                        System.out.println(newInstanceMethod.invoke(parameter, null).getClass());
                        XmlObject xmlObject = (XmlObject) newInstanceMethod.invoke(parameter, null);
                        if (xmlObject.schemaType().isDocumentType()) {
                            mainClassListForSchemaGeneration.add(xmlObject);
                        }
                    }
                }
            }

            // now we have all the document type obejcts in the ArrayList. Now generate the schema
            // from that
            Iterator documentTypeElementIter = mainClassListForSchemaGeneration.iterator();
            while (documentTypeElementIter.hasNext()) {
                XmlObject xmlObject = (XmlObject) documentTypeElementIter.next();
//                String sourceName = xmlObject.schemaType().getSourceName();
//                XmlCursor xCur = xmlObject.newCursor();
//
//                if (xCur.toFirstContentToken() == XmlCursor.TokenType.START) {
//                do {
//                    Node n = xCur.getDomNode();
//                    System.out.println("n = " + n);
//                    if (n.getNodeType() == Node.ELEMENT_NODE) {
//                        System.out.println(((Element) n));
//                    }
//                } while (xCur.toNextSibling());

//            }

                File file = new File("test/test2");
                if(!file.exists()){
                    file.delete();
                    file.createNewFile();
                }

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                xmlObject.save(byteArrayOutputStream);
                String str = new String(byteArrayOutputStream.toByteArray());
                System.out.println("str = " + str);
//                xmlObject.schemaType().getTypeSystem().
//                SchemaType[] schemaTypes = xmlObject.schemaType().getTypeSystem().documentTypes();
//                for (int i = 0; i < schemaTypes.length; i++) {
//                    SchemaType schemaType = schemaTypes[i];
//                    schemaType.
//
//                }
//                InputStream is = classLoader.getResourceAsStream("schemaorg_apache_xmlbeans/src/" + sourceName);
//                String str = new String(getBytesFromFile(is));
//                System.out.println("str = " + str);

            }

        } catch (ClassNotFoundException e) {
            log.error("Can not load the service " + serviceDescription + " from the given class loader");
            throw new AxisFault(e);
        } catch (NoSuchMethodException e) {
            throw new AxisFault(e);
        } catch (IllegalAccessException e) {
            throw new AxisFault(e);
        } catch (InvocationTargetException e) {
            throw new AxisFault(e);
        } catch (IOException e) {
            throw new AxisFault(e);

        }
        return null;
    }

    public static byte[] getBytesFromFile(InputStream is) throws IOException {

        // Get the size of the file

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
//        if (length > Integer.MAX_VALUE) {
//            // File is too large
//        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[55555];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read the input stream ");
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
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
