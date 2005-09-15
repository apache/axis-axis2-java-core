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

import org.apache.wsdl.WSDLBindingMessageReference;

public class WSDLBindingMessageReferenceImpl extends ExtensibleComponentImpl
        implements WSDLBindingMessageReference {

    // Referes to the MEP the Message relates to.

    /**
     * Field messageLabel
     */
    private String messageLabel;

    // Can be "in" or "out" depending on the element name being "input" or
    // "output" respectively;

    /**
     * Field Direction
     */
    private String Direction;

    /**
     * Method getDirection
     *
     * @return
     */
    public String getDirection() {
        return Direction;
    }

    /**
     * Method setDirection
     *
     * @param direction
     */
    public void setDirection(String direction) {
        Direction = direction;
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
}
