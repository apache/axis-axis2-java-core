package org.apache.axis2.util;

import org.apache.axis2.databinding.utils.ADBPullParser;
import org.apache.axis2.rpc.receivers.SimpleTypeMapper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
 * Time: 12:52:36 PM
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
            return ADBPullParser.createPullParser(beanName, objetc.toArray());
        } catch (IllegalAccessException e) {
           //todo has to throw this exeception
           return null;
        }
    }
}
