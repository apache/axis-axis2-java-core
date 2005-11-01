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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.i18n.Messages;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * ArchiveFileData = Hot Deployment File Item , to store infromation of the module or servise
 * item to be deploy
 */
public class ArchiveFileData {

    private ClassLoader classLoader;
    private File file = null;
    private int type;
    private String messageReceiver;
    private String moduleClass;
    private String name;


    //To store services in a serviceGroup , if there are wsdl for those servics ,
    //so wsdl service will be created for each wsdl an those will be temeororaly store
    //in this table
    private HashMap service = new HashMap();

    private ArrayList deployableServices = new ArrayList();

    public ArchiveFileData(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getMessageReceiver() {
        return messageReceiver;
    }

    public void setMessageReceiver(String messageReceiver) {
        this.messageReceiver = messageReceiver;
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

    public void setClassLoader(boolean extarctArichive) throws AxisFault {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (! extarctArichive) {
            // has to be craeted taking that file to the account
            if (file != null) {
                URL[] urlsToLoadFrom;
                try {
                    if (!file.exists()) {
                        throw new AxisFault(Messages.getMessage(DeploymentErrorMsgs.FNF));
                    }
                    urlsToLoadFrom = new URL[]{file.toURL()};
                    classLoader =
                            new DeploymentClassLoader(urlsToLoadFrom, parent);
                    //                classLoader = new URLClassLoader(urlsToLoadFrom, parent);
                } catch (Exception e) {
                    throw new AxisFault(e);
                }
            }
        } else {
            if (file != null) {
                try {
                    ArrayList urls = new ArrayList();
                    urls.add(file.toURL());

                    //if lib is simple
                    File libfiles = new File(file, "lib");
                    if(libfiles.exists()){
                        urls.add(libfiles.toURL());
                        File jarfiles [] = libfiles.listFiles();
                        for (int i = 0; i < jarfiles.length; i++) {
                            File jarfile = jarfiles[i];
                            if(jarfile.getName().endsWith(".jar")){
                                urls.add(jarfile.toURL());
                            }
                        }
                    }

                    //if lib is capital
                    libfiles = new File(file, "Lib");
                    if(libfiles.exists()){
                        urls.add(libfiles.toURL());
                        File jarfiles [] = libfiles.listFiles();
                        for (int i = 0; i < jarfiles.length; i++) {
                            File jarfile = jarfiles[i];
                            if(jarfile.getName().endsWith(".jar")){
                                urls.add(jarfile.toURL());
                            }
                        }
                    }

                    URL urllist [] = new URL[urls.size()];
                    for (int i = 0; i < urls.size(); i++) {
                        urllist[i] = (URL) urls.get(i);
                    }
                    classLoader = new URLClassLoader(urllist,parent);


                } catch (MalformedURLException e) {
                    throw new AxisFault(e);
                }

            }
        }

    }

    /**
     * to check whthere a given file is  a  jar file
     *
     * @param filename
     * @return boolean
     */
    public static  boolean isServiceArchiveFile(String filename) {
        return ((filename.endsWith(".jar")) | (filename.endsWith(".aar")));
    }

    public static  boolean isModuleArchiveFile(String filename) {
        return ((filename.endsWith(".jar")) || (filename.endsWith(".mar")));
    }


    public void addService(AxisService servicedesc){
        service.put(servicedesc.getName().getLocalPart(),servicedesc);
    }

    public AxisService getService(String serviceName){
        return (AxisService)service.get(serviceName);
    }

    public HashMap getService() {
        return service;
    }

    public ArrayList getDeployableServices() {
        return deployableServices;
    }

    public void setDeployableServices(ArrayList deployableServices) {
        this.deployableServices = deployableServices;
    }
}
