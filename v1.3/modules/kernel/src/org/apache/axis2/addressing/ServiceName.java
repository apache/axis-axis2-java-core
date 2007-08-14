/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.addressing;

import javax.xml.namespace.QName;

/**
 * @deprecated I don't think this class is used anywhere. Shout if this is not the case.
 *             Class ServiceName
 */
public class ServiceName {

    /**
     * Field name
     */
    private QName name;

    /**
     * Field portName
     */
    private String portName;

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
        this.portName = portName;
    }

    /**
     * Method getName
     */
    public QName getName() {
        return name;
    }

    /**
     * Method getPortName
     */
    public String getPortName() {
        return portName;
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
     * Method setPortName
     *
     * @param portName
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }
}
