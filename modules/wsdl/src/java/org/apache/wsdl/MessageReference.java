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
package org.apache.wsdl;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public interface MessageReference extends ExtensibleComponent {
    public String getDirection();

    public void setDirection(String direction);


    /**
     * This Element refers to the actual message that will get transported. This Element
     * Abstracts all the Message Parts that was defined in the WSDL 1.1.
     */
    public QName getElement();

    public void setElement(QName element);

    public String getMessageLabel();

    public void setMessageLabel(String messageLabel);
}