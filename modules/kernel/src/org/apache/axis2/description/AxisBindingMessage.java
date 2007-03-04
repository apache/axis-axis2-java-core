/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

             http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.     
 */
package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

public class AxisBindingMessage extends AxisDescription {

    private String name;

    private String direction;

    private Map options;

    private AxisMessage axisMessage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AxisMessage getAxisMessage() {
        return axisMessage;
    }

    public void setAxisMessage(AxisMessage axisMessage) {
        this.axisMessage = axisMessage;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public AxisBindingMessage() {
        options = new HashMap();
    }


    public void setProperty(String name, Object value) {
        options.put(name, value);
    }

    /**
     * @param name name of the property to search for
     * @return the value of the property, or null if the property is not found
     */
    public Object getProperty(String name) {
        Object obj = options.get(name);
        if (obj != null) {
            return obj;
        }

        return null;
    }


    public Object getKey() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(QName moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged(qName) is not supported");

    }
}

