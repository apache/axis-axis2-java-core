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
import org.apache.axis2.classloader.JarFileClassLoader;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.AARFileBasedURIResolver;
import org.apache.axis2.deployment.scheduler.DeploymentIterator;
import org.apache.axis2.deployment.scheduler.Scheduler;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public abstract class DeploymentEngine implements DeploymentConstants {
    private static final Log log = LogFactory.getLog(DeploymentEngine.class);

    //to keep the web resource location if any
    protected static String webLocationString = null;
    protected Scheduler scheduler;

    public static void setWebLocationString(String webLocationString) {
        DeploymentEngine.webLocationString = webLocationString;
    }


    /**
     * Support for hot update is controlled by this flag
     */
    protected boolean hotUpdate = true;

    /**
     * Support for hot deployment is controlled by this flag
     */
    protected boolean hotDeployment = true;

    /**
     * Stores all the web Services to deploy.
     */
    protected List wsToDeploy = new ArrayList();

    /**
     * Stores all the web Services to undeploy.
     */
    protected List wsToUnDeploy = new ArrayList();

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    protected AxisConfiguration axisConfig;

    protected ConfigurationContext configContext;

    protected RepositoryListener repoListener;

    protected String servicesPath = null;
    protected File servicesDir = null;
    protected String modulesPath = null;
    protected File modulesDir = null;
    private File repositoryDir = null;

    //to deploy service (both aar and expanded)
    protected ServiceDeployer serviceDeployer;
    //To deploy modules (both mar and expanded)
    protected ModuleDeployer moduleDeployer;

    //to keep map of which deployer can process which file extension ,
    // for example ServiceDeployer will process .aar file
    private HashMap extensionToDeployerMappingMap = new HashMap();
    
    private Map<String, Map<String, Deployer>> deployerMap = new HashMap<String, Map<String, Deployer>>();

    public void loadServices() {
        repoListener.checkServices();
        if (hotDeployment) {
            startSearch(repoListener);
        }
    }

    public void loadRepository(String repoDir) throws DeploymentException {
        File axisRepo = new File(repoDir);
        if (!axisRepo.exists()) {
            throw new DeploymentException(
                    Messages.getMessage("cannotfindrepo", repoDir));
        }
        setDeploymentFeatures();
        prepareRepository(repoDir);
        // setting the CLs
        setClassLoaders(repoDir);
        repoListener = new RepositoryListener(this, false);
        org.apache.axis2.util.Utils
                .calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
        try {
            try {
                axisConfig.setRepository(axisRepo.toURL());
            } catch (MalformedURLException e) {
                log.info(e.getMessage());
            }
            axisConfig.validateSystemPredefinedPhases();
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
    }

    public void loadFromClassPath() throws DeploymentException {
        //loading modules from the classpath
        new RepositoryListener(this, true);
        org.apache.axis2.util.Utils.calculateDefaultModuleVersion(
                axisConfig.getModules(), axisConfig);
        axisConfig.validateSystemPredefinedPhases();
        try {
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VALIDATION_FAILED,
                                         axisFault.getMessage()));
            throw new DeploymentException(axisFault);
        }
    }

    private void loadCustomServices(URL repoURL){
        for (Map.Entry<String, Map<String, Deployer>> entry : getDeployers().entrySet()) {
            String directory = entry.getKey();
            Map<String, Deployer> extensionMap = entry.getValue();
            try {
                String listName;
                if (!directory.endsWith("/")) {
                    listName = directory + ".list";
                    directory += "/";
                } else {
                    listName = directory.replaceAll("/","") + ".list";
                }
                String repoPath = repoURL.getPath();
                if (!repoPath.endsWith("/")) {
                    repoPath += "/";
                    repoURL = new URL(repoURL.getProtocol() + "://" + repoPath);
                }

                URL servicesDir = new URL(repoURL, directory);
                URL filelisturl = new URL(servicesDir, listName);
                ArrayList files = getFileList(filelisturl);
                for (int i = 0; i < files.size(); i++) {
                    String fileName = (String) files.get(i);
                    String extension = getExtension(fileName);
                    Deployer deployer = extensionMap.get(extension);
                    if (deployer == null) {
                        continue;
                    }
                    URL servicesURL = new URL(servicesDir, fileName);

                    // We are calling reflection code here , to avoid changes to the interface
                    Class classToLoad = deployer.getClass();
                    // We can not call classToLoad.getDeclaredMethed() , since there
                    //  can be insatnce where mutiple services extends using one class
                    // just for init and other reflection methods
                    Method method = null;
                    try {
                        method = classToLoad.getMethod("deployFromURL", new Class[]{URL.class});
                    } catch (Exception e) {
                        //We do not need to inform this to user , since this something
                        // Axis2 is checking to support Session. So if the method is
                        // not there we should ignore that
                    }
                    if (method != null) {
                        try {
                            method.invoke(deployer, new Object[]{servicesURL});
                        } catch (Exception e) {
                            log.info("Exception trying to call " + "deployFromURL for the deployer" + deployer.getClass() , e);
                        }
                    }
                }

            } catch (MalformedURLException e) {
                //I am just ignoring the error at the moment , but need to think how to handle this
            }

        }
    }

    private String getExtension(String fileName){
        int lastIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastIndex +1);
    }

    public void loadServicesFromUrl(URL repoURL) {
        try {
            String path = servicesPath == null ? DeploymentConstants.SERVICE_PATH : servicesPath;
            if (!path.endsWith("/")) {
                path += "/";
            }
            String repoPath = repoURL.getPath();
            if (!repoPath.endsWith("/")) {
                repoPath += "/";
                repoURL = new URL(repoURL.getProtocol() + "://" + repoPath);
            }
            URL servicesDir = new URL(repoURL, path);
            URL filelisturl = new URL(servicesDir, "services.list");
            ArrayList files = getFileList(filelisturl);

            for (Iterator fileIterator = files.iterator();
                 fileIterator.hasNext();) {
                String fileUrl = (String) fileIterator.next();
                if (fileUrl.endsWith(".aar")) {
                    AxisServiceGroup serviceGroup = new AxisServiceGroup();
                    URL servicesURL = new URL(servicesDir, fileUrl);
                    ArrayList servicelist =
                            populateService(serviceGroup,
                                            servicesURL,
                                            fileUrl.substring(0, fileUrl.indexOf(".aar")));
                    addServiceGroup(serviceGroup, servicelist, servicesURL, null, axisConfig);
                    log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS,
                                                 serviceGroup.getServiceGroupName(),
                                                 servicesURL.toString()));
                }
            }
            //Loading other type of services such as custom deployers
            loadCustomServices(repoURL);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void loadRepositoryFromURL(URL repoURL) throws DeploymentException {
        try {
            String path = modulesPath == null ? DeploymentConstants.MODULE_PATH : modulesPath;
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            String repoPath = repoURL.getPath();
            if (!repoPath.endsWith("/")) {
                repoPath += "/";
                repoURL = new URL(repoURL.getProtocol() + "://" + repoPath);
            }
            URL moduleDir = new URL(repoURL, path);
            URL filelisturl = new URL(moduleDir, "modules.list");
            ArrayList files = getFileList(filelisturl);
            Iterator fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                String fileUrl = (String) fileIterator.next();
                if (fileUrl.endsWith(".mar")) {
                    URL moduleurl = new URL(moduleDir, fileUrl);
                    ClassLoader deploymentClassLoader =
                            Utils.createClassLoader(
                                    new URL[]{moduleurl},
                                    axisConfig.getModuleClassLoader(),
                                    true,
                                    (File) axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
                    AxisModule module = new AxisModule();
                    module.setModuleClassLoader(deploymentClassLoader);
                    module.setParent(axisConfig);
                    String moduleFile = fileUrl.substring(0, fileUrl.indexOf(".mar"));
                    module.setArchiveName(moduleFile);
                    populateModule(module, moduleurl);
                    module.setFileName(moduleurl);
                    addNewModule(module, axisConfig);
                    log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                                 module.getArchiveName(),
                                                 moduleurl.toString()));
                }
            }
            org.apache.axis2.util.Utils.
                    calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
            axisConfig.validateSystemPredefinedPhases();
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
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

    protected ArrayList populateService(AxisServiceGroup serviceGroup,
                                        URL servicesURL,
                                        String serviceName) throws DeploymentException {
        try {
            serviceGroup.setServiceGroupName(serviceName);
            ClassLoader serviceClassLoader = Utils.createClassLoader(
                    new URL[]{servicesURL},
                    axisConfig.getServiceClassLoader(),
                    true,
                    (File) axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
            String metainf = "meta-inf";
            serviceGroup.setServiceGroupClassLoader(serviceClassLoader);
            //processing wsdl.list
            InputStream wsdlfilesStream =
                    serviceClassLoader.getResourceAsStream("meta-inf/wsdl.list");
            if (wsdlfilesStream == null) {
                wsdlfilesStream = serviceClassLoader.getResourceAsStream("META-INF/wsdl.list");
                if (wsdlfilesStream != null) {
                    metainf = "META-INF";
                }
            }
            HashMap servicesMap = new HashMap();
            if (wsdlfilesStream != null) {
                ArchiveReader reader = new ArchiveReader();
                BufferedReader input = new BufferedReader(new InputStreamReader(wsdlfilesStream));
                String line;
                while ((line = input.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0 && line.charAt(0)!='#') {
                        line = metainf + "/" + line;
                        try {
                            List services = reader.getAxisServiceFromWsdl(
                                    serviceClassLoader.getResourceAsStream(line),
                                    serviceClassLoader, line);
                            if (services != null) {
                                for (int i = 0; i < services.size(); i++) {
                                    AxisService axisService = (AxisService) services.get(i);
                                    servicesMap.put(axisService.getName(), axisService);
                                }
                            }

                        } catch (Exception e) {
                            throw new DeploymentException(e);
                        }
                    }
                }
            }
            InputStream servicexmlStream =
                    serviceClassLoader.getResourceAsStream("META-INF/services.xml");
            if (servicexmlStream == null) {
                servicexmlStream = serviceClassLoader.getResourceAsStream("meta-inf/services.xml");
            } else {
                metainf = "META-INF";
            }
            if (servicexmlStream == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND,
                                            servicesURL.toString()));
            }
            DescriptionBuilder builder = new DescriptionBuilder(servicexmlStream, configContext);
            OMElement rootElement = builder.buildOM();
            String elementName = rootElement.getLocalName();

            if (TAG_SERVICE.equals(elementName)) {
                AxisService axisService = null;
                String wsdlLocation =  "META-INF/service.wsdl";
                InputStream wsdlStream =
                        serviceClassLoader.getResourceAsStream(wsdlLocation);
                URL wsdlURL = serviceClassLoader.getResource(metainf + "/service.wsdl");
                if (wsdlStream == null) {
                    wsdlLocation =  "META-INF/" + serviceName + ".wsdl";
                    wsdlStream = serviceClassLoader
                            .getResourceAsStream(wsdlLocation);
                    wsdlURL = serviceClassLoader.getResource(wsdlLocation);
                }
                if (wsdlStream != null) {
                    WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder =
                            new WSDL11ToAxisServiceBuilder(wsdlStream, null, null);
                    File file = Utils.toFile(servicesURL);
                    if(file != null && file.exists()){
                        wsdl2AxisServiceBuilder.setCustomWSLD4JResolver(
                                    new AARBasedWSDLLocator(wsdlLocation, file, wsdlStream));
                        wsdl2AxisServiceBuilder.setCustomResolver(
                                new AARFileBasedURIResolver(file));
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

                ServiceBuilder serviceBuilder = new ServiceBuilder(configContext, axisService);
                AxisService service = serviceBuilder.populateService(rootElement);

                ArrayList serviceList = new ArrayList();
                serviceList.add(service);
                return serviceList;
            } else if (TAG_SERVICE_GROUP.equals(elementName)) {
                ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement, servicesMap,
                                                                           configContext);
                ArrayList servicList = groupBuilder.populateServiceGroup(serviceGroup);
                Iterator serviceIterator = servicList.iterator();
                while (serviceIterator.hasNext()) {
                    AxisService axisService = (AxisService) serviceIterator.next();
                    String wsdlLocation =  "META-INF/service.wsdl";
                    InputStream wsdlStream =
                            serviceClassLoader.getResourceAsStream(wsdlLocation);
                    URL wsdlURL = serviceClassLoader.getResource(wsdlLocation);
                    if (wsdlStream == null) {
                        wsdlLocation =  "META-INF/" + serviceName + ".wsdl";
                        wsdlStream = serviceClassLoader
                                .getResourceAsStream(wsdlLocation);
                        wsdlURL =
                                serviceClassLoader.getResource(wsdlLocation);
                    }
                    if (wsdlStream != null) {
                        WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder =
                                new WSDL11ToAxisServiceBuilder(wsdlStream, axisService);
                        File file = Utils.toFile(servicesURL);
                        if(file != null && file.exists()){
                            wsdl2AxisServiceBuilder.setCustomWSLD4JResolver(
                                        new AARBasedWSDLLocator(wsdlLocation, file, wsdlStream));
                            wsdl2AxisServiceBuilder.setCustomResolver(
                                    new AARFileBasedURIResolver(file));
                        }
                        if (wsdlURL != null) {
                            wsdl2AxisServiceBuilder.setDocumentBaseUri(wsdlURL.toString());
                        }
                        axisService = wsdl2AxisServiceBuilder.populateService();
                        axisService.setWsdlFound(true);
                        axisService.setCustomWsdl(true);
                        // Set the default message receiver for the operations that were
                        // not listed in the services.xml
                        Iterator operations = axisService.getOperations();
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

    protected MessageReceiver loadDefaultMessageReceiver(String mepURL, AxisService service) {
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

    public static void addNewModule(AxisModule modulemetadata,
                                    AxisConfiguration axisConfiguration) throws AxisFault {

        Flow inflow = modulemetadata.getInFlow();
        ClassLoader moduleClassLoader = modulemetadata.getModuleClassLoader();

        if (inflow != null) {
            Utils.addFlowHandlers(inflow, moduleClassLoader);
        }

        Flow outFlow = modulemetadata.getOutFlow();

        if (outFlow != null) {
            Utils.addFlowHandlers(outFlow, moduleClassLoader);
        }

        Flow faultInFlow = modulemetadata.getFaultInFlow();

        if (faultInFlow != null) {
            Utils.addFlowHandlers(faultInFlow, moduleClassLoader);
        }

        Flow faultOutFlow = modulemetadata.getFaultOutFlow();

        if (faultOutFlow != null) {
            Utils.addFlowHandlers(faultOutFlow, moduleClassLoader);
        }

        axisConfiguration.addModule(modulemetadata);
        log.debug(Messages.getMessage(DeploymentErrorMsgs.ADDING_NEW_MODULE));
    }

    public static void addServiceGroup(AxisServiceGroup serviceGroup,
                                       ArrayList serviceList,
                                       URL serviceLocation,
                                       DeploymentFileData currentDeploymentFile,
                                       AxisConfiguration axisConfiguration) throws AxisFault {
        fillServiceGroup(serviceGroup, serviceList, serviceLocation, axisConfiguration);
        axisConfiguration.addServiceGroup(serviceGroup);
        if (currentDeploymentFile != null) {
            addAsWebResources(currentDeploymentFile.getFile(),
                              serviceGroup.getServiceGroupName(), serviceGroup);
        }
    }

    protected static void fillServiceGroup(AxisServiceGroup serviceGroup,
                                           ArrayList serviceList,
                                           URL serviceLocation,
                                           AxisConfiguration axisConfig) throws AxisFault {
//        serviceGroup.setParent(axisConfig);
        // module from services.xml at serviceGroup level
        ArrayList groupModules = serviceGroup.getModuleRefs();
        serviceGroup.setParent(axisConfig);
        for (int i = 0; i < groupModules.size(); i++) {
            String moduleName = (String) groupModules.get(i);
            AxisModule module = axisConfig.getModule(moduleName);

            if (module != null) {
                serviceGroup.engageModule(axisConfig.getModule(moduleName));
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE,
                                serviceGroup.getServiceGroupName(), moduleName));
            }
        }

        Iterator services = serviceList.iterator();

        while (services.hasNext()) {
            AxisService axisService = (AxisService) services.next();
            axisService.setUseDefaultChains(false);

            axisService.setFileName(serviceLocation);
            serviceGroup.addService(axisService);

            // modules from <service>
            ArrayList list = axisService.getModules();

            for (int i = 0; i < list.size(); i++) {
                AxisModule module = axisConfig.getModule((String) list.get(i));

                if (module == null) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE,
                                    axisService.getName(),
                                    ((QName) list.get(i)).getLocalPart()));
                }

                axisService.engageModule(module);
            }

            for (Iterator iterator = axisService.getOperations(); iterator.hasNext();) {
                AxisOperation opDesc = (AxisOperation) iterator.next();
                ArrayList modules = opDesc.getModuleRefs();

                for (int i = 0; i < modules.size(); i++) {
                    String moduleName = (String) modules.get(i);
                    AxisModule module = axisConfig.getModule(moduleName);

                    if (module != null) {
                        opDesc.engageModule(module);
                    } else {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.BAD_MODULE_FROM_OPERATION,
                                        opDesc.getName().getLocalPart(),
                                        moduleName));
                    }
                }
            }
        }
    }

    /**
     * @param file ArchiveFileData
     */
    public void addWSToDeploy(DeploymentFileData file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file WSInfo
     */
    public void addWSToUndeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploy() {
        try {
            if (wsToDeploy.size() > 0) {
                for (int i = 0; i < wsToDeploy.size(); i++) {
                    DeploymentFileData fileToDeploy = (DeploymentFileData) wsToDeploy.get(i);
                    try {
                        fileToDeploy.deploy();
                    } catch (DeploymentException e) {
                        // TODO : This probably isn't sufficient.  Maybe provide an option to stop?
                        log.info(e);
                    }
                }
            }
        } finally {
            wsToDeploy.clear();
        }
    }

    /**
     * Checks if the modules, referred by server.xml, exist or that they are deployed.
     *
     * @throws org.apache.axis2.AxisFault : If smt goes wrong
     */
    public void engageModules() throws AxisFault {
        axisConfig.engageGlobalModules();
    }

    /**
     * To get AxisConfiguration for a given inputStream this method can be used.
     * The inputstream should be a valid axis2.xml , else you will be getting
     * DeploymentExceptions.
     * <p/>
     * First creat a AxisConfiguration using given inputSream , and then it will
     * try to find the repository location parameter from AxisConfiguration, so
     * if user has add a parameter with the name "repository" , then the value
     * specified by that parameter will be the repository and system will try to
     * load modules and services from that repository location if it a valid
     * location. hot deployment and hot update will work as usual in this case.
     * <p/>
     * You will be getting AxisConfiguration corresponding to given inputstream
     * if it is valid , if something goes wrong you will be getting
     * DeploymentException
     *
     * @param in : InputStream to axis2.xml
     * @return a populated AxisConfiguration
     * @throws DeploymentException : If something goes wrong
     */
    public AxisConfiguration populateAxisConfiguration(InputStream in) throws DeploymentException {
        axisConfig = new AxisConfiguration();
        AxisConfigBuilder builder = new AxisConfigBuilder(in, axisConfig, this);
        builder.populateConfig();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            log.info("error in closing input stream");
        }
        moduleDeployer = new ModuleDeployer(axisConfig);
        return axisConfig;
    }

    /**
     * Starts the Deployment engine to perform Hot deployment and so on.
     *
     * @param listener : RepositoryListener
     */
    protected void startSearch(RepositoryListener listener) {
        scheduler = new Scheduler();

        scheduler.schedule(new SchedulerTask(listener), new DeploymentIterator());
    }

    public void unDeploy() {
        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                    if (wsInfo.getType() == WSInfo.TYPE_SERVICE) {
                        //No matter what we need to undeploy the service
                        // if user has deleted the file from the repository
                        serviceDeployer.unDeploy(wsInfo.getFileName());
                    } else {
                        //We need to undeploy the service whether we have enable hotUpdate or not ,
                        // o.w what happen if someone delete the service from the repo
                        Deployer deployer = wsInfo.getDeployer();
                        if (deployer != null) {
                            deployer.unDeploy(wsInfo.getFileName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info(e);
        }
        wsToUnDeploy.clear();
    }

    /**
     * Gets AxisConfiguration.
     *
     * @return AxisConfiguration <code>AxisConfiguration</code>
     */
    public AxisConfiguration getAxisConfig() {
        return axisConfig;
    }

    /**
     * Retrieves service name from the archive file name.
     * If the archive file name is service1.aar , then axis2 service name would be service1
     *
     * @param fileName the archive file name
     * @return Returns String.
     */
    public static String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value;
        int index = fileName.lastIndexOf(seperator);

        if (index > 0) {
            value = fileName.substring(0, index);

            return value;
        }

        return fileName;
    }

    public AxisModule getModule(String moduleName) throws AxisFault {
        return axisConfig.getModule(moduleName);
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }

    private static void addAsWebResources(File in,
                                          String serviceFileName,
                                          AxisServiceGroup serviceGroup) {
        try {
            if (webLocationString == null) {
                return;
            }
            if (in.isDirectory()) {
                return;
            }
            File webLocation = new File(webLocationString);
            File out = new File(webLocation, serviceFileName);
            int BUFFER = 1024;
            byte data[] = new byte[BUFFER];
            FileInputStream fin = new FileInputStream(in);
            ZipInputStream zin = new ZipInputStream(
                    fin);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                ZipEntry zip = new ZipEntry(entry);
                if (zip.getName().toUpperCase().startsWith("WWW")) {
                    String fileName = zip.getName();
                    fileName = fileName.substring("WWW/".length(),
                                                  fileName.length());
                    if (zip.isDirectory()) {
                        new File(out, fileName).mkdirs();
                    } else {
                        FileOutputStream tempOut = new FileOutputStream(new File(out, fileName));
                        int count;
                        while ((count = zin.read(data, 0, BUFFER)) != -1) {
                            tempOut.write(data, 0, count);
                        }
                        tempOut.close();
                        tempOut.flush();
                    }
                    serviceGroup.setFoundWebResources(true);
                }
            }
            zin.close();
            fin.close();
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public static String getWebLocationString() {
        return webLocationString;
    }

    /**
     * To set the all the classLoader hierarchy this method can be used , the top most parent is
     * CCL then SCL(system Class Loader)
     * CCL
     * :
     * SCL
     * :  :
     * MCCL  SCCL
     * :      :
     * MCL    SCL
     * <p/>
     * <p/>
     * MCCL :  module common class loader
     * SCCL : Service common class loader
     * MCL : module class loader
     * SCL  : Service class loader
     *
     * @param axis2repoURI : The repository folder of Axis2
     * @throws DeploymentException if there's a problem
     */
    protected void setClassLoaders(String axis2repoURI) throws DeploymentException {
        ClassLoader sysClassLoader =
                Utils.getClassLoader(Thread.currentThread().getContextClassLoader(), axis2repoURI);

        axisConfig.setSystemClassLoader(sysClassLoader);
        if (servicesDir.exists()) {
            axisConfig.setServiceClassLoader(
                    Utils.getClassLoader(axisConfig.getSystemClassLoader(), servicesDir));
        } else {
            axisConfig.setServiceClassLoader(axisConfig.getSystemClassLoader());
        }

        if (modulesDir.exists()) {
            axisConfig.setModuleClassLoader(Utils.getClassLoader(axisConfig.getSystemClassLoader(),
                                                                 modulesDir));
        } else {
            axisConfig.setModuleClassLoader(axisConfig.getSystemClassLoader());
        }
    }

    /**
     * Sets hotDeployment and hot update.
     */
    protected void setDeploymentFeatures() {
        Parameter hotDeployment = axisConfig.getParameter(TAG_HOT_DEPLOYMENT);
        Parameter hotUpdate = axisConfig.getParameter(TAG_HOT_UPDATE);

        if (hotDeployment != null) {
            this.hotDeployment = JavaUtils.isTrue(hotDeployment.getValue(), true);
        }

        if (hotUpdate != null) {
            this.hotUpdate = JavaUtils.isTrue(hotUpdate.getValue(), true);
        }

        String serviceDirPara = (String)
                axisConfig.getParameterValue(DeploymentConstants.SERVICE_DIR_PATH);
        if (serviceDirPara != null) {
            servicesPath = serviceDirPara;
        }

        String moduleDirPara = (String)
                axisConfig.getParameterValue(DeploymentConstants.MODULE_DRI_PATH);
        if (moduleDirPara != null) {
            modulesPath = moduleDirPara;
        }
    }

    /**
     * Creates directories for modules/services, copies configuration xml from class loader if necessary
     *
     * @param repositoryName the pathname of the repository
     */

    protected void prepareRepository(String repositoryName) {
        repositoryDir = new File(repositoryName);
        if (servicesPath != null) {
            servicesDir = new File(servicesPath);
            if (!servicesDir.exists()) {
                servicesDir = new File(repositoryDir, servicesPath);
            }
        } else {
            servicesDir = new File(repositoryDir, DeploymentConstants.SERVICE_PATH);
        }
        if (!servicesDir.exists()) {
            log.info(Messages.getMessage("noservicedirfound", getRepositoryPath(repositoryDir)));
        }
        if (modulesPath != null) {
            modulesDir = new File(modulesPath);
            if (!modulesDir.exists()) {
                modulesDir = new File(repositoryDir, modulesPath);
            }
        } else {
            modulesDir = new File(repositoryDir, DeploymentConstants.MODULE_PATH);
        }
        if (!modulesDir.exists()) {
            log.info(Messages.getMessage("nomoduledirfound", getRepositoryPath(repositoryDir)));
        }
    }

    protected String getRepositoryPath(File repository) {
        try {
            return repository.getCanonicalPath();
        } catch (IOException e) {
            return repository.getAbsolutePath();
        }
    }

    protected ArrayList getFileList(URL fileListUrl) {
        ArrayList fileList = new ArrayList();
        InputStream in;
        try {
            in = fileListUrl.openStream();
        } catch (IOException e) {
            return fileList;
        }
        BufferedReader input = null;
        try {
            input = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = input.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && line.charAt(0)!='#') {
                    fileList.add(line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return fileList;
    }

    public void setConfigContext(ConfigurationContext configContext) {
        this.configContext = configContext;
        initializeDeployers(this.configContext);
    }

    private void initializeDeployers(ConfigurationContext configContext) {
        serviceDeployer = new ServiceDeployer();
        serviceDeployer.init(configContext);        
        for (Map<String, Deployer> extensionMap : deployerMap.values()) {
            for (Deployer deployer : extensionMap.values()) {
                deployer.init(configContext);
            }
        }
    }
    
    /**
     * Builds an AxisModule for a given module archive file. This does not
     * called the init method since there is no reference to configuration context
     * so who ever create module using this has to called module.init if it is
     * required
     *
     * @param modulearchive : Actual module archive file
     * @param config        : AxisConfiguration : for get classloaders etc..
     * @return a complete AxisModule read from the file.
     * @throws org.apache.axis2.deployment.DeploymentException
     *          if there's a problem
     */
    public static AxisModule buildModule(File modulearchive,
                                         AxisConfiguration config)
            throws DeploymentException {
        final String MODULE_DEPLOYER = "moduleDeployer";
        AxisModule axismodule;
        ModuleDeployer deployer = (ModuleDeployer) config.getParameterValue(MODULE_DEPLOYER);
        try {
            if (deployer == null) {
                deployer = new ModuleDeployer(config);
                config.addParameter(MODULE_DEPLOYER, deployer);
            }

            DeploymentFileData currentDeploymentFile = new DeploymentFileData(modulearchive,
                                                                              deployer);
            axismodule = new AxisModule();
            ArchiveReader archiveReader = new ArchiveReader();

            currentDeploymentFile.setClassLoader(false, config.getModuleClassLoader(), null);
            axismodule.setModuleClassLoader(currentDeploymentFile.getClassLoader());
            archiveReader.readModuleArchive(currentDeploymentFile, axismodule,
                                            false, config);
            ClassLoader moduleClassLoader = axismodule.getModuleClassLoader();
            Flow inflow = axismodule.getInFlow();

            if (inflow != null) {
                Utils.addFlowHandlers(inflow, moduleClassLoader);
            }

            Flow outFlow = axismodule.getOutFlow();

            if (outFlow != null) {
                Utils.addFlowHandlers(outFlow, moduleClassLoader);
            }

            Flow faultInFlow = axismodule.getFaultInFlow();

            if (faultInFlow != null) {
                Utils.addFlowHandlers(faultInFlow, moduleClassLoader);
            }

            Flow faultOutFlow = axismodule.getFaultOutFlow();

            if (faultOutFlow != null) {
                Utils.addFlowHandlers(faultOutFlow, moduleClassLoader);
            }
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
        return axismodule;
    }

    /**
     * Fills an axisservice object using services.xml. First creates
     * an axisservice object using WSDL and then fills it using the given services.xml.
     * Loads all the required class and builds the chains, finally adds the
     * servicecontext to EngineContext and axisservice into EngineConfiguration.
     *
     * @param serviceInputStream InputStream containing configuration data
     * @param configCtx          the ConfigurationContext in which we're deploying
     * @return Returns AxisService.
     * @throws DeploymentException if there's a problem
     */
    public static AxisService buildService(InputStream serviceInputStream,
                                           ConfigurationContext configCtx)
            throws DeploymentException {
        AxisService axisService = new AxisService();
        try {
            ServiceBuilder builder = new ServiceBuilder(serviceInputStream, configCtx, axisService);
            builder.populateService(builder.buildOM());
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }

        return axisService;
    }

    /**
     * To build a AxisServiceGroup for a given services.xml
     * You have to add the created group into AxisConfig
     *
     * @param servicesxml      InputStream created from services.xml or equivalent
     * @param classLoader      ClassLoader to use
     * @param serviceGroupName name of the service group
     * @param configCtx        the ConfigurationContext in which we're deploying
     * @param archiveReader    the ArchiveReader we're working with
     * @param wsdlServices     Map of existing WSDL services
     * @return a fleshed-out AxisServiceGroup
     * @throws AxisFault if there's a problem
     */
    public static AxisServiceGroup buildServiceGroup(InputStream servicesxml,
                                                     ClassLoader classLoader,
                                                     String serviceGroupName,
                                                     ConfigurationContext configCtx,
                                                     ArchiveReader archiveReader,
                                                     HashMap wsdlServices) throws AxisFault {
        DeploymentFileData currentDeploymentFile = new DeploymentFileData(null, null);
        currentDeploymentFile.setClassLoader(classLoader);
        AxisServiceGroup serviceGroup = new AxisServiceGroup();
        serviceGroup.setServiceGroupClassLoader(classLoader);
        serviceGroup.setServiceGroupName(serviceGroupName);
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        try {
            ArrayList serviceList = archiveReader.buildServiceGroup(servicesxml,
                                                                    currentDeploymentFile,
                                                                    serviceGroup,
                                                                    wsdlServices, configCtx);
            fillServiceGroup(serviceGroup, serviceList, null, axisConfig);
            return serviceGroup;
        } catch (XMLStreamException e) {
            throw AxisFault.makeFault(e);
        }
    }

    public static AxisServiceGroup loadServiceGroup(File serviceFile,
                                                    ConfigurationContext configCtx)
            throws AxisFault {
        try {
            DeploymentFileData currentDeploymentFile = new DeploymentFileData(serviceFile, null);
            DeploymentClassLoader classLoader = Utils.createClassLoader(serviceFile);
            currentDeploymentFile.setClassLoader(classLoader);
            AxisServiceGroup serviceGroup = new AxisServiceGroup();
            serviceGroup.setServiceGroupClassLoader(classLoader);

            // Drop the extension and take the name
            String fileName = serviceFile.getName();
            String serviceGroupName = fileName.substring(0, fileName.lastIndexOf("."));
            serviceGroup.setServiceGroupName(serviceGroupName);
            AxisConfiguration axisConfig = configCtx.getAxisConfiguration();

            ArchiveReader archiveReader = new ArchiveReader();
            HashMap wsdlServices = archiveReader.processWSDLs(currentDeploymentFile);
            InputStream serviceXml = classLoader.getResourceAsStream("META-INF/services.xml");
            ArrayList serviceList = archiveReader.buildServiceGroup(serviceXml,
                                                                    currentDeploymentFile,
                                                                    serviceGroup,
                                                                    wsdlServices, configCtx);
            fillServiceGroup(serviceGroup, serviceList, null, axisConfig);
            return serviceGroup;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public File getServicesDir() {
        return servicesDir;
    }

    public File getModulesDir() {
        return modulesDir;
    }


    public File getRepositoryDir() {
        return repositoryDir;
    }

    public void setExtensionToDeployerMappingMap(HashMap extensionToDeployerMappingMap) {
        this.extensionToDeployerMappingMap = extensionToDeployerMappingMap;
    }
    
    public void setDeployers(Map<String, Map<String, Deployer>> deployerMap) {
        this.deployerMap = deployerMap;
    }    
    
    public Map<String, Map<String, Deployer>> getDeployers() {
        return this.deployerMap;
    }
    
    public RepositoryListener getRepoListener() {
        return repoListener;
    }


    public ServiceDeployer getServiceDeployer() {
        return serviceDeployer;
    }

    public ModuleDeployer getModuleDeployer() {
        return moduleDeployer;
    }
    
    public Deployer getDeployer(String directory, String extension) {
        Map<String, Deployer> extensionMap = deployerMap.get(directory);
        return (extensionMap != null) ? extensionMap.get(extension) : null;
    }
    
    public Deployer getDeployerForExtension(String extension) {
        return (Deployer) extensionToDeployerMappingMap.get(extension);
    }

    /**
     * Clean up the mess
     */
    public void cleanup() {
        if (axisConfig.getModuleClassLoader() instanceof JarFileClassLoader) {
            ((JarFileClassLoader)axisConfig.getModuleClassLoader()).destroy();
        }
        if (axisConfig.getServiceClassLoader() instanceof JarFileClassLoader) {
            ((JarFileClassLoader)axisConfig.getServiceClassLoader()).destroy();
        }
        if (axisConfig.getSystemClassLoader() instanceof JarFileClassLoader) {
            ((JarFileClassLoader)axisConfig.getSystemClassLoader()).destroy();
        }
        if (scheduler != null) {
            scheduler.cleanup();
        }
    }
}
