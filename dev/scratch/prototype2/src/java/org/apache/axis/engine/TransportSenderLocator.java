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

package org.apache.axis.engine;

import org.apache.axis.context.MessageContext;
import org.apache.axis.impl.transport.http.TCPTrasnportSender;


/**
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class TransportSenderLocator {
    public static final String TRANSPORT_TCP = "TRANSPORT_TCP";
    
    public static TransportSender locateTransPortSender(MessageContext msgContext){
        String type= (String)msgContext.getProperty(MessageContext.TRANSPORT_TYPE);
                               
        if( TransportSenderLocator.TRANSPORT_TCP.equals(type)){
            return new TCPTrasnportSender();
        }
        return null;
    }
}
