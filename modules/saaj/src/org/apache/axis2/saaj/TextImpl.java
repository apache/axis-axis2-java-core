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

import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMText;
import org.w3c.dom.DOMException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.Text;

/**
 * @author Ashutosh Shahi ashutosh.shahi@gmail.com
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class TextImpl extends NodeImpl implements Text {

    private OMText omText;

    public TextImpl(String s) {
        //super();
        omNode =
                omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(s);
    }

    public TextImpl(SOAPElementImpl parent, String s) throws SOAPException {
        //super();
        //super.setParentElement(parent);
        OMElement par = parent.getOMElement();
        omNode =
                omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(par, s);
    }

    public TextImpl(org.w3c.dom.CharacterData data) {
        if (data == null) {
            throw new IllegalArgumentException("Text value may not be null.");
        }
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(data.getData());
    }

    /*Overridden Method*/
    public SOAPElement getParentElement() {
        OMElement parent = (OMElement) omText.getParent();
        return new SOAPElementImpl(parent);
    }

    /*Overridden Method*/
    public void setParentElement(SOAPElement parent) throws SOAPException {
        OMElement omParent = ((SOAPElementImpl) parent).getOMElement();
        omText.setParent(omParent);
    }

    /*Overridden Method*/
    public String getValue() {
        return omText.getText();
    }

    public boolean isComment() {

        String temp = omText.getText();
        return temp.startsWith("<!--") && temp.endsWith("-->");
    }

    /**
     * Implementation of DOM TEXT Interface
     * *************************************************************
     */


    public org.w3c.dom.Text splitText(int offset) throws DOMException {

        String temp = omText.getText();
        int length = temp.length();
        String tailData = temp.substring(offset);
        temp = temp.substring(0, offset);
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(temp);
        TextImpl tailText = new TextImpl(tailData);
        org.w3c.dom.Node myParent = getParentNode();
        if (myParent != null) {
            org.w3c.dom.NodeList brothers = myParent.getChildNodes();
            for (int i = 0; i < brothers.getLength(); i++) {
                if (brothers.item(i).equals(this)) {
                    myParent.insertBefore(tailText, this);
                    return tailText;
                }
            }
        }
        return tailText;
    }


    public int getLength() {

        return omText.getText().length();
    }


    public void deleteData(int offset, int count) throws DOMException {

        String temp = omText.getText();
        StringBuffer subString = new StringBuffer(temp.substring(0, offset));
        if (temp.length() - offset >= count - offset)
            subString = subString.append(temp.substring(offset + count));
        temp = subString.toString();
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(temp);

    }


    public String getData() throws DOMException {

        return omText.getText();
    }


    public String substringData(int offset, int count) throws DOMException {

        String temp = omText.getText();
        if (temp.length() - offset >= count - offset)
            return temp.substring(offset, count);
        else
            return temp.substring(offset);
    }


    public void replaceData(int offset, int count, String arg)
            throws DOMException {

        deleteData(offset, count);
        StringBuffer temp = new StringBuffer(omText.getText());
        temp.append(arg);
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(temp.toString());
    }


    public void insertData(int offset, String arg) throws DOMException {

        if (offset < 0 || offset > omText.getText().length())
            throw new DOMException(DOMException.INDEX_SIZE_ERR, "");
        StringBuffer temp = new StringBuffer(
                omText.getText().substring(0, offset));
        temp = temp.append(arg);
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(temp.toString());
    }


    public void appendData(String arg) throws DOMException {

        StringBuffer temp = new StringBuffer(omText.getText());
        temp = temp.append(arg);
        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(temp.toString());

    }


    public void setData(String arg) throws DOMException {

        omText =
                org.apache.axis2.om.OMAbstractFactory.getOMFactory()
                .createText(arg);
    }

}
