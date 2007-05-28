/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.scripting;

import java.io.File;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.RepositoryListener;

/**
 * An Axis2 RepositoryListener subclass for dealing with script services
 */
public class ScriptRepositoryListener extends RepositoryListener {

    public ScriptRepositoryListener(DeploymentEngine deploymentEngine) {
        super(deploymentEngine, false);
    }

    /**
     * Searches a given folder for script services and adds them to a list in
     * the WSInfolist class.
     */
    protected void findServicesInDirectory() {

        File[] files = deploymentEngine.getServicesDir().listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                wsInfoList.addWSInfoItem(files[i], deploymentEngine.getModuleDeployer());
            }
        }

        wsInfoList.addWSInfoItem(null, deploymentEngine.getModuleDeployer());
    }

    /*
     * Override the RepositoryListener method to do nothing as not required for
     * script services
     */
    public void checkModules() {
    }

    /*
     * Override the RepositoryListener method to do nothing as not required for
     * script services
     */
    protected void loadClassPathModules() {
    }
}
