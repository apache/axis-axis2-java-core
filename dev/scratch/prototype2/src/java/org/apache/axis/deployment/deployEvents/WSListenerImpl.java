package org.apache.axis.deployment.deployEvents;

import org.apache.axis.deployment.DeployCons;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.fileloader.utill.WSInfoList;

import java.io.File;
import java.util.Vector;

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
 *         12:16:19 PM
 *
 */
public class WSListenerImpl implements WSListener, DeployCons {

    /**
     * to store curreently checking jars
     */
    private Vector current_jars;
    /**
     * Referance to a WSInfoList
     */
    private WSInfoList wsinfoList;

    private String folderName;

    public WSListenerImpl(String folderName, DeploymentEngine deploy_engine) {
        this.folderName = folderName;
        wsinfoList = new WSInfoList(deploy_engine);
        // wsinfoList.init();
        init();
        //  this.init();
    }

    public void checkModules() {
        String modulepath = folderName + MODULE_PATH;
        searchWS(modulepath, MODULE);
    }

    public void checkServices() {
        String modulepath = folderName + SERVICE_PATH;
        ;
        searchWS(modulepath, SERVICE);
    }

    public void update() {
        //todo completet this
        // this call the update method of WSInfoList
        wsinfoList.update();
    }

    /**
     * all the initialization should come here
     */
    public void init() {
        wsinfoList.init();
        checkModules();
        checkServices();
        update();
    }

    /**
     * this is the actual method that is call from scheduler
     */
    public void startListent() {
        checkModules();
        checkServices();
        update();
    }

    /**
     * This method is to search a given folder  for jar files
     * and added them to a list wich is in the WSInfolist class
     */
    private void searchWS(String folderName, int type) {
        String files[];
        current_jars = new Vector();
        File root = new File(folderName);
        // adding the root folder to the vector
        current_jars.addElement(root);

        while (current_jars.size() > 0) {         // loop until empty
            File dir = (File) current_jars.elementAt(0); // get first dir
            current_jars.remove(0);       // remove it
            files = dir.list();              // get list of files

            for (int i = 0; i < files.length; i++) { // iterate
                File f = new File(dir, files[i]);
                if (f.isDirectory()) {        // see if it's a directory
                    current_jars.insertElementAt(f, 0);
                } // add dir to start of agenda
                else if (isJarFile(f.getName())) {
                    wsinfoList.addWSInfoItem(f, type);
                }
            }
        }
    }

    private boolean isJarFile(String filename) {
        // to check whether the file is  a jar file
        if (!filename.endsWith(".jar")) {
            return false;
        } else
            return true;
    }

}
