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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * HDFileItem = Hot Deployment File Item , to store infromation of the module or servise
 * item to be deploy
 */
public class HDFileItem {

    private ClassLoader classLoader;
    private File file = null;
    private int type;
    private String className;
    private String provideName;
    private String name;

    public HDFileItem(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getProvideName() {
        return provideName;
    }

    public void setProvideName(String provideName) {
        this.provideName = provideName;
    }

    public HDFileItem(File file, int type) {
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
}
