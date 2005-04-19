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
 *  Runtime state of the engine
 */
package org.apache.axis.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractMEPContext implements MEPContext {
    protected Map inMessageContextMap;
    protected Map outMessageContextMap;
    protected boolean serverSide = true;

    protected String mepId;

    public String getMepId() {
        return mepId;
    }

    public void setMepId(String mepId) {
        this.mepId = mepId;
    }

    public AbstractMEPContext(boolean serverSide) {
        super();
        outMessageContextMap = new HashMap();
        inMessageContextMap = new HashMap();
        this.serverSide = serverSide;
    }

    /**
     *
     * @param ctxt
     */
    public void addInMessageContext(MessageContext ctxt) {
        inMessageContextMap.put(ctxt.getMessageID(), ctxt);
    }

    /* (non-Javadoc)
     * @see org.apache.axis.context.MEPContext#addOutMessageContext(org.apache.axis.context.MessageContext)
     */
    public void addOutMessageContext(MessageContext msgctx) {
        outMessageContextMap.put(msgctx.getMessageID(), msgctx);

    }

    public MessageContext getInMessageContext(String messageID) {

        return (MessageContext) inMessageContextMap.get(messageID);
    }

    public List getInMessageContexts() {
        return new ArrayList(inMessageContextMap.values());
    }

    /* (non-Javadoc)
     * @see org.apache.axis.context.MEPContext#getOutMessageContext(java.lang.String)
     */
    public MessageContext getOutMessageContext(String messageID) {
        return (MessageContext) outMessageContextMap.get(messageID);
    }

    public List getOutMessageContexts() {
        return new ArrayList(outMessageContextMap.values());
    }

    public abstract boolean isComplete() ;

}
