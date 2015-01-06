/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.databinding.utils;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.activation.DataHandler;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.classloader.BeanInfoCache;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.apache.axis2.deployment.util.BeanExcludeInfo;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.StreamWrapper;
import org.w3c.dom.Document;

public class BeanUtil {
   
    private static int nsCount = 1;

    /**
     * To Serilize Bean object this method is used, this will create an object array using given
     * bean object
     */
    public static XMLStreamReader getPullParser(Object beanObject,
                                                QName beanName,
                                                TypeTable typeTable,
                                                boolean qualified,
                                                boolean processingDocLitBare) {

        Class beanClass = beanObject.getClass();
        List<Object> propertyQnameValueList = getPropertyQnameList(beanObject,
                                                           beanClass, beanName, typeTable, qualified, processingDocLitBare);

        ArrayList<QName> objectAttributes = new ArrayList<QName>();

        if ((typeTable != null)) {
            QName qNamefortheType = typeTable.getQNamefortheType(getClassName(beanClass));
            if (qNamefortheType != null) {
                objectAttributes.add(new QName(Constants.XSI_NAMESPACE, "type", "xsi"));
                objectAttributes.add(qNamefortheType);
            }
        }

        return new ADBXMLStreamReaderImpl(beanName, propertyQnameValueList.toArray(), objectAttributes.toArray(),
                                          typeTable, qualified);

    }

    private static String getClassName(Class type) {
        String name = type.getName();
        if (name.indexOf("$") > 0) {
            name = name.replace('$', '_');
        }
        return name;
    }


    private static BeanInfo getBeanInfo(Class beanClass, Class beanSuperclass) throws IntrospectionException {
        return BeanInfoCache.getCachedBeanInfo(beanClass, beanSuperclass);
    }

    private static BeanInfo getBeanInfo(Class beanClass) throws IntrospectionException {
        return getBeanInfo(beanClass, null);
    }

    private static List<Object> getPropertyQnameList(Object beanObject,
                                                     Class<?> beanClass,
                                                     QName beanName,
                                                     TypeTable typeTable,
                                                     boolean qualified,
                                                     boolean processingDocLitBare) {
        List<Object> propertyQnameValueList;
        Class<?> supperClass = beanClass.getSuperclass();

        if (!getQualifiedName(supperClass.getPackage()).startsWith("java.")) {
            propertyQnameValueList = getPropertyQnameList(beanObject,
                                                          supperClass, beanName, typeTable, qualified, processingDocLitBare);
        } else {
            propertyQnameValueList = new ArrayList<Object>();
        }

        try {
            QName elemntNameSpace = null;
            if (typeTable != null && qualified) {
                QName qNamefortheType = typeTable.getQNamefortheType(beanClass.getName());
                if (qNamefortheType == null) {
                    qNamefortheType = typeTable.getQNamefortheType(beanClass.getPackage().getName());
                }
                if (qNamefortheType == null) {
                    throw new AxisFault("Mapping qname not fond for the package: " +
                                        beanObject.getClass().getPackage().getName());
                }

                elemntNameSpace = new QName(qNamefortheType.getNamespaceURI(), "elementName", qNamefortheType.getPrefix());
            }
            AxisService axisService = null;
            if (MessageContext.getCurrentMessageContext() != null) {
                axisService = MessageContext.getCurrentMessageContext().getAxisService();
            }

            BeanExcludeInfo beanExcludeInfo = null;
            if (axisService != null && axisService.getExcludeInfo() != null) {
                beanExcludeInfo = axisService.getExcludeInfo().getBeanExcludeInfoForClass(beanClass.getName());
            }
            BeanInfo beanInfo = getBeanInfo(beanClass, beanClass.getSuperclass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : properties) {
                String propertyName = property.getName();
                Class<?> ptype = property.getPropertyType();
                if (propertyName.equals("class") ||
                    beanExcludeInfo != null && beanExcludeInfo.isExcludedProperty(propertyName) || ptype == null) {
                    continue;
                }

                Method readMethod = property.getReadMethod();
                if (readMethod == null) {
                    Class propertyType = property.getPropertyType();
                    if (propertyType == java.lang.Boolean.class) {
                        Method writeMethod = property.getWriteMethod();
                        if (writeMethod != null) {
                            String tmpWriteMethodName = writeMethod.getName();
                            PropertyDescriptor tmpPropDesc =
                                    new PropertyDescriptor(property.getName(),
                                            beanObject.getClass(),
                                            "is" + tmpWriteMethodName.substring(3),
                                            tmpWriteMethodName);
                            readMethod = tmpPropDesc.getReadMethod();
                        }
                    }
                }
                Object value;
                if (readMethod != null) {
                    readMethod.setAccessible(true);
                    value = readMethod.invoke(beanObject);
                } else {
                    throw new AxisFault("Property '" + propertyName + "' in bean class '"
                                        + beanClass.getName() + "'is not readable.");
                }

                if (SimpleTypeMapper.isSimpleType(ptype)) {
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                 beanName, processingDocLitBare);
                    propertyQnameValueList.add(
                            value == null ? null : SimpleTypeMapper.getStringValue(value));
                } else if(SimpleTypeMapper.isDomDocument(ptype)){
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                            beanName, processingDocLitBare);
                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    propertyQnameValueList.add(convertDOMtoOM(fac, value));
                    
                } else if (ptype.isArray()) {
                    if (SimpleTypeMapper.isSimpleType(ptype.getComponentType())) {
                        if (value != null) {
                            if (Byte.TYPE.equals(ptype.getComponentType())) {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);
                                propertyQnameValueList.add(Base64Utils.encode((byte[]) value));
                            } else {
                                int i1 = Array.getLength(value);
                                for (int j = 0; j < i1; j++) {
                                    Object o = Array.get(value, j);
                                    addTypeQname(elemntNameSpace, propertyQnameValueList,
                                                 property, beanName, processingDocLitBare);
                                    propertyQnameValueList.add(o == null ? null :
                                                               SimpleTypeMapper.getStringValue(o));
                                }
                            }
                        } else {
                            addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                         beanName, processingDocLitBare);
                            propertyQnameValueList.add(value);
                        }
                    } else {
                        if (value != null) {
                            for (Object o : (Object[]) value) {
                                addTypeQname(elemntNameSpace, propertyQnameValueList,
                                             property, beanName, processingDocLitBare);   
                                QName propertyQName = null;
                                if (elemntNameSpace != null) {
                                    propertyQName = new QName(
                                            elemntNameSpace.getNamespaceURI(),
                                            propertyName,
                                            elemntNameSpace.getPrefix());
                                } else {
                                    propertyQName = new QName(propertyName);

                                }
                                
                                if (SimpleTypeMapper
                                        .isObjectArray(o.getClass())
                                        || SimpleTypeMapper
                                                .isMultidimensionalObjectArray(o
                                                        .getClass())) {
                                    /**
                                     * If it is a Object[] we need to add instance type
                                     * attributes to the response message.
                                     * Copied from ADBXMLStreamReaderImpl. 
                                     * For inner Arrary Complex types we use the special local name array - "array"
                                     */
                                    QName itemName;
                                    if (qualified) {
                                        itemName = new QName(elemntNameSpace.getNamespaceURI(),
                                                Constants.INNER_ARRAY_COMPLEX_TYPE_NAME,
                                                elemntNameSpace.getPrefix());
                                    } else {
                                        itemName = new QName(Constants.INNER_ARRAY_COMPLEX_TYPE_NAME);
                                    }
                                    propertyQnameValueList.add(getOMElement(propertyQName , (Object[]) o,
                                            itemName, qualified, typeTable));
                                } else {
                                    if(SimpleTypeMapper.isObjectArray(value.getClass())){
                                        OMFactory fac = OMAbstractFactory.getOMFactory();
                                        OMElement element = fac.createOMElement(propertyQName);
                                        element.addChild(fac.createOMText(SimpleTypeMapper.getStringValue(o)));  
                                        addInstanceTypeAttribute(fac, element, o, typeTable);
                                        propertyQnameValueList.add(element);
                                    } else {
                                        propertyQnameValueList.add(o);
                                    }
                                }
                               
                            }
                        } else {
                            addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                         beanName, processingDocLitBare);
                            propertyQnameValueList.add(value);
                        }
                    }
                } else  if (SimpleTypeMapper.isCollection(ptype) && value != null) { 
                    if (typeTable != null) {
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        QName qNamefortheType = null;
                        qNamefortheType = (QName) typeTable
                                .getComplexSchemaMap().get(getClassName(beanClass));
                        Type genericType = property.getReadMethod().getGenericReturnType();
                        OMElement collection = BeanUtil.getCollectionElement(
                                fac, genericType,
                                (Collection) value, propertyName,null,
                                qNamefortheType,typeTable,
                                qualified);
//                      addTypeQname(elemntNameSpace, propertyQnameValueList,
//                              property, beanName, processingDocLitBare);
                        Iterator childItr = collection.getChildren();
                        while(childItr.hasNext()){
                            addTypeQname(elemntNameSpace, propertyQnameValueList,
                                    property, beanName, processingDocLitBare);
                            propertyQnameValueList.add(childItr.next());
                        }
                    
                    } else {
                        Collection<?> objList = (Collection<?>) value;
                        if (objList != null && objList.size() > 0) {
                            //this was given error , when the array.size = 0
                            // and if the array contain simple type , then the ADBPullParser asked
                            // PullParser from That simpel type
                            for (Object o : objList) {
                                if (SimpleTypeMapper.isSimpleType(o)) {
                                    addTypeQname(elemntNameSpace, propertyQnameValueList,
                                            property, beanName, processingDocLitBare);
                                    propertyQnameValueList.add(o);
                                } else {
                                    addTypeQname(elemntNameSpace, propertyQnameValueList,
                                            property, beanName, processingDocLitBare);
                                    propertyQnameValueList.add(o);
                                }
                            }

                        } else {
                            addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                    beanName, processingDocLitBare);
                            propertyQnameValueList.add(value);
                        }
                    }
                    
                } else if (SimpleTypeMapper.isMap(ptype) && value != null) {
                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    QName qNamefortheType = (QName) typeTable
                            .getComplexSchemaMap().get(getClassName(beanClass));
                    OMNamespace ns = fac.createOMNamespace(
                                        qNamefortheType.getNamespaceURI(),
                                        qNamefortheType.getPrefix());
                    List<OMElement> mapEntries = getMapElement(fac,
                            ptype, (Map) value, typeTable, qualified);
                    OMElement map = fac.createOMElement(propertyName,
                                        qNamefortheType.getNamespaceURI(),
                                        qNamefortheType.getPrefix());
                    for (OMElement ele : mapEntries) {
                        map.addChild(ele);
                    }
                    addTypeQname(elemntNameSpace, propertyQnameValueList,
                            property, beanName, processingDocLitBare);
                    propertyQnameValueList.add(map);
                } else if (SimpleTypeMapper.isEnum(ptype)){
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                 beanName, processingDocLitBare);
                    propertyQnameValueList.add(
                            value == null ? null : SimpleTypeMapper.getStringValue(value.toString()));
                }else {
                    addTypeQname(elemntNameSpace, propertyQnameValueList, property,
                                 beanName, processingDocLitBare);
                    if (Object.class.equals(ptype)) {
                        //this is required to match this element prefix as
                        //root element's prefix.
                        QName qNamefortheType = (QName) typeTable
                                .getComplexSchemaMap().get(
                                        getClassName(beanClass));
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        QName elementName;
                        OMElement element;
                        if (elemntNameSpace != null) {
                            elementName = new QName(
                                    elemntNameSpace.getNamespaceURI(),
                                    property.getName(),
                                    qNamefortheType.getPrefix());
                        } else {
                            elementName = new QName(property.getName());
                        }
                        
                        if(SimpleTypeMapper.isSimpleType(value)){
                            element = fac.createOMElement(elementName);
                            element.addChild(fac.createOMText(SimpleTypeMapper
                                    .getStringValue(value)));
                        }else{
                             XMLStreamReader xr = BeanUtil.getPullParser(value,
                                     elementName, typeTable, qualified, false);
                             OMXMLParserWrapper stAXOMBuilder =
                                     OMXMLBuilderFactory.createStAXOMBuilder(
                                             OMAbstractFactory.getOMFactory(), new StreamWrapper(xr));
                             element = stAXOMBuilder.getDocumentElement();
                             
                            
                        }
                        addInstanceTypeAttribute(fac, element, value, typeTable);
                        propertyQnameValueList.add(element);
                        continue;
                    }

                    propertyQnameValueList.add(value);
                }
            }

            return propertyQnameValueList;

        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } catch (java.beans.IntrospectionException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addTypeQname(QName elemntNameSpace,
                                     List<Object> propertyQnameValueList,
                                     PropertyDescriptor propDesc,
                                     QName beanName,
                                     boolean processingDocLitBare) {
        if (elemntNameSpace != null) {
            propertyQnameValueList.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                 propDesc.getName(), elemntNameSpace.getPrefix()));
        } else {
            if (processingDocLitBare) {
                propertyQnameValueList.add(new QName(propDesc.getName()));
            } else {
                propertyQnameValueList.add(new QName(beanName.getNamespaceURI(), propDesc.getName(), beanName.getPrefix()));
            }

        }
    }

    /**
     * to get the pull parser for a given bean object , generate the wrpper element using class
     * name
     *
     * @param beanObject
     */
    public static XMLStreamReader getPullParser(Object beanObject) {
        String className = beanObject.getClass().getName();
        if (className.indexOf(".") > 0) {
            className = className.substring(className.lastIndexOf('.') + 1,
                                            className.length());
        }
        return getPullParser(beanObject, new QName(className), null, false, false);
    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     ObjectSupplier objectSupplier,
                                     String arrayLocalName)
            throws AxisFault {
        Object beanObj;
        try {
            // Added this block as a fix for issues AXIS2-2055 and AXIS2-1899
            // to support polymorphism in POJO approach.
            // Retrieve the type name of the instance from the 'type' attribute
            // and retrieve the class.
            String instanceTypeName = null;
            if (beanClass != null && !beanClass.isArray()) {
                instanceTypeName = beanElement.getAttributeValue(new QName(
                        Constants.XSI_NAMESPACE, "type"));
            }
            boolean hexBin = false;
            if (instanceTypeName != null) {
                MessageContext messageContext = MessageContext.getCurrentMessageContext();
                // we can have this support only at the server side. we need to find the axisservice
                // to get the type table.
                if (messageContext != null) {
                    AxisService axisService = messageContext.getAxisService();
                    if (axisService != null) {
                        QName typeQName = beanElement.resolveQName(instanceTypeName);
                        //Need this flag to differentiate "xsd:hexBinary" and "xsd:base64Binary" data. 
                        if(org.apache.ws.commons.schema.constants.Constants.XSD_HEXBIN.equals(typeQName)){
                            hexBin = true;
                        }
                        TypeTable typeTable = axisService.getTypeTable();
                        String className = typeTable.getClassNameForQName(typeQName);
                        if (className != null) {
                            try {
                                beanClass = Loader.loadClass(axisService.getClassLoader(), className);
                            } catch (ClassNotFoundException ce) {
                                throw AxisFault.makeFault(ce);
                            }
                        } else {
                            throw new AxisFault("Unknow type " + typeQName);
                        }
                    }
                }
            }

            // check for nil attribute:
            QName nilAttName = new QName(Constants.XSI_NAMESPACE, Constants.NIL, "xsi");
            if (beanElement.getAttribute(nilAttName) != null) {
                return null;
            }
            
            if(beanClass.getName().equals(DataHandler.class.getName())){
                return SimpleTypeMapper.getDataHandler(beanElement,hexBin);
            }

            if (beanClass.isArray()) {
                ArrayList<Object> valueList = new ArrayList<Object>();
                Class arrayClassType = beanClass.getComponentType();
                if ("byte".equals(arrayClassType.getName())) {
                    // find the part first and decode it
                    OMElement partElement = null;
                    for (Iterator iter = beanElement.getChildElements(); iter.hasNext();) {
                        partElement = (OMElement) iter.next();
                        if (partElement.getLocalName().equals(arrayLocalName)) {
                            break;
                        }
                    }
                    return Base64Utils.decode(partElement.getText());
                } else {
                    Iterator parts = beanElement.getChildElements();
                    OMElement omElement;
                    while (parts.hasNext()) {
                        Object objValue = parts.next();
                        if (objValue instanceof OMElement) {
                            omElement = (OMElement) objValue;
                            if ((arrayLocalName != null) && !arrayLocalName.equals(omElement.getLocalName())) {
                                continue;
                            }
                            // this is a multi dimentional array so always inner element is array
                            Object obj = deserialize(arrayClassType,
                                                     omElement,
                                                     objectSupplier, "array");

                            valueList.add(obj);
                        }
                    }
                    return ConverterUtil.convertToArray(arrayClassType, valueList);
                }
            }else if(SimpleTypeMapper.isDomDocument(beanClass)){
                return convertOMtoDOM(beanElement);
                
            } else if (XMLGregorianCalendar.class.getName().equals(
                    beanClass.getName())) {
                return getXMLGregorianCalendar(beanElement);

            } else {
                if (SimpleTypeMapper.isSimpleType(beanClass)) {
                    return getSimpleTypeObjectChecked(beanClass, beanElement);
                } else if ("java.lang.Object".equals(beanClass.getName())) {
                    return beanElement.getFirstOMChild();
                }

                //use a comaprator to ignore the case of the bean element
                //names eg. if the property descriptor is getServiceName it
                //should accept child element with ServiceName as well.
                //but currently accepts only serviceName
                Comparator comparator = new Comparator() {
                    public int compare(Object o1, Object o2) {
                        String string1 = (String) o1;
                        String string2 = (String) o2;
                        return string1.compareToIgnoreCase(string2);
                    }
                };
                Map<String, PropertyDescriptor> properties = new TreeMap<String, PropertyDescriptor>(comparator);


                BeanInfo beanInfo = getBeanInfo(beanClass);
                PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor proprty : propDescs) {
                    properties.put(proprty.getName(), proprty);
                }
                Iterator elements = beanElement.getChildren();
                beanObj = objectSupplier.getObject(beanClass);
                while (elements.hasNext()) {
                    // the beanClass could be an abstract one.
                    // so create an instance only if there are elements, in
                    // which case a concrete subclass is available to instantiate.
                    OMElement parts;
                    Object objValue = elements.next();
                    if (objValue instanceof OMElement) {
                        parts = (OMElement) objValue;
                    } else {
                        continue;
                    }
                    OMAttribute attribute = parts.getAttribute(
                            new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi"));

                    // if parts/@href != null then need to find element with id and deserialize.
                    // before that first check whether we already have it in the hashtable
                    String partsLocalName = parts.getLocalName();
                    PropertyDescriptor prty = properties.remove(partsLocalName);
                    if (prty != null) {
                        Class parameters = prty.getPropertyType();
                        if (prty.getName().equals("class"))
                            continue;

                        Object partObj;
                        boolean isNil = false;
                        if (attribute != null) {
                            String nilValue = attribute.getAttributeValue();
                            if ("true".equals(nilValue) || "1".equals(nilValue)) {
                                isNil = true;
                            }
                        }
                        if (isNil) {
                            partObj = null;
                        } else {
                            if (SimpleTypeMapper.isSimpleType(parameters)) {
                                partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                            } else if (SimpleTypeMapper.isHashSet(parameters)) {
                                partObj = SimpleTypeMapper.getHashSet((OMElement)
                                        parts.getParent(), prty.getName());
                            } else if (SimpleTypeMapper.isCollection(parameters)) {
                                Type type = prty.getReadMethod().getGenericReturnType();
                                partObj = processGenericCollection(parts, type, null, objectSupplier);  

                            } else if (SimpleTypeMapper.isDataHandler(parameters)) {
                                partObj = SimpleTypeMapper.getDataHandler(parts);
                            } else if (parameters.isArray()) {
                                partObj = deserialize(parameters, (OMElement) parts.getParent(),
                                                      objectSupplier, prty.getName());
                            } else if (SimpleTypeMapper.isMap(parameters)){
                                partObj = null;
                                final Type type = prty.getReadMethod().getGenericReturnType();
                                if (type instanceof ParameterizedType) {
                                    ParameterizedType aType = (ParameterizedType) type;
                                    Type[] parameterArgTypes = aType.getActualTypeArguments();
                                    partObj = processGenericsMapElement(parameterArgTypes
                                          , (OMElement) parts.getParent(), null, parts.getChildren(), objectSupplier, beanClass);
                                } else {
                                    Type[] parameterArgTypes = {Object.class,Object.class}; 
                                    partObj = processGenericsMapElement(parameterArgTypes
                                             , (OMElement) parts.getParent(), null, parts.getChildren(), objectSupplier, beanClass);
                                }
                            }else if (SimpleTypeMapper.isEnum(parameters)) {
                                partObj =processEnumObject(parameters , parts);
                            } else {
                                partObj = deserialize(parameters, parts, objectSupplier, null);
                            }
                        }
                        Object[] parms = new Object[]{partObj};
                        Method writeMethod = prty.getWriteMethod();
                        if (writeMethod != null) {
                            writeMethod.setAccessible(true);
                            writeMethod.invoke(beanObj, parms);
                        }
                    }
                }
                return beanObj;
            }
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        } catch (InvocationTargetException e) {
            throw new AxisFault("InvocationTargetException : " + e);
        } catch (IntrospectionException e) {
            throw new AxisFault("IntrospectionException : " + e);
        } catch (DatatypeConfigurationException e) {
            throw new AxisFault("DatatypeConfigurationException : " + e);            
        }


    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     MultirefHelper helper,
                                     ObjectSupplier objectSupplier) throws AxisFault {
        Object beanObj;
        try {
            HashMap<String, PropertyDescriptor> properties = new HashMap<String, PropertyDescriptor>();
            BeanInfo beanInfo = getBeanInfo(beanClass);
            PropertyDescriptor[] propDescs = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor proprty : propDescs) {
                properties.put(proprty.getName(), proprty);
            }

            beanObj = objectSupplier.getObject(beanClass);
            Iterator elements = beanElement.getChildren();
            while (elements.hasNext()) {
                Object child = elements.next();
                OMElement parts;
                if (child instanceof OMElement) {
                    parts = (OMElement) child;
                } else {
                    continue;
                }
                String partsLocalName = parts.getLocalName();
                PropertyDescriptor prty = properties.get(
                        partsLocalName.toLowerCase());
                if (prty != null) {
                    Class parameters = prty.getPropertyType();
                    if (prty.getName().equals("class"))
                        continue;
                    Object partObj;
                    OMAttribute attr = MultirefHelper.processRefAtt(parts);
                    if (attr != null) {
                        String refId = MultirefHelper.getAttvalue(attr);
                        partObj = helper.getObject(refId);
                        if (partObj == null) {
                            partObj = helper.processRef(parameters, refId, objectSupplier);
                        }
                    } else {
                        partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                        if (partObj == null) {
                            partObj = deserialize(parameters, parts, objectSupplier, null);
                        }
                    }
                    Object[] parms = new Object[]{partObj};
                    Method writeMethod = prty.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.setAccessible(true);
                        writeMethod.invoke(beanObj, parms);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        } catch (InvocationTargetException e) {
            throw new AxisFault("InvocationTargetException : " + e);
        } catch (IntrospectionException e) {
            throw new AxisFault("IntrospectionException : " + e);
        }
        return beanObj;
    }


    /**
     * To get JavaObjects from XML element , the element most of the time contains only one element
     * in that case that element will be converted to the JavaType specified by the javaTypes array
     * The algo is as follows, get the childerns of the response element , and if it conatian more
     * than one element then check the retuen type of that element and conver that to corresponding
     * JavaType
     *
     * @param response  OMElement
     * @param javaTypes Array of JavaTypes
     * @return Array of objects
     * @throws AxisFault
     */
    public static Object[] deserialize(OMElement response,
                                       Object[] javaTypes,
                                       ObjectSupplier objectSupplier) throws AxisFault {
        return BeanUtil.deserialize(response, javaTypes, objectSupplier, null, null);
    }

    public static Object[] deserialize(OMElement response,
                                       Object[] javaTypes,
                                       ObjectSupplier objectSupplier,
                                       String[] parameterNames,
                                       Method method) throws AxisFault {
        /*
         * Take the number of parameters in the method and , only take that much of child elements
         * from the OMElement , other are ignore , as an example
         * if the method is , foo(String a , int b)
         * and if the OMElemet
         * <foo>
         *  <arg0>Val1</arg0>
         *  <arg1>Val2</arg1>
         *  <arg2>Val3</arg2>
         *
         * only the val1 and Val2 take into account
         */
        int length = javaTypes.length;
        int count = 0;
        Object[] retObjs = new Object[length];

        /*
        * If the body first child contains , then there can not be any other element withot
        * refs , so I can assume if the first child of the body first element has ref then
        * the message has to handle as mutiref message.
        * as an exmple if the body is like below
        * <foo>
        *  <arg0 href="#0"/>
        * </foo>
        *
        * then there can not be any element without refs , meaning following we are not handling
        * <foo>
        *  <arg0 href="#0"/>
        *  <arg1>absbsbs</arg1>
        * </foo>
        */
        Iterator parts = response.getChildren();
        //to handle multirefs
        //have to check the instanceof
        MultirefHelper helper = new MultirefHelper((OMElement) response.getParent());
        //to support array . if the parameter type is array , then all the omelemnts with that paramtre name
        // has to  get and add to the list
        Class classType;
        String currentLocalName;
        Type[] genericParameterTypes = null;
        if (method != null) {
            genericParameterTypes = method.getGenericParameterTypes();
        }
        Type genericType = null;
        while (parts.hasNext() && count < length) {
            Object objValue = parts.next();
            OMElement omElement;
            if (objValue instanceof OMElement) {
                omElement = (OMElement) objValue;
            } else {
                continue;
            }

            // if the local part is not match. this means element is not present
            // due to min occurs zero.
            // we need to hard code arg and item since that has been used in RPCService client
            // and some test cases
            while ((parameterNames != null) &&
                   (!omElement.getQName().getLocalPart().startsWith("arg")) &&
                   (!omElement.getQName().getLocalPart().startsWith("item")) &&
                   !omElement.getQName().getLocalPart().equals(parameterNames[count])) {
                // POJO handles OMElement in a differnt way so need this check for OMElement
                Class paramClassType = (Class) javaTypes[count];
                if (!paramClassType.getName().equals(OMElement.class.getName())) {
                    count++;
                } else {
                    break;
                }
            }

            currentLocalName = omElement.getLocalName();
            classType = (Class) javaTypes[count];
            if (genericParameterTypes != null) {
                genericType = genericParameterTypes[count];
            }
            /*
             * In bare invocation "parameterNames" comes as null value.
             */
            boolean bare = false;
            if(parameterNames == null){
                bare = true;
            }
           
            omElement = processElement(classType, omElement, helper, parts,
                                       currentLocalName, retObjs, count, objectSupplier, genericType, bare);
            while (omElement != null) {
                count++;
                // if the local part is not match. this means element is not present
                // due to min occurs zero.
                // we need to hard code arg and item since that has been used in RPCService client
                // and some test cases
                while ((parameterNames != null) &&
                       (!omElement.getQName().getLocalPart().startsWith("arg")) &&
                       (!omElement.getQName().getLocalPart().startsWith("item")) &&
                       !omElement.getQName().getLocalPart().equals(parameterNames[count])) {
                    // POJO handles OMElement in a differnt way so need this check for OMElement
                    Class paramClassType = (Class) javaTypes[count];
                    if (!paramClassType.getName().equals(OMElement.class.getName())) {
                        count++;
                    } else {
                        break;
                    }
                }

                currentLocalName = omElement.getLocalName();
                classType = (Class) javaTypes[count];
                if (genericParameterTypes != null) {
                    genericType = genericParameterTypes[count];
                }
                omElement = processElement((Class) javaTypes[count], omElement,
                                           helper, parts, omElement.getLocalName(), retObjs, count,
                                           objectSupplier, genericType);
            }
            count++;
        }

        // Ensure that we have at least a zero element array
        for (int i = 0; i < length; i++) {
            Class clazz = (Class) javaTypes[i];
            if (retObjs[i] == null && clazz.isArray()) {
                retObjs[i] = Array.newInstance(clazz.getComponentType(), 0);
            }
        }

        helper.clean();
        return retObjs;
    }

    private static OMElement processElement(Class classType,
            OMElement omElement, MultirefHelper helper, Iterator parts,
            String currentLocalName, Object[] retObjs, int count,
            ObjectSupplier objectSupplier, Type genericType) throws AxisFault {

        return processElement(classType, omElement, helper, parts,
                currentLocalName, retObjs, count, objectSupplier, genericType, false);

    }
    private static OMElement processElement(Class classType, OMElement omElement,
                                            MultirefHelper helper, Iterator parts,
                                            String currentLocalName,
                                            Object[] retObjs,
                                            int count,
                                            ObjectSupplier objectSupplier,
                                            Type genericType, boolean bare) throws AxisFault {
        Object objValue;
        boolean isRef = false;
        OMAttribute omatribute = MultirefHelper.processRefAtt(omElement);
        if (omatribute != null) {
            isRef = true;
        }
        if (classType.isArray()) {
            boolean done = true;
            ArrayList<Object> valueList = new ArrayList<Object>();
            Class arrayClassType = classType.getComponentType();
            if ("byte".equals(arrayClassType.getName())) {
                retObjs[count] =
                        processObject(omElement, arrayClassType, helper, true, objectSupplier, genericType);
                return null;
            } else {
                valueList.add(processObject(omElement, arrayClassType, helper, true,
                                            objectSupplier, genericType));
            }
            while (parts.hasNext()) {
                objValue = parts.next();
                if (objValue instanceof OMElement) {
                    omElement = (OMElement) objValue;
                } else {
                    continue;
                }
                if (!currentLocalName.equals(omElement.getLocalName())) {
                    done = false;
                    break;
                }
                Object o = processObject(omElement, arrayClassType,
                                         helper, true, objectSupplier, genericType);
                valueList.add(o);
            }
            if (valueList.size() == 1 && valueList.get(0) == null) {
                retObjs[count] = null;
            } else {
                retObjs[count] = ConverterUtil.convertToArray(arrayClassType,
                                                              valueList);
            }
            if (!done) {
                return omElement;
            }
            
        } else if(SimpleTypeMapper.isCollection(classType) && ! isRef){
            if(bare){
                OMElement[] toReturn = new OMElement[1];
                parts = omElement.getChildren();
                retObjs[count] = processGenericCollection(omElement.getFirstElement(), toReturn, genericType, helper, objectSupplier, parts,bare);
                OMNode node = omElement.getNextOMSibling();
                while(node != null){
                    if(OMElement.class.isAssignableFrom(node.getClass())){
                        return (OMElement) node;
                    } else {
                        node = node.getNextOMSibling();
                    }
                }
            
            } else {
            OMElement[] toReturn = new OMElement[1];
                retObjs[count] = processGenericCollection(omElement, toReturn, genericType, helper, objectSupplier, parts,bare);
                 if (toReturn[0] != null) {
                     return toReturn[0];
                 }
            }
        } else if (SimpleTypeMapper.isEnum(classType)) {
            /* handling enum types */
            retObjs[count] = processEnumObject(classType, omElement);
        } else{
            //handling refs
            retObjs[count] = processObject(omElement, classType, helper, false, objectSupplier, genericType);
            
            
        }
        return null;
    }

    private static Collection<Object> processGenericsElement(Type classType, OMElement omElement,
                                               MultirefHelper helper, Iterator parts,
                                               ObjectSupplier objectSupplier,
                                               Type genericType) throws AxisFault {
        Object objValue;
        Collection<Object> valueList = getCollectionInstance(genericType);
        while (parts.hasNext()) {
            objValue = parts.next();
            Object o;
            if (objValue instanceof OMElement) {
                omElement = (OMElement) objValue;
            } else {
                continue;
            }
            if (classType instanceof ParameterizedType) {
                ParameterizedType parameterizedClassType = (ParameterizedType) classType;
                if (Collection.class
                        .isAssignableFrom((Class<?>) parameterizedClassType
                                .getRawType())) {
                    o = processGenericCollection(omElement.getFirstElement(),
                            classType, helper, objectSupplier);
                } else if (Map.class
                        .isAssignableFrom((Class<?>) parameterizedClassType
                                .getRawType())) {
                    o = processGenericsMapElement( 
                            parameterizedClassType.getActualTypeArguments(),
                            omElement, helper, omElement.getChildren(), objectSupplier,
                            parameterizedClassType);
                } else {
                    o = processObject(omElement, (Class) classType,
                             helper, true, objectSupplier, genericType);
                }
                
            } else {
                o = processObject(omElement, (Class) classType,
                         helper, true, objectSupplier, genericType);
                
            }
            
            valueList.add(o);
            
        }
        return valueList;
    }


    public static Object processObject(OMElement omElement,
                                       Class classType,
                                       MultirefHelper helper,
                                       boolean isArrayType,
                                       ObjectSupplier objectSupplier,
                                       Type generictype) throws AxisFault {
        boolean hasRef = false;
        OMAttribute omatribute = MultirefHelper.processRefAtt(omElement);
        String ref = null;
        if (omatribute != null) {
            hasRef = true;
            ref = MultirefHelper.getAttvalue(omatribute);
        }
        if (OMElement.class.isAssignableFrom(classType)) {
            if (hasRef) {
                OMElement elemnt = helper.getOMElement(ref);
                if (elemnt == null) {
                    return helper.processOMElementRef(ref);
                } else {
                    return elemnt;
                }
            } else
                return omElement;
        } else {
            if (hasRef) {
                if (helper.getObject(ref) != null) {
                    return helper.getObject(ref);
                } else {
                    return helper.processRef(classType, generictype, ref, objectSupplier);
                }
            } else {
                OMAttribute attribute = omElement.getAttribute(
                        new QName("http://www.w3.org/2001/XMLSchema-instance", "nil", "xsi"));
                if (attribute != null) {
                    return null;
                }
                if (SimpleTypeMapper.isSimpleType(classType)) {
                    if (isArrayType && "byte".equals(classType.getName())) {
                        String value = omElement.getText();
                        return Base64Utils.decode(value);
                    } else {
                        return getSimpleTypeObjectChecked(classType, omElement);
                    }
                } else if (SimpleTypeMapper.isCollection(classType)) {
                    return processGenericCollection(omElement, generictype, null, objectSupplier); 

                } else if (SimpleTypeMapper.isDataHandler(classType)) {
                    return SimpleTypeMapper.getDataHandler(omElement);
                    
                } else if(SimpleTypeMapper.isDomDocument(classType)){
                    return convertOMtoDOM(omElement);
                } else if(SimpleTypeMapper.isMap(classType)){
                    if (generictype != null && (generictype instanceof ParameterizedType)) {
                        ParameterizedType aType = (ParameterizedType) generictype;
                        Type[] parameterArgTypes = aType.getActualTypeArguments();
                        Iterator parts = omElement.getChildElements();
                        return processGenericsMapElement(parameterArgTypes
                             , omElement, helper, parts, objectSupplier, generictype);
                    } else {
                        Type[] parameterArgTypes = {Object.class,Object.class}; 
                        Iterator parts = omElement.getChildElements();
                        return processGenericsMapElement(parameterArgTypes,
                                omElement, helper, parts, objectSupplier, generictype);   
                    }
                
                }else if(SimpleTypeMapper.isEnum(classType)){
                    return processEnumObject(classType, omElement);
                }else {
                    return BeanUtil.deserialize(classType, omElement, objectSupplier, null);
                }
            }
        }
    }

    /*This method check is service method required enum type instance as method parameter
    * if so return required enum object
    *
    * @param classType method required instance type
    * @param omElement OMElement
    * @return an Enum object
    * */
    public static Object processEnumObject(Class classType , OMElement omElement)throws AxisFault{
          /*
            *reason to add this block is check is soap sending a string but service require Enum
            * then this convert string to relevant enum object and add to retObjs[] as object
            * */
          String paraArgString = omElement.getText();
         Object enumIbj;
        if (paraArgString == null || paraArgString.length() == 0) {
            enumIbj = null;
        }else{
            enumIbj = Enum.valueOf(classType , paraArgString);
        }
        return enumIbj;

    }


    public static OMElement getOMElement(QName opName,
                                         Object[] args,
                                         QName partName,
                                         boolean qualifed,
                                         TypeTable typeTable) {
        ArrayList<Object> objects;
        objects = new ArrayList<Object>();
        int argCount = 0;
        for (Object arg : args) {
            if (arg == null) {
                if (partName == null) {
                    objects.add("item" + argCount);
                } else {
                    objects.add(partName);
                }
                objects.add(arg);
                continue;
            }

            if (arg instanceof Object[]) {
                // at the client side the partname is always null. At client side this means user try to
                // invoke a service with an array argument.
                if (partName == null) {
                    Object array[] = (Object[]) arg;
                    for (Object o : array) {
                        if (o == null) {
                            objects.add("item" + argCount);
                            objects.add(o);
                        } else {
                            if (SimpleTypeMapper.isSimpleType(o)) {
                                objects.add("item" + argCount);
                                objects.add(SimpleTypeMapper.getStringValue(o));
                            } else {
                                objects.add(new QName("item" + argCount));
                                if (o instanceof OMElement) {
                                    OMFactory fac = OMAbstractFactory.getOMFactory();
                                    OMElement wrappingElement;
                                    wrappingElement = fac.createOMElement("item" + argCount, null);
                                    wrappingElement.addChild((OMElement) o);
                                    objects.add(wrappingElement);
                                } else {
                                    objects.add(o);
                                }
                            }
                        }
                    }
                } else {
                    // this happens at the server side. this means it is an multidimentional array.
                    objects.add(partName);
                    if (SimpleTypeMapper.isObjectArray(arg.getClass())
                            || SimpleTypeMapper
                                    .isMultidimensionalObjectArray(arg
                                            .getClass())) {
                        /**
                         * If it is a Object[] we need to add instance type
                         * attributes to the response message.
                         * Copied from ADBXMLStreamReaderImpl. 
                         * For inner Arrary Complex types we use the special local name array - "array"
                         */
                        QName itemName = new QName(partName.getNamespaceURI(),
                                Constants.INNER_ARRAY_COMPLEX_TYPE_NAME,
                                partName.getPrefix());
                        objects.add(getOMElement(partName, (Object[]) arg,
                                itemName, qualifed, typeTable));
                    } else {
                        objects.add(arg);
                    }
                }
            } else {
                if (SimpleTypeMapper.isSimpleType(arg)) { 
                    OMElement element;
                    OMFactory fac = OMAbstractFactory.getOMFactory();
                    if(partName != null){
                        element = fac.createOMElement(partName, null);
                    }else{
                        String eleName = "arg" + argCount;
                        element = fac.createOMElement(eleName, null);
                    }
                    element.addChild(fac.createOMText(SimpleTypeMapper
                            .getStringValue(arg)));
                    if (SimpleTypeMapper.isObjectArray(args.getClass())) {
                        addInstanceTypeAttribute(fac, element, arg, typeTable);
                    }
                    objects.add(element.getQName());
                    objects.add(element);
                } else {
                    if (partName == null) {
                        objects.add(new QName("arg" + argCount));
                    } else {
                        objects.add(partName);
                    }
                    if (arg instanceof OMElement) {
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        OMElement wrappingElement;
                        if (partName == null) {
                            wrappingElement = fac.createOMElement("arg" + argCount, null);
                            wrappingElement.addChild((OMElement) arg);
                        } else {
                            wrappingElement = fac.createOMElement(partName, null);
                            wrappingElement.addChild((OMElement) arg);
                        }
                        objects.add(wrappingElement);
                    } else if (arg instanceof byte[]) {
                        objects.add(Base64Utils.encode((byte[]) arg));
                    } else if (SimpleTypeMapper.isDataHandler(arg.getClass())) {
                        OMFactory fac = OMAbstractFactory.getOMFactory();
                        OMElement wrappingElement;
                        if (partName == null) {
                            wrappingElement = fac.createOMElement("arg" + argCount, null);
                        } else {
                            wrappingElement = fac.createOMElement(partName, null);
                        }
                        OMText text = fac.createOMText(arg, true);
                        wrappingElement.addChild(text);
                        objects.add(wrappingElement);
                    }else if (SimpleTypeMapper.isEnum(arg.getClass())) {
                        // in here i can return enum instances but for now i return it as a simple string
                        objects.add(arg.toString());
                    } else {
                        objects.add(arg);
                    }
                }
            }
            argCount++;
        }

        XMLStreamReader xr =
                new ADBXMLStreamReaderImpl(opName, objects.toArray(), null, typeTable, qualifed);

        StreamWrapper parser = new StreamWrapper(xr);
        OMXMLParserWrapper stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(), parser);
        return stAXOMBuilder.getDocumentElement();
    }

    /**
     * @deprecated Please use getUniquePrefix
     */
    public static synchronized String getUniquePrifix() {
        return getUniquePrefix();
    }

    /**
     * increments the namespace counter and returns a new prefix
     *
     * @return unique prefix
     */
    public static synchronized String getUniquePrefix() {
        if (nsCount > 1000) {
            nsCount = 1;
        }
        return "s" + nsCount++;
    }


    private static String getQualifiedName(Package packagez) {
        if (packagez != null) {
            return packagez.getName();
        } else {
            return "";
        }
    }

    private static Object getSimpleTypeObjectChecked(Class classType,
                                                     OMElement omElement) throws AxisFault {
        try {
            return SimpleTypeMapper.getSimpleTypeObject(classType, omElement);
        } catch (NumberFormatException e) {
            MessageContext msgContext = MessageContext.getCurrentMessageContext();
            QName faultCode = msgContext != null ?
                              msgContext.getEnvelope().getVersion().getSenderFaultCode() :
                              null;

            throw new AxisFault("Invalid value \"" + omElement.getText() + "\" for element " +
                                omElement.getLocalName(), faultCode, e);
        }
    }
    
    /**
     * Adds the instance type attribute to the passed OMElement.
     *  
     *  e.g - <sam:obj xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     *                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
     *                xsi:type="xsd:string">
     *                String Value
     *        </sam:obj> 
     *
     *
     * @param fac the SOAPFactory instance.
     * @param element the child OMElement to add attributes.
     * @param resObject the java reflection method
     * @param resObject the res object
     * @param typeTable the type table of particular Axis2 service
     */
    public static void addInstanceTypeAttribute(OMFactory fac,
            OMElement element, Object resObject,
            TypeTable typeTable) {
        if(typeTable == null){
            return;
        }
        OMNamespace xsiNS = fac.createOMNamespace(Constants.XSI_NAMESPACE,
                Constants.DEFAULT_XSI_NAMESPACE_PREFIX);
        OMNamespace xsdNS = fac.createOMNamespace(Constants.XSD_NAMESPACE,
                Constants.DEFAULT_XSD_NAMESPACE_PREFIX);
        element.declareNamespace(xsiNS);
        element.declareNamespace(xsdNS);
        QName xsdType = typeTable.getSchemaTypeName(resObject.getClass()
                .getName());
        String attrValue = xsdType.getPrefix() + ":" + xsdType.getLocalPart();
        element.addAttribute(Constants.XSI_TYPE_ATTRIBUTE, attrValue, xsiNS);
    }
    
    /**
     * Gets the DOOM implementation of org.w3c.dom.Document  
     *
     * @param omElement the OMelement
     * @return the DOOM document
     */
    public static OMDocument convertOMtoDOM(OMContainer omElement) {
        // use an Axiom meta factory with feature "dom" to get org.w3c.dom.Document
        OMFactory doomFactory = OMAbstractFactory.getMetaFactory(
                OMAbstractFactory.FEATURE_DOM).getOMFactory();
        OMXMLParserWrapper doomBuilder = OMXMLBuilderFactory.createStAXOMBuilder(doomFactory,
                omElement.getXMLStreamReader());
        OMDocument domElement = doomBuilder.getDocument();
        return domElement;
    }
    
    /**
     * Convert DOM Document to a OMElement.
     *
     * @param fac the fac
     * @param document the document
     * @return the OMElement
     */
    public static OMElement convertDOMtoOM(OMFactory fac, Object document) {
        if( document == null ) {
            return null;
        }
        if (document instanceof OMDocument) {
            return ((OMDocument)document).getOMDocumentElement();
        
        } else {
            return OMXMLBuilderFactory.createOMBuilder((Document)document, false).getDocumentElement(true);
        }
    }
    
    /**
     * This method deserialize OM model in to a instance of java.util.Map
     *
     * @param parameterArgTypes the parameter argument types of Map <k,V>
     * @param omElement the OMElement
     * @param helper the helper
     * @param parts the parts
     * @param objectSupplier the object supplier
     * @param genericType the generic type
     * @return a instance of java.util.Map
     * @throws AxisFault the axis fault
     */
    public static Map<Object,Object> processGenericsMapElement(Type[] parameterArgTypes,
            OMElement omElement, MultirefHelper helper, Iterator parts,
            ObjectSupplier objectSupplier, Type genericType) throws AxisFault {
        Object objValue;
        Map<Object,Object> valueMap = getMapInstance(genericType) ;
        while (parts.hasNext()) {
            objValue = parts.next();
            if (objValue instanceof OMElement) {
                omElement = (OMElement) objValue;
            } else {
                continue;
            }
            
            if(omElement != null){
                Iterator entryParts = omElement.getChildren();
                Object entryKey = null;
                Object entryValue = null;
                while (entryParts.hasNext()) {
                    objValue = entryParts.next();
                    if (objValue instanceof OMElement) {
                        omElement = (OMElement) objValue;
                    } else {
                        continue;
                    }
                    if (omElement.getLocalName().equals(
                            org.apache.axis2.Constants.MAP_KEY_ELEMENT_NAME)) {
                        entryKey = processMapParameterObject( parameterArgTypes[0], omElement,
                                helper, objectSupplier, genericType);
                        continue;
                    }
                    if (omElement.getLocalName().equals(
                            org.apache.axis2.Constants.MAP_VALUE_ELEMENT_NAME)) {
                        entryValue = processMapParameterObject( parameterArgTypes[1],
                                omElement, helper, objectSupplier, genericType);
                        continue;
                    }
                }
                if(entryKey != null){
                    valueMap.put(entryKey, entryValue);
                }
            }

        }
        return valueMap;
    }
    
    
    /**
     * This method convert a instance of java.util.Map into
     * OM object model for serialization. 
     *
     * @param fac the OMFactory
     * @param type of the java.util.Map
     * @param results the results values
     * @param typeTable the type table
     * @param elementFormDefault the element form default
     * @return list of OMElement
     */
    public static List<OMElement> getMapElement(OMFactory fac, Type type,
            Map results, TypeTable typeTable, boolean elementFormDefault) {
        Iterator<Object> keyItr = results.keySet().iterator();
        List<OMElement> list = new ArrayList<OMElement>();
        OMNamespace ns = null;
        Type keyType = Object.class;
        Type valueType = Object.class;
        if (elementFormDefault) {
            ns = fac.createOMNamespace(
                    org.apache.axis2.Constants.AXIS2_MAP_NAMESPACE_URI,
                    org.apache.axis2.Constants.AXIS2_MAP_NAMESPACE_PREFIX);
        }
        
        if (type instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) type;
            Type[] parameterArgTypes = aType.getActualTypeArguments();
            keyType = parameterArgTypes[0];
            valueType = parameterArgTypes[1];
        }

        while (keyItr.hasNext()) {
            OMElement omEntry;
            Object key = keyItr.next();
            Object value;
            if (key != null) {
                value = results.get(key);
                List<Object> properties = new ArrayList<Object>();
                QName keyName;
                QName valueName;
                if (elementFormDefault) {
                    keyName = new QName(ns.getNamespaceURI(),
                            org.apache.axis2.Constants.MAP_KEY_ELEMENT_NAME,
                            ns.getPrefix());
                    valueName = new QName(ns.getNamespaceURI(),
                            org.apache.axis2.Constants.MAP_VALUE_ELEMENT_NAME,
                            ns.getPrefix());
                } else {
                    keyName = new QName(
                            org.apache.axis2.Constants.MAP_KEY_ELEMENT_NAME);
                    valueName = new QName(
                            org.apache.axis2.Constants.MAP_VALUE_ELEMENT_NAME);
                }

                Object kValue = getMapParameterElement(fac,
                        org.apache.axis2.Constants.MAP_KEY_ELEMENT_NAME, key,
                        keyType, typeTable, ns, elementFormDefault);
                
                Object vValue = getMapParameterElement(fac,
                        org.apache.axis2.Constants.MAP_VALUE_ELEMENT_NAME,
                        value, valueType, typeTable, ns, elementFormDefault);
                
                if(Iterator.class.isAssignableFrom(kValue.getClass())){
                    Iterator valItr = (Iterator) kValue;
                    while (valItr.hasNext()) {
                        properties.add(keyName);
                        properties.add(valItr.next());
                    }
                } else {
                    properties.add(keyName);
                    properties.add(kValue);
                }

                
                if(vValue != null && Iterator.class.isAssignableFrom(vValue.getClass())){
                    Iterator valItr = (Iterator) vValue;
                    while (valItr.hasNext()) {
                        properties.add(valueName);
                        properties.add(valItr.next());
                    }
                } else {
                    properties.add(valueName);
                    properties.add(vValue);
                }
                
                QName entryQName;
                if (elementFormDefault) {
                    entryQName = new QName(ns.getNamespaceURI(),
                            org.apache.axis2.Constants.MAP_ENTRY_ELEMENT_NAME,
                            ns.getPrefix());
                } else {
                    entryQName = new QName(
                            org.apache.axis2.Constants.MAP_ENTRY_ELEMENT_NAME);

                }
                XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(
                        entryQName, properties.toArray(), null,
                        typeTable, elementFormDefault);

                StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(
                        OMAbstractFactory.getOMFactory(), new StreamWrapper(
                                pullParser));
                omEntry = stAXOMBuilder.getDocumentElement();
                list.add(omEntry);
            }
        }
        return list;
    }
    
    /**
     * Helper method to deserialize each parameter of Map.
     *
     * @param paraType the parameter type
     * @param omElement the OMElement
     * @param helper the helper
     * @param objectSupplier the object supplier
     * @param genericType the generic type
     * @return the object
     * @throws AxisFault the axis fault
     */
    private static Object processMapParameterObject(Type paraType, OMElement omElement,
            MultirefHelper helper, ObjectSupplier objectSupplier,
            Type genericType) throws AxisFault {
        if (paraType instanceof ParameterizedType) {
            if (Map.class.isAssignableFrom((Class)
                    ((ParameterizedType) paraType).getRawType())) {
                return processGenericsMapElement(
                        ((ParameterizedType) paraType).getActualTypeArguments(),
                        omElement, helper, omElement.getChildren(),
                        objectSupplier, paraType);
            } else if (Collection.class.isAssignableFrom((Class) 
                    ((ParameterizedType) paraType).getRawType())) {
                return processGenericCollection(
                        omElement,
                        (ParameterizedType) paraType,
                        helper, objectSupplier);
            } else {
                throw new AxisFault("Map parameter does not support for "
                        + ((ParameterizedType) paraType).getRawType());
            }

        } else {
            return processObject(omElement, (Class) paraType, helper, true,
                    objectSupplier, genericType);
        }
    }
    
    /**
     * This method instantiate a Map instance according to the expected 
     * parameter type of the service method. a instance HashMap<Object, Object> 
     * returns as the default value and in case of Exception.   
     *
     * @param genericType the generic type
     * @return the map instance 
     */
    private static Map<Object, Object> getMapInstance(Type genericType) {
        Class rowType;
        if (genericType instanceof ParameterizedType) {
            rowType = (Class) ((ParameterizedType) genericType).getRawType();
        } else {
            rowType = (Class) genericType;
        }
        
        if (Map.class.getName().equals(rowType.getName())) {
            return new HashMap<Object, Object>();
            
        } else if (ConcurrentMap.class.getName().equals(rowType.getName())) {
            return new ConcurrentHashMap<Object, Object>();
            
        } else if (SortedMap.class.getName().equals(rowType.getName())) {
            return new TreeMap<Object, Object>();
            
        } 
//        TODO - Enable this logic once the Axis2 move to Java 1.6.
//        else if (NavigableMap.class.getName().equals(rowType.getName())) {
//            return new TreeMap<Object, Object>();
//            
//        }  else if (ConcurrentNavigableMap.class.getName().equals(rowType.getName())) {
//            return new ConcurrentSkipListMap<Object, Object>();
//        }
//        
        else {
            try {
                return (Map<Object, Object>) rowType.newInstance();
            } catch (Exception e) {
                return new HashMap<Object, Object>();
            }
        }
    }
    
    /**
     * Process the provided return  value and constructs OMElement accordingly.  
     *
     * @param fac the OMFactory instance
     * @param elementName the element name for return OMElement
     * @param value the actual return value
     * @param valueType the value type of return value
     * @param typeTable the type table
     * @param ns the OMNamespace
     * @param elementFormDefault the element form default
     * @return the map parameter object
     */
    private static Object getMapParameterElement(OMFactory fac,
            String elementName, Object value, Type valueType,
            TypeTable typeTable, OMNamespace ns, boolean elementFormDefault) {
         //TODO - key/value can be a Collection, Array , Dom document ,OMElement etc
        if(value == null) {
            return null;
        }
        if (SimpleTypeMapper.isMap(value.getClass())) {
            List<OMElement> childList = getMapElement(fac, valueType,
                    (Map) value, typeTable, elementFormDefault);
            OMElement omValue;
            if(elementFormDefault) {
                omValue = fac.createOMElement(elementName,
                        ns.getNamespaceURI(), ns.getPrefix());
            } else {
                omValue = fac.createOMElement(elementName, null);
                
            }
            for (OMElement child : childList) {
                omValue.addChild(child);
            }
            return omValue;
            
        } else if (SimpleTypeMapper.isCollection(value.getClass())) {
            QName elementQName;
            if(elementFormDefault) {
               elementQName = new QName(ns.getNamespaceURI(), elementName,
                        ns.getPrefix());
            } else {
               elementQName = new QName(elementName);
                
            }
            
            return getCollectionElement(fac, valueType, (Collection) value,
                    elementName, null, elementQName, typeTable,
                    elementFormDefault).getChildren();
            
        } else if(SimpleTypeMapper.isDomDocument((Class)valueType)) {
            return convertDOMtoOM(fac, value);
            
        } else if (SimpleTypeMapper.isObjectType((Class) valueType)) {
            OMElement omValue;
            omValue = fac.createOMElement(elementName, ns);
            if (SimpleTypeMapper.isSimpleType(value)) {
                omValue.addChild(fac.createOMText(SimpleTypeMapper
                        .getStringValue(value)));
            } else {
                QName name;
                if(elementFormDefault) {
                    name = new QName(ns.getNamespaceURI(), elementName,
                            ns.getPrefix());
                } else {
                    name = new QName(elementName);
                }
                XMLStreamReader xr = BeanUtil.getPullParser(value, name,
                        typeTable, true, false);
                OMXMLParserWrapper stAXOMBuilder = OMXMLBuilderFactory
                        .createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                new StreamWrapper(xr));
                omValue = stAXOMBuilder.getDocumentElement();

            }
            addInstanceTypeAttribute(fac, omValue, value, typeTable);
            return omValue;
            
        } else if (SimpleTypeMapper.isSimpleType(value)) {
            OMElement omValue;
            omValue = fac.createOMElement(elementName, ns);
            omValue.addChild(fac.createOMText(SimpleTypeMapper
                    .getStringValue(value)));
            return omValue;
        } 
        return value;
    }
    
    /**
     * Process generic collection.
     *
     * @param omElement the om element
     * @param generictype the generictype
     * @param helper the helper
     * @param objectSupplier the object supplier
     * @return the collection
     * @throws AxisFault the axis fault
     */
    public static Collection<Object> processGenericCollection(OMElement omElement,
            Type generictype, MultirefHelper helper,
            ObjectSupplier objectSupplier) throws AxisFault {
    QName partName = omElement.getQName();
    Type parameter = Object.class;
    if (generictype != null && (generictype instanceof ParameterizedType)) {
        ParameterizedType aType = (ParameterizedType) generictype;
        Type[] parameterArgTypes = aType.getActualTypeArguments();
        parameter = parameterArgTypes[0];
    }
    /*
     * Fix for AXIS2-5090. Use siblings with same QName instead of look for
     * children because list elements available on same level.
     */
    Iterator parts = omElement.getParent().getChildrenWithName(partName);
    return processGenericsElement(parameter, omElement, helper, parts,
        objectSupplier, generictype);
    }
    
    /**
     * Process collection.
     *
     * @param omElement the om element
     * @param toReturn the to return
     * @param generictype the generictype
     * @param helper the helper
     * @param objectSupplier the object supplier
     * @param parts the parts
     * @param bare the bare
     * @return the collection
     * @throws AxisFault the axis fault
     */
    public static Collection<Object> processGenericCollection(OMElement omElement,
        OMElement[] toReturn, Type generictype, MultirefHelper helper,
        ObjectSupplier objectSupplier, Iterator parts, boolean bare)
        throws AxisFault {
        String currentLocalName = omElement.getLocalName();
        Type parameter = Object.class;
        List<OMElement> eleList = new ArrayList<OMElement>();
        // in 'Bare' style no need to add first element to the list.
        if (!bare) {
            eleList.add(omElement);
        }

        if (generictype != null && (generictype instanceof ParameterizedType)) {
            ParameterizedType aType = (ParameterizedType) generictype;
            Type[] parameterArgTypes = aType.getActualTypeArguments();
            parameter = parameterArgTypes[0];
        }

        while (parts.hasNext()) {
            Object objValue = parts.next();
            OMElement currElement;
            if (objValue instanceof OMElement) {
                currElement = (OMElement) objValue;
            } else {
                continue;
            }
            if (currentLocalName.equals(currElement.getLocalName())) {
                eleList.add(currElement);
            } else {
                // This just a container to bring back un-proceeded OMEleemnt.
                toReturn[0] = currElement;
                break;
            }
        }
        return processGenericsElement(parameter, omElement, helper,
            eleList.iterator(), objectSupplier, generictype);
    }

    /**
     * Gets the collection element.
     *
     * @param fac the fac
     * @param type the type
     * @param results the results
     * @param name the name
     * @param innerName the inner name
     * @param elementQName the element q name
     * @param typeTable the type table
     * @param elementFormDefault the element form default
     * @return the collection element
     */
    public static OMElement getCollectionElement(OMFactory fac, Type type,
        Collection results, String name, String innerName,
        QName elementQName, TypeTable typeTable, boolean elementFormDefault) {

        String elementName = (innerName == null) ? name : innerName;
        Iterator<Object> itr = results.iterator();
        List<Object> properties = new ArrayList<Object>();
        OMNamespace ns = fac.createOMNamespace(elementQName.getNamespaceURI(),
            elementQName.getPrefix());
        Type valueType = Object.class;
        if (type instanceof ParameterizedType) {
            ParameterizedType aType = (ParameterizedType) type;
            Type[] parameterArgTypes = aType.getActualTypeArguments();
            valueType = parameterArgTypes[0];
        }

        while (itr.hasNext()) {
            Object value = itr.next();
            if (value != null) {
                value = getCollectionItemElement(fac, elementName, value,
                        valueType, typeTable, ns, elementFormDefault);
                QName valueQName;
                if (elementFormDefault) {
                    valueQName = new QName(ns.getNamespaceURI(), elementName,
                            ns.getPrefix());
                } else {
                    valueQName = new QName(elementName);
                }
                properties.add(valueQName);
                properties.add(value);
            }
        }
        QName eleQName;
        if (elementFormDefault) {
            eleQName = new QName(ns.getNamespaceURI(),
                    elementQName.getLocalPart(), ns.getPrefix());
        } else {
            eleQName = new QName(elementQName.getLocalPart());
        }
        XMLStreamReader pullParser = new ADBXMLStreamReaderImpl(eleQName, properties.toArray(), null, typeTable,
            elementFormDefault);

        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(
            OMAbstractFactory.getOMFactory(), new StreamWrapper(pullParser));
        return stAXOMBuilder.getDocumentElement();
    }

    /**
     * Gets the collection item element.
     *
     * @param fac the fac
     * @param elementName the element name
     * @param value the value
     * @param valueType the value type
     * @param typeTable the type table
     * @param ns the ns
     * @param elementFormDefault the element form default
     * @return the collection item element
     */
    private static Object getCollectionItemElement(OMFactory fac,
            String elementName, Object value, Type valueType,
            TypeTable typeTable, OMNamespace ns, boolean elementFormDefault) {
        if (SimpleTypeMapper.isMap(value.getClass())) {
            List<OMElement> childList = getMapElement(fac, valueType,
                (Map) value, typeTable, elementFormDefault);
            OMElement omValue = fac.createOMElement(elementName,
                ns.getNamespaceURI(), ns.getPrefix());
            for (OMElement child : childList) {
                omValue.addChild(child);
            }
            return omValue;
    
        } else if (SimpleTypeMapper.isCollection(value.getClass())) {
            return getCollectionElement(
                fac,
                valueType,
                (Collection) value,
                elementName,
                Constants.INNER_ARRAY_COMPLEX_TYPE_NAME,
                new QName(ns.getNamespaceURI(), elementName, ns.getPrefix()),
                typeTable, elementFormDefault);
        } else if (SimpleTypeMapper.isObjectType((Class) valueType)) {
            OMElement omValue;
            omValue = fac.createOMElement(elementName, ns);
            if (SimpleTypeMapper.isSimpleType(value)) {
                omValue.addChild(fac.createOMText(SimpleTypeMapper
                    .getStringValue(value)));
            } else {
                QName name;
                if (elementFormDefault) {
                    name = new QName(ns.getNamespaceURI(), elementName,
                            ns.getPrefix());
                } else {
                    name = new QName(elementName);
                }
                XMLStreamReader xr = BeanUtil.getPullParser(value, name,
                        typeTable, true, false);
                OMXMLParserWrapper stAXOMBuilder = OMXMLBuilderFactory
                        .createStAXOMBuilder(OMAbstractFactory.getOMFactory(),
                                new StreamWrapper(xr));
                omValue = stAXOMBuilder.getDocumentElement();

            }
            addInstanceTypeAttribute(fac, omValue, value, typeTable);
            return omValue;
        }
        return value;
    }
    
    /**
     * Gets the collection instance object according to the genericType passed.
     *
     * @param genericType the generic type
     * @return the collection instance
     */
    private static Collection<Object> getCollectionInstance(Type genericType) {
        Class rowType;
        if (genericType instanceof ParameterizedType) {
            rowType = (Class) ((ParameterizedType) genericType).getRawType();
        } else {
            rowType = (Class) genericType;
        }

        if (Collection.class.getName().equals(rowType.getName())
            || List.class.getName().equals(rowType.getName())) {
            return new ArrayList<Object>();

        } else if (Set.class.getName().equals(rowType.getName())) {
            return new HashSet<Object>();

        } else if (Queue.class.getName().equals(rowType.getName())) {
            return new LinkedList<Object>();

        } else if (BlockingQueue.class.getName().equals(rowType.getName())) {
            return new LinkedBlockingQueue<Object>();

        } 
//        TODO - Enable this logic once the Axis2 move to Java 1.6.
//        else if (BlockingDeque.class.getName().equals(rowType.getName())) {
//        return new LinkedBlockingDeque<Object>();
//
//        }else if (NavigableSet.class.getName().equals(rowType.getName())
//            || SortedSet.class.getName().equals(rowType.getName())) {
//        return new TreeSet<Object>();
//
//        }
        else {
            try {
                return (Collection<Object>) rowType.newInstance();
            } catch (Exception e) {
                return new ArrayList<Object>();
            }
        }
    }
    
    private static XMLGregorianCalendar getXMLGregorianCalendar(
            OMElement beanElement) throws DatatypeConfigurationException {
        String greCal = beanElement.getText();
        XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(greCal);
        return xmlCal;
    }

}
