/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.wsdl.WSDLBindingMessageReference;
import org.apache.wsdl.WSDLBindingOperation;
import org.apache.wsdl.WSDLOperation;

import javax.xml.namespace.QName;

/**
 * @author chathura@opensource.lk
 */
public class WSDLBindingOperationImpl extends ExtensibleComponentImpl implements WSDLBindingOperation {

    private QName name;

    private WSDLOperation operation;

    private WSDLBindingMessageReference input;

    private WSDLBindingMessageReference output;


    public WSDLBindingMessageReference getInput() {
        return input;
    }

    public void setInput(WSDLBindingMessageReference input) {
        this.input = input;
    }

    public WSDLOperation getOperation() {
        return operation;
    }

    public void setOperation(WSDLOperation operation) {
        this.operation = operation;
    }

    public WSDLBindingMessageReference getOutput() {
        return output;
    }

    public void setOutput(WSDLBindingMessageReference output) {
        this.output = output;
    }


    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }
}
