package org.apache.axis2.schema.util;

import java.util.List;
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
 */

/**
 * A simple utiliy to find whether a given class name is
 * primitive or not
 */
public class PrimitiveTypeFinder {

    private static List primitiveClassNameList;

    static{
        primitiveClassNameList =  new ArrayList();
        //add the java primitive class names
        primitiveClassNameList.add(int.class.getName());
        primitiveClassNameList.add(long.class.getName());
        primitiveClassNameList.add(byte.class.getName());
        primitiveClassNameList.add(double.class.getName());
        primitiveClassNameList.add(boolean.class.getName());
        primitiveClassNameList.add(float.class.getName());
        primitiveClassNameList.add(short.class.getName());
        primitiveClassNameList.add(char.class.getName());

    }

    /**
     *
     * @param className
     * @return
     */
    public static boolean isPrimitive(String className){
        return primitiveClassNameList.contains(className);
    }
}
