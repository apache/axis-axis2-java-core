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
 */
package org.apache.wsdl.impl;

import org.apache.wsdl.MessageReference;
import org.apache.wsdl.WSDLOperation;

import javax.xml.namespace.QName;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chathura Herath
 */
public class WSDLOperationImpl extends ExtensibleComponentImpl implements WSDLOperation {

    private QName name;

    /**
     * URI of the MEP
     */
    private String messageExchangePattern;
    private MessageReference inputMessage;
    private MessageReference outputMessage;
    private List infaults = new LinkedList();
    private List outfaults = new LinkedList();
    //value of parent if not specified
    private int style;

    private boolean safety = false;


    public List getInfaults() {
        return infaults;
    }

    public void setInfaults(List infaults) {
        this.infaults = infaults;
    }

    public MessageReference getInputMessage() {
        return inputMessage;
    }

    public void setInputMessage(MessageReference inputMessage) {
        this.inputMessage = inputMessage;
    }

    public String getMessageExchangePattern() {
        return messageExchangePattern;
    }

    public void setMessageExchangePattern(String messageExchangePattern) {
        this.messageExchangePattern = messageExchangePattern;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public List getOutfaults() {
        return outfaults;
    }

    public void setOutfaults(List outfaults) {
        this.outfaults = outfaults;
    }

    public MessageReference getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(MessageReference outputMessage) {
        this.outputMessage = outputMessage;
    }

    public boolean isSafe() {
        return safety;
    }

    public void setSafety(boolean safe) {
        this.safety = safe;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    /**
     * Will return the Namespace of the QName of this <code>WSDLOperation</code>. Will return null if not set.
     */
    public String getTargetnemespace() {
        if (null != this.name) return this.name.getNamespaceURI();
        return null;
    }

}
