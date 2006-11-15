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


package org.apache.axis2.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveFileData;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.scheduler.DeploymentIterator;
import org.apache.axis2.deployment.scheduler.Scheduler;
import org.apache.axis2.deployment.scheduler.SchedulerTask;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DeploymentEngine implements DeploymentConstants {

    private static final Log log = LogFactory.getLog(DeploymentEngine.class);
    private boolean hotUpdate = true;    // to do hot update or not
    private boolean hotDeployment = true;    // to do hot deployment or not
    private boolean antiJARLocking = false;    // to do hot deployment or not
    /**
     * Stores all the web Services to deploy.
     */
    private List wsToDeploy = new ArrayList();

    /**
     * Stores all the web Services to undeploy.
     */
    private List wsToUnDeploy = new ArrayList();

    //to keep the web resource location if any
    private String webLocationString = null;

    /**
     * to keep a ref to engine register
     * this ref will pass to engine when it call start()
     * method
     */
    protected AxisConfiguration axisConfig;

    protected ConfigurationContext configContext;

    private RepositoryListener repoListener;

    private String servicesPath = null;
    private File servicesDir = null;
    private String modulesPath = null;
    private File modulesDir = null;

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
        org.apache.axis2.util.Utils.calculateDefaultModuleVersion(axisConfig.getModules(), axisConfig);
        try {
            try {
                axisConfig.setRepository(axisRepo.toURL());
            } catch (MalformedURLException e) {
                log.info(e.getMessage());
            }
            validateSystemPredefinedPhases();
        } catch (AxisFault axisFault) {
            throw new DeploymentException(axisFault);
        }
    }

    public void loadFromClassPath() throws DeploymentException {
        //loading modules from the classpath
        new RepositoryListener(this, true);
        org.apache.axis2.util.Utils.calculateDefaultModuleVersion(
                axisConfig.getModules(), axisConfig);
        validateSystemPredefinedPhases();
        try {
            engageModules();
        } catch (AxisFault axisFault) {
            log.info(Messages.getMessage(DeploymentErrorMsgs.MODULE_VALIDATION_FAILED,
                    axisFault.getMessage()));
            throw new DeploymentException(axisFault);
        }
    }

    public void loadServicesFromUrl(URL repoURL) {
        try {
            String path = servicesPath == null ? DeploymentConstants.SERVICE_PATH : servicesPath;
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            URL servicesDir = new URL(repoURL, path);
            URL filelisturl = new URL(servicesDir, "services.list");
            ArrayList files = getFileList(filelisturl);
            Iterator fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                String fileUrl = (String) fileIterator.next();
                if (fileUrl.endsWith(".aar")) {
                    AxisServiceGroup serviceGroup = new AxisServiceGroup();
                    URL servicesURL = new URL(servicesDir, fileUrl);
                    ArrayList servicelist = populateService(serviceGroup,
                            servicesURL,
                            fileUrl.substring(0, fileUrl.indexOf(".aar")));
                    addServiceGroup(serviceGroup, servicelist, servicesURL, null);
                }
            }
        } catch (MalformedURLException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }

    public void loadRepositoryFromURL(URL repoURL) throws DeploymentException {
        try {
            String path = modulesPath == null ? DeploymentConstants.MODULE_PATH : modulesPath;
            if (!path.endsWith("/")) {
                path = path + "/";
            }
            URL moduleDir = new URL(repoURL, path);
            URL filelisturl = new URL(moduleDir, "modules.list");
            ArrayList files = getFileList(filelisturl);
            Iterator fileIterator = files.iterator();
            while (fileIterator.hasNext()) {
                String fileUrl = (String) fileIterator.next();
                if (fileUrl.endsWith(".mar")) {
                    URL moduleurl = new URL(moduleDir, fileUrl);
                    DeploymentClassLoader deploymentClassLoader =
                            new DeploymentClassLoader(
                                    new URL[]{moduleurl},
                                    axisConfig.getModuleClassLoader(),
                                    antiJARLocking);
                    AxisModule module = new AxisModule();
                    module.setModuleClassLoader(deploymentClassLoader);
                    module.setParent(axisConfig);
                    String moduleName = fileUrl.substring(0, fileUrl.indexOf(".mar"));
                    module.setName(new QName(moduleName));
                    populateModule(module, moduleurl);
                    module.setFileName(moduleurl);
                    addNewModule(module);
                }
            }
            org.apache.axis2.util.Utils.calculateDefaultModuleVersion(
                    axisConfig.getModules(), axisConfig);
            validateSystemPredefinedPhases();
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
    }

    private void populateModule(AxisModule module, URL moduleUrl) throws DeploymentException {
        try {
            ClassLoader classLoadere = module.getModuleClassLoader();
            InputStream moduleStream = classLoadere.getResourceAsStream("META-INF/module.xml");
            if (moduleStream == null) {
                moduleStream = classLoadere.getResourceAsStream("meta-inf/module.xml");
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

    private ArrayList populateService(AxisServiceGroup serviceGroup,
                                      URL servicesURL,
                                      String serviceName) throws DeploymentException {
        try {
            serviceGroup.setServiceGroupName(serviceName);
            DeploymentClassLoader serviceClassLoader = new DeploymentClassLoader(
                    new URL[]{servicesURL}, axisConfig.getServiceClassLoader(), antiJARLocking);
            String metainf = "meta-inf";
            serviceGroup.setServiceGroupClassLoader(serviceClassLoader);
            InputStream servicexmlStream = serviceClassLoader.getResourceAsStream("META-INF/services.xml");
            if (servicexmlStream == null) {
                servicexmlStream = serviceClassLoader.getResourceAsStream("meta-inf/services.xml");
            } else {
                metainf = "META-INF";
            }
            if (servicexmlStream == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND, servicesURL.toString()));
            }
            DescriptionBuilder builder = new DescriptionBuilder(servicexmlStream, configContext);
            OMElement rootElement = builder.buildOM();
            String elementName = rootElement.getLocalName();

            if (TAG_SERVICE.equals(elementName)) {
                AxisService axisService = null;
                InputStream wsdlStream = serviceClassLoader.getResourceAsStream(metainf + "/service.wsdl");
                if (wsdlStream == null) {
                    wsdlStream = serviceClassLoader.getResourceAsStream(metainf + "/" + serviceName + ".wsdl");
                }
                if (wsdlStream != null) {
                    WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder =
                            new WSDL11ToAxisServiceBuilder(wsdlStream, null, null);
                    axisService = wsdl2AxisServiceBuilder.populateService();
                    axisService.setWsdlFound(true);
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
                ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement, new HashMap(),
                        configContext);
                ArrayList servicList = groupBuilder.populateServiceGroup(serviceGroup);
                Iterator serviceIterator = servicList.iterator();
                while (serviceIterator.hasNext()) {
                    AxisService axisService = (AxisService) serviceIterator.next();
                    InputStream wsdlStream = serviceClassLoader.getResourceAsStream(metainf + "/service.wsdl");
                    if (wsdlStream == null) {
                        wsdlStream = serviceClassLoader.getResourceAsStream(metainf + "/" + serviceName + ".wsdl");
                        if (wsdlStream != null) {
                            WSDL11ToAxisServiceBuilder wsdl2AxisServiceBuilder =
                                    new WSDL11ToAxisServiceBuilder(wsdlStream, axisService);
                            axisService = wsdl2AxisServiceBuilder.populateService();
                            axisService.setWsdlFound(true);
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
            mepURL = WSDLConstants.WSDL20_2004Constants.MEP_URI_IN_OUT;
        }
        if (service != null) {
            messageReceiver = service.getMessageReceiver(mepURL);
            if (messageReceiver != null) {
                return messageReceiver;
            }
        }
        return axisConfig.getMessageReceiver(mepURL);
    }

    private void addNewModule(AxisModule modulemetadata) throws AxisFault {

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

        axisConfig.addModule(modulemetadata);
        log.debug(Messages.getMessage(DeploymentErrorMsgs.ADDING_NEW_MODULE));
    }

    private void addServiceGroup(AxisServiceGroup serviceGroup,
                                 ArrayList serviceList,
                                 URL serviceLocation,
                                 ArchiveFileData currentArchiveFile)
            throws AxisFault {
        fillServiceGroup(serviceGroup, serviceList, serviceLocation, axisConfig);
        axisConfig.addServiceGroup(serviceGroup);
        if (currentArchiveFile != null) {
            addAsWebResources(currentArchiveFile.getFile(),
                    serviceGroup.getServiceGroupName(), serviceGroup);
        }
    }

    private static void fillServiceGroup(AxisServiceGroup serviceGroup,
                                         ArrayList serviceList,
                                         URL serviceLocation,
                                         AxisConfiguration axisConfig) throws AxisFault {
        serviceGroup.setParent(axisConfig);
        // module from services.xml at serviceGroup level
        ArrayList groupModules = serviceGroup.getModuleRefs();

        for (int i = 0; i < groupModules.size(); i++) {
            QName moduleName = (QName) groupModules.get(i);
            AxisModule module = axisConfig.getModule(moduleName);

            if (module != null) {
                serviceGroup.engageModule(axisConfig.getModule(moduleName), axisConfig);
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE,
                                serviceGroup.getServiceGroupName(), moduleName.getLocalPart()));
            }
        }

        Iterator services = serviceList.iterator();

        while (services.hasNext()) {
            AxisService axisService = (AxisService) services.next();
            String scope = axisService.getScope();
            if (Constants.SCOPE_TRANSPORT_SESSION.equals(scope)) {
                if (!axisConfig.isManageTransportSession()) {
                    throw new DeploymentException("You can not deploy the service " +
                            "in transport session , since transport session management" +
                            " disabled in axis2.xml change manageTransportSession parameter value to true");
                }
            }
            axisService.setUseDefaultChains(false);

            axisService.setFileName(serviceLocation);
            serviceGroup.addService(axisService);

            // modules from <service>
            ArrayList list = axisService.getModules();

            for (int i = 0; i < list.size(); i++) {
                AxisModule module = axisConfig.getModule((QName) list.get(i));

                if (module == null) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.BAD_MODULE_FROM_SERVICE, axisService.getName(),
                                    ((QName) list.get(i)).getLocalPart()));
                }

                axisService.engageModule(module, axisConfig);
            }

            for (Iterator iterator = axisService.getOperations(); iterator.hasNext();) {
                AxisOperation opDesc = (AxisOperation) iterator.next();
                ArrayList modules = opDesc.getModuleRefs();

                for (int i = 0; i < modules.size(); i++) {
                    QName moduleName = (QName) modules.get(i);
                    AxisModule module = axisConfig.getModule(moduleName);

                    if (module != null) {
                        opDesc.engageModule(module, axisConfig);
                    } else {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.BAD_MODULE_FROM_OPERATION,
                                        opDesc.getName().getLocalPart(), moduleName.getLocalPart()));
                    }
                }
            }
        }
    }

    private void addAsWebResources(File in, String serviceFileName, AxisServiceGroup serviceGroup) {
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

    /**
     * @param file
     */
    public void addWSToDeploy(ArchiveFileData file) {
        wsToDeploy.add(file);
    }

    /**
     * @param file
     */
    public void addWSToUndeploy(WSInfo file) {
        wsToUnDeploy.add(file);
    }

    public void doDeploy() {
        if (wsToDeploy.size() > 0) {
            for (int i = 0; i < wsToDeploy.size(); i++) {
                ArchiveFileData currentArchiveFile = (ArchiveFileData) wsToDeploy.get(i);
                boolean explodedDir = currentArchiveFile.getFile().isDirectory();
                int type = currentArchiveFile.getType();
                try {
                    ArchiveReader archiveReader;
                    StringWriter errorWriter = new StringWriter();
                    switch (type) {
                        case TYPE_SERVICE :
                            currentArchiveFile.setClassLoader(explodedDir,
                                    axisConfig.getServiceClassLoader());
                            archiveReader = new ArchiveReader();
                            String serviceStatus = "";
                            try {
                                HashMap wsdlservice = archiveReader.processWSDLs(currentArchiveFile);
                                if (wsdlservice != null && wsdlservice.size() > 0) {
                                    Iterator services = wsdlservice.values().iterator();
                                    while (services.hasNext()) {
                                        AxisService service = (AxisService) services.next();
                                        Iterator operations = service.getOperations();
                                        while (operations.hasNext()) {
                                            AxisOperation axisOperation = (AxisOperation) operations.next();
                                            axisConfig.getPhasesInfo().setOperationPhases(axisOperation);
                                        }
                                    }
                                }
                                AxisServiceGroup sericeGroup = new AxisServiceGroup(axisConfig);
                                sericeGroup.setServiceGroupClassLoader(
                                        currentArchiveFile.getClassLoader());
                                ArrayList serviceList = archiveReader.processServiceGroup(
                                        currentArchiveFile.getAbsolutePath(), currentArchiveFile,
                                        sericeGroup, explodedDir, wsdlservice,
                                        configContext);
                                addServiceGroup(sericeGroup, serviceList, currentArchiveFile.getFile().toURL(), currentArchiveFile);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_WS,
                                        currentArchiveFile.getName()));
                            } catch (DeploymentException de) {
                                de.printStackTrace();
                                log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                        currentArchiveFile.getName(),
                                        de.getMessage()),
                                        de);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                de.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } catch (AxisFault axisFault) {
                                log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_SERVICE,
                                        currentArchiveFile.getName(),
                                        axisFault.getMessage()),
                                        axisFault);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                axisFault.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } catch (Exception e) {
                                if (log.isInfoEnabled()) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    e.printStackTrace(pw);
                                    log.info(Messages.getMessage(
                                            DeploymentErrorMsgs.INVALID_SERVICE,
                                            currentArchiveFile.getName(),
                                            sw.getBuffer().toString()));
                                }
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                e.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } catch (Throwable t) {
                                if (log.isInfoEnabled()) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    t.printStackTrace(pw);
                                    log.info(Messages.getMessage(
                                            DeploymentErrorMsgs.INVALID_SERVICE,
                                            currentArchiveFile.getName(),
                                            sw.getBuffer().toString()));
                                }
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                t.printStackTrace(error_ptintWriter);
                                serviceStatus = "Error:\n" + errorWriter.toString();
                            } finally {
                                if (serviceStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyServices().put(currentArchiveFile.getFile().getAbsolutePath(),
                                            serviceStatus);
                                }
                                currentArchiveFile = null;
                            }
                            break;
                        case TYPE_MODULE :
                            currentArchiveFile.setClassLoader(explodedDir,
                                    axisConfig.getModuleClassLoader());
                            archiveReader = new ArchiveReader();
                            String moduleStatus = "";
                            try {
                                AxisModule metaData = new AxisModule();
                                metaData.setModuleClassLoader(currentArchiveFile.getClassLoader());
                                metaData.setParent(axisConfig);
                                archiveReader.readModuleArchive(currentArchiveFile, metaData, explodedDir,
                                        axisConfig);
                                metaData.setFileName(currentArchiveFile.getFile().toURL());
                                addNewModule(metaData);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                        metaData.getName().getLocalPart()));
                            } catch (DeploymentException e) {
                                log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                        currentArchiveFile.getName(),
                                        e.getMessage()),
                                        e);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                e.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } catch (AxisFault axisFault) {
                                log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                        currentArchiveFile.getName(),
                                        axisFault.getMessage()),
                                        axisFault);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                axisFault.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } catch (MalformedURLException e) {
                                log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                        currentArchiveFile.getName(),
                                        e.getMessage()),
                                        e);
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                e.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } catch (Throwable t) {
                                if (log.isInfoEnabled()) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    t.printStackTrace(pw);
                                    log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                            currentArchiveFile.getName(),
                                            t.getMessage()),
                                            t);
                                }
                                PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
                                t.printStackTrace(error_ptintWriter);
                                moduleStatus = "Error:\n" + errorWriter.toString();
                            } finally {
                                if (moduleStatus.startsWith("Error:")) {
                                    axisConfig.getFaultyModules().put(
                                            getAxisServiceName(currentArchiveFile.getName()), moduleStatus);
                                }
                                currentArchiveFile = null;
                            }
                            break;
                    }
                } catch (AxisFault axisFault) {
                    log.info(Messages.getMessage(DeploymentErrorMsgs.ERROR_SETTING_CLIENT_HOME,
                            axisFault.getMessage()),
                            axisFault);
                }
            }
        }

        wsToDeploy.clear();
    }

    /**
     * Checks if the modules, referred by server.xml, exist or that they are deployed.
     */
    public void engageModules() throws AxisFault {
        for (Iterator iterator = axisConfig.getGlobalModules().iterator(); iterator.hasNext();) {
            QName name = (QName) iterator.next();
            axisConfig.engageModule(name);
        }
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
     * DeploymentExeption
     *
     * @param in
     * @throws DeploymentException
     */
    public AxisConfiguration populateAxisConfiguration(InputStream in) throws DeploymentException {
        axisConfig = new AxisConfiguration();
        AxisConfigBuilder builder = new AxisConfigBuilder(in, axisConfig);
        builder.populateConfig();
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            log.info("error in closing input stream");
        }
        return axisConfig;
    }

    /**
     * Starts the Deployment engine to perform Hot deployment and so on.
     */
    private void startSearch(RepositoryListener listener) {
        Scheduler scheduler = new Scheduler();

        scheduler.schedule(new SchedulerTask(listener), new DeploymentIterator());
    }

    public void unDeploy() {
        String fileName;
        try {
            if (wsToUnDeploy.size() > 0) {
                for (int i = 0; i < wsToUnDeploy.size(); i++) {
                    WSInfo wsInfo = (WSInfo) wsToUnDeploy.get(i);
                    if (wsInfo.getType() == TYPE_SERVICE) {
                        if (isHotUpdate()) {
                            try {
                                fileName = getAxisServiceName(wsInfo.getFileName());
                                axisConfig.removeServiceGroup(fileName);
                                log.info(Messages.getMessage(DeploymentErrorMsgs.SERVICE_REMOVED,
                                        wsInfo.getFileName()));
                            } catch (AxisFault axisFault) {
                                //May be a faulty service
                                axisConfig.removeFaultyService(wsInfo.getFileName());
                            }
                        } else {
                            axisConfig.removeFaultyService(wsInfo.getFileName());
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
     * Checks whether some one has changed the system pre-defined phases
     * for all the flows. If they have been changed,throws a DeploymentException.
     *
     * @throws DeploymentException
     */
    private void validateSystemPredefinedPhases() throws DeploymentException {
        PhasesInfo phasesInfo = axisConfig.getPhasesInfo();
        axisConfig.setInPhasesUptoAndIncludingPostDispatch(phasesInfo.getGlobalInflow());
        axisConfig.setInFaultPhases(phasesInfo.getGlobalInFaultPhases());
        axisConfig.setGlobalOutPhase(phasesInfo.getGlobalOutPhaseList());
        axisConfig.setOutFaultPhases(phasesInfo.getOUT_FaultPhases());
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
     * @param fileName
     * @return Returns String.
     */
    private String getAxisServiceName(String fileName) {
        char seperator = '.';
        String value;
        int index = fileName.indexOf(seperator);

        if (index > 0) {
            value = fileName.substring(0, index);

            return value;
        }

        return fileName;
    }

    public AxisModule getModule(QName moduleName) throws AxisFault {
        return axisConfig.getModule(moduleName);
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }

    public boolean isAntiJARLocking() {
        return antiJARLocking;
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
     * @throws DeploymentException
     */
    private void setClassLoaders(String axis2repoURI) throws DeploymentException {
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
    private void setDeploymentFeatures() {
        String value;
        Parameter parahotdeployment = axisConfig.getParameter(TAG_HOT_DEPLOYMENT);
        Parameter parahotupdate = axisConfig.getParameter(TAG_HOT_UPDATE);
//        Parameter paraantiJARLocking = axisConfig.getParameter(TAG_ANTI_JAR_LOCKING);

        if (parahotdeployment != null) {
            value = (String) parahotdeployment.getValue();

            if ("false".equalsIgnoreCase(value)) {
                hotDeployment = false;
            }
        }

        if (parahotupdate != null) {
            value = (String) parahotupdate.getValue();

            if ("false".equalsIgnoreCase(value)) {
                hotUpdate = false;
            }
        }

        if (parahotupdate != null) {
            value = (String) parahotupdate.getValue();

            if ("true".equalsIgnoreCase(value)) {
                antiJARLocking = true;
            }
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
     * @param repositoryName
     */

    private void prepareRepository(String repositoryName) {
        File repository = new File(repositoryName);
        if (servicesPath != null) {
            servicesDir = new File(servicesPath);
            if (!servicesDir.exists()) {
                servicesDir = new File(repository, servicesPath);
            }
        } else {
            servicesDir = new File(repository, DeploymentConstants.SERVICE_PATH);
        }
        if (!servicesDir.exists()) {
            log.info(Messages.getMessage("noservicedirfound", getRepositoryPath(repository)));
        }
        if (modulesPath != null) {
            modulesDir = new File(modulesPath);
            if (!modulesDir.exists()) {
                modulesDir = new File(repository, modulesPath);
            }
        } else {
            modulesDir = new File(repository, DeploymentConstants.MODULE_PATH);
        }
        if (!modulesDir.exists()) {
            log.info(Messages.getMessage("nomoduledirfound", getRepositoryPath(repository)));
        }
    }

    private String getRepositoryPath(File repository) {
        try {
            return repository.getCanonicalPath();
        } catch (IOException e) {
            return repository.getAbsolutePath();
        }
    }

    private ArrayList getFileList(URL fileListUrl) {
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
                if (line.length() > 0) {
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

    public String getWebLocationString() {
        return webLocationString;
    }

    public void setWebLocationString(String webLocationString) {
        this.webLocationString = webLocationString;
    }

    public void setConfigContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    /**
     * Builds ModuleDescription for a given module archive file. This does not
     * called the init method since there is no reference to configuration context
     * so who ever create module using this has to called module.init if it is
     * required
     *
     * @param modulearchive : Actual module archive file
     * @param config        : AxisConfiguration : for get classloaders etc..
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public static AxisModule buildModule(File modulearchive,
                                         AxisConfiguration config)
            throws DeploymentException {
        AxisModule axismodule;
        try {
            ArchiveFileData currentArchiveFile = new ArchiveFileData(modulearchive,
                    DeploymentConstants.TYPE_MODULE, false);
            axismodule = new AxisModule();
            ArchiveReader archiveReader = new ArchiveReader();

            currentArchiveFile.setClassLoader(false, config.getModuleClassLoader());
            axismodule.setModuleClassLoader(currentArchiveFile.getClassLoader());
            archiveReader.readModuleArchive(currentArchiveFile, axismodule,
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
     * @param serviceInputStream
     * @param classLoader
     * @return Returns AxisService.
     * @throws DeploymentException
     */
    public static AxisService buildService(InputStream serviceInputStream,
                                           ClassLoader classLoader,
                                           ConfigurationContext configCtx)
            throws DeploymentException {
        AxisService axisService = new AxisService();
        try {

            AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
            Parameter parahotupdate = axisConfig.getParameter(TAG_HOT_UPDATE);
            boolean antiJARLocking = true;
            if (parahotupdate != null) {
                String value = (String) parahotupdate.getValue();

                if ("false".equalsIgnoreCase(value)) {
                    antiJARLocking = false;
                }
            }
            ArchiveFileData currentArchiveFile = new ArchiveFileData(
                    DeploymentConstants.TYPE_SERVICE, "", antiJARLocking);
            currentArchiveFile.setClassLoader(classLoader);

            ServiceBuilder builder = new ServiceBuilder(serviceInputStream, configCtx,
                    axisService);

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
     * @param servicesxml      : inpupstream create using services.xml
     * @param classLoader      : corresponding class loader to load the class
     * @param serviceGroupName : name of the service group
     * @throws AxisFault
     */
    public static AxisServiceGroup buildServiceGroup(InputStream servicesxml,
                                                     ClassLoader classLoader,
                                                     String serviceGroupName,
                                                     ConfigurationContext configCtx,
                                                     ArchiveReader archiveReader,
                                                     HashMap wsdlServices) throws AxisFault {
        ArchiveFileData currentArchiveFile = new ArchiveFileData(
                DeploymentConstants.TYPE_SERVICE, "", false);
        currentArchiveFile.setClassLoader(classLoader);
        AxisServiceGroup serviceGroup = new AxisServiceGroup();
        serviceGroup.setServiceGroupClassLoader(classLoader);
        serviceGroup.setServiceGroupName(serviceGroupName);
        AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
        try {
            ArrayList serviceList = archiveReader.buildServiceGroup(servicesxml,
                    currentArchiveFile, serviceGroup,
                    wsdlServices, configCtx);
            fillServiceGroup(serviceGroup, serviceList, null, axisConfig);
            return serviceGroup;
        } catch (XMLStreamException e) {
            throw new AxisFault(e);
        }
    }

    public File getServicesDir() {
        return servicesDir;
    }

    public File getModulesDir() {
        return modulesDir;
    }
}
