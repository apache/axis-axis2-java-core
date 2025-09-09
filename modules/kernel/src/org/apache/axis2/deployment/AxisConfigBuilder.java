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

import org.apache.axiom.attachments.lifecycle.LifecycleManager;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.RolePlayer;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.transaction.TransactionConfiguration;
import org.apache.axis2.builder.ApplicationXMLBuilder;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.MIMEBuilder;
import org.apache.axis2.builder.MTOMBuilder;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.builder.XFormURLEncodedBuilder;
import org.apache.axis2.dataretrieval.DRConstants;
import org.apache.axis2.deployment.util.PhasesInfo;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisObserver;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.phaseresolver.PhaseException;
import org.apache.axis2.kernel.MessageFormatter;
import org.apache.axis2.kernel.TransportListener;
import org.apache.axis2.kernel.TransportSender;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.ThreadContextMigrator;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Constructor;

public class AxisConfigBuilder extends DescriptionBuilder {

    protected static final Log log = LogFactory.getLog(AxisConfigBuilder.class);
    private DeploymentEngine deploymentEngine;

    public AxisConfigBuilder(InputStream serviceInputStream,
                             AxisConfiguration axisConfiguration,
                             DeploymentEngine deploymentEngine) {
        super(serviceInputStream, axisConfiguration);
        this.deploymentEngine = deploymentEngine;
    }


    public AxisConfigBuilder(AxisConfiguration axisConfiguration) {
        this.axisConfig = axisConfiguration;
    }

    public void populateConfig() throws DeploymentException {
        try {
            OMElement config_element = buildOM();

            if (!TAG_AXISCONFIG.equals(config_element.getLocalName())) {
                throw new DeploymentException(Messages.getMessage("badelementfound", TAG_AXISCONFIG,
                                                                  config_element.getLocalName()));
            }
            // processing Parameters
            // Processing service level parameters
            Iterator<OMElement> itr = config_element.getChildrenWithName(new QName(TAG_PARAMETER));

            processParameters(itr, axisConfig, axisConfig);

            // process MessageReceiver
            OMElement messageReceiver =
                    config_element.getFirstChildWithName(new QName(TAG_MESSAGE_RECEIVERS));
            if (messageReceiver != null) {
                HashMap mrs = processMessageReceivers(messageReceiver);
                Iterator keys = mrs.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    axisConfig.addMessageReceiver(key, (MessageReceiver) mrs.get(key));
                }
            }
            // Process Module refs
            Iterator<OMElement> moduleitr =
                    config_element.getChildrenWithName(new QName(DeploymentConstants.TAG_MODULE));

            processModuleRefs(moduleitr, axisConfig);

            // Processing Transport Senders
            Iterator<OMElement> trs_senders =
                    config_element.getChildrenWithName(new QName(TAG_TRANSPORT_SENDER));

            processTransportSenders(trs_senders);

            // Processing Transport Receivers
            Iterator<OMElement> trs_Reivers =
                    config_element.getChildrenWithName(new QName(TAG_TRANSPORT_RECEIVER));

            processTransportReceivers(trs_Reivers);


            // Process ThreadContextMigrators
            OMElement threadContextMigrators =
                    config_element.getFirstChildWithName(new QName(TAG_THREAD_CONTEXT_MIGRATORS));
            processThreadContextMigrators(axisConfig, threadContextMigrators);

            // Process Observers
            Iterator<OMElement> obs_ittr = config_element.getChildrenWithName(new QName(TAG_LISTENER));

            processObservers(obs_ittr);

            // Processing Phase orders
            Iterator<OMElement> phaseorders = config_element.getChildrenWithName(new QName(TAG_PHASE_ORDER));

            processPhaseOrders(phaseorders);

            Iterator<OMElement> moduleConfigs =
                    config_element.getChildrenWithName(new QName(TAG_MODULE_CONFIG));

            processModuleConfig(moduleConfigs, axisConfig, axisConfig);

            // processing <wsp:Policy> .. </..> elements
            Iterator<OMElement> policyElements = PolicyUtil.getPolicyChildren(config_element);

            if (policyElements != null && policyElements.hasNext()) {
                processPolicyElements(policyElements,
                                      axisConfig.getPolicySubject());
            }

            // processing <wsp:PolicyReference> .. </..> elements
            Iterator<OMElement> policyRefElements = PolicyUtil.getPolicyRefChildren(config_element);

            if (policyRefElements != null && policyRefElements.hasNext()) {
                processPolicyRefElements(policyElements,
                                         axisConfig.getPolicySubject());
            }

            //to process default module versions
            OMElement defaultModuleVerionElement = config_element.getFirstChildWithName(new QName(
                    TAG_DEFAULT_MODULE_VERSION));
            if (defaultModuleVerionElement != null) {
                processDefaultModuleVersions(defaultModuleVerionElement);
            }


            //Add jta transaction  configuration
            OMElement transactionElement = config_element.getFirstChildWithName(new QName(TAG_TRANSACTION));
            if (transactionElement != null) {
                ParameterInclude transactionParameters = new ParameterIncludeImpl();
                Iterator<OMElement> parameters = transactionElement.getChildrenWithName(new QName(TAG_PARAMETER));
                processParameters(parameters, transactionParameters, null);

                TransactionConfiguration txcfg = null;
                OMAttribute txConfigurationClassAttribute =
                        transactionElement.getAttribute(new QName(TAG_TRANSACTION_CONFIGURATION_CLASS));

                if (txConfigurationClassAttribute != null){
                    String txConfigurationClassName = txConfigurationClassAttribute.getAttributeValue();
                    try {
                        Class txConfigurationClass = Class.forName(txConfigurationClassName);
                        Constructor constructor = txConfigurationClass.getConstructor(new Class[]{ParameterInclude.class});
                        txcfg = (TransactionConfiguration) constructor.newInstance(new Object[]{transactionParameters});
                    } catch (Exception e) {
                        throw new DeploymentException("Can not found or instantiate the class " + txConfigurationClassName, e);
                    }
                } else {
                    txcfg = new TransactionConfiguration(transactionParameters);
                }

                OMAttribute timeoutAttribute = transactionElement.getAttribute(new QName(TAG_TIMEOUT));
                if(timeoutAttribute != null) {
                    txcfg.setTransactionTimeout(Integer.parseInt(timeoutAttribute.getAttributeValue()));
                }

                axisConfig.setTransactionConfig(txcfg);
            }

                    
            /*
            * Add Axis2 default builders if they are not overidden by the config
            */
            axisConfig.addMessageBuilder("multipart/related", new MIMEBuilder());
            axisConfig.addMessageBuilder("application/soap+xml", new SOAPBuilder());
            axisConfig.addMessageBuilder("text/xml", new SOAPBuilder());
            axisConfig.addMessageBuilder("application/xop+xml", new MTOMBuilder());
            axisConfig.addMessageBuilder("application/xml", new ApplicationXMLBuilder());
            axisConfig.addMessageBuilder("application/x-www-form-urlencoded",
                                         new XFormURLEncodedBuilder());
            // process MessageBuilders
            OMElement messageBuildersElement =
                    config_element.getFirstChildWithName(new QName(TAG_MESSAGE_BUILDERS));
            if (messageBuildersElement != null) {
                HashMap builderSelector = processMessageBuilders(messageBuildersElement);
                Iterator keys = builderSelector.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    axisConfig.addMessageBuilder(key, (Builder) builderSelector.get(key));
                }
            }
            

            //process dataLocator configuration
            OMElement dataLocatorElement =
                    config_element
                            .getFirstChildWithName(new QName(DRConstants.DATA_LOCATOR_ELEMENT));

            if (dataLocatorElement != null) {
                processDataLocatorConfig(dataLocatorElement);
            }
            
            // process roleplayer configuration
            OMElement rolePlayerElement =
                    config_element
                            .getFirstChildWithName(new QName(Constants.SOAP_ROLE_CONFIGURATION_ELEMENT));

            if (rolePlayerElement != null) {
                processSOAPRoleConfig(axisConfig, rolePlayerElement);
            }

            // process MessageFormatters
            OMElement messageFormattersElement =
                    config_element.getFirstChildWithName(new QName(TAG_MESSAGE_FORMATTERS));
            if (messageFormattersElement != null) {
                HashMap messageFormatters = processMessageFormatters(messageFormattersElement);
                Iterator keys = messageFormatters.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    axisConfig.addMessageFormatter(key,
                                                   (MessageFormatter) messageFormatters.get(key));
                }
            }
            //Processing deployers.
            Iterator<OMElement> deployerItr = config_element.getChildrenWithName(new QName(DEPLOYER));
            if (deployerItr != null) {
                processDeployers(deployerItr);
            }

            //process Attachments Lifecycle manager configuration
            OMElement attachmentsLifecycleManagerElement =
                    config_element
                            .getFirstChildWithName(new QName(ATTACHMENTS_LIFECYCLE_MANAGER));

            if (attachmentsLifecycleManagerElement != null) {
                processAttachmentsLifecycleManager(axisConfig, attachmentsLifecycleManagerElement);
            }
        } catch (XMLStreamException e) {
            throw new DeploymentException(e);
        }
    }


    private void processThreadContextMigrators(AxisConfiguration axisConfig, OMElement targetResolvers) {
        if (targetResolvers != null) {
            Iterator<OMElement> iterator = targetResolvers.getChildrenWithName(new QName(TAG_THREAD_CONTEXT_MIGRATOR));
            while (iterator.hasNext()) {
                OMElement threadContextMigrator = iterator.next();
                OMAttribute listIdAttribute =
                    threadContextMigrator.getAttribute(new QName(TAG_LIST_ID));
                String listId = listIdAttribute.getAttributeValue();
                OMAttribute classNameAttribute =
                    threadContextMigrator.getAttribute(new QName(TAG_CLASS_NAME));
                String className = classNameAttribute.getAttributeValue();
                try {
                    Class clazz = Loader.loadClass(className);
                    ThreadContextMigrator migrator = (ThreadContextMigrator) clazz.newInstance();
                    ThreadContextMigratorUtil.addThreadContextMigrator(axisConfig, listId, migrator);
                } catch (UnsupportedClassVersionError e){
                    log.info("Disabled - " + className + " - " + e.getMessage());
                } catch (Exception e) {
                    if (log.isTraceEnabled()) {
                        log.trace(
                                "processThreadContextMigrators: Exception thrown initialising ThreadContextMigrator: " +
                                        e.getMessage());
                    }
                }
            }
        }
    }

    private void processAttachmentsLifecycleManager(AxisConfiguration axisConfig, OMElement element) {
        String className = element.getAttributeValue(new QName(TAG_CLASS_NAME));
        try {
            Class classInstance = Loader.loadClass(className);
            LifecycleManager manager = (LifecycleManager) classInstance.newInstance();
            axisConfig.addParameter(DeploymentConstants.ATTACHMENTS_LIFECYCLE_MANAGER, manager);
        } catch (Exception e) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "processAttachmentsLifecycleManager: Exception thrown initialising LifecycleManager: " +
                                e.getMessage());
            }
        }
    }

    private void processSOAPRoleConfig(AxisConfiguration axisConfig, OMElement soaproleconfigElement) {
    	if (soaproleconfigElement != null) {
    		final boolean isUltimateReceiever = JavaUtils.isTrue(soaproleconfigElement.getAttributeValue(new QName(Constants.SOAP_ROLE_IS_ULTIMATE_RECEIVER_ATTRIBUTE)), true);
    		ArrayList roles = new ArrayList();
    		Iterator<OMElement> iterator = soaproleconfigElement.getChildrenWithName(new QName(Constants.SOAP_ROLE_ELEMENT));
    		while (iterator.hasNext()) {
    			OMElement roleElement = iterator.next();
    			roles.add(roleElement.getText());
    		}
    		final List unmodifiableRoles = Collections.unmodifiableList(roles);
    		try{
    			RolePlayer rolePlayer = new RolePlayer(){
    				public List getRoles() {
    					return unmodifiableRoles;
    				}
    				public boolean isUltimateDestination() {
    					return isUltimateReceiever;
    				}
    			};
    			axisConfig.addParameter("rolePlayer", rolePlayer);
    		} catch (AxisFault e) {
    			if (log.isTraceEnabled()) {
    				log.trace(
    						"processTargetResolvers: Exception thrown initialising TargetResolver: " +
    						e.getMessage());
    			}
    		}
    	}
    }
    
    private void processDeployers(Iterator deployerItr) {
        Map<String, Map<String, Deployer>> deployers = new HashMap<String, Map<String, Deployer>>();
        while (deployerItr.hasNext()) {
            OMElement element = (OMElement) deployerItr.next();
            String directory = element.getAttributeValue(new QName(DIRECTORY));
            if (directory == null) {
                log.error("Deployer missing 'directory' attribute : " + element.toString());
                continue;
            }

            String extension = element.getAttributeValue(new QName(EXTENSION));
            if (extension == null) {
                log.error("Deployer missing 'extension' attribute : " + element.toString());
                continue;
            }

            // A leading dot is redundant, so strip it.  So we allow either ".foo" or "foo", either
            // of which will result in extension="foo"
            if (extension.charAt(0) == '.') extension = extension.substring(1);

            String deployerClassName = element.getAttributeValue(new QName(TAG_CLASS_NAME));
            Deployer deployer;
            try {
                Class deployerClass = Loader.loadClass(deployerClassName);
                deployer = (Deployer) deployerClass.newInstance();
            } catch (UnsupportedClassVersionError ex) {
                log.info("Disabled - " + deployerClassName + " - " + ex.getMessage());
                continue;
            } catch (Throwable e) {
                log.warn("Unable to instantiate deployer " + deployerClassName +
                        "; see debug logs for more details");
                log.debug(e.getMessage(), e);
                continue;
            }
            deployer.setDirectory(directory);
            deployer.setExtension(extension);
            
            for (Iterator<OMElement> itr = element.getChildrenWithName(new QName(
                    TAG_SERVICE_BUILDER_EXTENSION)); itr.hasNext();) {
                OMElement serviceBuilderEle = itr.next();
                String serviceBuilderClass = serviceBuilderEle.getAttributeValue(new QName(
                        TAG_CLASS_NAME));
                String serviceBuilderName = serviceBuilderEle.getAttributeValue(new QName(
                        ATTRIBUTE_CLASS));
                if (serviceBuilderClass != null && serviceBuilderName != null) {
                    ServiceBuilderExtension builderExtension;
                    try {
                        Class<ServiceBuilderExtension> builderExtensionClass = Loader
                                .loadClass(serviceBuilderClass);
                        builderExtension = builderExtensionClass.newInstance();
                        builderExtension.setDirectory(directory);
                    } catch (UnsupportedClassVersionError ex) {
                        log.info("Disabled - " + deployerClassName + " - " + ex.getMessage());
                        continue;
                    } catch (Throwable e) {
                        log.warn("Unable to instantiate serviceBuilderClass " + serviceBuilderClass + " , error: " + e.getMessage(), e);
                        log.warn("This error could mean the server was able to instantiate the deployer, however was unable to load an extension for it. This class will continue processing the next extension...");
                        continue;
                    }
                    if (deployer instanceof AbstractDeployer) {
                        ((AbstractDeployer) deployer).addServiceBuilderExtensions(builderExtension);
                    }
                }  
            }     
            
            Map<String, Deployer> extensionMap = deployers.get(directory);
            if (extensionMap == null) {
                extensionMap = new HashMap<String, Deployer>();
                deployers.put(directory, extensionMap);
            }
            extensionMap.put(extension, deployer);
        }
        if (deploymentEngine != null) {
            deploymentEngine.setDeployers(deployers);
        }
    }

    protected void processModuleConfig(Iterator moduleConfigs, ParameterInclude parent,
                                       AxisConfiguration config)
            throws DeploymentException {
        while (moduleConfigs.hasNext()) {
            OMElement moduleConfig = (OMElement) moduleConfigs.next();
            OMAttribute moduleName_att = moduleConfig.getAttribute(new QName(ATTRIBUTE_NAME));

            if (moduleName_att == null) {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE_CONFIG));
            } else {
                String module = moduleName_att.getAttributeValue();
                ModuleConfiguration moduleConfiguration =
                        new ModuleConfiguration(module, parent);
                Iterator<OMElement> parameters = moduleConfig.getChildrenWithName(new QName(TAG_PARAMETER));

                processParameters(parameters, moduleConfiguration, parent);
                config.addModuleConfig(moduleConfiguration);
            }
        }
    }

    /**
     * Update the list of modules that is required to be engaged globally.
     */
    protected void processModuleRefs(Iterator moduleRefs, AxisConfiguration config) {
        while (moduleRefs.hasNext()) {
            OMElement moduleref = (OMElement) moduleRefs.next();
            OMAttribute moduleRefAttribute = moduleref.getAttribute(new QName(TAG_REFERENCE));
            String refName = moduleRefAttribute.getAttributeValue();
            axisConfig.addGlobalModuleRef(refName);
        }
    }

    /**
     * Processes AxisObservers.
     *
     * @param oservers
     */
    private void processObservers(Iterator oservers) {
        while (oservers.hasNext()) {
            try {
                OMElement observerelement = (OMElement) oservers.next();
                AxisObserver observer;
                OMAttribute trsClas = observerelement.getAttribute(new QName(TAG_CLASS_NAME));
                if (trsClas == null) {
                    log.info(Messages.getMessage(DeploymentErrorMsgs.OBSERVER_ERROR));
                    return;
                }
                final String clasName = trsClas.getAttributeValue();

                Class observerclass;
                try {
                    observerclass = (Class) org.apache.axis2.java.security.AccessController
                            .doPrivileged(new PrivilegedExceptionAction() {
                                public Object run() throws ClassNotFoundException {
                                    return Loader.loadClass(clasName);
                                }
                            });
                } catch (PrivilegedActionException e) {
                    throw (ClassNotFoundException) e.getException();
                }
                observer = (AxisObserver) observerclass.newInstance();
                // processing Parameters
                // Processing service level parameters
                Iterator<OMElement> itr = observerelement.getChildrenWithName(new QName(TAG_PARAMETER));
                processParameters(itr, observer, axisConfig);
                // initialization
                try {
                    observer.init(axisConfig);
                } catch (Throwable e) {
                    //Observer init may throw runtime exception , but we can stil
                    // start Axis2
                    log.info(e.getMessage());
                }
                axisConfig.addObservers(observer);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        }
    }

    private ArrayList processPhaseList(OMElement phaseOrders) throws DeploymentException {
        ArrayList phaselist = new ArrayList();
        Iterator<OMElement> phases = phaseOrders.getChildrenWithName(new QName(TAG_PHASE));

        while (phases.hasNext()) {
            OMElement phaseelement = phases.next();
            String phaseName =
                    phaseelement.getAttribute(new QName(ATTRIBUTE_NAME)).getAttributeValue();
            String phaseClass = phaseelement.getAttributeValue(new QName(TAG_CLASS_NAME));
            Phase phase;

            try {
                phase = getPhase(phaseClass);
            } catch (Exception e) {
                throw new DeploymentException(
                        Messages.getMessage("phaseclassnotfound", phaseClass, e.getMessage()));
            }

            phase.setName(phaseName);

            Iterator<OMElement> handlers = phaseelement.getChildrenWithName(new QName(TAG_HANDLER));

            while (handlers.hasNext()) {
                OMElement omElement = handlers.next();
                HandlerDescription handler = processHandler(omElement, axisConfig, phaseName);

                handler.getRules().setPhaseName(phaseName);
                try {
                    if (Utils.loadHandler(axisConfig.getSystemClassLoader(), handler)) {
                        try {
                            phase.addHandler(handler);
                        } catch (PhaseException e) {
                            throw new DeploymentException(e);
                        }
                    }
                } catch (UnsupportedClassVersionError e) {
                    log.info("Disabled - " + handler + " - " + e.getMessage());
                }
            }

            phaselist.add(phase);
        }

        return phaselist;
    }

    /**
     * Processes all the phase orders which are defined in axis2.xml.
     *
     * @param phaserders
     */
    private void processPhaseOrders(Iterator phaserders) throws DeploymentException {
        PhasesInfo info = axisConfig.getPhasesInfo();

        while (phaserders.hasNext()) {
            OMElement phaseOrders = (OMElement) phaserders.next();
            String flowType = phaseOrders.getAttribute(new QName(TAG_TYPE)).getAttributeValue();

            if (TAG_FLOW_IN.equals(flowType)) {
                info.setINPhases(processPhaseList(phaseOrders));
            } else if (TAG_FLOW_IN_FAULT.equals(flowType)) {
                info.setIN_FaultPhases(processPhaseList(phaseOrders));
            } else if (TAG_FLOW_OUT.equals(flowType)) {
                info.setOUTPhases(processPhaseList(phaseOrders));
            } else if (TAG_FLOW_OUT_FAULT.equals(flowType)) {
                info.setOUT_FaultPhases(processPhaseList(phaseOrders));
            }
        }
    }

    private void processDefaultModuleVersions(OMElement defaultVersions)
            throws DeploymentException {
        Iterator<OMElement> moduleVersions = defaultVersions.getChildrenWithName(new QName(TAG_MODULE));
        while (moduleVersions.hasNext()) {
            OMElement omElement = moduleVersions.next();
            String name = omElement.getAttributeValue(new QName(ATTRIBUTE_NAME));
            if (name == null) {
                throw new DeploymentException(Messages.getMessage("modulenamecannotbenull"));
            }
            String version =
                    omElement.getAttributeValue(new QName(ATTRIBUTE_DEFAULT_VERSION));
            if (version == null) {
                throw new DeploymentException(Messages.getMessage("moduleversioncannotbenull"));
            }
            axisConfig.addDefaultModuleVersion(name, version);
        }
    }

    public ArrayList  processTransportReceivers(Iterator trs_senders) throws DeploymentException {
        ArrayList transportReceivers = new ArrayList();
        while (trs_senders.hasNext()) {
            TransportInDescription transportIN;
            OMElement transport = (OMElement) trs_senders.next();
            // getting transport Name
            OMAttribute trsName = transport.getAttribute(new QName(ATTRIBUTE_NAME));
            if (trsName != null) {
                String name = trsName.getAttributeValue();
                transportIN = new TransportInDescription(name);
                // transport impl class
                OMAttribute trsClas = transport.getAttribute(new QName(TAG_CLASS_NAME));
                if (trsClas != null) {
                    try {
                        String clasName = trsClas.getAttributeValue();
                        Class receiverClass;
                        receiverClass = Loader.loadClass(clasName);

                        TransportListener receiver =
                                (TransportListener) receiverClass.newInstance();
                        transportIN.setReceiver(receiver);
                    } catch (NoClassDefFoundError e) {
                        if(deploymentEngine != null){
                            throw new DeploymentException(e);
                        } else {
                            // Called from createDefaultConfigurationContext in ConfigurationContextFactory
                            // Please don't throw an exception.
                            log.debug(Messages.getMessage("classnotfound", trsClas.getAttributeValue()));
                        }
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException(e);
                    } catch (IllegalAccessException e) {
                        throw new DeploymentException(e);
                    } catch (InstantiationException e) {
                        throw new DeploymentException(e);
                    }
                }
                try {
                    Iterator<OMElement> itr = transport.getChildrenWithName(new QName(TAG_PARAMETER));
                    processParameters(itr, transportIN, axisConfig);
                    // adding to axis2 config
                    axisConfig.addTransportIn(transportIN);
                    transportReceivers.add(transportIN);
                } catch (AxisFault axisFault) {
                    throw new DeploymentException(axisFault);
                }
            }
        }
        return transportReceivers;
    }

    public void processTransportSenders(Iterator trs_senders) throws DeploymentException {
        while (trs_senders.hasNext()) {
            TransportOutDescription transportout;
            OMElement transport = (OMElement) trs_senders.next();

            // getting transport Name
            OMAttribute trsName = transport.getAttribute(new QName(ATTRIBUTE_NAME));

            if (trsName != null) {
                String name = trsName.getAttributeValue();

                transportout = new TransportOutDescription(name);

                // transport impl class
                OMAttribute trsClas = transport.getAttribute(new QName(TAG_CLASS_NAME));

                if (trsClas == null) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.TRANSPORT_SENDER_ERROR, name));
                }

                String clasName = trsClas.getAttributeValue();
                Class sender;

                try {
                    sender = Loader.loadClass(clasName);

                    TransportSender transportSender = (TransportSender) sender.newInstance();

                    transportout.setSender(transportSender);

                    // process Parameters
                    // processing Parameters
                    // Processing service level parameters
                    Iterator<OMElement> itr = transport.getChildrenWithName(new QName(TAG_PARAMETER));

                    processParameters(itr, transportout, axisConfig);
                    // adding to axis2 config
                    axisConfig.addTransportOut(transportout);
                } catch (NoClassDefFoundError e) {
                    if(deploymentEngine != null){
                        log.debug(Messages.getMessage("errorinloadingts", clasName), e);
                        throw new DeploymentException(e);
                    } else {
                        // Called from createDefaultConfigurationContext in ConfigurationContextFactory
                        // Please don't throw an exception.
                        log.debug(Messages.getMessage("classnotfound", trsClas.getAttributeValue()));
                    }
                } catch (ClassNotFoundException e) {
                    log.debug(Messages.getMessage("errorinloadingts", clasName), e);
                    throw new DeploymentException(e);
                } catch (IllegalAccessException e) {
                    log.debug(Messages.getMessage("errorinloadingts", clasName), e);
                    throw new DeploymentException(e);
                } catch (InstantiationException e) {
                    log.debug(Messages.getMessage("errorinloadingts", clasName), e);
                    throw new DeploymentException(e);
                } catch (AxisFault axisFault) {
                    log.debug(Messages.getMessage("errorinloadingts", clasName), axisFault);
                    throw new DeploymentException(axisFault);
                }
            }
        }
    }

    /*
     * process data locator configuration for data retrieval.
     */
    private void processDataLocatorConfig(OMElement dataLocatorElement) {
        OMAttribute serviceOverallDataLocatorclass = dataLocatorElement
                .getAttribute(new QName(DRConstants.CLASS_ATTRIBUTE));
        if (serviceOverallDataLocatorclass != null) {
            String className = serviceOverallDataLocatorclass
                    .getAttributeValue();
            axisConfig.addDataLocatorClassNames(DRConstants.GLOBAL_LEVEL,
                                                className);
        }
        Iterator<OMElement> iterator = dataLocatorElement.getChildrenWithName(new QName(
                DRConstants.DIALECT_LOCATOR_ELEMENT));

        while (iterator.hasNext()) {
            OMElement locatorElement = iterator.next();
            OMAttribute dialect = locatorElement.getAttribute(new QName(
                    DRConstants.DIALECT_ATTRIBUTE));
            OMAttribute dialectclass = locatorElement.getAttribute(new QName(
                    DRConstants.CLASS_ATTRIBUTE));
            axisConfig.addDataLocatorClassNames(dialect.getAttributeValue(),
                                                dialectclass.getAttributeValue());

        }
    }

    protected HashMap processMessageFormatters(OMElement messageFormattersElement)
            throws DeploymentException {
        try {
            return super.processMessageFormatters(messageFormattersElement);
        } catch (NoClassDefFoundError e) {
            if (deploymentEngine != null) {
                throw new DeploymentException(e);
            } else {
                // Called from createDefaultConfigurationContext in ConfigurationContextFactory
                // Please don't throw an exception.
                return new HashMap();
            }
        }
    }

    protected HashMap processMessageBuilders(OMElement messageBuildersElement)
            throws DeploymentException {
        try {
            return super.processMessageBuilders(messageBuildersElement);
        } catch (NoClassDefFoundError e) {
            if (deploymentEngine != null) {
                throw new DeploymentException(e);
            } else {
                // Called from createDefaultConfigurationContext in ConfigurationContextFactory
                // Please don't throw an exception.
                return new HashMap();
            }
        }
    }

    private Phase getPhase(String className)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if (className == null) {
            return new Phase();
        }
        Class phaseClass = Loader.loadClass(axisConfig.getSystemClassLoader(), className);
        return (Phase) phaseClass.newInstance();
    }
}
