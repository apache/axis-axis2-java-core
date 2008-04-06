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

package org.apache.axis2.extensions.osgi.util;

import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class BundleClassLoader extends ClassLoader {
    private final Bundle bundle;

    public BundleClassLoader(Bundle bundle, ClassLoader parent) {
        super(parent);
        this.bundle = bundle;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return bundle.loadClass(name);
        } catch (Exception e) {
            return super.loadClass(name);
        }
    }

    public URL findResource(String name) {
        URL resource = bundle.getResource(name);
        if (resource != null) {
            return resource;
        }
        return super.findResource(name);
    }

    public Enumeration findResources(String name) throws IOException {
        Enumeration enumeration = bundle.getResources(name);
        if (enumeration != null) {
            return enumeration;
        }
        return super.findResources(name);
    }

    public URL getResource(String name) {
        URL resource = findResource(name);
        if (resource != null) {
            return resource;
        }
        return super.getResource(name);
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz;
        try {
            clazz = findClass(name);
        }
        catch (ClassNotFoundException cnfe) {
            clazz = super.loadClass(name, resolve);
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }
}
