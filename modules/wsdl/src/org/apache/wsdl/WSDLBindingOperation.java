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
public interface WSDLBindingOperation extends ExtensibleComponent {
    /**
     * Method getInput
     *
     * @return
     */
    public WSDLBindingMessageReference getInput();

    /**
     * Method setInput
     *
     * @param input
     */
    public void setInput(WSDLBindingMessageReference input);

    /**
     * Method getOperation
     *
     * @return
     */
    public WSDLOperation getOperation();

    /**
     * Method setOperation
     *
     * @param operation
     */
    public void setOperation(WSDLOperation operation);

    /**
     * Method getOutput
     *
     * @return
     */
    public WSDLBindingMessageReference getOutput();

    /**
     * Method setOutput
     *
     * @param output
     */
    public void setOutput(WSDLBindingMessageReference output);

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
     * Add the InFault to the Components InFaults
     *
     * @param inFault
     */
    public void addInFault(WSDLBindingFault inFault);

    /**
     * Add the OutFault to the Component OutFaults
     *
     * @param outFault
     */
    public void addOutFault(WSDLBindingFault outFault);

    /**
     * @return The list of infault <code>WSDLBindingFault</code>s
     */
    public List getInfaults();

    /**
     * Sets the list of infault <code>WSDLBindingFault</code>s
     *
     * @param infaults
     */
    public void setInfaults(List infaults);

    /**
     * @return The list of Outfaults <code>WSDLBindingFault</code>s
     */
    public List getOutfaults();

    /**
     * Sets the list of outfault <code>WSDLBindingFault</code>s
     *
     * @param outfaults
     */
    public void setOutfaults(List outfaults);
}
