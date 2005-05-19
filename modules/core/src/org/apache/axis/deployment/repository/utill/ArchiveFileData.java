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

package org.apache.axis.deployment.repository.utill;

import org.apache.axis.engine.AxisFault;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

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

    private ArrayList modules = new ArrayList() ;


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
        if (file != null) {
            URL[] urlsToLoadFrom = new URL[0];
            try {
                if (!file.exists()) {
                    throw new RuntimeException("file not found !!!!!!!!!!!!!!!");
                }
                urlsToLoadFrom = new URL[]{file.toURL()};
                classLoader = new URLClassLoader(urlsToLoadFrom, parent);

            } catch (MalformedURLException e) {
                throw new AxisFault(e.getMessage(), e);
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }
    
    public void addModule(QName moduleName){
        modules.add(moduleName);
    }

    public ArrayList getModules(){
        return modules;
    }
}
