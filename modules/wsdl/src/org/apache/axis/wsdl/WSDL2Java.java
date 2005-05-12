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
		System.out.println("Usage WSDL2Java -URI <Location of WSDL> ");
		System.out.println("-o <output Location> ");
		System.out.println("-x ");
		System.exit(0);
	}
	
	
	
	private static void validateCommandLineOptions(CommandLineOptionParser parser){
		if(parser.getInvalidOptions().size()>0)
			printUsage();
		if(null == parser.getAllOptions().get(CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION))
			printUsage();
	}
	
	

}
