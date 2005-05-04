package org.apache.axis.wsdl.tojava.extension;

import org.apache.axis.wsdl.tojava.CodeGenConfiguration;


/**
 * @author chathura@opensource.lk
 *
 */
public interface CodeGenExtention {
	
	
	public void init(CodeGenConfiguration configuration);
	
	public void engage();
	
	

}
