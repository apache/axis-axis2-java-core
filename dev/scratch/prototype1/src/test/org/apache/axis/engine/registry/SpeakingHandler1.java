/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.engine.registry;

import javax.xml.namespace.QName;

import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;
import org.apache.axis.engine.context.MessageContext;

public class SpeakingHandler1 extends AbstractEngineElement implements Handler {
    private String message;
    private QName name;
    public SpeakingHandler1(String message) {
       this.message = message;
    }
    public QName getName() {
        return name;
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        System.out.println("I am " + message + " Handler Running :)");
    }

    public void revoke(MessageContext msgContext) {
        System.out.println("I am " + message + " Handler Running :)");
    }

    public void setName(QName name) {
        this.name = name;
    }

}
