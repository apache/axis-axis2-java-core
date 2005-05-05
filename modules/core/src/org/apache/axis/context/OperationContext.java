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

import java.util.ArrayList;
import java.util.List;

/**
 * An OperationContext represents a running "instance" of an operation. This
 * particular implementation of OperationContext supports MEPs which have one
 * input message and/or one output message. That is, it supports the all the
 * MEPs that are in the WSDL 2.0 specification.
 */

public class OperationContext extends AbstractContext {

	private ArrayList messageContextList;

	private String MepId;

	private AxisOperation axisOperation;

	public OperationContext(AxisOperation axisOperation,
			ServiceContext serviceContext) {
		super(serviceContext);
		this.axisOperation = axisOperation;

		// Most frequently used MEPs are IN ONLY and IN-OUT MEP. So the number
		// of messagecontext for those MEPs are at most 2. Setting the initial
		// capacity of the arrayList to 2.
		messageContextList = new ArrayList(2);
	}

	/**
	 * 
	 * When a new message is added to the <code>MEPContext</code> the logic
	 * should be included remove the MEPContext from the table in the
	 * <code>EngineContext</code>. Example: IN_IN_OUT At the second IN
	 * message the MEPContext should be removed from the AxisOperation
	 * 
	 * @param msgContext
	 */
	public void addMessageContext(MessageContext msgContext) throws AxisFault {
		if (WSDLConstants.MEP_URI_IN_ONLY.equals(this.axisOperation
				.getMessageExchangePattern())) {
			messageContextList.add(msgContext);
		} else if (WSDLConstants.MEP_URI_IN_OUT.equals(this.axisOperation
				.getMessageExchangePattern())) {
			messageContextList.add(msgContext);
		}

		if (this.isComplete())
			msgContext.getEngineContext().removeMEP(this);

	}

	/**
	 * @param index
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

		throw new AxisFault(
				" Message does not exist in the current MEP : Invalid MessageID :"
						+ messageID);
	}

	public void addMessageContext(String messageLabel, MessageContext msgContext)
			throws AxisFault {
		// TODO : Chathura
		throw new UnsupportedOperationException();

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

	public void cleanup() throws AxisFault {
		//TODO Chathura
		throw new UnsupportedOperationException();
	}

	public boolean isComplete() {
		if (WSDLConstants.MEP_URI_IN_ONLY.equals(this.axisOperation
				.getMessageExchangePattern())) {
			if (1 == this.messageContextList.size())
				return true;
		} else if (WSDLConstants.MEP_URI_IN_OUT.equals(this.axisOperation
				.getMessageExchangePattern())) {
			if (2 == this.messageContextList.size())
				return true;
		}

		return false;
	}
    public ServiceContext getServiceContext(){
        return (ServiceContext)super.parent;
    }
}