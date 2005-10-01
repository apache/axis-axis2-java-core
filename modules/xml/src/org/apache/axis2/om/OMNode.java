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

import org.apache.axis2.om.impl.OMOutputImpl;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Defines the the base interface used by most of the XML object model within Axis.
 *
 * <p>This tree model for XML captures the idea of deferring the construction of child nodes
 * until they are needed.  The <code>isComplete</code> function identifies whether or not
 * a particular node has been fully parsed.  A node may not be fully parsed, for example, if
 * all of the children of an element have not yet been parsed.</p>
 *
 * <p>In comparison to DOM, in this model, you will not find document fragments, or entities.
 * In addition, while {@link OMDocument} and {@link OMAttribute} exist, neither is an extension
 * of <code>OMNode</code>.
 * </p>
 */
public interface OMNode {
    /**
     * The node is an <code>Element</code>.
     *
     * @see #getType()
     */
    public static final short ELEMENT_NODE = 1;

    /**
     * The node is a <code>Text</code> node.
     *
     * @see #getType()
     */
    public static final short TEXT_NODE = XMLStreamConstants.CHARACTERS;

    /**
     * The node is a <code>CDATASection</code>.
     *
     * @see #getType()
     */
    public static final short CDATA_SECTION_NODE = XMLStreamConstants.CDATA;

    /**
     * The node is a <code>Comment</code>.
     *
     * @see #getType()
     */
    public static final short COMMENT_NODE = XMLStreamConstants.COMMENT;

    /**
     * This node is a <code>DTD</code>.
     *
     * @see #getType()
     */
    public static final short DTD_NODE = XMLStreamConstants.DTD;

    /**
     * This node is a <code>ProcessingInstruction</code>.
     *
     * @see #getType()
     */
    public static final short PI_NODE = XMLStreamConstants.PROCESSING_INSTRUCTION;

    /**
     * This node is a <code>Entity Reference</code>.
     *
     * @see #getType()
     */
    public static final short ENTITY_REFERENCE_NODE = XMLStreamConstants.ENTITY_REFERENCE;

    /**
     * This node is a <code>Entity Reference</code>.
     *
     * @see #getType()
     */
    public static final short SPACE_NODE = XMLStreamConstants.SPACE;

    /**
     * Returns the parent containing node.
     *
     * <p>Returns the parent container, which may be either an {@link OMDocument} or {@link OMElement}.
     *
     * @return The {@link OMContainer} of the node.
     */
    public OMContainer getParent();

    /**
     * Returns the next sibling in document order.
     *
     * @return The next sibling in document order.
     */
    public OMNode getNextOMSibling() throws OMException;

    /**
     * this will indicate whether parser has parsed this information item completely or not.
     * If somethings info are not available in the item, one has to check this attribute to make sure that, this
     * item has been parsed completely or not.
     *
     * @return boolean
     */
    public boolean isComplete();

    /**
     * Removes a node (and all of its children) from its containing parent.
     *
     * <p>Removes a node from its parent.  Partially complete nodes will be completed before
     * they are detached from the model.  A node cannot be detached until its next sibling
     * has been identified, so that the next sibling and parent can be updated appropriately.
     * </p>
     *
     * @throws OMException If a node is not complete, the detach can trigger further
     * parsing, which may cause an exception.
     */
    public OMNode detach() throws OMException;

    /**
     * Discards a node.
     *
     * <p>Discard goes to the parser level and if the element is not completely built, then it will be
     * completely skipped at the parser level.</p>
     *
     * @throws OMException
     */
    public void discard() throws OMException;

    /**
     * Inserts a new sibling after the current node.
     *
     * @param sibling The node that will be added after the current node.
     *
     * @throws OMException
     */
    public void insertSiblingAfter(OMNode sibling) throws OMException;

    /**
     * This will insert a sibling just before the current node.
     *
     * @param sibling The node that will be added before the current node.
     * @throws OMException
     */
    public void insertSiblingBefore(OMNode sibling) throws OMException;

    /**
     * Returns the type of node.
     *
     * @return One of {@link #ELEMENT_NODE}, {@link #TEXT_NODE}, {@link #CDATA_SECTION_NODE}, {@link #COMMENT_NODE},
     *  {@link #DTD_NODE}, {@link #PI_NODE}, {@link #ENTITY_REFERENCE_NODE}, {@link #SPACE_NODE},
     * or {@link #TEXT_NODE}.
     */
    public int getType();

    /**
     * get the previous sibling
     *
     * @return node
     */
    public OMNode getPreviousOMSibling();

    /**
     * Serialize the node with caching
     *
     * @param xmlWriter
     * @throws XMLStreamException
     * @see #serializeWithCache(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serializeWithCache(XMLStreamWriter xmlWriter)
            throws XMLStreamException;

    /**
     * Serialize the node with caching
     *
     * @param omOutput
     * @throws XMLStreamException
     * @see #serializeWithCache(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serializeWithCache(org.apache.axis2.om.impl.OMOutputImpl omOutput)
            throws XMLStreamException;

    /**
     * Serialize the node without caching
     *
     * @param xmlWriter
     * @throws XMLStreamException
     * @see #serialize(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serialize(XMLStreamWriter xmlWriter) throws XMLStreamException;

    /**
     * Serialize the node without caching
     *
     * @param omOutput
     * @throws XMLStreamException
     * @see #serialize(org.apache.axis2.om.impl.OMOutputImpl)
     */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException;

    /**
     * Builds itself
     */
    public void build();
}
