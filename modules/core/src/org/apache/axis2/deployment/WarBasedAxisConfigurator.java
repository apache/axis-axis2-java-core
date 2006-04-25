package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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

public class WarBasedAxisConfigurator implements AxisConfigurator {

    private AxisConfiguration axisConfig;
    private Log log = LogFactory.getLog(getClass());

    public WarBasedAxisConfigurator(ServletConfig config) {
        try {
            DeploymentEngine deploymentEngine = new DeploymentEngine();
            String axis2xmlpath = config.getInitParameter("axis2.xml.path");
            String repository;
            InputStream axis2Steram;
            if (axis2xmlpath != null) {
                axis2Steram = new FileInputStream(axis2xmlpath);
                axisConfig = deploymentEngine.populateAxisConfiguration(axis2Steram);
            } else {
                String axisurl = config.getInitParameter("axis2.xml.url");
                if (axisurl != null) {
                    axis2Steram = new URL(axisurl).openStream();
                    deploymentEngine.populateAxisConfiguration(axis2Steram);
                } else {
                    try {
                        repository = config.getServletContext().getRealPath("/WEB-INF");
                        axis2Steram = new FileInputStream(repository + "/conf/axis2.xml");
                        axisConfig = deploymentEngine.populateAxisConfiguration(axis2Steram);
                        setWebLocationProperty(config.getServletContext(),deploymentEngine);
                    } catch (Exception e) {
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();
                        axis2Steram = cl.getResourceAsStream(DeploymentConstants.AXIS2_CONFIGURATION_RESOURCE);
                        axisConfig = deploymentEngine.populateAxisConfiguration(axis2Steram);
                    }
                }
            }
            String axis2repopath = config.getInitParameter("axis2.repository.path");
            if (axis2repopath != null) {
                deploymentEngine.loadRepository(axis2repopath);
            } else {
                String axis2repourl = config.getInitParameter("axis2.repository.url");
                if (axis2repourl != null) {
                    deploymentEngine.loadRepositoryFromURL(new URL(axis2repourl));
                } else {
                    try {
                        repository = config.getServletContext().getRealPath("/WEB-INF");
                        deploymentEngine.loadRepository(repository);
                    } catch (Exception e) {
                        deploymentEngine.loadFromClassPath();
                    }
                }
            }

        } catch (FileNotFoundException e) {
            log.info(e.getMessage());
        } catch (DeploymentException e) {
            log.info(e.getMessage());
        } catch (MalformedURLException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.info(e.getMessage());
        } finally {
            try {
                Parameter unableHttp = new Parameter("enableHTTP", "true");
                axisConfig.addParameter(unableHttp);
            } catch (AxisFault axisFault) {
                log.info(axisFault.getMessage());
            }
        }
    }

    /**
     * To find out the location where web reposurce need to be coiped, when
     * deployment fine any service aar with web resources.
     *
     * @param context
     */
    private void setWebLocationProperty(ServletContext context, DeploymentEngine depeng) {
        String webpath = context.getRealPath("");
        if (webpath == null || "".equals(webpath)) {
            return;
        }
        File weblocation = new File(webpath);
        depeng.setWebLocationString(weblocation.getAbsolutePath());
    }

    public AxisConfiguration getAxisConfiguration() throws AxisFault {
        return axisConfig;
    }
}
