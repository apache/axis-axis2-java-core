package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.util.Loader;
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

public class FileSystemConfigurator extends DeploymentEngine implements AxisConfigurator {

    private static final Log log = LogFactory.getLog(FileSystemConfigurator.class);
    /**
     * To check whether need to create a service side or client side
     */
    private String axis2xml = null;
    private String repoLocation = null;

    /**
     * Load an AxisConfiguration from the repository directory specified
     *
     * @param repoLocation
     * @param axis2xml
     */
    public FileSystemConfigurator(String repoLocation, String axis2xml) throws AxisFault {
        if (repoLocation == null) {
            //checking wether user has set the system property
            repoLocation = System.getProperty(Constants.AXIS2_REPO);
        }

        // OK, we've got a repository location in mind.  Let's make
        // sure it exists.
        if (repoLocation != null) {
            File repo = new File(repoLocation);
            if (repo.exists()) {
                // ok, save it if so
                this.repoLocation = repo.getAbsolutePath();
            } else {
                log.info("Couldn't find repository location '" +
                        repoLocation + "'");
                throw new AxisFault("Couldn't find repository location '" +
                        repoLocation + "'");
            }
        }
        // Deal with the config file.  If a filename was specified as an
        // arg to this constructor, just respect it.
        if (axis2xml == null) {
            // If not, check for a system property setting
            axis2xml = System.getProperty(Constants.AXIS2_CONF);
            // In either case, check that the file exists... if not
            // we'll use the default axis2.xml on the classpath.
            if (axis2xml != null) {
                File configFile = new File(axis2xml);
                if (!configFile.exists()) {
                    log.info("Error in file (axis2.xml) creation inside FileSystemConfigurator");
                    throw new AxisFault(
                            "Error in file (axis2.xml) creation inside FileSystemConfigurator");
                }
            }
        }

        this.axis2xml = axis2xml;
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
                axis2xmlSream =
                        Loader.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
            }
            axisConfig = populateAxisConfiguration(axis2xmlSream);
        } catch (FileNotFoundException e) {
            throw new AxisFault("System can not find the given axis2.xml " + axis2xml);
        }
        Parameter axis2repoPara = axisConfig.getParameter(DeploymentConstants.AXIS2_REPO);
        if (axis2repoPara != null) {
            repoLocation = (String) axis2repoPara.getValue();
        }
        if (!(repoLocation == null || "".equals(repoLocation))) {
            loadRepository(repoLocation);
        } else {
            loadFromClassPath();
        }
        return axisConfig;
    }

    public void engageGlobalModules() throws AxisFault {
        engageModules();
    }

    public void loadServices() {
        if (!(repoLocation == null || "".equals(repoLocation))) {
            super.loadServices();
        }
    }
}
