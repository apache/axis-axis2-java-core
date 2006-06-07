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

package org.apache.axis2.client.async;

/**
 * This Class is the abstract representation of a callback and is called at the completion of an
 * asynchronous invocation.
 */
public abstract class Callback {

    /**
     * Field complete
     */
    private boolean complete;

    /**
     * This method is invoked by Axis Engine once the asynchronous operation has completed sucessfully.
     *
     * @param result
     */
    public abstract void onComplete(AsyncResult result);

    /**
     * This method is called by Axis Engine if the asynchronous operation fails.
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
}
