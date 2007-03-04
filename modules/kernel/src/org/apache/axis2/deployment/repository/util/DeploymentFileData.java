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

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentClassLoader;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.i18n.Messages;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/**
 * ArchiveFileData stores information about the module or service item to be deployed.
 */
public class DeploymentFileData {
    private File file = null;
    private ArrayList deployableServices = new ArrayList();
    private ClassLoader classLoader;
    private String messageReceiver;
    private boolean lock;

    private String name;
    private String type;

    public DeploymentFileData(File file, String type, boolean lock) {
        this.file = file;
        this.type = type;
        this.lock = lock;
    }

    public DeploymentFileData(String type, String name, boolean lock) {
        this.type = type;
        this.name = name;
        this.lock = lock;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ArrayList getDeployableServices() {
        return deployableServices;
    }

    public File getFile() {
        return file;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }

    public String getName() {
        return file.getName();
    }

    public String getServiceName() {
        if (file != null) {
            return file.getName();
        } else {
            return name;
        }
    }

    public String getType() {
        return type;
    }

    public static boolean isModuleArchiveFile(String filename) {
        return (filename.endsWith(".mar"));
    }

    /**
     * Checks whether a given file is a jar or an aar file.
     *
     * @param filename
     * @return Returns boolean.
     */
    public static boolean isServiceArchiveFile(String filename) {
        return ((filename.endsWith(".jar")) | (filename.endsWith(".aar")));
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(boolean extractArchive, ClassLoader parent) throws AxisFault {
        if (!extractArchive) {

            if (file != null) {
                URL[] urlsToLoadFrom;
                try {
                    if (!file.exists()) {
                        throw new AxisFault(Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                                file.getAbsolutePath()));
                    }
                    urlsToLoadFrom = new URL[]{file.toURL()};
                    classLoader = new DeploymentClassLoader(urlsToLoadFrom, parent, lock);
                } catch (Exception e) {
                    throw new AxisFault(e);
                }
            }
        } else {
            if (file != null) {
                classLoader = Utils.getClassLoader(parent, file);
            }
        }
    }

    public void setDeployableServices(ArrayList deployableServices) {
        this.deployableServices = deployableServices;
    }

    public void setMessageReceiver(String messageReceiver) {
        this.messageReceiver = messageReceiver;
    }
}
