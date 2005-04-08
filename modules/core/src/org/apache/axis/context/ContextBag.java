package org.apache.axis.context;

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
public class ContextBag {

    private EngineContext engineContext;
    private ServiceContext serviceContext;
    private OperationContext operationContext;
    private MEPContext mepContext;
    private MessageContext messageContext;

    public EngineContext getEngineContext() {
        return engineContext;
    }

    public void setEngineContext(EngineContext engineContext) {
        this.engineContext = engineContext;
    }

    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public OperationContext getOperationContext() {
        return operationContext;
    }

    public void setOperationContext(OperationContext operationContext) {
        this.operationContext = operationContext;
    }

    public MEPContext getMepContext() {
        return mepContext;
    }

    public void setMepContext(MEPContext mepContext) {
        this.mepContext = mepContext;
    }

    public MessageContext getMessageContext() {
        return messageContext;
    }

    public void setMessageContext(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

}
