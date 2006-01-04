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
*/


package org.apache.axis2.deployment.listener;

/**
 * RepositoryListener listens to a specific directory for updates.
 * eg: addition, removal or modification of files.
 */
public interface RepositoryListener {

    /**
     * Checks whether new module has been added.
     */
    void checkModules();

    /**
     * Checks whether new service has been added.
     */
    void checkServices();

    /**
     * Initializes the deployment. This is invoked during Axis engine start up
     * at which point it deploys all the services and modules, and initializes the WSInfolist.
     */
    void init();

    /**
     * Updates when new services or modules are added.
     */
    void update();
}
