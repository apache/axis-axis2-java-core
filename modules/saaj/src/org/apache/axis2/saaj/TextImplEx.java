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
package org.apache.axis2.saaj;

import org.apache.axis2.om.DOOMAbstractFactory;
import org.apache.axis2.om.OMContainer;
import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.OMOutputImpl;
import org.apache.axis2.om.impl.dom.TextImpl;
import org.w3c.dom.DOMException;

import javax.xml.soap.Text;
import javax.xml.stream.XMLStreamException;

public class TextImplEx extends NodeImplEx implements Text {

    //TODO: assign textNode

    private TextImpl textNode;

    public TextImplEx(String data) {
        textNode = (TextImpl) DOOMAbstractFactory.getOMFactory().createText(data);
    }
    /*public TextImplEx(SOAPElementImpl parent, String s) throws SOAPException {
        //super();
        //super.setParentElement(parent);
        OMElement par = parent.getOMElement();
        omNode =
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                        .createText(par, s);
    }

    public TextImplEx(org.w3c.dom.CharacterData data) {
        if (data == null) {
            throw new IllegalArgumentException("Text value may not be null.");
        }
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                        .createText(data.getData());
    }

    public TextImplEx(OMText omText) {
        omNode = this.omText = omText;
    }*/

    /**
     * Retrieves whether this <CODE>Text</CODE> object
     * represents a comment.
     *
     * @return <CODE>true</CODE> if this <CODE>Text</CODE> object is
     *         a comment; <CODE>false</CODE> otherwise
     */
    public boolean isComment() {
        String value = textNode.getText();
        return value.startsWith("<!--") && value.endsWith("-->");
    }

    /**
     * The name of this node, depending on its type; see the table above.
     */
    public String getNodeName() {
        return textNode.getNodeName();
    }

    /**
     * A code representing the type of the underlying object, as defined above.
     */
    public short getNodeType() {
        return textNode.getNodeType();
    }

    /**
     * Breaks this node into two nodes at the specified <code>offset</code>,
     * keeping both in the tree as siblings. After being split, this node
     * will contain all the content up to the <code>offset</code> point. A
     * new node of the same type, which contains all the content at and
     * after the <code>offset</code> point, is returned. If the original
     * node had a parent node, the new node is inserted as the next sibling
     * of the original node. When the <code>offset</code> is equal to the
     * length of this node, the new node has no data.
     *
     * @param offset The 16-bit unit offset at which to split, starting from
     *               <code>0</code>.
     * @return The new node, of the same type as this node.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified offset is negative or greater
     *                      than the number of 16-bit units in <code>data</code>.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public org.w3c.dom.Text splitText(int offset) throws DOMException {
        return textNode.splitText(offset);
    }

    /**
     * The character data of the node that implements this interface. The DOM
     * implementation may not put arbitrary limits on the amount of data
     * that may be stored in a <code>CharacterData</code> node. However,
     * implementation limits may mean that the entirety of a node's data may
     * not fit into a single <code>DOMString</code>. In such cases, the user
     * may call <code>substringData</code> to retrieve the data in
     * appropriately sized pieces.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *                      fit in a <code>DOMString</code> variable on the implementation
     *                      platform.
     */
    public String getData() throws DOMException {
        return textNode.getData();
    }

    /**
     * The character data of the node that implements this interface. The DOM
     * implementation may not put arbitrary limits on the amount of data
     * that may be stored in a <code>CharacterData</code> node. However,
     * implementation limits may mean that the entirety of a node's data may
     * not fit into a single <code>DOMString</code>. In such cases, the user
     * may call <code>substringData</code> to retrieve the data in
     * appropriately sized pieces.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised when the node is readonly.
     * @throws DOMException DOMSTRING_SIZE_ERR: Raised when it would return more characters than
     *                      fit in a <code>DOMString</code> variable on the implementation
     *                      platform.
     */
    public void setData(String data) throws DOMException {
        textNode.setData(data);
    }

    /**
     * Extracts a range of data from the node.
     *
     * @param offset Start offset of substring to extract.
     * @param count  The number of 16-bit units to extract.
     * @return The specified substring. If the sum of <code>offset</code> and
     *         <code>count</code> exceeds the <code>length</code>, then all 16-bit
     *         units to the end of the data are returned.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *                      negative or greater than the number of 16-bit units in
     *                      <code>data</code>, or if the specified <code>count</code> is
     *                      negative.
     *                      <br>DOMSTRING_SIZE_ERR: Raised if the specified range of text does
     *                      not fit into a <code>DOMString</code>.
     */
    public String substringData(int offset, int count) throws DOMException {
        return textNode.substringData(offset, count);
    }

    /**
     * Append the string to the end of the character data of the node. Upon
     * success, <code>data</code> provides access to the concatenation of
     * <code>data</code> and the <code>DOMString</code> specified.
     *
     * @param value The <code>DOMString</code> to append.
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void appendData(String value) throws DOMException {
        textNode.appendData(value);
    }

    /**
     * Insert a string at the specified 16-bit unit offset.
     *
     * @param offset The character offset at which to insert.
     * @param data   The <code>DOMString</code> to insert.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *                      negative or greater than the number of 16-bit units in
     *                      <code>data</code>.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void insertData(int offset, String data) throws DOMException {
        textNode.insertData(offset, data);
    }

    /**
     * Remove a range of 16-bit units from the node. Upon success,
     * <code>data</code> and <code>length</code> reflect the change.
     *
     * @param offset The offset from which to start removing.
     * @param count  The number of 16-bit units to delete. If the sum of
     *               <code>offset</code> and <code>count</code> exceeds
     *               <code>length</code> then all 16-bit units from <code>offset</code>
     *               to the end of the data are deleted.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *                      negative or greater than the number of 16-bit units in
     *                      <code>data</code>, or if the specified <code>count</code> is
     *                      negative.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void deleteData(int offset, int count) throws DOMException {
        textNode.deleteData(offset, count);
    }

    /**
     * Replace the characters starting at the specified 16-bit unit offset
     * with the specified string.
     *
     * @param offset The offset from which to start replacing.
     * @param count  The number of 16-bit units to replace. If the sum of
     *               <code>offset</code> and <code>count</code> exceeds
     *               <code>length</code>, then all 16-bit units to the end of the data
     *               are replaced; (i.e., the effect is the same as a <code>remove</code>
     *               method call with the same range, followed by an <code>append</code>
     *               method invocation).
     * @param data   The <code>DOMString</code> with which the range must be
     *               replaced.
     * @throws DOMException INDEX_SIZE_ERR: Raised if the specified <code>offset</code> is
     *                      negative or greater than the number of 16-bit units in
     *                      <code>data</code>, or if the specified <code>count</code> is
     *                      negative.
     *                      <br>NO_MODIFICATION_ALLOWED_ERR: Raised if this node is readonly.
     */
    public void replaceData(int offset, int count, String data) throws DOMException {
        textNode.replaceData(offset, count, data);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.impl.OMNodeEx#setParent(org.apache.axis2.om.OMContainer)
      */
    public void setParent(OMContainer element) {
        textNode.setParent(element);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#getParent()
      */
    public OMContainer getParent() {
        return textNode.getParent();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#discard()
      */
    public void discard() throws OMException {
        textNode.discard();
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#serialize(org.apache.axis2.om.impl.OMOutputImpl)
      */
    public void serialize(OMOutputImpl omOutput) throws XMLStreamException {
        textNode.serialize(omOutput);
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.om.OMNode#serializeAndConsume(org.apache.axis2.om.impl.OMOutputImpl)
      */
    public void serializeAndConsume(OMOutputImpl omOutput) throws XMLStreamException {
        textNode.serializeAndConsume(omOutput);
    }

    /**
     * Retrieve the text value (data) of this
     *
     * @return The text value (data) of this
     */
    public String getValue() {
        return textNode.getData();
    }

    public String toString() {
        return getValue();
    }
}
