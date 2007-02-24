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

import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.cluster.ClusterManager;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Builds a service description from OM
 */
public class ClusterBuilder extends DescriptionBuilder {
    private static final Log log = LogFactory.getLog(ClusterBuilder.class);
    private AxisService service;

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
    	
		String className = clusterElement.getAttribute(
				new QName(TAG_CLASS_NAME)).getAttributeValue();
		ClusterManager clusterManager;
		try {
			Class clazz = Class.forName(className);
			clusterManager = (ClusterManager) clazz.newInstance();
			axisConfig.setClusterManager(clusterManager);
			return;
		} catch (ClassNotFoundException e) {
			throw new DeploymentException (Messages.getMessage("clusterImplNotFound"));
		} catch (InstantiationException e) {
			throw new DeploymentException (Messages.getMessage("cannotLoadClusterImpl"));
		} catch (IllegalAccessException e) {
			throw new DeploymentException (e);
		}

	}

}
