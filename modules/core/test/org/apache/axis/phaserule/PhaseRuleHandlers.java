package org.apache.axis.phaserule;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.Handler;
import org.apache.axis.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

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
 * 
 */

/**
 * Author : Deepal Jayasinghe
 * Date: May 20, 2005
 * Time: 11:46:55 AM
 */
public class PhaseRuleHandlers extends AbstractHandler implements Handler {

    private Log log = LogFactory.getLog(getClass());
    private String message;
    private QName name;

    public PhaseRuleHandlers() {
        this.message = "inside service 2";
    }

    public QName getName() {
        return name;
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        System.out.println("I am " + name + " Handler Running :)");
        log.info("I am " + name + " Handler Running :)");
    }

    public void revoke(MessageContext msgContext) {
        log.info("I am " + name + " Handler Running :)");
    }

    public void setName(QName name) {
        this.name = name;
    }
}
