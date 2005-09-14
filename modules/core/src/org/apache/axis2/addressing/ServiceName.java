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
package org.apache.axis2.addressing;

import java.io.Serializable;

import javax.xml.namespace.QName;

/**
 * Class ServiceName
 */
public class ServiceName implements Serializable{
    /**
     * Field name
     */
    private QName name;

    /**
     * Field endpointName
     */
    private String endpointName;

    /**
     * @param name
     */
    public ServiceName(QName name) {
        this.name = name;
    }

    /**
     * @param name
     * @param portName
     */
    public ServiceName(QName name, String portName) {
        this.name = name;
        this.endpointName = portName;
    }

    /**
     * Method getName
     *
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Method getEndpointName
     *
     * @return
     */
    public String getEndpointName() {
        return endpointName;
    }

    /**
     * Method setEndpointName
     *
     * @param endpointName
     */
    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }
}
