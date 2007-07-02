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
import org.apache.axis2.clustering.configuration.ConfigurationManager;
import org.apache.axis2.clustering.configuration.ConfigurationManagerListener;
import org.apache.axis2.clustering.context.ContextManager;
import org.apache.axis2.clustering.context.ContextManagerListener;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Builds a service description from OM
 */
public class ClusterBuilder extends DescriptionBuilder {

//	private static final Log log = LogFactory.getLog(ClusterBuilder.class);

    public ClusterBuilder(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    public ClusterBuilder(InputStream serviceInputStream, AxisConfiguration axisConfig) {
        super(serviceInputStream, axisConfig);
    }

    /**
     * Populates service from corresponding OM.
     */
    public void buildCluster(OMElement clusterElement) throws DeploymentException {

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

    private void loadContextManager(OMElement clusterElement,
                                    ClusterManager clusterManager) throws DeploymentException,
                                                                          InstantiationException,
                                                                          IllegalAccessException {
        OMElement contextManagerEle =
                clusterElement.getFirstChildWithName(new QName(TAG_CONTEXT_MANAGER));
        if (contextManagerEle != null) {

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
