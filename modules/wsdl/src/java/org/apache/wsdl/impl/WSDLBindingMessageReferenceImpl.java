/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * @author chathura@opensource.lk
 */
public class WSDLBindingMessageReferenceImpl extends ExtensibleComponentImpl implements WSDLBindingMessageReference {

    //  Referes to the MEP the Message relates to.
    private String messageLabel;

    // Can be "in" or "out" depending on the element name being "input" or
    // "output" respectively;
    private String Direction;

    public String getDirection() {
        return Direction;
    }

    public void setDirection(String direction) {
        Direction = direction;
    }

    public String getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(String messageLabel) {
        this.messageLabel = messageLabel;
    }
}