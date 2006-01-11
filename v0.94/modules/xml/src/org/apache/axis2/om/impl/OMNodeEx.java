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

package org.apache.axis2.om.impl;

import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.OMNode;

import javax.xml.stream.XMLStreamException;

/**
 * Interface OMNodeEx
 *
 * Internal Implementation detail. Adding special interface to stop folks from accidently using OMNode.
 * Please use at your own risk. May corrupt the data integrity.
 */
public interface OMNodeEx extends OMNode {
    public void setNextOMSibling(OMNode node);

    public void setPreviousOMSibling(OMNode previousSibling);

    public void setParent(OMContainer element);

    public void setComplete(boolean state);

    public void setType(int nodeType) throws OMException;

    /**
     * Serializes the node with caching.
     *
     * @param omOutput
     * @throws javax.xml.stream.XMLStreamException
     * @see #serialize(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serialize(org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException;

    /**
     * Serializes the node without caching.
     *
     * @param omOutput
     * @throws XMLStreamException
     */
    public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException;
}
