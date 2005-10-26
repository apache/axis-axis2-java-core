package org.apache.axis2.description;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.wsdl.WSDLOperation;

import javax.xml.namespace.QName;
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
*
*
*/

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 3, 2005
 * Time: 6:04:11 PM
 */
public class OutOnlyAxisOperation extends AxisOperation {

    public OutOnlyAxisOperation(WSDLOperation wsdlopeartion) {
        super(wsdlopeartion);
    }

    public OutOnlyAxisOperation() {
        super();
    }

    public OutOnlyAxisOperation(QName name) {
        super(name);  
    }

    public void addMessageContext(MessageContext msgContext, OperationContext opContext)
            throws AxisFault {
        if(!opContext.isComplete()){
            opContext.getMessageContexts().put(MESSAGE_LABEL_OUT_VALUE,msgContext);
            opContext.setComplete(true);
        } else {
            throw new AxisFault("Invalid messge addition , operation context completed") ;
        }
    }
}