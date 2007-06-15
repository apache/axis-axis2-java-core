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

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.ServiceDeployer;

public class WSInfo {
    private String fileName;
    private long lastModifiedDate;

    public static final int TYPE_SERVICE = 0;
    public static final int TYPE_MODULE = 1;

    /**
     * To check whether the file is a module or a servise
     */
    private int type = TYPE_SERVICE;

    private Deployer deployer;

    public WSInfo(String filename, long lastmodifieddate) {
        this.fileName = filename;
        this.lastModifiedDate = lastmodifieddate;
    }


    public WSInfo(String fileName, long lastModifiedDate, int type) {
        this.fileName = fileName;
        this.lastModifiedDate = lastModifiedDate;
        this.type = type;
    }

    public WSInfo(String fileName, long lastModifiedDate, Deployer deployer) {
        this.fileName = fileName;
        this.lastModifiedDate = lastModifiedDate;
        this.deployer = deployer;
        //TODO: This is a temporary fix for the hot update in custom deployers
//        if (!(deployer instanceof ServiceDeployer)) {
//           this.type=2;
//        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getType() {
        return type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLastModifiedDate(long lastmodifieddate) {
        this.lastModifiedDate = lastmodifieddate;
    }
    
    public Deployer getDeployer() {
        return deployer;
    }
}
