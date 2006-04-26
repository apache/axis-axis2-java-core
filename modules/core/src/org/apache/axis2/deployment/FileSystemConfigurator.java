package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
*
*
*/

public class FileSystemConfigurator implements AxisConfigurator {

    private Log log = LogFactory.getLog(getClass());
    /**
     * To check whether need to create a service side or client side
     */
    private String axis2xml = null;
    private String repoLocation = null;
    private DeploymentEngine deploymentEngine;
    private AxisConfiguration axisConfiguration;

    /**
     * Load an AxisConfiguration from the repository directory specified
     *
     * @param repoLocation
     * @param axis2xml
     */
    public FileSystemConfigurator(String repoLocation, String axis2xml) {
        if (repoLocation == null) {
            //checking wether user has set the system property
            repoLocation = System.getProperty(Constants.AXIS2_REPO);
        }

        // OK, we've got a repository location in mind.  Let's make
        // sure it exists.
        try {
            File repo = new File(repoLocation);
            if (repo.exists()) {
                // ok, save it if so
                this.repoLocation = repo.getAbsolutePath();
            }
        } catch (Exception e) {
            log.info("Couldn't find repository location '" +
                    repoLocation + "'");
            this.repoLocation = null;
        }

        // Deal with the config file.  If a filename was specified as an
        // arg to this constructor, just respect it.
        if (axis2xml == null) {
            String propertyAxis2xml = System.getProperty(Constants.AXIS2_CONF);
            if (propertyAxis2xml != null) {
                try {
                    File axis2discriptor = new File(propertyAxis2xml);
                    if (axis2discriptor.exists()) {
                        this.axis2xml = axis2discriptor.getAbsolutePath();
                    } else {
                        this.axis2xml = null;
                    }
                } catch (Exception e) {
                    this.axis2xml = null;
                    log.info("Error in file (axis2.xml) creation inside FileSystemConfigurator");
                }

            } else {
                this.axis2xml = null;
            }
        } else {
            this.axis2xml = axis2xml;
        }
        deploymentEngine = new DeploymentEngine();
    }

    /**
     * First create a Deployment engine, use that to create an AxisConfiguration
     *
     * @return Axis Configuration
     * @throws AxisFault
     */
    public synchronized AxisConfiguration getAxisConfiguration() throws AxisFault {
        InputStream axis2xmlSream;
        try {
            if (axis2xml != null && !"".equals(axis2xml)) {
                axis2xmlSream = new FileInputStream(axis2xml);
            } else {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                axis2xmlSream = cl.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
            }
            axisConfiguration = deploymentEngine.populateAxisConfiguration(axis2xmlSream);
        } catch (FileNotFoundException e) {
            throw new AxisFault("System can not find the given axis2.xml " + axis2xml);
        }
        Parameter axis2repoPara = axisConfiguration.getParameter(DeploymentConstants.AXIS2_REPO);
        if (axis2repoPara != null) repoLocation = (String) axis2repoPara.getValue();
        if (!(repoLocation == null || "".equals(repoLocation))) {
            deploymentEngine.loadRepository(repoLocation);
        } else {
            deploymentEngine.loadFromClassPath();
        }
        return axisConfiguration;
    }

    public void engageGlobalModules() throws AxisFault {
        deploymentEngine.engageModules();
    }

    public void loadServices() {
        if (!(repoLocation == null || "".equals(repoLocation))) {
            deploymentEngine.loadServices();
        }

    }
}
