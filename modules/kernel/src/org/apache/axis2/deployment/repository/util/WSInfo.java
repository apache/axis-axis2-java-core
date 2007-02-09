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

public class WSInfo {
    private String fileName;
    private long lastModifiedDate;

    /**
     * To check whether the file is a module or a servise
     */
    private String type;

    public WSInfo(String filename, long lastmodifieddate) {
        this.fileName = filename;
        this.lastModifiedDate = lastmodifieddate;
    }

    public WSInfo(String filename, long lastmodifieddate, String type) {
        this.fileName = filename;
        this.lastModifiedDate = lastmodifieddate;
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public long getLastModifiedDate() {
        return lastModifiedDate;
    }

    public String getType() {
        return type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setLastModifiedDate(long lastmodifieddate) {
        this.lastModifiedDate = lastmodifieddate;
    }
}
