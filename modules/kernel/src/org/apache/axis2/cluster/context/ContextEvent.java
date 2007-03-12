package org.apache.axis2.cluster.context;

public class ContextEvent {
	String contextID;
	String parentContextID;
	int contextType;
	String descriptionID = null;
	

	public int getContextType() {
		return contextType;
	}
	public void setContextType(int contextType) {
		this.contextType = contextType;
	}
	public String getContextID() {
		return contextID;
	}
	public void setContextID(String contextID) {
		this.contextID = contextID;
	}
	public String getDescriptionID() {
		return descriptionID;
	}
	public void setDescriptionID(String descriptionID) {
		this.descriptionID = descriptionID;
	}
	public String getParentContextID() {
		return parentContextID;
	}
	public void setParentContextID(String parentContextID) {
		this.parentContextID = parentContextID;
	}

	
	
}
