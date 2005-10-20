package org.apache.axis2.util;


import org.apache.axis2.rpc.receivers.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.ADBPullParser;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.beans.PropertyDescriptor;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.BeanInfo;
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

public class BeanSerializerUtil {


    /**
     * To Serilize Bean object this method is used, this will create an object array using given
     * bean object
     * @param beanObject
     * @param beanName
     */
    public static XMLStreamReader getPullParser(Object beanObject, QName beanName ) {
        try {
            Field [] fields = beanObject.getClass().getDeclaredFields();
            ArrayList objetc = new ArrayList();
            for (int i = 0; i < fields.length; i++) {
                Field filed = fields[i];
                Class parameters = filed.getType();
                if(SimpleTypeMapper.isSimpleType(parameters)){
                    objetc.add(filed.getName());
                    filed.setAccessible(true);
                    objetc.add(filed.get(beanObject).toString());
                    filed.setAccessible(false);
                } else {
                    objetc.add(new QName(filed.getName()));
                    filed.setAccessible(true);
                    objetc.add(filed.get(beanObject));
                    filed.setAccessible(false);
                }
            }
            return ADBPullParser.createPullParser(beanName, objetc.toArray(), null);
            // TODO : Deepal fix this. I added another parameter to the above method in the ADBPullPrser
            // to get the attributes array. For the time being I passed null. Pass attributes array here.

        } catch (IllegalAccessException e) {
            //todo has to throw this exeception
            return null;
        }
    }

    public static Object deserialize(Class beanClass, OMElement beanElement) throws AxisFault{
        Object beanObj ;
        try {
            HashMap properties = new HashMap() ;
            BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
            PropertyDescriptor [] propDescs = beanInfo.getPropertyDescriptors();
            for (int i = 0; i < propDescs.length; i++) {
                PropertyDescriptor proprty = propDescs[i];
                properties.put(proprty.getName(), proprty);
            }

            beanObj = beanClass.newInstance();
            Iterator elements = beanElement.getChildren();
            while (elements.hasNext()) {
                OMElement parts = (OMElement) elements.next();
//                if parts/@href != null then need to find element with id and deserialize. before that first check wheher already have in hashtable
                String partsLocalName = parts.getLocalName();
                PropertyDescriptor prty =(PropertyDescriptor)properties.get(partsLocalName.toLowerCase());
//                if (prty == null) {
                /**
                 * I think this can be happen , that is because there is a method whcih take Man
                 * object and request can contain a Employee object (which extend Man) , there for
                 * Employee may have more field than Man , so no need to thow an exception
                 */
//                    throw new AxisFault("User Error , In vaild bean ! prty does not exist " + "set" +
//                            partsLocalName);
                if(prty !=null){
                    Class parameters = prty.getPropertyType();
                    if (prty.equals("class"))
                        continue;
                    Object partObj = SimpleTypeMapper.getSimpleTypeObject(parameters, parts);
                    if (partObj == null) {
                        partObj = deserialize(parameters, parts);
                    }
                    Object [] parms = new Object[]{partObj};
                    prty.getWriteMethod().invoke(beanObj,parms);
                }
            }
        } catch (InstantiationException e) {
            throw new AxisFault("InstantiationException : " + e);
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
     * in that case that element will be converted to the JavaType specified by the returnTypes array
     * The algo is as follows, get the childerns of the response element , and if it conatian more than
     * one element then check the retuen type of that element and conver that to corresponding JavaType
     * @param response    OMElement
     * @param returnTypes Array of JavaTypes
     * @return  Array of objects
     * @throws AxisFault
     */
    public static Object [] deserialize(OMElement response , Object [] returnTypes ) throws AxisFault {
         /**
         * Take the number of paramters in the method and , only take that much of child elements
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
        int length = returnTypes.length;
        int count =0;
        Object [] retObjs = new Object[length];
        Iterator parts = response.getChildren();
        while (parts.hasNext() && count < length) {
            OMElement omElement = (OMElement) parts.next();
            Class classType = (Class)returnTypes[count];
            if(OMElement.class.isAssignableFrom(classType)){
                retObjs[count] =omElement;
            }  else if(SimpleTypeMapper.isSimpleType(classType)){
                retObjs[count]  = SimpleTypeMapper.getSimpleTypeObject(classType, omElement);
            } else {
                retObjs[count] = BeanSerializerUtil.deserialize(classType, omElement);
            }
            count ++;
        }
        return  retObjs;
    }

}
