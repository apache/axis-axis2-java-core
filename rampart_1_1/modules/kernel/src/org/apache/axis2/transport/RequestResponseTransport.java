/*
* Copyright 2006 The Apache Software Foundation.
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

package org.apache.axis2.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;

/**
 * This interface represents a control object for a Request/Response transport.
 * The normal flow of Axis2 is rooted at the transport -- this does not
 * allow for an acknowledgement to be transmitted before processing has
 * completed, nor does it allow for processing to be paused and resumed
 * on a separate thread without having a response be sent back.  This interface
 * enables both of those scenarios by allowing the transport to expose
 * controls to the rest of the engine via a callback.     
 */
public interface RequestResponseTransport
{
  /*This is the name of the property that is to be stored on the
    MessageContext*/
  public static final String TRANSPORT_CONTROL
    = "RequestResponseTransportControl";

  public void acknowledgeMessage(MessageContext msgContext) throws AxisFault;
  
  //public void suspendOnReturn(MessageContext msgContext);
  //public void processResponse(MessageContext msgContext);
}
