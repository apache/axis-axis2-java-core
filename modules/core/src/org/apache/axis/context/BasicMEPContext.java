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

	public BasicMEPContext() {
		super();
		messageContextList = new ArrayList();
	}

	public BasicMEPContext(AxisOperation axisOperation) {
		this();
		this.axisOperation = axisOperation;

	}

	/**
	 * 
	 * When a new message is added to the <code>MEPContext</code> the logic
	 * should be included remove the MEPContext from the MEPMAp of the
	 * <code>AxisOperation</code>. Example: IN_IN_OUT At the second IN
	 * message the MEPContext should be removed from the AxisOperation
	 * 
	 * @param ctxt
	 */
	public void addMessageContext(MessageContext ctxt) {
		if (WSDLConstants.MEP_URI_IN_ONLY.equals(this.axisOperation
				.getMessageExchangePattern()))
			//    		this.axisOperation.removeMEPContext(this);
			messageContextList.add(ctxt);

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

		throw new AxisFault(" Message doennot exist in the current MEP : Invalid MessageID :" + messageID);
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
}