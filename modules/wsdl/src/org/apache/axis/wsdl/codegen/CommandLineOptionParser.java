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

package org.apache.axis.wsdl.codegen;

import java.util.*;

/**
 * @author chathura@opensource.lk
 *  
 */
public class CommandLineOptionParser implements CommandLineOptionConstants {

	private Map commandLineOptions;

	public CommandLineOptionParser(String[] args) {
		this.commandLineOptions = this.parse(args);

	}

	/**
	 * Return a list with <code>CommandLineOption</code> objects
	 * 
	 * @param args
	 * @return CommandLineOption List
	 */
private Map parse(String[] args){
		Map commandLineOptions = new HashMap();
		
		if(0 == args.length)
			return commandLineOptions;
		
		//State 0 means started
		//State 1 means earlier one was a new -option
		//State 2 means earlier one was a sub param of a -option
		
		int state = 0;
		ArrayList optionBundle = null;
		String optionType = null;
		CommandLineOption commandLineOption ;
		
		for(int i=0; i< args.length ; i++){ 	
			
			if(args[i].substring(0,1).equals("-")){
				if(0 == state){
					// fresh one
					state = 1;
					optionType = args[i];
				}else if(2 == state || 1 == state){
					// new one but old one should be saved
					commandLineOption = new CommandLineOption(optionType, optionBundle); 
					commandLineOptions.put(commandLineOption.getType(), commandLineOption);
					state = 1;
					optionType = args[i];
					optionBundle = null;
					
				}			
			}else{
				if(0 == state){
					commandLineOption = new CommandLineOption(CommandLineOptionConstants.SOLE_INPUT, args);
					commandLineOptions.put(commandLineOption.getType(), commandLineOption);
					return commandLineOptions;
					
				}else if(1 == state){
					optionBundle = new ArrayList();
					optionBundle.add(args[i]);
					state =2;
					
				}else if(2 == state){
					optionBundle.add(args[i]);
				}
				
			}
			
			
		}		
		
		commandLineOption = new CommandLineOption(optionType, optionBundle); 
		commandLineOptions.put(commandLineOption.getType(), commandLineOption);
		return commandLineOptions;

	}
	public Map getAllOptions() {
		return this.commandLineOptions;
	}

	public List getInvalidOptions() {
		List faultList = new ArrayList();
		Iterator iterator = this.commandLineOptions.values().iterator();
		while (iterator.hasNext()) {
			CommandLineOption commandLineOption = ((CommandLineOption) (iterator
					.next()));
			if (commandLineOption.isInvalid()) {
				faultList.add(commandLineOption);
			}
		}

		return faultList;
	}

}