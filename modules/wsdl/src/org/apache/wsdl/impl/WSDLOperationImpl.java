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
package org.apache.wsdl.impl;

import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLOperation;

/**
 * @author Chathura Herath
 */
public class WSDLOperationImpl extends ExtensibleComponentImpl
        implements WSDLOperation {
    /**
     * Field name
     */
    private QName name;

    /**
     * URI of the MEP
     */
    private String messageExchangePattern;

    /**
     * Field inputMessage
     */
    private MessageReference inputMessage;

    /**
     * Field outputMessage
     */
    private MessageReference outputMessage;

    /**
     * Field infaults
     */
    private List infaults = new LinkedList();

    /**
     * Field outfaults
     */
    private List outfaults = new LinkedList();

    // value of parent if not specified

    /**
     * Field style
     */
    private String style;

    /**
     * Field safety
     */
    private boolean safety = false;

    /**
     * Method getInfaults
     *
     * @return
     */
    public List getInfaults() {
        return infaults;
    }

    /**
     * Method setInfaults
     *
     * @param infaults
     */
    public void setInfaults(List infaults) {
        this.infaults = infaults;
    }

    /**
     * Method getInputMessage
     *
     * @return
     */
    public MessageReference getInputMessage() {
        return inputMessage;
    }

    /**
     * Method setInputMessage
     *
     * @param inputMessage
     */
    public void setInputMessage(MessageReference inputMessage) {
        this.inputMessage = inputMessage;
    }

    /**
     * Method getMessageExchangePattern
     *
     * @return
     */
    public String getMessageExchangePattern() {
        return messageExchangePattern;
    }

    /**
     * Method setMessageExchangePattern
     *
     * @param messageExchangePattern
     */
    public void setMessageExchangePattern(String messageExchangePattern) {
        this.messageExchangePattern = messageExchangePattern;
    }

    /**
     * Method getName
     *
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Method getOutfaults
     *
     * @return
     */
    public List getOutfaults() {
        return outfaults;
    }

    /**
     * Method setOutfaults
     *
     * @param outfaults
     */
    public void setOutfaults(List outfaults) {
        this.outfaults = outfaults;
    }

    /**
     * Method getOutputMessage
     *
     * @return
     */
    public MessageReference getOutputMessage() {
        return outputMessage;
    }

    /**
     * Method setOutputMessage
     *
     * @param outputMessage
     */
    public void setOutputMessage(MessageReference outputMessage) {
        this.outputMessage = outputMessage;
    }

    /**
     * Method isSafe
     *
     * @return
     */
    public boolean isSafe() {
        return safety;
    }

    /**
     * Method setSafety
     *
     * @param safe
     */
    public void setSafety(boolean safe) {
        this.safety = safe;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    /**
     * Will return the Namespace of the QName of this <code>WSDLOperation</code>. Will return null if not set.
     *
     * @return
     */
    public String getTargetnemespace() {
        if (null != this.name) {
            return this.name.getNamespaceURI();
        }
        return null;
    }
}
