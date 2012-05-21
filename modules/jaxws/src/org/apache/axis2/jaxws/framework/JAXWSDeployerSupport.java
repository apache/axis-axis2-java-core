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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMap;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMapManager;
import org.apache.axis2.jaxws.addressing.util.EndpointKey;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.server.JAXWSMessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>The Class JAXWSDeployerSupport act as a helper class for both JAXWSDeployer
 * and JAXWSServiceBuilderExtension.</p>
 * 
 * @since 1.7.0
 */
public class JAXWSDeployerSupport {

    private static final Log log = LogFactory.getLog(JAXWSDeployerSupport.class);

    /** The configuration context. */
    private ConfigurationContext configurationContext;

    /** The directory. */
    private String directory;

    public JAXWSDeployerSupport() {
        this(null, null);
    }

    /**
     * Instantiates a new jAXWS deployer support.
     * 
     * @param configurationContext
     *            the configuration context
     */
    public JAXWSDeployerSupport(ConfigurationContext configurationContext) {
        this(configurationContext, null);
    }

    /**
     * Instantiates a new jAXWS deployer support.
     * 
     * @param configurationContext
     *            the configuration context
     * @param directory
     *            the directory
     */
    public JAXWSDeployerSupport(ConfigurationContext configurationContext, String directory) {
        this.configurationContext = configurationContext;
        this.directory = directory;
    }

    /**
     * Gets the configuration context.
     * 
     * @return the configuration context
     */
    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    /**
     * Sets the configuration context.
     * 
     * @param configurationContext
     *            the new configuration context
     */
    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Gets the directory.
     * 
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the directory.
     * 
     * @param directory
     *            the new directory
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Deploy classes.
     * 
     * @param groupName
     *            the group name
     * @param location
     *            the location
     * @param classLoader
     *            the class loader
     * @param classList
     *            the class list
     * @return the axis service group
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws AxisFault
     *             the axis fault
     */
    protected AxisServiceGroup deployClasses(String groupName, URL location,
            ClassLoader classLoader, List<String> classList) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, AxisFault {

        String serviceHierarchy = Utils.getServiceHierarchy(location.getPath(), this.directory);
        Collection<AxisService> axisServiceList = deployClasses(location, classLoader, classList)
                .values();
        // creating service group by considering the hierarchical path also
        if (axisServiceList.size() > 0) {
            AxisServiceGroup serviceGroup = new AxisServiceGroup();
            for (Iterator<AxisService> axItr = axisServiceList.iterator(); axItr.hasNext();) {
                serviceGroup.addService(axItr.next());
            }
            if (serviceHierarchy != null) {
                serviceGroup.setServiceGroupName(serviceHierarchy + groupName);
            }
            getConfigurationContext().getAxisConfiguration().addServiceGroup(serviceGroup);
            configureAddressing(serviceGroup);
            return serviceGroup;
        }
        return null;
    }

    /**
     * Deploy classes.
     * 
     * @param location
     *            the location
     * @param classLoader
     *            the class loader
     * @param classList
     *            the class list
     * @return the hash map
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws AxisFault
     *             the axis fault
     */
    protected HashMap<String, AxisService> deployClasses(URL location, ClassLoader classLoader,
            List<String> classList) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, AxisFault {
        HashMap<String, AxisService> services = new HashMap<String, AxisService>();
        // Get the hierarchical path of the service

        String serviceHierarchy = Utils.getServiceHierarchy(location.getPath(), getDirectory());
        for (String className : classList) {
            Class<?> pojoClass;
            try {
                pojoClass = Loader.loadClass(classLoader, className);
            } catch (Exception e) {
                continue;
            }
            WebService wsAnnotation = pojoClass.getAnnotation(WebService.class);
            WebServiceProvider wspAnnotation = null;
            if (wsAnnotation == null) {
                wspAnnotation = pojoClass.getAnnotation(WebServiceProvider.class);
            }

            // Create an Axis Service only if the class is not an interface and
            // it has either
            // @WebService annotation or @WebServiceProvider annotation.
            if ((wsAnnotation != null || wspAnnotation != null) && !pojoClass.isInterface()) {
                AxisService axisService;
                axisService = createAxisService(classLoader, className, location);
                if (axisService != null) {
                    log.info("Deploying JAXWS annotated class " + className + " as a service - "
                            + serviceHierarchy + axisService.getName());
                    services.put(axisService.getName(), axisService);
                }
            }
        }
        return services;
    }

    /**
     * Creates the axis service.
     * 
     * @param classLoader
     *            the class loader
     * @param className
     *            the class name
     * @param serviceLocation
     *            the service location
     * @return the axis service
     * @throws ClassNotFoundException
     *             the class not found exception
     * @throws InstantiationException
     *             the instantiation exception
     * @throws IllegalAccessException
     *             the illegal access exception
     * @throws AxisFault
     *             the axis fault
     */
    protected AxisService createAxisService(ClassLoader classLoader, String className,
            URL serviceLocation) throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, AxisFault {
        Class<?> pojoClass = Loader.loadClass(classLoader, className);
        AxisService axisService;
        try {
            axisService = DescriptionFactory
                    .createAxisService(pojoClass, getConfigurationContext());
        } catch (Throwable t) {
            log.info("Exception creating Axis Service : " + t.getCause(), t);
            return null;
        }
        if (axisService != null) {
            Iterator<AxisOperation> operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation axisOperation = operations.next();
                if (axisOperation.getMessageReceiver() == null) {
                    axisOperation.setMessageReceiver(new JAXWSMessageReceiver());
                }
            }
            axisService.setElementFormDefault(false);
            axisService.setFileName(serviceLocation);
            axisService.setClassLoader(classLoader);
            axisService.addParameter(new Parameter(
                    org.apache.axis2.jaxws.spi.Constants.CACHE_CLASSLOADER, classLoader));
        }
        return axisService;
    }

    /**
     * Gets the list of classes.
     * 
     * @param deploymentFileData
     *            the deployment file data
     * @return the list of classes
     * @throws DeploymentException
     *             the deployment exception
     */
    public List<String> getListOfClasses(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        return Utils.getListOfClasses(deploymentFileData);
    }

    /**
     * Gets the service class name from meta data.
     * 
     * @param serviceMetaData
     *            the service meta data
     * @return the service class name from meta data
     */
    public List<String> getServiceClassNameFromMetaData(OMElement serviceMetaData) {
        List<String> classNames = new ArrayList<String>();
        if (serviceMetaData.getLocalName().equals("serviceGroup")) {
            for (Iterator<OMElement> services = serviceMetaData.getChildrenWithLocalName("service"); services
                    .hasNext();) {
                for (Iterator<OMElement> parameters = services.next().getChildrenWithLocalName(
                        "parameter"); parameters.hasNext();) {
                    OMElement parameter = parameters.next();
                    OMAttribute att = parameter.getAttribute(new QName("name"));
                    if (att != null) {
                        String value = att.getAttributeValue();
                        if (value != null && "ServiceClass".equals(value)) {
                            classNames.add(parameter.getText());
                        }
                    }
                }
            }

        } else if (serviceMetaData.getLocalName().equals("service")) {
            for (Iterator<OMElement> parameters = serviceMetaData
                    .getChildrenWithLocalName("parameter"); parameters.hasNext();) {
                OMElement parameter = parameters.next();
                OMAttribute att = parameter.getAttribute(new QName("name"));
                if (att != null) {
                    String value = att.getAttributeValue();
                    if (value != null && "ServiceClass".equals(value)) {
                        classNames.add(parameter.getText());
                    }
                }
            }

        }
        return classNames;
    }

    /**
     * Configure addressing.
     * 
     * @param serviceGroup
     *            the service group
     */
    private void configureAddressing(AxisServiceGroup serviceGroup) {
        EndpointContextMap map = (EndpointContextMap) getConfigurationContext().getProperty(
                org.apache.axis2.jaxws.Constants.ENDPOINT_CONTEXT_MAP);

        if (map == null) {
            map = EndpointContextMapManager.getEndpointContextMap();
            getConfigurationContext().setProperty(
                    org.apache.axis2.jaxws.Constants.ENDPOINT_CONTEXT_MAP, map);
        }

        Iterator<AxisService> iterator = serviceGroup.getServices();

        while (iterator.hasNext()) {
            AxisService axisService = iterator.next();
            Parameter param = axisService.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
            EndpointDescription ed = (EndpointDescription) param.getValue();
            QName serviceName = ed.getServiceQName();
            QName portName = ed.getPortQName();
            EndpointKey key = new EndpointKey(serviceName, portName);

            map.put(key, axisService);
        }
    }

}
