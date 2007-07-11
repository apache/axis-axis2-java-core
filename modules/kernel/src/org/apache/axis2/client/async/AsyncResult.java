/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.client.async;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.MessageContext;

/**
 * This class holds the results of an asynchronous invocation. The Axis2
 * engine returns an instance of this class via the {@link
 * Callback#onComplete(AsyncResult)} method when the operation completes
 * successfully.
 *
 * @deprecated please see org.apache.axis2.client.async.AxisCallback.
 */
public class AsyncResult {

    /**
     * Message context that supplies the result information.
     */
    private MessageContext result;

    /**
     * Constructor.
     *
     * @param result message context providing result information
     *               (<code>null</code> if no response)
     */
    public AsyncResult(MessageContext result) {
        this.result = result;
    }

    /**
     * Get the SOAP Envelope for the response message.
     *
     * @return Envelope (<code>null</code> if none)
     */
    public SOAPEnvelope getResponseEnvelope() {
        if (result != null) {
            return result.getEnvelope();
        } else {
            return null;
        }
    }

    /**
     * Get the complete message context for the response.
     *
     * @return context (<code>null</code> if none)
     */
    public MessageContext getResponseMessageContext() {
        return result;
    }
}
