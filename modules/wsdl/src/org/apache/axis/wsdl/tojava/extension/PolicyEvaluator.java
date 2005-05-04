package org.apache.axis.wsdl.tojava.extension;

import org.apache.axis.wsdl.tojava.CodeGenConfiguration;


/**
 * @author chathura@opensource.lk
 *
 */
public class PolicyEvaluator implements CodeGenExtention{
	
	CodeGenConfiguration configuration;

	public PolicyEvaluator() {
	}
	
	/**
	 * Go through the WSDL and extract the WS-Policy elements
	 * and map it into a Axis Module. 
	 *
	 */
	public void init( CodeGenConfiguration configuration){
		this.configuration = configuration;		
	}
	
	public void engage(){
		
		
	}
	
}
