/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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

package org.apache.axis2.jaxws.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.activation.DataHandler;

import org.apache.axiom.attachments.Attachments;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.MTOMConstants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants.Configuration;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextImpl;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.Attachment;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.attachments.AttachmentUtils;
import org.apache.axis2.jaxws.message.impl.AttachmentImpl;
import org.apache.axis2.jaxws.util.Constants;
import org.apache.axis2.util.ThreadContextMigratorUtil;
import org.apache.axis2.wsdl.WSDLConstants.WSDL20_2004Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The JAXWSMessageReceiver is the entry point, from the server's perspective,
 * to the JAX-WS code.  This will be called by the Axis Engine and is the end
 * of the chain from an Axis2 perspective.
 */
public class JAXWSMessageReceiver implements MessageReceiver {

private static final Log log = LogFactory.getLog(JAXWSMessageReceiver.class);
    
    private static String PARAM_SERVICE_CLASS = "ServiceClass";
    
    /**
     * We should have already determined which AxisService we're targetting at
     * this point.  So now, just get the service implementation and invoke
     * the appropriate method.
     */
    public void receive(org.apache.axis2.context.MessageContext axisRequestMsgCtx) 
        throws AxisFault {
    	if (log.isDebugEnabled()) {
            log.debug("new request received");
        }
    	
    	//Get the name of the service impl that was stored as a parameter
        // inside of the services.xml.
        AxisService service = axisRequestMsgCtx.getAxisService();
        AxisOperation operation = axisRequestMsgCtx.getAxisOperation();
        String mep = operation.getMessageExchangePattern();
        if (log.isDebugEnabled()){
            log.debug("MEP: "+ mep);
        }
        
        org.apache.axis2.description.Parameter svcClassParam = service.getParameter(PARAM_SERVICE_CLASS);
        
        try {
            if (svcClassParam == null) { 
                throw new RuntimeException(Messages.getMessage("JAXWSMessageReceiverNoServiceClass"));
            }
            
            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performMigrationToThread(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
            
            //We'll need an instance of the EndpointController to actually
            //drive the invocation.
            //TODO: More work needed to determine the lifecycle of this thing
        	EndpointController endpointCtlr = new EndpointController();
          	
            MessageContext requestMsgCtx = new MessageContext(axisRequestMsgCtx);
            
            //FIXME: This should be revisited when we re-work the MTOM support.
            //This destroys performance by forcing a double pass through the message.
            //If attachments are found on the MessageContext, then that means
            //the inbound message has more than just the normal XML payload
            Attachments as = (Attachments) axisRequestMsgCtx.getProperty(MTOMConstants.ATTACHMENTS); 
            if (as != null) { 
                Message request = requestMsgCtx.getMessage();
                request.setMTOMEnabled(true);
                
                //Walk the tree and find all of the optimized binary nodes.
                ArrayList<OMText> binaryNodes = AttachmentUtils.findBinaryNodes(
                        axisRequestMsgCtx.getEnvelope());
                if (binaryNodes != null) {
                    //Replace each of the nodes with it's corresponding <xop:include>
                    //element, so JAXB can process it correctly.
                    Iterator<OMText> itr = binaryNodes.iterator();
                    while (itr.hasNext()) {
                        OMText node = itr.next();
                        OMElement xop = AttachmentUtils.makeXopElement(node);
                        node.getParent().addChild(xop);
                        node.detach();
                        
                        //We have to add the individual attachments in their raw
                        //binary form, so we can access them later.
                        Attachment a = new AttachmentImpl((DataHandler) node.getDataHandler(), 
                                node.getContentID());
                        request.addAttachment(a);
                    }
                }
            }
            

            InvocationContext ic = new InvocationContextImpl();            
            ic.setRequestMessageContext(requestMsgCtx);
            
            //TODO:Once we the JAX-WS MessageContext one of the next things that
            //needs to be done here is setting up all of the javax.xml.ws.* 
            //properties for the MessageContext.
            
            if (isMepInOnly(mep)) {
            	endpointCtlr.invoke(ic);
            }
            else{
	            ic = endpointCtlr.invoke(ic);
                
                // If this is a two-way exchange, there should already be a 
                // JAX-WS MessageContext for the response.  We need to pull 
                // the Message data out of there and set it on the Axis2 
                // MessageContext.
                MessageContext responseMsgCtx = ic.getResponseMessageContext();
                org.apache.axis2.context.MessageContext axisResponseMsgCtx = 
                    responseMsgCtx.getAxisMessageContext();                
                
                Message responseMsg = responseMsgCtx.getMessage();
                SOAPEnvelope responseEnv = (SOAPEnvelope) responseMsg.getAsOMElement();
                axisResponseMsgCtx.setEnvelope(responseEnv);
                
                if (responseMsg.isMTOMEnabled()) {
                    Options opts = axisResponseMsgCtx.getOptions();
                    opts.setProperty(Configuration.ENABLE_MTOM, "true");                    
                }
                
                OperationContext opCtx = axisResponseMsgCtx.getOperationContext();
                opCtx.addMessageContext(axisResponseMsgCtx);
            
                //This assumes that we are on the ultimate execution thread
                ThreadContextMigratorUtil.performMigrationToContext(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
                
                //Create the AxisEngine for the reponse and send it.
                AxisEngine engine = new AxisEngine(axisResponseMsgCtx.getConfigurationContext());
                engine.send(axisResponseMsgCtx);
                
                //This assumes that we are on the ultimate execution thread
                ThreadContextMigratorUtil.performContextCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisResponseMsgCtx);
            }

            //This assumes that we are on the ultimate execution thread
            ThreadContextMigratorUtil.performThreadCleanup(Constants.THREAD_CONTEXT_MIGRATOR_LIST_ID, axisRequestMsgCtx);
            
        } catch (Exception e) {
        	//TODO: This temp code for alpha till we add fault processing on client code.
        	// TODO NLS
            throw ExceptionFactory.makeWebServiceException(e);
        } 
    }
    
    private boolean isMepInOnly(String mep){
    	boolean inOnly = mep.equals(WSDL20_2004Constants.MEP_URI_ROBUST_IN_ONLY) || 
            mep.equals(WSDL20_2004Constants.MEP_URI_IN_ONLY) || 
            mep.equals(WSDL20_2004Constants.MEP_CONSTANT_ROBUST_IN_ONLY) || 
            mep.equals(WSDL20_2004Constants.MEP_CONSTANT_IN_ONLY);
        return inOnly;
    }

}
