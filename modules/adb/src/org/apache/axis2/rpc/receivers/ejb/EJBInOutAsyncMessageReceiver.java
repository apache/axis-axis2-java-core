package org.apache.axis2.rpc.receivers.ejb;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.rpc.receivers.RPCInOutAsyncMessageReceiver;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
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

/**
 * An EJBInOutAsyncMessageReceiver
 *
 * @deprecated you can just use EJBMessageReceiver
 */
public class EJBInOutAsyncMessageReceiver extends RPCMessageReceiver {
    public void receive(final MessageContext messageCtx) throws AxisFault {
        messageCtx.setProperty(DO_ASYNC, Boolean.TRUE);
        super.receive(messageCtx);
    }

    protected Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        return EJBUtil.makeNewServiceObject(msgContext);
    }
}
