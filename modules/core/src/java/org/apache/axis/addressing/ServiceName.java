package org.apache.axis.addressing;

import javax.xml.namespace.QName;

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
 * <p/>
 */
public class ServiceName {
    private QName name;
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

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }
}
