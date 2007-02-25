package org.apache.axis2.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;

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
/**
 * This interface is used to provide the custom deployment mechanism , where you
 * can write your owm Deployer to process a particular type and make that to
 * a service or a module.
 */
public interface Deployer {
    //To initialize the deployer
    void init(ConfigurationContext configCtx);

    //Will process the file and add that to axisConfig
    void deploy(DeploymentFileData deploymentFileData);
    void setDirectory(String directory);
    void setExtension(String extension);
    void unDeploy(String fileName);
}
