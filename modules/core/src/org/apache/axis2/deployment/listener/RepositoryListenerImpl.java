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

import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.ArchiveFileData;
import org.apache.axis2.deployment.repository.util.WSInfoList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;

public class RepositoryListenerImpl implements RepositoryListener, DeploymentConstants {
    protected Log log = LogFactory.getLog(getClass());
    private DeploymentEngine deEngine;

    /**
     * The parent directory of the modules and services directories
     * taht the listentner should listent
     */
    private String folderName;

    /**
     * Referance to a WSInfoList
     */
    private WSInfoList wsInfoList;

    /**
     * This constructor take two argumnets folder name and referance to Deployment Engine
     * Fisrt it initilize the syetm , by loading all the modules in the /modules directory
     * and also create a WSInfoList to keep infor about available modules and services
     *
     * @param folderName    path to parent directory that the listener should listent
     * @param deploy_engine refearnce to engine registry  inorder to inform the updates
     */
    public RepositoryListenerImpl(String folderName, DeploymentEngine deploy_engine) {
        this.folderName = folderName;
        wsInfoList = new WSInfoList(deploy_engine);
        this.deEngine = deploy_engine;
        init();
    }

    /**
     * this method ask serachWS to serch for the folder to caheck
     * for updates
     */
    public void checkModules() {
        String modulepath = folderName + MODULE_PATH;
        File root = new File(modulepath);
        File[] files = root.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                if (!file.isDirectory()) {
                    if (ArchiveFileData.isModuleArchiveFile(file.getName())) {
                        wsInfoList.addWSInfoItem(file, TYPE_MODULE);
                    }
                } else {
                    if ("lib".equals(file.getName()) || "Lib".equals(file.getName())) {

                        // this is a lib file no need to take this as a sevice
                    } else {
                        wsInfoList.addWSInfoItem(file, TYPE_MODULE);
                    }
                }
            }
        }
    }

    /**
     * this method ask serachWS to serch for the folder to caheck
     * for updates
     */
    public void checkServices() {
        String modulepath = folderName + SERVICE_PATH;

        searchWS(modulepath);
        update();
    }

    /**
     * First it call to initalize method of WSInfoList to initilizat that
     * then it call to checkModules to load all the module.jar s
     * and then it call to update() method inorder to update the Deployment engine and
     * engine regsitry
     */
    public void init() {
        wsInfoList.init();
        checkModules();
        deEngine.doDeploy();
    }

    /**
     * This method is to search a given folder  for jar files
     * and added them to a list wich is in the WSInfolist class
     */
    private void searchWS(String folderName) {
        File root = new File(folderName);
        File[] files = root.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];

                if (!file.isDirectory()) {
                    if (ArchiveFileData.isServiceArchiveFile(file.getName())) {
                        wsInfoList.addWSInfoItem(file, TYPE_SERVICE);
                    }
                } else {
                    if ("lib".equals(file.getName()) || "Lib".equals(file.getName())) {

                        // this is a lib file no need to take this as a sevice
                    } else {
                        wsInfoList.addWSInfoItem(file, TYPE_SERVICE);
                    }
                }
            }
        }
    }

    /**
     * this is the actual method that is call from scheduler
     */
    public void startListener() {
        checkServices();
        update();
    }

    /**
     * call to update method of WSInfoList object
     */
    public void update() {
        wsInfoList.update();
    }
}
