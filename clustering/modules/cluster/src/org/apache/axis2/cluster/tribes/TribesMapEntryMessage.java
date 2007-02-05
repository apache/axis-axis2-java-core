package org.apache.axis2.cluster.tribes;

import java.io.Serializable;

public class TribesMapEntryMessage extends TribesCommandMessage {
	
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

	public TribesMapEntryMessage(String commandName, String parentId,
			String contextId, String axisDescriptionName,
			String key, Serializable value,short ctxType,short operation) {
		super(commandName, parentId, contextId, axisDescriptionName);
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
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("TribesCommandMessage [");
		builder.append(this.getCommandName() + ",");
		builder.append(this.getParentId() + ",");
		builder.append(this.getContextId() + ",");
		builder.append(this.getOperation() == ADD_OR_UPDATE_ENTRY ? "ADD_OR_UPDATE_ENTRY" : "REMOVE_ENTRY" + ",");
		builder.append(this.getCtxType() == SERVICE_GROUP_CONTEXT ? "SERVICE_GROUP_CONTEXT" : "SERVICE_CONTEXT" + "]");
		
		return builder.toString();
	}

}
