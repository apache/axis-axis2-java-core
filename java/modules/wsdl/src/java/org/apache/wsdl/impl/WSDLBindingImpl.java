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
package org.apache.wsdl.impl;

import org.apache.wsdl.WSDLBinding;
import org.apache.wsdl.WSDLBindingFault;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLInterface;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author chathura@opensource.lk
 */
public class WSDLBindingImpl extends ExtensibleComponentImpl
        implements WSDLBinding {
    /**
     * Field name
     */
    private QName name;

    /**
     * Field boundInterface
     */
    private WSDLInterface boundInterface;

    /**
     * Field bindingFaults
     */
    private HashMap bindingFaults = new HashMap();

    /**
     * Field bindingOperations
     */
    private HashMap bindingOperations = new HashMap();

    /**
     * Method getBoundInterface
     *
     * @return
     */
    public WSDLInterface getBoundInterface() {
        return boundInterface;
    }

    /**
     * Method setBoundInterface
     *
     * @param boundInterface
     */
    public void setBoundInterface(WSDLInterface boundInterface) {
        this.boundInterface = boundInterface;
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
     * Method getTargetNameSpace
     *
     * @return
     */
    public String getTargetNameSpace() {
        return this.name.getLocalPart();
    }

    /**
     * Method getBindingFaults
     *
     * @return
     */
    public HashMap getBindingFaults() {
        return bindingFaults;
    }

    /**
     * Method setBindingFaults
     *
     * @param bindingFaults
     */
    public void setBindingFaults(HashMap bindingFaults) {
        this.bindingFaults = bindingFaults;
    }

    /**
     * Method getBindingOperations
     *
     * @return
     */
    public HashMap getBindingOperations() {
        return bindingOperations;
    }

    /**
     * Method setBindingOperations
     *
     * @param bindingOperations
     */
    public void setBindingOperations(HashMap bindingOperations) {
        this.bindingOperations = bindingOperations;
    }

    /**
     * Method addBindingOperation
     *
     * @param bindingOperation
     */
    public void addBindingOperation(WSDLBindingOperation bindingOperation) {
        if (null != bindingOperation) {
            this.bindingOperations.put(bindingOperation.getName(),
                    bindingOperation);
        }
    }

    /**
     * Method getBindingOperation
     *
     * @param qName
     * @return
     */
    public WSDLBindingOperation getBindingOperation(QName qName) {
        return (WSDLBindingOperation) this.bindingOperations.get(qName);
    }

    /**
     * Method addBindingFaults
     *
     * @param bindingFault
     */
    public void addBindingFaults(WSDLBindingFault bindingFault) {
        if (null != bindingFault) {
            this.bindingFaults.put(bindingFault.getRef(), bindingFault);
        }
    }

    /**
     * Method getBindingFault
     *
     * @param ref
     * @return
     */
    public WSDLBindingFault getBindingFault(QName ref) {
        return (WSDLBindingFault) this.bindingFaults.get(ref);
    }
}
