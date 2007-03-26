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

package org.apache.axis2.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Counter uses java.util.concurrent.atomic.AtomicLong if present,
// else falls back to the backport version
public class Counter {
    private static Class clazz;
    private static Method method;
    private Object counter;

    static {
        try {
            clazz = Class.forName("java.util.concurrent.atomic.AtomicLong");
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName("edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicLong");
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException(e1);
            }
        }
        try {
            method = clazz.getMethod("incrementAndGet", new Class[]{});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Counter() {
        try {
            counter = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public final synchronized long incrementAndGet() {
        try {
            return ((Long) method.invoke(counter, new Object[]{})).longValue();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
