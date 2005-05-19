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
package org.apache.axis.handlers;

import org.apache.axis.context.MessageContext;
import org.apache.axis.description.HandlerDescription;
import org.apache.axis.description.Parameter;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;

import javax.xml.namespace.QName;

/**
 * Class AbstractHandler
 */
public abstract class AbstractHandler implements Handler {

    /**
     * Field EMPTY_HANDLER_METADATA
     */
    private static HandlerDescription EMPTY_HANDLER_METADATA =
            new HandlerDescription();

    /**
     * Field handlerDesc
     */
    protected HandlerDescription handlerDesc;

    /**
     * Constructor AbstractHandler
     */
    public AbstractHandler() {
        handlerDesc = EMPTY_HANDLER_METADATA;
    }

    /**
     * Method getName
     *
     * @return
     */
    public QName getName() {
        return handlerDesc.getName();
    }

    /**
     * Method revoke
     *
     * @param msgContext
     */
    public void revoke(MessageContext msgContext) {
    }

    /**
     * Method cleanup
     *
     * @throws AxisFault
     */
    public void cleanup() throws AxisFault {
    }

    /**
     * Method getParameter
     *
     * @param name
     * @return
     */
    public Parameter getParameter(String name) {
        return handlerDesc.getParameter(name);
    }

    /**
     * Method init
     *
     * @param handlerdesc
     */
    public void init(HandlerDescription handlerdesc) {
        this.handlerDesc = handlerdesc;
    }

    /**
     * To get the phaseRule of a handler it is required to get the HnadlerDescription of the handler
     * so the argumnet pass when it call return as HnadlerDescription
     *
     * @return
     */
    public HandlerDescription getHandlerDesc() {
        return handlerDesc;
    }
}
