
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
package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;

public interface Phase {
    public void addHandler(Handler handler, int index);

    /**
     * add to next empty handler
     *
     * @param handler
     */
    public void addHandler(Handler handler);

    /**
     * If need to see how this works look at the stack!
     *
     * @param msgctx
     * @throws AxisFault
     */

    public String getPhaseName();
    /**
     * @param phaseName The name to set.
     */
    public void setName(String phaseName);
    //   public void preCondition(MessageContext msgCtx)throws AxisFault;
    public void invoke(MessageContext msgCtx) throws AxisFault;
    //    public void postCondition(MessageContext msgCtx)throws AxisFault;
}