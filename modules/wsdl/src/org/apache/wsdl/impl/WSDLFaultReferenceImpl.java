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

import org.apache.wsdl.WSDLFaultReference;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class WSDLFaultReferenceImpl extends ComponentImpl
        implements WSDLFaultReference {


    /**
     * Field ref
     */
    private QName ref;


    /**
     * Field messageLabel
     */
    private String messageLabel;

    /**
     * Field direction In or Out
     */
    private String direction;


    public WSDLFaultReferenceImpl() {

    }

    /**
     * Returns the direction of the Fault according the MEP
     *
     * @return
     */
    public String getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the Fault.
     *
     * @param direction
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * Method getMessageLabel
     *
     * @return
     */
    public String getMessageLabel() {
        return messageLabel;
    }

    /**
     * Method setMessageLabel
     *
     * @param messageLabel
     */
    public void setMessageLabel(String messageLabel) {
        this.messageLabel = messageLabel;
    }

    /**
     * Returns the Fault reference.
     *
     * @return
     */
    public QName getRef() {
        return ref;
    }

    /**
     * Sets the Fault reference.
     *
     * @param ref
     */
    public void setRef(QName ref) {
        this.ref = ref;
    }
}
