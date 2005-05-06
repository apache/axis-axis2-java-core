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

package org.apache.axis.wsdl.tojava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author chathura@opensource.lk
 *
 */
public class CommandLineOptionParser implements CommandLineOptionConstants{


	private Map commandLineOptions;
	
	public CommandLineOptionParser(String[] args){
		this.commandLineOptions = this.parse(args);
		
	}
	
	/**
	 * Return a list with <code>CommandLineOption</code> objects
	 * @param args
	 * @return CommandLineOption List
	 */
	private Map parse(String[] args){
		Map commandLineOptions = new HashMap();
		for(int i=0; i< args.length; i= i+2){
			CommandLineOption commandLineOption = new CommandLineOption(args[i], args[i+1]);
			commandLineOptions.put(commandLineOption.getType(), commandLineOption);
		}		
		return commandLineOptions;

	}
	
	
	public Map getAllOptions(){
		return this.commandLineOptions;
	}
	
	public List getInvalidOptions(){
		List faultList = new ArrayList();
		Iterator iterator = this.commandLineOptions.values().iterator();
		while (iterator.hasNext()){		
			CommandLineOption commandLineOption = ((CommandLineOption)(iterator.next()));
			if(commandLineOption.isInvalid()){
				faultList.add(commandLineOption);
			}
		}
		
		return faultList;
	}
	
	
}
