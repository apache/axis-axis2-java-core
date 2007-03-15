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


package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WSInfoList implements DeploymentConstants {

    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private List jarList = new ArrayList();

    /**
     * All the currently updated jars
     */
    public List currentJars = new ArrayList();

    /**
     * Reference to DeploymentEngine to make update
     */
    private DeploymentEngine deployer;

    private boolean check;

    public WSInfoList(DeploymentEngine deploy_engine) {
        deployer = deploy_engine;
    }

    /**
     * First checks whether the file is already available by the
     * system call isFileExist. If it is not deployed yet then adds to the jarList
     * and to the deployment engine as a new service or module.
     * While adding new item to jarList, first creates the WSInfo object and
     * then adds to the jarlist and actual jar file is added to DeploymentEngine.
     * <p/>
     * If the files already exists, then checks whether it has been updated
     * then changes the last update date of the wsInfo and adds two entries to
     * DeploymentEngine - one for new deployment and other for undeployment.
     *
     * @param file actual jar files for either Module or service
     * @param type indicate either Service or Module
     */
    public synchronized void addWSInfoItem(File file, String type) {
        if (TYPE_SERVICE.equals(type)) {
            if (!isFileExist(file.getName())) {    // checking whether the file is already deployed
                WSInfo wsInfo = new WSInfo(file.getName(), file.lastModified(), TYPE_SERVICE);
                jarList.add(wsInfo);
                DeploymentFileData deploymentFileData =
                        new DeploymentFileData(file, TYPE_SERVICE);
                deployer.addWSToDeploy(
                        deploymentFileData);    // inform that new web service is deployed
            } else {
                if (deployer.isHotUpdate()) {
                    WSInfo tempWSInfo = getFileItem(file.getName());
                    if (isModified(file, tempWSInfo)) {    // check whether file is updated
                        tempWSInfo.setLastModifiedDate(file.lastModified());
                        WSInfo wsInfo = new WSInfo(tempWSInfo.getFileName(),
                                                   tempWSInfo.getLastModifiedDate(), TYPE_SERVICE);
                        deployer.addWSToUndeploy(wsInfo);           // add entry to undeploy list
                        DeploymentFileData deploymentFileData = new DeploymentFileData(file,
                                                                                       TYPE_SERVICE);
                        deployer.addWSToDeploy(deploymentFileData);    // add entry to deploylist
                    }
                }
            }
        } else if (TYPE_MODULE.equals(type)) {
            if (!isFileExist(file.getName()))
            {                     // checking whether the file is already deployed
                WSInfo wsInfo = new WSInfo(file.getName(), file.lastModified(), TYPE_MODULE);
                jarList.add(wsInfo);
                DeploymentFileData deploymentFileData =
                        new DeploymentFileData(file, TYPE_MODULE);
                deployer.addWSToDeploy(
                        deploymentFileData);    // inform that new web service is deployed
            }
        } else {
            if (file != null) {
                String extension = DeploymentFileData.getFileExtension(file.getName());
                if (!isFileExist(file.getName())) {
                    WSInfo wsInfo = new WSInfo(file.getName(), file.lastModified(), extension);
                    jarList.add(wsInfo);
                    DeploymentFileData deploymentFileData =
                            new DeploymentFileData(file, extension);
                    deployer.addWSToDeploy(
                            deploymentFileData);    // inform that new web service is deployed
                } else {
                    if (deployer.isHotUpdate()) {
                        WSInfo tempWSInfo = getFileItem(file.getName());
                        if (isModified(file, tempWSInfo)) {    // check whether file is updated
                            tempWSInfo.setLastModifiedDate(file.lastModified());
                            WSInfo wsInfo = new WSInfo(tempWSInfo.getFileName(),
                                                       tempWSInfo.getLastModifiedDate(), extension);
                            deployer.addWSToUndeploy(
                                    wsInfo);           // add entry to undeploy list
                            DeploymentFileData deploymentFileData = new DeploymentFileData(file,
                                                                                           extension);
                            deployer.addWSToDeploy(
                                    deploymentFileData);    // add entry to deploylist
                        }
                    }
                }
            } else {
                check = true;
            }
        }
        if (file != null) {
            String jarname = file.getName();
            currentJars.add(jarname);
        }
        check = true;
    }

    /**
     * Checks undeployed Services. Checks old jars files and current jars.
     * If name of the old jar file does not exist in the current jar
     * list then it is assumed that the jar file has been removed
     * and that is hot undeployment.
     */
    private synchronized void checkForUndeployedServices() {
        if (!check) {
            return;
        } else {
            check = false;
        }

        Iterator iter = jarList.listIterator();
        int size = currentJars.size();
        List tempvector = new ArrayList();

        tempvector.clear();

        String filename;
        boolean exist;

        while (iter.hasNext()) {
            WSInfo fileitem = (WSInfo) iter.next();
            if (TYPE_MODULE.equals(fileitem.getType())) {
                continue;
            }
            exist = false;
            for (int i = 0; i < size; i++) {
                filename = (String) currentJars.get(i);
                if (filename.equals(fileitem.getFileName())) {
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                tempvector.add(fileitem);
                WSInfo wsInfo = new WSInfo(fileitem.getFileName(), fileitem.getLastModifiedDate(),
                                           fileitem.getType());

                deployer.addWSToUndeploy(wsInfo);    // this is to be undeployed
            }
        }
        for (int i = 0; i < tempvector.size(); i++) {
            WSInfo fileItem = (WSInfo) tempvector.get(i);
            jarList.remove(fileItem);
        }
        tempvector.clear();
        currentJars.clear();
    }

    /**
     * Clears the jarlist.
     */
    public void init() {
        jarList.clear();
    }

    /**
     *
     */
    public void update() throws DeploymentException {
        synchronized (deployer) {
            checkForUndeployedServices();

            deployer.unDeploy();
            deployer.doDeploy();

        }
    }

    /**
     * Gets the WSInfo object related to a file if it exists, null otherwise.
     *
     * @param filename
     */
    private WSInfo getFileItem(String filename) {
        int sise = jarList.size();

        for (int i = 0; i < sise; i++) {
            WSInfo wsInfo = (WSInfo) jarList.get(i);

            if (wsInfo.getFileName().equals(filename)) {
                return wsInfo;
            }
        }

        return null;
    }

    /**
     * Checks whether the file already exists in the list.
     *
     * @param filename
     */
    private boolean isFileExist(String filename) {
        return !(getFileItem(filename) == null);
    }

    /**
     * Checks if a file has been modified by comparing the last update date of
     * both files and WSInfo. If they are different, the file is assumed to have
     * been modified.
     *
     * @param file
     * @param wsInfo
     */
    private boolean isModified(File file, WSInfo wsInfo) {
        if (file.isDirectory()) {
            if (isChanged(file, wsInfo.getLastModifiedDate(), wsInfo)) {
                setLastModifiedDate(file, wsInfo);
                return true;
            } else {
                return false;
            }
        } else {
            return (wsInfo.getLastModifiedDate() != file.lastModified());
        }
    }

    private void setLastModifiedDate(File file, WSInfo wsInfo) {
        File files [] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File fileItem = files[i];
            if (fileItem.isDirectory()) {
                setLastModifiedDate(fileItem, wsInfo);
            } else {
                fileItem.setLastModified(wsInfo.getLastModifiedDate());
            }
        }
    }

    private boolean isChanged(File file, long lastModifedData, WSInfo wsInfo) {
        File files [] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File fileItem = files[i];
            if (fileItem.isDirectory()) {
                if (isChanged(fileItem, lastModifedData, wsInfo)) {
                    wsInfo.setLastModifiedDate(fileItem.lastModified());
                    return true;
                }
            } else {
                if (lastModifedData != fileItem.lastModified()) {
                    wsInfo.setLastModifiedDate(fileItem.lastModified());
                    return true;
                }
            }
        }
        return false;
    }


}
