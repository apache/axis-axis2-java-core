package org.apache.axis2.cluster.tribes.configuration;

import java.io.Serializable;

public class ConfigurationCommand implements Serializable {
	final static int COMMAND_LOAD_SERVICE_GROUP = 1;
	final static int COMMAND_UNLOAD_SERVICE_GROUP = 2;
	final static int COMMAND_APPLY_POLICY = 3;
	final static int COMMAND_RELOAD_CONFIGURATION = 4;
	final static int COMMAND_PREPARE = 5;
	final static int COMMAND_COMMIT = 6;
	final static int COMMAND_ROLLBACK = 7;
	
	int commandType;
	String policy;
	String sgcName;
	
	public int getCommandType() {
		return commandType;
	}
	
	public void setCommandType(int commandType) {
		this.commandType = commandType;
	}
	
	public String getPolicy() {
		return policy;
	}
	
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	
	public String getSgcName() {
		return sgcName;
	}
	
	public void setSgcName(String sgcName) {
		this.sgcName = sgcName;
	}
	
}
