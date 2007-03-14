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

package org.apache.axis2.cluster.tribes.context;

import org.apache.axis2.cluster.tribes.CommandMessage;

public class ContextCommandMessage extends CommandMessage {

	private String parentId;

	private String contextId;

	private String axisDescriptionName;

	private int contextType;

	public ContextCommandMessage(int commandType, String parentId, String contextId,
			String axisDescriptionName, int contextType) {
		super (commandType);
		this.parentId = parentId;
		this.contextId = contextId;
		this.axisDescriptionName = axisDescriptionName;
		this.contextType = contextType;
	}

	public String getContextId() {
		return contextId;
	}

	public void setContextId(String contextId) {
		this.contextId = contextId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TribesCommandMessage [");
		builder.append(getCommandType() + ",");
		builder.append(parentId + ",");
		builder.append(contextId + ",");
		builder.append(axisDescriptionName + "]");

		return builder.toString();
	}

	public String getAxisDescriptionName() {
		return axisDescriptionName;
	}

	public void setAxisDescriptionName(String axisDescriptionName) {
		this.axisDescriptionName = axisDescriptionName;
	}

	public int getContextType() {
		return contextType;
	}

	public void setContextType(int contextType) {
		this.contextType = contextType;
	}

}
