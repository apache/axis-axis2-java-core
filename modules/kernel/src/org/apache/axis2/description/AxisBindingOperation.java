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

public class AxisBindingOperation extends AxisDescription {

    private AxisOperation axisOperation;

    private QName name;

    private Map faults;

    private Map options;

    public AxisBindingOperation() {
        options = new HashMap();
        faults = new HashMap();
    }

    public AxisBindingMessage getFault(String name) {
        return (AxisBindingMessage) faults.get(name);
    }

    public void addFault(AxisBindingMessage fault) {
        this.faults.put(fault.getName(), fault);
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public AxisOperation getAxisOperation() {
        return axisOperation;
    }

    public void setAxisOperation(AxisOperation axisOperation) {
        this.axisOperation = axisOperation;
    }

    public void setProperty(String name, Object value) {
        options.put(name, value);
    }

    public Object getProperty(String name) {
        Object property = this.options.get(name);

        AxisBinding parent;
        if (property == null && (parent = (AxisBinding) this.getParent()) != null) {
            property = parent.getProperty(name);
        }

        if (property == null) {
            property = WSDL20DefaultValueHolder.getDefaultValue(name);
        }

        return property;
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
