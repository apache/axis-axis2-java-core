package org.apache.axis.deployment.repository.utill;

import org.apache.axis.deployment.DeploymentConstants;
import org.apache.axis.deployment.DeploymentEngine;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

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
 */
public class WSInfoList implements DeploymentConstants {
    /**
     * This is to store all the jar files in a specified folder (WEB_INF)
     */
    private static List jarlist = new ArrayList();

    /**
     * All the curently updated jars
     */
    public List currentjars = new ArrayList();

    /**
     * Referance to DeploymentEngine to make update
     */
    private DeploymentEngine deplorer;

    public WSInfoList(DeploymentEngine deploy_engine) {
        deplorer = deploy_engine;
    }

    /**
     * This method is used to initialize the vector
     */
    public void init() {
        jarlist.clear();
    }

    /**
     * First it check whether the file is already available in the
     * system call isFileExist , if it is not deployed yet then it will add
     * that to jarlist and to the deployment engine as new service or module
     * in adding new item to jarlist it first create optimice and requird object to
     * keep those infor call WSInfo and that will be added to jarist and actual
     * jar file will be added to DeploymentEngine
     * <p/>
     * If it is alredy exsit then it check whether it has been updated
     * then change the last update date of the wsInfo and added two entries to DeploymentEngine
     * one for New Deployment and other for undeployment
     *
     * @param file actual jar files for either Module or service
     * @param type indicate either Service or Module
     */
    public void addWSInfoItem(File file, int type) {
        switch (type) {
            case SERVICE:
                {
                    if (!isFileExist(file.getName())) { // chacking whether the file is already deployed
                        WSInfo wsInfo = new WSInfo(file.getName(), file.lastModified(), SERVICE);
                        jarlist.add(wsInfo);
                        HDFileItem hdFileItem = new HDFileItem(file, SERVICE);
                        deplorer.addtowsToDeploy(hdFileItem);//to inform that new web service is deployed
                    } else {
                        WSInfo tempWSInfo = getFileItem(file.getName());
                        if (isModified(file, tempWSInfo)) {  // caheck whether file is updated
                            tempWSInfo.setLastmodifieddate(file.lastModified());
                            WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(), tempWSInfo.getLastmodifieddate(), SERVICE);
                            deplorer.addtowstoUnDeploy(wsInfo);  // add entry to undeploy list
                            HDFileItem hdFileItem = new HDFileItem(file, SERVICE);
                            deplorer.addtowsToDeploy(hdFileItem);   // add entry to deploylist

                        }
                    }
                    break;
                }
            case MODULE:
                {
                    if (!isFileExist(file.getName())) {  // chacking whether the file is already deployed
                        WSInfo wsInfo = new WSInfo(file.getName(), file.lastModified(), MODULE);
                        jarlist.add(wsInfo);
                        HDFileItem hdFileItem = new HDFileItem(file, MODULE);
                        deplorer.addtowsToDeploy(hdFileItem);//to inform that new web service is deployed
                    } else {
                        WSInfo tempWSInfo = getFileItem(file.getName());
                        if (isModified(file, tempWSInfo)) {
                            tempWSInfo.setLastmodifieddate(file.lastModified());
                            WSInfo wsInfo = new WSInfo(tempWSInfo.getFilename(), tempWSInfo.getLastmodifieddate(), MODULE);
                            deplorer.addtowstoUnDeploy(wsInfo);   // add entry to undeploy list
                            HDFileItem hdFileItem = new HDFileItem(file, MODULE);
                            deplorer.addtowsToDeploy(hdFileItem); // add entry to deploylist

                        }
                    }
                    break;
                }
        }
        String jarname = file.getName();
        currentjars.add(jarname);
    }

    /**
     * This method is to use to check the file exist and if so
     * it will return related wsinfo object to the file else return null;
     *
     * @param filename
     * @return
     */
    public WSInfo getFileItem(String filename) {
        int sise = jarlist.size();
        for (int i = 0; i < sise; i++) {
            WSInfo wsInfo = (WSInfo) jarlist.get(i);
            if (wsInfo.getFilename().equals(filename)) {
                return wsInfo;
            }
        }
        return null;
    }

    /**
     * comapre the last update dates of both files and if those are differ
     * that will assume as the file is been modified
     *
     * @param file
     * @param wsInfo
     * @return
     */
    public boolean isModified(File file, WSInfo wsInfo) {
        if (wsInfo.getLastmodifieddate() != file.lastModified()) {
            return true;
        }
        return false;
    }

    /**
     * to check whether the file is alredy in the list
     *
     * @param filename
     * @return
     */
    public boolean isFileExist(String filename) {
        if (getFileItem(filename) == null) {
            return false;
        } else
            return true;
    }

    /**
     * this is to check , undeploye WS
     * what this relly does is it caheck older jars files and
     * current jars if name of the old jar file does not exit in the currecntjar
     * list then it is assumed that the jar file has been removed
     * that is hot undeployment
     */
    public void checkForUndeploye() {
        Iterator iter = jarlist.listIterator();
        int size = currentjars.size();
        List tempvector = new ArrayList();
        tempvector.clear();
        String filename = "";
        boolean exist = false;
        while (iter.hasNext()) {
            WSInfo fileitem = (WSInfo) iter.next();
            exist = false;
            for (int i = 0; i < size; i++) {
                filename = (String) currentjars.get(i);
                if (filename.equals(fileitem.getFilename())) {
                    exist = true;
                    break;
                }
            }

            if (!exist) {
                tempvector.add(fileitem);
                WSInfo wsInfo = new WSInfo(fileitem.getFilename(), fileitem.getLastmodifieddate());
                deplorer.addtowstoUnDeploy(wsInfo);//this is to be undeploye
            }

        }

        for (int i = 0; i < tempvector.size(); i++) {
            WSInfo fileItem = (WSInfo) tempvector.get(i);
            jarlist.remove(fileItem);
        }
        tempvector.clear();
        currentjars.clear();
    }


    /**
     *
     */
    public void update() {
        checkForUndeploye();
        deplorer.doUnDeploye();
        deplorer.doDeploy();

    }

}
