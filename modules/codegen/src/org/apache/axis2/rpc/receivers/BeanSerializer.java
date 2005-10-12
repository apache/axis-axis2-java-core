package org.apache.axis2.rpc.receivers;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.AxisFault;

import java.util.Iterator;
import java.util.HashMap;
import java.lang.reflect.Field;
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

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 12, 2005
 * Time: 10:36:58 AM
 */
public class BeanSerializer {

    private Class beanClass;
    private OMElement beanElement;
    private HashMap fields;

    public BeanSerializer(Class beanClass, OMElement beanElement) {
        this.beanClass = beanClass;
        this.beanElement = beanElement;
        this.fields = new HashMap();
        fillMethods();
    }

    public Object deserilze() throws AxisFault {
        Object beanObj ;
        try {
            beanObj = beanClass.newInstance();
            Iterator elements = beanElement.getChildren();
            while (elements.hasNext()) {
                OMElement parts = (OMElement) elements.next();
                String partsLocalName = parts.getLocalName();

                //getting the setter field
                Field field =(Field)fields.get(partsLocalName);
                if(field == null){
                    throw new AxisFault("User Error , In vaild bean ! field does not exist " + "set" +
                            partsLocalName);
                } else {
                    Class parameters = field.getType();
                    Object partObj = SimpleTypeMapper.getSimpleTypeObject(parameters,parts);
                    if(partObj == null){
                        // Assuming paramter itself as a bean
                        partObj = new BeanSerializer(parameters,parts).deserilze();
                    }
//                    Object [] parms = new Object[]{partObj};
                    field.setAccessible(true);
                    field.set(beanObj,partObj);
                    field.setAccessible(false);
                }


            }
        } catch (InstantiationException e) {
            throw new AxisFault("InstantiationException : " + e);
        } catch (IllegalAccessException e) {
            throw new AxisFault("IllegalAccessException : " + e);
        }
        return beanObj;
    }

    /**
     * This will fill the hashmap by getting all the methods in a given class , since it make easier
     * to acess latter
     */
    private void fillMethods(){

        Field [] fields = beanClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            this.fields.put(field.getName(),field);
        }


//
//        Method[] methods = beanClass.getMethods();
//        for (int i = 0; i < methods.length; i++) {
//            Method method = methods[i];
//            fields.put(method.getName(),method);
//        }
    }

}
