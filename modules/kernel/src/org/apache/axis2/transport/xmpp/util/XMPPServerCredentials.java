package org.apache.axis2.transport.xmpp.util;

/**
 * Holds connection details to a XMPP Server 
 * 
 */
public class XMPPServerCredentials {
	private String accountName;
	private String serverUrl;
	private String password;		
	private String serverType;
	private String resource; 
	
	public String getAccountName() {
		return accountName;
	}
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getServerType() {
		return serverType;
	}
	public void setServerType(String serverType) {
		this.serverType = serverType;
	}
	public String getResource() {
		return resource;
	}
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public XMPPServerCredentials() {
		super();
		this.accountName = "";
		this.serverUrl = "";
		this.password = "";
		this.serverType = "";
		this.resource = "soapserver"; //Default value
	}
	
	public XMPPServerCredentials(String accountName, String serverUrl,
			String password, String serverType, String resource) {
		super();
		this.accountName = accountName;
		this.serverUrl = serverUrl;
		this.password = password;
		this.serverType = serverType;
		this.resource = resource;
	}
	

}
