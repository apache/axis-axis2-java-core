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
package org.apache.axis.clientapi;

/**
 * Class Callback
 */
public abstract class Callback {
    /**
     * Field complete
     */
    private boolean complete = false;

    /**
     * Field result
     */
    private AsyncResult result;

    /**
     * Method onComplete
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * Method reportError
     *
     * @param e
     */
    public abstract void reportError(Exception e);

    /**
     * Method isComplete
     *
     * @return
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * Method setComplete
     *
     * @param complete
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Method getResult
     *
     * @return
     */
    public AsyncResult getResult() {
        return result;
    }

    /**
     * Method setResult
     *
     * @param result
     */
    public void setResult(AsyncResult result) {
        this.result = result;
    }
}
