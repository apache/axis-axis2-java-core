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
package org.apache.axis2.om;

import javax.activation.DataHandler;

/**
 * Interface OMText
 */
public interface OMText extends OMNode {
    /**
     * Returns the text value of this node
     *
     * @return string
     */
    String getText();

    /**
     * get the datahandler
     * @return datahandler
     */
    DataHandler getDataHandler();

    /**
     * @return boolean flag saying whether the node contains
     *         an optimized text or not
     */
    boolean isOptimized();

    /**
     * set the optimize flag
     * @param value
     */
    void setOptimize(boolean value);

    /**
     * get the content id
     * @return string
     */
    String getContentID();
}