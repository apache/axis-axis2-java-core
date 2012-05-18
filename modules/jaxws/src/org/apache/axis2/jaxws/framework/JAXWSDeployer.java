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
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarInputStream;

/*
 * JAXWSDeployer is a custom deployer modeled after the POJODeployer. Its purpose
 * is to deploy .wars and expanded .war directories
 */
public class JAXWSDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(JAXWSDeployer.class);

    protected ConfigurationContext configCtx;
    protected AxisConfiguration axisConfig;
    private String directory;

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
            URL repository = axisConfig.getRepository();
            if (!file.isDirectory() || repository == null)
                return;
            ArrayList<String> classList = getClassesInWebInfDirectory(file);
            ClassLoader threadClassLoader = null;
            try {
                threadClassLoader = Thread.currentThread().getContextClassLoader();
                ArrayList<URL> urls = new ArrayList<URL>();
                urls.add(repository);
                String webLocation = DeploymentEngine.getWebLocationString();
                if (webLocation != null) {
                    urls.add(new File(webLocation).toURL());
                }
                ClassLoader classLoader = Utils.createClassLoader(
                        urls,
                        axisConfig.getSystemClassLoader(),
                        true,
                        (File) axisConfig.
                                getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                        axisConfig.isChildFirstClassLoading());
                Thread.currentThread().setContextClassLoader(classLoader);
                JAXWSDeployerSupport deployerSupport = new JAXWSDeployerSupport(configCtx, directory);
                deployerSupport.deployClasses("JAXWS-Builtin", file.toURL(), Thread.currentThread().getContextClassLoader(), classList);
            } catch (NoClassDefFoundError e) {
                if (log.isDebugEnabled()) {
                    log.debug(Messages.getMessage("deployingexception", e.getMessage()), e);
                }
            } catch (Exception e) {
                log.info(Messages.getMessage("deployingexception", e.getMessage()), e);
            } finally {
                if (threadClassLoader != null) {
                    Thread.currentThread().setContextClassLoader(threadClassLoader);
                }
            }
        }
    }

    protected ArrayList<String> getClassesInWebInfDirectory(File file) {
        String filePath = file.getAbsolutePath();
        Collection<File> files = FileUtils.listFiles(file, new String[]{"class"}, true);
        ArrayList<String> classList = new ArrayList<String>();
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File f = iterator.next();
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
                log.info("Deploying artifact : " + deploymentFileData.getAbsolutePath());
                ArrayList<URL> urls = new ArrayList<URL>();
                urls.add(deploymentFileData.getFile().toURL());
                urls.add(axisConfig.getRepository());

                // adding libs under jaxws deployment dir
                addJaxwsLibs(urls, axisConfig.getRepository().getPath() + directory);

                String webLocation = DeploymentEngine.getWebLocationString();
                if (webLocation != null) {
                    urls.add(new File(webLocation).toURL());
                }
                ClassLoader classLoader = Utils.createClassLoader(
                        urls,
                        axisConfig.getSystemClassLoader(),
                        true,
                        (File) axisConfig.
                                getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                        axisConfig.isChildFirstClassLoading());
                Thread.currentThread().setContextClassLoader(classLoader);

                List<String> classList = Utils.getListOfClasses(deploymentFileData);
                JAXWSDeployerSupport deployerSupport = new JAXWSDeployerSupport(configCtx, directory);
                AxisServiceGroup serviceGroup = deployerSupport.deployClasses(groupName, location, classLoader, classList);
                
                if(serviceGroup == null) {
                    String msg = "Error while deploying JAX-WS jar: " +
                            location.toString() +
                            ". JAX-WS Service deployment failed.";
                    log.error(msg);
                    axisConfig.getFaultyServices().
                            put(deploymentFileData.getFile().getAbsolutePath(), msg);
                }
            }
            super.deploy(deploymentFileData);
        } catch (Throwable t) {
            log.debug(Messages.getMessage("stroringfaultyservice", t.getMessage()), t);
            storeFaultyService(deploymentFileData, t);
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }
    }

    protected void storeFaultyService(DeploymentFileData deploymentFileData, Throwable t) {
        StringWriter errorWriter = new StringWriter();
        PrintWriter ptintWriter = new PrintWriter(errorWriter);
        t.printStackTrace(ptintWriter);
        String error = "Error:\n" + errorWriter.toString();
        axisConfig.getFaultyServices().
                put(deploymentFileData.getFile().getAbsolutePath(), error);
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) {
        //find the hierarchical part of the service group name
        String serviceHierarchy = Utils.getServiceHierarchy(fileName, this.directory);
        fileName = serviceHierarchy + Utils.getShortFileName(fileName);
        try {
            AxisServiceGroup serviceGroup =
                    axisConfig.removeServiceGroup(fileName);
            if(configCtx != null) {
                configCtx.removeServiceGroupContext(serviceGroup);
            }
            super.undeploy(fileName);
            log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                    fileName));
        } catch (AxisFault axisFault) {
            //May be a faulty service
            log.debug(Messages.getMessage(DeploymentErrorMsgs.FAULTY_SERVICE_REMOVAL,
                    axisFault.getMessage()), axisFault);
            axisConfig.removeFaultyService(fileName);
        }
    }

    /**
     * Check if this inputstream is a jar/zip
     *
     * @param f - file
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

    /**
     * Checks whether there's a 'lib' folder inside the provided folder and adds all the lib URLs
     * into the provided URL list.
     *
     * @param urls - list of URLs
     * @param jaxwsDepDirPath - jaxws deployment folder path
     * @throws Exception - on error while geting URLs of libs
     */
    private void addJaxwsLibs(ArrayList<URL> urls, String jaxwsDepDirPath)
            throws Exception {
        File jaxwsDepDirLib = new File(jaxwsDepDirPath + File.separator + "lib");
        if (jaxwsDepDirLib.exists() && jaxwsDepDirLib.isDirectory()) {
            for (File file : jaxwsDepDirLib.listFiles()) {
                if (file.isFile()) {
                    try {
                        urls.add(file.toURI().toURL());
                    } catch (MalformedURLException e) {
                        throw new Exception("Error while loading libraries from the " +
                                "'lib' directory under jaxws deployment direcotry.", e);
                    }
                }
            }
        }
    }

}

