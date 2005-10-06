/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.symbolTable;

/**
 * Class MimeInfo
 * 
 * @version %I%, %G%
 */
public class MimeInfo {

    /** Field type */
    String type;

    /** Field dims */
    String dims;

    /**
     * Constructor MimeInfo
     * 
     * @param type 
     * @param dims 
     */
    public MimeInfo(String type, String dims) {
        this.type = type;
        this.dims = dims;
    }

    /**
     * Method getDimensions
     * 
     * @return 
     */
    public String getDimensions() {
        return this.dims;
    }

    /**
     * Method getType
     * 
     * @return 
     */
    public String getType() {
        return this.type;
    }

    /**
     * Method toString
     * 
     * @return 
     */
    public String toString() {
        return "(" + type + "," + dims + ")";
    }
}
