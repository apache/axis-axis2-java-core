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
package org.apache.axis2.jaxws.calculator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "addResponse", namespace = "http://calculator.jaxws.axis2.apache.org")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "addResponse", namespace = "http://calculator.jaxws.axis2.apache.org")
public class AddResponse {

    @XmlElement(name = "return", namespace = "http://calculator.jaxws.axis2.apache.org")
    private int _return;

    /**
     * @return returns int
     */
    public int getReturn() {
        return this._return;
    }

    /**
     * @param _return the value for the _return property
     */
    public void setReturn(int _return) {
        this._return = _return;
    }

}
