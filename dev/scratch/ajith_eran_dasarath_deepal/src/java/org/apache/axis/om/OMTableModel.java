package org.apache.axis.om;

import org.apache.axis.om.storage.AttributeRow;
import org.apache.axis.om.storage.ElementRow;
import org.apache.axis.om.storage.GlobalRow;
import org.apache.axis.om.storage.NodeTable;
import org.apache.axis.om.storage.Row;
import org.apache.axis.om.storage.Table;
import org.apache.axis.om.storage.TextRow;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

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
 *
 * @author Ajith Ranabahu
 *         Date: Sep 16, 2004
 *         Time: 6:44:16 PM
 *
 * Note that since the OMTableModel is an abstraction of the XML document, it
 * implements the DOM document interface .
 * At leaset it should, in my opinion!
 */
public class OMTableModel implements Document{

//    private Sequence globalSequence = new Sequence();
//    private Sequence elementSequence = new Sequence();
//    private Sequence textSequence = new Sequence();
//    private Sequence attributeSequence = new Sequence();

    private Table globalTable = new Table();
    private NodeTable elementTable = new NodeTable();
    private NodeTable textTable = new NodeTable();
    private NodeTable attributeTable = new NodeTable();
    // a flag that indicates whether the model is
    // completed or not
    private boolean completed = false;
    private StreamingOmBuilder builder;

    public OMTableModel(StreamingOmBuilder builder) {
        this.builder = builder;
    }

    /**
     * Get elements by parent
     * @param parent
     * @return
     */
    public Object[] getElementsByParent(Object parent){
        return elementTable.getRowsbyParent(parent);
    }

    /**
     * get the attributes by parent
     * @param parent
     * @return
     */
    public Object[] getAttribtByParent(Object parent){
        return attributeTable.getRowsbyParent(parent);
    }

    /**
     * Get the text nodes (text,comments and Cdata)
     * @param parent
     * @return
     */
    public Object[] getTextByParent(Object parent){
        return textTable.getRowsbyParent(parent);
    }

    /**
     * @param URI
     * @param localName
     * @param prefix
     * @param parent
     */
    public Object addElement(String URI, String localName, String prefix, Object parent,StreamingOmBuilder builder) {

        ElementRow eltRow = new ElementRow(builder,this);
        eltRow.setKey(eltRow);//set the object itself as the key
        eltRow.setURI(URI);
        eltRow.setDone(false);
        eltRow.setParent(parent);
        eltRow.setLocalName(localName);
        eltRow.setPrefix(prefix);
        eltRow.setNextSibling(null);

        elementTable.addRow(eltRow);

        //add an entry to the global table as well
        addToGlobalTable(OMConstants.TYPE_ELEMENT, eltRow);

        return eltRow;

    }

    /**
     * @param parent
     * @param child
     */
    public void updateElementParent(Object parent, Object child) {
        ElementRow row = (ElementRow) elementTable.getRowByKey(child);
        row.setParent(parent);

    }

    /**
     * @param key
     */
    public void updateElementDone(Object key) {
        ElementRow row = (ElementRow) elementTable.getRowByKey(key);
        row.setDone(true);
    }

    /**
     * @param value
     * @param parent
     *
     */
    public Object addText(String value, Object parent) {

        return addCharacterData(value,OMConstants.TYPE_TEXT,parent);
    }



    /**
     * @param localName
     * @param prefix
     * @param URI
     * @param value
     * @param parent
     *
     */
    public Object addAttribute(String localName, String prefix, String URI, String value,  Object parent) {

        AttributeRow attRow = new AttributeRow();

        attRow.setKey(attRow);
        attRow.setLocalName(localName);
        attRow.setPrefix(prefix);
        attRow.setURI(URI);
        attRow.setValue(value);
        attRow.setParent(parent);


        attributeTable.addRow(attRow);

        return attRow;
    }

    public void updateAttributeSibling(Object myKey,Object siblingKey){

        AttributeRow attrRow = (AttributeRow)attributeTable.getRowByKey(myKey);
        attrRow.setNextSibling(siblingKey);

    }
    /**
     * @param type
     * @param referenceKey
     */
    private void addToGlobalTable(int type, Object referenceKey) {
        GlobalRow globalRow = new GlobalRow();

        globalRow.setType(type);
        globalRow.setReferenceKey(referenceKey);

        globalTable.addRow(globalRow);

    }

    /**
     * @return
     */
    public int getElementCount() {
        return elementTable.getSize();
    }

    public Row getElement(Object key) {
        return elementTable.getRowByKey(key);
    }

    public Row getElementByIndex(int index) {
        return elementTable.getRowByIndex(index);
    }

    public Object addCData(String value,Object parent){
        return addCharacterData(value,OMConstants.TYPE_CDATA,parent);
    }

    public Object addComment(String value,Object parent){
        return addCharacterData(value,OMConstants.TYPE_COMMENT,parent);
    }

    private Object addCharacterData(String value,int type,Object parentKey){
        TextRow textRow = new TextRow();

        textRow.setKey(textRow);
        textRow.setParent(parentKey);
        textRow.setValue(value);
        textRow.setType(type);
        textTable.addRow(textRow);

        //add an entry to the global table as well
        addToGlobalTable(type, textRow);
        return textRow;
    }

    private Object getRoot(){
        Object[] rootChildren = elementTable.getRowsbyParent(null);

        return (rootChildren==null || rootChildren.length==0)?null:rootChildren[0];
    }
    /**
     * Debug method
     */
    public void dumpTablesToConsole() {
        System.out.println("Global \n");
        System.out.println(globalTable);
        System.out.println("Elements \n");
        System.out.println(elementTable);
        System.out.println("Attribute \n");
        System.out.println(attributeTable);
        System.out.println("text \n");
        System.out.println(textTable);

    }

    /*    Interface Methods */

    public DocumentType getDoctype() {
        return null;
    }

    public DOMImplementation getImplementation() {
        return null;
    }

    public Element getDocumentElement() {
        //return the first element
        while(getRoot()==null){
            try {
                builder.proceed();
            } catch (OMBuilderException e) {
                e.printStackTrace();
                break;
            }
        }
       return (Element)getRoot();

    }

    public Element createElement(String tagName)
            throws DOMException {
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        return null;
    }

    public Text createTextNode(String data) {
        return null;
    }

    public Comment createComment(String data) {
        return null;
    }

    public CDATASection createCDATASection(String data)
            throws DOMException {
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data)
            throws DOMException {
        return null;
    }

    public Attr createAttribute(String name)
            throws DOMException {
        return null;
    }

    public EntityReference createEntityReference(String name)
            throws DOMException {
        return null;
    }

    public NodeList getElementsByTagName(String tagname) {
        return null;
    }

    public Node importNode(Node importedNode,
                           boolean deep)
            throws DOMException {
        return null;
    }

    public Element createElementNS(String namespaceURI,
                                   String qualifiedName)
            throws DOMException {
        return null;
    }

    public Attr createAttributeNS(String namespaceURI,
                                  String qualifiedName)
            throws DOMException {
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI,
                                           String localName) {
        return null;
    }

    public Element getElementById(String elementId) {
        return null;
    }

    public String getNodeName() {
        return null;
    }

    public String getNodeValue()
            throws DOMException {
        return null;
    }

    public void setNodeValue(String nodeValue)
            throws DOMException {
    }

    public short getNodeType() {
        return 0;
    }

    public Node getParentNode() {
        return null;
    }

    public NodeList getChildNodes() {
        return null;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public Document getOwnerDocument() {
        return null;
    }

    public Node insertBefore(Node newChild,
                             Node refChild)
            throws DOMException {
        return null;
    }

    public Node replaceChild(Node newChild,
                             Node oldChild)
            throws DOMException {
        return null;
    }

    public Node removeChild(Node oldChild)
            throws DOMException {
        return null;
    }

    public Node appendChild(Node newChild)
            throws DOMException {
        return null;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node cloneNode(boolean deep) {
        return null;
    }

    public void normalize() {
    }

    public boolean isSupported(String feature,
                               String version) {
        return false;
    }

    public String getNamespaceURI() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public void setPrefix(String prefix)
            throws DOMException {
    }

    public String getLocalName() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
