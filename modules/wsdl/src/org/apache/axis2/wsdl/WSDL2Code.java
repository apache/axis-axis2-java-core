package org.apache.axis2.wsdl;

import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis2.wsdl.codegen.CommandLineOptionParser;

 /**  
 * Copyright 2004,2005 The Apache Software Foundation.
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
public class WSDL2Code {
	
	
	public static void main(String[] args) throws Exception{
		CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(args);
		validateCommandLineOptions(commandLineOptionParser);
		new CodeGenerationEngine(commandLineOptionParser).generate();
		
	}
	
	private static void printUsage(){
		System.out.println("Usage WSDL2Code -uri <Location of WSDL> :WSDL file location ");
		System.out.println("-o <output Location> : output file location ");
		System.out.println("-a : Generate async style code only. Default if off");
		System.out.println("-s : Generate sync style code only. Default if off. takes precedence over -a");
		System.out.println("-p <package name> : set custom package name");
		System.out.println("-l <language> : valid languages are java and csharp. Default is java");
		System.out.println("-t : Generate TestCase to test the generated code");
		System.out.println("-ss : Generate server side code (i.e. skeletons).Default is off");
		System.out.println("-sd : Generate service descriptor (i.e. axis2.xml).Default is off.Valid with -ss ");
		System.exit(0);
	}
	
	
	
	private static void validateCommandLineOptions(CommandLineOptionParser parser){
		if(parser.getInvalidOptions().size()>0)
			printUsage();
		if(null == parser.getAllOptions().get(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION))
			printUsage();
	}
	
	

}
