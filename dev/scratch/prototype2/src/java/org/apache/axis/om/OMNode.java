package org.apache.axis.om;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
public interface OMNode {
    //==================================================================================

    // Followings are the codes for node types. Extracted these from DOM API
    /**
     * The node is an <code>Element</code>.
     */
    public static final short ELEMENT_NODE = 1;
    /**
     * The node is an <code>Attr</code>.
     */
    public static final short ATTRIBUTE_NODE = 2;
    /**
     * The node is a <code>Text</code> node.
     */
    public static final short TEXT_NODE = 3;
    /**
     * The node is a <code>CDATASection</code>.
     */
    public static final short CDATA_SECTION_NODE = 4;

    /**
     * The node is a <code>Comment</code>.
     */
    public static final short COMMENT_NODE = 8;
    /**
     * The node is a <code>Document</code>.
     */
    public static final short DOCUMENT_NODE = 9;

    //==================================================================================

    /**
     * This method should return the immediate parent of the node.
     * Parent is always an Element
     *
     * @return
     * @throws OMException
     */
    public OMElement getParent() throws OMException;

    public void setParent(OMElement element);

    /**
     * This will give the next sibling. This can be an OMAttribute for OMAttribute or OMText or OMELement for others.
     *
     * @return
     * @throws OMException
     */
    public OMNode getNextSibling() throws OMException;

    public void setNextSibling(OMNode node);

    /**
     * This will return the literal value of the node.
     * OMText --> the text
     * OMElement --> local name of the element in String format
     * OMAttribute --> the value of the attribue
     *
     * @return
     * @throws OMException
     */
    public String getValue() throws OMException;

    public void setValue(String value);

    /**
     * this will indicate whether parser has parsed this information item completely or not.
     * If somethings info are not available in the item, one has to check this attribute to make sure that, this
     * item has been parsed completely or not.
     *
     * @return
     */
    public boolean isComplete();

    public void setComplete(boolean state);

    /**
     * This will remove this information item and its children, from the model completely
     *
     * @throws OMException
     */
    public void detach() throws OMException;

    /**
     * This will insert a sibling just after the current information item.
     *
     * @param sibling
     * @throws OMException
     */
    public void insertSiblingAfter(OMNode sibling) throws OMException;

    /**
     * This will insert a sibling just before the current information item
     *
     * @param sibling
     * @throws OMException
     */
    public void insertSiblingBefore(OMNode sibling) throws OMException;

    /**
     * This is to get the type of node, as this is the super class of all the nodes
     * @return
     * @throws OMException
     */
    public short getType() throws OMException;

    public void setType(short nodeType) throws OMException;

    /**
     * get the previous sibling
     * @return
     */
    public OMNode getPreviousSibling();

    /**
     * Set the previous sibling
     * @param previousSibling
     */
    public void setPreviousSibling(OMNode previousSibling);

    
    /**
     * 
     * @param writer
     * @param cache
     * @throws XMLStreamException
     */
    public void serialize(XMLStreamWriter writer, boolean cache) throws XMLStreamException ;

}
