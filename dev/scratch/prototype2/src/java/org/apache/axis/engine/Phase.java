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
package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.handlers.AbstractHandler;

import java.util.ArrayList;
import java.util.Stack;

/**
 * <p>This is Phase, a orderd collection of Handlers.
 * seems this is Handler Chain with order.</p>
 * Should this exttends Hanlders?
 *
 * @author Srinath Perera (hemapani@opensource.lk)
 */
public class Phase extends AbstractHandler implements Handler {
    private String phaseName;
    private ArrayList handlers;

    public Phase(String phaseName) {
        handlers = new ArrayList();
        this.phaseName = phaseName;
    }

    public void addHandler(Handler handler, int index) {
        handlers.add(index, handler);
    }

    /**
     * add to next empty handler
     *
     * @param handler
     */
    public void addHandler(Handler handler) {
        handlers.add(handler);
    }

    /**
     * If need to see how this works look at the stack!
     *
     * @param msgctx
     * @throws AxisFault
     */

    public void invoke(MessageContext msgctx) throws AxisFault {
        Stack executionStack = new Stack();
        try {
            for (int i = 0; i < handlers.size(); i++) {
                Handler handler = (Handler) handlers.get(i);
                if (handler != null) {
                    executionStack.push(handler);
                    handler.invoke(msgctx);
                }
            }
        } catch (Exception e) {
            while (!executionStack.isEmpty()) {
                Handler handler = (Handler) executionStack.pop();
                handler.revoke(msgctx);
            }
            throw AxisFault.makeFault(e);
        }
    }

    public void revoke(MessageContext msgctx) {
        for (int i = handlers.size() - 1; i > -1; i--) {
            Handler handler = (Handler) handlers.get(i);
            if (handler != null) {
                handler.revoke(msgctx);
            }
        }
    }

    /**
     * @return Returns the name.
     */
    public String getPhaseName() {
        return phaseName;
    }

    /**
     * @param phaseName The name to set.
     */
    public void setName(String phaseName) {
        this.phaseName = phaseName;
    }
}
