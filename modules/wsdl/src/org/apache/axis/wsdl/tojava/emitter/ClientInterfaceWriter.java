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
import org.apache.wsdl.WSDLConstants;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;

/**
 * @author chathura@opensource.lk
 *  
 */
public class ClientInterfaceWriter implements ClassWriterConstants {

	private WSDLInterface womInterface;

	private File directory ;

	private TypeMapper typeMapper;
	
	

	
	public ClientInterfaceWriter(WSDLInterface womInterface, File directory,
			TypeMapper typeMapper) {
		
		this.womInterface = womInterface;
		this.directory = directory;
		this.typeMapper = typeMapper;
	}
	public void emit() throws IOException{
		String name = womInterface.getName().getLocalPart();
		OutputStream out = new FileOutputStream(new File(directory, name+CLASS_FILE_EXTENSION));
		
		PrintStream printStream = new PrintStream(out);
		printStream.println(PUBLIC_INTERFACE
				+ name
				+ " extends "+REMOTE_INTERFACE+ "{");
		printStream.println();
		Iterator iterator = womInterface.getAllOperations().values()
				.iterator();
		while (iterator.hasNext()) {
			
			//FIXME Handle the multipart as multiple arguments.
			WSDLOperation operation = (WSDLOperation) iterator.next();
			if (WSDLConstants.MEP_URI_IN_OUT.equals(operation.getMessageExchangePattern())) {
                Class outTypeMapping = this.typeMapper.getTypeMapping(operation.getOutputMessage().getElement());
                Class inTypeMapping = this.typeMapper.getTypeMapping(operation.getInputMessage().getElement());
                printStream.println( INDENDATION_TAB+"public "
						+ outTypeMapping==null?"void" :outTypeMapping.getName()
						+" "+ operation.getName().getLocalPart()+"("
                       	+inTypeMapping==null?"":(inTypeMapping.getName()
						+" "+this.typeMapper.getParameterName(operation.getInputMessage().getElement()))
						+") throws "+ REMOTE_EXCEPTION+";");
				printStream.println();
				
			}
		}
		printStream.println("}");
		printStream.flush();
		printStream.close();

	}

}