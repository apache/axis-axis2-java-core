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
package org.apache.axis2.jaxws.framework;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
import javax.xml.ws.WebServiceProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.lang.annotation.Annotation;

/*
 * JAXWSDeployer is a custom deployer modeled after the POJODeployer. Its purpose
 * is to deploy .wars and expanded .war directories
 */
public class JAXWSDeployer implements Deployer {

    private static Log log = LogFactory.getLog(JAXWSDeployer.class);

    protected ConfigurationContext configCtx = null;
    protected AxisConfiguration axisConfig = null;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = configCtx.getAxisConfiguration();
        deployServicesInWARClassPath();
    }//Will process the file and add that to axisConfig

    protected void deployServicesInWARClassPath() {
        String dir = DeploymentEngine.getWebLocationString();
        if (dir != null) {
            File file = new File(dir + "/WEB-INF/classes/");
            if (!file.isDirectory())
                return;
            ArrayList classList = getClassesInWebInfDirectory(file);
            ClassLoader threadClassLoader = null;
            try {
                threadClassLoader = Thread.currentThread().getContextClassLoader();
                ArrayList urls = new ArrayList();
                urls.add(axisConfig.getRepository());
                String webLocation = DeploymentEngine.getWebLocationString();
                if (webLocation != null) {
                    urls.add(new File(webLocation).toURL());
                }
                ClassLoader classLoader = Utils.createClassLoader(
                        urls,
                        axisConfig.getSystemClassLoader(),
                        true,
                        (File) axisConfig.
                                getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
                Thread.currentThread().setContextClassLoader(classLoader);
                deployClasses("JAXWS-Builtin", file.toURL(), Thread.currentThread().getContextClassLoader(), classList);
            } catch (Exception e) {
                log.info(Messages.getMessage("deployingexception", e.getMessage()), e);
            } finally {
                if (threadClassLoader != null) {
                    Thread.currentThread().setContextClassLoader(threadClassLoader);
                }
            }
        }
    }

    protected ArrayList getClassesInWebInfDirectory(File file) {
        String filePath = file.getAbsolutePath();
        Collection files = FileUtils.listFiles(file, new String[]{"class"}, true);
        ArrayList classList = new ArrayList();
        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File f = (File) iterator.next();
            String fPath = f.getAbsolutePath();
            String fqcn = fPath.substring(filePath.length() + 1);
            fqcn = fqcn.substring(0, fqcn.length() - ".class".length());
            fqcn = fqcn.replace('/', '.');
            fqcn = fqcn.replace('\\', '.');
            classList.add(fqcn);
        }
        return classList;
    }

    public void deploy(DeploymentFileData deploymentFileData) {
        ClassLoader threadClassLoader = null;
        try {
            threadClassLoader = Thread.currentThread().getContextClassLoader();
            String groupName = deploymentFileData.getName();
            URL location = deploymentFileData.getFile().toURL();
            if (isJar(deploymentFileData.getFile())) {
                log.info("Deploying artifact : " + deploymentFileData.getName());
                ArrayList urls = new ArrayList();
                urls.add(deploymentFileData.getFile().toURL());
                urls.add(axisConfig.getRepository());
                String webLocation = DeploymentEngine.getWebLocationString();
                if (webLocation != null) {
                    urls.add(new File(webLocation).toURL());
                }
                ClassLoader classLoader = Utils.createClassLoader(
                        urls,
                        axisConfig.getSystemClassLoader(),
                        true,
                        (File) axisConfig.
                                getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
                Thread.currentThread().setContextClassLoader(classLoader);

                ArrayList classList = getListOfClasses(deploymentFileData);
                int count = deployClasses(groupName, location, classLoader, classList); 
                
                if(count == 0) {
                    String msg = "Error:\n No annotated classes found in the jar: " +
                            location.toString() +
                            ". Service deployment failed.";
                    log.error(msg);
                    axisConfig.getFaultyServices().
                            put(deploymentFileData.getFile().getAbsolutePath(), msg);
                }
            }
        } catch (Throwable t) {
            log.debug(Messages.getMessage("stroringfaultyservice", t.getMessage()), t);
            storeFaultyService(deploymentFileData, t);
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }
    }

    protected int deployClasses(String groupName, URL location, ClassLoader classLoader, List classList)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, AxisFault {
        ArrayList axisServiceList = new ArrayList();
        for (int i = 0; i < classList.size(); i++) {
            String className = (String) classList.get(i);
            Class pojoClass = Loader.loadClass(classLoader, className);
            WebService wsAnnotation = (WebService) pojoClass.getAnnotation(WebService.class);
            WebServiceProvider wspAnnotation = null;
            if (wsAnnotation == null) {
                wspAnnotation = (WebServiceProvider) pojoClass.getAnnotation(WebServiceProvider.class);
            }
            if ((wsAnnotation != null || wspAnnotation != null) && !pojoClass.isInterface()) {
                log.info("Deploying JAXWS class : " + className);
                AxisService axisService;
                axisService =
                        createAxisService(classLoader,
                                className,
                                location);
                if(axisService != null) {
                    axisServiceList.add(axisService);
                }
            }
        }
        int count = axisServiceList.size();
        if (count > 0) {
            AxisServiceGroup serviceGroup = new AxisServiceGroup();
            serviceGroup.setServiceGroupName(groupName);
            for (int i = 0; i < axisServiceList.size(); i++) {
                AxisService axisService = (AxisService) axisServiceList.get(i);
                serviceGroup.addService(axisService);
            }
            axisConfig.addServiceGroup(serviceGroup);
        }
        return count;
    }

    protected ArrayList getListOfClasses(DeploymentFileData deploymentFileData) throws IOException {
        ArrayList classList;
        FileInputStream fin = null;
        ZipInputStream zin = null;
        try {
            fin = new FileInputStream(deploymentFileData.getAbsolutePath());
            zin = new ZipInputStream(fin);
            ZipEntry entry;
            classList = new ArrayList();
            while ((entry = zin.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    name = name.replaceAll(".class", "");
                    name = name.replaceAll("/", ".");
                    classList.add(name);
                }
            }
            zin.close();
            fin.close();
        } catch (Exception e) {
            log.debug(Messages.getMessage("deployingexception", e.getMessage()), e);
            throw new DeploymentException(e);
        } finally {
            if (zin != null) {
                zin.close();
            }
            if (fin != null) {
                fin.close();
            }
        }
        return classList;
    }

    protected void storeFaultyService(DeploymentFileData deploymentFileData, Throwable t) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter ptintWriter = new PrintWriter(errorWriter);
        t.printStackTrace(ptintWriter);
        String error = "Error:\n" + errorWriter.toString();
        axisConfig.getFaultyServices().
                put(deploymentFileData.getFile().getAbsolutePath(), error);
    }

    protected AxisService createAxisService(ClassLoader classLoader,
                                          String className,
                                          URL serviceLocation) throws ClassNotFoundException,
            InstantiationException,
            IllegalAccessException,
            AxisFault {
        Class pojoClass = Loader.loadClass(classLoader, className);
        AxisService axisService;
        try {
            axisService = DescriptionFactory.createAxisService(pojoClass);
        } catch (Throwable t) {
            log.info("Exception creating Axis Service : " + t.getCause(), t);
            return null;
        }
        if (axisService != null) {
            Iterator operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation axisOperation = (AxisOperation) operations.next();
                if (axisOperation.getMessageReceiver() == null) {
                    axisOperation.setMessageReceiver(new JAXWSMessageReceiver());
                }
            }
        }
        axisService.setElementFormDefault(false);
        axisService.setFileName(serviceLocation);
        return axisService;
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void unDeploy(String fileName) {
        fileName = Utils.getShortFileName(fileName);
        if (isJar(new File(fileName))) {
            try {
                AxisServiceGroup serviceGroup =
                        axisConfig.removeServiceGroup(fileName);
                if(configCtx != null) {
                    configCtx.removeServiceGroupContext(serviceGroup);
                }
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                        fileName));
            } catch (AxisFault axisFault) {
                //May be a faulty service
                log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL, 
                        axisFault.getMessage()), axisFault);
                axisConfig.removeFaultyService(fileName);
            }
        }
    }

    /**
     * Check if this inputstream is a jar/zip
     *
     * @param is
     * @return true if inputstream is a jar
     */
    public static boolean isJar(File f) {
        try {
            JarInputStream jis = new JarInputStream(new FileInputStream(f));
            if (jis.getNextEntry() != null) {
                return true;
            }
        } catch (IOException ioe) {
        }
        return false;
    }
}

