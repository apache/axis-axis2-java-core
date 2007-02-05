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

package org.apache.rampart.handler;

import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.rampart.RampartEngine;
import org.apache.rampart.RampartException;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import javax.xml.namespace.QName;

import java.util.Iterator;
import java.util.Vector;


public class RampartReceiver implements Handler {
    
    private static HandlerDescription EMPTY_HANDLER_METADATA =
        new HandlerDescription("deafult Handler");

    private HandlerDescription handlerDesc;
    
    
    public RampartReceiver() {
        this.handlerDesc = EMPTY_HANDLER_METADATA;
    }
    
    public void cleanup() {        
    }

    public void init(HandlerDescription handlerdesc) {
        this.handlerDesc = handlerdesc;
    }

    public void flowComplete(MessageContext msgContext)
    {
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        
        if (!msgContext.isEngaged(new QName(WSSHandlerConstants.SECURITY_MODULE_NAME))) {
          return InvocationResponse.CONTINUE;        
        }
        
        RampartEngine engine = new RampartEngine();
        Vector wsResult;
        try {
            wsResult = engine.process(msgContext);
            
        } catch (WSSecurityException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } catch (WSSPolicyException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } catch (RampartException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } 
        
        if(wsResult == null) {
          return InvocationResponse.CONTINUE;        
        }
        
        Vector results = null;
        if ((results = (Vector) msgContext.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            results = new Vector();
            msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, results);
        }
        WSHandlerResult rResult = new WSHandlerResult("", wsResult);
        results.add(0, rResult);
        
        SOAPHeader header = null;
        try {
            header = msgContext.getEnvelope().getHeader();
        } catch (OMException ex) {
            throw new AxisFault(
                    "RampartReceiver: cannot get SOAP header after security processing",
                    ex);
        }

        Iterator headers = header.getChildElements();

        SOAPHeaderBlock headerBlock = null;

        while (headers.hasNext()) { // Find the wsse header
            SOAPHeaderBlock hb = (SOAPHeaderBlock) headers.next();
            if (hb.getLocalName().equals(WSConstants.WSSE_LN)
                    && hb.getNamespace().getNamespaceURI().equals(WSConstants.WSSE_NS)) {
                headerBlock = hb;
                break;
            }
        }

        headerBlock.setProcessed();
        return InvocationResponse.CONTINUE;        

    }

    
    public HandlerDescription getHandlerDesc() {
        return this.handlerDesc;
    }

    public String getName() {
        return "Apache Rampart inflow handler";
    }

    public Parameter getParameter(String name) {
        return this.handlerDesc.getParameter(name);
    }

}
