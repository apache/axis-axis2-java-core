package org.apache.axis.deployment.listener;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         12:00:34 PM
 *
 */

/**
 * RepositaryListener is no listent to a specific folder whether the folder is
 * update eg: remove , added or modified files
 */
public interface RepositoryListener {

    /**
     * this method is to check whether new module is added (or modules)
     */
    void checkModules();

    /**
     * this method is to check whether new service is added (or services)
     */
    void checkServices();

    /**
     * If new services or modules(service or module) are added then this method will call
     */
    void update();

    /**
     * this is to Initialize the Deployment , this only call when the Axis engine start up
     * Then it should deploy all the WS and modules , and should initialize the WSInfoList
     */
    void init();

}