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
import java.util.List;

/**
 * @author chathura@opensource.lk
 */
public interface WSDLOperation extends ExtensibleComponent {
    /**
     * Method getInfaults
     *
     * @return
     */
    public List getInfaults();

    /**
     * Method setInfaults
     *
     * @param infaults
     */
    public void setInfaults(List infaults);

    /**
     * Method getInputMessage
     *
     * @return
     */
    public MessageReference getInputMessage();

    /**
     * Method setInputMessage
     *
     * @param inputMessage
     */
    public void setInputMessage(MessageReference inputMessage);

    /**
     * Method getMessageExchangePattern
     *
     * @return
     */
    public String getMessageExchangePattern();

    /**
     * Method setMessageExchangePattern
     *
     * @param messageExchangePattern
     */
    public void setMessageExchangePattern(String messageExchangePattern);

    /**
     * Method getName
     *
     * @return
     */
    public QName getName();

    /**
     * Method setName
     *
     * @param name
     */
    public void setName(QName name);

    /**
     * Method getOutfaults
     *
     * @return
     */
    public List getOutfaults();

    /**
     * Method setOutfaults
     *
     * @param outfaults
     */
    public void setOutfaults(List outfaults);

    /**
     * Method getOutputMessage
     *
     * @return
     */
    public MessageReference getOutputMessage();

    /**
     * Method setOutputMessage
     *
     * @param outputMessage
     */
    public void setOutputMessage(MessageReference outputMessage);

    /**
     * Method isSafe
     *
     * @return
     */
    public boolean isSafe();

    /**
     * Method setSafety
     *
     * @param safe
     */
    public void setSafety(boolean safe);

    public String getStyle();

    public void setStyle(String style);

    /**
     * Method getTargetnemespace
     *
     * @return
     */
    public String getTargetnamespace();

    /**
     * Add the InFault to the Components InFaults
     *
     * @param inFault
     */
    public void addInFault(WSDLFaultReference inFault);

    /**
     * Add the OutFault to the Component OutFaults
     *
     * @param outFault
     */
    public void addOutFault(WSDLFaultReference outFault);
}
