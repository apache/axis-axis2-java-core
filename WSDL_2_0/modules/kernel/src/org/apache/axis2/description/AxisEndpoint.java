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

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;

public class AxisEndpoint extends AxisDescription{

    // The name of the endpoint
    private String name;

    // The binding reffered to by the endpoint
    private AxisBinding binding;

    // The alias used for the endpoint
    private String alias;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AxisBinding getBinding() {
        return binding;
    }

    public void setBinding(AxisBinding binding) {
        this.binding = binding;
    }

    public Object getKey() {
        //ToDO
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void engageModule(AxisModule axisModule, AxisConfiguration axisConfig) throws AxisFault {
        throw new UnsupportedOperationException("Sorry we do not support this");
    }

    public boolean isEngaged(QName moduleName) {
        throw new UnsupportedOperationException("axisMessage.isEngaged(qName) is not supported");

    }
}
