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

import java.util.HashMap;

import org.apache.axis.addressing.MessageInformationHeadersCollection;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.ServiceContext;
import org.apache.axis.context.SystemContext;
import org.apache.axis.om.OMElement;

/**
 * @author hemapani
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Call1 extends InOutMEPClient{

    private MessageInformationHeadersCollection messageInfoHeaders;

    private HashMap properties;
    
    
    
    
    
    public Call1(SystemContext sysContext, ServiceContext service) {
        super(service);
     }

     public MessageContext invokeBlocking(String axisop, OMElement payload) {
         return null;
     }

     public MessageContext invokeNonBlocking(String axisop, OMElement payload,
         Callback callback) {
         return null;
     }
}
