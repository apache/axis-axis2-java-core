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
package org.apache.axis2.client;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.soap.SOAPEnvelope;

/**
 * This class represents the results of an asynchronous invocation. The axis engine 
 * reports back the results in this object via the callback function. 
 */
public class AsyncResult {

    public AsyncResult(MessageContext result) {
        this.result = result;
    }
    /**
     * Field result
     */
    private MessageContext result;

    /**
     * @return SOAPEnvelope
     */
    public SOAPEnvelope getResponseEnvelope() {
        if (result != null) {
            return result.getEnvelope();
        } else {
            return null;
        }

    }

     /**
     * @return MessageContext
     */
    public MessageContext getResponseMessageContext() {
        return result;
    }
}
