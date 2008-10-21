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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.clustering.ClusterManager;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.LoadBalanceEventHandler;
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.ConfigurationManagerListener;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.ContextManagerListener;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Builds the cluster configuration from the axis2.xml file
 */
public class ClusterBuilder extends DescriptionBuilder {

    private static final Log log = LogFactory.getLog(ClusterBuilder.class);

    public ClusterBuilder(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    /**
     * Build the cluster configuration
     *
     * @param clusterElement Cluster element
     * @throws DeploymentException If an error occurs while building the cluster configuration
     */
    public void buildCluster(OMElement clusterElement) throws DeploymentException {

        if (!isEnabled(clusterElement)) {
            log.info("Clustering has been disabled");
            return;
        }
        log.info("Clustering has been enabled");

        OMAttribute classNameAttr = clusterElement.getAttribute(new QName(TAG_CLASS_NAME));
        if (classNameAttr == null) {
            throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                              TAG_CLUSTER));
        }

        String className = classNameAttr.getAttributeValue();
        ClusterManager clusterManager;
        try {
            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }
            clusterManager = (ClusterManager) clazz.newInstance();

            clusterManager.setConfigurationContext(configCtx);

            //loading the parameters.
            processParameters(clusterElement.getChildrenWithName(new QName(TAG_PARAMETER)),
                              clusterManager,
                              null);

            // loading the application domains
            loadApplicationDomains(clusterManager, clusterElement);

            // loading the members
            loadWellKnownMembers(clusterManager, clusterElement);

            //loading the ConfigurationManager
            loadConfigManager(clusterElement, clusterManager);

            // loading the ContextManager
            loadContextManager(clusterElement, clusterManager);

            axisConfig.setClusterManager(clusterManager);
        } catch (InstantiationException e) {
            throw new DeploymentException(Messages.getMessage("cannotLoadClusterImpl"));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(e);
        }
    }

    private boolean isEnabled(OMElement element) {
        boolean enabled = true;
        OMAttribute enableAttr = element.getAttribute(new QName("enable"));
        if (enableAttr != null) {
            enabled = Boolean.parseBoolean(enableAttr.getAttributeValue().trim());
        }
        return enabled;
    }

    private void loadApplicationDomains(ClusterManager clusterManager,
                                        OMElement clusterElement) throws DeploymentException {
        OMElement lbEle = clusterElement.getFirstChildWithName(new QName("loadBalancer"));
        if (lbEle != null) {
            if (isEnabled(lbEle)) {
                log.info("Running in load balance mode");
            } else {
                log.info("Running in application mode");
                return;
            }

            for (Iterator iter = lbEle.getChildrenWithName(new QName("applicationDomain"));
                 iter.hasNext();) {
                OMElement omElement = (OMElement) iter.next();
                String domainName = omElement.getAttributeValue(new QName("name")).trim();
                String handlerClass = omElement.getAttributeValue(new QName("handler")).trim();
                LoadBalanceEventHandler eventHandler;
                try {
                    eventHandler = (LoadBalanceEventHandler) Class.forName(handlerClass).newInstance();
                } catch (Exception e) {
                    String msg = "Could not instantiate LoadBalanceEventHandler " + handlerClass +
                                 " for domain " + domainName;
                    log.error(msg, e);
                    throw new DeploymentException(msg, e);
                }
                clusterManager.addLoadBalanceEventHandler(eventHandler, domainName);
            }
        }
    }

    private void loadWellKnownMembers(ClusterManager clusterManager, OMElement clusterElement) {
        clusterManager.setMembers(new ArrayList<Member>());
        Parameter membershipSchemeParam = clusterManager.getParameter("membershipScheme");
        if (membershipSchemeParam != null) {
            String membershipScheme = ((String) membershipSchemeParam.getValue()).trim();
            if (membershipScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
                List<Member> members = new ArrayList<Member>();
                OMElement membersEle =
                        clusterElement.getFirstChildWithName(new QName("members"));
                if (membersEle != null) {
                    for (Iterator iter = membersEle.getChildrenWithLocalName("member"); iter.hasNext();) {
                        OMElement memberEle = (OMElement) iter.next();
                        String hostName =
                                memberEle.getFirstChildWithName(new QName("hostName")).getText().trim();
                        String port =
                                memberEle.getFirstChildWithName(new QName("port")).getText().trim();
                        members.add(new Member(replaceVariables(hostName),
                                               Integer.parseInt(replaceVariables(port))));
                    }
                }
                clusterManager.setMembers(members);
            }
        }
    }

    private String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    private void loadContextManager(OMElement clusterElement,
                                    ClusterManager clusterManager) throws DeploymentException,
                                                                          InstantiationException,
                                                                          IllegalAccessException {
        OMElement contextManagerEle =
                clusterElement.getFirstChildWithName(new QName(TAG_CONTEXT_MANAGER));
        if (contextManagerEle != null) {
            if (!isEnabled(contextManagerEle)) {
                log.info("Clustering context management has been disabled");
                return;
            }
            log.info("Clustering context management has been enabled");

            // Load & set the ContextManager class
            OMAttribute classNameAttr =
                    contextManagerEle.getAttribute(new QName(ATTRIBUTE_CLASS));
            if (classNameAttr == null) {
                throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                                  TAG_CONTEXT_MANAGER));
            }

            String className = classNameAttr.getAttributeValue();

            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }
            ContextManager contextManager = (ContextManager) clazz.newInstance();
            clusterManager.setContextManager(contextManager);

            // Load & set the ContextManagerListener
            OMElement listenerEle =
                    contextManagerEle.getFirstChildWithName(new QName(TAG_LISTENER));
            if (listenerEle != null) {
                classNameAttr = listenerEle.getAttribute(new QName(TAG_CLASS_NAME));
                if (classNameAttr == null) {
                    throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                                      TAG_LISTENER));
                }
                className = classNameAttr.getAttributeValue();
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                      className));
                }
                ContextManagerListener listener = (ContextManagerListener) clazz.newInstance();
                contextManager.setContextManagerListener(listener);
            } else {
                throw new DeploymentException(Messages.getMessage("contextManagerListenerIsNull"));
            }

            //loading the parameters.
            processParameters(contextManagerEle.getChildrenWithName(new QName(TAG_PARAMETER)),
                              contextManager,
                              null);

            // Load the replication patterns to be excluded. We load the following structure.
            /*<replication>
                <defaults>
                    <exclude name="foo.bar.*"/>
                </defaults>
                <context class="org.apache.axis2.context.ConfigurationContext">
                    <exclude name="my.sandesha.*"/>
                </context>
                <context class="org.apache.axis2.context.ServiceGroupContext">
                    <exclude name="my.sandesha.*"/>
                </context>
                <context class="org.apache.axis2.context.ServiceContext">
                    <exclude name="my.sandesha.*"/>
                </context>
            </replication>*/
            OMElement replicationEle =
                    contextManagerEle.getFirstChildWithName(new QName(TAG_REPLICATION));
            if (replicationEle != null) {
                // Process defaults
                OMElement defaultsEle =
                        replicationEle.getFirstChildWithName(new QName(TAG_DEFAULTS));
                if (defaultsEle != null) {
                    List defaults = new ArrayList();
                    for (Iterator iter = defaultsEle.getChildrenWithName(new QName(TAG_EXCLUDE));
                         iter.hasNext();) {
                        OMElement excludeEle = (OMElement) iter.next();
                        OMAttribute nameAtt = excludeEle.getAttribute(new QName(ATTRIBUTE_NAME));
                        defaults.add(nameAtt.getAttributeValue());
                    }
                    contextManager.setReplicationExcludePatterns(TAG_DEFAULTS, defaults);
                }

                // Process specifics
                for (Iterator iter = replicationEle.getChildrenWithName(new QName(TAG_CONTEXT));
                     iter.hasNext();) {
                    OMElement contextEle = (OMElement) iter.next();
                    String ctxClassName =
                            contextEle.getAttribute(new QName(ATTRIBUTE_CLASS)).getAttributeValue();
                    List excludes = new ArrayList();
                    for (Iterator iter2 = contextEle.getChildrenWithName(new QName(TAG_EXCLUDE));
                         iter2.hasNext();) {
                        OMElement excludeEle = (OMElement) iter2.next();
                        OMAttribute nameAtt = excludeEle.getAttribute(new QName(ATTRIBUTE_NAME));
                        excludes.add(nameAtt.getAttributeValue());
                    }
                    contextManager.setReplicationExcludePatterns(ctxClassName, excludes);
                }
            }
        }
    }

    private void loadConfigManager(OMElement clusterElement,
                                   ClusterManager clusterManager) throws DeploymentException,
                                                                         InstantiationException,
                                                                         IllegalAccessException {
        OMElement configManagerEle =
                clusterElement.getFirstChildWithName(new QName(TAG_CONFIGURATION_MANAGER));
        if (configManagerEle != null) {
            if (!isEnabled(configManagerEle)) {
                log.info("Clustering configuration management has been disabled");
                return;
            }
            log.info("Clustering configuration management has been enabled");

            OMAttribute classNameAttr = configManagerEle.getAttribute(new QName(ATTRIBUTE_CLASS));
            if (classNameAttr == null) {
                throw new DeploymentException(Messages.getMessage("classAttributeNotFound",
                                                                  TAG_CONFIGURATION_MANAGER));
            }

            String className = classNameAttr.getAttributeValue();
            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                  className));
            }

            ConfigurationManager configurationManager =
                    (ConfigurationManager) clazz.newInstance();
            clusterManager.setConfigurationManager(configurationManager);

            OMElement listenerEle =
                    configManagerEle.getFirstChildWithName(new QName(TAG_LISTENER));
            if (listenerEle != null) {
                classNameAttr = listenerEle.getAttribute(new QName(TAG_CLASS_NAME));
                if (classNameAttr == null) {
                    throw new DeploymentException(Messages.getMessage("clusterImplNotFound",
                                                                      TAG_LISTENER));
                }

                className = classNameAttr.getAttributeValue();
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage("configurationManagerListenerIsNull"));
                }
                ConfigurationManagerListener listener = (ConfigurationManagerListener) clazz
                        .newInstance();
                listener.setConfigurationContext(configCtx);
                configurationManager.setConfigurationManagerListener(listener);
            } else {
                throw new DeploymentException(Messages.getMessage("configurationManagerListenerIsNull"));
            }

            //updating the ConfigurationManager with the new ConfigurationContext
            configurationManager.setConfigurationContext(configCtx);

            //loading the parameters.
            processParameters(configManagerEle.getChildrenWithName(new QName(TAG_PARAMETER)),
                              configurationManager,
                              null);
        }
    }
}
