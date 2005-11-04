package org.apache.axis2.deployment.util;

import org.apache.axis2.deployment.DeploymentException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class Utils {

    public static ClassLoader getClassLoader(ClassLoader parent , File file) throws DeploymentException {
        URLClassLoader classLoader;
        if (file != null) {
            try {
                ArrayList urls = new ArrayList();
                urls.add(file.toURL());
                //if lib is simple
                File libfiles = new File(file, "lib");
                if (libfiles.exists()) {
                    urls.add(libfiles.toURL());
                    File jarfiles [] = libfiles.listFiles();
                    for (int i = 0; i < jarfiles.length; i++) {
                        File jarfile = jarfiles[i];
                        if (jarfile.getName().endsWith(".jar")) {
                            urls.add(jarfile.toURL());
                        }
                    }
                }
                //if lib is capital
                libfiles = new File(file, "Lib");
                if (libfiles.exists()) {
                    urls.add(libfiles.toURL());
                    File jarfiles [] = libfiles.listFiles();
                    for (int i = 0; i < jarfiles.length; i++) {
                        File jarfile = jarfiles[i];
                        if (jarfile.getName().endsWith(".jar")) {
                            urls.add(jarfile.toURL());
                        }
                    }
                }
                URL urllist [] = new URL[urls.size()];
                for (int i = 0; i < urls.size(); i++) {
                    urllist[i] = (URL) urls.get(i);
                }
                classLoader = new URLClassLoader(urllist, parent);
                return classLoader;
            } catch (MalformedURLException e) {
                throw new DeploymentException(e);
            }
        }
        return null;
    }
}
