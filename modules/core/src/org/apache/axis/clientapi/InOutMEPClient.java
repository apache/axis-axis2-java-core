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
package org.apache.axis.clientapi;

import org.apache.axis.Constants;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.description.AxisOperation;

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class InOutMEPClient extends MEPClient{

    private String senderTransport = Constants.TRANSPORT_HTTP;
    private String Listenertransport = Constants.TRANSPORT_HTTP;

    private boolean useSeparateListener = false;

    //variables use for internal implementations
    private ListenerManager listenerManager;
    private CallbackReceiver callbackReceiver;

    public InOutMEPClient(ServiceContext service) {
        //service context has the engine context set in to it !
    }

    public MessageContext invokeBocking(AxisOperation axisop, MessageContext msgctx) {
        return null;
    }

    public MessageContext invokeNonBocking(
        AxisOperation axisop,
        MessageContext msgctx,
        Callback callback) {
        return null;
    }

}
