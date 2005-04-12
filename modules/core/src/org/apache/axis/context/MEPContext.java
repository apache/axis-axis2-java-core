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

import java.util.HashMap;
import java.util.Map;

public class MEPContext  extends AbstractContext{
    private Map messageContextMap;
    private String mepId;

    public String getMepId() {
        return mepId;
    }

    public void setMepId(String mepId) {
        this.mepId = mepId;
    }

    public MEPContext() {
        super();
        messageContextMap = new HashMap();
    }

    /**
     *
     * @param ctxt
     */
    public void addMessageContext(MessageContext ctxt){
         messageContextMap.put(ctxt.getMessageID(),ctxt);
    }

    /**
     *
     * @param messageId
     * @return
     */
    public MessageContext getMessageContext(String messageId){
        return (MessageContext)messageContextMap.get(messageId);
    }


    public MessageContext removeMessageContext(MessageContext ctxt){
        messageContextMap.remove(ctxt.getMessageID());
        return ctxt;
    }
}
