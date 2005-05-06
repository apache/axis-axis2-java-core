/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.axis.wsdl.tojava.emitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import org.apache.axis.wsdl.databinding.TypeMapper;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;

/**
 * @author chathura@opensource.lk
 *
 */
public class ClientStubWriter  implements ClassWriterConstants{
	
	private WSDLInterface womInterface;
	
	private File directory;
	
	private TypeMapper typeMapper;
	
	

	/**
	 * @param wsdlInterface
	 * @param directory
	 * @param typeMapper
	 */
	public ClientStubWriter(WSDLInterface wsdlInterface, File directory,
			TypeMapper typeMapper) {
		this.womInterface = wsdlInterface;
		this.directory = directory;
		this.typeMapper = typeMapper;
	}
	
	public void emit() throws IOException{
		
		String name = womInterface.getName().getLocalPart();
		name += "Stub";
		OutputStream out = new FileOutputStream(new File(directory, name+CLASS_FILE_EXTENSION));
		
		PrintStream printStream = new PrintStream(out);
		printStream.println(PUBLIC_CLASS + INDENDATION_SPACE
				+ name
				+ " extends "
				+ ABSTRACT_STUB
				+ " implements "+REMOTE_INTERFACE+ "{");
		printStream.println();
		
		
		
		///Start of Static block
		printStream.println(INDENDATION_TAB+"static {");
		printStream.println(INDENDATION_DOUBLE_TAB+ INDENDATION_SPACE
				+AXIS_OPERATION +INDENDATION_SPACE
				+STUB_VARIABLE___OPERATION +";");
		
		Iterator iterator = womInterface.getAllOperations().values()
		.iterator();
		int operationCounter = 0;
		while (iterator.hasNext()) {
			WSDLOperation operation = (WSDLOperation) iterator.next();
			printStream.println();
			printStream.println(INDENDATION_DOUBLE_TAB + STUB_VARIABLE___OPERATION
					+INDENDATION_SPACE + "="
					+INDENDATION_SPACE + "new"
					+INDENDATION_SPACE + AXIS_OPERATION
					+"("+"}"+";");
			
			printStream.println(INDENDATION_DOUBLE_TAB
					+STUB_VARIABLE__OPERATION_ARRRAY
					+"["+operationCounter+"]"
					+INDENDATION_SPACE + "="+INDENDATION_SPACE
					+STUB_VARIABLE___OPERATION +";");
			printStream.println();
			
				
			operationCounter++;
		}
		printStream.println(INDENDATION_TAB+"}");
		///End of Static block
		printStream.println("}");
		printStream.flush();
		printStream.close();
		
		
	}
}
