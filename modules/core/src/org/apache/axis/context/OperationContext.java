package org.apache.axis.context;

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

import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.wsdl.WSDLConstants;

/**
 * An OperationContext represents a running "instance" of an operation. This
 * particular implementation of OperationContext supports MEPs which have one
 * input message and/or one output message. That is, it supports the all the
 * MEPs that are in the WSDL 2.0 specification.
 */

public class OperationContext extends AbstractContext {
	
	private MessageContext inMessageContext;
	
	private MessageContext outMessageContext;

    private String MepId;

    private AxisOperation axisOperation;

    public OperationContext(AxisOperation axisOperation,
                            ServiceContext serviceContext) {
        super(serviceContext);
        this.axisOperation = axisOperation;
    }




    public void addMessageContext(String messageLabel, MessageContext msgContext)
            throws AxisFault {
    	
    	if(WSDLConstants.MESSAGE_LABLE_IN.equals(messageLabel)){
    		if(null == this.inMessageContext){
    			this.inMessageContext = msgContext;
    			this.getServiceContext().getEngineContext().registerOperationContext
					(msgContext.getMessageID(), this);
    			return;
    		}    	
    		throw new AxisFault("Message: messageID "+msgContext.getMessageID()+" is inconsistent " +
    				"with the MEP. MEP does not" +
    				" associate with two In Messages");
    	}else if(WSDLConstants.MESSAGE_LABLE_OUT.equals(messageLabel)){
    		if(null == this.outMessageContext){
    			this.outMessageContext = msgContext;
    			this.getServiceContext().getEngineContext().registerOperationContext
					(msgContext.getMessageID(), this);
    			return;
    		}
    		throw new AxisFault("Message messageID: "+msgContext.getMessageID()+" is inconsistent " +
    				"with the MEP. MEP does not" +
    				" associate with two Out Messages");
    	}
    	
    	throw new AxisFault("MessageLable :"+messageLabel+" is not supported in the OperationContext" +
    			" implementation. Only "+WSDLConstants.MESSAGE_LABLE_IN+" and "+WSDLConstants.MESSAGE_LABLE_OUT+
				" are the only known messageLables of the OperationContext implemantation");
    }

    /**
     * @return Returns the mepId.
     */
    public String getMepId() {
        return MepId;
    }

    /**
     * @param mepId
     *            The mepId to set.
     */
    public void setMepId(String mepId) {
        MepId = mepId;
    }

    /**
     * Chathura, please implement this method to return the last in message of
     * the MEP. I want this for addressing - Chinthaka
     *
     * @return
     */
    public MessageContext getLastInMessageContext() {
        throw new UnsupportedOperationException();

    }

    /**
     * Removes the pointers to this <code>OperationContext</code>
     * in the <code>EngineContext</code>'s OperationContextMap so 
     * that this  <code>OperationContext</code> will eventually get garbage
     * collected along with the <code>MessageContext</code>s it
     * contain.
     * @throws AxisFault If the MEP is not Complete.
     */
    public void cleanup() throws AxisFault {
    	if(!this.isComplete()){
    		throw new AxisFault("Illegal attempt to drop the global " +
    				"reference of an incomplete MEPContext");    		
    	}
    	
    	if(null != this.inMessageContext){
    		this.getServiceContext().getEngineContext().getOperationContextMap().
				remove(this.inMessageContext.getMessageID());
    	}
    	if(null != this.outMessageContext){
    		this.getServiceContext().getEngineContext().getOperationContextMap().
				remove(this.outMessageContext.getMessageID());
    	}
    	
    }
    
    
    /**
     * Checks to see if the MEP is complete. i.e. whether 
     * all the messages that are associated with the MEP 
     * has arrived and MEP is complete. 
     * @return
     */
    public boolean isComplete() {
    	if (WSDLConstants.MEP_URI_IN_OUT.equals
				(this.axisOperation.getMessageExchangePattern()) 
			|| WSDLConstants.MEP_URI_OUT_IN.equals
				(this.axisOperation.getMessageExchangePattern())) {
		    if (null != this.inMessageContext && null != this.outMessageContext)
		        return true;
    	} else if (WSDLConstants.MEP_URI_IN_ONLY.equals
    			(this.axisOperation.getMessageExchangePattern())) {
    		if (null != this.inMessageContext)
    			return true;
    	} else if(WSDLConstants.MEP_URI_OUT_ONLY.equals
    			(this.axisOperation.getMessageExchangePattern())){
    		if(null != this.outMessageContext)
    			return true;
    	}

        return false;
    }
    public ServiceContext getServiceContext(){
        return (ServiceContext)super.parent;
    }
	/**
	 * @return Returns the axisOperation.
	 */
	public AxisOperation getAxisOperation() {
		return axisOperation;
	}
}