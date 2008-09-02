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

package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class DefaultObjectSupplier implements ObjectSupplier {

    public Object getObject(Class clazz) throws AxisFault {
        try {
            return clazz.newInstance();
        } catch (Exception exception) {
            String className = clazz.getName();
            // if this is an inner class then that can be a non static inner class. those classes have to be instanciate
            // in a different way than a normal initialization
            if (className.indexOf("$") > 0) {
                String outerClassName = className.substring(0, className.indexOf("$"));
                try {
                    Class outerClass = Class.forName(outerClassName);
                    Object outerClassObject = outerClass.newInstance();
                    Constructor innterClassConstructor = clazz.getConstructor(new Class[]{outerClass});
                    return innterClassConstructor.newInstance(new Object[]{outerClassObject});
                } catch (ClassNotFoundException e) {
                    throw AxisFault.makeFault(e);
                } catch (IllegalAccessException e) {
                    throw AxisFault.makeFault(e);
                } catch (InstantiationException e) {
                    throw AxisFault.makeFault(e);
                } catch (NoSuchMethodException e) {
                    throw AxisFault.makeFault(e);
                } catch (InvocationTargetException e) {
                    throw AxisFault.makeFault(e);
                }
            }
            throw AxisFault.makeFault(exception);
        }
    }
}
