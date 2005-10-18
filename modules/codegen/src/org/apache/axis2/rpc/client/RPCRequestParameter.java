package org.apache.axis2.rpc.client;

import org.apache.axis2.rpc.receivers.SimpleTypeMapper;

import javax.xml.namespace.QName;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 13, 2005
 * Time: 10:43:45 AM
 */
public class RPCRequestParameter {

    private QName name;
    private Object value;

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public void setValue(int value) {
        this.value = new Integer(value);
    }

    public void setValue(byte value) {
        this.value = new Byte(value);
    }

    public void setValue(short value) {
        this.value = new Short(value);
    }


    public void setValue(long value) {
        this.value = new Long(value);
    }

    public void setValue(double value) {
        this.value = new Double(value);
    }

    public void setValue(char value) {
        this.value = new Character(value);
    }

    public void setValue(boolean value) {
        this.value = (value) ? Boolean.TRUE : Boolean.FALSE;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isSimpletype() {
        return SimpleTypeMapper.isSimpleType(value);
    }

}
