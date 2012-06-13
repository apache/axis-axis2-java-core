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

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.AbstractServiceBuilderExtension;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * The Class JAXWSServiceBuilderExtension is an implementation of
 * org.apache.axis2.deployment.ServiceBuilderExtension interface and facilitate
 * to deploy JAX-WS artifacts through other Deployers.
 * </p>
 * 
 * <p>
 * As an example it is possible to use JAXWSServiceBuilderExtension class to add
 * JAX-WS support for service.xml meta file based service deployment. First,
 * JAXWSServiceBuilderExtension create initial AxisService and ServiceDeployer
 * add further configuration based of provided service.xml meta data file.
 * Annotated call may load from embedded archive (AAR), a exploded directory or
 * from Classpath.
 * </p>
 * 
 * <p>
 * It is expected to define only JAXWSMessageReceiver as MessageReceivers in the
 * service.xml file
 * </p>
 * 
 * <p>
 * Example :
 * </p>
 * 
 * <pre>
 * {@code
 *  <messageReceivers>
 *         <messageReceiver mep="http://www.w3.org/ns/wsdl/in-only" class="org.apache.axis2.jaxws.server.JAXWSMessageReceiver"/>
 *         <messageReceiver mep="http://www.w3.org/ns/wsdl/in-out" class="org.apache.axis2.jaxws.server.JAXWSMessageReceiver"/>
 *     </messageReceivers>
 * 
 * }
 * </pre>
 * 
 * @since 1.7.0
 */
public class JAXWSServiceBuilderExtension extends AbstractServiceBuilderExtension {

    private static final Log log = LogFactory.getLog(JAXWSServiceBuilderExtension.class);

    public Map<String, AxisService> buildAxisServices(DeploymentFileData deploymentFileData)
            throws DeploymentException {

        if (!checkPreconditions(deploymentFileData.getServiceMetaData())) {
            // Should not process further.
            return null;
        }

        ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(deploymentFileData.getClassLoader());
            try {
                JAXWSDeployerSupport deployerSupport = new JAXWSDeployerSupport(
                        getConfigurationContext(), getDirectory());
                List<String> listOfClasses = deployerSupport.getListOfClasses(deploymentFileData);

                /*
                 * if listOfClasses contains no results, let's try to load
                 * implementation class from service.xml.
                 */
                if ((listOfClasses == null || listOfClasses.size() == 0)
                        && deploymentFileData.getServiceMetaData() != null
                        && deploymentFileData.getServiceMetaData() instanceof OMElement) {
                    OMElement serviceMetaData = (OMElement) deploymentFileData.getServiceMetaData();
                    listOfClasses = deployerSupport
                            .getServiceClassNameFromMetaData(serviceMetaData);
                }

                return deployerSupport.deployClasses(deploymentFileData.getFile().toURL(),
                        deploymentFileData.getClassLoader(), listOfClasses);

            } catch (AxisFault e) {
                log.error(e);
            } catch (MalformedURLException e) {
                log.error(e);
            } catch (ClassNotFoundException e) {
                log.error(e);
            } catch (InstantiationException e) {
                log.error(e);
            } catch (IllegalAccessException e) {
                log.error(e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(threadClassLoader);
        }
        return null;
    }

    /**
     * This method check whether all the defined <messageReceiver> are type of
     * JAXWSMessageReceiver. Return true only if all the <messageReceiver>
     * elements satisfy above condition.
     * 
     * @param metaData
     *            the meta data
     * @return true, if successful
     */
    protected boolean checkPreconditions(Object metaData) {
        boolean checkOK = false;
        if (metaData != null && metaData instanceof OMElement) {
            OMElement metaDataEle = (OMElement) metaData;
            if (DeploymentConstants.TAG_SERVICE.equals(metaDataEle.getLocalName())) {
                // if only one <service> present.
                return checkMessageReceivers(metaDataEle);
            } else if (DeploymentConstants.TAG_SERVICE_GROUP.equals(metaDataEle.getLocalName())) {
                // if <serviceGroup> present.               
                for (Iterator<OMElement> serviceItr = metaDataEle
                        .getChildrenWithLocalName(DeploymentConstants.TAG_SERVICE); serviceItr
                        .hasNext();) {
                    // check list of <service> under <serviceGroup>.
                    if (!checkMessageReceivers(serviceItr.next())) {
                        return false;
                    } else {
                        checkOK = true;
                    }
                }
            }
        }
        return checkOK;
    }

    private boolean checkMessageReceivers(OMElement mrsElement) {
        boolean checkOK = false;
        if (mrsElement != null) {
            Iterator<OMElement> eleItr = (mrsElement)
                    .getChildrenWithLocalName(DeploymentConstants.TAG_MESSAGE_RECEIVERS);
            if (eleItr != null) {
                try {
                    for (Iterator<OMElement> mrItr = eleItr.next().getChildrenWithLocalName(
                            DeploymentConstants.TAG_MESSAGE_RECEIVER); mrItr.hasNext();) {
                        OMElement mrEle = mrItr.next();
                        String mrCalssName = mrEle.getAttributeValue(new QName(
                                DeploymentConstants.ATTRIBUTE_CLASS));
                        if (mrCalssName == null
                                || !JAXWSMessageReceiver.class.getName().equals(mrCalssName)) {
                            return false;
                        } else {
                            checkOK = true;
                        }
                    }

                } catch (Exception e) {
                    return false;
                }

            }

        }
        return checkOK;
    }

}
