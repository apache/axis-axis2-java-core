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
package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.metadata.Attribute;
import org.apache.axis2.rmi.metadata.Type;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.metadata.Operation;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.util.Constants;
import org.apache.axis2.rmi.util.JavaTypeToQNameMap;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.databinding.utils.ConverterUtil;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;


public class XmlStreamParser {

    private Configurator configurator;
    private Map qNameToTypeMap;

    public XmlStreamParser(Map processedTypeMap,
                           Configurator configurator,
                           Map schemaMap) {
        this.configurator = configurator;
        try {
            populateQNameToTypeMap(processedTypeMap, schemaMap);
        } catch (MetaDataPopulateException e) {
            // TODO: what to do this exceptions is not going to happen
        } catch (SchemaGenerationException e) {
            // TODO: what to do thsi exceptions is not going to happen
        }
    }

    private void populateQNameToTypeMap(Map processedTypeMap, Map schemaMap)
            throws MetaDataPopulateException,
            SchemaGenerationException {
        // first add all the proceced type map values
        Set defaultTypeMapKeys = JavaTypeToQNameMap.getKeys();
        Class typeClass;
        for (Iterator iter = defaultTypeMapKeys.iterator(); iter.hasNext();) {
            typeClass = (Class) iter.next();
            if (!processedTypeMap.containsKey(typeClass)) {
                Type newType = new Type(typeClass);
                processedTypeMap.put(typeClass, newType);
                newType.populateMetaData(this.configurator, processedTypeMap);
                newType.generateSchema(this.configurator, schemaMap);
            }
        }
        this.qNameToTypeMap = new HashMap();
        Type type = null;
        for (Iterator iter = processedTypeMap.values().iterator(); iter.hasNext();) {
            type = (Type) iter.next();
            this.qNameToTypeMap.put(type.getXmlType().getQname(), type);
        }

    }

    public Object getOutputObject(XMLStreamReader reader,
                                  Operation operation)
            throws XMLStreamException, XmlParsingException {
        Object returnObject = null;
        // first we have to point to the reader to the begining of the element
        while (!reader.isStartElement() && !reader.isEndElement()) {
            reader.next();
        }

        // first check whether we have got the correct input element or not
        if (reader.getLocalName().equals(operation.getOutPutElement().getName()) &&
                reader.getNamespaceURI().equals(operation.getOutPutElement().getNamespace())) {
            reader.next();
            if (operation.getOutputParameter() != null) {
                // i.e this is not a void return type
                returnObject = getObjectForParameter(reader, operation.getOutputParameter());
            }
        }
        return returnObject;
    }

    public Object[] getInputParameters(XMLStreamReader reader,
                                       Operation operation)
            throws XMLStreamException,
            XmlParsingException {

        List returnObjects = new ArrayList();
        // first we have to point to the reader to the begining for the element
        while (!reader.isStartElement() && !reader.isEndElement()) {
            reader.next();
        }

        // first check whether we have got the correct input element or not
        if (reader.getLocalName().equals(operation.getInputElement().getName()) &&
                reader.getNamespaceURI().equals(operation.getInputElement().getNamespace())) {
            // point the reader to parameters
            reader.next();
            Parameter parameter = null;
            List inputParameters = operation.getInputParameters();
            QName parameterQName = null;

            for (Iterator iter = inputParameters.iterator(); iter.hasNext();) {
                parameter = (Parameter) iter.next();
                parameterQName = new QName(parameter.getNamespace(), parameter.getName());
                returnObjects.add(getObjectForParameter(reader, parameter));
                // if the reader is at the end of this parameter
                // then we move it to next element.
                if (reader.isEndElement() && reader.getName().equals(parameterQName)){
                    reader.next();
                }
            }

        } else {
            throw new XmlParsingException("Unexpected Subelement " + reader.getName() + " but " +
                    "expected " + operation.getInputElement().getName());
        }
        return returnObjects.toArray();
    }

    /**
     * parameter has the same logic as the attribute. so reader pre and post conditions same.
     *
     * @param reader
     * @param parameter
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    public Object getObjectForParameter(XMLStreamReader reader,
                                        Parameter parameter)
            throws XMLStreamException,
            XmlParsingException {

        QName parameterQName = new QName(parameter.getNamespace(), parameter.getName());
        return getObjectForElement(reader,
                parameterQName,
                parameter.getType(),
                parameter.isArray(),
                parameter.getElement().isNillable(),
                parameter.getElement().isMinOccurs0(),
                parameter.getClassType(),
                parameter.getJavaClass());

    }

    /**
     * when calls to this method reader must point to the start of the type.
     * if it is a simple type it points the the text
     * if it is a complex type it points to the start element of the first attribute. and we returning
     * from the method it should point to the end element of the last parameter.
     *
     * @param reader
     * @param type
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */


    public Object getObjectForType(XMLStreamReader reader,
                                   Type type)
            throws XMLStreamException,
            XmlParsingException {

        try {
            Object returnObject = null;
            if (type.getXmlType().isSimpleType()) {
                // this is a simple known type for us
                // constructor should be able to invoke with the string.
                if (type.getJavaClass().equals(Object.class)) {
                    returnObject = new Object();
                } else {
                    // find the object for this string using converter util classs
                    String methodName = null;
                    try {
                        methodName = getMethodName(type.getJavaClass().getName());
                        Method  methodToInvoke = ConverterUtil.class.getMethod(methodName,new Class[]{String.class});
                        returnObject = methodToInvoke.invoke(null,new Object[]{reader.getText()});
                    } catch (NoSuchMethodException e) {
                        throw new XmlParsingException("Can not invoke the converter util class method " + methodName, e);
                    }
                }
            } else {
                // first we have to point to the reader to the begining for the element
                while (!reader.isStartElement() && !reader.isEndElement()) {
                    reader.next();
                }
                // this is a complex type
                returnObject = type.getJavaClass().newInstance();
                // we have to get all the attribute and populate them
                List attributes = type.getAllAttributes();
                Attribute attribute;
                Object attributeObject;
                QName attributeQName = null;

                for (Iterator iter = attributes.iterator(); iter.hasNext();) {
                    attribute = (Attribute) iter.next();
                    attributeObject = getObjectForAttribute(reader, attribute);
                    attributeQName = new QName(attribute.getNamespace(), attribute.getName());
                    if (attributeObject != null) {
                        attribute.getSetterMethod().invoke(returnObject, new Object[]{attributeObject});
                    }
                    // if the reader is at the end of this attribute
                    // then we move it to next element.
                    if (reader.isEndElement() && reader.getName().equals(attributeQName)){
                        reader.next();
                    }

                }

            }
            return returnObject;
        } catch (InstantiationException e) {
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            throw new XmlParsingException("Constructor invoking exception for type " + type.getName());
        }

    }

    private String getMethodName(String className) {
        // first handle some exceptional casses
        if (className.equals(Integer.class.getName())) {
            return "convertToInt";
        } else if (className.equals(Calendar.class.getName())) {
            return "convertToDateTime";
        }
        if (className.indexOf(".") > -1) {
            className = className.substring(className.lastIndexOf(".") + 1);
        } else {
            // this is a primitive type class
            // so capitalize the firt letter
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
        }
        return "convertTo" + className;
    }

    /**
     * give the relavent object for attribute.
     *
     * @param reader
     * @param attribute
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    private Object getObjectForAttribute(XMLStreamReader reader,
                                         Attribute attribute)
            throws XMLStreamException,
            XmlParsingException {
        QName attributeQName = new QName(attribute.getNamespace(), attribute.getName());
        return getObjectForElement(reader,
                attributeQName,
                attribute.getType(),
                attribute.isArray(),
                attribute.getElement().isNillable(),
                attribute.getElement().isMinOccurs0(),
                attribute.getClassType(),
                attribute.getPropertyDescriptor().getPropertyType());

    }

    /**
     * element parser corresponds to parse the element (for an attribute or parameter) correctly.
     * when calling to this method reader must point to the start element of the elemetnt. and when returning
     * it should point to the corresponding end eleemnt of the element.
     * if the element is an array it may point to the start element of the next element.
     *
     * @param reader
     * @param elementQName
     * @param elementType
     * @param isArray
     * @param classType
     * @param javaClass - if this is a list javaClass for the list
     * @return
     * @throws XMLStreamException
     * @throws XmlParsingException
     */

    private Object getObjectForElement(XMLStreamReader reader,
                                       QName elementQName,
                                       Type elementType,
                                       boolean isArray,
                                       boolean isNillable,
                                       boolean isMinOccurs0,
                                       int classType,
                                       Class javaClass)
            throws XMLStreamException,
            XmlParsingException {
        // first we have to point to the reader to the begining for the element
        while (!reader.isStartElement() && !reader.isEndElement()) {
            reader.next();
        }

        // first validate the attribute
        if (reader.getName().equals(elementQName)) {

            String nillble = reader.getAttributeValue(Constants.URI_DEFAULT_SCHEMA_XSI, "nil");
            // actual element type may be different from the element type given
            // if extensions has used.
            Type actualElementType = elementType;
            QName typeQName = getTypeQName(reader);
            if ((typeQName != null) && !elementType.getXmlType().getQname().equals(typeQName)) {
                // i.e this is an extension type
                if (this.qNameToTypeMap.containsKey(typeQName)) {
                    actualElementType = (Type) this.qNameToTypeMap.get(typeQName);
                } else {
                    throw new XmlParsingException("Unknown type found ==> " + typeQName);
                }
            }
            // point to the complex type elements
            if (isArray) {
                Collection objectsCollection = getCollectionObject(classType, javaClass);

                // read the first element
                if ("true".equals(nillble) || "1".equals(nillble)) {
                    // this is a nill attribute
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    if (isNillable){
                       objectsCollection.add(null);
                    } else {
                       throw new XmlParsingException("Element " + elementQName + " can not be null");
                    }

                } else {
                    reader.next();
                    objectsCollection.add(getObjectForType(reader, actualElementType));

                    // we have to move the cursor until the end element of this attribute
                    while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                        reader.next();
                    }
                }
                boolean loop = true;
                while (loop) {
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    reader.next();
                    // now we are at the end element of the first element
                    while (!reader.isStartElement() && !reader.isEndElement()) {
                        reader.next();
                    }

                    // in this step if it is an end element we have found an end element
                    // so have to exit from the loop
                    if (reader.isEndElement()) {
                        loop = false;
                    } else {
                        // now it should be in a start element
                        // check whether still we read the original element attributes. otherwise return
                        if (reader.getName().equals(elementQName)) {
                            nillble = reader.getAttributeValue(Constants.URI_DEFAULT_SCHEMA_XSI, "nil");
                            // since this is a new element we check for extensions
                            actualElementType = elementType;
                            typeQName = getTypeQName(reader);
                            if ((typeQName != null) && !elementType.getXmlType().getQname().equals(typeQName)) {
                                // i.e this is an extension type
                                if (this.qNameToTypeMap.containsKey(typeQName)) {
                                    actualElementType = (Type) this.qNameToTypeMap.get(typeQName);
                                } else {
                                    throw new XmlParsingException("Unknown type found ==> " + typeQName);
                                }
                            }
                            if ("true".equals(nillble) || "1".equals(nillble)) {
                                // this is a nill attribute
                                while (!reader.isEndElement()) {
                                    reader.next();
                                }
                                if (isNillable) {
                                    objectsCollection.add(null);
                                } else {
                                    throw new XmlParsingException("Element " + elementQName + " can not be null");
                                }
                            } else {
                                reader.next();
                                objectsCollection.add(getObjectForType(reader, actualElementType));
                                // we have to move the cursor until the end element of this attribute
                                while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                                    reader.next();
                                }
                            }
                        } else {
                            loop = false;
                        }

                    }
                }

                // this is very important in handling primitivs
                // they can not have null values so if array then we have to return the
                // array object is null
                // for other also it is covenient to assume like that.
                // TODO: dissable nillable true for primitives
                if ((Constants.OTHER_TYPE & classType) == Constants.OTHER_TYPE){
                    // i.e this is not a collection type
                    List objectsList = (List) objectsCollection;
                    if ((objectsCollection.size() == 0) ||
                            ((objectsCollection.size() == 1) && (objectsList.get(0) == null))) {
                        return null;
                    } else {
                        // create an array with the original element type
                        Object objectArray = Array.newInstance(elementType.getJavaClass(), objectsCollection.size());
                        for (int i = 0; i < objectsCollection.size(); i++) {
                            Array.set(objectArray, i, objectsList.get(i));
                        }
                        return objectArray;
                    }
                } else if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE){
                     if ((objectsCollection.size() == 0) ||
                             ((objectsCollection.size() == 1) && (objectsCollection.iterator().next() == null))){
                         return null;
                     } else {
                         return objectsCollection;
                     }

                } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE){
                     // TODO : handle maps
                    return null;
                } else {
                    throw new XmlParsingException("Unknow class type " + classType);
                }

            } else {
                if ("true".equals(nillble) || "1".equals(nillble)) {
                    // this is a nill attribute
                    while (!reader.isEndElement()) {
                        reader.next();
                    }
                    reader.next();
                    if (isNillable) {
                        return null;
                    } else {
                        throw new XmlParsingException("Element " + elementQName + " can not be null");
                    }

                } else {
                    reader.next();
                    Object returnObject = getObjectForType(reader, actualElementType);

                    // we have to move the cursor until the end element of this attribute
                    while (!reader.isEndElement() || !reader.getName().equals(elementQName)) {
                        reader.next();
                    }
                    return returnObject;
                }
            }
        } else {
            if (isMinOccurs0) {
                return null;
            } else {
                throw new XmlParsingException("Unexpected Subelement " + reader.getName() + " but " +
                        "expected " + elementQName.getLocalPart());
            }
        }
    }

    /**
     * returtns the collection object according to the type.
     * @param classType
     * @param javaClass
     * @return
     * @throws XmlParsingException
     */

    private Collection getCollectionObject(int classType,
                                           Class javaClass)
            throws XmlParsingException {

        Collection objectsCollection = null;
        try {
            if ((Constants.OTHER_TYPE & classType) == Constants.OTHER_TYPE) {
                // i.e this is not a list or a map
                objectsCollection = new ArrayList();
            } else {
                if (javaClass.isInterface()) {
                    if ((Constants.LIST_TYPE & classType) == Constants.LIST_TYPE) {
                       objectsCollection = new ArrayList();
                    } else if ((Constants.SET_TYPE & classType) == Constants.SET_TYPE) {
                       objectsCollection = new HashSet();
                    } else if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE) {
                       objectsCollection = new ArrayList();
                    } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE) {
                       // TODO : handle maps
                    }
                } else {
                    if ((Constants.COLLECTION_TYPE & classType) == Constants.COLLECTION_TYPE) {
                        objectsCollection = (Collection) javaClass.newInstance();
                    } else if ((Constants.MAP_TYPE & classType) == Constants.MAP_TYPE) {
                        // TODO: handle maps
                    }
                }
            }

        } catch (InstantiationException e) {
            throw new XmlParsingException("Problem with instanciating the element class " +
                        javaClass.getName() , e);
        } catch (IllegalAccessException e) {
            throw new XmlParsingException("Problem with instanciating the element class " +
                        javaClass.getName() , e);
        }
        return objectsCollection;
    }

    /**
     * reader must be at the start of the element
     *
     * @param reader
     * @return qName for the type attribute
     */
    private QName getTypeQName(XMLStreamReader reader) {
        QName typeQName = null;
        String typeValue = reader.getAttributeValue(Constants.URI_2001_SCHEMA_XSI, "type");
        if ((typeValue != null) && !typeValue.equals("")) {
            int index = typeValue.indexOf(":");
            String nsPrefix = "";
            if (index > -1) {
                nsPrefix = typeValue.substring(0, index);
            }

            String localPart = typeValue.substring(index + 1);
            typeQName = new QName(reader.getNamespaceURI(nsPrefix), localPart);
        }
        return typeQName;
    }

}
