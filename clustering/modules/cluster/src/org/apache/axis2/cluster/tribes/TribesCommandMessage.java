package org.apache.axis2.cluster.tribes;

import java.io.Serializable;


public class TribesCommandMessage implements CommandConstants, Serializable{
	
	private String commandName;
	private String parentId;
	private String contextId;
	private String axisDescriptionName;
	
	public TribesCommandMessage(){	
	}

	public TribesCommandMessage(String commandName, String parentId, String contextId, String axisDescriptionName) {
		this.commandName = commandName;
		this.parentId = parentId;
		this.contextId = contextId;
		this.axisDescriptionName = axisDescriptionName;
	}
	
	public String getCommandName() {
		return commandName;
	}

	public void setCommandName(String commandName) {
		this.commandName = commandName;
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
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("TribesCommandMessage [");
		builder.append(commandName + ",");
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
}
