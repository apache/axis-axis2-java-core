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

import javax.xml.namespace.QName;

import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLOperation;

/**
 * @author chathura@opensource.lk
 */
public class WSDLBindingOperationImpl extends ExtensibleComponentImpl
        implements WSDLBindingOperation {
    /**
     * Field name
     */
    private QName name;

    /**
     * Field operation
     */
    private WSDLOperation operation;

    /**
     * Field input
     */
    private WSDLBindingMessageReference input;

    /**
     * Field output
     */
    private WSDLBindingMessageReference output;

    /**
     * Method getInput
     *
     * @return
     */
    public WSDLBindingMessageReference getInput() {
        return input;
    }

    /**
     * Method setInput
     *
     * @param input
     */
    public void setInput(WSDLBindingMessageReference input) {
        this.input = input;
    }

    /**
     * Method getOperation
     *
     * @return
     */
    public WSDLOperation getOperation() {
        return operation;
    }

    /**
     * Method setOperation
     *
     * @param operation
     */
    public void setOperation(WSDLOperation operation) {
        this.operation = operation;
    }

    /**
     * Method getOutput
     *
     * @return
     */
    public WSDLBindingMessageReference getOutput() {
        return output;
    }

    /**
     * Method setOutput
     *
     * @param output
     */
    public void setOutput(WSDLBindingMessageReference output) {
        this.output = output;
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
}
