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


package org.apache.axis2.deployment;

import org.apache.axis2.classloader.JarStreamHandlerFactory;
import org.apache.axis2.deployment.util.Utils;

import java.net.URL;
import java.net.URLClassLoader;

public class DeploymentClassLoader extends URLClassLoader {
    /**
     * DeploymentClassLoader is extended from URLClassLoader. The constructor
     * does not override the super constructor, but does additional steps like find out
     * jar files inside /lib directory.
     *
     * @param urls   <code>URL</code>s
     * @param parent parent classloader <code>ClassLoader</code>
     */
    public DeploymentClassLoader(URL[] urls, ClassLoader parent, boolean antiJARLocking) {
//        super(Utils.getURLsForAllJars(urls[0], antiJARLocking), parent);
        super(Utils.getURLsForAllJars(urls[0]), parent, new JarStreamHandlerFactory());
    }

    public DeploymentClassLoader(URL[] urls, ClassLoader parent) {
        super(Utils.getURLsForAllJars(urls[0]), parent, new JarStreamHandlerFactory());
    }
}
