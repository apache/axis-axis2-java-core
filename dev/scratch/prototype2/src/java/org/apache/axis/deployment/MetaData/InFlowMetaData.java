package org.apache.axis.deployment.MetaData;

import java.util.Vector;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 21, 2004
 *         11:25:27 AM
 *
 */
public class InFlowMetaData implements FlowMetaData {

    private Vector handlers = new Vector();
    private int handlercount = 0;

    public InFlowMetaData() {
        handlers.removeAllElements();
    }

    /**
     *
     * @param handler
     */

    public void addHandler(HandlerMetaData handler) {
        handlers.add(handler);
        handlercount++;
    }

    public int getHandlercount() {
        return handlercount;
    }

    public HandlerMetaData getHandler(int index) {
        if (index <= handlercount) {
            return (HandlerMetaData) handlers.get(index);
        } else
            return null;
    }
}
