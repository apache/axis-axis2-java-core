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

import org.apache.axiom.om.impl.dom.jaxp.DocumentBuilderFactoryImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.Handler;
import org.apache.rampart.RampartEngine;
import org.apache.rampart.RampartException;
import org.apache.rampart.util.Axis2Util;
import org.apache.ws.secpolicy.WSSPolicyException;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

import java.util.Vector;


public class RampartReceiver implements Handler {

    protected static final WSSecurityEngine secEngine = WSSecurityEngine.getInstance();
    
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

    public void invoke(MessageContext msgContext) throws AxisFault {
        
        if (!msgContext.isEngaged(new QName(WSSHandlerConstants.SECURITY_MODULE_NAME))) {
            return;
        }
        
        RampartEngine engine = new RampartEngine();
        Vector wsResult;
        try {
            wsResult = engine.process(msgContext);
            
            //Convert back to LLOM
            Document doc = ((Element)msgContext.getEnvelope()).getOwnerDocument();
            msgContext.setEnvelope(Axis2Util.getSOAPEnvelopeFromDOOMDocument(doc));
        } catch (WSSecurityException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } catch (WSSPolicyException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } catch (RampartException e) {
            e.printStackTrace();
            throw new AxisFault(e);
        } finally {
            // Reset the document builder factory
            DocumentBuilderFactoryImpl.setDOOMRequired(false);

        }
        
        if(wsResult == null) {
            return;
        }
        
        Vector results = null;
        if ((results = (Vector) msgContext.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
            results = new Vector();
            msgContext.setProperty(WSHandlerConstants.RECV_RESULTS, results);
        }
        WSHandlerResult rResult = new WSHandlerResult("", wsResult);
        results.add(0, rResult);
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

    public void flowComplete(MessageContext msgContext)
    {
    }

}
