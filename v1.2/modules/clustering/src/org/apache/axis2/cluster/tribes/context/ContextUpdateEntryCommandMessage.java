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

import java.io.Serializable;

import org.apache.axis2.cluster.CommandType;


public class ContextUpdateEntryCommandMessage extends ContextCommandMessage {

	public static short ADD_OR_UPDATE_ENTRY = 0;

	public static short REMOVE_ENTRY = 1;

	public static short SERVICE_GROUP_CONTEXT = 0;

	public static short SERVICE_CONTEXT = 1;

	String key;

	Serializable value;

	short operation;

	short ctxType;

	public short getOperation() {
		return operation;
	}

	public short getCtxType() {
		return ctxType;
	}

	public void setCtxType(short ctxType) {
		this.ctxType = ctxType;
	}

	public void setOperation(short operation) {
		this.operation = operation;
	}

	public ContextUpdateEntryCommandMessage(String parentId,
			String contextId, String axisDescriptionName, String key,
			Serializable value, short ctxType, short operation) {
		super(CommandType.UPDATE_STATE_MAP_ENTRY, parentId, contextId, axisDescriptionName, ctxType);
		this.key = key;
		this.value = value;
		this.operation = operation;
		this.ctxType = ctxType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Serializable getValue() {
		return value;
	}

	public void setValue(Serializable value) {
		this.value = value;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TribesCommandMessage [");
		builder.append(this.getCommandType() + ",");
		builder.append(this.getParentId() + ",");
		builder.append(this.getContextId() + ",");
		builder.append(this.getOperation() == ADD_OR_UPDATE_ENTRY ? "ADD_OR_UPDATE_ENTRY"
						: "REMOVE_ENTRY" + ",");
		builder.append(this.getCtxType() == SERVICE_GROUP_CONTEXT ? "SERVICE_GROUP_CONTEXT"
						: "SERVICE_CONTEXT" + "]");

		return builder.toString();
	}

}
