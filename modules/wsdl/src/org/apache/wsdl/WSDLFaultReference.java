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

/**
 * @author chathura@opensource.lk
 */
public interface WSDLFaultReference extends Component {
    /**
     * Returns the direction of the Fault according the MEP
     *
     * @return
     */
    public String getDirection();

    /**
     * Sets the direction of the Fault.
     *
     * @param direction
     */
    public void setDirection(String direction);

    /**
     * Method getMessageLabel
     *
     * @return
     */
    public String getMessageLabel();

    /**
     * Method setMessageLabel
     *
     * @param messageLabel
     */
    public void setMessageLabel(String messageLabel);

    /**
     * Returns the Fault reference.
     *
     * @return
     */
    public QName getRef();

    /**
     * Sets the Fault reference.
     *
     * @param ref
     */
    public void setRef(QName ref);
}
