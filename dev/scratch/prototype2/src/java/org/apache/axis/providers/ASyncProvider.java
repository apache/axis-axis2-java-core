/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.providers;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Provider;
import org.apache.axis.engine.context.MessageContext;
import org.apache.axis.handlers.AbstractHandler;

/**
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class ASyncProvider extends AbstractHandler{
    private Provider doworkProvider;
    public ASyncProvider(Provider doworkProvider){
        this.doworkProvider = doworkProvider;
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        msgContext.getGlobalContext().getQueue().submitAJob(msgContext);
    }
}
