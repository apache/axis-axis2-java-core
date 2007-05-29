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
*/
package org.apache.axis2.databinding.utils;


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.axis2.util.StreamWrapper;
import org.apache.axis2.util.Loader;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JProperty;
import org.codehaus.jam.JamClassIterator;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;


public class BeanUtil {

    private static int nsCount = 1;

    private static final Log log = LogFactory.getLog(BeanUtil.class);

    /**
     * To Serilize Bean object this method is used, this will create an object array using given
     * bean object
     *
     * @param beanObject
     * @param beanName
     */
    public static XMLStreamReader getPullParser(Object beanObject,
                                                QName beanName,
                                                TypeTable typeTable, boolean qualified) {
        try {
            JamServiceFactory factory = JamServiceFactory.getInstance();
            JamServiceParams jam_service_parms = factory.createServiceParams();
            ClassLoader cl = beanObject.getClass().getClassLoader();
            if (cl == null)
                cl = ClassLoader.getSystemClassLoader();
            jam_service_parms.addClassLoader(cl);

            jam_service_parms.includeClass(beanObject.getClass().getName());
            JamService service = factory.createService(jam_service_parms);
            JamClassIterator jClassIter = service.getClasses();
            JClass jClass;
            if (jClassIter.hasNext()) {
                jClass = (JClass)jClassIter.next();
            } else {
                throw new AxisFault("No service class found , exception from JAM");
            }
            QName elemntNameSpace = null;
            if (typeTable != null && qualified) {
                QName qNamefortheType =
                        typeTable.getQNamefortheType(beanObject.getClass().getName());
                if (qNamefortheType == null) {
                    qNamefortheType = typeTable.getQNamefortheType(
                            beanObject.getClass().getPackage().getName());
                }
                if (qNamefortheType == null) {
                    throw new AxisFault("Mapping qname not fond for the package: " +
                            beanObject.getClass().getPackage().getName());
                }

                elemntNameSpace = new QName(qNamefortheType.getNamespaceURI(),
                                            "elementName");
            }

            // properties from JAM
            ArrayList propertyList = new ArrayList();
            JProperty properties [] = jClass.getDeclaredProperties();
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                propertyList.add(property);
            }
            JClass supClass = jClass.getSuperclass();
            while (!"java.lang.Object".equals(supClass.getQualifiedName())) {
                properties = supClass.getDeclaredProperties();
                for (int i = 0; i < properties.length; i++) {
                    JProperty property = properties[i];
                    propertyList.add(property);
                }
                supClass = supClass.getSuperclass();
            }
            properties = new JProperty[propertyList.size()];
            for (int i = 0; i < propertyList.size(); i++) {
                JProperty jProperty = (JProperty)propertyList.get(i);
                properties[i] = jProperty;
            }
            Arrays.sort(properties);
            BeanInfo beanInfo = Introspector.getBeanInfo(beanObject.getClass());
            PropertyDescriptor [] propDescs = beanInfo.getPropertyDescriptors();
            HashMap propertMap = new HashMap();
            for (int i = 0; i < propDescs.length; i++) {
                PropertyDescriptor propDesc = propDescs[i];
                propertMap.put(propDesc.getName(), propDesc);
            }
            ArrayList object = new ArrayList();
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                PropertyDescriptor propDesc = (PropertyDescriptor)propertMap.get(
                        getCorrectName(property.getSimpleName()));
                if (propDesc == null) {
                    // JAM does bad thing so I need to add this
                    continue;
                }
                Class ptype = propDesc.getPropertyType();
                if (propDesc.getName().equals("class")) {
                    continue;
                }
                if (SimpleTypeMapper.isSimpleType(ptype)) {
                    Object value = propDesc.getReadMethod().invoke(beanObject,
                                                                   null);
                    if (elemntNameSpace != null) {
                        object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                             propDesc.getName(), elemntNameSpace.getPrefix()));
                    } else {
                        object.add(new QName(beanName.getNamespaceURI(),
                                             propDesc.getName(), beanName.getPrefix()));
                    }
                    object.add(value == null ? null : SimpleTypeMapper.getStringValue(value));
                } else if (ptype.isArray()) {
                    if (SimpleTypeMapper.isSimpleType(ptype.getComponentType())) {
                        Object value = propDesc.getReadMethod().invoke(beanObject,
                                                                       null);
                        if (value != null) {
                            int i1 = Array.getLength(value);
                            for (int j = 0; j < i1; j++) {
                                Object o = Array.get(value, j);
                                if (elemntNameSpace != null) {
                                    object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                         propDesc.getName(),
                                                         elemntNameSpace.getPrefix()));
                                } else {
                                    object.add(new QName(beanName.getNamespaceURI(),
                                                         propDesc.getName(), beanName.getPrefix()));
                                }
                                object.add(o == null ? null : SimpleTypeMapper.getStringValue(o));
                            }
                        } else {
                            if (elemntNameSpace != null) {
                                object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                     propDesc.getName(),
                                                     elemntNameSpace.getPrefix()));
                            } else {
                                object.add(new QName(beanName.getNamespaceURI(),
                                                     propDesc.getName(), beanName.getPrefix()));
                            }
                            object.add(value);
                        }

                    } else {
                        Object value [] = (Object[])propDesc.getReadMethod().invoke(beanObject,
                                                                                    null);
                        if (value != null) {
                            for (int j = 0; j < value.length; j++) {
                                Object o = value[j];
                                if (elemntNameSpace != null) {
                                    object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                         propDesc.getName(),
                                                         elemntNameSpace.getPrefix()));
                                } else {
                                    object.add(new QName(beanName.getNamespaceURI(),
                                                         propDesc.getName(), beanName.getPrefix()));
                                }
                                object.add(o);
                            }
                        } else {
                            if (elemntNameSpace != null) {
                                object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                     propDesc.getName(),
                                                     elemntNameSpace.getPrefix()));
                            } else {
                                object.add(new QName(beanName.getNamespaceURI(),
                                                     propDesc.getName(), beanName.getPrefix()));
                            }
                            object.add(value);
                        }
                    }
                } else if (SimpleTypeMapper.isCollection(ptype)) {
                    Object value = propDesc.getReadMethod().invoke(beanObject,
                                                                   null);
                    Collection objList = (Collection)value;
                    if (objList != null && objList.size() > 0) {
                        //this was given error , when the array.size = 0
                        // and if the array contain simple type , then the ADBPullParser asked
                        // PullParser from That simpel type
                        for (Iterator j = objList.iterator(); j.hasNext();) {
                            Object o = j.next();
                            if (SimpleTypeMapper.isSimpleType(o)) {
                                if (elemntNameSpace != null) {
                                    object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                         propDesc.getName(),
                                                         elemntNameSpace.getPrefix()));
                                } else {
                                    object.add(new QName(beanName.getNamespaceURI(),
                                                         propDesc.getName(), beanName.getPrefix()));
                                }
                                object.add(o);
                            } else {
                                if (elemntNameSpace != null) {
                                    object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                         propDesc.getName(),
                                                         elemntNameSpace.getPrefix()));
                                } else {
                                    object.add(new QName(beanName.getNamespaceURI(),
                                                         propDesc.getName(), beanName.getPrefix()));
                                }
                                object.add(o);
                            }
                        }

                    } else {
                        if (elemntNameSpace != null) {
                            object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                                 propDesc.getName(), elemntNameSpace.getPrefix()));
                        } else {
                            object.add(new QName(beanName.getNamespaceURI(),
                                                 propDesc.getName(), beanName.getPrefix()));
                        }
                        object.add(value);
                    }
                } else {
                    if (elemntNameSpace != null) {
                        object.add(new QName(elemntNameSpace.getNamespaceURI(),
                                             propDesc.getName(), elemntNameSpace.getPrefix()));
                    } else {
                        object.add(new QName(beanName.getNamespaceURI(),
                                             propDesc.getName(), beanName.getPrefix()));
                    }
                    Object value = propDesc.getReadMethod().invoke(beanObject,
                                                                   null);
                    object.add(value);
                }
            }
             // Added objectAttributes as a fix for issues AXIS2-2055 and AXIS2-1899 to 
            // support polymorphism in POJO approach.
            // For some reason, using QName(Constants.XSI_NAMESPACE, "type", "xsi") does not generate
            // an xsi:type attribtue properly for inner objects. So just using a simple QName("type").
            ArrayList objectAttributes = new ArrayList();
            objectAttributes.add(new QName("type"));
            objectAttributes.add(beanObject.getClass().getName());
            return new ADBXMLStreamReaderImpl(beanName, object.toArray(), null,
                                              typeTable, qualified);

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
        return getPullParser(beanObject, new QName(className), null, false);
    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     ObjectSupplier objectSupplier,
                                     String arrayLocalName)
            throws AxisFault {
        Object beanObj =null;
        try {
            // Added this block as a fix for issues AXIS2-2055 and AXIS2-1899
            // to support polymorphism in POJO approach.
            // Retrieve the type name of the instance from the 'type' attribute
            // and retrieve the class.
            String instanceTypeName = beanElement.getAttributeValue(new QName("type"));
            if ((instanceTypeName != null) && (! beanClass.isArray())) {
                try {
                    beanClass = Loader.loadClass(beanClass.getClassLoader(), instanceTypeName);
                } catch (ClassNotFoundException ce) {
                    throw AxisFault.makeFault(ce);
                }
            }
   
            if (beanClass.isArray()) {
                ArrayList valueList = new ArrayList();
                Class arrayClassType = beanClass.getComponentType();
                Iterator parts = beanElement.getChildElements();
                OMElement omElement;
                while (parts.hasNext()) {
                    Object objValue = parts.next();
                    if (objValue instanceof OMElement) {
                        omElement = (OMElement)objValue;
                        if (!arrayLocalName.equals(omElement.getLocalName())) {
                            continue;
                        }
                        Object obj = deserialize(arrayClassType,
                                                 omElement,
                                                 objectSupplier, null);
                        if (obj != null) {
                            valueList.add(obj);
                        }
                    }
                }
                return ConverterUtil.convertToArray(arrayClassType,
                                                    valueList);
            } else {
                if (SimpleTypeMapper.isSimpleType(beanClass)) {
                    return SimpleTypeMapper.getSimpleTypeObject(beanClass, beanElement);
                }
                HashMap properties = new HashMap();
                BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
                PropertyDescriptor [] propDescs = beanInfo.getPropertyDescriptors();
                for (int i = 0; i < propDescs.length; i++) {
                    PropertyDescriptor proprty = propDescs[i];
                    properties.put(proprty.getName(), proprty);
                }
                 beanObj = objectSupplier.getObject(beanClass);
                boolean tuched = false;
                Iterator elements = beanElement.getChildren();
                while (elements.hasNext()) {
                    // the beanClass could be an abstract one.
                    // so create an instance only if there are elements, in
                    // which case a concrete subclass is available to instantiate.
                    if (beanObj == null) {
                        beanObj = objectSupplier.getObject(beanClass);
                    }
                    OMElement parts;
                    Object objValue = elements.next();
                    if (objValue instanceof OMElement) {
                        parts = (OMElement)objValue;
                    } else {
                        continue;
                    }
                    // if parts/@href != null then need to find element with id and deserialize.
                    // before that first check whether we already have it in the hashtable
                    String partsLocalName = parts.getLocalName();
                    PropertyDescriptor prty = (PropertyDescriptor)properties.get(partsLocalName);
                    if (prty != null) {
                        Class parameters = prty.getPropertyType();
                        if (prty.equals("class"))
                            continue;

                        Object partObj;
                        if (SimpleTypeMapper.isSimpleType(parameters)) {
                            partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                        } else if (SimpleTypeMapper.isCollection(parameters)) {
                            partObj = SimpleTypeMapper.getArrayList((OMElement)
                                    parts.getParent(), prty.getName());
                        } else if (parameters.isArray()) {
                            partObj = deserialize(parameters, (OMElement)parts.getParent(),
                                                  objectSupplier, prty.getName());
                        } else {
                            partObj = deserialize(parameters, parts, objectSupplier, null);
                        }
                        Object [] parms = new Object[] { partObj };
                        Method writeMethod = prty.getWriteMethod();
                        if (writeMethod != null) {
                            writeMethod.invoke(beanObj, parms);
                        }
                        tuched = true;
                    }
                }
                if (tuched) {
                    return beanObj;
                } else {
                    return null;
                }
            }
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        } catch (InvocationTargetException e) {
            throw new AxisFault("InvocationTargetException : " + e);
        } catch (IntrospectionException e) {
            throw new AxisFault("IntrospectionException : " + e);
        }


    }

    public static Object deserialize(Class beanClass,
                                     OMElement beanElement,
                                     MultirefHelper helper,
                                     ObjectSupplier objectSupplier) throws AxisFault {
        Object beanObj;
        try {
            HashMap properties = new HashMap();
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor [] propDescs = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propDescs.length; i++) {
                PropertyDescriptor proprty = propDescs[i];
                properties.put(proprty.getName(), proprty);
            }

            beanObj = objectSupplier.getObject(beanClass);
            Iterator elements = beanElement.getChildren();
            while (elements.hasNext()) {
                Object child = elements.next();
                OMElement parts;
                if (child instanceof OMElement) {
                    parts = (OMElement)child;
                } else {
                    continue;
                }
                String partsLocalName = parts.getLocalName();
                PropertyDescriptor prty = (PropertyDescriptor)properties.get(
                        partsLocalName.toLowerCase());
                if (prty != null) {
                    Class parameters = prty.getPropertyType();
                    if (prty.equals("class"))
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
                    Object [] parms = new Object[] { partObj };
                    Method writeMethod = prty.getWriteMethod();
                    if (writeMethod != null) {
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
     * To get JavaObjects from XML elemnt , the element most of the time contains only one element
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
    public static Object [] deserialize(OMElement response,
                                        Object [] javaTypes,
                                        ObjectSupplier objectSupplier) throws AxisFault {
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
        Object [] retObjs = new Object[length];

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
        MultirefHelper helper = new MultirefHelper((OMElement)response.getParent());
        //to support array . if the parameter type is array , then all the omelemnts with that paramtre name
        // has to  get and add to the list
        Class classType;
        String currentLocalName;
        while (parts.hasNext() && count < length) {
            Object objValue = parts.next();
            OMElement omElement;
            if (objValue instanceof OMElement) {
                omElement = (OMElement)objValue;
            } else {
                continue;
            }
            currentLocalName = omElement.getLocalName();
            classType = (Class)javaTypes[count];
            omElement = ProcessElement(classType, omElement, helper, parts,
                                       currentLocalName, retObjs, count, objectSupplier);
            while (omElement != null) {
                count ++;
                omElement = ProcessElement((Class)javaTypes[count], omElement,
                                           helper, parts, omElement.getLocalName(), retObjs, count,
                                           objectSupplier);
            }
            count ++;
        }

        // Ensure that we have at least a zero element array
        for (int i = 0; i < length; i++) {
            Class clazz = (Class)javaTypes[i];
            if (retObjs[i] == null && clazz.isArray()) {
                retObjs[i] = Array.newInstance(clazz.getComponentType(), 0);
            }
        }

        helper.clean();
        return retObjs;
    }

    private static OMElement ProcessElement(Class classType, OMElement omElement,
                                            MultirefHelper helper, Iterator parts,
                                            String currentLocalName,
                                            Object[] retObjs,
                                            int count,
                                            ObjectSupplier objectSupplier) throws AxisFault {
        Object objValue;
        if (classType.isArray()) {
            boolean done = true;
            ArrayList valueList = new ArrayList();
            Class arrayClassType = classType.getComponentType();
            if ("byte".equals(arrayClassType.getName())) {
                retObjs[count] =
                        processObject(omElement, arrayClassType, helper, true, objectSupplier);
                return null;
            } else {
                valueList.add(processObject(omElement, arrayClassType, helper, true,
                                            objectSupplier));
            }
            while (parts.hasNext()) {
                objValue = parts.next();
                if (objValue instanceof OMElement) {
                    omElement = (OMElement)objValue;
                } else {
                    continue;
                }
                if (!currentLocalName.equals(omElement.getLocalName())) {
                    done = false;
                    break;
                }
                Object o = processObject(omElement, arrayClassType,
                                         helper, true, objectSupplier);
                valueList.add(o);
            }
            retObjs[count] = ConverterUtil.convertToArray(arrayClassType,
                                                          valueList);
            if (!done) {
                return omElement;
            }
        } else {
            //handling refs
            retObjs[count] = processObject(omElement, classType, helper, false, objectSupplier);
        }
        return null;
    }

    public static Object processObject(OMElement omElement,
                                       Class classType,
                                       MultirefHelper helper,
                                       boolean isArrayType,
                                       ObjectSupplier objectSupplier) throws AxisFault {
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
                    return helper.processRef(classType, ref, objectSupplier);
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
                        return Base64.decode(value);
                    } else {
                        return SimpleTypeMapper.getSimpleTypeObject(classType, omElement);
                    }
                } else if (SimpleTypeMapper.isCollection(classType)) {
                    return SimpleTypeMapper.getArrayList(omElement);
                } else if (SimpleTypeMapper.isDataHandler(classType)) {
                    return SimpleTypeMapper.getDataHandler(omElement);
                } else {
                    return BeanUtil.deserialize(classType, omElement, objectSupplier, null);
                }
            }
        }
    }

    public static OMElement getOMElement(QName opName,
                                         Object [] args,
                                         QName partName,
                                         boolean qualifed,
                                         TypeTable typeTable) {
        ArrayList objects;
        objects = new ArrayList();
        int argCount = 0;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                objects.add("item" + i);
                objects.add(arg);
                continue;
            }
            //todo if the request parameter has name other than argi (0<i<n) , there should be a
            //way to do that , to solve that problem we need to have RPCRequestParameter
            //note that The value of request parameter can either be simple type or JavaBean
            if (arg instanceof Object[]) {
                Object array [] = (Object[])arg;
                for (int j = 0; j < array.length; j++) {
                    Object o = array[j];
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
                                if (partName == null) {
                                    wrappingElement = fac.createOMElement("item" + argCount, null);
                                    wrappingElement.addChild((OMElement)o);
                                } else {
                                    wrappingElement = fac.createOMElement(partName, null);
                                    wrappingElement.addChild((OMElement)o);
                                }
                                objects.add(wrappingElement);
                            } else {
                                objects.add(o);
                            }
                        }
                    }
                }
            } else {
                if (SimpleTypeMapper.isSimpleType(arg)) {
                    if (partName == null) {
                        objects.add("arg" + argCount);
                    } else {
                        objects.add(partName);
                    }
                    objects.add(SimpleTypeMapper.getStringValue(arg));
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
                            wrappingElement.addChild((OMElement)arg);
                        } else {
                            wrappingElement = fac.createOMElement(partName, null);
                            wrappingElement.addChild((OMElement)arg);
                        }
                        objects.add(wrappingElement);
                    } else if (arg instanceof byte[]) {
                        objects.add(Base64.encode((byte[])arg));
                    } else {
                        objects.add(arg);
                    }
                }
            }
            argCount ++;
        }

        XMLStreamReader xr =
                new ADBXMLStreamReaderImpl(opName, objects.toArray(), null, typeTable, qualifed);

        StreamWrapper parser = new StreamWrapper(xr);
        StAXOMBuilder stAXOMBuilder =
                OMXMLBuilderFactory.createStAXOMBuilder(
                        OMAbstractFactory.getSOAP11Factory(), parser);
        return stAXOMBuilder.getDocumentElement();
    }

    /** @deprecated Please use getUniquePrefix */
    public static String getUniquePrifix() {
        return "s" + nsCount++;
    }

    /**
     * increments the namespace counter and returns a new prefix
     *
     * @return unique prefix
     */
    public static String getUniquePrefix() {
        return "s" + nsCount++;
    }


    /**
     * JAM convert first name of an attribute into UpperCase as an example if there is a instance
     * variable called foo in a bean , then Jam give that as Foo so this method is to correct that
     * error
     *
     * @param wrongName
     * @return the right name, using english as the locale for case conversion
     */
    private static String getCorrectName(String wrongName) {
        if (wrongName.length() > 1) {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH)
                    + wrongName.substring(1, wrongName.length());
        } else {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH);
        }
    }

}
