package org.apache.axis2.deployment.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.engine.Handler;

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
    public static void addFlowHandlers(Flow flow, ClassLoader clsLoader) throws AxisFault {
        int count = flow.getHandlerCount();

        for (int j = 0; j < count; j++) {
            HandlerDescription handlermd = flow.getHandler(j);
            Class handlerClass;
            Handler handler;

            handlerClass = getHandlerClass(handlermd.getClassName(), clsLoader);

            try {
                handler = (Handler) handlerClass.newInstance();
                handler.init(handlermd);
                handlermd.setHandler(handler);
            } catch (InstantiationException e) {
                throw new AxisFault(e);
            } catch (IllegalAccessException e) {
                throw new AxisFault(e);
            }
        }
    }

    public static void loadHandler(ClassLoader loader1, HandlerDescription desc)
            throws DeploymentException {
        String handlername = desc.getClassName();
        Handler handler;
        Class handlerClass;

        try {
            handlerClass = Class.forName(handlername, true, loader1);
            handler = (Handler) handlerClass.newInstance();
            handler.init(desc);
            desc.setHandler(handler);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public static ClassLoader getClassLoader(ClassLoader parent, File file)
            throws DeploymentException {
        URLClassLoader classLoader;

        if (file != null) {
            try {
                ArrayList urls = new ArrayList();

                urls.add(file.toURL());

                // lower case directory name
                File libfiles = new File(file, "lib");

                if (libfiles.exists()) {
                    urls.add(libfiles.toURL());

                    File jarfiles[] = libfiles.listFiles();

                    for (int i = 0; i < jarfiles.length; i++) {
                        File jarfile = jarfiles[i];

                        if (jarfile.getName().endsWith(".jar")) {
                            urls.add(jarfile.toURL());
                        }
                    }
                }

                // upper case directory name
                libfiles = new File(file, "Lib");

                if (libfiles.exists()) {
                    urls.add(libfiles.toURL());

                    File jarfiles[] = libfiles.listFiles();

                    for (int i = 0; i < jarfiles.length; i++) {
                        File jarfile = jarfiles[i];

                        if (jarfile.getName().endsWith(".jar")) {
                            urls.add(jarfile.toURL());
                        }
                    }
                }

                URL urllist[] = new URL[urls.size()];

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

    private static Class getHandlerClass(String className, ClassLoader loader1) throws AxisFault {
        Class handlerClass;

        try {
            handlerClass = Class.forName(className, true, loader1);
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }

        return handlerClass;
    }
}
