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

import java.util.ArrayList;
import java.util.List;

import org.apache.axis.description.AxisOperation;
import org.apache.axis.engine.AxisFault;
import org.apache.wsdl.WSDLConstants;

/**
 * @author chathura@opensource.lk
 *
 */

/**
 * This class will provide the functionality to support the two basic MEPs
 * IN_OUT IN_ONLY.
 * 
 * @author chathura@opensource.lk
 *  
 */

public class BasicMEPContext extends AbstractContext implements MEPContext {

	private ArrayList messageContextList;

	private String MepId;

	private AxisOperation axisOperation;

	public BasicMEPContext(AxisOperation axisOperation) {
		this.axisOperation = axisOperation;
        messageContextList = new ArrayList();
	}

	/**
	 * 
	 * When a new message is added to the <code>MEPContext</code> the logic
	 * should be included remove the MEPContext from the table in the
	 * <code>EngineContext</code>. Example: IN_IN_OUT At the second IN
	 * message the MEPContext should be removed from the AxisOperation
	 * 
	 * @param ctxt
	 */
	public void addMessageContext(MessageContext msgContext) throws AxisFault {
		if (WSDLConstants.MEP_URI_IN_ONLY.equals(this.axisOperation.getMessageExchangePattern())){
			messageContextList.add(msgContext);				
		} else if(WSDLConstants.MEP_URI_IN_OUT.equals(this.axisOperation.getMessageExchangePattern())){
			messageContextList.add(msgContext);			
		}
		
		if(this.isComplete())
			msgContext.getEngineContext().removeMEP(this);

	}

	/**
	 * 
	 * @param messageId
	 * @return
	 */
	public MessageContext getMessageContext(int index) {
		return (MessageContext) messageContextList.get(index);
	}

	public MessageContext removeMessageContext(MessageContext ctxt) {
		messageContextList.remove(ctxt.getMessageID());
		return ctxt;
	}

	public List getAllMessageContexts() {
		return this.messageContextList;
	}

	public MessageContext getMessageContext(String messageID) throws AxisFault {
		if (null != messageID) {
			for (int i = 0; i < this.messageContextList.size(); i++) {
				if (messageID.equals(((MessageContext) (this.messageContextList
						.get(i))).getMessageID())) {
					return ((MessageContext) (this.messageContextList.get(i)));
				}
			}
		}

		throw new AxisFault(" Message doesnot exist in the current MEP : Invalid MessageID :" + messageID);
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
	
	public boolean isComplete(){
		if (WSDLConstants.MEP_URI_IN_ONLY.equals(this.axisOperation.getMessageExchangePattern())){
			if(1 == this.messageContextList.size())
				return true;
		}else if(WSDLConstants.MEP_URI_IN_OUT.equals(this.axisOperation.getMessageExchangePattern())){
			if(2 == this.messageContextList.size())
				return true;
		}
		
		return false;
	}
}