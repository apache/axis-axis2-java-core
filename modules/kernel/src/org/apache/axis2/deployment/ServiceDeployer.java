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

import org.apache.axiom.om.OMElement;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.AARFileBasedURIResolver;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * <p>
 * Standard Axis2 service Deployer which use services.xml file to build
 * services. ServiceDeployer can be used with Axis2 archive (.aar) or exploded
 * directory structure. Some of the example formats given below.
 * </p>
 * <p>Examples : <p>
 *        <ul>
 *          <li>repository/services/SimpleService.aar/meta-inf/services.xml</li>
 *          <li>repository/services/SimpleService/meta-inf/services.xml</li>
 *          <li>WEB-INF/services/SimpleService.aar/meta-inf/services.xml</li>
 *          <li>WEB-INF/services/SimpleService/meta-inf/services.xml</li>
 *       </ul>
 * 
 * <p>Further ServiceDeployer can be used to deploy services from a remote
 * repository or load a repository from a JAR file. In this case service files access through URLs.
 * To deploy services from a remote repository or JAR file it is expected to
 * present service/services.list file which contains names of services.</p>
 * 
 * <p>Example :</p> <p>jar:file:/home/resources/repo.jar!/repo/services/services.list may contains following
 * list of service.</p>
 *          <ul>
 *          <li>StudentService.aar</li>
 *          <li>SimpleService.aar</li>
 *          </ul>
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
public class ServiceDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(ServiceDeployer.class);
    private AxisConfiguration axisConfig;
    private ConfigurationContext configCtx;
    private String directory;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = this.configCtx.getAxisConfiguration();
    }

    //Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        File deploymentFile = deploymentFileData.getFile();
        if(deploymentFile == null){
            //This can be a URL
            deployFromUrl(deploymentFileData);
            return;
        }
        boolean isDirectory = deploymentFile.isDirectory();
        ArchiveReader archiveReader;
        StringWriter errorWriter = new StringWriter();
        archiveReader = new ArchiveReader();
        String serviceStatus = "";
        try {
            deploymentFileData.setClassLoader(isDirectory,
                                              axisConfig.getServiceClassLoader(),
                    (File)axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    axisConfig.isChildFirstClassLoading());
            OMElement serviceMetaData = archiveReader.buildServiceDescription(
                    deploymentFileData.getAbsolutePath(), configCtx, isDirectory);
            deploymentFileData.setServiceMetaData(serviceMetaData);
            Map<String, AxisService> serviceMap = executeServiceBuilderExtensions(
                  deploymentFileData, configCtx);        
            
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());
            ArrayList<AxisService> serviceList = archiveReader.processServiceGroup(
                    serviceMetaData, deploymentFileData,
                    serviceGroup, isDirectory, serviceMap,
                    configCtx);
            URL location = deploymentFileData.getFile().toURI().toURL();

            // Add the hierarchical path to the service group
            if (location != null) {
                String serviceHierarchy = Utils.getServiceHierarchy(location.getPath(),
                        this.directory);
                if (serviceHierarchy != null && !"".equals(serviceHierarchy)) {
                    serviceGroup.setServiceGroupName(serviceHierarchy
                            + serviceGroup.getServiceGroupName());
                    for (AxisService axisService : serviceList) {
                        axisService.setName(serviceHierarchy + axisService.getName());
                    }
                }
            }
            DeploymentEngine.addServiceGroup(serviceGroup,
                                             serviceList,
                                             location,
                                             deploymentFileData,
                                             axisConfig);

            super.deploy(deploymentFileData);
        } catch (DeploymentException de) {
            de.printStackTrace();
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                          deploymentFileData.getName(),
                                          de.getMessage()),
                      de);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            de.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw de;

        } catch (AxisFault axisFault) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                          deploymentFileData.getName(),
                                          axisFault.getMessage()),
                      axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(axisFault);

        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.info(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(),
                        sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(e);

        } catch (Throwable t) {
            if (log.isInfoEnabled()) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                log.info(Messages.getMessage(
                        DeploymentErrorMsgs.INVALID_SERVICE,
                        deploymentFileData.getName(),
                        sw.getBuffer().toString()));
            }
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();

            throw new DeploymentException(new Exception(t));

        } finally {
            if (serviceStatus.startsWith("Error:")) {
                axisConfig.getFaultyServices().put(deploymentFileData.getFile().getAbsolutePath(),
                                                   serviceStatus);
            }
        }
    }
    
    public void deployFromUrl(DeploymentFileData deploymentFileData) throws DeploymentException {
        URL servicesURL = deploymentFileData.getUrl();
        if (servicesURL == null) {
            return;
        }
        AxisServiceGroup serviceGroup = new AxisServiceGroup();
        StringWriter errorWriter = new StringWriter();
        int index = servicesURL.getPath().lastIndexOf(File.separator);
         String serviceFile;
         if(index > 0){
             serviceFile = servicesURL.getPath().substring(index);
         } else {
             serviceFile = servicesURL.getPath();
         }
         ArrayList<AxisService> servicelist =
         populateService(serviceGroup,
         servicesURL,
         serviceFile.substring(0, serviceFile.indexOf(".aar")));
         try {
            DeploymentEngine.addServiceGroup(serviceGroup, servicelist, servicesURL, null,
             axisConfig);
            // let the system have hidden services
            if (!JavaUtils.isTrueExplicitly(serviceGroup.getParameterValue(
                    Constants.HIDDEN_SERVICE_PARAM_NAME))) {
                    log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS,
                    serviceGroup.getServiceGroupName(),
                    servicesURL.toString()));
            }
        } catch (AxisFault axisFault) {
            log.error(
                    Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                            deploymentFileData.getName(), axisFault.getMessage()), axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            throw new DeploymentException(axisFault);
        }
    

    }
    
    /*
     * TODO - This need to be implemented to support
     * DeploymentEngine#loadCustomServices method.
     */
    public void deployFromUrl(Deployer deployer, URL servicesURL) throws DeploymentException {
        throw new DeploymentException(
                "Not support for this operation - deployFromUrl(Deployer deployer, URL servicesURL)");
    }
    

   
    /*
     * TODO - This method is used by deployFromUrl() method and this should be
     * refactored to reduce code complexity.
     */
    protected ArrayList<AxisService> populateService(AxisServiceGroup serviceGroup,
            URL servicesURL, String serviceName) throws DeploymentException {
        try {
            serviceGroup.setServiceGroupName(serviceName);
            ClassLoader serviceClassLoader = Utils
                    .createClassLoader(new URL[] { servicesURL }, axisConfig
                            .getServiceClassLoader(), true, (File) axisConfig
                            .getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                            axisConfig.isChildFirstClassLoading());
            String metainf = "meta-inf";
            serviceGroup.setServiceGroupClassLoader(serviceClassLoader);
            // processing wsdl.list
            InputStream wsdlfilesStream = serviceClassLoader
                    .getResourceAsStream("meta-inf/wsdl.list");
            if (wsdlfilesStream == null) {
                wsdlfilesStream = serviceClassLoader.getResourceAsStream("META-INF/wsdl.list");
                if (wsdlfilesStream != null) {
                    metainf = "META-INF";
                }
            }
            HashMap<String, AxisService> servicesMap = new HashMap<String, AxisService>();
            if (wsdlfilesStream != null) {
                ArchiveReader reader = new ArchiveReader();
                BufferedReader input = new BufferedReader(new InputStreamReader(wsdlfilesStream));
                String line;
                while ((line = input.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0 && line.charAt(0) != '#') {
                        line = metainf + "/" + line;
                        try {
                            List<AxisService> services = reader.getAxisServiceFromWsdl(
                                    serviceClassLoader.getResourceAsStream(line),
                                    serviceClassLoader, line);
                            if (services != null) {
                                for (Object service : services) {
                                    AxisService axisService = (AxisService) service;
                                    servicesMap.put(axisService.getName(), axisService);
                                }
                            }

                        } catch (Exception e) {
                            throw new DeploymentException(e);
                        }
                    }
                }
            }
            InputStream servicexmlStream = serviceClassLoader
                    .getResourceAsStream("META-INF/services.xml");
            if (servicexmlStream == null) {
                servicexmlStream = serviceClassLoader.getResourceAsStream("meta-inf/services.xml");
            } else {
                metainf = "META-INF";
            }
            if (servicexmlStream == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND, servicesURL.toString()));
            }
            DescriptionBuilder builder = new DescriptionBuilder(servicexmlStream, configCtx);
            OMElement rootElement = builder.buildOM();
            String elementName = rootElement.getLocalName();

            if (DeploymentConstants.TAG_SERVICE.equals(elementName)) {
                AxisService axisService = null;
                String wsdlLocation = "META-INF/service.wsdl";
                InputStream wsdlStream = serviceClassLoader.getResourceAsStream(wsdlLocation);
                URL wsdlURL = serviceClassLoader.getResource(metainf + "/service.wsdl");
                if (wsdlStream == null) {
                    wsdlLocation = "META-INF/" + serviceName + ".wsdl";
                    wsdlStream = serviceClassLoader.getResourceAsStream(wsdlLocation);
                    wsdlURL = serviceClassLoader.getResource(wsdlLocation);
                }
                if (wsdlStream != null) {
                    WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder = new WSDL11ToAxisServiceBuilder(
                            wsdlStream, null, null);
                    File file = Utils.toFile(servicesURL);
                    if (file != null && file.exists()) {
                        wsdl2AxisServiceBuilder.setCustomWSDLResolver(new AARBasedWSDLLocator(
                                wsdlLocation, file, wsdlStream));
                        wsdl2AxisServiceBuilder
                                .setCustomResolver(new AARFileBasedURIResolver(file));
                    }
                    if (wsdlURL != null) {
                        wsdl2AxisServiceBuilder.setDocumentBaseUri(wsdlURL.toString());
                    }
                    axisService = wsdl2AxisServiceBuilder.populateService();
                    axisService.setWsdlFound(true);
                    axisService.setCustomWsdl(true);
                    axisService.setName(serviceName);
                }
                if (axisService == null) {
                    axisService = new AxisService(serviceName);
                }

                axisService.setParent(serviceGroup);
                axisService.setClassLoader(serviceClassLoader);

                ServiceBuilder serviceBuilder = new ServiceBuilder(configCtx, axisService);
                AxisService service = serviceBuilder.populateService(rootElement);

                ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
                serviceList.add(service);
                return serviceList;
            } else if (DeploymentConstants.TAG_SERVICE_GROUP.equals(elementName)) {
                ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement,
                        servicesMap, configCtx);
                ArrayList<AxisService> servicList = groupBuilder.populateServiceGroup(serviceGroup);
                Iterator<AxisService> serviceIterator = servicList.iterator();
                while (serviceIterator.hasNext()) {
                    AxisService axisService = (AxisService) serviceIterator.next();
                    String wsdlLocation = "META-INF/service.wsdl";
                    InputStream wsdlStream = serviceClassLoader.getResourceAsStream(wsdlLocation);
                    URL wsdlURL = serviceClassLoader.getResource(wsdlLocation);
                    if (wsdlStream == null) {
                        wsdlLocation = "META-INF/" + serviceName + ".wsdl";
                        wsdlStream = serviceClassLoader.getResourceAsStream(wsdlLocation);
                        wsdlURL = serviceClassLoader.getResource(wsdlLocation);
                    }
                    if (wsdlStream != null) {
                        WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder = new WSDL11ToAxisServiceBuilder(
                                wsdlStream, axisService);
                        File file = Utils.toFile(servicesURL);
                        if (file != null && file.exists()) {
                            wsdl2AxisServiceBuilder.setCustomWSDLResolver(new AARBasedWSDLLocator(
                                    wsdlLocation, file, wsdlStream));
                            wsdl2AxisServiceBuilder.setCustomResolver(new AARFileBasedURIResolver(
                                    file));
                        }
                        if (wsdlURL != null) {
                            wsdl2AxisServiceBuilder.setDocumentBaseUri(wsdlURL.toString());
                        }
                        axisService = wsdl2AxisServiceBuilder.populateService();
                        axisService.setWsdlFound(true);
                        axisService.setCustomWsdl(true);
                        // Set the default message receiver for the operations
                        // that were
                        // not listed in the services.xml
                        Iterator<AxisOperation> operations = axisService.getOperations();
                        while (operations.hasNext()) {
                            AxisOperation operation = (AxisOperation) operations.next();
                            if (operation.getMessageReceiver() == null) {
                                operation.setMessageReceiver(loadDefaultMessageReceiver(
                                        operation.getMessageExchangePattern(), axisService));
                            }
                        }
                    }
                }
                return servicList;
            }
        } catch (IOException e) {
            throw new DeploymentException(e);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
        return null;
    }
    
    private MessageReceiver loadDefaultMessageReceiver(String mepURL, AxisService service) {
        MessageReceiver messageReceiver;
        if (mepURL == null) {
            mepURL = WSDL2Constants.MEP_URI_IN_OUT;
        }
        if (service != null) {
            messageReceiver = service.getMessageReceiver(mepURL);
            if (messageReceiver != null) {
                return messageReceiver;
            }
        }
        return axisConfig.getMessageReceiver(mepURL);
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            //find the hierarchical part of the service group name
            String serviceHierarchy = Utils.getServiceHierarchy(fileName, this.directory);
            fileName = Utils.getShortFileName(fileName);
            fileName = DeploymentEngine.getAxisServiceName(fileName);

            //attach the hierarchical part if it is not null
            if (serviceHierarchy != null) {
                fileName = serviceHierarchy + fileName;
            }
            AxisServiceGroup serviceGroup = axisConfig.removeServiceGroup(fileName);
            //Fixed - https://issues.apache.org/jira/browse/AXIS2-4610
            if (serviceGroup != null) {
                for (Iterator<AxisService> services = serviceGroup.getServices(); services.hasNext();) {
                AxisService axisService = (AxisService) services.next();
                ServiceLifeCycle serviceLifeCycle = axisService.getServiceLifeCycle();
                if (serviceLifeCycle != null) {
                    serviceLifeCycle.shutDown(configCtx, axisService);
                }
            }
                configCtx.removeServiceGroupContext(serviceGroup);
                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                        fileName));
            } else {
                axisConfig.removeFaultyService(fileName);
            }
            super.undeploy(fileName);
        } catch (AxisFault axisFault) {
            //May be a faulty service
            axisConfig.removeFaultyService(fileName);

            throw new DeploymentException(axisFault);
        }
    }
}
