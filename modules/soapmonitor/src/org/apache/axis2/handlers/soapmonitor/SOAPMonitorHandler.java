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

package org.apache.axis2.handlers.soapmonitor;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.handlers.AbstractHandler;

import javax.xml.namespace.QName;
import org.apache.axis2.soapmonitor.servlet.SOAPMonitorConstants;
import org.apache.axis2.soapmonitor.servlet.SOAPMonitorService;
import org.apache.axis2.addressing.EndpointReference;

public class SOAPMonitorHandler extends AbstractHandler {

    private QName name;

    private static long next_message_id = 1;

    /**
     * Constructor
     */
    public SOAPMonitorHandler() {
      super();
    }
 
    public QName getName() {
        return name;
    }

    public void revoke(MessageContext msgContext) {
        ;
    }

    public void setName(QName name) {
        this.name = name;
    }


  /**
   * Process and SOAP message
   */
  public void invoke(MessageContext messageContext) throws AxisFault {

    EndpointReference ref = null;

    // Get id, type and content
    Long    id;
    Integer type;
    // 'soap request' must be called first
    if (messageContext.getFLOW() == MessageContext.IN_FLOW) {
        // show soap message inside the 'soap request' pane in the applet
        id = assignMessageId(messageContext);
        type = new Integer(SOAPMonitorConstants.SOAP_MONITOR_REQUEST);
        ref = messageContext.getTo();
    } else { 
        id = getMessageId(messageContext);
        // show soap message inside the 'soap response' pane in the applet
        type = new Integer(SOAPMonitorConstants.SOAP_MONITOR_RESPONSE);
        ref = messageContext.getFrom();
    }

    String target = ref.getAddress();
    // Check for null target
    if (target == null) {
        target = "";
    }

    // Get the SOAP portion of the message
    String  soap = null;
    if (messageContext.getEnvelope() != null) {
      soap = messageContext.getEnvelope().toString();
    }
    // If we have an id and a SOAP portion, then send the
    // message to the SOAP monitor service
    if ((id != null) && (soap != null)) {
      SOAPMonitorService.publishMessage(id,type,target,soap);
    }
  }

  /**
   * Assign a new message id
   */
  private Long assignMessageId(MessageContext messageContext) {
    Long id = null;
    synchronized(SOAPMonitorConstants.SOAP_MONITOR_ID) {
      id = new Long(next_message_id);
      next_message_id++;
    }
    messageContext.getOperationContext().setProperty(
            SOAPMonitorConstants.SOAP_MONITOR_ID, id);
    return id;
  }

  /**
   * Get the already assigned message id
   */
  private Long getMessageId(MessageContext messageContext) {
    Long id = null;
    id = (Long) messageContext.getOperationContext().getProperty(
            SOAPMonitorConstants.SOAP_MONITOR_ID);
    return id;
  }
}
