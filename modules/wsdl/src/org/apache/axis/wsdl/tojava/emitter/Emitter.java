package org.apache.axis.wsdl.tojava.emitter;

import org.apache.axis.wsdl.tojava.CodeGenConfiguration;
import org.apache.axis.wsdl.tojava.CodeGenerationException;

/**
 * @author chathura@opensource.lk
 *
 */
public interface Emitter {
	
	public void setCodeGenConfiguration(CodeGenConfiguration configuration);	
	
	public void emitStub() throws CodeGenerationException;
	
	public void emitSkeleton() throws CodeGenerationException;
	

}
