package org.apache.axis2.tool.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;

import sun.misc.URLClassPath;
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
public class ClassFileHandler {


    /**
     * @deprecated
     * This needs to be written in a functional manner.
     * @param location
     * @return Returns ArrayList.
     * @throws IOException
     */
    //todo see whether this is possible
    public ArrayList getClassesAtLocation(String location) throws IOException {
        File fileEndpoint = new File(location);
        if (!fileEndpoint.exists())
            throw new IOException("the location is invalid");
        URL[] urlList = {fileEndpoint.toURL()};
        URLClassPath classLoader = new URLClassPath(urlList);
        Enumeration enumerator = classLoader.getResources("");

        while (enumerator.hasMoreElements()) {
            Object o =  enumerator.nextElement();
        }
        return null;

    }

    public ArrayList getMethodNamesFromClass(String classFileName,String location) throws IOException, ClassNotFoundException{
        ArrayList returnList = new ArrayList();
        File fileEndpoint = new File(location);
        if (!fileEndpoint.exists())
            throw new IOException("the location is invalid");
        URL[] urlList = {fileEndpoint.toURL()};
        URLClassLoader clazzLoader = new URLClassLoader(urlList);
        Class clazz = clazzLoader.loadClass(classFileName);
        Method[] methods = clazz.getDeclaredMethods();

        for (int i = 0; i < methods.length; i++) {
            returnList.add(methods[i].getName());

        }
        return returnList;
    }

}
