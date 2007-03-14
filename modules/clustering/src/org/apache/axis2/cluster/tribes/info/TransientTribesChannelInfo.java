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

package org.apache.axis2.cluster.tribes.info;

import java.io.Serializable;

import org.apache.axis2.cluster.tribes.CommandType;
import org.apache.axis2.cluster.tribes.context.ContextCommandMessage;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;

public class TransientTribesChannelInfo implements ChannelListener {

	private long messageCount = 0;

	private long grpCtxCreationCount = 0;

	private long srvCtxCreationCount = 0;

	private long grpCtxRemoveCount = 0;

	private long srvCtxRemoveCount = 0;

	private long updateStateCount = 0;

	public boolean accept(Serializable msg, Member sender) {
		return msg instanceof String;
	}

	public void messageReceived(Serializable msg, Member sender) {
		messageCount++;

		System.out.println("Tribes message " + msg);

		if (msg instanceof ContextCommandMessage) {
			ContextCommandMessage comMsg = (ContextCommandMessage) msg;

			if (comMsg.getCommandType()==CommandType.CREATE_SERVICE_GROUP_CONTEXT) {
				grpCtxCreationCount++;
			} else if (comMsg.getCommandType()==CommandType.CREATE_SERVICE_CONTEXT) {
				srvCtxCreationCount++;
			} else if (comMsg.getCommandType()==CommandType.REMOVE_SERVICE_GROUP_CONTEXT) {
				grpCtxRemoveCount++;
			} else if (comMsg.getCommandType()==CommandType.REMOVE_SERVICE_CONTEXT) {
				srvCtxRemoveCount++;
			} else if (comMsg.getCommandType()==CommandType.UPDATE_STATE) {
				updateStateCount++;
			}
		}
	}

	public long getGrpCtxCreationCount() {
		return grpCtxCreationCount;
	}

	public long getGrpCtxRemoveCount() {
		return grpCtxRemoveCount;
	}

	public long getMessageCount() {
		return messageCount;
	}

	public long getSrvCtxCreationCount() {
		return srvCtxCreationCount;
	}

	public long getSrvCtxRemoveCount() {
		return srvCtxRemoveCount;
	}

	public long getUpdateStateCount() {
		return updateStateCount;
	}

}
