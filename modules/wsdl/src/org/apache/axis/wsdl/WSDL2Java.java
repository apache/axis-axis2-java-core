package org.apache.axis.wsdl;

import org.apache.axis.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis.wsdl.codegen.CommandLineOptionConstants;
import org.apache.axis.wsdl.codegen.CommandLineOptionParser;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDL2Java {
	
	
	public static void main(String[] args) throws Exception{
		CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(args);
		validateCommandLineOptions(commandLineOptionParser);
		new CodeGenerationEngine(commandLineOptionParser).generate();
		
	}
	
	private static void printUsage(){
		System.out.println("Usage WSDL2Java -uri <Location of WSDL> :WSDL file location ");
		System.out.println("-o <output Location> : output file location ");
		System.out.println("-x : Switch to advanced mode. Default is off");
		System.out.println("-a : Generate async style code only. Default if off");
		System.out.println("-s : Generate sync style code only. Default if off. takes precedence over -a");
		System.out.println("-p <package name> : set custom package name");
		System.out.println("-l <language> : valid languages are java and csharp. Default is java");
		System.out.println("-ss : Generate server side code (i.e. skeletons).Default is off");
		System.out.println("-sd : Generate service descriptor (i.e. server.xml).Default is off.Valid with -ss ");
		System.exit(0);
	}
	
	
	
	private static void validateCommandLineOptions(CommandLineOptionParser parser){
		if(parser.getInvalidOptions().size()>0)
			printUsage();
		if(null == parser.getAllOptions().get(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION))
			printUsage();
	}
	
	

}
