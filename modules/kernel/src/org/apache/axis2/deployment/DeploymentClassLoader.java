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

package org.apache.axis2.deployment;

import org.apache.axis2.classloader.BeanInfoCache;
import org.apache.axis2.classloader.BeanInfoCachingClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

public class DeploymentClassLoader extends URLClassLoader implements BeanInfoCachingClassLoader {
    private boolean isChildFirstClassLoading;

    private final BeanInfoCache beanInfoCache = new BeanInfoCache();
    
    /**
     * Constructor.
     *
     * @param urls   <code>URL</code>s
     * @param parent parent classloader <code>ClassLoader</code>
     */
    public DeploymentClassLoader(URL[] urls,
                                 ClassLoader parent,
                                 boolean isChildFirstClassLoading) {
        super(urls, parent);
        this.isChildFirstClassLoading = isChildFirstClassLoading;
    }

    public InputStream getResourceAsStream(String name) {
        URL url = findResource(name);
        if(url == null) {
            url = getResource(name);
        }
        if(url!=null){
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = null;
        if (!isChildFirstClassLoading) {
            c = super.loadClass(name, resolve);
        } else {
            c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (Exception e) {
                    c = super.loadClass(name, resolve);
                }
            }
        }
        return c;
    }

    public boolean isChildFirstClassLoading() {
        return isChildFirstClassLoading;
    }

    public void setChildFirstClassLoading(boolean childFirstClassLoading) {
        isChildFirstClassLoading = childFirstClassLoading;
    }

    public final BeanInfoCache getBeanInfoCache() {
        return beanInfoCache;
    }
}
