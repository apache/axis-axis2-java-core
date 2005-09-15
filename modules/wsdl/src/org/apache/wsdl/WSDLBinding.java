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

package org.apache.wsdl;

import javax.xml.namespace.QName;
import java.util.HashMap;

public interface WSDLBinding extends ExtensibleComponent {
    /**
     * Method getBoundInterface
     *
     * @return WSDLInterface
     */
    public WSDLInterface getBoundInterface();

    /**
     * Method setBoundInterface
     *
     * @param boundInterface
     */
    public void setBoundInterface(WSDLInterface boundInterface);

    /**
     * Method getName
     *
     * @return  QName
     */
    public QName getName();

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(QName name);

    /**
     * Method getTargetNameSpace
     *
     * @return  String
     */
    public String getTargetNameSpace();

    /**
     * Method getBindingFaults
     *
     * @return String
     */
    public HashMap getBindingFaults();

    /**
     * Method setBindingFaults
     *
     * @param bindingFaults
     */
    public void setBindingFaults(HashMap bindingFaults);

    /**
     * Method getBindingOperations
     *
     * @return  Hashmap
     */
    public HashMap getBindingOperations();

    /**
     * Method setBindingOperations
     *
     * @param bindingOperations
     */
    public void setBindingOperations(HashMap bindingOperations);

    /**
     * Method addBindingOperation
     *
     * @param bindingOperation
     */
    public void addBindingOperation(WSDLBindingOperation bindingOperation);

    /**
     * Method getBindingOperation
     *
     * @param qName
     * @return  Hashmap
     */
    public WSDLBindingOperation getBindingOperation(QName qName);

    /**
     * Method addBindingFaults
     *
     * @param bindingFault
     */
    public void addBindingFaults(WSDLBindingFault bindingFault);

    /**
     * Method getBindingFault
     *
     * @param ref
     * @return  Hashmap
     */
    public WSDLBindingFault getBindingFault(QName ref);
}
