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

package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisFault;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * ArchiveFileData = Hot Deployment File Item , to store infromation of the module or servise
 * item to be deploy
 */
public class ArchiveFileData {

    private ClassLoader classLoader;
    private File file = null;
    private int type;
    private String messgeReceiver;
    private String moduleClass;
    private String name;

    private ArrayList modules = new ArrayList();


    public ArchiveFileData(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getMessgeReceiver() {
        return messgeReceiver;
    }

    public void setMessgeReceiver(String messgeReceiver) {
        this.messgeReceiver = messgeReceiver;
    }

    public ArchiveFileData(File file, int type) {
        this.file = file;
        this.type = type;
    }

    public String getName() {
        return file.getName();
    }

    public String getServiceName() {
        if (file != null) {
            return file.getName();
        } else
            return name;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public int getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getModuleClass() {
        return moduleClass;
    }

    public void setModuleClass(String moduleClass) {
        this.moduleClass = moduleClass;
    }

    public void setClassLoader() throws AxisFault {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        ArrayList URLs = new ArrayList();
        boolean tobeRecreated = false;// if there is a jar file inside a jar file then the URLClassLoader
        // has to be craeted taking that file to the account
        if (file != null) {
            URL[] urlsToLoadFrom = new URL[0];
            try {
                if (!file.exists()) {
                    throw new RuntimeException("file not found !!!!!!!!!!!!!!!");
                }
                URLs.add(file.toURL());
                urlsToLoadFrom = new URL[]{file.toURL()};
                classLoader = new URLClassLoader(urlsToLoadFrom, parent);
            } catch (Exception e) {
                throw new AxisFault(e);
            }
            try {
                ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
                ZipEntry entry;
                String entryName = "";
                int BUFFER = 2048;
                while ((entry = zin.getNextEntry()) != null) {
                    entryName = entry.getName();
                    if (entryName != null && entryName.startsWith("lib/") && entryName.endsWith(".jar")) {
                        //extarcting jar file form the orignial jar file and copy it to the axis2 lib
                        File libFile = new File(DeploymentEngine.axis2repository, entryName);
                        FileOutputStream dest = new FileOutputStream(libFile);
                        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                        byte data[] = new byte[BUFFER];
                        InputStream in = classLoader.getResourceAsStream(entryName);
                        ZipInputStream jar_zin = null;
                        jar_zin = new ZipInputStream(in);
                        ZipEntry jarentry;
                        while ((jarentry = jar_zin.getNextEntry()) != null) {
                            ZipEntry zip = new ZipEntry(jarentry);
                            out.putNextEntry(zip);
                            int count;
                            while ((count = jar_zin.read(data, 0, BUFFER)) != -1) {
                                out.write(data, 0, count);
                            }
                        }
                        out.close();
                        jar_zin.close();
                        URLs.add(libFile.toURL());
                        tobeRecreated = true;
                    }
                }
                zin.close();
                if (tobeRecreated) {
                    URL[] urlstobeload = new URL[URLs.size()];
                    for (int i = 0; i < URLs.size(); i++) {
                        URL url = (URL) URLs.get(i);
                        urlstobeload[i] = url;
                    }
                    //recreating the classLoader
                    classLoader = new URLClassLoader(urlstobeload, parent);
                }
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }
    }

    public void addModule(QName moduleName) {
        modules.add(moduleName);
    }

    public ArrayList getModules() {
        return modules;
    }
}
