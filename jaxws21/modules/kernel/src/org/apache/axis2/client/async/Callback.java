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

/**
 * Base class for asynchronous client callback handler. The application code
 * needs to create an instance of this class (actually an instance of a
 * subclass, since this base class cannot be used directly) and pass it to the
 * generated startXXX client stub method when initiating an asynchronous
 * operation. The Axis2 code then calls the appropriate methods of this class
 * {@link #setComplete(boolean)}, and either {@link #onComplete(AsyncResult)}
 * or {@link #onError(Exception)} when the operation is completed.
 *
 * @deprecated Please use AxisCallback instead, this class is deprecated as of 1.3
 */
public abstract class Callback {

    /**
     * Field complete
     */
    private boolean complete;

    /**
     * Method is invoked by Axis2 once the asynchronous operation has completed
     * successfully.
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * Method invoked by Axis2 if the asynchronous operation fails.
     *
     * @param e
     */
    public abstract void onError(Exception e);

    /**
     * Returns true if the asynchronous operation has completed, false otherwise. Typically this is
     * used for polling. e.g.
     * <code>
     * <pre>
     *          while(!callback.isComplete()){
     *             Thread.sleep(1000);
     *          }
     *          do whatever u need to do
     *      </pre>
     * </code>
     *
     * @return boolean
     */
    public synchronized boolean isComplete() {
        return complete;
    }

    /**
     * Method invoked by Axis2 to set the completion state of the operation.
     *
     * @param complete
     */
    public final synchronized void setComplete(boolean complete) {
        this.complete = complete;
        notify();
    }
}
