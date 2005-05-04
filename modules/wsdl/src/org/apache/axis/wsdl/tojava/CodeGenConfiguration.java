package org.apache.axis.wsdl.tojava;

import java.io.File;

import org.apache.wsdl.WSDLDescription;

/**
 * @author chathura@opensource.lk
 *  
 */
public class CodeGenConfiguration implements CommandLineOptionConstants {

	private WSDLDescription wom;

	private CommandLineOptionParser parser;

	private File outputLocation;

	/**
	 * @param wom
	 * @param parser
	 */
	public CodeGenConfiguration(WSDLDescription wom,
			CommandLineOptionParser parser) {
		this.wom = wom;
		this.parser = parser;
		String outputLocation = ((CommandLineOption) parser.getAllOptions().get(
				OUTPUT_LOCATION_OPTION)).getOptionValue();

		this.outputLocation = new File(outputLocation);

	}

	/**
	 * @return Returns the parser.
	 */
	public CommandLineOptionParser getParser() {
		return parser;
	}

	/**
	 * @return Returns the wom.
	 */
	public WSDLDescription getWom() {
		return wom;
	}
	
	
	/**
	 * @return Returns the outputLocation.
	 */
	public File getOutputLocation() {
		return outputLocation;
	}
}