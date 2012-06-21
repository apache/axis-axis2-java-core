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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>
 * Standard Axis2 Module Deployer which use module.xml file to deploy
 * Modules. ModuleDeployer can be used with Axis2 Module archive (.mar) or exploded
 * directory structure. Some of the example formats given below.
 * </p>
 * <p>Examples :</p>
 *  <ul>
 *          <li> repository/modules/LogModule.mar/meta-inf/module.xml  </li>
 *          <li> repository/modules/LogModule/meta-inf/module.xml </li>
 *          <li> WEB-INF/modules/LogModule.mar/meta-inf/module.xml </li>
 *          <li> WEB-INF/modules/LogModule/meta-inf/module.xml </li>
 *  </ul>
 * <p>Further ModuleDeployer can be used to deploy Modules from a remote
 * repository or load a repository from a JAR file. In this case module files access through URLs.
 * To deploy Modules from a remote repository or JAR file it is expected to
 * present modules/modules.list file which contains names of Modules. </p>
 * 
 * <p>Example : </p><p>jar:file:/home/resources/repo.jar!/repo/modules/modules.list may contains following
 * list of service.</p>
 *         <ul>
 *            <li>LogModule.mar </li>
 *            <li>Addressing.mar</li>
 *          </ul>
 * 
  * <p>There are several approaches available to set repository URL to Axis2 run
 * time. As an example one can provide repository URL as a init-param of
 * AxisServlet. Some of the examples given below.</p>
 * <ul>
 * <li>
 * <p>Example -1 : </p>
 *      <pre>
 *                  {@code        <init-param> 
 *                      <param-name>axis2.repository.url</param-name>
 *                      <param-value>http://localhost/repo/</param-value> 
 *                 </init-param> }
 *      </pre>
 * </li>
 * <li>
 * <p>Example -2 : </p>
 *     <pre> 
 *                  {@code         <init-param> 
 *                      <param-name>axis2.repository.url</param-name>
 *                      <param-value>jar:file:/home/resources/repo.jar!/repo/</param-value> 
 *                  </init-param>  } 
 *    </pre>
 * </li>
 * </ul>
 * 
 * <p><b>NOTE</b> - It is discouraged to use above services.list based deployment approach
 * because it does not support hot-deployment, hot-update and some of other
 * important deployment features as well. </p>
 * 
 */

public class ModuleDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(ModuleDeployer.class);
    private AxisConfiguration axisConfig;


    public ModuleDeployer() {
    }

    public ModuleDeployer(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.axisConfig = configCtx.getAxisConfiguration();
    }
    //Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) {
        File deploymentFile = deploymentFileData.getFile();
        // deploymentFile == null indicate this can be a URL, try to deploy using a URL.
        if(deploymentFile == null){
            deoloyFromUrl(deploymentFileData);
            return;
        }
        boolean isDirectory = deploymentFile.isDirectory();
        if (isDirectory && deploymentFileData.getName().startsWith(".")) {  // Ignore special meta directories starting with .
            return;
        }

        ArchiveReader archiveReader = new ArchiveReader();
        String moduleStatus = "";
        StringWriter errorWriter = new StringWriter();
        try {

            deploymentFileData.setClassLoader(isDirectory,
                                              axisConfig.getModuleClassLoader(),
                    (File)axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    this.axisConfig.isChildFirstClassLoading());
            AxisModule metaData = new AxisModule();
            metaData.setModuleClassLoader(deploymentFileData.getClassLoader());
            metaData.setParent(axisConfig);
            archiveReader.readModuleArchive(deploymentFileData, metaData, isDirectory, axisConfig);
            URL url = deploymentFile.toURI().toURL();
            metaData.setFileName(url);
            DeploymentEngine.addNewModule(metaData, axisConfig);
            super.deploy(deploymentFileData);
            log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                         metaData.getArchiveName(),
                                         url.toString()));
        } catch (DeploymentException e) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          e.getMessage()),
                      e);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (AxisFault axisFault) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          axisFault.getMessage()),
                      axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (MalformedURLException e) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          e.getMessage()),
                      e);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                    deploymentFileData.getName(),
                    t.getMessage()),
                    t);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } finally {
            if (moduleStatus.startsWith("Error:")) {
                axisConfig.getFaultyModules().put(DeploymentEngine.getAxisServiceName(
                        deploymentFileData.getName()), moduleStatus);
            }
        }
    }
    
    public void deoloyFromUrl(DeploymentFileData deploymentFileData) {
        URL fileUrl = deploymentFileData.getUrl();
        StringWriter errorWriter = new StringWriter();
        String moduleStatus = "";
        if (fileUrl == null) {
            return;
        }

        try {
            ClassLoader deploymentClassLoader = Utils.createClassLoader(new URL[] { fileUrl },
                    axisConfig.getModuleClassLoader(), true,
                    (File) axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    axisConfig.isChildFirstClassLoading());
            AxisModule module = new AxisModule();
            module.setModuleClassLoader(deploymentClassLoader);
            module.setParent(axisConfig);
            int index = fileUrl.getPath().lastIndexOf(File.separator);
            String moduleFile;
            if(index > 0){
                moduleFile = fileUrl.getPath().substring(index);                
            } else {
                moduleFile = fileUrl.getPath();                
            }            
            module.setArchiveName(moduleFile);
            populateModule(module, fileUrl);
            module.setFileName(fileUrl);
            DeploymentEngine.addNewModule(module, axisConfig);
            log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE, module.getArchiveName(),
                    fileUrl.toString()));

        } catch (DeploymentException e) {
            log.error(
                    Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                            deploymentFileData.getName(), e.getMessage()), e);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (AxisFault axisFault) {
            log.error(
                    Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                            deploymentFileData.getName(), axisFault.getMessage()), axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            log.error(
                    Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                            deploymentFileData.getName(), t.getMessage()), t);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } finally {
            if (moduleStatus.startsWith("Error:")) {
                axisConfig.getFaultyModules().put(
                        DeploymentEngine.getAxisServiceName(deploymentFileData.getName()),
                        moduleStatus);
            }
        }
       

    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        super.undeploy(fileName);
    }
    
    private void populateModule(AxisModule module, URL moduleUrl) throws DeploymentException {
        try {
            ClassLoader classLoader = module.getModuleClassLoader();
            InputStream moduleStream = classLoader.getResourceAsStream("META-INF/module.xml");
            if (moduleStream == null) {
                moduleStream = classLoader.getResourceAsStream("meta-inf/module.xml");
            }
            if (moduleStream == null) {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_XML_MISSING, moduleUrl.toString()));
            }
            ModuleBuilder moduleBuilder = new ModuleBuilder(moduleStream, module, axisConfig);
            moduleBuilder.populateModule();
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }
}
