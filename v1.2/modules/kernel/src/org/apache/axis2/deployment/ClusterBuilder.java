/**
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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.cluster.configuration.ConfigurationManager;
import org.apache.axis2.cluster.configuration.ConfigurationManagerListener;
import org.apache.axis2.cluster.context.ContextManager;
import org.apache.axis2.cluster.context.ContextManagerListener;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;


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
    public void buildCluster(OMElement clusterElement)
            throws DeploymentException {

        OMAttribute classNameAttr = clusterElement.getAttribute(new QName(TAG_CLASS_NAME));
        if (classNameAttr==null) {
            throw new DeploymentException(Messages.getMessage("classAttributeNotFound", TAG_CLUSTER));
        }
        
        String className = classNameAttr.getAttributeValue();
        ClusterManager clusterManager;
        try {
            Class clazz = Class.forName(className);
            clusterManager = (ClusterManager) clazz.newInstance();
            
            //loading the parameters.
            Iterator params = clusterElement.getChildrenWithName(new QName(TAG_PARAMETER));
            processParameters(params, clusterManager,null );
            
            //loading the ConfigurationManager
            OMElement configurationManagerElement = clusterElement.getFirstChildWithName(
            			new QName (TAG_CONFIGURATION_MANAGER));
            if (configurationManagerElement != null) {
                classNameAttr = configurationManagerElement.getAttribute(new QName(TAG_CLASS_NAME));
                if (classNameAttr==null) {
                    throw new DeploymentException(Messages.getMessage("classAttributeNotFound", TAG_CONFIGURATION_MANAGER));
                }
                
                className = classNameAttr.getAttributeValue();
				clazz = Class.forName(className);
				
				ConfigurationManager configurationManager = (ConfigurationManager) clazz
						.newInstance();
				clusterManager.setConfigurationManager(configurationManager);

				OMElement listenersElement = configurationManagerElement
						.getFirstChildWithName(new QName(TAG_LISTENERS));
				if (listenersElement != null) {
					Iterator listenerElemIter = listenersElement.getChildrenWithName(new QName(
							TAG_LISTENER));
					while (listenerElemIter.hasNext()) {
						OMElement listenerElement = (OMElement) listenerElemIter.next();
				        classNameAttr = listenerElement.getAttribute(new QName(TAG_CLASS_NAME));
				        if (classNameAttr==null) {
				            throw new DeploymentException(Messages.getMessage("classAttributeNotFound", TAG_LISTENER));
				        }
				        
				        className = classNameAttr.getAttributeValue();
						clazz = Class.forName(className);
						ConfigurationManagerListener listener = (ConfigurationManagerListener) clazz
								.newInstance();
						listener.setConfigurationContext(configCtx);
						configurationManager.addConfigurationManagerListener(listener);
					}
				}

				//updating the ConfigurationManager with the new ConfigurationContext
				configurationManager.setConfigurationContext(configCtx);
			}

			
            // loading the ContextManager
            OMElement contextManagerElement = clusterElement.getFirstChildWithName(
            			new QName (TAG_CONTEXT_MANAGER));
            if (contextManagerElement != null) {
                classNameAttr = contextManagerElement.getAttribute(new QName(TAG_CLASS_NAME));
                if (classNameAttr==null) {
                    throw new DeploymentException(Messages.getMessage("classAttributeNotFound", TAG_CONTEXT_MANAGER));
                }
                
                className = classNameAttr.getAttributeValue();

				clazz = Class.forName(className);
				ContextManager contextManager = (ContextManager) clazz.newInstance();
				clusterManager.setContextManager(contextManager);

				OMElement listenersElement = contextManagerElement.getFirstChildWithName(new QName(
						TAG_LISTENERS));
				if (listenersElement != null) {
					Iterator listenerElemIter = listenersElement.getChildrenWithName(new QName(
							TAG_LISTENER));
					while (listenerElemIter.hasNext()) {
						OMElement listenerElement = (OMElement) listenerElemIter.next();
				        classNameAttr = listenerElement.getAttribute(new QName(TAG_CLASS_NAME));
				        if (classNameAttr==null) {
				            throw new DeploymentException(Messages.getMessage("classAttributeNotFound", TAG_LISTENER));
				        }
				        
				        className = classNameAttr.getAttributeValue();
						clazz = Class.forName(className);
						System.out.println(className);
						ContextManagerListener listener = (ContextManagerListener) clazz.newInstance();
						contextManager.addContextManagerListener(listener);
					}
				}
			}
            
            axisConfig.setClusterManager(clusterManager);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(Messages.getMessage("clusterImplNotFound"));
        } catch (InstantiationException e) {
        	e.printStackTrace();
            throw new DeploymentException(Messages.getMessage("cannotLoadClusterImpl"));
        } catch (IllegalAccessException e) {
            throw new DeploymentException(e);
        }
    }
}
